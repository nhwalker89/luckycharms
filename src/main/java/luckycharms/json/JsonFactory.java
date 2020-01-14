package luckycharms.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonFactory {
   public static final Gson WIRE = new GsonBuilder().create();
   public static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().create();

}
