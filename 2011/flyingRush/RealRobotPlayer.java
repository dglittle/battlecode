package flyingRush;

import flyingRush.blocks.FlyingRush;
import flyingRush.blocks.LittleColonist;
import flyingRush.blocks.Soldier;
import flyingRush.blocks.SyncBuildBlock;
import flyingRush.core.D;
import flyingRush.core.S;
import flyingRush.core.U;
import flyingRush.core.X;
import flyingRush.core.xconst.XRoleType;
import flyingRush.toplevel.HatcheryTopLevel;
import flyingRush.toplevel.HiveTopLevel;
import flyingRush.toplevel.InitialColonistBuildTopLevel;
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
            if (U.hasComponentsReady(ComponentType.CONSTRUCTOR, ComponentType.SIGHT))
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
            if (U.hasComponentsReady(ComponentType.CONSTRUCTOR, ComponentType.SIGHT))
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
                  FlyingRush.mainRecycler();
                  for (int i = 0; i < 3; ++i) {
                    SyncBuildBlock.buildColonist();
                  }
                  HiveTopLevel.run();
                }
              }
              S.rc.turnOff();
              HatcheryTopLevel.go();
            }
            if (U.hasComponentsReady(ComponentType.ARMORY)) {
              FlyingRush.mainArmory();
              HiveTopLevel.run();
            }
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
