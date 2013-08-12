package heavy1.toplevel;

import heavy1.blocks.BuildBlock;
import heavy1.core.S;
import heavy1.core.U;
import heavy1.core.X;
import heavy1.core.xconst.XChassis;
import heavy1.core.xconst.XComponentType;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;

public class HiveTopLevel {
  public static Chassis[] chassis = {Chassis.FLYING, Chassis.FLYING};
  public static ComponentType[][] components = {{ComponentType.CONSTRUCTOR, ComponentType.SIGHT},
    {ComponentType.BLASTER, ComponentType.RADAR}};
  public static int[] ratio = {0, 8};
  
  public static final void run() throws GameActionException {
    switch(S.chassisInt) {
      case XChassis.BUILDING_INT:
        while(S.builderController == null)
          X.yield();
        if (S.builderTypeInt == XComponentType.RECYCLER_INT)
          S.rc.turnOff();
        BuildBlock.setParameters(null, true, false, chassis, components, ratio);
        while (true) {
          BuildBlock.async();
          X.yield();
        }
      case XChassis.LIGHT_INT:
        BuildBlock.setParameters(null, true, false, chassis, components, ratio);
        while (true) {
          if (BuildBlock.async()) {
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
