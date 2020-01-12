package lucky.charms.storage.sizeable;

public class Sizes {
   public static final int BYTE = Byte.BYTES;
   public static final int SHORT = Short.BYTES;
   public static final int INT = Integer.BYTES;
   public static final int LONG = Long.BYTES;
   public static final int DOUBLE = Double.BYTES;
   public static final int FLOAT = Float.BYTES;
   public static final int BOOLEAN = 1;

   public static final int LOCAL_TIME = BYTE + BYTE + BYTE + INT;

   public static final int LOCAL_DATE = INT + SHORT + SHORT;

   public static final int LOCAL_DATE_TIME = LOCAL_DATE + LOCAL_TIME;

   public static final int ZONED_DATE_TIME = LOCAL_DATE_TIME + /* estimate zone data */ 128;

   public static final int TIME_INTERVAL = LONG + ZONED_DATE_TIME;

   public static final int sizes(String str) {
      return str.length() * Character.BYTES;
   }

}
