package luckycharms.util;

public class DecimalFormat {

   public static void main(String[] args) {
      System.out.println(format(1.00000001, 2));
   }

   public static final String format(double value, int scale) {
      StringBuilder b = new StringBuilder();
      format(b, value, scale);
      return b.toString();
   }

   public static final void format(StringBuilder b, double value, int scale) {
      long shift = getShift(scale);
      long newValue = Math.round(value * shift);
      if (newValue < shift) {
         newValue = shift + newValue;
         int start = b.length();
         b.append(newValue);
         b.setCharAt(start, '0');
      } else {
         b.append(newValue);
      }
      b.insert(b.length() - scale, '.');

   }

   private static final long getShift(int scale) {
      switch (scale) {
      case 0:
         return 1L;
      case 1:
         return 1_0L;
      case 2:
         return 1_00L;
      case 3:
         return 1_000L;
      case 4:
         return 1_0000L;
      case 5:
         return 1_00000L;
      case 6:
         return 1_000000L;
      case 7:
         return 1_0000000L;
      case 8:
         return 1_00000000L;
      case 9:
         return 1_000000000L;
      case 10:
         return 1_0000000000L;
      default:
         throw new IllegalArgumentException("Cannot Format");
      }
   }
}
