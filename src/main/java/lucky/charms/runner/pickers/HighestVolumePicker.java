package lucky.charms.runner.pickers;

import java.util.List;

import com.google.common.collect.ImmutableList;

import lucky.charms.runner.IPicker;
import lucky.charms.runner.IRunnerContext;
import luckycharms.datasets.prices.DailyPriceDataSet;
import luckycharms.datasets.prices.PriceBar;
import luckycharms.storage.KeyValuePair;
import luckycharms.time.units.DaysKey;

public class HighestVolumePicker implements IPicker {

   final int picks;

   public HighestVolumePicker() {
      this(15);
   }

   public HighestVolumePicker(int picks) {
      this.picks = picks;
   }

   @Override
   public List<String> pick(IRunnerContext context) {
      List<String> symbols = context.getSymbols();
      DaysKey today = context.clock().marketTimeState().today();

      ImmutableList<String> sorted = symbols.parallelStream()//
            .map(s -> score(today, s))//
            .sorted().limit(this.picks)//
            .map(ScoredStock::symbol)//
            .collect(ImmutableList.toImmutableList());
      return sorted;
   }

   private static ScoredStock score(DaysKey today, String symbol) {
      KeyValuePair<DaysKey, PriceBar> bar = DailyPriceDataSet.instance(symbol)
            .getLastFullPriceBar(today);
      if (bar == null) {
         return new ScoredStock(symbol, Double.MIN_VALUE);
      }
      return new ScoredStock(symbol, bar.getValue().getVolume());
   }
}
