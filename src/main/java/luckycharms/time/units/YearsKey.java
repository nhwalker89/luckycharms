package luckycharms.time.units;

import java.time.ZonedDateTime;
import java.util.OptionalLong;

import com.google.common.base.Converter;

import luckycharms.time.IntervalDefinition;
import luckycharms.time.IntervalDefinition.YearsDefinition;
import luckycharms.time.MarketTimeUtils;
import luckycharms.time.TimeFormats;

public class YearsKey extends ATimeInterval<YearsKey> {

   public static final Converter<YearsKey, String> FORMAT = //
         Converter.from(YearsKey::toString, YearsKey::parse);

   public static final Converter<YearsKey, byte[]> BYTE_FORMAT = new TimeIntervalToBytes<>(
         YearsKey::of);

   public static final YearsDefinition INTERVAL = IntervalDefinition.YEARS;

   public static YearsKey of(long years) {
      return new YearsKey(years);
   }

   public static YearsKey of(ZonedDateTime dt) {
      return new YearsKey(INTERVAL.toIndex(dt));
   }

   public static YearsKey now() {
      return of(MarketTimeUtils.now());
   }

   private YearsKey(long index) {
      super(index);
   }

   @Override
   public IntervalDefinition interval() {
      return INTERVAL;
   }

   @Override
   protected YearsKey createNew(long index) {
      return new YearsKey(index);
   }

   public static YearsKey parse(String parse) {
      OptionalLong index = tryParseLong(parse);
      if (index.isPresent()) {
         return of(index.getAsLong());
      }
      return of(TimeFormats.parse(parse));
   }

   @Override
   public String toString() {
      int yearValue = Math.toIntExact(index());
      int absYear = Math.abs(yearValue);
      StringBuilder buf = new StringBuilder(10);
      if (absYear < 1000) {
         if (yearValue < 0) {
            buf.append(yearValue - 10000).deleteCharAt(1);
         } else {
            buf.append(yearValue + 10000).deleteCharAt(0);
         }
      } else {
         if (yearValue > 9999) {
            buf.append('+');
         }
         buf.append(yearValue);
      }
      return buf.toString();

   }

}
