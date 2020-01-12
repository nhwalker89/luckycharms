package luckycharms.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.Callable;

public class UncheckedExceptions {

   public static <V> V wrap(Callable<V> call) {
      try {
         return call.call();
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      } catch (Exception t) {
         throw new UncheckedException(t);
      }
   }

}
