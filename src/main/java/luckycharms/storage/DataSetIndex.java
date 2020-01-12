package luckycharms.storage;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Converter;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import luckycharms.protos.PropertiesProto;
import luckycharms.protos.PropertyValueProto;
import luckycharms.util.Configure;

public class DataSetIndex {

   public static final Converter<Long, byte[]> LONG_CONVERT = new Converter<Long, byte[]>() {

      @Override
      protected byte[] doForward(Long a) {
         byte[] bytes = new byte[Long.BYTES];
         ByteBuffer wrap = ByteBuffer.wrap(bytes);
         wrap.order(ByteOrder.BIG_ENDIAN);
         wrap.putLong(a);
         return bytes;
      }

      @Override
      protected Long doBackward(byte[] b) {
         ByteBuffer wrap = ByteBuffer.wrap(b);
         wrap.order(ByteOrder.BIG_ENDIAN);
         return wrap.getLong();
      }

   };
   public static final Converter<Double, byte[]> DOUBLE_CONVERT = new Converter<Double, byte[]>() {

      @Override
      protected byte[] doForward(Double a) {
         byte[] bytes = new byte[Double.BYTES];
         ByteBuffer wrap = ByteBuffer.wrap(bytes);
         wrap.order(ByteOrder.BIG_ENDIAN);
         wrap.putDouble(a);
         return bytes;
      }

      @Override
      protected Double doBackward(byte[] b) {
         ByteBuffer wrap = ByteBuffer.wrap(b);
         wrap.order(ByteOrder.BIG_ENDIAN);
         return wrap.getDouble();
      }

   };
   public static final Converter<Integer, byte[]> INT_CONVERT = new Converter<Integer, byte[]>() {

      @Override
      protected byte[] doForward(Integer a) {
         byte[] bytes = new byte[Integer.BYTES];
         ByteBuffer wrap = ByteBuffer.wrap(bytes);
         wrap.order(ByteOrder.BIG_ENDIAN);
         wrap.putInt(a);
         return bytes;
      }

      @Override
      protected Integer doBackward(byte[] b) {
         ByteBuffer wrap = ByteBuffer.wrap(b);
         wrap.order(ByteOrder.BIG_ENDIAN);
         return wrap.getInt();
      }

   };
   public static final Converter<Boolean, byte[]> BOOLEAN_CONVERT = new Converter<Boolean, byte[]>() {

      @Override
      protected byte[] doForward(Boolean a) {
         return new byte[] { (byte) (a ? 1 : 0) };
      }

      @Override
      protected Boolean doBackward(byte[] b) {
         return b[0] != 0;
      }

   };

   private final ConcurrentHashMap<String, byte[]> map = new ConcurrentHashMap<>();

   public DataSetIndex() {

   }

   public DataSetIndex(PropertiesProto proto) {
      for (PropertyValueProto p : proto.getPropertyList()) {
         map.put(p.getName(), p.getPayload().toByteArray());
      }
   }

   public DataSetIndex(Configure<Map<String, byte[]>> c) {
      map.putAll(c.define(new HashMap<>()));
   }

   public DataSetIndex(byte[] data) {
      this(parse(data));
   }

   private static PropertiesProto parse(byte[] data) {
      try {
         return PropertiesProto.parseFrom(data);
      } catch (InvalidProtocolBufferException e) {
         throw new UncheckedIOException(e);
      }
   }

   public byte[] getBytes(String name) {
      return map.get(name);
   }

   public <T> T get(String name, Converter<T, byte[]> converter, T aDefault) {
      byte[] value = getBytes(name);
      if (value == null) {
         return aDefault;
      }
      return converter.reverse().convert(value);
   }

   public Long getLong(String name, Long aDefault) {
      return get(name, LONG_CONVERT, aDefault);
   }

   public Integer getInt(String name, Integer aDefault) {
      return get(name, INT_CONVERT, aDefault);
   }

   public Double getDouble(String name, Double aDefault) {
      return get(name, DOUBLE_CONVERT, aDefault);
   }

   public Boolean getBoolean(String name, Boolean aDefault) {
      return get(name, BOOLEAN_CONVERT, aDefault);
   }

   public byte[] toBytes() {
      PropertiesProto.Builder builder = PropertiesProto.newBuilder();
      map.entrySet().stream()//
            .sorted(Comparator.comparing(Entry::getKey))//
            .map(v -> PropertyValueProto.newBuilder()//
                  .setName(v.getKey()).setPayload(ByteString.copyFrom(v.getValue())))
            .forEach(builder::addProperty);
      return builder.build().toByteArray();
   }

   public <T> DataSetIndex put(String name, T value, Converter<T, byte[]> converter) {
      byte[] valueBytes = converter.convert(value);
      map.put(name, valueBytes);
      return this;
   }

   public DataSetIndex putLong(String name, long value) {
      put(name, value, LONG_CONVERT);
      return this;
   }

   public DataSetIndex putInt(String name, int value) {
      put(name, value, INT_CONVERT);
      return this;
   }

   public DataSetIndex putDouble(String name, double value) {
      put(name, value, DOUBLE_CONVERT);
      return this;
   }

   public DataSetIndex putBoolean(String name, boolean value) {
      put(name, value, BOOLEAN_CONVERT);
      return this;
   }

   public DataSetIndex remove(String name) {
      map.remove(name);
      return this;
   }

   public DataSetIndex clear() {
      map.clear();
      return this;
   }

   public boolean isEmpty() { return map.isEmpty(); }
}
