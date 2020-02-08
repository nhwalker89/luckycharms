package lucky.charms.portfolio;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lucky.charms.runner.IRunnerContext;

public class FakePortfolio implements Portfolio {
   private static final org.slf4j.Logger sLog = org.slf4j.LoggerFactory
         .getLogger(FakePortfolio.class);

   private PortfolioState state = new PortfolioState();
   private final PortfolioDataSet dataset;
   private final String name;

   public FakePortfolio(String name) {
      this.name = name;
      this.dataset = new PortfolioDataSet(name);
   }

   @Override
   public PortfolioWorth getWorth(IRunnerContext ctx) {
      return state.computeWorth(ctx);
   }

   @Override
   public PortfolioState getState() { return state; }

   public synchronized void addCash(double cash) {
      state = state.updateCash(state.getCash() + cash);
   }

   @Override
   public synchronized void sell(IRunnerContext ctx, Map<String, Integer> toSell) {
      Map<String, Double> prices = ctx.currentPrices(toSell.keySet().iterator());

      Map<String, Integer> selling = new HashMap<>();
      double profit = 0.0;

      for (Entry<String, Integer> entry : toSell.entrySet()) {
         Double optPrice = prices.get(entry.getKey());
         if (optPrice == null) {
            sLog.error("Cannot sell {}. Price undetermined.", entry.getKey());
         } else {
            double price = optPrice.doubleValue();
            double madeMoney = entry.getValue().intValue() * price;
            profit += madeMoney;
            selling.put(entry.getKey(), entry.getValue());
         }
      }
      state = state.removePositions(selling).updateCash(state.getCash() + profit);
   }

   @Override
   public Duration waitForPendingSells(IRunnerContext ctx, Duration ofMinutes) {
      return Duration.ZERO;
   }

   @Override
   public void buy(IRunnerContext ctx, Map<String, Integer> toBuy) {
      Map<String, Double> prices = ctx.currentPrices(toBuy.keySet().iterator());

      Map<String, Integer> buys = new HashMap<>();
      double cost = 0.0;

      for (Entry<String, Integer> entry : toBuy.entrySet()) {
         Double optPrice = prices.get(entry.getKey());
         if (optPrice == null) {
            sLog.error("Cannot buy {}. Price undetermined.", entry.getKey());
         } else {
            double price = optPrice.doubleValue();
            double usedMoney = entry.getValue().intValue() * price;
            cost += usedMoney;
            buys.put(entry.getKey(), entry.getValue());
         }
      }
      state = state.addPositions(ctx.clock().marketTimeState().today(), buys)
            .updateCash(state.getCash() - cost);
      if (state.getCash() < 0) {
         throw new IllegalStateException("Cannot reduce porfolio to less than $0 cash");
      }
   }

   @Override
   public Duration waitForPendingBuys(IRunnerContext ctx, Duration ofMinutes) {
      return Duration.ZERO;
   }

   public String getName() { return name; }

   @Override
   public PortfolioDataSet dataset() {
      return dataset;
   }

   @Override
   public void save(IRunnerContext ctx) throws IOException {
      PortfolioWorth worth = state.computeWorth(ctx);
      dataset.put(ctx.clock().marketTimeState().today(), worth);
   }

}
