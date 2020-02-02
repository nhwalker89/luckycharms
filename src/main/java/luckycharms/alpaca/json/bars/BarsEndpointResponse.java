package luckycharms.alpaca.json.bars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.google.gson.annotations.SerializedName;

import luckycharms.time.units.SecondsKey;

public class BarsEndpointResponse extends HashMap<String, BarsEndpointResponse.BarForSymbol> {

   private static final long serialVersionUID = 1L;

   @Override
   public String toString() {
      return entrySet().stream().map(e -> {
         return e.getKey() + "\n" + e.getValue();
      }).collect(Collectors.joining("\n"));
   }

   public static class BarForSymbol extends ArrayList<BarEntry> {
      private static final long serialVersionUID = 1L;

      // nothing else
      @Override
      public String toString() {
         return stream().map(Object::toString).collect(Collectors.joining("\n   ", "   ", ""));
      }
   }

   public static class BarEntry {
      @SerializedName("t")
      public Long timestamp;
      @SerializedName("o")
      public Double open;
      @SerializedName("h")
      public Double high;
      @SerializedName("l")
      public Double low;
      @SerializedName("c")
      public Double close;
      @SerializedName("v")
      public Double volume;

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("BarEntry [");
         builder.append(timestamp == null ? "null" : SecondsKey.of(timestamp));
         builder.append(", open=");
         builder.append(open == null ? "null" : open);
         builder.append(", high=");
         builder.append(high == null ? "null" : high);
         builder.append(", low=");
         builder.append(low == null ? "null" : low);
         builder.append(", close=");
         builder.append(close == null ? "null" : close);
         builder.append(", volume=");
         builder.append(volume == null ? "null" : volume);
         builder.append("]");
         return builder.toString();
      }

   }
}
