package lucky.charms.runner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import lucky.charms.clock.IClock;
import lucky.charms.clock.MarketTimeStateData;
import luckycharms.config.StockUniverse;
import luckycharms.datasets.prices.DailyPriceDataSet;
import luckycharms.datasets.prices.PriceBar;
import luckycharms.storage.KeyValuePair;
import luckycharms.time.units.DaysKey;

public class BacktestRunnerContext implements IRunnerContext {

   private final ImmutableList<String> symbols;

   private final IClock clock;

   public BacktestRunnerContext(DaysKey start, DaysKey end) {
      this(StockUniverse.SP500, start, end);
   }

   public BacktestRunnerContext(List<String> symbols, DaysKey start, DaysKey end) {
      this.symbols = ImmutableList.copyOf(symbols);
      this.clock = new IClock.FakeClock(start, end);
   }

   @Override
   public List<String> getSymbols() { return symbols; }

   @Override
   public Map<String, Double> currentPrices(Iterator<String> symbols) {
      MarketTimeStateData state = clock().marketTimeState();
      Map<String, Double> prices = new HashMap<>();
      while (symbols.hasNext()) {
         Double price = null;
         String symbol = symbols.next();
         DailyPriceDataSet ds = DailyPriceDataSet.instance(symbol);
         KeyValuePair<DaysKey, PriceBar> bar = ds.getLastFullPriceBar(state.today());
         if (bar != null) {
            if (bar.getKey().compareTo(state.today()) < 0) {
               // bar is from a previous day - use close
               price = bar.getValue().getClose();
            } else if (state.isBeforeMiddleOfValidTradingDay()) {
               price = bar.getValue().getOpen();
            } else {
               price = bar.getValue().getClose();
            }
         }
         if (price != null) {
            prices.put(symbol, price);
         }
      }
      return prices;
   }

   @Override
   public boolean isRobust() { return false; }

   @Override
   public boolean isLive() { return false; }

   @Override
   public IClock clock() {
      return clock;
   }

}
