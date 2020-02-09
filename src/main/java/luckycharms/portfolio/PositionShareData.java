package luckycharms.portfolio;

import java.util.Objects;

import luckycharms.protos.portfolio.PositionShareDataProto;
import luckycharms.time.units.DaysKey;
import luckycharms.util.sizeable.ISizeable;

public class PositionShareData implements Comparable<PositionShareData>, ISizeable {

   private final DaysKey purchaseDate;

   public PositionShareData(DaysKey purchaseDate) {
      this.purchaseDate = Objects.requireNonNull(purchaseDate);
   }

   public PositionShareData(PositionShareDataProto proto) {
      this.purchaseDate = DaysKey.of(proto.getPurchaseDate().getIndex());
   }

   @Override
   public double byteSize() {
      return purchaseDate.byteSize();
   }

   @Override
   public int compareTo(PositionShareData o) {
      return purchaseDate.compareTo(o.purchaseDate);
   }

   public DaysKey getPurchaseDate() { return purchaseDate; }

   @Override
   public int hashCode() {
      return Objects.hash(purchaseDate);
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
      PositionShareData other = (PositionShareData) obj;
      return Objects.equals(purchaseDate, other.purchaseDate);
   }

   public PositionShareDataProto toProto() {
      return PositionShareDataProto.newBuilder().setPurchaseDate(purchaseDate.toProto()).build();
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("PositionShareData [");
      if (purchaseDate != null) {
         builder.append("purchaseDate=");
         builder.append(purchaseDate);
      }
      builder.append("]");
      return builder.toString();
   }

}
