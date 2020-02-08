package luckycharms.datasets.prices;

import java.math.BigDecimal;
import java.util.Objects;

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
      open = proto.hasOpen() ? proto.getOpen().getValue() : null;
      close = proto.hasClose() ? proto.getClose().getValue() : null;
      high = proto.hasHigh() ? proto.getHigh().getValue() : null;
      low = proto.hasLow() ? proto.getLow().getValue() : null;
      volume = proto.hasVolume() ? proto.getVolume().getValue() : null;
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
         b.getCloseBuilder().setValue(this.getClose());
      }
      if (this.hasOpen()) {
         b.getOpenBuilder().setValue(this.getOpen());
      }
      if (this.hasHigh()) {
         b.getHighBuilder().setValue(this.getHigh());
      }
      if (this.hasLow()) {
         b.getLowBuilder().setValue(this.getLow());
      }
      if (this.hasVolume()) {
         b.getVolumeBuilder().setValue(this.getVolume());
      }
      return b.build();
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("PriceBar [open=");
      builder.append(open);
      builder.append(", close=");
      builder.append(close);
      builder.append(", high=");
      builder.append(high);
      builder.append(", low=");
      builder.append(low);
      builder.append(", volume=");
      builder.append(BigDecimal.valueOf(volume).toPlainString());
      builder.append("]");
      return builder.toString();
   }

   @Override
   public int hashCode() {
      return Objects.hash(close, high, low, open, volume);
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
      PriceBar other = (PriceBar) obj;
      return Objects.equals(close, other.close) && Objects.equals(high, other.high)
            && Objects.equals(low, other.low) && Objects.equals(open, other.open)
            && Objects.equals(volume, other.volume);
   }

}
