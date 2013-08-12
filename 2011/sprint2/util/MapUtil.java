package sprint2.util;

import sprint2.core.S;
import sprint2.core.X;
import battlecode.common.Direction;
import battlecode.common.MapLocation;

/**
 * For navigation.
 */
public final class MapUtil {
  // NOTE: the fastest way to get an int from a Direction is to call Direction.ordinal().

  /** Converts an int between 0 and 9 to a Direction. */
  public static Direction[] intToDirection = { Direction.NORTH, Direction.NORTH_EAST,
      Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
      Direction.NORTH_WEST, Direction.NONE, Direction.OMNI };

  /** Converts an int between 0 and 9 to the Directon's dx. */
  public static final int intToDeltaX[] = { 0, 1, 1, 1, 0, -1, -1, -1, 0, 0 };

  /** Converts an int between 0 and 9 to the Directon's dy. */
  public static final int intToDeltaY[] = { -1, -1, 0, 1, 1, 1, 0, -1, 0, 0 };

  /** Number of adjacent Directions. They are numbered between 0 and ADJACENT_DIRECTIONS - 1 */
  public static final int ADJACENT_DIRECTIONS = 8;

  /**
   * Initializes map-related constructs in {@link S}, such as {@link S#sensorDxByDirection}.
   * 
   * This is called by {@link S#setSensorComponent(battlecode.common.ComponentController)}.
   */
  public static final void initSensorDxDy() {
    switch (S.sensorTypeInt) {
    case ComponentUtil.SATELLITE_INT:
    case ComponentUtil.TELESCOPE_INT:
    case ComponentUtil.SIGHT_INT:
    case ComponentUtil.RADAR_INT:
    case ComponentUtil.BUILDING_SENSOR_INT:
      MapUtil1.initSensorDxDy();
      break;
    }
  };
  
  
  // TODO(pwnall): rename and test stuffs below
  
  
  public static final Direction[] dirs = new Direction[]{
    Direction.NORTH, Direction.NORTH_EAST, Direction.EAST,
    Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST,
    Direction.WEST, Direction.NORTH_WEST};

  public static final Direction[] orthoDirs = new Direction[]{
      Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

  public static MapLocation add(MapLocation a, Direction d, int times) {
    return new MapLocation(a.x + (d.dx * times), a.y + (d.dy * times));
  }

  public static Direction rotate(Direction d, int amount) {
    int dd = (d.ordinal() + amount) % 8;
    if (dd < 0)
      dd += 8;
    return Direction.values()[dd];
  }

  public static boolean moveRandomDirection() throws Exception {
    for (int i = 0; i < 30; i++) {
      Direction d = Direction.values()[S.rand.nextInt(8)];
      if (S.canMove(d)) {
        X.moveDir(d);
        return true;
      }
    }
    return false;
  }  
}
