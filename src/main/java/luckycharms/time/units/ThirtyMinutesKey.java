package luckycharms.time.units;

import java.time.ZonedDateTime;

import com.google.common.base.Converter;

import luckycharms.time.IntervalDefinition;
import luckycharms.time.IntervalDefinition.DurationBasedDefinition;
import luckycharms.time.MarketTimeUtils;

public class ThirtyMinutesKey extends ATimeInterval<ThirtyMinutesKey> {
   public static final DurationBasedDefinition INTERVAL = IntervalDefinition.THIRTY_MINUTES;

   public static final Converter<ThirtyMinutesKey, String> FORMAT = //
         Converter.from(ThirtyMinutesKey::toString, ThirtyMinutesKey::parse);
   public static final Converter<ThirtyMinutesKey, byte[]> BYTE_FORMAT = new TimeIntervalToBytes<>(
         ThirtyMinutesKey::of);

   public static ThirtyMinutesKey of(long thirtyMins) {
      return new ThirtyMinutesKey(thirtyMins);
   }

   public static ThirtyMinutesKey of(ZonedDateTime dt) {
      return new ThirtyMinutesKey(INTERVAL.toIndex(dt));
   }

   public static ThirtyMinutesKey now() {
      return of(MarketTimeUtils.now());
   }

   public static ThirtyMinutesKey parse(String parse) {
      try {
         long index = Long.parseLong(parse);
         return of(index);
      } catch (NumberFormatException e) {
         // ignore
      }
      return of(ZonedDateTime.parse(parse));
   }

   private ThirtyMinutesKey(long index) {
      super(index);
   }

   @Override
   public IntervalDefinition interval() {
      return INTERVAL;
   }

   @Override
   protected ThirtyMinutesKey createNew(long index) {
      return new ThirtyMinutesKey(index);
   }

}
