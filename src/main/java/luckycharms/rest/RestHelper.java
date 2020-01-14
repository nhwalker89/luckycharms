package luckycharms.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

class RestHelper {
   static String encode(String input) throws RestException {
      try {
         return URLEncoder.encode(input, "UTF-8");
      } catch (UnsupportedEncodingException e) {
         throw new RestException(e);
      }
   }
}
