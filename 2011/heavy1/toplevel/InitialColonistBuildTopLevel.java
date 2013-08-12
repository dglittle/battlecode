package heavy1.toplevel;

import heavy1.blocks.RandomMovementBlock;
import heavy1.blocks.pathfinding.BugPathfindingBlock;
import heavy1.blocks.SyncBuildBlock;
import heavy1.core.D;
import heavy1.core.S;
import heavy1.core.X;
import heavy1.core.xconst.XDirection;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;

public class InitialColonistBuildTopLevel {

  /**
   * Builds two Recyclers on top of the initial two mines.
   * 
   * Preconditions: We're the initial Constructor. Postcondition: The two
   * initial mines will have Recyclers built on top of them.
   * 
   * @throws GameActionException
   */
  public static final void go() throws GameActionException {
    // Find target1 and target2
    if (S.direction != Direction.NORTH) {
      X.setDirection(Direction.NORTH);
      X.yield();
    }
    while (true) {
      if (S.sensorController.senseObjectAtLocation(S.front,
          RobotLevel.ON_GROUND) != null) {
        _target1 = S.front.add(S.direction);

        // Find target2
        MapLocation potentialTarget2 = _target1.add(S.direction.rotateLeft().rotateLeft());
        if (S.sensorController.senseObjectAtLocation(potentialTarget2,
            RobotLevel.MINE) != null) {
          _target2 = potentialTarget2;
        } else {
          _target2 = _target1.add(S.direction.rotateRight().rotateRight());
        }
        debug_checkTargets();
        break;
      }
      X.setDirection(S.direction.rotateLeft().rotateLeft());
      X.yield();
    }

    // Navigate to target1
    while (true) {
      // if standing on target2, build target1
      // if standing on target1, build target2
      if (S.location.equals(_target1)) {
        // build target2
        buildRecycler(_target2);
        break;
      } else if (S.location.equals(_target2)) {
        // build target1
        buildRecycler(_target1);
        break;
      }
      while (S.movementController.isActive()) {
        X.yield();
      }
      Direction nextDirection = BugPathfindingBlock.nextDirection(_target1);
      if (nextDirection.ordinal() < XDirection.ADJACENT_DIRECTIONS) {
        X.moveTowardsSync(nextDirection);
      }
    }
    // Back up and build on where you were
    MapLocation previousLocation = S.location;
    RandomMovementBlock.sync();
    buildRecycler(previousLocation);
  }
  
  /** Verifies that the two mines have been detected correctly. */
  public static final void debug_checkTargets() {
    try {
      D.debug_assert(S.sensorController.senseObjectAtLocation(_target1,
          RobotLevel.MINE) != null, "no mine at target1");
      D.debug_assert(S.sensorController.senseObjectAtLocation(_target1,
          RobotLevel.ON_GROUND) == null, "robot on top of target1");
      D.debug_assert(S.sensorController.senseObjectAtLocation(_target2,
          RobotLevel.MINE) != null, "no mine at target1");
      D.debug_assert(S.sensorController.senseObjectAtLocation(_target2,
          RobotLevel.ON_GROUND) == null, "robot on top of target2");
    } catch (GameActionException e) {
      D.debug_logException(e);
    }
  }

  public static final void buildRecycler(MapLocation where)
      throws GameActionException {
    while (!S.allowedToBuildMine(Chassis.BUILDING, ComponentType.RECYCLER)) {
      X.yield();
    }
    SyncBuildBlock.build(where, Chassis.BUILDING, ComponentType.RECYCLER);
  }
  
  /** The locations of the two mines. */
  public static MapLocation _target1, _target2;
}
