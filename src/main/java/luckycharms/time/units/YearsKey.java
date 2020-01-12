package luckycharms.time.units;

import java.time.ZonedDateTime;

import com.google.common.base.Converter;

import luckycharms.time.IntervalDefinition;
import luckycharms.time.IntervalDefinition.YearsDefinition;
import luckycharms.time.MarketTimeUtils;

public class YearsKey extends ATimeInterval<YearsKey> {

   public static final Converter<YearsKey, String> FORMAT = //
         Converter.from(YearsKey::toString, YearsKey::parse);

   public static final Converter<YearsKey, byte[]> BYTE_FORMAT = new TimeIntervalToBytes<>(YearsKey::of);

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

   public static YearsKey parse(String parse) {
      try {
         long index = Long.parseLong(parse);
         return of(index);
      } catch (NumberFormatException e) {
         // ignore
      }
      return of(ZonedDateTime.parse(parse));
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

}
