package luckycharms.datasets.prices;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

import luckycharms.alpaca.Alpaca;
import luckycharms.config.StockUniverse;
import luckycharms.config.Storage;
import luckycharms.storage.DirectoryStorageSingleSet;
import luckycharms.storage.KeyValuePair;
import luckycharms.storage.SortedPagedDataSet;
import luckycharms.time.MarketTimeUtils;
import luckycharms.time.units.DaysKey;
import luckycharms.time.units.MonthsKey;
import luckycharms.util.progress.CountingProgressManager;
import luckycharms.util.progress.ProgressGui;

public class DailyPriceDataSet extends SortedPagedDataSet<DaysKey, PriceBar, MonthsKey> {

   public static void main(String[] args) {
      System.out.println("Running Daily Data Update");

      try {
         update(StockUniverse.SP500);
      } catch (IOException e) {
         e.printStackTrace();
      }
      System.out.println("Finished.");
      System.exit(0);
   }

   private static final org.slf4j.Logger sLog = org.slf4j.LoggerFactory
         .getLogger(DailyPriceDataSet.class);
   private static final DaysKey DATASET_DESIRED_START = DaysKey.of(2008, 1, 1);

   public static DailyPriceDataSet instance(String symbol) {
      return instances.computeIfAbsent(symbol, DailyPriceDataSet::new);
   }

   private static final File DAILY_PRICES_DS_DIR = Storage.dailyPricesDatasetDir;
   private final String symbol;

   private DailyPriceDataSet(String symbol) {
      super(MonthsKey.FORMAT, DaysKey.BYTE_FORMAT, PriceBar.BYTES_FORMAT, p -> p.asMonthKey(),
            new DirectoryStorageSingleSet(new File(DAILY_PRICES_DS_DIR, symbol).toPath()));
      this.symbol = symbol;
   }

   public String toString() {
      return this.getClass().getSimpleName() + "[" + symbol + "]";
   }

   public static void update(List<String> symbols) throws IOException {
      SetMultimap<Long, DailyPriceDataSet> toUpdate = MultimapBuilder.hashKeys().hashSetValues()
            .build();
      for (String symbol : symbols) {
         DailyPriceDataSet ds = DailyPriceDataSet.instance(symbol);
         Long lastUpdate = ds.index().getLong("last_update", null);
         if (lastUpdate == null) {
            toUpdate.put(-1L, ds);
         } else {
            toUpdate.put(lastUpdate, ds);
         }
      }

      for (Long lastUpdate : toUpdate.keySet()) {
         if (lastUpdate.longValue() < 0) {
            restartDatasets(toUpdate.get(lastUpdate));
         } else {
            long now = System.currentTimeMillis();
            Instant lastUpdateInstant = Instant.ofEpochMilli(lastUpdate);
            ZonedDateTime lastUpdateInMarketTime = MarketTimeUtils.inMarketTime(lastUpdateInstant);
            DaysKey lastUpdateDay = DaysKey.of(lastUpdateInMarketTime);
            Set<DailyPriceDataSet> datasets = toUpdate.get(lastUpdate);
            fetchByDataSet(datasets, lastUpdateDay.minus(10),
                  DaysKey.now());/* 10 day overlap buffer */
            datasets.forEach(ds -> {
               ds.index().putLong("last_update", now);
               ds.saveIndex();
            });
         }
      }
   }

   public String getSymbol() { return symbol; }

   private static void fetchByDataSet(Set<DailyPriceDataSet> datasets, DaysKey start, DaysKey end)
         throws IOException {
      Set<String> ds = datasets.parallelStream().map(DailyPriceDataSet::getSymbol)
            .collect(Collectors.toSet());
      fetch(ds, start, end);
   }

   public static void fetch(Set<String> set, DaysKey start, DaysKey end) throws IOException {
      Alpaca api = Alpaca.instance();
      List<String> symbols = new ArrayList<>(set);
      Collections.sort(symbols);

      try {
         api.getBulkDailyBar(start, end, symbols, response -> {
            if (response.isSuccess()) {
               Map<String, List<KeyValuePair<DaysKey, PriceBar>>> payload = response.getPayload();
               for (Map.Entry<String, List<KeyValuePair<DaysKey, PriceBar>>> entry : payload
                     .entrySet()) {
                  if (set.contains(entry.getKey())) {
                     try {
                        instance(entry.getKey()).putAll(entry.getValue().stream());
                     } catch (IOException e) {
                        throw new UncheckedIOException(e);
                     }
                  } else {
                     sLog.error("Got a symbol {} in fetch response that wasn't asked for",
                           entry.getKey());
                  }
               }
            } else {
               sLog.error("Failure fetching daily day data", response, response.getError());
            }
         });
      } catch (UncheckedIOException e) {
         throw e.getCause();
      }
   }

   private static void restartDatasets(Set<DailyPriceDataSet> datasets) throws IOException {

      datasets.forEach(t -> {
         try {
            t.clear();
         } catch (IOException e) {
            sLog.error("Problem clearing dataset {}", t.getSymbol());
         }
      });

      fetchByDataSet(datasets, DATASET_DESIRED_START, DaysKey.now());

      ProgressGui<CountingProgressManager> progressReport = ProgressGui
            .openPopup("Updating Indexes", true, datasets.size());
      long now = System.currentTimeMillis();
      datasets.forEach(ds -> {
         ds.index().putLong("last_update", now);
         ds.saveIndex();
         progressReport.getProgressManager()
               .incrementCompleted("Updated Index For " + ds.getSymbol());
      });
   }

   @Override
   protected String formatPagedData(SortedPagedData data) {
      return formatPagedDataSingleLine(data);
   }

   private static final ConcurrentHashMap<String, DailyPriceDataSet> instances = new ConcurrentHashMap<>();
}
