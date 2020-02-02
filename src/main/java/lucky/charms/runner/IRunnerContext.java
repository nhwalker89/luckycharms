package lucky.charms.runner;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucky.charms.clock.IClock;

public interface IRunnerContext {

   List<String> getSymbols();

   Map<String, Double> currentPrices(Iterator<String> symbols);

   boolean isRobust();

   boolean isLive();

   IClock clock();

}