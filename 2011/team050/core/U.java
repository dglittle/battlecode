package team050.core;

import team050.blocks.JumpUtil;
import team050.blocks.RandomMovementBlock;
import team050.core.xconst.XComponentType;
import team050.core.xconst.XDirection;
import battlecode.common.Chassis;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;
import battlecode.common.TerrainTile;

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
   * True if the robot has all the listed components.
   * 
   * @param types the components that the robot should have
   */
  public static boolean hasComponents(ComponentType... types) {
    int[] typeCount = new int[XComponentType.COMPONENT_TYPES];
    for (int i = S.components.length - 1; i >= 0; i--) {
      typeCount[S.components[i].ordinal()]++;
    }
    for (int i = types.length - 1; i >= 0; i--) {
      final int typeInt = types[i].ordinal();
      if (typeCount[typeInt] > 0) {
        typeCount[typeInt]--;
      } else {
        return false;
      }
    }
    return true;
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

  /** True if all the robot's components are fired up and ready to go. */
  public static boolean allComponentsReady() {
    final ComponentController[] components = S.rc.components();
    for (int i = components.length - 1; i >= 0; i--) {
      if (components[i].isActive()) {
        return false;
      }
    }
    return true;
  }

  /** True if b {@link Object#equals(Object)} an element in a. */
  public static <T> boolean find(T[] haystack, T needle) {
    for (int i = haystack.length - 1; i >= 0; i--) {
      if (haystack[i].equals(needle)) {
        return true;
      }
    }
    return false;
  }

  /** True if b == an element in a. */
  public static <T> boolean findEnum(T[] haystack, T needle) {
    for (int i = haystack.length - 1; i >= 0; i--) {
      if (haystack[i] == needle) {
        return true;
      }
    }
    return false;
  }

  /** How many times b {@link Object#equals(Object)} an element in a. */
  public static final <T> int count(T[] haystack, T needle) {
    int occurrences = 0;
    for (int i = haystack.length - 1; i >= 0; i--) {
      if (haystack[i].equals(needle)) {
        occurrences += 1;
      }
    }
    return occurrences;
  }

  /** How many times b == an element in a. */
  public static final <T> int countEnum(T[] haystack, T needle) {
    int occurrences = 0;
    for (int i = haystack.length - 1; i >= 0; i--) {
      if (haystack[i] == needle) {
        occurrences += 1;
      }
    }
    return occurrences;
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
    if (m == null) return "null";
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
    // If there is jumpControllers, we should not be trapped, hopefully.
    if (S.jumpControllers != null) return false;
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

  public static final boolean isAdjacent(int x1, int y1, int x2, int y2) {
    final int dx = x1 - x2;
    final int dy = y1 - y2;
    return dx <= 1 && dx >= -1 && dy <= 1 && dy >= -1
        && (!(dx == 0 && dy == 0));
  }

  /** How many adjacent squares are good for putting buildings or spawning. */
  public static int adjacentBuildingLots() {
    final SensorController sensor = S.buildingSensorController;
    int emptyLots = 0;
    for (int d = XDirection.ADJACENT_DIRECTIONS - 1; d >= 0; d--) {
      final MapLocation there = S.location.add(XDirection.intToDirection[d]);
      TerrainTile tt = S.rc.senseTerrainTile(there);
      if (tt == TerrainTile.LAND) {
        try {
          if (sensor.senseObjectAtLocation(there, RobotLevel.MINE) != null) {
            continue;
          }
          final GameObject object = sensor.senseObjectAtLocation(there,
              RobotLevel.ON_GROUND);
          if (object != null) {
            final RobotInfo info = sensor.senseRobotInfo((Robot) object);
            if (info.chassis == Chassis.BUILDING
                || info.chassis == Chassis.DEBRIS)
              continue;
          }
          emptyLots++;
        } catch (GameActionException gae) {
          D.debug_logException(gae);
        }
      }
    }
    return emptyLots;
  }

  public static Direction[] offsets = new Direction[]{
      Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.NORTH,
      Direction.SOUTH_WEST, Direction.SOUTH_EAST, Direction.NORTH_WEST,
      Direction.NORTH_EAST};

  public static boolean buildLocationTaken(MapLocation goal)
      throws GameActionException {
    if (S.direction != S.location.directionTo(goal))
      return false;

    for (Direction offset : offsets) {
      MapLocation spot = goal.add(offset);
      if (spot.equals(S.location) && S.level == RobotLevel.ON_GROUND)
        return false;
      RobotInfo ri = S.senseRobotInfo(spot, RobotLevel.ON_GROUND);
      if (ri != null && ri.robot.getTeam() == S.team
          && ri.chassis != Chassis.BUILDING
          && ri.direction == offset.opposite()) {
        return true;
      }
      if (spot.equals(S.location) && S.level == RobotLevel.IN_AIR)
        return false;
      ri = S.senseRobotInfo(spot, RobotLevel.IN_AIR);
      if (ri != null && ri.robot.getTeam() == S.team
          && ri.direction == offset.opposite()) {
        return true;
      }
    }
    D.debug_assert(false, "we should never get here");
    return false;
  }
  
  /**
   * 
   * @param component
   * @return True is a component is a weapon.
   */
  public static final boolean isWeapon(ComponentType component) {
    if (component.ordinal() <= XComponentType.BEAM_INT && 
        component.ordinal() >= XComponentType.SMG_INT) 
      return true;
    return false;
  }
  
  /**
   * 
   * @param goal
   * @return true if we can easily go to the goal
   * @throws GameActionException
   */
  public static boolean canEasilyGetTo(MapLocation goal)
      throws GameActionException {
    // flying guys can easily get anywhere!
    if (S.chassis == Chassis.FLYING)
      return true;

    // for jumpers...
    if (S.jumpControllers != null) {
      return JumpUtil.canJumpNextTo(goal);
    }

    MapLocation cursor = S.location;
    MapLocation newCursor = null;
    while (!cursor.equals(goal)) {
      Direction toGoal = cursor.directionTo(goal);

      newCursor = cursor.add(toGoal);
      if (S.rc.senseTerrainTile(newCursor) == TerrainTile.LAND
          && S.sensorController.canSenseSquare(newCursor)
          && S.senseRobot(newCursor, RobotLevel.ON_GROUND) == null) {
        cursor = newCursor;
        continue;
      }

      newCursor = cursor.add(toGoal.rotateLeft());
      if (S.rc.senseTerrainTile(newCursor) == TerrainTile.LAND
          && S.sensorController.canSenseSquare(newCursor)
          && S.senseRobot(newCursor, RobotLevel.ON_GROUND) == null) {
        cursor = newCursor;
        continue;
      }

      newCursor = cursor.add(toGoal.rotateRight());
      if (S.rc.senseTerrainTile(newCursor) == TerrainTile.LAND
          && S.sensorController.canSenseSquare(newCursor)
          && S.senseRobot(newCursor, RobotLevel.ON_GROUND) == null) {
        cursor = newCursor;
        continue;
      }
      return false;
    }
    return true;
  }
}
