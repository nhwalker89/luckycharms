package luckycharms.apps;

import java.io.IOException;
import java.util.List;

import luckycharms.config.StockUniverse;
import luckycharms.portfolio.FakePortfolio;
import luckycharms.runner.BacktestRunnerContext;
import luckycharms.runner.DayRunner;
import luckycharms.runner.IPicker;
import luckycharms.runner.pickers.HighestVolumePicker;
import luckycharms.time.units.DaysKey;

public class BacktestMain {

   public static void main(String[] args) throws IOException {
      List<String> symbols = StockUniverse.SP500;

      BacktestRunnerContext context = new BacktestRunnerContext(symbols, //
            DaysKey.of(2018, 01, 01), //
            DaysKey.now());
      IPicker picker = new HighestVolumePicker();
      FakePortfolio portfolio = new FakePortfolio("PortfolioTest", true);
      portfolio.addCash(10_000);
      DayRunner runner = new DayRunner(context, picker, portfolio);
      runner.run();

      System.out.println("done");
   }

}
