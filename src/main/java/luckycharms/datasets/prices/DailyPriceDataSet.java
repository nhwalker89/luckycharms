package luckycharms.datasets.prices;

import java.io.File;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;

import luckycharms.config.Storage;
import luckycharms.storage.DirectoryStorageSingleSet;
import luckycharms.storage.PagedDataSet;
import luckycharms.time.units.DaysKey;
import luckycharms.time.units.MonthsKey;

public class DailyPriceDataSet extends PagedDataSet<DaysKey, PriceBar, MonthsKey> {

   private static final org.slf4j.Logger sLog = org.slf4j.LoggerFactory
         .getLogger(DailyPriceDataSet.class);
   @SuppressWarnings("unused")
   private static final LocalDate DATASET_DESIRED_START = LocalDate.of(2008, 1, 1);

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

   private static final ConcurrentHashMap<String, DailyPriceDataSet> instances = new ConcurrentHashMap<>();
}
