package team050.chaos.little;

import team050.blocks.JumpExplorationBlock;
import team050.blocks.JumpUtil;
import team050.blocks.SyncBuildBlock;
import team050.blocks.pathfinding.BugPathfindingBlock;
import team050.core.D;
import team050.core.S;
import team050.core.U;
import team050.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class JumpColonistBlock {

  public static boolean inited = false;

  public static MapLocation goalMine = null;
  public static int spin = 0;
  public static int ignoreRounds = 0;
  private static final int IGNORE_ROUNDS = 17;

  public static final void init() {
    JumpExplorationBlock.init();
    inited = true;
  }

  public static final void update() {
    if (!inited)
      init();
    JumpExplorationBlock.update();

    ignoreRounds--;
  }

  public static final void step() throws GameActionException {

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
        if (!S.builderController.isActive()
            && S.allowedToBuildMine(Chassis.BUILDING, ComponentType.RECYCLER)) {
          try {
            SyncBuildBlock.build(goalMine, Chassis.BUILDING,
                ComponentType.RECYCLER);
          } catch (Exception e) {
          }
          spin = 3;
          goalMine = null;
          updateGoalMine();
        }
      } else {
        ignoreRounds = IGNORE_ROUNDS;
        goalMine = null;
      }
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
        } else if (S.location.isAdjacentTo(goalMine)) {
          // good...
        } else {
          // can we jump closer?
          boolean jumped = false;
          if (S.jumpReady()) {
            MapLocation loc = JumpUtil.closestPotentialJumpLocationTo_butNotOnIt(goalMine);
            if (loc != null) {
              jumped = X.jump(loc);
            }
          }

          if (!jumped) {
            // if we're not close enough, move toward it
            BugPathfindingBlock.bugTowardAsync(goalMine, 2);
          }
        }
      } else {
        // no mine,
        // let's explore, I guess
        if (spin > 0) {
          X.setDirection(S.direction.rotateLeft().rotateLeft());
          spin--;
        } else {
          JumpExplorationBlock.step();
        }
      }
    }
  }

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
        && (goalMine == null || nearestMine.distanceSquaredTo(S.location) < goalMine.distanceSquaredTo(S.location))) {
      if (JumpUtil.canJumpNextTo(nearestMine)) {
        goalMine = nearestMine;
        spin = 0;
      }
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
}
