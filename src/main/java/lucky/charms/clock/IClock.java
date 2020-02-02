package lucky.charms.clock;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.common.collect.Range;

import luckycharms.datasets.calendar.MarketDayDataSet;
import luckycharms.time.MarketTimeUtils;
import luckycharms.time.units.DaysKey;

public interface IClock {

   MarketTimeStateData marketTimeState();

   void waitUntil(ZonedDateTime data) throws InterruptedException;

   void waitUntilOpen() throws InterruptedException;

   void waitUntilMarketClose() throws InterruptedException;

   boolean hasMoreDays();

   public static class FakeClock implements IClock {

      private final DaysKey start;
      private final DaysKey end;
      private final ZonedDateTime lastMoment;

      private final NavigableSet<DaysKey> openDays;

      private DaysKey currentDay;
      private LocalTime currentTime;

      public FakeClock(DaysKey start, DaysKey end) {
         this.openDays = MarketDayDataSet.instance().keys(Range.closed(start, end))
               .collect(Collectors.toCollection(TreeSet::new));
         this.start = openDays.first();
         this.end = openDays.last();

         this.lastMoment = MarketTimeUtils.inMarketTime(end, LocalTime.MAX);

         this.currentDay = this.start;
         this.currentTime = LocalTime.MIDNIGHT;
      }

      @Override
      public MarketTimeStateData marketTimeState() {
         ZonedDateTime now = MarketTimeUtils.inMarketTime(currentDay, currentTime);
         return MarketTimeStateData.loadState(now);
      }

      @Override
      public void waitUntil(ZonedDateTime target) throws InterruptedException {
         ZonedDateTime waitForIt = MarketTimeUtils.inMarketTime(target);
         ZonedDateTime now = MarketTimeUtils.inMarketTime(currentDay, currentTime);
         if (now.compareTo(waitForIt) >= 0) {
            // already past so do nothing
            return;
         } else if (waitForIt.compareTo(lastMoment) > 0) {
            // don't advance past the last moment this clock supports
            currentDay = end;
            currentTime = lastMoment.toLocalTime();
         } else {
            // update to new date
            currentDay = DaysKey.of(waitForIt);
            currentTime = waitForIt.toLocalTime();
         }
      }

      @Override
      public void waitUntilOpen() throws InterruptedException {
         MarketTimeStateData data = marketTimeState();
         while (!data.isOpen()) {
            switch (data.openState()) {
            case OPEN:
               // already open (shouldn't reach)
               break;
            case BEFORE_TODAYS_OPEN:
            case AFTER_TODAYS_CLOSED:
            case NOT_OPEN_TODAY:
            default:
               waitUntil(data.nextExpectedMarketStateChange());
               break;
            }
            data = marketTimeState();
         }
      }

      @Override
      public void waitUntilMarketClose() throws InterruptedException {
         MarketTimeStateData data = marketTimeState();
         while (data.isOpen()) {
            switch (data.openState()) {
            case OPEN:
               waitUntil(data.nextExpectedMarketStateChange());
               break;
            case BEFORE_TODAYS_OPEN:
            case AFTER_TODAYS_CLOSED:
            case NOT_OPEN_TODAY:
            default:
               // already closed (shouldn't reach)
               break;
            }
            data = marketTimeState();
         }
      }

      @Override
      public boolean hasMoreDays() {
         return currentDay.compareTo(end) < 0;
      }

   }
}
