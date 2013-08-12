package heavy1.chaos.pwnall;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotLevel;
import heavy1.blocks.PathfindingBlock;
import heavy1.core.D;
import heavy1.core.S;
import heavy1.core.X;

public class PwningColonistBlock {
  public static final void sync() {
    while (true) {
      async();
      X.yield();
    }
  }
  
  public static final boolean async() {
    D.debug_setIndicator(1, "at " + S.location);
    if (VikingBuildBlock.busy) {
      D.debug_setIndicator(0, "building");
      if (VikingBuildBlock._roles == MineHuntingBlock._mineBuildOrder) {
        Direction direction = S.location.directionTo(
            MineHuntingBlock._goalLocation);
        if (!S.builderController.canBuild(direction, RobotLevel.ON_GROUND) &&
            !VikingBuildBlock.startedBuilding()) {
          VikingBuildBlock.cancelBuildOrder();
        }
      }
      if (!S.movementController.isActive()) {
        try {
          X.setDirection(S.direction.rotateRight().rotateRight());          
        } catch (GameActionException e) {
          D.debug_logException(e);
        }
      }
      return VikingBuildBlock.async();
    }
    D.debug_setIndicator(0, "not building ");
    boolean mineAction = MineHuntingBlock.async();
    if (MineHuntingBlock._goalMine != null) {
      D.debug_setIndicator(0, "mining " + MineHuntingBlock._goalLocation);
      return mineAction || PathfindingBlock.async();
    } else {
      D.debug_setIndicator(0, "exploring " + ExplorationBlock._targetLocation);
      return ExplorationBlock.async();
    }
  }
}
