package sprint3.chaos;

import sprint3.blocks.BuildBlock;
import sprint3.core.D;
import sprint3.core.S;
import sprint3.core.X;
import sprint3.core.U;
import sprint3.core.xconst.XChassis;
import sprint3.core.xconst.XComponentType;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;

public class BuildBlockTestTopLevel {
  public static final int ROLE_OTHER_RECYCLER = 0;
  public static final int ROLE_BUILDER_RECYCLER = 1;
  
  public static Chassis[] chassis = {Chassis.FLYING};
  public static ComponentType[][] components = {{ComponentType.CONSTRUCTOR, ComponentType.SIGHT}};
  
  public static final void run() throws GameActionException {
    switch(S.chassisInt) {
      case XChassis.FLYING_INT:
        waitForComponents();
        D.debug_py("Flux = " + S.flux + " dFlux = " + S.dFlux);
        Direction exploreDir = U.directions[S.rand.nextInt(8)];
        while (true) {
          if (!S.movementController.isActive()) {
            if (S.movementController.canMove(exploreDir))
              X.moveTowardsAsync(exploreDir);
            else exploreDir = U.rotateClockwise(exploreDir, 2);
          }
          X.yield();
        }
      case XChassis.BUILDING_INT:
        while(S.builderController == null)
          X.yield();
        if (S.builderTypeInt == XComponentType.RECYCLER_INT && 
            electBuilder() != ROLE_BUILDER_RECYCLER)
          S.rc.turnOff();
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
          BuildBlock.tryBuilding();
          X.yield();
        }
      default:
        break;
    }
  }
  
  public static final void waitForComponents() {
    while (!U.hasComponents(components[0])) {
      X.yield();
    }
  }
  
  // The building with the lowest id is the leader.
  // TODO(Ying): may need a better way to do this.
  public static final int electBuilder() throws GameActionException {
    for (RobotInfo ri : S.nearbyRobotInfos()) {
      if (ri.chassis != Chassis.BUILDING) {
        continue;
      }
      if (ri.robot.getID() < S.id)
        return ROLE_OTHER_RECYCLER;
    }
    return ROLE_BUILDER_RECYCLER;
  }
}
