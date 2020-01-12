package luckycharms.time;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import luckycharms.time.units.DaysKey;

public class MarketTimeUtils {
   public static final ZoneId NY_ZONE = ZoneId.of("America/New_York");
   public static final ZoneId MARKET_ZONE = NY_ZONE;
   public static final Clock MARKET_CLOCK = Clock.system(MARKET_ZONE);

   public static final LocalDateTime LOCAL_EPOCH = LocalDateTime.of(LocalDate.EPOCH,
         LocalTime.MIDNIGHT);
   public static final ZonedDateTime MARKET_EPOCH = ZonedDateTime.of(LOCAL_EPOCH, MARKET_ZONE);

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
}
