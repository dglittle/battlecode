package legenClone.blocks;

import legenClone.core.M;
import legenClone.core.S;
import legenClone.util.LittleUtil;
import legenClone.util.MapUtil;
import legenClone.util.NotBugUtil;
import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class LittleExplorer {

  public static MapLocation macroGoal = null;

  public static final int deadZoneRadiusSq = 10 * 10;
  public static MapLocation[] deadZones = new MapLocation[1];
  public static int numDeadZones = 0;
  public static int nextDeadZone = 0;

  public static final boolean inDeadZone(MapLocation m) {
    for (int i = 0; i < numDeadZones; i++) {
      MapLocation deadZone = deadZones[i];
      if (deadZone.distanceSquaredTo(m) <= deadZoneRadiusSq)
        return true;
    }
    return false;
  }

  public static final void addDeadZone(MapLocation m) {
    if (numDeadZones < deadZones.length) {
      deadZones[numDeadZones] = m;
      numDeadZones++;
    } else {
      deadZones[nextDeadZone] = m;
      nextDeadZone = (nextDeadZone + 1) % deadZones.length;
    }
  }

  public static final Direction explore() {

    // new macro goal
    if (macroGoal == null || S.location.distanceSquaredTo(macroGoal) <= 25
        || !M.onMap(macroGoal) || inDeadZone(macroGoal)) {
      // find a place that we haven't been to, that's not too close to us
      // (if we can't find a place that we haven't been after 10 guesses,
      // then settle for a place not too near us)
      for (int i = 0; i < 100; i++) {
        macroGoal = new MapLocation(M.mapMinX
            + S.rand.nextInt(M.mapMaxX - M.mapMinX + 1), M.mapMinY
            + S.rand.nextInt(M.mapMaxY - M.mapMinY + 1));
        if (S.location.distanceSquaredTo(macroGoal) > 25
            && (i > 10 || (!M.known[macroGoal.x - M.arrayBaseX][macroGoal.y
                - M.arrayBaseY] && !inDeadZone(macroGoal)))) {
          break;
        }
      }
    }

    // see if there is some nearby unexplored territory
    /*
    double currentDirCount = 0;
    Direction bestDir = null;
    double bestCount = Double.MAX_VALUE;

    for (Direction d : MapUtil.dirs) {
      double count = Double.MAX_VALUE;
      MapLocation cursor = S.location;
      for (int i = 0; i < 5; i++) {
        cursor = cursor.add(d);
        if (!M.onMap(cursor) || inDeadZone(cursor)) {
          break;
        } else if (!M.known[cursor.x - M.arrayBaseX][cursor.y - M.arrayBaseY]) {
          count = i * (d.isDiagonal() ? 1.41421356 : 1);
          break;
        } else if (!M.land[cursor.x - M.arrayBaseX][cursor.y - M.arrayBaseY]) {
          break;
        }
      }
      if (count < bestCount) {
        bestCount = count;
        bestDir = d;
      }
      if (d == S.direction) {
        currentDirCount = count;
      }
    }

    if (bestDir != null) {
      if (S.movementController.canMove(bestDir))
        return bestDir;
    }
*/

    // guess not, so let's bug toward the macro goal
    return BugPathfinding.bugTo(macroGoal);
  }
}
