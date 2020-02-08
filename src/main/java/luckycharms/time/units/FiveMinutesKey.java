package luckycharms.time.units;

import java.time.ZonedDateTime;
import java.util.OptionalLong;

import com.google.common.base.Converter;

import luckycharms.time.IntervalDefinition;
import luckycharms.time.IntervalDefinition.DurationBasedDefinition;
import luckycharms.time.MarketTimeUtils;
import luckycharms.time.TimeFormats;

public class FiveMinutesKey extends ATimeInterval<FiveMinutesKey> {
   public static final DurationBasedDefinition INTERVAL = IntervalDefinition.FIVE_MINUTES;

   public static final Converter<FiveMinutesKey, String> FORMAT = //
         Converter.from(FiveMinutesKey::toString, FiveMinutesKey::parse);
   public static final Converter<FiveMinutesKey, byte[]> BYTE_FORMAT = new TimeIntervalToBytes<>(
         FiveMinutesKey::of);

   public static FiveMinutesKey of(long fiveMins) {
      return new FiveMinutesKey(fiveMins);
   }

   public static FiveMinutesKey of(ZonedDateTime dt) {
      return new FiveMinutesKey(INTERVAL.toIndex(dt));
   }

   public static FiveMinutesKey now() {
      return of(MarketTimeUtils.now());
   }

   public static FiveMinutesKey parse(String parse) {
      OptionalLong index = tryParseLong(parse);
      if (index.isPresent()) {
         return of(index.getAsLong());
      }
      return of(TimeFormats.parse(parse));
   }

   private FiveMinutesKey(long index) {
      super(index);
   }

   @Override
   public IntervalDefinition interval() {
      return INTERVAL;
   }

   @Override
   protected FiveMinutesKey createNew(long index) {
      return new FiveMinutesKey(index);
   }

}
