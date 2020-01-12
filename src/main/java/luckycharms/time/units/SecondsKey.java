package luckycharms.time.units;

import java.time.ZonedDateTime;

import com.google.common.base.Converter;

import luckycharms.time.IntervalDefinition;
import luckycharms.time.IntervalDefinition.DurationBasedDefinition;
import luckycharms.time.MarketTimeUtils;

public class SecondsKey extends ATimeInterval<SecondsKey> {
   public static final DurationBasedDefinition INTERVAL = IntervalDefinition.SECONDS;

   public static final Converter<SecondsKey, String> FORMAT = //
         Converter.from(SecondsKey::toString, SecondsKey::parse);
   public static final Converter<SecondsKey, byte[]> BYTE_FORMAT = new TimeIntervalToBytes<>(SecondsKey::of);

   public static SecondsKey of(long seconds) {
      return new SecondsKey(seconds);
   }

   public static SecondsKey of(ZonedDateTime dt) {
      return new SecondsKey(INTERVAL.toIndex(dt));
   }

   public static SecondsKey now() {
      return of(MarketTimeUtils.now());
   }

   public static SecondsKey parse(String parse) {
      try {
         long index = Long.parseLong(parse);
         return of(index);
      } catch (NumberFormatException e) {
         // ignore
      }
      return of(ZonedDateTime.parse(parse));
   }

   private SecondsKey(long index) {
      super(index);
   }

   @Override
   public IntervalDefinition interval() {
      return INTERVAL;
   }

   @Override
   protected SecondsKey createNew(long index) {
      return new SecondsKey(index);
   }

   public DaysKey asDaysKey() {
      return DaysKey.of(time());
   }

}
