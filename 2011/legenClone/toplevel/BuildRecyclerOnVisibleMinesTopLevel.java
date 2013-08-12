package legenClone.toplevel;

import legenClone.blocks.BuildRecyclerStrategyBlock;
import legenClone.blocks.Strategy;

/** Top-level strategy for the initial constructors.  */
public class BuildRecyclerOnVisibleMinesTopLevel {
  
  public static Strategy strategy = null;
  
  public static final void run() {
    while (true) {
      try {
        if (strategy == null) {
          strategy = new BuildRecyclerStrategyBlock();
          strategy.init();
        }
        strategy.step();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
