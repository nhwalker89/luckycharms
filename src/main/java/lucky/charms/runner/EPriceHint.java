package lucky.charms.runner;

import lucky.charms.clock.MarketTimeStateData;
import luckycharms.datasets.prices.PriceBar;

public enum EPriceHint {
   DEFAULT {
      @Override
      public Double get(MarketTimeStateData time, PriceBar bar) {
         if (time.isBeforeMiddleOfValidTradingDay()) {
            return bar.getOpen();
         } else {
            return bar.getClose();
         }
      }
   },
   OPEN {
      @Override
      public Double get(MarketTimeStateData time, PriceBar bar) {
         return bar.getOpen();
      }
   },
   CLOSE {
      @Override
      public Double get(MarketTimeStateData time, PriceBar bar) {
         return bar.getClose();
      }
   },
   HIGH {
      @Override
      public Double get(MarketTimeStateData time, PriceBar bar) {
         return bar.getHigh();
      }
   },
   LOW {
      @Override
      public Double get(MarketTimeStateData time, PriceBar bar) {
         return bar.getLow();
      }
   },

   ;

   public abstract Double get(MarketTimeStateData time, PriceBar bar);
}