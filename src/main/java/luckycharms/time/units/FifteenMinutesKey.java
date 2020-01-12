package luckycharms.time.units;

import java.time.ZonedDateTime;

import com.google.common.base.Converter;

import luckycharms.time.IntervalDefinition;
import luckycharms.time.IntervalDefinition.DurationBasedDefinition;
import luckycharms.time.MarketTimeUtils;

public class FifteenMinutesKey extends ATimeInterval<FifteenMinutesKey> {
   public static final DurationBasedDefinition INTERVAL = IntervalDefinition.FIFTEEN_MINUTES;

   public static final Converter<FifteenMinutesKey, String> FORMAT = //
         Converter.from(FifteenMinutesKey::toString, FifteenMinutesKey::parse);
   public static final Converter<FifteenMinutesKey, byte[]> BYTE_FORMAT = new TimeIntervalToBytes<>(
         FifteenMinutesKey::of);

   public static FifteenMinutesKey of(long fifteenMins) {
      return new FifteenMinutesKey(fifteenMins);
   }

   public static FifteenMinutesKey of(ZonedDateTime dt) {
      return new FifteenMinutesKey(INTERVAL.toIndex(dt));
   }

   public static FifteenMinutesKey now() {
      return of(MarketTimeUtils.now());
   }

   public static FifteenMinutesKey parse(String parse) {
      try {
         long index = Long.parseLong(parse);
         return of(index);
      } catch (NumberFormatException e) {
         // ignore
      }
      return of(ZonedDateTime.parse(parse));
   }

   private FifteenMinutesKey(long index) {
      super(index);
   }

   @Override
   public IntervalDefinition interval() {
      return INTERVAL;
   }

   @Override
   protected FifteenMinutesKey createNew(long index) {
      return new FifteenMinutesKey(index);
   }

}
