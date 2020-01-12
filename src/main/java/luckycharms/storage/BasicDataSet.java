package luckycharms.storage;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import com.google.common.base.Converter;
import com.google.common.cache.Cache;

import luckycharms.util.sizeable.ISizeable;

public class BasicDataSet<K extends ISizeable, V extends ISizeable> extends ADataSet<K, V> {

   private final IStorage storage;
   private final Cache<DataSetCacheKey, Optional<ISizeable>> cache;
   private final Converter<K, String> keyFn;
   private final Converter<V, byte[]> valueFn;
   private final org.slf4j.Logger sLog = org.slf4j.LoggerFactory.getLogger(this.getClass());
   private final DataSetIndex index;

   public BasicDataSet(Converter<K, String> keyFn, Converter<V, byte[]> valueFn, IStorage storage) {
      this(keyFn, valueFn, storage, GlobalCache.cache());
   }

   public BasicDataSet(Converter<K, String> keyFn, Converter<V, byte[]> valueFn, IStorage storage,
         Cache<DataSetCacheKey, Optional<ISizeable>> cache) {
      this.cache = Objects.requireNonNull(cache);
      this.keyFn = Objects.requireNonNull(keyFn);
      this.valueFn = Objects.requireNonNull(valueFn);
      this.storage = Objects.requireNonNull(storage);
      this.index = storage.getIndex();
   }

   private Optional<ISizeable> doLoad(K key) {
      String stringKey = keyFn.convert(key);
      if (stringKey == null) {
         return Optional.empty();
      }
      try {
         byte[] data = storage.load(stringKey);
         if (data == null) {
            return Optional.empty();
         }
         V value = valueFn.reverse().convert(data);
         return Optional.ofNullable(value);
      } catch (Exception e) {
         sLog.error("Problem fetching loading data", e);
      }
      return Optional.empty();
   }

   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Override
   public V get(K key) {
      if (key == null) {
         return null;
      }
      try {
         Optional value = this.cache.get(new DataSetCacheKey(this, key), () -> doLoad(key));
         if (value == null) {
            return null;
         } else {
            return (V) value.orElse(null);
         }
      } catch (ExecutionException e) {
         sLog.error("Problem fetching loading data", e);
      }
      return null;
   }

   @Override
   public void put(K key, V value) throws IOException {
      Objects.requireNonNull(key);
      Objects.requireNonNull(value);

      String stringKey = keyFn.convert(key);
      byte[] valueBytes = valueFn.convert(value);

      try {
         Objects.requireNonNull(stringKey);
         Objects.requireNonNull(valueBytes);
      } catch (NullPointerException e) {
         throw new IOException("Problem encoding data", e);
      }

      storage.save(stringKey, valueBytes);
      cache.put(new DataSetCacheKey(this, key), Optional.of(value));

   }

   @Override
   public void remove(K key) throws IOException {
      Objects.requireNonNull(key);
      String stringKey = keyFn.convert(key);
      if (stringKey != null) {
         try {
            storage.remove(stringKey);
         } catch (IOException e) {
            throw e;
         }
         cache.put(new DataSetCacheKey(this, key), Optional.empty());
      }
   }

   @Override
   public Stream<K> keys() {
      return storage.keyStream().map(keyFn.reverse()::convert);
   }

   @Override
   public DataSetIndex index() {
      return index;
   }

   @Override
   public void saveIndex() {
      storage.saveIndex(index);
   }

}
