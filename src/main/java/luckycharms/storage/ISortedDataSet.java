package luckycharms.storage;

import java.util.stream.Stream;

import com.google.common.collect.Range;

public interface ISortedDataSet<K extends Comparable<? super K>, V> extends IDataSet<K, V> {

   Stream<K> keys(Range<K> keys);

   default Stream<? extends KeyValuePair<? extends K, ? extends V>> getAll(Range<K> range) {
      return getAll(keys(range));
   }

   K lastKey();

   default V lastValue() {
      K key = lastKey();
      return key == null ? null : get(key);
   }

   default KeyValuePair<K, V> latestEntry() {
      K key = lastKey();
      if (key == null) {
         return null;
      }
      V value = get(key);
      return value == null ? null : new KeyValuePair<>(key, value);
   }
}
