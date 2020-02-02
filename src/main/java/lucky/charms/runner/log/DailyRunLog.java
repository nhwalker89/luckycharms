package lucky.charms.runner.log;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import lucky.charms.portfolio.PortfolioState;
import lucky.charms.portfolio.PortfolioWorth;

public interface DailyRunLog {

   public void logStocksPicked(List<String> picks);

   public void logStocksToSell(Map<String, Integer> toSell);

   public void logTimeWaitedForBulkSell(Duration timeWaited);

   public void logStocksToBuy(Map<String, Integer> toBuy);

   public void logTimeWaitedForBulkBuy(Duration timeWaited);

   public void logExtraBuy(String symbol);

   public void logTimeWaitedForExtraBuy(Duration timeWaited);

   public void logEndOfDayPortfolio(PortfolioState state, PortfolioWorth worth);

   public void logStartOfDayPortfolio(PortfolioState state, PortfolioWorth worth);

   public void dayEnded();

   public void dayStarted();
}
