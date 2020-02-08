package luckycharms.time.units;

import java.time.ZonedDateTime;
import java.util.OptionalLong;

import com.google.common.base.Converter;

import luckycharms.time.IntervalDefinition;
import luckycharms.time.IntervalDefinition.DurationBasedDefinition;
import luckycharms.time.MarketTimeUtils;
import luckycharms.time.TimeFormats;

public class MinutesKey extends ATimeInterval<MinutesKey> {
   public static final DurationBasedDefinition INTERVAL = IntervalDefinition.MINUTES;

   public static final Converter<MinutesKey, String> FORMAT = //
         Converter.from(MinutesKey::toString, MinutesKey::parse);
   public static final Converter<MinutesKey, byte[]> BYTE_FORMAT = new TimeIntervalToBytes<>(
         MinutesKey::of);

   public static MinutesKey of(long mins) {
      return new MinutesKey(mins);
   }

   public static MinutesKey of(ZonedDateTime dt) {
      return new MinutesKey(INTERVAL.toIndex(dt));
   }

   public static MinutesKey now() {
      return of(MarketTimeUtils.now());
   }

   public static MinutesKey parse(String parse) {
      OptionalLong index = tryParseLong(parse);
      if (index.isPresent()) {
         return of(index.getAsLong());
      }
      return of(TimeFormats.parse(parse));
   }

   private MinutesKey(long index) {
      super(index);
   }

   @Override
   public IntervalDefinition interval() {
      return INTERVAL;
   }

   @Override
   protected MinutesKey createNew(long index) {
      return new MinutesKey(index);
   }

}
