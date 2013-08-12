package sprint3.toplevel;

import sprint3.blocks.BuildBlock;
import sprint3.core.D;
import sprint3.core.S;
import sprint3.core.U;
import sprint3.core.X;
import sprint3.core.xconst.XChassis;
import sprint3.core.xconst.XDirection;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;

public class HiveTopLevel {
  public static Chassis[] chassis = {Chassis.FLYING};
  public static ComponentType[][] components = {{ComponentType.CONSTRUCTOR, ComponentType.SIGHT}};
  
  public static final void run() throws GameActionException {
    switch(S.chassisInt) {
      case XChassis.BUILDING_INT:
        while(S.builderController == null)
          X.yield();
        BuildBlock.setParameters(null, true, false, chassis, components);
        while (true) {
          D.debug_setIndicator(0, "flux = " + S.flux + " dflux = " + S.dFlux);
          if (U.hasComponents(ComponentType.RECYCLER))
            D.debug_setIndicator(1, "income = " + S.sensorController.senseIncome(S.r));
          BuildBlock.tryBuilding();
          X.yield();
        }
      case XChassis.LIGHT_INT:
        BuildBlock.setParameters(null, true, false, chassis, components);
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
