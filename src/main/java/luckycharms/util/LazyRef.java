package luckycharms.util;

import java.util.Objects;
import java.util.function.Supplier;

public class LazyRef<T> {
   private volatile Supplier<T> sup;
   private volatile T value;

   public LazyRef(Supplier<T> sup) {
      this.sup = sup;
   }

   public void init() {
      get();
   }

   public T get() {
      if (sup != null) {
         synchronized (this) {
            if (sup != null) {
               value = sup.get();
               sup = null;
            }
         }
      }
      return value;
   }

   public boolean isInitialized() { return sup == null; }

   @Override
   public String toString() {
      return "LazyRef[" + (isInitialized() ? get() : "<uninitialized>") + "]";
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(get());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (obj == this) {
         return true;
      } else if (obj != this.getClass()) {
         return false;
      }
      return Objects.equals(this.get(), ((LazyRef<?>) obj).get());
   }
}
