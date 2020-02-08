package luckycharms.time.units;

import java.time.ZonedDateTime;
import java.util.OptionalLong;

import com.google.common.base.Converter;

import luckycharms.time.IntervalDefinition;
import luckycharms.time.IntervalDefinition.DurationBasedDefinition;
import luckycharms.time.MarketTimeUtils;
import luckycharms.time.TimeFormats;

public class HoursKey extends ATimeInterval<HoursKey> {
   public static final DurationBasedDefinition INTERVAL = IntervalDefinition.HOURS;

   public static final Converter<HoursKey, String> FORMAT = //
         Converter.from(HoursKey::toString, HoursKey::parse);
   public static final Converter<HoursKey, byte[]> BYTE_FORMAT = new TimeIntervalToBytes<>(
         HoursKey::of);

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
      OptionalLong index = tryParseLong(parse);
      if (index.isPresent()) {
         return of(index.getAsLong());
      }
      return of(TimeFormats.parse(parse));
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
