package lucky.charms.portfolio;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableList;

import luckycharms.config.Storage;
import luckycharms.storage.DirectoryStorageSingleSet;
import luckycharms.storage.FileNameSanatizer;
import luckycharms.storage.SortedPagedDataSet;
import luckycharms.time.units.DaysKey;
import luckycharms.time.units.MonthsKey;

public class PortfolioDataSet extends SortedPagedDataSet<DaysKey, PortfolioWorth, MonthsKey> {

   public static List<String> getSavedPortfolios() {
      return ImmutableList.copyOf(DAILY_PRICES_DS_DIR.list());
   }

   public static PortfolioDataSet instance(String symbol) {
      return instances.computeIfAbsent(symbol, PortfolioDataSet::new);
   }

   private static final File DAILY_PRICES_DS_DIR = Storage.portfolioDatasetDir;
   private final String name;

   public PortfolioDataSet(String name) {
      super(MonthsKey.FORMAT, DaysKey.BYTE_FORMAT, PortfolioWorth.BYTE_FORMAT, DaysKey::asMonthKey,
            new DirectoryStorageSingleSet(new File(DAILY_PRICES_DS_DIR, //
                  FileNameSanatizer.sanatize(name)).toPath()));
      this.name = name;
   }

   public String getName() { return name; }

   private static final ConcurrentHashMap<String, PortfolioDataSet> instances = new ConcurrentHashMap<>();

}
