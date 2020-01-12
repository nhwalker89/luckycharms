package luckycharms.util;

import java.io.IOException;
import java.io.UncheckedIOException;

import com.google.common.base.Converter;
import com.google.protobuf.MessageLite;

public class ProtobufSerializer<A extends MessageLite> extends Converter<A, byte[]> {

   private final ExceptionalFunction<byte[], A> parseFromFn;

   public ProtobufSerializer(ExceptionalFunction<byte[], A> parseFromFn) {
      this.parseFromFn = parseFromFn;
   }

   @Override
   protected byte[] doForward(A a) {
      return a.toByteArray();
   }

   @Override
   protected A doBackward(byte[] b) {
      try {
         return parseFromFn.apply(b);
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      } catch (Exception e) {
         // shouldn't happen
         throw new UncheckedException(e);
      }
   }

}
