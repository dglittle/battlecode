package sprint2.util;

import sprint2.core.S;
import sprint2.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.MapLocation;
import battlecode.common.TerrainTile;

public class BugUtil {

  public static int bugging;
  public static int bugDistToBeat;
  public static Direction bugDir;
  public static MapLocation lastBugLoc;

  public static boolean moveDirIfCan(Direction d) throws GameActionException {
    if (S.movementController.canMove(d)) {
      X.moveDir(d);
      return true;
    }
    return false;
  }

  public static boolean bugUpTo(MapLocation goal) {
    return bugTo(goal, 2);
  }

  public static boolean bugTo(MapLocation goal) {
    return bugTo(goal, 0);
  }

  public static boolean bugTo(MapLocation goal, int toWithinRadiusSq) {
    bugging = 0;

    l1 : while (true) {
      try {
        // wait until we can move
        while (S.movementController.isActive())
          X.yield();

        // see if we made it
        int dist = S.location.distanceSquaredTo(goal);
        if (dist <= toWithinRadiusSq) {
          return true;
        }

        // if we're in bug mode,
        if (bugging != 0) {
          // see if we're close enough to exit bug mode
          if (dist < bugDistToBeat) {
            bugging = 0;
          } else {
            // "face the wall" again
            bugDir = MapUtil.rotate(bugDir, bugDir.isDiagonal() ? -3 * bugging : -2
                * bugging);
            lastBugLoc = S.location;
          }
        }

        // if we're not in bug mode,
        // go the best direction toward our goal,
        // or enter bug mode if we need to go around something
        if (bugging == 0) {
          Direction d = S.location.directionTo(goal);
          if (moveDirIfCan(d))
            continue;

          Direction left = d.rotateLeft();
          if (moveDirIfCan(left))
            continue;

          Direction right = d.rotateRight();
          if (moveDirIfCan(right))
            continue;

          // we might be able to get closer by turning further left
          left = left.rotateLeft();
          if (S.location.add(left).distanceSquaredTo(goal) < dist) {
            if (moveDirIfCan(left))
              continue;
          }

          // we might be able to get closer by turning further right
          right = right.rotateRight();
          if (S.location.add(right).distanceSquaredTo(goal) < dist) {
            if (moveDirIfCan(right))
              continue;
          }

          // alas, we must go around whatever is in our way
          bugging = S.rand.nextBoolean() ? 1 : -1;
          bugDistToBeat = dist;
          bugDir = d;
          lastBugLoc = S.location;
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

            X.moveDir(bugDir);
            continue l1;
          } else {
            MapLocation checkOffMap = MapUtil.add(S.location, bugDir, 2);
            if (S.sensorController != null && S.sensorController.canSenseSquare(checkOffMap)) {
              TerrainTile tt = S.rc.senseTerrainTile(checkOffMap);
              if (tt != null && tt == TerrainTile.OFF_MAP) {
                bugging *= -1;
              }
            }
          }
          bugDir = MapUtil.rotate(bugDir, bugging);
        }

        // hm.. we tried all directions, so let's kindof reset,
        // and hopefully some robot will move out of the way
        bugging = 0;
        X.yield();
      } catch (GameActionException e) {
        if (e.getType() == GameActionExceptionType.CANT_MOVE_THERE) {
          // some robot got in our way,
          // let's just try again...
          bugging = 0;
          X.yield();
        } else {
          e.printStackTrace();  // Should never happen.
        }
      }
    }
  }
}
