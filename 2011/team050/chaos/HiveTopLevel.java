package team050.chaos;

import team050.core.Role;
import team050.core.S;
import team050.core.U;
import team050.core.X;
import team050.core.xconst.XChassis;
import team050.core.xconst.XComponentType;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;

public class HiveTopLevel {
  public static Chassis[] chassis = {Chassis.HEAVY, Chassis.FLYING};
  public static ComponentType[][] components = {Role.HEAVY_SOLDIER.components,
    Role.FLYING_COLONIST.components};
  public static int[] ratio = {3, 1};
  public static int totalNumToBuild = 3;
  
  public static final void run() throws GameActionException {
    switch(S.chassisInt) {
      case XChassis.BUILDING_INT:
        while(S.builderController == null)
          X.yield();
        if (S.builderTypeInt == XComponentType.RECYCLER_INT)
          S.rc.turnOff();
        YingBuildBlock.setParameters(null, true, true, chassis, components, ratio);
        int numBuilt  = 0;
        while (true) {
          if (numBuilt < totalNumToBuild) {
            if (YingBuildBlock.async())
              numBuilt++;
            }
          X.yield();
        }
      case XChassis.LIGHT_INT:
        YingBuildBlock.setParameters(null, true, true, chassis, components, ratio);
        while (true) {
          if (YingBuildBlock.async()) {
            if (U.isTrapped()) {
              S.rc.turnOff();
            } else {
              X.yield();
              return;
            }
          }
          X.yield();
        }
      default:
        break;
    }
  }
}
