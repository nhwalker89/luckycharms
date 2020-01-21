package lucky.charms.runner;

import java.util.List;

public interface IPicker {

   List<String> pick(RunnerContext context);

   void beforeDailyExecution();
}
