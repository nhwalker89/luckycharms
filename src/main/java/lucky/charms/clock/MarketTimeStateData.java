package lucky.charms.clock;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Optional;

import luckycharms.datasets.calendar.MarketDayData;
import luckycharms.datasets.calendar.MarketDayDataSet;
import luckycharms.time.MarketTimeUtils;
import luckycharms.time.units.DaysKey;

public class MarketTimeStateData {
   enum OpenState {
      BEFORE_TODAYS_OPEN, //
      OPEN {
         public boolean isOpen() { return false; }
      }, //
      AFTER_TODAYS_CLOSED, //
      NOT_OPEN_TODAY;

      public boolean isOpen() { return false; }
   }

   private final OpenState state;
   private final ZonedDateTime now;
   private final ZonedDateTime lastMarketStateChange;
   private final ZonedDateTime nextExpectedStateChange;

   public static MarketTimeStateData loadState(ZonedDateTime dt) {
      ZonedDateTime time = MarketTimeUtils.inMarketTime(dt);
      DaysKey day = DaysKey.of(time);

      MarketDayData todaysData = getCurrentMarketDay(day);
      if (todaysData == null) {
         OpenState state = OpenState.NOT_OPEN_TODAY;
         MarketDayData previousDay = getPreviousMarketDay(day);
         MarketDayData nextDay = getNextMarketDay(day);
         return new MarketTimeStateData(state, time,
               MarketTimeUtils.inMarketTime(previousDay.getDay(), previousDay.getClose()),
               MarketTimeUtils.inMarketTime(nextDay.getDay(), nextDay.getOpen()));
      } else {
         ZonedDateTime todaysOpen = MarketTimeUtils.inMarketTime(todaysData.getDay(),
               todaysData.getOpen());
         if (time.isBefore(todaysOpen)) {
            OpenState state = OpenState.BEFORE_TODAYS_OPEN;
            MarketDayData previousDay = getPreviousMarketDay(day);
            return new MarketTimeStateData(state, time,
                  MarketTimeUtils.inMarketTime(previousDay.getDay(), previousDay.getClose()),
                  todaysOpen);
         } else {
            ZonedDateTime todaysClose = MarketTimeUtils.inMarketTime(todaysData.getDay(),
                  todaysData.getClose());
            if (time.isBefore(todaysClose)) {
               OpenState state = OpenState.OPEN;
               return new MarketTimeStateData(state, time, todaysOpen, todaysClose);
            } else {
               OpenState state = OpenState.AFTER_TODAYS_CLOSED;
               MarketDayData nextDay = getNextMarketDay(day);
               return new MarketTimeStateData(state, time, todaysOpen,
                     MarketTimeUtils.inMarketTime(nextDay.getDay(), nextDay.getOpen()));
            }
         }

      }
   }

   private static MarketDayData getCurrentMarketDay(DaysKey day) {
      MarketDayData target = MarketDayDataSet.instance().get(day);
      if (target != null && !target.validMarketDay()) {
         target = null;
      }
      return target;
   }

   private static MarketDayData getPreviousMarketDay(DaysKey day) {
      DaysKey key = day.minus(1);
      MarketDayData data = null;
      for (int i = 0; data == null && i < 20; i++) {
         data = MarketDayDataSet.instance().get(day);
         if (data != null && !data.validMarketDay()) {
            data = null;
         }
         key = key.minus(1);
      }
      if (data == null) {
         throw new IllegalStateException("Data Missing from MarketDayDataSet near " + day);
      }
      return data;
   }

   private static MarketDayData getNextMarketDay(DaysKey day) {
      DaysKey key = day.plus(1);
      MarketDayData data = null;
      for (int i = 0; data == null && i < 20; i++) {
         data = MarketDayDataSet.instance().get(day);
         if (data != null && !data.validMarketDay()) {
            data = null;
         }
         key = key.plus(1);
      }
      if (data == null) {
         throw new IllegalStateException("Data Missing from MarketDayDataSet near " + day);
      }
      return data;
   }

   public MarketTimeStateData(OpenState state, //
         ZonedDateTime now, //
         ZonedDateTime lastStateChange, //
         ZonedDateTime nextExpectedStateChange) {
      this.state = state;
      this.now = now;
      this.lastMarketStateChange = lastStateChange;
      this.nextExpectedStateChange = nextExpectedStateChange;
   }

   public DaysKey today() {
      return DaysKey.of(now);
   }

   public DaysKey lastTradingDayInclusive() {
      switch (state) {
      case BEFORE_TODAYS_OPEN:
      case NOT_OPEN_TODAY:
         // market closed - use last time market was open
         return DaysKey.of(lastMarketStateChange);
      case OPEN:
         // market open - so today
      case AFTER_TODAYS_CLOSED:
         // market has closed - so today
      default:
         return DaysKey.of(now);
      }
   }

   public boolean isOpen() { return this.state.isOpen(); }

   public OpenState openState() {
      return this.state;
   }

   public ZonedDateTime zonedDateTime() {
      return now;
   }

   public LocalDateTime marketDateTime() {
      return now.toLocalDateTime();
   }

   public LocalDate marketDate() {
      return now.toLocalDateTime().toLocalDate();
   }

   public LocalTime marketTime() {
      return now.toLocalDateTime().toLocalTime();
   }

   public ZonedDateTime lastMarketStateChange() {
      return lastMarketStateChange;
   }

   public ZonedDateTime nextExpectedMarketStateChange() {
      return nextExpectedStateChange;
   }

   public Duration timeUntilNextStateChange() {
      return Duration.between(now, nextExpectedStateChange);
   }

   public boolean isBeforeMiddleOfValidTradingDay() {
      switch (state) {
      case BEFORE_TODAYS_OPEN:
         return true;
      case OPEN:
         Duration time = Duration.between(lastMarketStateChange, nextExpectedStateChange);
         Duration half = time.dividedBy(2);
         ZonedDateTime mid = lastMarketStateChange.plus(half);
         return now.isBefore(mid);
      case AFTER_TODAYS_CLOSED:
      case NOT_OPEN_TODAY:
      default:
         return false;
      }
   }

   public Optional<Duration> timeSinceOpen() {
      switch (state) {
      case OPEN:
         return Optional.of(Duration.between(lastMarketStateChange, now));
      default:
         return Optional.empty();
      }
   }
}
