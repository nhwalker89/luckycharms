package luckycharms.time.units;

import java.time.ZonedDateTime;

import com.google.common.base.Converter;

import luckycharms.time.IntervalDefinition;
import luckycharms.time.IntervalDefinition.DurationBasedDefinition;
import luckycharms.time.MarketTimeUtils;

public class HoursKey extends ATimeInterval<HoursKey> {
   public static final DurationBasedDefinition INTERVAL = IntervalDefinition.HOURS;

   public static final Converter<HoursKey, String> FORMAT = //
         Converter.from(HoursKey::toString, HoursKey::parse);
   public static final Converter<HoursKey, byte[]> BYTE_FORMAT = new TimeIntervalToBytes<>(HoursKey::of);

   public static HoursKey of(long hours) {
      return new HoursKey(hours);
   }

   public static HoursKey of(ZonedDateTime dt) {
      return new HoursKey(INTERVAL.toIndex(dt));
   }

   public static HoursKey now() {
      return of(MarketTimeUtils.now());
   }

   public static HoursKey parse(String parse) {
      try {
         long index = Long.parseLong(parse);
         return of(index);
      } catch (NumberFormatException e) {
         // ignore
      }
      return of(ZonedDateTime.parse(parse));
   }

   private HoursKey(long index) {
      super(index);
   }

   @Override
   public IntervalDefinition interval() {
      return INTERVAL;
   }

   @Override
   protected HoursKey createNew(long index) {
      return new HoursKey(index);
   }

}
