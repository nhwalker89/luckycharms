package luckycharms.util;

public class UncheckedException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   public UncheckedException(Exception e) {
      super(e);
   }
}
