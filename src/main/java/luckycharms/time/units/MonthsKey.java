package luckycharms.time.units;

import java.time.ZonedDateTime;

import com.google.common.base.Converter;

import luckycharms.time.IntervalDefinition;
import luckycharms.time.IntervalDefinition.MonthsDefinition;
import luckycharms.time.MarketTimeUtils;

public class MonthsKey extends ATimeInterval<MonthsKey> {
   public static final MonthsDefinition INTERVAL = IntervalDefinition.MONTHS;

   public static final Converter<MonthsKey, String> FORMAT = //
         Converter.from(MonthsKey::toString, MonthsKey::parse);
   public static final Converter<MonthsKey, byte[]> BYTE_FORMAT = new TimeIntervalToBytes<>(MonthsKey::of);

   public static MonthsKey of(long months) {
      return new MonthsKey(months);
   }

   public static MonthsKey of(ZonedDateTime dt) {
      return new MonthsKey(INTERVAL.toIndex(dt));
   }

   public static MonthsKey now() {
      return of(MarketTimeUtils.now());
   }

   public static MonthsKey parse(String parse) {
      try {
         long index = Long.parseLong(parse);
         return of(index);
      } catch (NumberFormatException e) {
         // ignore
      }
      return of(ZonedDateTime.parse(parse));
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

}
