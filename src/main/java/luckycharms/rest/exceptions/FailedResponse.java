package luckycharms.rest.exceptions;

import java.io.IOException;

public class FailedResponse extends IOException {

   private static final long serialVersionUID = -2653857653919050748L;

   public FailedResponse() {
      super();
   }

   public FailedResponse(String message, Throwable cause) {
      super(message, cause);
   }

   public FailedResponse(String message) {
      super(message);
   }

   public FailedResponse(Throwable cause) {
      super(cause);
   }

}
