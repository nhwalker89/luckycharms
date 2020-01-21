package lucky.charms.clock;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.common.collect.Range;

import luckycharms.datasets.calendar.MarketDayData;
import luckycharms.datasets.calendar.MarketDayDataSet;
import luckycharms.time.MarketTimeUtils;
import luckycharms.time.units.DaysKey;

public abstract class Clock {
   private static final org.slf4j.Logger sLog = org.slf4j.LoggerFactory.getLogger(Clock.class);

//   public static Clock realClock() {
//      return new RealClock();
//   }

   public static Clock fakeClock(DaysKey start, DaysKey end) {
      return new FakeClock(start, end);
   }

   public abstract ZonedDateTime now();

   public abstract boolean canAdvance();

   public abstract void waitUntil(ZonedDateTime time) throws InterruptedException, ClockException;

   public abstract void waitFor(Duration time) throws InterruptedException, ClockException;

   public abstract ZonedDateTime nextMarketOpen() throws ClockException;

   public abstract ZonedDateTime nextMarketClose() throws ClockException;

   public abstract ZonedDateTime nowIfMarketOpen() throws ClockException;

   public abstract ZonedDateTime nowIfMarketClosed() throws ClockException;

   public abstract ZonedDateTime waitForMarketOpen() throws InterruptedException, ClockException;

   public abstract ZonedDateTime waitForMarketClose() throws InterruptedException, ClockException;

   public abstract boolean isMarketOpen() throws ClockException;

   public abstract MarketDayData getTodaysMarketData() throws ClockException;

   public boolean isAfterNoon() { return now().toLocalTime().isAfter(LocalTime.NOON); }

   public boolean isBeforeNoon() { return now().toLocalTime().isBefore(LocalTime.NOON); }

   public boolean isBeforeMiddleOfValidTradingDay() throws ClockException {
      ZonedDateTime now = now();

      MarketDayData data = getTodaysMarketData();

      DaysKey todayKey = DaysKey.of(now);
      if (!data.getDay().equals(todayKey)) {
         // we must be doing this right as the day
         // rolled over to the next day. So not before
         // mid day
         return false;
      }

      if (!data.validMarketDay()) {
         // not a valid trading day
         return false;
      }
      LocalTime open = data.getOpen();
      LocalTime closed = data.getClose();
      LocalTime midTradingDay = open
            .plus(Duration.between(open, closed).dividedBy(2).truncatedTo(ChronoUnit.MINUTES));
      return now.toLocalTime().isBefore(midTradingDay);

   }

   public static class FakeClock extends Clock {

      private final DaysKey start;
      private final DaysKey end;
      private final ZonedDateTime lastMoment;

      private final NavigableSet<DaysKey> openDays;

      private DaysKey currentDay;
      private LocalTime currentTime;
      private MarketDayData marketDayData;

      public FakeClock(DaysKey start, DaysKey end) {
         this.openDays = MarketDayDataSet.instance().keys(Range.closed(start, end))
               .collect(Collectors.toCollection(TreeSet::new));
         this.start = openDays.first();
         this.end = openDays.last();

         this.lastMoment = MarketTimeUtils.inMarketTime(end, LocalTime.MAX);

         this.currentDay = this.start;
         this.currentTime = LocalTime.MIDNIGHT;
         this.marketDayData = MarketDayDataSet.instance().get(currentDay);
      }

      @Override
      public ZonedDateTime now() {
         return MarketTimeUtils.inMarketTime(currentDay, currentTime);
      }

      @Override
      public boolean canAdvance() {
         return currentDay.compareTo(end) < 0;
      }

      @Override
      public void waitUntil(ZonedDateTime time) throws InterruptedException, ClockException {
         ZonedDateTime waitForIt = MarketTimeUtils.inMarketTime(time);
         if (now().compareTo(waitForIt) >= 0) {
            // already past so do nothing
            return;
         } else if (waitForIt.compareTo(lastMoment) > 0) {
            // don't advance past the last moment this clock supports
            currentDay = end;
            currentTime = lastMoment.toLocalTime();
         } else {
            // update to new date
            currentDay = DaysKey.of(waitForIt);
            currentTime = time.toLocalTime();
            marketDayData = MarketDayDataSet.instance().get(currentDay);
            if (marketDayData != null && !marketDayData.validMarketDay()) {
               marketDayData = null;
            }
         }

      }

      @Override
      public void waitFor(Duration time) throws InterruptedException, ClockException {
         waitUntil(now().plus(time));
      }

      @Override
      public ZonedDateTime nextMarketOpen() throws ClockException {
         DaysKey day = currentDay;
         MarketDayData data = marketDayData;
         if (currentTime.compareTo(data.getOpen()) > 0) {
            day = openDays.higher(day);
            data = MarketDayDataSet.instance().get(day);
            if (data != null && !data.validMarketDay()) {
               data = null;
            }
         }

         while (data == null && day.compareTo(end) < 0) {
            day = openDays.higher(day);
            data = MarketDayDataSet.instance().get(day);
            if (data != null && !data.validMarketDay()) {
               data = null;
            }
         }
         if (data == null) {
            throw new ClockException("No more dates remaining");
         }
         return MarketTimeUtils.inMarketTime(day, data.getOpen());
      }

      @Override
      public ZonedDateTime nextMarketClose() throws ClockException {
         DaysKey day = currentDay;
         MarketDayData data = marketDayData;
         if (currentTime.compareTo(data.getClose()) > 0) {
            day = openDays.higher(day);
            data = MarketDayDataSet.instance().get(day);
            if (data != null && !data.validMarketDay()) {
               data = null;
            }
         }

         while (data == null && day.compareTo(end) < 0) {
            day = openDays.higher(day);
            data = MarketDayDataSet.instance().get(day);
            if (data != null && !data.validMarketDay()) {
               data = null;
            }
         }
         if (data == null) {
            throw new ClockException("No more dates remaining");
         }
         return MarketTimeUtils.inMarketTime(day, data.getClose());
      }

      @Override
      public ZonedDateTime nowIfMarketOpen() throws ClockException {
         if (isMarketOpen()) {
            return now();
         }
         return null;
      }

      @Override
      public ZonedDateTime nowIfMarketClosed() throws ClockException {
         if (!isMarketOpen()) {
            return now();
         }
         return null;
      }

      public ZonedDateTime waitForMarketOpen() throws InterruptedException, ClockException {
         ZonedDateTime nowIfOpen = nowIfMarketOpen();
         if (nowIfOpen == null) {
            return nowIfOpen;
         }

         waitUntil(nextMarketOpen());

         return waitForMarketOpen();
      }

      public ZonedDateTime waitForMarketClose() throws InterruptedException, ClockException {
         ZonedDateTime nowIfClosed = nowIfMarketClosed();
         if (nowIfClosed == null) {
            return nowIfClosed;
         }

         waitUntil(nextMarketClose());

         return waitForMarketClose();
      }

      @Override
      public boolean isMarketOpen() throws ClockException {
         return marketDayData != null && marketDayData.getOpen().compareTo(currentTime) <= 0
               && marketDayData.getClose().compareTo(currentTime) > 0;
      }

      @Override
      public MarketDayData getTodaysMarketData() throws ClockException {
         return marketDayData == null ? new MarketDayData(currentDay, null, null) : marketDayData;
      }
   }

//   public static class RealClock extends Clock {
//
//      @Override
//      public ZonedDateTime now() {
//         return MarketTimeUtils.now();
//      }
//
//      @Override
//      public boolean canAdvance() {
//         return true;
//      }
//
//      @Override
//      public void waitUntil(ZonedDateTime time) throws InterruptedException {
//         long deadline = time.toInstant().toEpochMilli();
//         long wait;
//         while (Math.max(wait = deadline - System.currentTimeMillis(), 3000) > 0) {
//            Thread.sleep(wait);
//         }
//      }
//
//      @Override
//      public void waitFor(Duration time) throws InterruptedException {
//         waitUntil(now().plus(time));
//      }
//
//      @Override
//      public ZonedDateTime nextMarketOpen() throws ClockException {
//         ClockEndpoint clock = currentClock.get();
//         if (clock == null) {
//            throw new ClockException("Problem fetching current clock");
//         } else if (clock.nextOpen == null) {
//            throw new ClockException("Problem fetching current clock. Missing nextOpen.");
//         } else {
//            return clock.nextOpen;
//         }
//      }
//
//      @Override
//      public ZonedDateTime nextMarketClose() throws ClockException {
//         ClockEndpoint clock = currentClock.get();
//         if (clock == null) {
//            throw new ClockException("Problem fetching current clock");
//         } else if (clock.nextClose == null) {
//            throw new ClockException("Problem fetching current clock. Missing nextClose.");
//         } else {
//            return clock.nextClose;
//         }
//      }
//
//      @Override
//      public ZonedDateTime nowIfMarketOpen() throws ClockException {
//         ClockEndpoint clock = currentClock.get();
//         if (clock == null) {
//            throw new ClockException("Problem fetching current clock");
//         } else if (clock.isOpen == null) {
//            throw new ClockException("Problem fetching current clock. Missing isOpen data");
//         } else if (clock.isOpen.booleanValue()) {
//            return clock.timestamp;
//         }
//         return null;
//      }
//
//      @Override
//      public ZonedDateTime nowIfMarketClosed() throws ClockException {
//         ClockEndpoint clock = currentClock.get();
//         if (clock == null) {
//            throw new ClockException("Problem fetching current clock");
//         } else if (clock.isOpen == null) {
//            throw new ClockException("Problem fetching current clock. Missing isOpen data");
//         } else if (!clock.isOpen.booleanValue()) {
//            return clock.timestamp;
//         }
//         return null;
//      }
//
//      @Override
//      public boolean isMarketOpen() throws ClockException {
//         ClockEndpoint clock = currentClock.get();
//         if (clock == null) {
//            throw new ClockException("Problem fetching current clock");
//         } else if (clock.isOpen == null) {
//            throw new ClockException("Problem fetching current clock. Missing isOpen data");
//         }
//         return clock.isOpen.booleanValue();
//      }
//
//      @Override
//      public ZonedDateTime waitForMarketOpen() throws InterruptedException, ClockException {
//         ZonedDateTime time = null;
//         long wait;
//         while (time == null) {
//            wait = CLOCK_REFRESH_LIMIT;
//            ClockEndpoint clock = currentClock.get();
//            if (clock == null) {
//               throw new ClockException("Could not get current clock data");
//            } else if (clock.isOpen == null || clock.timestamp == null) {
//               throw new ClockException(
//                     "Current clock data was missing required values (" + clock + ")");
//            } else if (clock.isOpen.booleanValue()) {
//               time = clock.timestamp;
//               wait = 0;
//            } else if (clock.nextOpen != null) {
//               wait = clamp(//
//                     Duration.between(Instant.now(), clock.nextOpen.toInstant()).toMillis(), //
//                     1_000, 60_000);
//            }
//            if (wait > 0) {
//               Thread.sleep(wait);
//            }
//         }
//         return time;
//      }
//
//      private static long clamp(long value, long min, long max) {
//         if (value < min) {
//            return min;
//         }
//         if (value > max) {
//            return max;
//         }
//         return value;
//      }
//
//      @Override
//      public ZonedDateTime waitForMarketClose() throws InterruptedException, ClockException {
//         ZonedDateTime time = null;
//         long wait;
//         while (time == null) {
//            wait = 5L;
//            ClockEndpoint clock = currentClock.get();
//            if (clock == null) {
//               throw new ClockException("Could not get current clock data");
//            } else if (clock.isOpen == null || clock.timestamp == null) {
//               throw new ClockException(
//                     "Current clock data was missing required values (" + clock + ")");
//            } else if (!clock.isOpen.booleanValue()) {
//               time = clock.timestamp;
//               wait = 0;
//            } else if (clock.nextClose != null) {
//               wait = clamp(//
//                     Duration.between(Instant.now(), clock.nextClose.toInstant()).toMillis(), //
//                     1_000, 60_000);
//            }
//            if (wait > 0) {
//               Thread.sleep(wait);
//            }
//         }
//         return time;
//      }
//
//      @Override
//      public MarketDayData getTodaysMarketData() throws ClockException {
//         return currentMarketDayData.get();
//      }
//
//      private static final long CLOCK_REFRESH_LIMIT = 500;
//      private static final long CURRENT_MARKET_DAY_REFRESH_LIMIT = 1000;
//
//      private final ExpiringValue<ClockEndpoint> currentClock = new ExpiringValue<>(
//            RealClock::getCurrentClockEndpoint, CLOCK_REFRESH_LIMIT);
//
//      private final ExpiringValue<MarketDayData> currentMarketDayData = new ExpiringValue<>(
//            RealClock::todaysCalendar, CURRENT_MARKET_DAY_REFRESH_LIMIT);
//
//      private static MarketDayData todaysCalendar() {
//         DaysKey today = DaysKey.now();
//         LocalDate date = today.time().toLocalDate();
//         try {
//            HttpResponse<CalendarEndpointResponse> response = AlpacaApi.calendar(date, date);
//            CalendarEndpointResponse body = response.getBody();
//            for (CalendarPoint value : body) {
//               MarketDayData data = new MarketDayData(value);
//               return data;
//            }
//         } catch (Exception e) {
//            sLog.error("Problem fetching clock endpoint", e);
//         }
//         return new MarketDayData(today, null, null);
//      }
//
//      private static ClockEndpoint getCurrentClockEndpoint() {
//
//         try {
//            ClockEndpoint endpoint = AlpacaApi.clock().getBody();
//            if (endpoint != null && endpoint.timestamp != null) {
//               long currentTime = System.currentTimeMillis();
//               long endpointTime = endpoint.timestamp.toInstant().toEpochMilli();
//               long delta = currentTime - endpointTime;
//               if (delta > 1000) {
//                  sLog.error(
//                        "Delta Between System Clock and Alpaca Clock is > 1 second ({} millis)",
//                        delta);
//               } else if (sLog.isDebugEnabled()) {
//                  sLog.debug("Delta Between System Clock and Alpaca Clock is {} millis", delta);
//               }
//            }
//            return endpoint;
//         } catch (Exception e) {
//            sLog.error("Problem fetching clock endpoint", e);
//            return null;
//         }
//
//      }
//   }
}
