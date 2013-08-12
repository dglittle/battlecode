package sprint3.blocks;

import sprint3.blocks.pathfinding.BugPathfindingBlock;
import sprint3.core.S;
import sprint3.core.U;
import sprint3.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class LittleColonist {

  public static MapLocation goalMine = null;

  public static final void go() throws GameActionException {
    while (true) {

      // ////////////////////////
      // update goals

      // see if we see a better mine to capture
      MapLocation nearestMine = S.nearestEmptyMine();
      if (nearestMine != null
          && (goalMine == null || nearestMine.distanceSquaredTo(S.location) < goalMine.distanceSquaredTo(S.location))) {
        goalMine = nearestMine;
      }

      // ////////////////////////
      // building section

      // if we're next to our goal mine, and we have enough flux, build on it
      if (goalMine != null && S.location.isAdjacentTo(goalMine)
          && !S.builderController.isActive()
          && S.allowedToBuildMine(Chassis.BUILDING, ComponentType.RECYCLER)) {
        try {
          SyncBuildBlock.build(goalMine, Chassis.BUILDING,
              ComponentType.RECYCLER);
        } catch (Exception e) {
        }
        goalMine = null;
      }

      // ////////////////////////
      // movement section

      // can we move?
      if (!S.movementController.isActive()) {
        // is there a mine to move toward?
        if (goalMine != null) {
          // if we're standing on it, move off
          if (S.location.equals(goalMine)) {
            U.moveOffSpotAsync();
          } else {
            // if we're not close enough, move toward it
            Direction d = BugPathfindingBlock.nextDirection(goalMine, 2);
            if (d.ordinal() < 8) {
              X.moveTowardsAsync(d);
            }
          }
        } else {
          // no mine,
          // let's explore, I guess
          Direction d = LittleExplorationBlock.explore();
          if (d.ordinal() < 8) {
            if (S.direction != d) {
              X.setDirection(d);
            } else {
              X.moveForward();
            }
          }
        }
      }
      X.yield();
    }
  }
}
