package luckycharms.runner.log;

import luckycharms.runner.IRunnerContext;

public interface RunLog {

   DailyRunLog createDailyRunLog(IRunnerContext ctx);

   public static class LoggerBasedRunLog implements RunLog {

      @Override
      public DailyRunLog createDailyRunLog(IRunnerContext ctx) {
         return new LoggerBasedDailyRunLog(ctx);
      }

   }
}
