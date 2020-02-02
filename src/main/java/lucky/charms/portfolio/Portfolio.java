package lucky.charms.portfolio;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import lucky.charms.runner.IRunnerContext;

public interface Portfolio {

   PortfolioWorth getWorth(IRunnerContext ctx);

   PortfolioState getState();

   void sell(IRunnerContext ctx, Map<String, Integer> toSell);

   Duration waitForPendingSells(IRunnerContext ctx, Duration ofMinutes);

   void buy(IRunnerContext ctx, Map<String, Integer> toBuy);

   Duration waitForPendingBuys(IRunnerContext ctx, Duration ofMinutes);

   void save(IRunnerContext ctx) throws IOException;

   PortfolioDataSet dataset();

}
