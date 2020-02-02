package luckycharms.storage;

import java.io.IOException;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Converter;
import com.google.common.cache.Cache;
import com.google.common.collect.Range;

import luckycharms.util.RangeUtils;
import luckycharms.util.sizeable.ISizeable;

public class BasicSortedDataSet<K extends Comparable<? super K> & ISizeable, V extends ISizeable>
      extends BasicDataSet<K, V> implements ISortedDataSet<K, V> {

   private NavigableSet<K> keySet = new ConcurrentSkipListSet<>();

   public BasicSortedDataSet(Converter<K, String> keyFn, Converter<V, byte[]> valueFn,
         IStorage storage) {
      this(keyFn, valueFn, storage, GlobalCache.cache());
   }

   public BasicSortedDataSet(Converter<K, String> keyFn, Converter<V, byte[]> valueFn,
         IStorage storage, Cache<DataSetCacheKey, Optional<ISizeable>> cache) {
      super(keyFn, valueFn, storage, cache);
      keySet.addAll(super.keys().collect(Collectors.toList()));
   }

   @Override
   public Stream<K> keys() {
      return keySet.stream();
   }

   @Override
   public Stream<K> keys(Range<K> keys) {
      return RangeUtils.subset(keySet, keys).stream();
   }

   @Override
   public void put(K key, V value) throws IOException {
      super.put(key, value);
      keySet.add(key);
   }

   @Override
   public void remove(K key) throws IOException {
      keySet.remove(key);
      super.remove(key);
   }

   @Override
   public void clear() throws IOException {
      keySet.clear();
      super.clear();
   }
}
