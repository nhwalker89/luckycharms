package luckycharms.storage.sizeable;

import java.util.Objects;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import com.google.common.cache.Weigher;

@FunctionalInterface
public interface ISizeable {
   static ISizeable ZERO = () -> 0.0;
   static ISizeable ONE = () -> 1.0;

   static Weigher<ISizeable, ISizeable> WEIGHER = new Weigher<ISizeable, ISizeable>() {
      @Override
      public int weigh(ISizeable key, ISizeable value) {
         long size = (long) Math.ceil(sizeof(value) + sizeof(key));
         if (((int) size) != size) {
            return 0;
         } else {
            return (int) size;
         }
      }
   };

   static double sizeof(ISizeable val) {
      return val == null ? 0.0 : val.byteSize();
   }

   static double sum(double... vals) {
      return DoubleStream.of(vals).sum();
   }

   static double sum(ISizeable a, ISizeable... vals) {
      return a.byteSize()
            + Stream.of(vals).filter(Objects::nonNull).mapToDouble(ISizeable::byteSize).sum();
   }

   static double sum(Stream<? extends ISizeable> vals) {
      return vals.filter(Objects::nonNull).mapToDouble(ISizeable::byteSize).sum();
   }

   double byteSize();
}
