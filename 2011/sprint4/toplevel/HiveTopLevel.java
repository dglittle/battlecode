package sprint4.toplevel;

import sprint4.blocks.BuildBlock;
import sprint4.core.D;
import sprint4.core.S;
import sprint4.core.U;
import sprint4.core.X;
import sprint4.core.xconst.XChassis;
import sprint4.core.xconst.XDirection;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;

public class HiveTopLevel {
  public static Chassis[] chassis = {Chassis.FLYING, Chassis.FLYING};
  public static ComponentType[][] components = {{ComponentType.CONSTRUCTOR, ComponentType.SIGHT},
    {ComponentType.BLASTER, ComponentType.RADAR}};
  public static int[] ratio = {0, 5};
  
  public static final void run() throws GameActionException {
    switch(S.chassisInt) {
      case XChassis.BUILDING_INT:
        while(S.builderController == null)
          X.yield();
        BuildBlock.setParameters(null, true, false, chassis, components, ratio);
        while (true) {
          BuildBlock.tryBuilding();
          X.yield();
        }
      case XChassis.LIGHT_INT:
        BuildBlock.setParameters(null, true, false, chassis, components, ratio);
        while (true) {
          if (BuildBlock.tryBuilding()) {
            if (isTrapped()) {
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
  
  public static final boolean isTrapped() {
    for (int i = XDirection.ADJACENT_DIRECTIONS - 1; i >= 0; i--) {
      if (S.movementController.canMove(XDirection.intToDirection[i]))
        return false;
    }
    return true;
  }
}
