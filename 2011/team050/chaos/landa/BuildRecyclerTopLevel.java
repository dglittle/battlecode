package team050.chaos.landa;

import team050.chaos.Strategy;
import team050.core.D;

public class BuildRecyclerTopLevel {

  public static Strategy strategy;

  public static void run() {
    strategy = new BuildRecyclerStrategyBlock();
    try {
      strategy.init();
      while (true) {
        strategy.step();
      }
    }
    catch (Exception e) {
      D.debug_logException(e);
    }
  }

}
