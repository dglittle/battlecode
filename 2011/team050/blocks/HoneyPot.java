package team050.blocks;

import team050.core.M;
import team050.core.S;
import team050.core.X;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.world.IronComponent;

public class HoneyPot {

  public static final void go() throws GameActionException {
    M.disableMapUpdates();

    while (true) {
      JumpExplorationBlock.update();

      // deal with irons
      if (S.round % 2 == 0) {
        for (ComponentController cc : S.rc.components()) {
          if (cc.type() == ComponentType.IRON && !cc.isActive()) {
            IronComponent ic = (IronComponent) cc;
            ic.activateShield();
          }
        }
      }

      // if we see the enemy, stay here
      if (AttackUtil.getClosestMobileEnemy() != null) {
        continue;
      }

      // ////////////////////////
      // Movement

      JumpExplorationBlock.step();
      X.yield();
    }
  }

}
