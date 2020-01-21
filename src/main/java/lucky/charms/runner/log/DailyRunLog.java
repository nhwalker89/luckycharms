package lucky.charms.runner.log;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import lucky.charms.portfolio.PortfolioState;
import lucky.charms.portfolio.PortfolioWorth;

public class DailyRunLog {

   public void logStocksPicked(List<String> picks) {
      // TODO Auto-generated method stub

   }

   public void logStocksToSell(Map<String, Integer> toSell) {
      // TODO Auto-generated method stub

   }

   public void logTimeWaitedForBulkSell(Duration timeWaited) {
      // TODO Auto-generated method stub

   }

   public void logStocksToBuy(Map<String, Integer> toBuy) {
      // TODO Auto-generated method stub

   }

   public void logTimeWaitedForBulkBuy(Duration timeWaited) {
      // TODO Auto-generated method stub

   }

   public void logExtraBuy(String symbol) {
      // TODO Auto-generated method stub

   }

   public void logTimeWaitedForExtraBuy(Duration timeWaited) {
      // TODO Auto-generated method stub

   }

   public void logEndOfDayPortfolio(PortfolioState state, PortfolioWorth worth) {
      // TODO Auto-generated method stub

   }

   public void logStartOfDayPortfolio(PortfolioState state, PortfolioWorth worth) {
      // TODO Auto-generated method stub

   }

}
