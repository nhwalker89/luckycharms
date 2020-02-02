package luckycharms.logging;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class log4j_Markers {
   public static final Marker REST = MarkerManager.getMarker("REST");
   public static final Marker ALPACA = MarkerManager.getMarker("ALPACA");

   static {
      ALPACA.addParents(REST);
   }

   public static final Marker REPORT = MarkerManager.getMarker("REPORT");

}
