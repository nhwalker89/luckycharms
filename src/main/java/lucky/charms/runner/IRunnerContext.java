package lucky.charms.runner;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import lucky.charms.clock.IClock;

public interface IRunnerContext {

   List<String> getSymbols();

   default Map<String, Double> currentPrices(Stream<String> symbols) {
      return currentPrices(symbols.iterator());
   }

   default Map<String, Double> currentPrices(Collection<String> symbols) {
      return currentPrices(symbols.iterator());
   }

   Map<String, Double> currentPrices(Iterator<String> symbols);

   boolean isRobust();

   boolean isLive();

   IClock clock();

}