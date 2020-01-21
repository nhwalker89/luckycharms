package lucky.charms.runner;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import lucky.charms.clock.Clock;
import luckycharms.datasets.prices.DailyPriceDataSet;
import luckycharms.datasets.prices.PriceBar;
import luckycharms.time.units.DaysKey;

public class RunnerContext {
   private final List<String> symbols;
   private final Clock clock;
   private final boolean isLive;

   public RunnerContext(List<String> symbols, Clock clock, boolean isLive) {
      this.symbols = ImmutableList.copyOf(symbols);
      this.clock = clock;
      this.isLive = isLive;

   }

   /* package */ Clock clock() {
      return clock;
   }

   public ZonedDateTime now() {
      return clock.now();
   }

   public DaysKey today() {
      return DaysKey.of(clock.now());
   }

   public List<String> getSymbols() { return symbols; }

   public boolean isRobust() { return isLive; }

   public boolean isLive() { return isLive; }

   public Map<String, Double> currentPrices(Iterator<String> symbols) {
      if (isLive) {
         // TODO
         throw new UnsupportedOperationException();
      } else {
         Map<String, Double> map = new HashMap<>();
         while (symbols.hasNext()) {
            String symbol = symbols.next();
            DailyPriceDataSet ds = DailyPriceDataSet.instance(symbol);
            DaysKey key = today();
            PriceBar bar = ds.get(key);
            if (bar == null) {
               for (int i = 0; i < 10 && bar == null; i++) {
                  key = key.previous();
                  bar = ds.get(key);
               }
               if (bar != null) {
                  map.put(symbol, bar.getClose());
               }
            } else {
               LocalTime time = now().toLocalTime();
               if (time.isBefore(LocalTime.NOON)) {
                  map.put(symbol, bar.getOpen());
               } else {
                  map.put(symbol, bar.getClose());
               }
            }

         }
         return map;
      }
   }
}
