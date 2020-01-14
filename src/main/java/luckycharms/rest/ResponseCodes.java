package luckycharms.rest;

public class ResponseCodes {
   public static boolean isSuccess(int code) {
      return code >= 200 && code < 300;
   }
}
