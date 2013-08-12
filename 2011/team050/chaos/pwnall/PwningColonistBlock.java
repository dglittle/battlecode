package team050.chaos.pwnall;

import team050.blocks.PathfindingBlock;
import team050.blocks.RandomMovementBlock;
import team050.blocks.ScvBuildDriverBlock;
import team050.blocks.building.BuildBlock;
import team050.core.D;
import team050.core.S;
import team050.core.X;
import battlecode.common.Chassis;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class PwningColonistBlock {
  public static final void sync() {
    while (true) {
      async();
      X.yield();
    }
  }
  
  public static final boolean async() {
    ScvBuildDriverBlock.async();
    
    D.debug_setIndicator(1, "at " + S.location);
    // If we're building a mine and the location gets filled, disengage.
    if (BuildBlock.busy && BuildBlock._tag == MineHuntingBlock._mineTag) {
      // TODO(pwnall): leader election
      if (!S.builderController.canBuild(Chassis.BUILDING,
                                        MineHuntingBlock._goalLocation) &&
          !BuildBlock.startedBuilding()) {
       BuildBlock.cancelBuildOrder();
      }
    }
    if (BuildBlock.busy) {
      // Now we're building for real. Not allowed to move.
      D.debug_setIndicator(0, "building at " + BuildBlock.currentBuildLocation());
      MapLocation buildLocation = BuildBlock.currentBuildLocation();
      if (buildLocation != null) {
        if (S.location.isAdjacentTo(buildLocation)) {
          MineHuntingBlock.scoutForMines();
          if (S.motorReady) {
            try {
              X.setDirection(S.direction.rotateRight().rotateRight());          
              return true;
            } catch (GameActionException e) {
              D.debug_logException(e);
              // No idea why this would happen.
            }
          }
          return false;          
        } else if (S.location.equals(buildLocation)) {
          if (!RandomMovementBlock.busy) {
            RandomMovementBlock.asyncSetup();
          }
          return RandomMovementBlock.async();
        } else {
          PathfindingBlock.setParameters(buildLocation, 250);
          return PathfindingBlock.async();
        }
      }
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
