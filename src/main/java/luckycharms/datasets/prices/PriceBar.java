package luckycharms.datasets.prices;

import com.google.common.base.Converter;

import luckycharms.protos.datasets.PriceBarProto;
import luckycharms.util.ProtobufSerializer;
import luckycharms.util.sizeable.ISizeable;
import luckycharms.util.sizeable.Sizes;

public class PriceBar implements ISizeable {
   public static final Converter<PriceBar, PriceBarProto> PROTO_FORMAT = Converter
         .from(PriceBar::toProto, PriceBar::new);
   public static final Converter<PriceBar, byte[]> BYTES_FORMAT = PROTO_FORMAT
         .andThen(new ProtobufSerializer<>(PriceBarProto::parseFrom));
   private final Double open;
   private final Double close;
   private final Double high;
   private final Double low;
   private final Double volume;
   private static final double SIZE = Sizes.DOUBLE * 5;

   public PriceBar(PriceBarProto proto) {
      open = proto.getOpen();
      close = proto.getClose();
      high = proto.getHigh();
      low = proto.getLow();
      volume = proto.getVolume();
   }

   public Double getClose() { return close; }

   public boolean hasClose() {
      return close != null;
   }

   public Double getHigh() { return high; }

   public boolean hasHigh() {
      return high != null;
   }

   public Double getLow() { return low; }

   public boolean hasLow() {
      return low != null;
   }

   public Double getOpen() { return open; }

   public boolean hasOpen() {
      return open != null;
   }

   public Double getVolume() { return volume; }

   public boolean hasVolume() {
      return volume != null;
   }

   public boolean isFullyDefined() {
      return hasClose() && hasHigh() && hasLow() && hasOpen() && hasVolume();
   }

   @Override
   public double byteSize() {
      return SIZE;
   }

   public PriceBarProto toProto() {
      PriceBarProto.Builder b = PriceBarProto.newBuilder();
      if (this.hasClose()) {
         b.setClose(this.getClose());
      }
      if (this.hasOpen()) {
         b.setOpen(this.getOpen());
      }
      if (this.hasHigh()) {
         b.setHigh(this.getHigh());
      }
      if (this.hasLow()) {
         b.setLow(this.getLow());
      }
      if (this.hasVolume()) {
         b.setVolume(this.getVolume());
      }
      return b.build();
   }
}
