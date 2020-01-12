package luckycharms.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import luckycharms.util.DualBase;

public final class KeyValuePair<K, V> extends DualBase<K, V> {

   public static <K,
         V> Map<K, V> streamToMap(Stream<? extends KeyValuePair<? extends K, ? extends V>> stream) {
      return new HashMap<>(
            stream.collect(Collectors.toMap(KeyValuePair::getKey, KeyValuePair::getValue)));
   }

   public static <K extends Comparable<? super K>, V> NavigableMap<K, V> streamToNavigableMap(
         Stream<? extends KeyValuePair<? extends K, ? extends V>> stream) {
      return stream.collect(Collectors.toMap(//
            KeyValuePair::getKey, KeyValuePair::getValue, (a, b) -> b, TreeMap::new));
   }

   public static <K, V> List<V> streamToValueList(
         Stream<? extends KeyValuePair<? extends K, ? extends V>> stream) {
      return stream.map(KeyValuePair::getValue)//
            .collect(Collectors.toCollection(ArrayList::new));
   }

   public KeyValuePair(K key, V value) {
      super(key, value);
   }

   public K getKey() { return a; }

   public V getValue() { return b; }
}
