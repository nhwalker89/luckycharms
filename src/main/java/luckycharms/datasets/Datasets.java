package luckycharms.datasets;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import luckycharms.config.StockUniverse;
import luckycharms.datasets.calendar.MarketDayDataSet;
import luckycharms.datasets.prices.DailyPriceDataSet;
import luckycharms.storage.IDataSet;

@SuppressWarnings("rawtypes")
public class Datasets {
   private Datasets() {
      throw new Error("Never Create");
   }

   public static final Map<String, IDataSet> all;
   static {
      ImmutableMap.Builder<String, IDataSet> bldr = ImmutableMap.builder();
      bldr.put("MarketDay", MarketDayDataSet.instance());

      for (String symbol : StockUniverse.SP500) {
         String key = "DailyPrice." + symbol;
         bldr.put(key, DailyPriceDataSet.instance(symbol));
      }
      all = bldr.build();
   }
}
