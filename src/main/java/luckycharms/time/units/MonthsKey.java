package luckycharms.time.units;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import com.google.common.base.Converter;

import luckycharms.time.IntervalDefinition;
import luckycharms.time.IntervalDefinition.MonthsDefinition;
import luckycharms.time.MarketTimeUtils;
import luckycharms.time.TimeFormats;

public class MonthsKey extends ATimeInterval<MonthsKey> {
   public static final MonthsDefinition INTERVAL = IntervalDefinition.MONTHS;

   public static final Converter<MonthsKey, String> FORMAT = //
         Converter.from(MonthsKey::toString, MonthsKey::parse);
   public static final Converter<MonthsKey, byte[]> BYTE_FORMAT = new TimeIntervalToBytes<>(
         MonthsKey::of);

   public static MonthsKey of(long months) {
      return new MonthsKey(months);
   }

   public static MonthsKey of(ZonedDateTime dt) {
      return new MonthsKey(INTERVAL.toIndex(dt));
   }

   public static MonthsKey now() {
      return of(MarketTimeUtils.now());
   }

   private MonthsKey(long index) {
      super(index);
   }

   @Override
   public IntervalDefinition interval() {
      return INTERVAL;
   }

   @Override
   protected MonthsKey createNew(long index) {
      return new MonthsKey(index);
   }

   public static MonthsKey parse(String parse) {
      try {
         long index = Long.parseLong(parse);
         return of(index);
      } catch (NumberFormatException e) {
         // ignore
      }
      return of(TimeFormats.parse(parse));
   }

   @Override
   public String toString() {
      LocalDate date = marketDate();

      int yearValue = date.getYear();
      int monthValue = date.getMonthValue();
      int absYear = Math.abs(yearValue);
      StringBuilder buf = new StringBuilder(10);
      if (absYear < 1000) {
         if (yearValue < 0) {
            buf.append(yearValue - 10000).deleteCharAt(1);
         } else {
            buf.append(yearValue + 10000).deleteCharAt(0);
         }
      } else {
         if (yearValue > 9999) {
            buf.append('+');
         }
         buf.append(yearValue);
      }
      return buf.append(monthValue < 10 ? "-0" : "-")//
            .append(monthValue).toString();

   }

}
