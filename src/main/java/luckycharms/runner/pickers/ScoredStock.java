package luckycharms.runner.pickers;

import luckycharms.util.DualBase;

public class ScoredStock extends DualBase<String, Double> implements Comparable<ScoredStock> {

   public ScoredStock(String a, Double b) {
      super(a, b);
   }

   public double score() {
      return b.doubleValue();
   }

   public String symbol() {
      return a;
   }

   @Override
   public int compareTo(ScoredStock o) {
      return Double.compare(o.score(), score());
   }

}
