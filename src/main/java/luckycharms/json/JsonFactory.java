package luckycharms.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class JsonFactory {
   public static final Gson WIRE = new GsonBuilder().create();
   public static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().create();

   public static String makePretty(String str) {
      try {
         JsonElement e = PRETTY.fromJson(str, JsonElement.class);
         String s = PRETTY.toJson(e);
         return s;
      } catch (Exception e) {
         return str;
      }
   }
}
