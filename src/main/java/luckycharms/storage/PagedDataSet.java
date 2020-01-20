package luckycharms.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Converter;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import luckycharms.protos.PagedDataEntryProto;
import luckycharms.protos.PagedDataProto;
import luckycharms.util.sizeable.ISizeable;
import luckycharms.util.sizeable.LazySizeable;

public class PagedDataSet<K extends ISizeable, V extends ISizeable, PK extends ISizeable>
      extends ADataSet<K, V> implements IPagedDataSet<K, PK, V> {
   private static final String DIVISOR = System.lineSeparator() + Strings.repeat("-", 50)
         + System.lineSeparator();

   private final Converter<PK, String> pageKeyFn;
   private final Converter<K, byte[]> keyFn;
   private final Converter<V, byte[]> valueFn;

   private final Function<K, PK> keyToPageKeyFn;
   private final IDataSet<PK, PagedData> pagedDataSet;
   private final IStorage storage;
   private final DataSetIndex index;

   public PagedDataSet(//
         Converter<PK, String> pageKeyFn, //
         Converter<K, byte[]> keyFn, //
         Converter<V, byte[]> valueFn, //
         Function<K, PK> keyToPageKeyFn, //
         IStorage storage) {
      this(pageKeyFn, keyFn, valueFn, keyToPageKeyFn, storage, GlobalCache.cache());
   }

   public PagedDataSet(//
         Converter<PK, String> pageKeyFn, //
         Converter<K, byte[]> keyFn, //
         Converter<V, byte[]> valueFn, //
         Function<K, PK> keyToPageKeyFn, //
         IStorage storage, //
         Cache<DataSetCacheKey, Optional<ISizeable>> cache) {
      this.keyFn = Objects.requireNonNull(keyFn);
      this.valueFn = Objects.requireNonNull(valueFn);
      this.pageKeyFn = Objects.requireNonNull(pageKeyFn);
      this.keyToPageKeyFn = Objects.requireNonNull(keyToPageKeyFn);
      pagedDataSet = new BasicDataSet<PK, PagedData>(//
            this.pageKeyFn, //
            Converter.from(PagedData::toBytes, PagedData::new), //
            storage, cache);
      this.storage = storage;
      this.index = storage.getIndex();
   }

   @Override
   public V get(K key) {
      PK pk = keyToPageKeyFn.apply(key);
      if (pk == null) {
         return null;
      }
      PagedData page = pagedDataSet.get(pk);
      if (page == null) {
         return null;
      }
      return page.asMap().get(key);
   }

   @Override
   public void put(K key, V value) throws IOException {
      PK pk = keyToPageKeyFn.apply(key);
      PagedData page = pagedDataSet.get(Objects.requireNonNull(pk));
      if (page == null) {
         page = new PagedData();
      }
      page = page.modify(m -> m.put(key, value));
      pagedDataSet.put(pk, page);

      onChange();
   }

   @Override
   public void putAll(Stream<KeyValuePair<K, V>> entries) throws IOException {
      Map<PK, List<KeyValuePair<K, V>>> batched = new HashMap<>();
      entries.forEach(pair -> {
         batched.computeIfAbsent(//
               Objects.requireNonNull(keyToPageKeyFn.apply(pair.getKey())),
               key -> new ArrayList<>()).add(pair);
      });
      for (Map.Entry<PK, List<KeyValuePair<K, V>>> e : batched.entrySet()) {
         PagedData page = pagedDataSet.get(e.getKey());
         if (page == null) {
            page = new PagedData();
         }
         page = page.modify(m -> e.getValue().forEach(v -> m.put(v.getKey(), v.getValue())));
         pagedDataSet.put(e.getKey(), page);
      }

      onChange();
   }

   @Override
   public void remove(K key) throws IOException {
      PK pk = keyToPageKeyFn.apply(key);
      PagedData page = pagedDataSet.get(Objects.requireNonNull(pk));
      if (page == null) {
         return;
      }
      page = page.modify(m -> m.remove(key));
      if (page.asMap().isEmpty()) {
         pagedDataSet.remove(pk);
      } else {
         pagedDataSet.put(pk, page);
      }

      onChange();
   }

   @Override
   public Stream<K> keys() {
      return pagedDataSet.getAll(pagedDataSet.keys())
            .flatMap(entry -> entry.getValue().asMap().keySet().stream());
   }

   @Override
   public DataSetIndex index() {
      return index;
   }

   @Override
   public void saveIndex() {
      storage.saveIndex(index);
      onChange();
   }

   @Override
   public IPagedData<K, V> getPage(PK pageKey) {
      return pagedDataSet.get(pageKey);
   }

   @Override
   public Stream<PK> pagedKeys() {
      return pagedDataSet.keys();
   }

   @Override
   public IDataSet<PK, ? extends IPagedData<K, V>> pagedDataSet() {
      return pagedDataSet;
   }

   protected String formatPagedData(PagedData data) {
      return formatPagedDataMultiLine(data);
   }

   protected String formatPagedDataSingleLine(PagedData data) {
      return data.asMap().entrySet().stream()
            .map(entry -> entry.getKey().toString() + " = " + entry.getValue().toString())
            .collect(Collectors.joining(DIVISOR));
   }

   protected String formatPagedDataMultiLine(PagedData data) {
      return data.asMap().entrySet().stream()
            .map(entry -> entry.getKey().toString() + "\n" + entry.getValue().toString())
            .collect(Collectors.joining(DIVISOR));
   }

   public class PagedData implements ISizeable, IPagedDataSet.IPagedData<K, V> {
      private final ImmutableMap<K, V> map;
      private final LazySizeable size = new LazySizeable(this::computeSize);

      public PagedData(byte[] data) {
         ImmutableMap.Builder<K, V> bldr = ImmutableMap.builder();
         try {
            PagedDataProto proto = PagedDataProto.parseFrom(data);
            for (PagedDataEntryProto e : proto.getValuesList()) {
               K key = keyFn.reverse().convert(e.getKey().toByteArray());
               V value = valueFn.reverse().convert(e.getValue().toByteArray());
               bldr.put(key, value);
            }
         } catch (InvalidProtocolBufferException e) {
            throw new UncheckedIOException(e);
         }

         this.map = bldr.build();
      }

      private double computeSize() {
         return ISizeable.sum(Stream.concat(map.keySet().stream(), map.values().stream()));
      }

      public PagedData() {
         this.map = ImmutableMap.of();
      }

      public PagedData(Map<K, V> map) {
         this.map = ImmutableMap.copyOf(map);
      }

      public Map<K, V> asMap() {
         return map;
      }

      public PagedData modify(Consumer<Map<K, V>> apply) {
         HashMap<K, V> map = new HashMap<>(this.map);
         apply.accept(map);
         return new PagedData(map);
      }

      public byte[] toBytes() {
         PagedDataEntryProto[] entries = map.entrySet().stream().map(e -> {
            K key = e.getKey();
            V value = e.getValue();
            byte[] keyBytes = Objects.requireNonNull(keyFn.convert(key));
            byte[] valueBytes = Objects.requireNonNull(valueFn.convert(value));
            return PagedDataEntryProto.newBuilder().setKey(ByteString.copyFrom(keyBytes))
                  .setValue(ByteString.copyFrom(valueBytes)).build();
         }).toArray(PagedDataEntryProto[]::new);
         PagedDataProto proto = PagedDataProto.newBuilder().addAllValues(Arrays.asList(entries))
               .build();
         return proto.toByteArray();
      }

      @Override
      public double byteSize() {
         return size.byteSize();
      }

      @Override
      public String toString() {
         return formatPagedData(this);
      }
   }

}
