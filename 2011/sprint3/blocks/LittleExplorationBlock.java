package sprint3.blocks;

import sprint3.blocks.pathfinding.BugPathfindingBlock;
import sprint3.core.M;
import sprint3.core.S;
import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class LittleExplorationBlock {

  public static MapLocation macroGoal = null;

  public static final int macroGoalRadiusSq = 25;

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
