package luckycharms.util;

import java.util.function.Supplier;

public class ExpiringValue<T> {
   private final Supplier<T> factory;
   private final long limit;

   private T value = null;
   private long expires = 0L;

   public ExpiringValue(Supplier<T> sup, long lifespanMillis) {
      factory = sup;
      limit = lifespanMillis;
   }

   public synchronized T get() {
      long now = -1;
      if (value == null || (now = System.currentTimeMillis()) > expires) {
         value = factory.get();
         expires = (now < 0 ? System.currentTimeMillis() : now) + limit;
      }
      return value;
   }

}
