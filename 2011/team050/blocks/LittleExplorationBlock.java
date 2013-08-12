package team050.blocks;

import team050.blocks.brain.BrainState;
import team050.blocks.pathfinding.BugPathfindingBlock;
import team050.core.M;
import team050.core.S;
import team050.core.X;
import team050.core.xconst.XChassis;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class LittleExplorationBlock {

  public static MapLocation macroGoal = null;

  public static final int macroGoalRadiusSq = 25;

  public static final void exploreMove() throws GameActionException {
    if (S.motorReady) {
      Direction d = explore();
      if (d.ordinal() < 8) {
        if (S.direction == d) {
          X.moveForward();
        } else {
          X.setDirection(d);
        }
      }
    }
  }

  public static final Direction explore() {
    // new macro goal
    if (macroGoal == null
        || S.location.distanceSquaredTo(macroGoal) <= macroGoalRadiusSq
        || !M.onMap(macroGoal)) {

      _setMacroGoal();
    }
    // guess not, so let's bug toward the macro goal
    return BugPathfindingBlock.nextDirection(macroGoal);
  }
  
  public static final void _setMacroGoal() {
    if (S.chassisInt == XChassis.FLYING_INT) {
      final MapLocation brainLocation = BrainState.closestBrain();
      if (brainLocation != null) {
        macroGoal = S.location.add(
            S.location.directionTo(brainLocation).opposite(),
            5);
        if(M.mapMinX <= macroGoal.x && M.mapMaxX >= macroGoal.x &&
           M.mapMinY <= macroGoal.y && M.mapMaxY >= macroGoal.y &&
           !M.known[macroGoal.x - M.arrayBaseX][macroGoal.y - M.arrayBaseY]) {
          return;
        }
      }
    }
    
    // find a place that we haven't been to, that's not too close to us
    // (if we can't find a place that we haven't been after 10 guesses,
    // then settle for a place not too near us)
    for (int i = 0; i < 100; i++) {
      macroGoal = new MapLocation(M.mapMinX
          + S.randomInt(M.mapMaxX - M.mapMinX + 1), M.mapMinY
          + S.randomInt(M.mapMaxY - M.mapMinY + 1));
      if (S.location.distanceSquaredTo(macroGoal) > macroGoalRadiusSq
          && (i > 10 || (!M.known[macroGoal.x - M.arrayBaseX][macroGoal.y
              - M.arrayBaseY]))) {
        return;
      }
    }    
  }
}
