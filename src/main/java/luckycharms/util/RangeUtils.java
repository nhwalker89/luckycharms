package luckycharms.util;

import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.function.Function;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

public class RangeUtils {

   public static <K extends Comparable<? super K>> NavigableSet<K> subset(NavigableSet<K> set, Range<K> range) {
      if (range.hasLowerBound()) {
         if (range.hasUpperBound()) {
            return set.subSet(range.lowerEndpoint(), isInclusive(range.lowerBoundType()), range.upperEndpoint(),
                  isInclusive(range.upperBoundType()));
         } else {
            return set.tailSet(range.lowerEndpoint(), isInclusive(range.lowerBoundType()));
         }
      } else {
         if (range.hasUpperBound()) {
            return set.headSet(range.upperEndpoint(), isInclusive(range.upperBoundType()));
         } else {
            return set;
         }
      }
   }

   public static <K extends Comparable<? super K>, V> NavigableMap<K, V> submap(NavigableMap<K, V> map,
         Range<K> range) {
      if (range.hasLowerBound()) {
         if (range.hasUpperBound()) {
            return map.subMap(range.lowerEndpoint(), isInclusive(range.lowerBoundType()), range.upperEndpoint(),
                  isInclusive(range.upperBoundType()));
         } else {
            return map.tailMap(range.lowerEndpoint(), isInclusive(range.lowerBoundType()));
         }
      } else {
         if (range.hasUpperBound()) {
            return map.headMap(range.upperEndpoint(), isInclusive(range.upperBoundType()));
         } else {
            return map;
         }
      }
   }

   public static <T extends Comparable<? super T>> Range<T> toClosed(Range<T> range) {

      if (range.hasLowerBound()) {
         T lower = range.lowerEndpoint();
         if (range.hasUpperBound()) {
            T upper = range.upperEndpoint();
            return Range.range(lower, BoundType.CLOSED, upper, range.upperBoundType());
         } else {
            return Range.downTo(lower, BoundType.CLOSED);
         }
      } else {
         if (range.hasUpperBound()) {
            T upper = range.upperEndpoint();
            return Range.upTo(upper, BoundType.CLOSED);
         } else {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Range<T> newRange = (Range) range;
            return newRange;
         }
      }

   }

   public static <T extends Comparable<? super T>, N extends Comparable<? super N>> Range<N> convert(Range<T> range,
         Function<? super T, ? extends N> convert) {
      if (range.hasLowerBound()) {
         N lower = convert.apply(range.lowerEndpoint());
         if (range.hasUpperBound()) {
            N upper = convert.apply(range.upperEndpoint());
            return Range.range(lower, range.lowerBoundType(), upper, range.upperBoundType());
         } else {
            return Range.downTo(lower, range.lowerBoundType());
         }
      } else {
         if (range.hasUpperBound()) {
            N upper = convert.apply(range.upperEndpoint());
            return Range.upTo(upper, range.upperBoundType());
         } else {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Range<N> newRange = (Range) range;
            return newRange;
         }
      }
   }

   private static boolean isInclusive(BoundType type) {
      switch (type) {
      case CLOSED:
         return true;
      case OPEN:
      default:
         return false;

      }
   }
}
