package luckycharms.storage;

import java.util.Optional;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;

import luckycharms.util.sizeable.ByteUnit;
import luckycharms.util.sizeable.ISizeable;

public class GlobalCache {
   public static final long MAX_WEIGHT = Math.round(ByteUnit.BYTE.fromGb(2.5));
   private static final Cache<DataSetCacheKey, Optional<ISizeable>> CACHE = initCache();

   private static Cache<DataSetCacheKey, Optional<ISizeable>> initCache() {
      return CacheBuilder.newBuilder()//
            .weigher(new Weigher<DataSetCacheKey, Optional<ISizeable>>() {

               @Override
               public int weigh(DataSetCacheKey key, Optional<ISizeable> value) {
                  return ISizeable.WEIGHER.weigh(key, value.orElse(ISizeable.ONE));
               }
            }).maximumWeight(MAX_WEIGHT).build();
   }

   public static Cache<DataSetCacheKey, Optional<ISizeable>> cache() {
      return CACHE;
   }
}
