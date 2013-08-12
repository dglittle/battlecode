package heavy1.chaos;

import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import heavy1.core.M;
import heavy1.core.S;
import heavy1.core.U;
import heavy1.core.X;
import heavy1.core.xconst.XChassis;
import heavy1.core.xconst.XComponentType;

public class DummyTopLevel {
  public static final int ROLE_OTHER_RECYCLER = 0;
  public static final int ROLE_BUILDER_RECYCLER = 1;
  
  public static final void run() throws GameActionException {
    switch(S.chassisInt) {
      case XChassis.FLYING_INT:
        waitForComponents();
        Direction exploreDir = U.directions[S.rand.nextInt(8)];
        while (true) {
          if (!S.dummyController.isActive()) {
            for (int i = 0; i < S.sensorEdgeDxByDirection[S.directionInt].length; i++) {
              int dx = S.sensorEdgeDxByDirection[S.directionInt][i];
              int dy = S.sensorEdgeDyByDirection[S.directionInt][i];
              int mx = S.locationX + dx, my = S.locationY + dy;
              if (M.land[mx - M.arrayBaseX][my - M.arrayBaseY]) {
                MapLocation there = new MapLocation(mx, my);
                if (S.sensorController.senseObjectAtLocation(there, RobotLevel.ON_GROUND) == null) {
                  S.dummyController.build(Chassis.DUMMY, there);
                  break;
                }
              }
            }
          }
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
        BuildOrderTopLevel.run();
        break;
      case XChassis.LIGHT_INT:
        BuildOrderTopLevel.run();
        break;
      default:
        break;
    }
  }
  
  public static final void waitForComponents() {
    while (!U.hasComponentsReady(ComponentType.DUMMY, ComponentType.SIGHT))
      X.yield();
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
