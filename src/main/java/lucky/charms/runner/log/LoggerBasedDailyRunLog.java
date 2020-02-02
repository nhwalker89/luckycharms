package lucky.charms.runner.log;

import static luckycharms.logging.log4j_Markers.REPORT;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lucky.charms.portfolio.PortfolioState;
import lucky.charms.portfolio.PortfolioWorth;
import lucky.charms.runner.IRunnerContext;
import luckycharms.util.Tabler;

public class LoggerBasedDailyRunLog implements DailyRunLog {
   private static final org.apache.logging.log4j.Logger sLog = org.apache.logging.log4j.LogManager
         .getLogger();

   private final IRunnerContext context;

   public LoggerBasedDailyRunLog(IRunnerContext ctx) {
      context = ctx;
   }

   private LocalDateTime localTime() {
      return context.clock().marketTimeState().marketDateTime();
   }

   @Override
   public void dayStarted() {
      sLog.info(REPORT, "{} Day Started", this::localTime);
   }

   @Override
   public void logStocksPicked(List<String> picks) {
      sLog.info(REPORT, "{} Picked Stocks \n{}", this::localTime, () -> {
         Tabler table = new Tabler();
         picks.forEach(table::row);
         return table.toString(4);
      });
   }

   @Override
   public void logStocksToSell(Map<String, Integer> toSell) {
      sLog.info(REPORT, "{} Selling Stocks \n{}", this::localTime, () -> {
         Tabler table = new Tabler();
         table.headers("Symbol", "Qty");
         toSell.forEach((k, v) -> table.row(k, v));
         return table.toString(3);
      });
   }

   @Override
   public void logTimeWaitedForBulkSell(Duration timeWaited) {
      sLog.info(REPORT, "{} Time Waited For Bulk Sell {}", this::localTime, () -> timeWaited);
   }

   @Override
   public void logStocksToBuy(Map<String, Integer> toBuy) {
      sLog.info(REPORT, "{} Buying Stocks \n{}", this::localTime, () -> {
         Tabler table = new Tabler();
         table.headers("Symbol", "Qty");
         toBuy.forEach((k, v) -> table.row(k, v));
         return table.toString(3);
      });
   }

   @Override
   public void logTimeWaitedForBulkBuy(Duration timeWaited) {
      sLog.info(REPORT, "{} Time Waited For Bulk Buy {}", this::localTime, () -> timeWaited);
   }

   @Override
   public void logExtraBuy(String symbol) {
      sLog.info(REPORT, "{} Extra Buy - 1 share of {}", this::localTime, () -> symbol);
   }

   @Override
   public void logTimeWaitedForExtraBuy(Duration timeWaited) {
      sLog.info(REPORT, "{} Time Waited For Extra Buy {}", this::localTime, () -> timeWaited);
   }

   @Override
   public void logEndOfDayPortfolio(PortfolioState state, PortfolioWorth worth) {
      sLog.info(REPORT, "{} End Of Day \n{}", this::localTime, worth::toString);
   }

   @Override
   public void logStartOfDayPortfolio(PortfolioState state, PortfolioWorth worth) {
      sLog.info(REPORT, "{} Start Of Day \n{}", this::localTime, worth::toString);
   }

   @Override
   public void dayEnded() {
      sLog.info(REPORT, "{} Day Ended", this::localTime);
   }

}
