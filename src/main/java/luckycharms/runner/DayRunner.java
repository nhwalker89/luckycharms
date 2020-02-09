package luckycharms.runner;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Iterators;

import luckycharms.clock.MarketTimeStateData;
import luckycharms.portfolio.Portfolio;
import luckycharms.portfolio.PortfolioState;
import luckycharms.portfolio.PortfolioWorth;
import luckycharms.portfolio.Position;
import luckycharms.runner.log.DailyRunLog;
import luckycharms.runner.log.RunLog;
import luckycharms.runner.log.RunLog.LoggerBasedRunLog;
import luckycharms.util.progress.ProgressGui;
import luckycharms.util.progress.ProgressManager;

public class DayRunner {
   private final org.slf4j.Logger sLog = org.slf4j.LoggerFactory.getLogger(this.getClass());

   private final AtomicBoolean isRunning = new AtomicBoolean(false);
   private final IRunnerContext context;
   private final IPicker picker;
   private final Portfolio portfolio;

   private RunLog runLog = new LoggerBasedRunLog();;
   private double reserveAmount = 200.0;
   private Duration delay = Duration.ofMinutes(45);
   private int daysToHold = 3;

   private DailyRunLog dailyLog = null;

   public DayRunner(IRunnerContext context, IPicker picker, Portfolio portfolio) {
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

      ProgressGui<ProgressManager> gui = ProgressGui.openPopup("DayRunner", true);
      gui.getProgressManager().setProgress("Initializing", 0.0f);
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

         context.clock().percentRemaining().ifPresent(val -> {
            gui.getProgressManager().setProgress(
                  context.clock().marketTimeState().today() + " Finished", (float) val);
         });

         if (!context.clock().hasMoreDays()) {
            isRunning.set(false);
         }
      }
   }

   protected void prepareToRun() {
      // nothing to do
   }

   protected void beforeDailyExecution() {
      picker.beforeDailyExecution(context);
   }

   protected void waitForStartTime() {
      boolean alive = true;
      while (alive) {
         try {

            // Wait until market open
            MarketTimeStateData data = context.clock().marketTimeState();
            if (data.isOpen() && data.isBeforeMiddleOfValidTradingDay()) {
               // Success !
            } else if (data.isOpen()) {
               context.clock().waitUntilMarketClose();
               context.clock().waitUntilOpen();
               data = context.clock().marketTimeState();
            } else {
               context.clock().waitUntilOpen();
               data = context.clock().marketTimeState();
            }

            // Wait until desired time has elapsed
            if (data.isOpen()) {
               ZonedDateTime target = data.lastMarketStateChange().plus(delay);
               context.clock().waitUntil(target);
            } else {
               sLog.error("Problem while waiting for market to open,"
                     + " expected open state but was closed");
            }
            alive = false;

         } catch (InterruptedException e) {
            sLog.error("Problem waiting for market to open", e);
            alive = isRobust();
         }
      }
   }

   protected void waitForMarketClose() {
      boolean alive = true;
      while (alive) {
         try {
            context.clock().waitUntilMarketClose();
            alive = false;
         } catch (InterruptedException e) {
            sLog.error("Problem waiting for market to open", e);
            alive = isRobust();
         }
      }
   }

   protected void afterMarketCloses() {
      try {
         portfolio.save(context);
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      PortfolioState state = portfolio.getState();
      PortfolioWorth worth = portfolio.getWorth(context);
      dailyLog.logEndOfDayPortfolio(state, worth);
      dailyLog.dayEnded();
   }

   protected void beforeDailyActions() {
      dailyLog = runLog.createDailyRunLog(context);
      dailyLog.dayStarted();

      PortfolioState state = portfolio.getState();
      PortfolioWorth worth = portfolio.getWorth(context);
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
      portfolio.sell(context, toSell);
      Duration timeWaited = portfolio.waitForPendingSells(context, Duration.ofMinutes(30));
      dailyLog.logTimeWaitedForBulkSell(timeWaited);

      // Determine what to buy
      Map<String, Integer> toBuy = determineWhatToBuy(picks);
      dailyLog.logStocksToBuy(toBuy);

      // Submit buy orders - wait at most 1/2 hour for pending buys to finalize
      portfolio.buy(context, toBuy);
      timeWaited = portfolio.waitForPendingBuys(context, Duration.ofMinutes(30));
      dailyLog.logTimeWaitedForBulkBuy(timeWaited);

      // Use remaining money to buy extra shares
      double availableBalance = portfolio.getWorth(context).getPortfolioState().getCash()
            - reserveAmount;
      boolean madeExtraBuy;
      do {
         madeExtraBuy = false;
         for (String symbol : picks) {
            Map<String, Double> currentPrice = context
                  .currentPrices(Iterators.singletonIterator(symbol), EPriceHint.HIGH);
            Double price = currentPrice.get(symbol);
            if (price == null) {
               continue;
            }
            double priceDoub = price.doubleValue();

            if (priceDoub < availableBalance) {
               dailyLog.logExtraBuy(symbol);
               portfolio.buy(context, Collections.singletonMap(symbol, 1));
               timeWaited = portfolio.waitForPendingBuys(context, Duration.ofMinutes(15));
               dailyLog.logTimeWaitedForExtraBuy(timeWaited);
               availableBalance = portfolio.getWorth(context).getPortfolioState().getCash()
                     - reserveAmount;
               madeExtraBuy = true;
            }
         }
      } while (madeExtraBuy);

   }

   private Map<String, Integer> determineWhatToBuy(List<String> picks) {
      Map<String, Integer> toBuy = new HashMap<>();

      PortfolioWorth worth = portfolio.getWorth(context);
      PortfolioState state = portfolio.getState();
      Map<String, Double> currentPrices = context.currentPrices(picks.iterator(), EPriceHint.HIGH);

      double amountPerSymbol = (worth.getTotalWorth() - reserveAmount) / picks.size();
      double availCash = worth.getPortfolioState().getCash() - reserveAmount;

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
            if (shares > 0) {
               toBuy.put(symbol, shares);
               availCash = availCash - cost;
            }
         }
      }
      return toBuy;
   }

   private Map<String, Integer> determineWhatToSell(List<String> picks) {
      PortfolioWorth worth = portfolio.getWorth(context);
      PortfolioState state = worth.getPortfolioState();

      Map<String, Integer> toSell = new HashMap<>();
      double amountPerSymbol = (worth.getTotalWorth() - reserveAmount) / picks.size();
      for (Position position : state.getPositions().values()) {
         String symbol = position.getSymbol();
         if (!picks.contains(symbol)) {
            toSell.put(symbol, position.getSharesCount());
         } else {
            OptionalDouble optSharePrice = worth.getPricePerShare(symbol);
            if (optSharePrice.isEmpty()) {
               toSell.put(symbol, position.getSharesCount());
            } else {
               double sharePrice = optSharePrice.getAsDouble();
               long target = (long) Math.ceil(amountPerSymbol / sharePrice);
               long sharesToSell = position.getSharesCount() - target;
               if (sharesToSell > 0) {
                  toSell.put(symbol, Math.toIntExact(sharesToSell));
               }
            }
         }
      }
      return toSell;
   }

   protected boolean isRobust() { return context.isRobust(); }

}
