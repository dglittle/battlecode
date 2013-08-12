package sprint3.core.xconst;

import sprint3.core.S;
import battlecode.common.ComponentClass;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;

public class XComponentClass {
  // NOTE: the fastest way to get an int from a ComponentClass is to call ComponentClass.ordinal().
  
  /** Converts an int between 0 and 6 to a ComponentClass. */
  public static ComponentClass[] intToComponentClass = {
    ComponentClass.ARMOR, ComponentClass.BUILDER, ComponentClass.COMM, ComponentClass.MISC,
    ComponentClass.MOTOR, ComponentClass.SENSOR, ComponentClass.WEAPON
  };

  /** The int for ComponentClass.ARMOR. */
  public static final int ARMOR_INT = 0;
  /** The int for ComponentClass.BUILDER. */
  public static final int BUILDER_INT = 1;
  /** The int for ComponentClass.COMM. */
  public static final int COMM_INT = 2;
  /** The int for ComponentClass.MISC. */
  public static final int MISC_INT = 3;
  /** The int for ComponentClass.MOTOR. */
  public static final int MOTOR_INT = 4;
  /** The int for ComponentClass.SENSOR. */
  public static final int SENSOR_INT = 5;
  /** The int for ComponentClass.WEAPON. */
  public static final int WEAPON_INT = 6;

  /** Total number of component types. */
  public static final int COMPONENT_CLASS_TYPES = intToComponentClass.length;

  
  // TODO(pwnall): test this
  
  
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
}
