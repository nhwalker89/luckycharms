package luckycharms.logging;

import com.google.gson.JsonElement;

import luckycharms.json.JsonFactory;

public class PrettyJson {

   public static PrettyJson lazy(String unformatted) {
      return new PrettyJson(unformatted);
   }

   public static String format(String unformatted) {
      try {
         JsonElement parsed = JsonFactory.PRETTY.fromJson(unformatted, JsonElement.class);
         return JsonFactory.PRETTY.toJson(parsed);
      } catch (Throwable t) {
         return unformatted;
      }
   }

   private final String unformatted;

   private PrettyJson(String unformatted) {
      this.unformatted = unformatted;
   }

   @Override
   public String toString() {
      return format(unformatted);
   }
}
