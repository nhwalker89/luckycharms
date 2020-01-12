package luckycharms.storage;

import java.util.Map;
import java.util.stream.Stream;

public interface IPagedDataSet<K, PK, V> extends IDataSet<K, V> {

   Stream<PK> pagedKeys();

   IPagedData<K, V> getPage(PK pageKey);

   IDataSet<PK, ? extends IPagedData<K, V>> pagedDataSet();

   public interface IPagedData<K, V> {
      Map<K, V> asMap();

   }
}
