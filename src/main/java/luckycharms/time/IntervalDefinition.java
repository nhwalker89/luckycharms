package luckycharms.time;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public interface IntervalDefinition {
   public static final DurationBasedDefinition SECONDS = new DurationBasedDefinition(1);
   public static final DurationBasedDefinition MINUTES = new DurationBasedDefinition(TimeUnit.MINUTES.toSeconds(1));
   public static final DurationBasedDefinition FIVE_MINUTES = new DurationBasedDefinition(
         TimeUnit.MINUTES.toSeconds(5));
   public static final DurationBasedDefinition FIFTEEN_MINUTES = new DurationBasedDefinition(
         TimeUnit.MINUTES.toSeconds(15));
   public static final DurationBasedDefinition THIRTY_MINUTES = new DurationBasedDefinition(
         TimeUnit.MINUTES.toSeconds(30));
   public static final DurationBasedDefinition HOURS = new DurationBasedDefinition(TimeUnit.HOURS.toSeconds(1));

   public static final DaysDefinition DAYS = new DaysDefinition();
   public static final MonthsDefinition MONTHS = new MonthsDefinition();
   public static final YearsDefinition YEARS = new YearsDefinition();

   long toIndex(ZonedDateTime dt);

   ZonedDateTime toTime(long index);

   static class DurationBasedDefinition implements IntervalDefinition {
      final long resolution;
      final Duration resDuration;

      DurationBasedDefinition(long res) {
         resolution = res;
         resDuration = Duration.ofSeconds(res);
      }

      @Override
      public long toIndex(ZonedDateTime dt) {
         return dt.toEpochSecond() / resolution;
      }

      @Override
      public ZonedDateTime toTime(long index) {
         return ZonedDateTime.ofInstant(Instant.ofEpochSecond(index * resolution), MarketTimeUtils.MARKET_ZONE);
      }

      public Duration resolution() {
         return resDuration;
      }
   }

   static class DaysDefinition implements IntervalDefinition {

      @Override
      public long toIndex(ZonedDateTime dt) {
         return dt.withZoneSameInstant(MarketTimeUtils.MARKET_ZONE).toLocalDate().toEpochDay();
      }

      @Override
      public ZonedDateTime toTime(long index) {
         return ZonedDateTime.of(LocalDate.ofEpochDay(index), LocalTime.MIDNIGHT, MarketTimeUtils.MARKET_ZONE);
      }
   }

   static class MonthsDefinition implements IntervalDefinition {

      @Override
      public long toIndex(ZonedDateTime dt) {
         LocalDate marketDt = dt.withZoneSameInstant(MarketTimeUtils.MARKET_ZONE).toLocalDate();
         long months = marketDt.getYear() * 12 + (marketDt.getMonthValue() - 1);
         return months;
      }

      @Override
      public ZonedDateTime toTime(long index) {
         int year = Math.toIntExact(index / 12);
         int month = Math.toIntExact((index % 12) + 1);

         return ZonedDateTime.of(LocalDate.of(year, month, 2), //
               LocalTime.MIDNIGHT, MarketTimeUtils.MARKET_ZONE);
      }
   }

   static class YearsDefinition implements IntervalDefinition {

      @Override
      public long toIndex(ZonedDateTime dt) {
         LocalDate marketDt = dt.withZoneSameInstant(MarketTimeUtils.MARKET_ZONE).toLocalDate();
         int year = marketDt.getYear();
         return year;
      }

      @Override
      public ZonedDateTime toTime(long index) {
         return ZonedDateTime.of(LocalDate.of(Math.toIntExact(index), 1, 1), //
               LocalTime.MIDNIGHT, MarketTimeUtils.MARKET_ZONE);
      }
   }

}
