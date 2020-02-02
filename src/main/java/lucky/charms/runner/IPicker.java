package lucky.charms.runner;

import java.util.List;

public interface IPicker {

   default void beforeDailyExecution(IRunnerContext context) {
      // Do Nothing
   }

   List<String> pick(IRunnerContext context);

}
