package lucky.charms.portfolio;

import java.time.Duration;
import java.util.Map;

public interface Portfolio {

   PortfolioWorth getWorth();

   PortfolioState getState();

   void sell(Map<String, Integer> toSell);

   Duration waitForPendingSells(Duration ofMinutes);

   void buy(Map<String, Integer> toBuy);

   Duration waitForPendingBuys(Duration ofMinutes);

}
