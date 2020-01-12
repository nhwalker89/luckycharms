package luckycharms.storage;

import java.io.IOException;
import java.util.stream.Stream;

public interface IStorage {
   byte[] load(String stringKey) throws IOException;

   void save(String stringKey, byte[] value) throws IOException;

   void remove(String stringKey) throws IOException;

   Stream<String> keyStream();

   void saveIndex(DataSetIndex index);

   DataSetIndex getIndex();
}