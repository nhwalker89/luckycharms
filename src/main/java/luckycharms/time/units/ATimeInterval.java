package luckycharms.time.units;

import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
}
