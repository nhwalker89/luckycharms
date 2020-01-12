package luckycharms.storage;

import java.util.stream.Stream;

import com.google.common.collect.Range;

public interface ISortedDataSet<K extends Comparable<? super K>, V> extends IDataSet<K, V> {

   Stream<K> keys(Range<K> keys);

   default Stream<? extends KeyValuePair<? extends K, ? extends V>> getAll(Range<K> range) {
      return getAll(keys(range));
   }

}
