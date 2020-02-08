package luckycharms.time.units;

import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.OptionalLong;
import java.util.function.LongFunction;

import com.google.common.base.Converter;
import com.google.protobuf.InvalidProtocolBufferException;

import luckycharms.protos.IndexedKeyProto;
import luckycharms.time.IntervalDefinition;
import luckycharms.util.sizeable.ISizeable;
import luckycharms.util.sizeable.Sizes;

public abstract class ATimeInterval<X extends ATimeInterval<X>>
      implements Comparable<X>, ISizeable {

   public static class TimeIntervalToBytes<X extends ATimeInterval<X>>
         extends Converter<X, byte[]> {
      private final LongFunction<X> constructor;

      public TimeIntervalToBytes(LongFunction<X> constructor) {
         this.constructor = constructor;
      }

      @Override
      protected byte[] doForward(X a) {
         return a.toProto().toByteArray();
      }

      @Override
      protected X doBackward(byte[] b) {
         try {
            IndexedKeyProto proto = IndexedKeyProto.parseFrom(b);
            return constructor.apply(proto.getIndex());
         } catch (InvalidProtocolBufferException e) {
            throw new UncheckedIOException(e);
         }
      }

   }

   private final long index;

   private transient ZonedDateTime time;

   public ATimeInterval(long index) {
      this.index = index;
   }

   public long index() {
      return index;
   }

   public abstract IntervalDefinition interval();

   @Override
   public int compareTo(X o) {
      return Long.compare(index, o.index());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (obj.getClass() == this.getClass()) {
         return index == ((ATimeInterval<?>) obj).index;
      }
      return false;
   }

   @Override
   public int hashCode() {
      return Long.hashCode(index);
   }

   @Override
   public String toString() {
      return time().toString();
   }

   public ZonedDateTime time() {
      return time == null ? time = interval().toTime(index) : time;
   }

   public LocalDateTime marketDateTime() {
      return time().toLocalDateTime();
   }

   public LocalDate marketDate() {
      return time().toLocalDate();
   }

   public X next() {
      return createNew(index + 1);
   }

   public X previous() {
      return createNew(index - 1);
   }

   public X plus(long value) {
      return createNew(index + value);
   }

   public X minus(long value) {
      return createNew(index - value);
   }

   protected abstract X createNew(long index);

   public YearsKey asYearKey() {
      return YearsKey.of(time());
   }

   public MonthsKey asMonthKey() {
      return MonthsKey.of(time());
   }

   public IndexedKeyProto toProto() {
      return IndexedKeyProto.newBuilder().setIndex(index()).build();
   }

   @Override
   public double byteSize() {
      return Sizes.TIME_INTERVAL;
   }

   protected static OptionalLong tryParseLong(String s) {
      return tryParse(s, 10);
   }

   protected static OptionalLong tryParse(String s, int radix) {

      if (s == null) {
         return OptionalLong.empty();
      }

      if (radix < Character.MIN_RADIX) {
         return OptionalLong.empty();
      }
      if (radix > Character.MAX_RADIX) {
         return OptionalLong.empty();
      }

      boolean negative = false;
      int i = 0, len = s.length();
      long limit = -Long.MAX_VALUE;

      if (len > 0) {
         char firstChar = s.charAt(0);
         if (firstChar < '0') { // Possible leading "+" or "-"
            if (firstChar == '-') {
               negative = true;
               limit = Long.MIN_VALUE;
            } else if (firstChar != '+') {
               return OptionalLong.empty();
            }

            if (len == 1) { // Cannot have lone "+" or "-"
               return OptionalLong.empty();
            }
            i++;
         }
         long multmin = limit / radix;
         long result = 0;
         while (i < len) {
            // Accumulating negatively avoids surprises near MAX_VALUE
            int digit = Character.digit(s.charAt(i++), radix);
            if (digit < 0 || result < multmin) {
               return OptionalLong.empty();
            }
            result *= radix;
            if (result < limit + digit) {
               return OptionalLong.empty();
            }
            result -= digit;
         }

         return OptionalLong.of(negative ? result : -result);
      } else {
         return OptionalLong.empty();
      }

   }
}
