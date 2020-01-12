package luckycharms.datasets.calendar;

import java.io.UncheckedIOException;
import java.time.LocalTime;
import java.util.Objects;

import com.google.common.base.Converter;
import com.google.common.collect.Range;
import com.google.protobuf.InvalidProtocolBufferException;

import luckycharms.protos.datasets.MarketDayDataProto;
import luckycharms.time.TimeProtos;
import luckycharms.time.units.DaysKey;
import luckycharms.util.sizeable.ISizeable;
import luckycharms.util.sizeable.Sizes;

public class MarketDayData implements ISizeable {

   public static final Converter<MarketDayData, byte[]> FORMAT = new Converter<MarketDayData, byte[]>() {

      @Override
      protected byte[] doForward(MarketDayData a) {
         return a.toProto().toByteArray();
      }

      @Override
      protected MarketDayData doBackward(byte[] b) {
         try {
            MarketDayDataProto parse = MarketDayDataProto.parseFrom(b);
            return new MarketDayData(parse);
         } catch (InvalidProtocolBufferException e) {
            throw new UncheckedIOException(e);
         }
      }
   };

   private static final double SIZE = Sizes.TIME_INTERVAL //
         + Sizes.LOCAL_TIME + Sizes.LOCAL_TIME;

   private final DaysKey day;
   private final LocalTime open;
   private final LocalTime close;

   public MarketDayData(DaysKey day, LocalTime open, LocalTime close) {
      this.day = Objects.requireNonNull(day);
      this.open = open;
      this.close = close;
   }

   @Override
   public double byteSize() {
      return SIZE;
   }

   public MarketDayData(MarketDayDataProto proto) {
      day = DaysKey.of(proto.getDate().getIndex());
      if (proto.hasOpen()) {
         open = TimeProtos.LOCAL_TIME_CONVERTER.reverse().convert(proto.getOpen());
      } else {
         open = null;
      }
      if (proto.hasClose()) {
         close = TimeProtos.LOCAL_TIME_CONVERTER.reverse().convert(proto.getClose());
      } else {
         close = null;
      }
   }

   public DaysKey getDay() { return day; }

   public LocalTime getOpen() { return open; }

   public LocalTime getClose() { return close; }

   public Range<LocalTime> timeRange() {
      return validMarketDay() ? Range.closed(open, close) : null;
   }

   public boolean validMarketDay() {
      return open != null && close != null;
   }

   @Override
   public String toString() {
      return day.toString() + " " + timeRange();
   }

   public MarketDayDataProto toProto() {
      MarketDayDataProto.Builder b = MarketDayDataProto.newBuilder();
      b.getDateBuilder().setIndex(day.index());
      if (close != null) {
         b.setClose(TimeProtos.LOCAL_TIME_CONVERTER.convert(close));
      }
      if (open != null) {
         b.setOpen(TimeProtos.LOCAL_TIME_CONVERTER.convert(open));
      }
      return b.build();
   }
}
