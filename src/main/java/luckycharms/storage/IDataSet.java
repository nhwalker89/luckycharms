package luckycharms.storage;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

public interface IDataSet<K, V> {

   DataSetIndex index();

   void saveIndex();

   V get(K key);

   void put(K key, V value) throws IOException;

   void remove(K key) throws IOException;

   Stream<K> keys();

   default void clear() throws IOException {
      removeAll(keys());
      index().clear();
      saveIndex();
   }

   default Stream<KeyValuePair<K, V>> getAll(Stream<K> keys) {
      return keys.map(k -> new KeyValuePair<>(k, get(k)));
   }

   default void putAll(Stream<KeyValuePair<K, V>> entries) throws IOException {
      IOException ex = null;
      Iterator<KeyValuePair<K, V>> iter = entries.iterator();
      while (iter.hasNext()) {
         KeyValuePair<? extends K, ? extends V> entry = iter.next();
         try {
            put(entry.getKey(), entry.getValue());
         } catch (IOException e) {
            if (ex == null) {
               ex = new IOException("Bulk put Exception(s)");
            }
            ex.addSuppressed(e);
         }
      }
   }

   default void removeAll(Stream<K> keys) throws IOException {
      IOException ex = null;
      Iterator<? extends K> iter = keys.iterator();
      while (iter.hasNext()) {
         K key = iter.next();
         try {
            remove(key);
         } catch (IOException e) {
            if (ex == null) {
               ex = new IOException("Bulk remove Exception(s)");
            }
            ex.addSuppressed(e);
         }
      }
   }

}
