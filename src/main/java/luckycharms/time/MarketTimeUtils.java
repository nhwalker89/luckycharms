package luckycharms.time;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import luckycharms.time.units.DaysKey;

public class MarketTimeUtils {
   public static final ZoneId NY_ZONE = ZoneId.of("America/New_York");
   public static final ZoneId MARKET_ZONE = NY_ZONE;
   public static final Clock MARKET_CLOCK = Clock.system(MARKET_ZONE);

   public static final LocalDateTime LOCAL_EPOCH = LocalDateTime.of(LocalDate.EPOCH,
         LocalTime.MIDNIGHT);
   public static final ZonedDateTime MARKET_EPOCH = ZonedDateTime.of(LOCAL_EPOCH, MARKET_ZONE);

   public static final LocalTime WELL_AFTER_MARKET_CLOSE = LocalTime.of(12 + 6, 0); // 6 PM

   public static final LocalTime USUAL_MARKET_OPEN = LocalTime.of(9, 30);

   public static final LocalTime USUAL_MARKET_CLOSE = LocalTime.of(12 + 4, 0);

   public static ZonedDateTime now() {
      return ZonedDateTime.now(MARKET_CLOCK);
   }

   public static LocalDateTime dateTimeNow() {
      return now().toLocalDateTime();
   }

   public static LocalTime timeNow() {
      return now().toLocalTime();
   }

   public static LocalDate dateNow() {
      return now().toLocalDate();
   }

   public static ZonedDateTime inMarketTime(ZonedDateTime dt) {
      return dt.withZoneSameInstant(MarketTimeUtils.MARKET_ZONE);
   }

   public static ZonedDateTime inMarketTime(Instant dt) {
      return dt.atZone(MARKET_ZONE);
   }

   public static ZonedDateTime inMarketTime(DaysKey day, LocalTime time) {
      return ZonedDateTime.of(day.time().toLocalDate(), time, MARKET_ZONE);
   }

   public static String format(long millis) {
      ZonedDateTime zdt = inMarketTime(Instant.ofEpochMilli(millis));
      return sFormat.format(zdt);
   }

   private static final DateTimeFormatter sFormat = new DateTimeFormatterBuilder()
         .parseCaseInsensitive().append(DateTimeFormatter.ISO_LOCAL_DATE).appendLiteral(' ')
         .appendValue(HOUR_OF_DAY, 2).appendLiteral(':').appendValue(MINUTE_OF_HOUR, 2)
         .optionalStart().appendLiteral(':').appendValue(SECOND_OF_MINUTE, 2).optionalStart()
         .appendFraction(NANO_OF_SECOND, 0, 3, true).toFormatter();
}
