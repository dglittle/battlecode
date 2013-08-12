package flyingRush.core.xconst;

import battlecode.common.ComponentType;

/**
 * Generic Robot stuff.
 *
 */
public final class XComponentType {
  // NOTE: the fastest way to get an int from a ComponentType is to call ComponentType.ordinal().
  
  /** Converts an int between 0 and 33 to a ComponentType. */
  public static ComponentType[] intToComponentType = {
    ComponentType.SHIELD, ComponentType.HARDENED, ComponentType.REGEN, ComponentType.PLASMA,
    ComponentType.IRON, ComponentType.PLATING, ComponentType.SMG, ComponentType.BLASTER,
    ComponentType.RAILGUN, ComponentType.HAMMER, ComponentType.BEAM, ComponentType.MEDIC,
    ComponentType.SATELLITE, ComponentType.TELESCOPE, ComponentType.SIGHT, ComponentType.RADAR,
    ComponentType.ANTENNA, ComponentType.DISH, ComponentType.NETWORK, ComponentType.PROCESSOR,
    ComponentType.JUMP, ComponentType.DUMMY, ComponentType.BUG, ComponentType.DROPSHIP,
    ComponentType.RECYCLER, ComponentType.FACTORY, ComponentType.CONSTRUCTOR, ComponentType.ARMORY,
    ComponentType.SMALL_MOTOR, ComponentType.MEDIUM_MOTOR, ComponentType.LARGE_MOTOR,
    ComponentType.FLYING_MOTOR, ComponentType.BUILDING_MOTOR, ComponentType.BUILDING_SENSOR
  };
  
  /** Invalid ComponentType that should trigger exceptions when used. */
  public static final int INVALID_INT = -1;
  /** The int for ComponentType.SHIELD. */
  public static final int SHIELD_INT = 0;
  /** The int for ComponentType.HARDENED. */
  public static final int HARDENED_INT = 1;
  /** The int for ComponentType.REGEN. */
  public static final int REGEN_INT = 2;
  /** The int for ComponentType.PLASMA. */
  public static final int PLASMA_INT = 3;
  /** The int for ComponentType.IRON. */
  public static final int IRON_INT = 4;
  /** The int for ComponentType.PLATING. */
  public static final int PLATING_INT = 5;
  /** The int for ComponentType.SMG. */
  public static final int SMG_INT = 6;
  /** The int for ComponentType.BLASTER. */
  public static final int BLASTER_INT = 7;
  /** The int for ComponentType.RAILGUN. */
  public static final int RAILGUN_INT = 8;
  /** The int for ComponentType.HAMMER. */
  public static final int HAMMER_INT = 9;
  /** The int for ComponentType.BEAM. */
  public static final int BEAM_INT = 10;
  /** The int for ComponentType.MEDIC. */
  public static final int MEDIC_INT = 11;
  /** The int for ComponentType.SATELLITE. */
  public static final int SATELLITE_INT = 12;
  /** The int for ComponentType.TELESCOPE. */
  public static final int TELESCOPE_INT = 13;
  /** The int for ComponentType.SIGHT. */
  public static final int SIGHT_INT = 14;
  /** The int for ComponentType.RADAR. */
  public static final int RADAR_INT = 15;
  /** The int for ComponentType.ANTENNA. */
  public static final int ANTENNA_INT = 16;
  /** The int for ComponentType.DISH. */
  public static final int DISH_INT = 17;
  /** The int for ComponentType.NETWORK. */
  public static final int NETWORK_INT = 18;
  /** The int for ComponentType.PROCESSOR. */
  public static final int PROCESSOR_INT = 19;
  /** The int for ComponentType.JUMP. */
  public static final int JUMP_INT = 20;
  /** The int for ComponentType.DUMMY. */
  public static final int DUMMY_INT = 21;
  /** The int for ComponentType.BUG. */
  public static final int BUG_INT = 22;
  /** The int for ComponentType.DROPSHIP. */
  public static final int DROPSHIP_INT = 23;
  /** The int for ComponentType.RECYCLER. */
  public static final int RECYCLER_INT = 24;
  /** The int for ComponentType.FACTORY. */
  public static final int FACTORY_INT = 25;
  /** The int for ComponentType.CONSTRUCTOR. */
  public static final int CONSTRUCTOR_INT = 26;
  /** The int for ComponentType.ARMORY. */
  public static final int ARMORY_INT = 27;
  /** The int for ComponentType.SMALL_MOTOR. */
  public static final int SMALL_MOTOR_INT = 28;
  /** The int for ComponentType.MEDIUM_MOTOR. */
  public static final int MEDIUM_MOTOR_INT = 29;
  /** The int for ComponentType.LARGE_MOTOR. */
  public static final int LARGE_MOTOR_INT = 30;
  /** The int for ComponentType.FLYING_MOTOR. */
  public static final int FLYING_MOTOR_INT = 31;
  /** The int for ComponentType.BUILDING_MOTOR. */
  public static final int BUILDING_MOTOR_INT = 32;
  /** The int for ComponentType.BUILDING_SENSOR. */
  public static final int BUILDING_SENSOR_INT = 33;
  
  /** Total number of component types. */
  public static final int COMPONENT_TYPES = intToComponentType.length;
}
