package heavy1.core;

import heavy1.blocks.RandomMovementBlock;
import heavy1.core.xconst.XDirection;
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

  public static final float SQRT2 = (float) 1.414;

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
   * @param types
   * @return
   */
  public static boolean hasComponents(ComponentType... types) {
    int count = 0;
    boolean[] marked = new boolean[types.length];
    for (ComponentController comp : S.rc.components()) {
      for (int i = 0; i < types.length; i++) {
        ComponentType type = types[i];
        if (comp.type() == type && !marked[i]) {
          count++;
          marked[i] = true;
          break;
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
    boolean[] marked = new boolean[types.length];
    for (ComponentController comp : S.rc.components()) {
      for (int i = 0; i < types.length; i++) {
        ComponentType type = types[i];
        if (comp.type() == type && !marked[i] && !comp.isActive()) {
          count++;
          marked[i] = true;
          break;
        }
      }
    }
    return count >= types.length;
  }

  public static boolean allComponentsReady() {
    for (ComponentController comp : S.rc.components()) {
      if (comp.isActive())
        return false;
    }
    return true;
  }

  /**
   * True if b {@link Object#equals(Object)} an element in a.
   * 
   * @param <T>
   * @param haystack
   * @param needle
   * @return
   */
  public static <T> boolean find(T[] haystack, T needle) {
    for (int i = haystack.length - 1; i >= 0; i--) {
      if (haystack[i].equals(needle))
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
  public static void waitForever() throws GameActionException {
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
  public static void waitForFlux(double amount) throws GameActionException {
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
  public static MapLocation getRandomAdjacentEmpty(RobotLevel level)
      throws GameActionException {
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
      if (robot.chassis.equals(Chassis.BUILDING)
          && (S.location.directionTo(robot.location).equals(Direction.SOUTH)
              || S.location.directionTo(robot.location).equals(Direction.WEST) || S.location.directionTo(
              robot.location).equals(Direction.SOUTH_WEST))) {
        return false;
      }
    }
    return true;
  }

  /**
   * generates a string suitable for display as an indicator, showing the offset
   * from your current location to m
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

  /**
   * 
   * @return true if the robot cannot move in all directions.
   */
  public static final boolean isTrapped() {
    for (int i = XDirection.ADJACENT_DIRECTIONS - 1; i >= 0; i--) {
      if (S.movementController.canMove(XDirection.intToDirection[i]))
        return false;
    }
    return true;
  }

  public static final MapLocation add(MapLocation loc, Direction d, int range) {
    int times = range;
    if (d.isDiagonal())
      times /= SQRT2;

    return loc.add(d, times);
  }

  /**
   * @param d the Direction to check
   * @return whether d is a compass direction
   */
  public static final boolean isCompassDirection(Direction d) {
    return d.ordinal() < 8;
  }
}
