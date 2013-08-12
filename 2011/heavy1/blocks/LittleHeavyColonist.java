package heavy1.blocks;

import heavy1.blocks.pathfinding.BugPathfindingBlock;
import heavy1.core.D;
import heavy1.core.S;
import heavy1.core.U;
import heavy1.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.JumpController;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

public class LittleHeavyColonist {

  public static MapLocation goalMine = null;
  public static int spin = 0;
  public static int ignoreRounds = 10;
  private static final int IGNORE_ROUNDS = 17;

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
    if (nearestMine != null && (goalMine == null || nearestMine.distanceSquaredTo(S.location) < goalMine.distanceSquaredTo(S.location))) {
      goalMine = nearestMine;
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

  public static final boolean jump(Direction direction) throws GameActionException {
    for (JumpController jc : S.jumpControllers) {
      if (!jc.isActive()) {
        final int[] jumpDx = S.jumpDxByDirection[direction.ordinal()];
        final int[] jumpDy = S.jumpDyByDirection[direction.ordinal()];
        for (int i = 0; i < jumpDx.length; i++) {
          MapLocation loc = S.location.add(jumpDx[i], jumpDy[i]);
          if (S.rc.senseTerrainTile(loc) == TerrainTile.LAND) {
            Robot r = S.sensorController.canSenseSquare(loc) ? S.senseRobot(loc, RobotLevel.ON_GROUND) : null;
            if (r == null) {
              if (!jc.withinRange(loc)) continue;
              jc.jump(loc);
              return true;
            }
          }
        }
      }
    }
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
          if (!S.builderController.isActive() && S.allowedToBuildMine(Chassis.BUILDING, ComponentType.RECYCLER)) {
            try {
              SyncBuildBlock.build(goalMine, Chassis.BUILDING, ComponentType.RECYCLER);
            }
            catch (Exception e) {
            }
            spin = 3;
            goalMine = null;
            updateGoalMine();
          }
        }
        else {
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
          }
          else {
            // if we're not close enough, move toward it
            Direction d = BugPathfindingBlock.nextDirection(goalMine, 2);
            if (d.ordinal() < 8) {
              X.moveTowardsAsync(d);
            }
          }
        }
        else {
          // no mine,
          // let's explore, I guess
          if (spin > 0) {
            X.setDirection(S.direction.rotateLeft().rotateLeft());
            spin--;
          }
          else {
            // let's explore, I guess
            Direction d = LittleExplorationBlock.explore();
            // Prioritize jumping...
            if (d.ordinal() < 8) {
              boolean jumped = jump(S.location.directionTo(LittleExplorationBlock.macroGoal));
              if (!jumped) {
                if (S.direction != d) {
                  D.debug_setIndicator(0, "Setting direction in exploration mode to " + d + ".");
                  X.setDirection(d);
                }
                else {
                  D.debug_setIndicator(0, "Moving in exploration mode in direction " + d + ".");
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
}
