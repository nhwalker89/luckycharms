package luckycharms.config;

import java.io.InputStream;
import java.util.Properties;

import luckycharms.util.LazyRef;

public class StandardConfig {
   private StandardConfig() {
      throw new Error("Never Create");
   }

   private static final org.slf4j.Logger sLog = org.slf4j.LoggerFactory
         .getLogger(SecretConfig.class);

   private static final LazyRef<Properties> sProps = new LazyRef<>(StandardConfig::load);
   private static volatile boolean isRestProgressReportsEnabled = false;

   private static Properties load() {
      Properties p = new Properties();
      try (InputStream s = ClassLoader.getSystemResourceAsStream("standard.properties")) {
         p.load(s);
         isRestProgressReportsEnabled = //
               Boolean.parseBoolean(p.getProperty("isRestProgressReportsEnabled", "false"));
      } catch (Exception e) {
         sLog.error("Couldn't load standard properties");
      }
      return p;
   }

   public static boolean isRestProgressReportsEnabled() {
      sProps.get();
      return isRestProgressReportsEnabled;
   }

}
