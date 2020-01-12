package luckycharms.util.sizeable;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.google.common.base.Converter;

public abstract class SizeableValue<T> implements ISizeable {
   protected final T value;

   public SizeableValue(T value) {
      this.value = value;
   }

   public T getValue() {
      return value;
   }

   @Override
   public abstract double byteSize();

   @Override
   public int hashCode() {
      return Objects.hash(value);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      SizeableValue<?> other = (SizeableValue<?>) obj;
      return Objects.equals(value, other.value);
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("SizeableValue [value=");
      builder.append(value);
      builder.append(", bytes=");
      builder.append(byteSize());
      builder.append("]");
      return builder.toString();
   }

   public static abstract class ComparableAndSizeableValue<T extends Comparable<? super T>, SELF extends ComparableAndSizeableValue<T, SELF>>
         extends SizeableValue<T> implements Comparable<SELF> {

      public ComparableAndSizeableValue(T value) {
         super(Objects.requireNonNull(value));
      }

      @Override
      public int compareTo(SELF o) {
         return value.compareTo(o.value);
      }
   }

   public static class SizeableString extends ComparableAndSizeableValue<String, SizeableString> {
      public static final Converter<SizeableString, String> FORMAT = Converter.from(SizeableString::getValue,
            SizeableString::new);

      public static final Converter<SizeableString, byte[]> BYTE_FORMAT = FORMAT.andThen(
            Converter.from(s -> s.getBytes(StandardCharsets.UTF_16), s -> new String(s, StandardCharsets.UTF_16)));

      public SizeableString(String value) {
         super(value);
      }

      @Override
      public double byteSize() {
         return value.length() * Character.BYTES;
      }
   }

   public static class SizeableLong extends ComparableAndSizeableValue<Long, SizeableLong> {

      public SizeableLong(long value) {
         super(value);
      }

      @Override
      public double byteSize() {
         return Long.BYTES;
      }

      public long longValue() {
         return value.longValue();
      }
   }

   public static class SizeableInteger extends ComparableAndSizeableValue<Integer, SizeableInteger> {

      public SizeableInteger(int value) {
         super(value);
      }

      @Override
      public double byteSize() {
         return Integer.BYTES;
      }

      public int intValue() {
         return value.intValue();
      }
   }

   public static class SizeableDouble extends ComparableAndSizeableValue<Double, SizeableDouble> {

      public SizeableDouble(double value) {
         super(value);
      }

      @Override
      public double byteSize() {
         return Double.BYTES;
      }

      public double doubleValue() {
         return value.doubleValue();
      }
   }

   public static class SizeableBoolean extends ComparableAndSizeableValue<Boolean, SizeableBoolean> {

      public SizeableBoolean(boolean value) {
         super(value);
      }

      @Override
      public double byteSize() {
         return 1;
      }

      public boolean booleanValue() {
         return value.booleanValue();
      }
   }
}
