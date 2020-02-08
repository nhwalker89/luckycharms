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
      public String timestamp;
      @SerializedName("o")
      public String open;
      @SerializedName("h")
      public String high;
      @SerializedName("l")
      public String low;
      @SerializedName("c")
      public String close;
      @SerializedName("v")
      public String volume;

      public SecondsKey getTimestamp() {
         return timestamp == null ? null : SecondsKey.of(Long.parseLong(timestamp));
      }

      public Double getOpen() { return open == null ? null : Double.parseDouble(open); }

      public Double getClose() { return close == null ? null : Double.parseDouble(close); }

      public Double getHigh() { return high == null ? null : Double.parseDouble(high); }

      public Double getLow() { return low == null ? null : Double.parseDouble(low); }

      public Double getVolume() { return volume == null ? null : Double.parseDouble(volume); }

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("BarEntry [");
         builder.append(getTimestamp());
         builder.append(", open=");
         builder.append(open);
         builder.append(", high=");
         builder.append(high);
         builder.append(", low=");
         builder.append(low);
         builder.append(", close=");
         builder.append(close);
         builder.append(", volume=");
         builder.append(volume);
         builder.append("]");
         return builder.toString();
      }

   }
}
