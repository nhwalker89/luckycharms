package lucky.charms.runner;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Iterators;
import com.google.common.collect.Range;

import lucky.charms.clock.Clock;
import lucky.charms.clock.ClockException;
import lucky.charms.portfolio.Portfolio;
import lucky.charms.portfolio.PortfolioState;
import lucky.charms.portfolio.PortfolioWorth;
import lucky.charms.portfolio.Position;
import lucky.charms.portfolio.PositionShareData;
import lucky.charms.runner.log.DailyRunLog;
import lucky.charms.runner.log.RunLog;

public class DayRunner {
   private final org.slf4j.Logger sLog = org.slf4j.LoggerFactory.getLogger(this.getClass());

   private final AtomicBoolean isRunning = new AtomicBoolean(false);
   private final RunnerContext context;
   private final IPicker picker;
   private final Portfolio portfolio;

   private RunLog runLog = null/* TODO */;
   private double reserveAmount = 200.0;
   private Duration delay = Duration.ofMinutes(45);
   private int daysToHold = 3;

   private DailyRunLog dailyLog = null;

   public DayRunner(RunnerContext context, IPicker picker, Portfolio portfolio) {
      this.context = context;
      this.picker = picker;
      this.portfolio = portfolio;
   }

   public void setReserveAmount(double reserveAmount) { this.reserveAmount = reserveAmount; }

   public void setDelay(Duration delay) { this.delay = delay.abs(); }

   public void setDaysToHold(int daysToHold) { this.daysToHold = daysToHold; }

   public void setRunLog(RunLog runLog) { this.runLog = runLog; }

   public boolean isRunning() { return isRunning.get(); }

   public void run() {
      isRunning.set(true);

      prepareToRun();

      while (isRunning()) {

         if (isRunning())
            beforeDailyExecution();

         if (isRunning())
            waitForStartTime();

         if (isRunning()) {
            beforeDailyActions();
            performDailyActions();
         }

         if (isRunning())
            waitForMarketClose();

         if (isRunning())
            afterMarketCloses();
      }
   }

   protected void prepareToRun() {
      // nothing to do
   }

   protected void beforeDailyExecution() {
      picker.beforeDailyExecution();
   }

   protected void waitForStartTime() {
      boolean alive = true;
      while (alive) {
         try {
            if (clock().isBeforeMiddleOfValidTradingDay()) {
               clock().waitUntil(clock().nextMarketOpen().plus(delay));
               clock().waitForMarketOpen();
            }

            alive = false;
         } catch (InterruptedException | ClockException e) {
            sLog.error("Problem waiting for market to open", e);
            alive = isRobust();
         }
      }
   }

   protected void waitForMarketClose() {
      boolean alive = true;
      while (alive) {
         try {
            clock().waitForMarketClose();
            alive = false;
         } catch (InterruptedException | ClockException e) {
            sLog.error("Problem waiting for market to open", e);
            alive = isRobust();
         }
      }
   }

   protected void afterMarketCloses() {
      PortfolioState state = portfolio.getState();
      PortfolioWorth worth = portfolio.getWorth();
      dailyLog.logEndOfDayPortfolio(state, worth);
   }

   protected void beforeDailyActions() {
      dailyLog = runLog.createDailyRunLog(context.today());
      PortfolioState state = portfolio.getState();
      PortfolioWorth worth = portfolio.getWorth();
      dailyLog.logStartOfDayPortfolio(state, worth);
   }

   protected void performDailyActions() {

      // Determine goal to hold
      List<String> picks = picker.pick(context);
      dailyLog.logStocksPicked(picks);

      // Determine what to sell
      Map<String, Integer> toSell = determineWhatToSell(picks);
      dailyLog.logStocksToSell(toSell);

      // Sell - wait at most 1/2 hour for pending sells to finalize
      portfolio.sell(toSell);
      Duration timeWaited = portfolio.waitForPendingSells(Duration.ofMinutes(30));
      dailyLog.logTimeWaitedForBulkSell(timeWaited);

      // Determine what to buy
      Map<String, Integer> toBuy = determineWhatToBuy(picks);
      dailyLog.logStocksToBuy(toBuy);

      // Submit buy orders - wait at most 1/2 hour for pending buys to finalize
      portfolio.buy(toBuy);
      timeWaited = portfolio.waitForPendingBuys(Duration.ofMinutes(30));
      dailyLog.logTimeWaitedForBulkBuy(timeWaited);

      // Use remaining money to buy extra shares
      double cash = portfolio.getWorth().getCash();
      boolean madeExtraBuy = true;
      while (madeExtraBuy) {
         madeExtraBuy = false;
         for (String symbol : picks) {
            Map<String, Double> currentPrice = context
                  .currentPrices(Iterators.singletonIterator(symbol));
            Double price = currentPrice.get(symbol);
            if (price == null) {
               continue;
            }
            double priceDoub = price.doubleValue();

            if (priceDoub < cash) {
               dailyLog.logExtraBuy(symbol);
               portfolio.buy(Collections.singletonMap(symbol, 1));
               timeWaited = portfolio.waitForPendingBuys(Duration.ofMinutes(15));
               dailyLog.logTimeWaitedForExtraBuy(timeWaited);
               cash = portfolio.getWorth().getCash();
               madeExtraBuy = true;
            }
         }
      }

   }

   private Map<String, Integer> determineWhatToBuy(List<String> picks) {
      Map<String, Integer> toBuy = new HashMap<>();

      PortfolioWorth worth = portfolio.getWorth();
      PortfolioState state = portfolio.getState();
      Map<String, Double> currentPrices = context.currentPrices(picks.iterator());

      double amountPerSymbol = (worth.getTotalWorth() - reserveAmount) / picks.size();
      double availCash = worth.getCash() - reserveAmount;

      for (String symbol : picks) {

         // Abort if no more money
         if (availCash <= 0) {
            break;
         }

         // Skip symbol if no current price data
         Double symbolPrice = currentPrices.get(symbol);
         if (symbolPrice == null) {
            continue;
         }

         // Desired amount to hold
         int shares = Math.toIntExact(Math.round(amountPerSymbol / symbolPrice.doubleValue()));

         // Subtract shares we already have
         Position currentlyInPortfolio = state.getPosition(symbol);
         if (currentlyInPortfolio != null) {
            shares = shares - currentlyInPortfolio.getSharesCount();
         }

         // If we want more shares, buy them
         if (shares > 0) {
            double cost = shares * symbolPrice.doubleValue();

            // Make sure we don't try to buy more than we can afford
            if (availCash < cost) {
               shares = Math.toIntExact((long) (availCash / symbolPrice.doubleValue()));
               cost = shares * symbolPrice.doubleValue();
            }
            toBuy.put(symbol, shares);
            availCash = availCash - cost;
         }
      }
      return toBuy;
   }

   private Map<String, Integer> determineWhatToSell(List<String> picks) {
      PortfolioState state = portfolio.getState();
      Map<String, Integer> toSell = new HashMap<>();
      for (Position position : state.getPositions()) {
         String symbol = position.getSymbol();
         if (!picks.contains(symbol)) {
            List<PositionShareData> positionsToSell = position
                  .getShares(Range.atMost(context.today().minus(daysToHold)));
            if (!positionsToSell.isEmpty()) {
               toSell.put(symbol, positionsToSell.size());
            }
         }
      }
      return toSell;
   }

   protected Clock clock() {
      return context.clock();
   }

   protected boolean isRobust() { return context.isRobust(); }

}
