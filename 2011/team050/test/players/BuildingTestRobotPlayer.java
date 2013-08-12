package team050.test.players;

import team050.blocks.GenericSoldier;
import team050.blocks.building.BuildDriverBlock;
import team050.chaos.pwnall.LeaderElectionBlock;
import team050.chaos.pwnall.PwningColonistBlock;
import team050.core.D;
import team050.core.Role;
import team050.core.S;
import team050.core.U;
import team050.core.X;
import team050.core.xconst.XChassis;
import battlecode.common.ComponentType;
import battlecode.common.RobotController;

/**
 * Overrides RobotPlayer's run method to test Independent* top-level blocks.
 */
public class BuildingTestRobotPlayer implements Runnable {
  public BuildingTestRobotPlayer(RobotController controller) {
    X.init(controller);
  }

  public void run() {
    try {
      switch (S.chassisInt) {
      case XChassis.LIGHT_INT:
      case XChassis.FLYING_INT:
        while (true) {
          if (S.builderController != null && S.sensorController != null) {
            PwningColonistBlock.sync();
          }
          else if (S.maxWeaponRange > 0) {
            GenericSoldier.go();
          } else {
            X.yield();
          }          
        }
      case XChassis.BUILDING_INT:
        if (S.builderType == ComponentType.RECYCLER) {
          LeaderElectionBlock.start();
          while(LeaderElectionBlock.busy)
            LeaderElectionBlock.async();
        }
        boolean isHive = (S.builderType == ComponentType.RECYCLER &&
            LeaderElectionBlock.isLeader);
        if (isHive) {
          BuildDriverBlock.setBuildOrder(new Role[] {Role.FLYING_COLONIST,
              Role.TANK, Role.VOIDRAY}, new int[] {1, 1, 1},
              new int[] {0, 0, 0}, 0);
        }
        while (true) {
          if (S.builderType == ComponentType.RECYCLER &&
              !LeaderElectionBlock.isLeader) {
            S.rc.turnOff();
            X.yield();
          } else {
            BuildDriverBlock.async();
            X.yield();            
          }
        }
      case XChassis.HEAVY_INT:
        while (true) {
          if (U.hasComponentsReady(Role.TANK.components))
            GenericSoldier.go();
          X.yield();
        }
      default:
        S.rc.turnOff();
      }
    }
    catch(Exception e) {
      D.debug_logException(e);  // This'll make us fail tests.
    }
  }
}
