package luckycharms.runner;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import luckycharms.clock.IClock;

public interface IRunnerContext {

   List<String> getSymbols();

   default Map<String, Double> currentPrices(Stream<String> symbols, EPriceHint hint) {
      return currentPrices(symbols.iterator(), hint);
   }

   default Map<String, Double> currentPrices(Collection<String> symbols, EPriceHint hint) {
      return currentPrices(symbols.iterator(), hint);
   }

   Map<String, Double> currentPrices(Iterator<String> symbols, EPriceHint hint);

   boolean isRobust();

   boolean isLive();

   IClock clock();

}