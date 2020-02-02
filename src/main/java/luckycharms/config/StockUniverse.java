package luckycharms.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.common.collect.ImmutableList;

public class StockUniverse {
   private static final org.slf4j.Logger sLog = org.slf4j.LoggerFactory
         .getLogger(StockUniverse.class);

   public static final ImmutableList<String> SP500;
   static {
      ImmutableList<String> list;
      try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(ClassLoader.getSystemResourceAsStream("sp500.txt")))) {
         list = reader.lines().collect(ImmutableList.toImmutableList());
      } catch (IOException e) {
         sLog.error("Problem reading S&P 500", e);
         list = ImmutableList.of();
      }
      SP500 = list;
   }

//   public static void main(String[] args) {
//      System.out.println(SP500);
//   }
}
