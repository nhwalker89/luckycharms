package luckycharms.storage;

import java.io.IOException;
import java.util.stream.Stream;

import luckycharms.util.events.CanChange;

public interface IDataSet<K, V> extends CanChange {

   DataSetIndex index();

   void saveIndex();

   V get(K key);

   void put(K key, V value) throws IOException;

   void remove(K key) throws IOException;

   Stream<K> keys();

   void clear() throws IOException;

   void putAll(Stream<KeyValuePair<K, V>> entries) throws IOException;

   void removeAll(Stream<K> keys) throws IOException;

   default Stream<KeyValuePair<K, V>> getAll(Stream<K> keys) {
      return keys.map(k -> new KeyValuePair<>(k, get(k)));
   }
}
