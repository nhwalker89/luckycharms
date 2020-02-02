package luckycharms.time;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.temporal.TemporalAccessor;

public class TimeFormats {

   public static ZonedDateTime parse(String toParse) {
      TemporalAccessor accessor = FLEXIBLE_ZONED_DATE_TIME.parse(toParse);
      return fromRelaxed(accessor);
   }

   public static final DateTimeFormatter FLEXIBLE_LOCAL_DATE;
   static {
      FLEXIBLE_LOCAL_DATE = new DateTimeFormatterBuilder()//

            // FORMAT
            .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)//
            .optionalStart()//
            .appendLiteral('-')//
            .appendValue(MONTH_OF_YEAR, 2)//
            .optionalStart()//
            .appendLiteral('-')//
            .appendValue(DAY_OF_MONTH, 2)//

            // BUILD
            .toFormatter()//
            .withChronology(IsoChronology.INSTANCE)//
            .withResolverStyle(ResolverStyle.STRICT);
   }
   public static final DateTimeFormatter FLEXIBLE_LOCAL_TIME;
   static {
      FLEXIBLE_LOCAL_TIME = new DateTimeFormatterBuilder()//
            // FORMAT
            .appendValue(HOUR_OF_DAY, 2)//
            .optionalStart()//
            .appendLiteral(':')//
            .appendValue(MINUTE_OF_HOUR, 2)//
            .optionalStart()//
            .appendLiteral(':')//
            .appendValue(SECOND_OF_MINUTE, 2)//
            .optionalStart()//
            .appendFraction(NANO_OF_SECOND, 0, 9, true)//

            // DEFAULT
            .parseDefaulting(NANO_OF_SECOND, 0)//
            .parseDefaulting(SECOND_OF_MINUTE, 0)//
            .parseDefaulting(MINUTE_OF_HOUR, 0)//
            .parseDefaulting(HOUR_OF_DAY, 0)//

            // BUILD
            .toFormatter()//
            .withChronology(null)//
            .withResolverStyle(ResolverStyle.STRICT);//
   }
   public static final DateTimeFormatter FLEXIBLE_LOCAL_DATE_TIME;
   static {
      FLEXIBLE_LOCAL_DATE_TIME = new DateTimeFormatterBuilder()//
            .parseCaseInsensitive()//

            // FORMAT
            .append(FLEXIBLE_LOCAL_DATE)//
            .optionalStart()//
            .appendLiteral('T')//
            .append(FLEXIBLE_LOCAL_TIME)//

            // BUILD
            .toFormatter()//
            .withChronology(IsoChronology.INSTANCE)//
            .withResolverStyle(ResolverStyle.STRICT);
   }

   public static final DateTimeFormatter FLEXIBLE_OFFSET_DATE_TIME;
   static {
      FLEXIBLE_OFFSET_DATE_TIME = new DateTimeFormatterBuilder()//
            .parseCaseInsensitive()//

            // FORMAT
            .append(FLEXIBLE_LOCAL_DATE_TIME)//
            .optionalStart()//
            .parseLenient()//
            .appendOffsetId()//
            .parseStrict()//

            // BUILD
            .toFormatter()//
            .withChronology(IsoChronology.INSTANCE)//
            .withResolverStyle(ResolverStyle.STRICT);//
   }
   public static final DateTimeFormatter FLEXIBLE_ZONED_DATE_TIME;
   static {
      FLEXIBLE_ZONED_DATE_TIME = new DateTimeFormatterBuilder()//

            // FORMAT
            .append(FLEXIBLE_OFFSET_DATE_TIME)//
            .optionalStart()//
            .appendLiteral('[')//
            .parseCaseSensitive()//
            .appendZoneRegionId()//
            .appendLiteral(']')//

            // BUILD
            .toFormatter()//
      ;
   }

   private static ZonedDateTime fromRelaxed(TemporalAccessor temporal) {
      if (temporal instanceof ZonedDateTime) {
         return (ZonedDateTime) temporal;
      }
      try {

         // Parse Zone: Default to Market Zone
         ZoneId zone;
         try {
            zone = ZoneId.from(temporal);
         } catch (DateTimeException e) {
            zone = MarketTimeUtils.MARKET_ZONE;
         }

         int year;
         if (!temporal.isSupported(YEAR)) {
            throw new DateTimeException("Missing Year");
         }
         year = temporal.get(YEAR);

         int month = 1;
         if (temporal.isSupported(MONTH_OF_YEAR)) {
            month = temporal.get(MONTH_OF_YEAR);
         }
         int day = 1;
         if (temporal.isSupported(DAY_OF_MONTH)) {
            day = temporal.get(DAY_OF_MONTH);
         }

         LocalDate date = LocalDate.of(year, month, day);

         // Parse Local Time: Default to MIDNIGHT(0)
         LocalTime time;
         try {
            time = LocalTime.from(temporal);
         } catch (DateTimeException e) {
            time = LocalTime.MIDNIGHT;
         }

         return ZonedDateTime.of(date, time, zone);
      } catch (DateTimeException ex) {
         throw new DateTimeException("Unable to obtain ZonedDateTime from TemporalAccessor: "
               + temporal + " of type " + temporal.getClass().getName(), ex);
      }
   }

}
