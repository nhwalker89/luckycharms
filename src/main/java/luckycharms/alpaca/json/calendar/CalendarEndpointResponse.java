package luckycharms.alpaca.json.calendar;

import java.util.ArrayList;

public class CalendarEndpointResponse extends ArrayList<CalendarEndpointResponse.CalendarPoint> {

   private static final long serialVersionUID = 1L;

   public class CalendarPoint {

      public String date;
      public String open;
      public String close;

   }

}
