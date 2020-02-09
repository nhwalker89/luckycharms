package luckycharms.time.units;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.OptionalLong;

import com.google.common.base.Converter;

import luckycharms.time.IntervalDefinition;
import luckycharms.time.IntervalDefinition.DaysDefinition;
import luckycharms.time.MarketTimeUtils;
import luckycharms.time.TimeFormats;

public class DaysKey extends ATimeInterval<DaysKey> {
   public static final DaysDefinition INTERVAL = IntervalDefinition.DAYS;

   public static final Converter<DaysKey, String> FORMAT = //
         Converter.from(DaysKey::toString, DaysKey::parse);

   public static final Converter<DaysKey, byte[]> BYTE_FORMAT = new TimeIntervalToBytes<>(
         DaysKey::of);

   public static DaysKey of(long days) {
      return new DaysKey(days);
   }

   public static DaysKey of(int year, int month, int day) {
      return of(ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.MIDNIGHT,
            MarketTimeUtils.MARKET_ZONE));
   }

   public static DaysKey of(ZonedDateTime dt) {
      return new DaysKey(INTERVAL.toIndex(dt));
   }

   public static DaysKey now() {
      return of(MarketTimeUtils.now());
   }

   public static DaysKey parse(String parse) {
      OptionalLong index = tryParseLong(parse);
      if (index.isPresent()) {
         return of(index.getAsLong());
      }
      return of(TimeFormats.parse(parse));

   }

   private DaysKey(long index) {
      super(index);
   }

   @Override
   public IntervalDefinition interval() {
      return INTERVAL;
   }

   @Override
   protected DaysKey createNew(long index) {
      return new DaysKey(index);
   }

   @Override
   public String toString() {
      return time().toLocalDate().toString();
   }

   public ZonedDateTime time(LocalTime time) {
      ZonedDateTime zoned = super.time();
      zoned = zoned.with(time);
      return zoned;
   }

}
