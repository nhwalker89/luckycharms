package luckycharms.datasets.prices;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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
import luckycharms.time.units.FifteenMinutesKey;
import luckycharms.time.units.MonthsKey;
import luckycharms.util.progress.CountingProgressManager;
import luckycharms.util.progress.ProgressGui;

public class FifteenMinPriceDataSet
      extends SortedPagedDataSet<FifteenMinutesKey, PriceBar, MonthsKey> {

   public static void main(String[] args) {
      System.out.println("Clearing");
      StockUniverse.SP500.parallelStream().forEach(e -> {
         try {
            instance(e).clear();
         } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
      });

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
         .getLogger(FifteenMinPriceDataSet.class);
   private static final FifteenMinutesKey DATASET_DESIRED_START = FifteenMinutesKey
         .of(LocalDate.of(2016, 1, 1), LocalTime.MIDNIGHT);

   public static FifteenMinPriceDataSet instance(String symbol) {
      return instances.computeIfAbsent(symbol, FifteenMinPriceDataSet::new);
   }

   private static final File DAILY_PRICES_DS_DIR = Storage.fifteenMinsPricesDatasetDir;
   private final String symbol;

   private FifteenMinPriceDataSet(String symbol) {
      super(MonthsKey.FORMAT, FifteenMinutesKey.BYTE_FORMAT, PriceBar.BYTES_FORMAT,
            p -> p.asMonthKey(),
            new DirectoryStorageSingleSet(new File(DAILY_PRICES_DS_DIR, symbol).toPath()));
      this.symbol = symbol;
   }

   public String toString() {
      return this.getClass().getSimpleName() + "[" + symbol + "]";
   }

   public static void update(List<String> symbols) throws IOException {
      SetMultimap<Long, FifteenMinPriceDataSet> toUpdate = MultimapBuilder.hashKeys()
            .hashSetValues().build();
      for (String symbol : symbols) {
         FifteenMinPriceDataSet ds = FifteenMinPriceDataSet.instance(symbol);
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
            FifteenMinutesKey lastUpdateDay = FifteenMinutesKey.of(lastUpdateInMarketTime);
            Set<FifteenMinPriceDataSet> datasets = toUpdate.get(lastUpdate);
            fetchByDataSet(datasets, lastUpdateDay.minus(FifteenMinutesKey.NUMBER_PER_DAY * 10),
                  FifteenMinutesKey.now());/* 10 day overlap buffer */
            datasets.forEach(ds -> {
               ds.index().putLong("last_update", now);
               ds.saveIndex();
            });
         }
      }
   }

   public String getSymbol() { return symbol; }

   public KeyValuePair<FifteenMinutesKey, PriceBar> getLastFullPriceBar(FifteenMinutesKey target) {
      PriceBar bar = get(target);
      if (bar != null && bar.isFullyDefined()) {
         return new KeyValuePair<FifteenMinutesKey, PriceBar>(target, bar);
      }
      FifteenMinutesKey newTarget;
      int fence = FifteenMinutesKey.NUMBER_PER_DAY * 10;
      for (int i = 1; bar == null & i <= fence; i++) {
         newTarget = target.minus(i);
         bar = get(newTarget);
         if (bar != null && bar.isFullyDefined()) {
            return new KeyValuePair<FifteenMinutesKey, PriceBar>(newTarget, bar);
         }
      }
      return null;
   }

   private static void fetchByDataSet(Set<FifteenMinPriceDataSet> datasets, FifteenMinutesKey start,
         FifteenMinutesKey end) throws IOException {
      Set<String> ds = datasets.parallelStream().map(FifteenMinPriceDataSet::getSymbol)
            .collect(Collectors.toSet());
      fetch(ds, start, end);
   }

   public static void fetch(Set<String> set, FifteenMinutesKey start, FifteenMinutesKey end)
         throws IOException {
      Alpaca api = Alpaca.instance();
      List<String> symbols = new ArrayList<>(set);
      Collections.sort(symbols);

      try {
         api.getBulkPriceBar15Min(start, end, symbols, response -> {
            if (response.isSuccess()) {
               Map<String, List<KeyValuePair<FifteenMinutesKey, PriceBar>>> payload = response
                     .getPayload();
               for (Map.Entry<String, List<KeyValuePair<FifteenMinutesKey, PriceBar>>> entry : payload
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

   private static void restartDatasets(Set<FifteenMinPriceDataSet> datasets) throws IOException {

      datasets.forEach(t -> {
         try {
            t.clear();
         } catch (IOException e) {
            sLog.error("Problem clearing dataset {}", t.getSymbol());
         }
      });

      fetchByDataSet(datasets, DATASET_DESIRED_START, FifteenMinutesKey.now());

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

   private static final ConcurrentHashMap<String, FifteenMinPriceDataSet> instances = new ConcurrentHashMap<>();
}
