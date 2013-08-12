package heavy1.core.xconst;

import battlecode.common.Direction;

/** Expands @{link {@link Direction}. */
public final class XDirection {
  // NOTE: the fastest way to get an int from a Direction is to call Direction.ordinal().

  /** Converts an int between 0 and 9 to a Direction. */
  public static Direction[] intToDirection = { Direction.NORTH, Direction.NORTH_EAST,
      Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
      Direction.NORTH_WEST, Direction.NONE, Direction.OMNI, null };

  /** The int for Direction.NORTH. */
  public static final int NORTH_INT = 0;
  /** The int for Direction.NORTH_EAST. */
  public static final int NORTH_EAST_INT = 1;
  /** The int for Direction.EAST. */
  public static final int EAST_INT = 2;
  /** The int for Direction.SOUTH_EAST. */
  public static final int SOUTH_EAST_INT = 3;
  /** The int for Direction.SOUTH. */
  public static final int SOUTH_INT = 4;
  /** The int for Direction.SOUTH_WEST. */
  public static final int SOUTH_WEST_INT = 5;
  /** The int for Direction.WEST. */
  public static final int WEST_INT = 6;
  /** The int for Direction.NORTH_WEST. */
  public static final int NORTH_WEST_INT = 7;
  /** The int for Direction.NONE. */
  public static final int NONE_INT = 8;
  /** The int for Direction.OMNI. */
  public static final int OMNI_INT = 9;
  /** The int for null. */
  public static final int NULL_INT = 10;
  
  /** Converts an int between 0 and 9 to the Directon's dx. */
  public static final int intToDeltaX[] = {0, 1, 1, 1, 0, -1, -1, -1, 0, 0, 0};

  /** Converts an int between 0 and 9 to the Directon's dy. */
  public static final int intToDeltaY[] = {-1, -1, 0, 1, 1, 1, 0, -1, 0, 0, 0};

  /**
   * The cost of each direction, for pathfinding algorithms.
   * 
   * 2 for integers, 3 for fractionals.
   */
  public static final int pathfindingCost[] = {2, 3, 2, 3, 2, 3, 2, 3};

  /** Number of adjacent Directions. They are numbered between 0 and ADJACENT_DIRECTIONS - 1 */
  public static final int ADJACENT_DIRECTIONS = 8;
}
