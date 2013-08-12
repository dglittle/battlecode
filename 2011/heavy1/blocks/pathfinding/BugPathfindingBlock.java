package heavy1.blocks.pathfinding;

import heavy1.core.S;
import heavy1.core.X;
import heavy1.util.Utilities;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.TerrainTile;

public class BugPathfindingBlock {

  public static MapLocation goal;
  public static int bugging;
  public static int bugDistToBeat;
  public static Direction bugDir;
  public static MapLocation lastBugLoc;
  
  public static final void bugTowardAsync(MapLocation goal) throws GameActionException {
    Direction d = nextDirection(goal);
    if (d.ordinal() < 8)
      X.moveTowardsAsync(d);
  }
  
  public static final void bugTowardAsync(MapLocation goal, int getWithinRadiusSq) throws GameActionException {
    Direction d = nextDirection(goal, getWithinRadiusSq);
    if (d.ordinal() < 8)
      X.moveTowardsAsync(d);
  }

  public static final Direction nextDirection(MapLocation goal) {
    return nextDirection(goal, 0);
  }

  public static final Direction nextDirection(MapLocation goal,
      int getWithinRadiusSq) {
    MapLocation here = S.rc.getLocation();

    // see if we made it
    int dist = here.distanceSquaredTo(goal);
    if (dist <= getWithinRadiusSq) {
      return Direction.NONE;
    }

    // update our goal,
    // and reset our state if we have a new goal
    if (BugPathfindingBlock.goal == null
        || !BugPathfindingBlock.goal.equals(goal)) {
      bugging = 0;
      BugPathfindingBlock.goal = goal;
    }

    // if we're in bug mode,
    if (bugging != 0) {
      // see if we're close enough to exit bug mode
      if (dist < bugDistToBeat) {
        bugging = 0;
      } else {
        // see if we moved since last time
        if (!here.equals(lastBugLoc)) {
          // we did, so we need to "face the wall" again
          bugDir = Utilities.rotate(bugDir, bugDir.isDiagonal() ? -3 * bugging
              : -2 * bugging);
          lastBugLoc = here;
        } else {
          // hm.. we haven't moved,
          // so keep trying to go the same way
          if (S.movementController.canMove(bugDir))
            return bugDir;

          // I guess we can't.. let's pop out of bug mode and try
          // again
          bugging = 0;
        }
      }
    }

    // if we're not in bug mode,
    // return the best direction toward our goal,
    // or enter bug mode if we need to go around something
    if (bugging == 0) {
      Direction d = here.directionTo(goal);
      if (S.movementController.canMove(d))
        return d;

      Direction left = d.rotateLeft();
      if (S.movementController.canMove(left))
        return left;

      Direction right = d.rotateRight();
      if (S.movementController.canMove(right))
        return right;

      // we might be able to get closer by turning further left
      left = left.rotateLeft();
      if (here.add(left).distanceSquaredTo(goal) < dist
          && S.movementController.canMove(left))
        return left;

      // we might be able to get closer by turning further right
      right = right.rotateRight();
      if (here.add(right).distanceSquaredTo(goal) < dist
          && S.movementController.canMove(right))
        return right;

      // alas, we must go around whatever is in our way
      bugging = Utilities.rdm.nextBoolean() ? 1 : -1;
      bugDistToBeat = dist;
      bugDir = d;
      lastBugLoc = here;
    }

    // we must be in bug mode,
    // so let's bug
    for (int i = 0; i < 8; i++) {
      if (S.movementController.canMove(bugDir)) {

        // if this is the first direction we're trying,
        // then there should be a wall...
        if (i == 0) {
          // hm.. no wall.. where did it go?
          // the "wall" was probably another robot that has gone,
          // so let's stop bugging
          bugging = 0;
        }

        return bugDir;
      } else {
        MapLocation checkOffMap = Utilities.add(here, bugDir, 2);
        if (S.sensorController.canSenseSquare(checkOffMap)) {
          TerrainTile tt = S.rc.senseTerrainTile(checkOffMap);
          if (tt != null && tt == TerrainTile.OFF_MAP) {
            bugging *= -1;
          }
        }
      }
      bugDir = Utilities.rotate(bugDir, bugging);
    }

    // hm.. we tries all directions, and we're stuck,
    // we could throw an exception,
    // or pretend that we made arrived
    return Direction.OMNI;
  }
}
