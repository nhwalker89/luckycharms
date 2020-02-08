package luckycharms.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class DecimalFormatter {

   public static void main(String[] args) {
      System.out.println(format(1000.00000001, 1));
   }

   public static final void format(StringBuilder b, double value, int scale) {
      b.append(format(value, scale));
   }

   public static String format(double value, int scale) {
      return format(value, scale, RoundingMode.HALF_UP);
   }

   public static String format(double value, int scale, RoundingMode mode) {
      return getFormat(mode, scale).format(value);
   }

   private static final int CACHE_MAX = 10;
   private static final int CACHE_LEN = CACHE_MAX + 1;
   private static final ThreadLocal<Map<RoundingMode, NumberFormat[]>> CACHE = ThreadLocal
         .withInitial(() -> new EnumMap<>(RoundingMode.class));

   private static NumberFormat getFormat(RoundingMode mode, int scale) {
      NumberFormat format;
      if (scale < CACHE_LEN) {
         Map<RoundingMode, NumberFormat[]> cacheMap = CACHE.get();
         NumberFormat[] cacheArray = cacheMap.get(mode);
         if (cacheArray == null) {
            cacheArray = new NumberFormat[CACHE_LEN];
            cacheMap.put(mode, cacheArray);
         }
         format = cacheArray[scale];
         if (format == null) {
            format = createFormat(mode, scale);
            cacheArray[scale] = format;
         }
      } else {
         format = createFormat(mode, scale);
      }
      return format;
   }

   private static NumberFormat createFormat(RoundingMode mode, int scale) {
      NumberFormat f = NumberFormat.getNumberInstance(Locale.US);
      if (f instanceof DecimalFormat) {
         DecimalFormat format = (DecimalFormat) f;
         format.setMinimumFractionDigits(scale);
         format.setMaximumFractionDigits(scale);
         format.setRoundingMode(mode);
      }
      return f;
   }
}
