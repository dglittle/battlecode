package team050.chaos.little;

import team050.core.M;
import team050.core.X;
import battlecode.common.GameActionException;

public class HeavyColonist {

  public static final void go() throws GameActionException {
    M.disableMapUpdates();

    while (true) {
      JumpColonistBlock.update();

      JumpColonistBlock.step();
      X.yield();
    }
  }
}
