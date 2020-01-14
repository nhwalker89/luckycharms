package luckycharms.config;

import java.io.InputStream;
import java.util.Properties;

import luckycharms.util.LazyRef;

public final class SecretConfig {
   private SecretConfig() {
      throw new Error("Never Create");
   }

   private static final org.slf4j.Logger sLog = org.slf4j.LoggerFactory
         .getLogger(SecretConfig.class);
   private static final LazyRef<Properties> sProps = new LazyRef<>(SecretConfig::load);

   private static volatile String storageDirectory = null;

   private static volatile String alpacaKeyID = null;
   private static volatile String alpacaSecretKey = null;

   private static Properties load() {
      Properties p = new Properties();
      try (InputStream s = ClassLoader.getSystemResourceAsStream("secret.properties")) {
         p.load(s);
      } catch (Exception e) {
         sLog.error("Couldn't load secret properties");
      }

      // load
      storageDirectory = p.getProperty("storageDirectory", "./storage");
      alpacaKeyID = p.getProperty("alpacaKeyID");
      alpacaSecretKey = p.getProperty("alpacaSecretKey");

      verify();

      return p;
   }

   private static void verify() {
      if (alpacaKeyID == null) {
         sLog.error("Alpaca Key ID Missing");
      }
      if (alpacaSecretKey == null) {
         sLog.error("Alpaca Key ID Missing");
      }
   }

   public static String storageDirectory() {
      sProps.init();
      return storageDirectory;
   }

   public static String alpacaKeyID() {
      sProps.init();
      return alpacaKeyID;
   }

   public static String alpacaSecretKey() {
      sProps.init();
      return alpacaSecretKey;
   }
}
