package luckycharms.storage;

import com.google.common.base.Converter;

public class FileNameSanatizer extends Converter<String, String> {

   private static final String LEGAL_CHARS = ".+-_[]()"//
         + "abcdefghijklmnopqrstuvwxyz" //
         + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"//
         + "1234567890";

   public static void main(String[] args) {
      String start = "abcde/fghijk_100%.txt/";
      String cleaned = INSTANCE.convert(start);
      String restored = INSTANCE.reverse().convert(cleaned);

      System.out.println(start);
      System.out.println(cleaned);
      System.out.println(restored);
      System.out.println(start.equals(restored) && !start.equals(cleaned) ? "Success" : "Fail");

   }

   public static final FileNameSanatizer INSTANCE = new FileNameSanatizer();
   private static final String[] REPLACEMENT_CHARS;
   static {
      REPLACEMENT_CHARS = new String[128];
      for (int i = 0; i < REPLACEMENT_CHARS.length; i++) {
         REPLACEMENT_CHARS[i] = encodeEscaped(i);
      }
      for (char c : LEGAL_CHARS.toCharArray()) {
         REPLACEMENT_CHARS[c] = null;
      }
   }

   private static String encodeEscaped(int c) {
      return String.format("%%%02x", c);
   }

   @Override
   protected String doForward(String value) {
      StringBuilder out = new StringBuilder(value.length() + 16);
      String[] replacements = REPLACEMENT_CHARS;
      int last = 0;
      int length = value.length();
      for (int i = 0; i < length; i++) {
         char c = value.charAt(i);
         String replacement;
         if (c < 128) {
            replacement = replacements[c];
            if (replacement == null) {
               continue;
            }
         } else {
            continue;
         }
         if (last < i) {
            out.append(value, last, i);
         }
         out.append(replacement);
         last = i + 1;
      }
      if (last == 0) {
         return value;
      } else if (last < length) {
         out.append(value, last, length);
      }
      return out.toString();
   }

   @Override
   protected String doBackward(String value) {
      StringBuilder out = new StringBuilder(value.length() + 16);
      int len = value.length();

      int last = 0;
      int i = 0;
      while (i < len) {
         char c = value.charAt(i);
         if (c == '%') {
            out.append(value, last, i);
            out.append(readEscaped(value.charAt(++i), value.charAt(++i)));
            last = i + 1;
         } else {
            i++;
         }
      }
      if (last == 0) {
         return value;
      } else if (last < len) {
         out.append(value, last, len);
      }
      return out.toString();
   }

   private char readEscaped(char a, char b) {
      char result = charValue(a);
      result <<= 4;
      result += charValue(b);
      return result;
   }

   private char charValue(char c) {
      int result;
      if (c >= '0' && c <= '9') {
         result = (c - '0');
      } else if (c >= 'a' && c <= 'f') {
         result = (c - 'a' + 10);
      } else if (c >= 'A' && c <= 'F') {
         result = (c - 'A' + 10);
      } else {
         throw new NumberFormatException("Not a valid hex value char = " + c);
      }
      return (char) result;
   }
}
