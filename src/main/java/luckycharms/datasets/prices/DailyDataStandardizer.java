package luckycharms.datasets.prices;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import luckycharms.alpaca.Alpaca;
import luckycharms.config.StockUniverse;
import luckycharms.json.JsonFactory;
import luckycharms.rest.RestException;
import luckycharms.rest.RestResponse;
import luckycharms.storage.KeyValuePair;
import luckycharms.time.units.DaysKey;
import luckycharms.util.DualBase;

public class DailyDataStandardizer {

   public static void main(String[] args) {
      try {
         RestResponse<Map<String, List<KeyValuePair<DaysKey, PriceBar>>>> body1 = Alpaca.instance()
               .dailyPriceData(DaysKey.of(2012, 02, 14), DaysKey.of(2012, 02, 16),
                     Arrays.asList("AAPL"));
         RestResponse<Map<String, List<KeyValuePair<DaysKey, PriceBar>>>> body2 = Alpaca.instance()
               .dailyPriceData(DaysKey.of(2012, 03, 13), DaysKey.of(2012, 03, 15),
                     Arrays.asList("AAPL"));
         System.out.println(body1.getRequest().toString());
         System.out.println(JsonFactory.makePretty(body1.getBody()));
         System.out.println();
         System.out.println(body2.getRequest().toString());
         System.out.println(JsonFactory.makePretty(body2.getBody()));
      } catch (RestException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      System.exit(0);

      String out = StockUniverse.SP500.parallelStream().//
            flatMap(symbol -> {
               DailyPriceDataSet ds = DailyPriceDataSet.instance(symbol);
               Stream<KeyValuePair<DaysKey, PriceBar>> values = ds.getAll(ds.keys());
               Stream<Stage1> values2 = values.map(Stage1.mapFn(symbol));
               return values2;
            })//
            .filter(Stage1::accept)//
            .map(Object::toString)//
            .collect(Collectors.joining(System.lineSeparator()));
      // .mapToDouble(pair -> pair.getValue().getVolume().doubleValue())//
      // .summaryStatistics();
      System.out.println(out);
   }

   private static class Stage1 extends DualBase<String, KeyValuePair<DaysKey, PriceBar>> {

      static Function<? super KeyValuePair<DaysKey, PriceBar>, ? extends Stage1> mapFn(
            String symbol) {
         return pair -> new Stage1(pair, symbol);
      }

      public boolean accept() {
         return price().isFullyDefined() && price().getVolume().doubleValue() < 0;
      }

      protected Stage1(KeyValuePair<DaysKey, PriceBar> a, String b) {
         super(b, a);
      }

      public String symbol() {
         return a;
      }

      public DaysKey day() {
         return b.getKey();
      }

      public PriceBar price() {
         return b.getValue();
      }

   }
}
