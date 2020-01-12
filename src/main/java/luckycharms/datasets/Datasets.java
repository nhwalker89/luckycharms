package luckycharms.datasets;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import luckycharms.datasets.calendar.MarketDayDataSet;
import luckycharms.storage.IDataSet;

@SuppressWarnings("rawtypes")
public class Datasets {
   private Datasets() {
      throw new Error("Never Create");
   }

   public static Map<String, IDataSet> all;
   static {
      ImmutableMap.Builder<String, IDataSet> bldr = ImmutableMap.builder();
      bldr.put("MarketDay", MarketDayDataSet.instance());
   }
}
