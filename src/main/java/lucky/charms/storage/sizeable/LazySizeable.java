package lucky.charms.storage.sizeable;

import java.util.Objects;

public class LazySizeable implements ISizeable {

   private ISizeable compute;
   private Double size;

   public LazySizeable(ISizeable compute) {
      this.compute = Objects.requireNonNull(compute);
   }

   public LazySizeable(ISizeable s, ISizeable... compute) {
      this.compute = () -> ISizeable.sum(s, compute);
   }

   @Override
   public double byteSize() {
      Double r = size;
      if (r == null) {
         synchronized (this) {
            r = size;
            if (r == null) {
               r = compute.byteSize() + Sizes.DOUBLE;
               size = r;
               compute = null;
            }
         }
      }
      return r.doubleValue();
   }

}
