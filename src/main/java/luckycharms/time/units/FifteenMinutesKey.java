package luckycharms.time.units;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.OptionalLong;

import com.google.common.base.Converter;
import com.google.common.collect.Range;

import luckycharms.datasets.calendar.MarketDayData;
import luckycharms.datasets.calendar.MarketDayDataSet;
import luckycharms.time.IntervalDefinition;
import luckycharms.time.IntervalDefinition.DurationBasedDefinition;
import luckycharms.time.MarketTimeUtils;
import luckycharms.time.TimeFormats;

public class FifteenMinutesKey extends ATimeInterval<FifteenMinutesKey> {

   public static final DurationBasedDefinition INTERVAL = IntervalDefinition.FIFTEEN_MINUTES;

   public static final Converter<FifteenMinutesKey, String> FORMAT = //
         Converter.from(FifteenMinutesKey::toString, FifteenMinutesKey::parse);
   public static final Converter<FifteenMinutesKey, byte[]> BYTE_FORMAT = new TimeIntervalToBytes<>(
         FifteenMinutesKey::of);

   public static final int NUMBER_PER_DAY = 24 * 60 / 15;

   public static Range<FifteenMinutesKey> openMarketRange(DaysKey day) {
      return openMarketRange(day, true);
   }

   public static Range<FifteenMinutesKey> openMarketRange(DaysKey day, boolean useDefault) {
      MarketDayData data = MarketDayDataSet.instance().get(day);
      FifteenMinutesKey open;
      FifteenMinutesKey close;
      if (data != null && data.validMarketDay()) {
         open = FifteenMinutesKey.of(day.marketDate(), data.getOpen());
         close = FifteenMinutesKey.of(day.marketDate(), data.getClose());
      } else {
         if (useDefault) {
            open = FifteenMinutesKey.of(day.marketDate(), MarketTimeUtils.USUAL_MARKET_OPEN);
            close = FifteenMinutesKey.of(day.marketDate(), MarketTimeUtils.USUAL_MARKET_CLOSE);
         } else {
            open = null;
            close = null;
         }
      }
      if (open == null || close == null) {
         return null;
      } else {
         return Range.closed(open, close);
      }
   }

   public static FifteenMinutesKey of(long fifteenMins) {
      return new FifteenMinutesKey(fifteenMins);
   }

   public static FifteenMinutesKey of(LocalDate date, LocalTime time) {
      return of(LocalDateTime.of(date, time));
   }

   public static FifteenMinutesKey of(LocalDateTime dt) {
      return of(ZonedDateTime.of(dt, MarketTimeUtils.MARKET_ZONE));
   }

   public static FifteenMinutesKey of(ZonedDateTime dt) {
      return new FifteenMinutesKey(INTERVAL.toIndex(dt));
   }

   public static FifteenMinutesKey now() {
      return of(MarketTimeUtils.now());
   }

   public static FifteenMinutesKey parse(String parse) {
      OptionalLong index = tryParseLong(parse);
      if (index.isPresent()) {
         return of(index.getAsLong());
      }
      return of(TimeFormats.parse(parse));
   }

   private FifteenMinutesKey(long index) {
      super(index);
   }

   @Override
   public IntervalDefinition interval() {
      return INTERVAL;
   }

   @Override
   protected FifteenMinutesKey createNew(long index) {
      return new FifteenMinutesKey(index);
   }

}
