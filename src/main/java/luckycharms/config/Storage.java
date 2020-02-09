package luckycharms.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Storage {
   private static final org.slf4j.Logger sLog = org.slf4j.LoggerFactory.getLogger(Storage.class);

   public static final Path rootPath = Paths.get(SecretConfig.storageDirectory());
   public static final File rootDirectory = rootPath.toFile();

   public static final File propertyStoreFile = new File(rootDirectory,
         "property-store.properties");

   public static final File marketDayDatasetDir = new File(rootDirectory, "market-day-dataset");

   public static final File dailyPricesDatasetDir = new File(rootDirectory,
         "daily-prices-datasets");

   public static final File fifteenMinsPricesDatasetDir = new File(rootDirectory,
         "15Min-prices-datasets");

   public static final File portfolioDatasetDir = new File(rootDirectory, "portfolio-datasets");

   static {

      try {
         createDirIfMissing(rootDirectory);
         createDirIfMissing(marketDayDatasetDir);
         createDirIfMissing(dailyPricesDatasetDir);
         createDirIfMissing(fifteenMinsPricesDatasetDir);
         createDirIfMissing(portfolioDatasetDir);

      } catch (Exception e) {
         sLog.error("Problem setting up storage directories", e);
      }
   }

   private static void createDirIfMissing(File dir) throws IOException {
      if (!dir.exists()) {
         dir.mkdirs();
         if (!dir.exists()) {
            throw new IOException("Failed to setup storage directory " + dir);
         }
      }
   }

}
