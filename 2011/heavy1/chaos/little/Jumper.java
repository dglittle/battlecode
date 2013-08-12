package heavy1.chaos.little;

import battlecode.common.GameActionException;
import heavy1.core.M;
import heavy1.core.X;

public class Jumper {
  public static void go() throws GameActionException {
    M.disableMapUpdates();
    
    while (true) {
      JumpExplorer.update();
      
      JumpExplorer.step();

      X.yield();
    }
  }
}
