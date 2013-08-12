package team050.chaos;

import team050.core.D;
import team050.core.S;
import team050.core.U;
import team050.core.X;
import team050.core.xconst.XChassis;
import team050.core.xconst.XComponentType;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;

public class BuildBlockTestTopLevel {
  public static final int ROLE_OTHER_RECYCLER = 0;
  public static final int ROLE_BUILDER_RECYCLER = 1;
  
  public static Chassis[] chassis = {Chassis.HEAVY, Chassis.FLYING};
  public static ComponentType[][] components = {{ComponentType.JUMP, ComponentType.JUMP, 
      ComponentType.RADAR, ComponentType.BLASTER, ComponentType.SHIELD, ComponentType.SHIELD,
      ComponentType.SHIELD, ComponentType.SHIELD, ComponentType.SHIELD, ComponentType.SHIELD}, 
      {ComponentType.CONSTRUCTOR, ComponentType.SIGHT}};
  public static int[] ratio = {0, 5};
  
  public static final void run() throws GameActionException {
    switch(S.chassisInt) {
      case XChassis.FLYING_INT:
        waitForComponents(1);
        D.debug_py("Flux = " + S.flux + " dFlux = " + S.dFlux);
        Direction exploreDir = U.directions[S.randomInt(8)];
        while (true) {
          if (S.motorReady) {
            if (S.movementController.canMove(exploreDir))
              X.moveTowardsAsync(exploreDir);
            else exploreDir = U.rotateClockwise(exploreDir, 2);
          }
          X.yield();
        }
      case XChassis.HEAVY_INT:
        waitForComponents(0);
        Direction jumpDir = U.directions[4];
        while (true) {
          if (S.motorReady) {
            if (S.movementController.canMove(jumpDir))
              X.moveTowardsAsync(jumpDir);
            else jumpDir = jumpDir.rotateLeft();
          }
          X.yield();
        }
      case XChassis.BUILDING_INT:
        while(S.builderController == null)
          X.yield();
        if (S.builderTypeInt == XComponentType.RECYCLER_INT && 
            electBuilder() != ROLE_BUILDER_RECYCLER)
          S.rc.turnOff();
        YingBuildBlock.setParameters(null, true, true, chassis, components, ratio);
        while (true) {
          D.debug_setIndicator(0, "flux = " + S.flux + " dflux = " + S.dFlux);
          if (U.hasComponents(ComponentType.RECYCLER))
            D.debug_setIndicator(1, "income = " + S.sensorController.senseIncome(S.r));
          YingBuildBlock.async();
          X.yield();
        }
      case XChassis.LIGHT_INT:
        YingBuildBlock.setParameters(null, true, true, chassis, components, ratio);
        while (true) {
          if (YingBuildBlock.async()) 
              S.rc.suicide();
          X.yield();
        }
      default:
        break;
    }
  }
  
  public static final void waitForComponents(int index) {
    while (!U.hasComponents(components[index])) {
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
