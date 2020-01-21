package lucky.charms.runner.log;

import luckycharms.time.units.DaysKey;

public abstract class RunLog {

   public abstract DailyRunLog createDailyRunLog(DaysKey key);
}
