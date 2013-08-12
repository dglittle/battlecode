package sprint4;

import sprint4.blocks.LittleColonist;
import sprint4.blocks.Soldier;
import sprint4.blocks.SyncBuildBlock;
import sprint4.core.D;
import sprint4.core.S;
import sprint4.core.U;
import sprint4.core.X;
import sprint4.core.xconst.XRoleType;
import sprint4.toplevel.HatcheryTopLevel;
import sprint4.toplevel.HiveTopLevel;
import sprint4.toplevel.InitialColonistBuildTopLevel;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class RealRobotPlayer implements Runnable {

  public RealRobotPlayer(RobotController controller) {
    X.init(controller);
  }

  @Override
  public void run() {
    // while (true) {
    // Determine role
    // Default: turn off

    // Run top-level for role
    // }

    while (true) {
      try {
        if (S.chassis == Chassis.FLYING) {
          while (true) {
            if (U.hasComponents(ComponentType.CONSTRUCTOR))
              LittleColonist.go();
            if (U.hasComponentsReady(ComponentType.BLASTER, ComponentType.RADAR))
              Soldier.go();
            X.yield();
          }
        }
        if (S.chassis == Chassis.LIGHT) {
          if (S.birthRound <= 2)
            InitialColonistBuildTopLevel.go();
          if (S.role == XRoleType.HIVE_CONSTRUCTOR)
            HiveTopLevel.run();
          while (true) {
            if (U.hasComponents(ComponentType.CONSTRUCTOR))
              LittleColonist.go();
            if (U.hasComponentsReady(ComponentType.BLASTER, ComponentType.RADAR))
              Soldier.go();
            X.yield();
          }
        } else if (S.chassis == Chassis.BUILDING) {
          if (S.birthRound <= 100) {
            S.rc.turnOff();
            HatcheryTopLevel.go();
          }
          while (true) {
            if (U.hasComponentsReady(ComponentType.RECYCLER)) {
              if (S.birthRound <= 200) {
                int buildingCount = 0;
                for (RobotInfo ri : S.nearbyRobotInfos()) {
                  if (ri.chassis.equals(Chassis.BUILDING))
                    buildingCount++;
                }
                if (buildingCount >= 3) {
                  for (int i = 0; i < 3; ++i) {
                    SyncBuildBlock.buildColonist();
                  }
                  HiveTopLevel.run();
                }
              }
              HatcheryTopLevel.go();
            }
            if (U.hasComponentsReady(ComponentType.ARMORY))
              HiveTopLevel.run();
            X.yield();
          }
        }
        U.waitForever();
      } catch (Exception e) {
        // This'll make us fail tests.
        D.debug_logException(e);
      }
    }
  }
}
