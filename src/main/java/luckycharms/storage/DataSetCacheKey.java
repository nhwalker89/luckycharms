package luckycharms.storage;

import java.util.Objects;

import luckycharms.util.DualBase;
import luckycharms.util.sizeable.ISizeable;

@SuppressWarnings("rawtypes")
public class DataSetCacheKey extends DualBase<IDataSet, ISizeable> implements ISizeable {

   protected DataSetCacheKey(IDataSet a, ISizeable b) {
      super(a, Objects.requireNonNull(b));
   }

   public IDataSet dataSet() {
      return a;
   }

   public ISizeable key() {
      return b;
   }

   @Override
   public double byteSize() {
      return b.byteSize();
   }

}
