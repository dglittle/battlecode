package sprint3.core;

import sprint3.blocks.RandomMovementBlock;
import sprint3.core.xconst.XDirection;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.Chassis;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

/** Small functions that don't deserve their own blocks. */
public class U {

  public static final Direction[] directions = new Direction[]{
      Direction.NORTH, Direction.NORTH_EAST, Direction.EAST,
      Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST,
      Direction.WEST, Direction.NORTH_WEST};
  // TODO(everyone): put stuff here
  public static final Direction[] orthogonalDirections = new Direction[]{
      Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

  /**
   * Rotates a {@link Direction} to the right by a multiple of 45 degrees.
   * 
   * @param d the direction to be rotated
   * @param amount multiple of 45 degrees; can be 0 or negative
   * 
   * @return the rotated {@link Direction}
   */
  public static final Direction rotateClockwise(Direction d, int amount) {
    int dd = (d.ordinal() + amount) % 8;
    if (dd < 0)
      dd += 8;
    return XDirection.intToDirection[dd];
  }

  /**
   * returns true if we have all the listed components
   * 
   * note: if you pass in two BLASTERS, it will make sure we have two of them
   * 
   * note: modifies the array you pass in, if you pass an array
   * 
   * @param types
   * @return
   */
  public static boolean hasComponents(ComponentType... types) {
    int count = 0;
    for (ComponentController comp : S.rc.components()) {
      for (int i = 0; i < types.length; i++) {
        ComponentType type = types[i];
        if (comp.type() == type) {
          count++;
          types[i] = null;
        }
      }
    }
    return count >= types.length;
  }

  /**
   * same as hasComponents, but also makes sure that each of these components is
   * ready to go (i.e. not active)
   * 
   * @param types
   * @return
   */
  public static boolean hasComponentsReady(ComponentType... types) {
    int count = 0;
    for (ComponentController comp : S.rc.components()) {
      for (int i = 0; i < types.length; i++) {
        ComponentType type = types[i];
        if (comp.type() == type && !comp.isActive()) {
          count++;
          types[i] = null;
        }
      }
    }
    return count >= types.length;
  }

  /**
   * returns true if b is equals to an element in a.
   * 
   * @param <T>
   * @param a
   * @param b
   * @return
   */
  public static <T> boolean find(T[] a, T b) {
    for (T aa : a) {
      if (aa.equals(b))
        return true;
    }
    return false;
  }

  /**
   * doesn't return until all our components are ready to go (i.e. not active)
   * 
   * @throws Exception
   */
  public static void waitForComponents() throws Exception {
    l1 : while (true) {
      for (ComponentController c : S.rc.components()) {
        if (c.isActive()) {
          X.yield();
          continue l1;
        }
      }
      break;
    }
  }

  /**
   * never returns. yields forever.
   * 
   * @throws Exception
   */
  public static void waitForever() throws Exception {
    while (true) {
      X.yield();
    }
  }

  /**
   * doesn't return until we have the given amount of flux
   * 
   * @param amount
   * @throws Exception
   */
  public static void waitForFlux(float amount) throws Exception {
    while (S.flux < amount + 2) {
      X.yield();
    }
  }
   
  /**
   * Get a random empty adjacent MapLocation or null if there is none.
   * 
   * @param level RobotLevel on which to look for an empty location
   * @return empty adjacent location or null if there is none
   * @throws GameActionException
   */
  public static MapLocation getRandomAdjacentEmpty(RobotLevel level) throws GameActionException {
    Direction direction = RandomMovementBlock.randomAvailableDirection();
    return direction == Direction.NONE ? null : S.location.add(direction);
  }
  
  /**
   * Determines whether this robot is the leader in a cluster of buildings.
   * 
   * @return whether this robot is the building leader
   */
  public static final boolean isBuildingLeader() {
    RobotInfo[] nearbyRobots = S.nearbyRobotInfos();
    for (RobotInfo robot : nearbyRobots) {
      if (robot.chassis.equals(Chassis.BUILDING) && (
           S.location.directionTo(robot.location).equals(Direction.SOUTH)
        || S.location.directionTo(robot.location).equals(Direction.WEST)
        || S.location.directionTo(robot.location).equals(Direction.SOUTH_WEST))) {
        return false;
      }
    }
    return true;
  }
  

  /**
   * generates a string suitable for display as an indicator,
   * showing the offset from your current location to m
   * 
   * @param m
   * @return
   */
  public static final String offsetTo(MapLocation m) {
    return "(" + (m.x - S.locationX) + ", " + (m.y - S.locationY) + ")";
  }

  /**
   * makes progress moving off of the spot you are on,
   * 
   * but returns false if it can't move to any adjacent square
   * 
   * @return
   * @throws GameActionException
   */
  public static final boolean moveOffSpotAsync() throws GameActionException {
    if (S.movementController.canMove(S.direction)) {
      X.moveForward();
      return true;
    } else if (S.movementController.canMove(S.direction.opposite())) {
      X.moveBackward();
      return true;
    } else {
      Direction d = S.direction;
      for (int i = 0; i < 7; i++) {
        d = d.rotateLeft();
        if (S.movementController.canMove(d)) {
          X.setDirection(d);
          return true;
        }
      }
    }
    return false;
  }
}
