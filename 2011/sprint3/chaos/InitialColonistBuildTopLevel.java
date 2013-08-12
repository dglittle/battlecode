package sprint3.chaos;

import sprint3.blocks.RandomMovementBlock;
import sprint3.blocks.pathfinding.BugPathfindingBlock;
import sprint3.blocks.SyncBuildBlock;
import sprint3.core.S;
import sprint3.core.X;
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
   * @throws GameActionException 
   */
  public static final void go() throws GameActionException {
    // Find target1 and target2
    MapLocation target1 = null, target2 = null;
    X.setDirection(Direction.NORTH);
    X.yield();
    while (true) {
      if (S.sensorController.senseObjectAtLocation(S.front, RobotLevel.ON_GROUND) != null) {
        target1 = S.location.add(S.direction, 2);
        // Find target2
        MapLocation potentialTarget2 = target1.add(S.direction.rotateLeft().rotateLeft());
        if (S.sensorController.senseObjectAtLocation(potentialTarget2, RobotLevel.MINE) != null)
          target2 = potentialTarget2;
        else
          target2 = target1.add(S.direction.rotateRight().rotateRight());
        break;
      }
      X.setDirection(S.direction.rotateLeft().rotateLeft());
      X.yield();
    }
    
    // Navigate to target1
    MapLocation buildLocation = null;
    while (true) {
      // if standing on target2, build target1
      // if standing on target1, build target2
      if (S.location.equals(target1)) {
        // build target2
        buildRecycler(target2);
        buildLocation = target2;
        break;
      }
      else if (S.location.equals(target2)) {
        // build target1
        buildRecycler(target1);
        buildLocation = target1;
        break;
      }
      while (S.movementController.isActive()) {
        X.yield();
      }
      Direction nextDirection = BugPathfindingBlock.nextDirection(target1);
      if (nextDirection.ordinal() < 8) {
        X.moveTowardsSync(nextDirection);
      }
    }
    // Back up and build on where you were
    MapLocation previousLocation = S.location;
    RandomMovementBlock.sync();
    buildRecycler(previousLocation);
  }
  
  public static final void buildRecycler(MapLocation where) throws GameActionException {
    while (!S.allowedToBuildMine(Chassis.BUILDING, ComponentType.RECYCLER)) {
      X.yield();
    }
    SyncBuildBlock.build(where, Chassis.BUILDING, ComponentType.RECYCLER);
  }
}
