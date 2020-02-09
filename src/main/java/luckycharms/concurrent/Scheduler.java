package luckycharms.concurrent;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class Scheduler {
   private static final org.slf4j.Logger sLog = org.slf4j.LoggerFactory.getLogger(Scheduler.class);

   private static final ScheduledExecutorService sEx = Executors
         .newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()//
               .setDaemon(true)//
               .setNameFormat("Scheduler-%d")//
               .setUncaughtExceptionHandler((thread, throwable) -> //
               sLog.error("Unexpected Error on Thread {}", thread, throwable))//
               .build());

   public static final void doLater(Duration dur, Runnable action) {
      doLater(dur.toMillis(), action, DirectExecutor.INSTANCE);
   }

   public static final void doLater(long millis, Runnable action) {
      doLater(millis, action, DirectExecutor.INSTANCE);
   }

   public static final void doLater(Duration dur, Runnable action, Executor pRunOn) {
      doLater(dur.toMillis(), action, pRunOn);
   }

   public static final void doLater(long millis, Runnable action, Executor pRunOn) {
      sEx.schedule(() -> pRunOn.execute(action), millis, TimeUnit.MILLISECONDS);
   }
}
