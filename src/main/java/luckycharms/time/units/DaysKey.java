package luckycharms.time.units;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.google.common.base.Converter;

import luckycharms.time.IntervalDefinition;
import luckycharms.time.IntervalDefinition.DaysDefinition;
import luckycharms.time.MarketTimeUtils;

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
      try {
         long index = Long.parseLong(parse);
         return of(index);
      } catch (NumberFormatException e) {
         // ignore
      }
      return of(ZonedDateTime.parse(parse));
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

   public String toIsoFormat() {
      return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(time());
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
