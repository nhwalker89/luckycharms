package luckycharms.storage;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

public class DirectoryStorageSingleSet implements IStorage {
   private static final org.slf4j.Logger sLog = org.slf4j.LoggerFactory
         .getLogger(DirectoryStorageSingleSet.class);

   private static final String INDEX_NAME = "%index";
   private Path directory;

   public DirectoryStorageSingleSet(Path directory) {
      this.directory = directory;
      try {
         Files.createDirectories(directory);
      } catch (IOException e) {
         sLog.error("Problem creating directory", e);
      }
   }

   @Override
   public byte[] load(String stringKey) throws IOException {
      String cleanKey = FileNameSanatizer.INSTANCE.convert(stringKey);
      try {
         byte[] value = Files.readAllBytes(this.directory.resolve(cleanKey));
         return value;
      } catch (IOException e) {
         sLog.trace("Expected exception if file is missing", e);
      } catch (Exception e) {
         sLog.error("Problem loading", e);
      }
      return null;
   }

   @Override
   public void save(String stringKey, byte[] value) throws IOException {
      String cleanKey = FileNameSanatizer.INSTANCE.convert(stringKey);
      Files.write(this.directory.resolve(cleanKey), value);
   }

   @Override
   public void remove(String stringKey) throws IOException {
      String cleanKey = FileNameSanatizer.INSTANCE.convert(stringKey);
      Files.delete(this.directory.resolve(cleanKey));

   }

   @Override
   public Stream<String> keyStream() {
      try {
         return Files.list(directory).filter(p -> !p.getFileName().toString().equals(INDEX_NAME))
               .map(path -> FileNameSanatizer.INSTANCE.reverse()
                     .convert(path.getFileName().toString()));
      } catch (IOException | UncheckedIOException e) {
         sLog.error("Problem reading keys", e);
         return Stream.empty();
      }
   }

   @Override
   public void saveIndex(DataSetIndex index) {
      byte[] toSave = index.toBytes();
      try {
         Files.write(this.directory.resolve(INDEX_NAME), toSave);
      } catch (IOException e) {
         sLog.error("Problem saving index", e);
      }
   }

   @Override
   public DataSetIndex getIndex() {
      try {
         byte[] value = Files.readAllBytes(this.directory.resolve(INDEX_NAME));
         return new DataSetIndex(value);
      } catch (IOException e) {
         sLog.trace("Expected exception if file is missing", e);
      } catch (Exception e) {
         sLog.error("Problem loading index", e);
      }
      return new DataSetIndex();
   }

   @Override
   public void clear() throws IOException {
      File file = directory.toFile();
      for (File children : file.listFiles()) {
         if (children.isDirectory()) {
            FileUtils.deleteDirectory(file);
         } else {
            children.delete();
         }

      }
      file.mkdirs();
      if (!file.exists()) {
         throw new IOException("Failed to setup storage directory " + directory);
      }
   }
}
