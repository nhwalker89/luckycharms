package luckycharms.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import luckycharms.util.LazyRef;

public class PropertyStore {
   private PropertyStore() {
      throw new Error("Never Create");
   }

   private static final org.slf4j.Logger sLog = org.slf4j.LoggerFactory
         .getLogger(SecretConfig.class);

   private static final LazyRef<Properties> sProps = new LazyRef<>(PropertyStore::load);

   private static Properties load() {
      Properties p = new Properties();
      if (Storage.propertyStoreFile.exists()) {
         try (InputStream s = new FileInputStream(Storage.propertyStoreFile)) {
            p.load(s);
         } catch (Exception e) {
            sLog.error("Couldn't load property store");
         }
      }
      // load

      return p;
   }

   @SuppressWarnings("unused")
   private synchronized static void save() {
      sProps.init();
      try (OutputStream s = new FileOutputStream(Storage.propertyStoreFile)) {
         sProps.get().store(s, "Lucky Charms Property Store");
      } catch (IOException e) {
         sLog.error("Couldn't save properties");
      }
   }

}
