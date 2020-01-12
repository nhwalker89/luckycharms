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

   static {
      try {
         if (!rootDirectory.exists()) {
            rootDirectory.mkdirs();
            if (!rootDirectory.exists()) {
               throw new IOException("No storage directory despite attempt to create");
            }
         }
      } catch (Exception e) {
         sLog.error("Problem setting up storage directories", e);
      }
   }

}
