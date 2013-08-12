package sprint4.core;

import battlecode.common.Chassis;
import battlecode.common.ComponentType;

public class PlayerConstants {
  // the threshold has a buffer so people can build mines,
  // and we calculate enough to upgrade a hive to build flying colonists
  public static final double tier1FluxThreshold = (Chassis.BUILDING.cost + ComponentType.RECYCLER.cost)
      + (Chassis.LIGHT.cost + ComponentType.CONSTRUCTOR.cost + ComponentType.SIGHT.cost)
      + (Chassis.BUILDING.cost + ComponentType.ARMORY.cost)
      + (Chassis.FLYING.cost + ComponentType.CONSTRUCTOR.cost + ComponentType.SIGHT.cost);
}
