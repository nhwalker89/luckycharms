package luckycharms.util;

import java.util.Objects;

public abstract class DualBase<A, B> {
   protected final A a;
   protected final B b;

   protected DualBase(A a, B b) {
      this.a = a;
      this.b = b;
   }

   @Override
   public String toString() {
      return a + "=" + b;
   }

   @Override
   public int hashCode() {
      return Objects.hash(a, b);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      DualBase<?, ?> other = (DualBase<?, ?>) obj;
      return Objects.equals(a, other.a) && Objects.equals(b, other.b);
   }
}
