package heavy1.chaos;

import heavy1.blocks.LittleExplorationBlock;
import heavy1.blocks.pathfinding.BugPathfindingBlock;
import heavy1.chaos.pwnall.VikingBuildBlock;
import heavy1.core.D;
import heavy1.core.Role;
import heavy1.core.S;
import heavy1.core.U;
import heavy1.core.X;
import battlecode.common.Chassis;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class Colonist {
  public static MapLocation goalMine = null;
  public static int spin = 0;
  public static int ignoreRounds = 0;
  private static final int IGNORE_ROUNDS = 17;
  public static final Role[] recyclerRole = {Role.RECYCLER};
  public static boolean _setRecyclerOrder = false;
  
  public static final void updateGoalMine() throws GameActionException {
    if (ignoreRounds > 0)
      return;

    // see if our current goal is taken
    if (goalMine != null && S.sensorController.canSenseSquare(goalMine)) {
      RobotInfo ri = S.senseRobotInfo(goalMine, RobotLevel.ON_GROUND);
      if (ri != null && ri.chassis == Chassis.BUILDING) {
        goalMine = null;
      }
    }

    // see if we see a better mine to capture
    MapLocation nearestMine = S.nearestEmptyMine();
    if (nearestMine != null
        && (goalMine == null || 
            nearestMine.distanceSquaredTo(S.location) < goalMine.distanceSquaredTo(S.location))) {
      goalMine = nearestMine;
      spin = 0;
    }
  }

  public static Direction[] offsets = new Direction[]{
      Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.NORTH,
      Direction.SOUTH_WEST, Direction.SOUTH_EAST, Direction.NORTH_WEST,
      Direction.NORTH_EAST};

  public static boolean mineTaken(MapLocation mine) throws GameActionException {
    for (Direction offset : offsets) {
      MapLocation spot = mine.add(offset);
      if (spot.equals(S.location))
        return false;
      RobotInfo ri = S.senseRobotInfo(spot, RobotLevel.ON_GROUND);
      if (ri != null && ri.robot.getTeam() == S.team
          && ri.chassis == Chassis.LIGHT && ri.direction == offset.opposite()) {
        return true;
      }
      ri = S.senseRobotInfo(spot, RobotLevel.IN_AIR);
      if (ri != null && ri.robot.getTeam() == S.team
          && ri.direction == offset.opposite()) {
        return true;
      }
    }
    D.debug_assert(false, "we should never get here");
    return false;
  }

  public static final void go() throws GameActionException {

    while (true) {
      if(!VikingBuildBlock.startedBuilding()) {
        ignoreRounds--;
        // ////////////////////////
        // update goals
        updateGoalMine();

        // ////////////////////////
        // building section

        // if we're next to our goal mine, and we have enough flux, build on it
        if (goalMine != null && S.location.isAdjacentTo(goalMine)) {
          // make sure we're facing the mine
          Direction towardMine = S.location.directionTo(goalMine);
          if (S.direction != towardMine) {
            while (S.movementController.isActive())
              X.yield();
            X.setDirection(towardMine);
            X.yield();
          }
  
          if (!mineTaken(goalMine)) {
            _setRecyclerOrderAsync();
          } else {
            ignoreRounds = IGNORE_ROUNDS;
            goalMine = null;
          }
        }
        if (goalMine == null)
          _cancelReceylerOrderAsync();
      }
      VikingBuildBlock.async();
      
      if (!VikingBuildBlock.busy) {
        if (_setRecyclerOrder) {
          spin = 3;
          goalMine = null;
          _setRecyclerOrder = false;
          updateGoalMine();
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
            if (spin > 0) {
              X.setDirection(S.direction.rotateLeft().rotateLeft());
              spin--;
            } else {
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
        }
      }
      X.yield();
    }
  }
  
  public static final boolean _setRecyclerOrderAsync() {
    VikingBuildBlock.setBuildOrder(recyclerRole, new MapLocation[]{goalMine});
    _setRecyclerOrder = true;
    return true; 
  }
  
  public static final void _cancelReceylerOrderAsync() {
    VikingBuildBlock.cancelBuildOrder();
    _setRecyclerOrder = false;
  }
}
