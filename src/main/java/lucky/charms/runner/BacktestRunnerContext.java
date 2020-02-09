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
import luckycharms.datasets.prices.FifteenMinPriceDataSet;
import luckycharms.datasets.prices.PriceBar;
import luckycharms.storage.KeyValuePair;
import luckycharms.time.units.DaysKey;
import luckycharms.time.units.FifteenMinutesKey;

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
   public Map<String, Double> currentPrices(Iterator<String> symbols, EPriceHint hint) {
      return currentPricesVia15Min(symbols, hint);
   }

   @SuppressWarnings("unused")
   private Map<String, Double> currentPricesVia1D(Iterator<String> symbols, EPriceHint hint) {
      MarketTimeStateData state = clock().marketTimeState();
      Map<String, Double> prices = new HashMap<>();
      while (symbols.hasNext()) {
         String symbol = symbols.next();
         Double price = get1DPrice(state, symbol, hint);
         if (price != null) {
            prices.put(symbol, price);
         }
      }
      return prices;
   }

   private Map<String, Double> currentPricesVia15Min(Iterator<String> symbols, EPriceHint hint) {
      MarketTimeStateData state = clock().marketTimeState();
      FifteenMinutesKey key = FifteenMinutesKey.of(state.zonedDateTime());
      Map<String, Double> prices = new HashMap<>();
      while (symbols.hasNext()) {
         String symbol = symbols.next();
         Double price = get15MinPrice(state, key, symbol, hint);
         if (price != null) {
            prices.put(symbol, price);
         }
      }
      return prices;
   }

   private static Double get1DPrice(MarketTimeStateData state, String symbol, EPriceHint hint) {
      Double price = null;
      DailyPriceDataSet ds = DailyPriceDataSet.instance(symbol);
      KeyValuePair<DaysKey, PriceBar> bar = ds.getLastFullPriceBar(state.today());
      if (bar != null) {
         if (bar.getKey().compareTo(state.today()) < 0) {
            // bar is from a previous day - use close
            price = bar.getValue().getClose();
         } else {
            price = hint.get(state, bar.getValue());
         }
      }
      return price;
   }

   private static Double get15MinPrice(MarketTimeStateData state, FifteenMinutesKey currentTimeKey,
         String symbol, EPriceHint hint) {
      Double price = null;
      FifteenMinPriceDataSet ds = FifteenMinPriceDataSet.instance(symbol);
      KeyValuePair<FifteenMinutesKey, PriceBar> bar = ds.getLastFullPriceBar(currentTimeKey);
      if (bar != null) {
         if (bar.getKey().compareTo(currentTimeKey) < 0) {
            if (bar.getKey().asDaysKey().compareTo(state.today()) < 0) {
               price = get1DPrice(state, symbol, hint);
            } else {
               // bar is from a previous time block - use close
               price = bar.getValue().getClose();
            }

         } else {
            price = hint.get(state, bar.getValue());
         }
      } else {
         price = get1DPrice(state, symbol, hint);
      }
      return price;
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
