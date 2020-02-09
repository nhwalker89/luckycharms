package luckycharms.datasets.calendar;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Range;

import luckycharms.alpaca.Alpaca;
import luckycharms.config.Storage;
import luckycharms.rest.RestResponse;
import luckycharms.storage.DirectoryStorageSingleSet;
import luckycharms.storage.KeyValuePair;
import luckycharms.storage.SortedPagedDataSet;
import luckycharms.time.MarketTimeUtils;
import luckycharms.time.units.DaysKey;
import luckycharms.time.units.YearsKey;

public class MarketDayDataSet extends SortedPagedDataSet<DaysKey, MarketDayData, YearsKey> {
   private static final File CALENDAR_DS_FILE = Storage.marketDayDatasetDir;

   public static void main(String[] args) {
      try {
         MarketDayDataSet.instance().restartDataset();
      } catch (IOException e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
      MarketDayDataSet.instance().getAll(Range.all()).forEach(e -> {
         System.out.println(e.getValue());
      });
      System.out.println("Done");
   }

   private static final org.slf4j.Logger sLog = org.slf4j.LoggerFactory
         .getLogger(MarketDayDataSet.class);
   private static final LocalDate DATASET_DESIRED_START = LocalDate.of(2008, 1, 1);

   public static MarketDayDataSet instance() {
      return InstanceHolder.sInstance;
   }

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
            fetch(lastUpdateDate.minusDays(10),
                  LocalDate.now().plusDays(3));/* -10/+3 day overlap buffer */
         } // else we are already up to date
         index().putLong("last_update", System.currentTimeMillis());
         saveIndex();
      }
   }

   public void restartDataset() throws IOException {
      clear();
      fetch(DATASET_DESIRED_START, LocalDate.now().plusDays(3));
      index().putLong("last_update", System.currentTimeMillis());
      saveIndex();
   }

   @Override
   protected String formatPagedData(SortedPagedData data) {
      return formatPagedDataSingleLine(data);
   }

   public void fetch(LocalDate start, LocalDate end) throws IOException {
      RestResponse<List<MarketDayData>> endpoint = Alpaca.instance().calendar(start, end);
      if (endpoint.isSuccess()) {
         List<KeyValuePair<DaysKey, MarketDayData>> results = endpoint.getPayload().stream()
               .map(point -> {
                  return new KeyValuePair<>(point.getDay(), point);
               }).collect(Collectors.toList());

         putAll(results.stream());
      } else {
         sLog.error("Failure fetching market day data set", endpoint, endpoint.getError());
      }
   }
}
