package lucky.charms.runner.log;

import lucky.charms.runner.IRunnerContext;

public interface RunLog {

   DailyRunLog createDailyRunLog(IRunnerContext ctx);

   public static class LoggerBasedRunLog implements RunLog {

      @Override
      public DailyRunLog createDailyRunLog(IRunnerContext ctx) {
         return new LoggerBasedDailyRunLog(ctx);
      }

   }
}
