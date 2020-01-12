package luckycharms.util.sizeable;

public enum ByteUnit {
   BYTE(1), //
   KB(1_000), //
   MB(1_000_000), //
   GB(1_000_000_000)//
   ;

   private final double factor;

   private ByteUnit(double factor) {
      this.factor = factor;
   }

   public double toBytes(double value) {
      return to(ByteUnit.BYTE, value);
   }

   public double toKb(double value) {
      return to(ByteUnit.KB, value);
   }

   public double toMb(double value) {
      return to(ByteUnit.MB, value);
   }

   public double toGb(double value) {
      return to(ByteUnit.GB, value);
   }

   public double fromBytes(double value) {
      return from(ByteUnit.BYTE, value);
   }

   public double fromKb(double value) {
      return from(ByteUnit.KB, value);
   }

   public double fromMb(double value) {
      return from(ByteUnit.MB, value);
   }

   public double fromGb(double value) {
      return from(ByteUnit.GB, value);
   }

   public double to(ByteUnit other, double value) {
      return value * (factor / other.factor);
   }

   public double from(ByteUnit other, double value) {
      return other.to(this, value);
   }
}
