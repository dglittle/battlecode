package sprint4.chaos;

import sprint4.core.D;

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
