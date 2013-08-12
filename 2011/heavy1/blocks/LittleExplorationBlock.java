package heavy1.blocks;

import heavy1.blocks.pathfinding.BugPathfindingBlock;
import heavy1.core.M;
import heavy1.core.S;
import heavy1.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class LittleExplorationBlock {

  public static MapLocation macroGoal = null;

  public static final int macroGoalRadiusSq = 25;
  
  public static final void exploreMove() throws GameActionException {
    Direction d = explore();
    if (d.ordinal() < 8) {
      if (S.direction == d) {
        X.moveForward();
      } else {
        X.setDirection(d);
      }
    }
  }

  public static final Direction explore() {

    // new macro goal
    if (macroGoal == null
        || S.location.distanceSquaredTo(macroGoal) <= macroGoalRadiusSq
        || !M.onMap(macroGoal)) {
      // find a place that we haven't been to, that's not too close to us
      // (if we can't find a place that we haven't been after 10 guesses,
      // then settle for a place not too near us)
      for (int i = 0; i < 100; i++) {
        macroGoal = new MapLocation(M.mapMinX
            + S.rand.nextInt(M.mapMaxX - M.mapMinX + 1), M.mapMinY
            + S.rand.nextInt(M.mapMaxY - M.mapMinY + 1));
        if (S.location.distanceSquaredTo(macroGoal) > macroGoalRadiusSq
            && (i > 10 || (!M.known[macroGoal.x - M.arrayBaseX][macroGoal.y
                - M.arrayBaseY]))) {
          break;
        }
      }
    }

    // guess not, so let's bug toward the macro goal
    return BugPathfindingBlock.nextDirection(macroGoal);
  }
}
