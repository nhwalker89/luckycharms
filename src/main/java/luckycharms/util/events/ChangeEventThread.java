package luckycharms.util.events;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ChangeEventThread {
   private static final org.slf4j.Logger sLog = org.slf4j.LoggerFactory
         .getLogger(ChangeEventThread.class);
   private static final ThreadFactory baseThreadFactory = new ThreadFactoryBuilder().setDaemon(true)
         .setNameFormat("ChangeEventThread-%d").build();

   private static volatile Thread sThread = null;

   private static final ExecutorService sEx = Executors.newSingleThreadExecutor((r) -> {
      Thread thread = baseThreadFactory.newThread(r);
      sThread = thread;
      return thread;
   });

   public static boolean isOnThread() { return sThread == Thread.currentThread(); }

   public static void sendOnChange(OnChange listener) {
      if (isOnThread()) {
         try {
            listener.onChange();
         } catch (Throwable e) {
            sLog.error("Problem notifying listener {}", listener, e);
         }
      }
      sEx.execute(() -> sendOnChange(listener));
   }

   public static void sendOnChange(Collection<? extends OnChange> listeners) {
      if (isOnThread()) {
         for (OnChange c : listeners) {
            try {
               c.onChange();
            } catch (Throwable e) {
               sLog.error("Problem notifying listener {}", c, e);
            }
         }
      }
      sEx.execute(() -> sendOnChange(listeners));
   }

   static void doOnChangThread(Runnable task) {
      if (isOnThread()) {
         sEx.execute(task);
      }
   }
}
