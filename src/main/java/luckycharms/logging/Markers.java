package luckycharms.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Markers {
   public static final Marker REST = MarkerFactory.getMarker("REST");
   public static final Marker ALPACA = MarkerFactory.getMarker("ALPACA");
   static {
      ALPACA.add(REST);
   }
}
