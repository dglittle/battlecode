package team050.chaos.little;

import battlecode.common.GameActionException;
import team050.blocks.JumpExplorationBlock;
import team050.core.M;
import team050.core.X;

public class Jumper {
  public static void go() throws GameActionException {
    M.disableMapUpdates();
    
    while (true) {
      JumpExplorationBlock.update();
      
      JumpExplorationBlock.step();

      X.yield();
    }
  }
}
