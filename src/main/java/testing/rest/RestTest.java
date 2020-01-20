package testing.rest;

import luckycharms.logging.Markers;

public class RestTest {
   private static final org.slf4j.Logger sLog = org.slf4j.LoggerFactory.getLogger(RestTest.class);

   public static void main(String[] args) {
      sLog.info(Markers.ALPACA, "Alpaca");
      sLog.info(Markers.REST,
            "ABCDEFGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
      sLog.error("121");
   }

}
