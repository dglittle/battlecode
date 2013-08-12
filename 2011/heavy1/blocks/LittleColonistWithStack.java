package heavy1.blocks;

import heavy1.blocks.pathfinding.BugPathfindingBlock;
import heavy1.chaos.FixedSizeStack;
import heavy1.core.D;
import heavy1.core.S;
import heavy1.core.U;
import heavy1.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class LittleColonistWithStack {

  public static FixedSizeStack<MapLocation> goalMineStack = new FixedSizeStack<MapLocation>();
  public static MapLocation goalMine = null;
  public static int spin = 0;
  public static int ignoreRounds = 0;
  private static final int IGNORE_ROUNDS = 17;

  public static final void updateGoalMine() throws GameActionException {
    if (ignoreRounds > 0)
      return;

    // see if our current goal is taken
    if (goalMineStack.peek() != null && S.sensorController.canSenseSquare(goalMineStack.peek())) {
      RobotInfo ri = S.senseRobotInfo(goalMineStack.peek(), RobotLevel.ON_GROUND);
      if (ri != null && ri.chassis == Chassis.BUILDING) {
        goalMineStack.pop();
      }
    }

    // see if we see a better mine to capture
    MapLocation nearestMine = S.nearestEmptyMine();
    if (nearestMine != null && (goalMineStack.peek() == null || nearestMine.distanceSquaredTo(S.location) < goalMineStack.peek().distanceSquaredTo(S.location))) {
      goalMineStack.push(nearestMine);
      spin = 0;
    }
  }

  public static Direction[] offsets = new Direction[] { Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH_WEST, Direction.SOUTH_EAST, Direction.NORTH_WEST, Direction.NORTH_EAST };

  public static boolean mineTaken(MapLocation mine) throws GameActionException {
    for (Direction offset : offsets) {
      MapLocation spot = mine.add(offset);
      if (spot.equals(S.location))
        return false;
      RobotInfo ri = S.senseRobotInfo(spot, RobotLevel.ON_GROUND);
      if (ri != null && ri.robot.getTeam() == S.team && ri.chassis == Chassis.LIGHT && ri.direction == offset.opposite()) {
        return true;
      }
      ri = S.senseRobotInfo(spot, RobotLevel.IN_AIR);
      if (ri != null && ri.robot.getTeam() == S.team && ri.direction == offset.opposite()) {
        return true;
      }
    }
    D.debug_assert(false, "we should never get here");
    return false;
  }

  public static final void go() throws GameActionException {

    while (true) {
      ignoreRounds--;

      // ////////////////////////
      // update goals
      updateGoalMine();

      // ////////////////////////
      // building section

      // if we're next to our goal mine, and we have enough flux, build on it
      if (goalMineStack.peek() != null && S.location.isAdjacentTo(goalMineStack.peek())) {
        // make sure we're facing the mine
        Direction towardMine = S.location.directionTo(goalMineStack.peek());
        if (S.direction != towardMine) {
          while (S.movementController.isActive())
            X.yield();
          X.setDirection(towardMine);
          X.yield();
        }

        if (!mineTaken(goalMineStack.peek())) {
          if (!S.builderController.isActive() && S.allowedToBuildMine(Chassis.BUILDING, ComponentType.RECYCLER)) {
            try {
              SyncBuildBlock.build(goalMineStack.peek(), Chassis.BUILDING, ComponentType.RECYCLER);
            }
            catch (Exception e) {
            }
            spin = 3;
            goalMineStack.pop(); // Spin, don't go to the stack.
            updateGoalMine();
          }
        }
        else {
          ignoreRounds = IGNORE_ROUNDS;
          goalMineStack.pop();
        }
      }

      // ////////////////////////
      // movement section

      // can we move?
      if (!S.movementController.isActive()) {
        if (spin > 0) {
          X.setDirection(S.direction.rotateLeft().rotateLeft());
          spin--;
        }
        // is there a mine to move toward?
        else if (goalMineStack.peek() != null) {
          // if we're standing on it, move off
          if (S.location.equals(goalMineStack.peek())) {
            U.moveOffSpotAsync();
          }
          else {
            // if we're not close enough, move toward it
            Direction d = BugPathfindingBlock.nextDirection(goalMineStack.peek(), 2);
            if (d.ordinal() < 8) {
              X.moveTowardsAsync(d);
            }
          }
        }
        else {
          // no mine,
          // let's explore, I guess
          Direction d = LittleExplorationBlock.explore();
          if (d.ordinal() < 8) {
            if (S.direction != d) {
              X.setDirection(d);
            }
            else {
              X.moveForward();
            }
          }
        }
      }
      X.yield();
    }
  }
}
