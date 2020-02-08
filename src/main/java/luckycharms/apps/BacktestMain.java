package luckycharms.apps;

import java.io.IOException;
import java.util.List;

import lucky.charms.portfolio.FakePortfolio;
import lucky.charms.runner.BacktestRunnerContext;
import lucky.charms.runner.DayRunner;
import lucky.charms.runner.IPicker;
import lucky.charms.runner.pickers.HighestVolumePicker;
import luckycharms.config.StockUniverse;
import luckycharms.time.units.DaysKey;

public class BacktestMain {

   public static void main(String[] args) throws IOException {
      List<String> symbols = StockUniverse.SP500;
//      MarketDayDataSet.instance().update();
//      DailyPriceDataSet.update(symbols);
      BacktestRunnerContext context = new BacktestRunnerContext(symbols, DaysKey.of(2015, 01, 01),
            DaysKey.now());
      IPicker picker = new HighestVolumePicker();
      FakePortfolio portfolio = new FakePortfolio("PortfolioTest");
      portfolio.addCash(10_000);
      DayRunner runner = new DayRunner(context, picker, portfolio);
      runner.run();
   }

}
