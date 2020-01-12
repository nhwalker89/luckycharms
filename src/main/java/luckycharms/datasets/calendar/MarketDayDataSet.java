package luckycharms.datasets.calendar;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;

import luckycharms.config.Storage;
import luckycharms.storage.DirectoryStorageSingleSet;
import luckycharms.storage.SortedPagedDataSet;
import luckycharms.time.MarketTimeUtils;
import luckycharms.time.units.DaysKey;
import luckycharms.time.units.YearsKey;

public class MarketDayDataSet extends SortedPagedDataSet<DaysKey, MarketDayData, YearsKey> {
   @SuppressWarnings("unused")
   private static final LocalDate DATASET_DESIRED_START = LocalDate.of(2008, 1, 1);

   public static MarketDayDataSet instance() {
      return InstanceHolder.sInstance;
   }

   private static final File CALENDAR_DS_FILE = Storage.marketDayDatasetDir;

   private MarketDayDataSet() {
      super(YearsKey.FORMAT, DaysKey.BYTE_FORMAT, MarketDayData.FORMAT, DaysKey::asYearKey,
            new DirectoryStorageSingleSet(CALENDAR_DS_FILE.toPath()));
   }

   private static class InstanceHolder {
      private static final MarketDayDataSet sInstance = new MarketDayDataSet();
   }

   public void update() throws IOException {
      Long lastUpdate = index().getLong("last_update", null);
      if (lastUpdate == null) {
         restartDataset();
      } else {
         Instant instant = Instant.ofEpochMilli(lastUpdate);
         LocalDate lastUpdateDate = MarketTimeUtils.inMarketTime(instant).toLocalDate();
         if (lastUpdateDate.isBefore(MarketTimeUtils.now().toLocalDate())) {
//            fetch(lastUpdateDate.minusDays(10), LocalDate.now());/* 10 day overlap buffer */
         } // else we are already up to date
         index().putLong("last_update", System.currentTimeMillis());
         saveIndex();
      }
   }

   public void restartDataset() throws IOException {
      clear();
//      fetch(DATASET_DESIRED_START, LocalDate.now());
      index().putLong("last_update", System.currentTimeMillis());
   }

   @Override
   protected String formatPagedData(SortedPagedData data) {
      return formatPagedDataSingleLine(data);
   }

//   public void fetch(LocalDate start, LocalDate end) throws IOException {
//      try {
//         HttpResponse<CalendarEndpointResponse> endpoint = AlpacaApi.calendar(start, end);
//         List<KeyValuePair<DaysKey, MarketDayData>> results = endpoint.getBody().stream()
//               .map(point -> {
//                  MarketDayData data = new MarketDayData(point);
//                  return new KeyValuePair<>(DaysKey.of(LocalDate.parse(point.date).toEpochDay()),
//                        data);
//               }).collect(Collectors.toList());
//
//         putAll(results.stream());
//      } catch (InterruptedException e) {
//         throw new IOException("Problem during request", e);
//      }
//   }
}
