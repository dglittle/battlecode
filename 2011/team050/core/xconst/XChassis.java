package team050.core.xconst;

import battlecode.common.Chassis;

/** Expands {@link Chassis}. */
public final class XChassis {
  // NOTE: the fastest way to get an int from a Chassis is to call Chassis.ordinal().
  
  /** Converts an int between 0 and 6 to a Chassis. */
  public static Chassis[] intToChassis = {
    Chassis.LIGHT, Chassis.MEDIUM, Chassis.HEAVY, Chassis.FLYING, Chassis.BUILDING, Chassis.DUMMY, 
    Chassis.DEBRIS
  };
  
  /** The int for Chassis.LIGHT. */
  public static final int LIGHT_INT = 0;
  /** The int for Chassis.MEDIUM. */
  public static final int MEDIUM_INT = 1;
  /** The int for Chassis.HEAVY. */
  public static final int HEAVY_INT = 2;
  /** The int for Chassis.FLYING. */
  public static final int FLYING_INT = 3;
  /** The int for Chassis.BUILDING. */
  public static final int BUILDING_INT = 4;
  /** The int for Chassis.DUMMY. */
  public static final int DUMMY_INT = 5;
  /** The int for Chassis.DEBRIS. */
  public static final int DEBRIS_INT = 6;
  
  /** Total number of chassis types. */
  public static final int CHASSIS_TYPES = intToChassis.length;
}
