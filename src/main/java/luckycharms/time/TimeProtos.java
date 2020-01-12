package luckycharms.time;

import java.time.LocalTime;

import com.google.common.base.Converter;

import luckycharms.protos.LocalTimeProto;

public class TimeProtos {

   public static final Converter<LocalTime, LocalTimeProto> LOCAL_TIME_CONVERTER = new Converter<LocalTime, LocalTimeProto>() {

      @Override
      protected LocalTimeProto doForward(LocalTime a) {
         return LocalTimeProto.newBuilder().setNanos(a.toNanoOfDay()).build();
      }

      @Override
      protected LocalTime doBackward(LocalTimeProto b) {
         return LocalTime.ofNanoOfDay(b.getNanos());
      }

   };
}
