package luckycharms.storage;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

import luckycharms.util.events.AChanger;

public abstract class ADataSet<K, V> extends AChanger implements IDataSet<K, V> {

   public ADataSet() {}

   @Override
   public final int hashCode() {
      return super.hashCode();
   }

   @Override
   public final boolean equals(Object obj) {
      return super.equals(obj);
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName();
   }

   public void removeAll(Stream<K> keys) throws IOException {
      pauseNotifications();

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

      resumeNotifications();
   }

   public void putAll(Stream<KeyValuePair<K, V>> entries) throws IOException {
      pauseNotifications();

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

      resumeNotifications();
   }

   public void clear() throws IOException {
      pauseNotifications();

      removeAll(keys());
      index().clear();
      saveIndex();

      resumeNotifications();
   }
}
