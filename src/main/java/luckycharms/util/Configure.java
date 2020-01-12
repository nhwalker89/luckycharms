package luckycharms.util;

@FunctionalInterface
public interface Configure<C> {
   void configure(C config);

   default C define(C config) {
      configure(config);
      return config;
   }
}
