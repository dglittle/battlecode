package heavy1.test.players;

import heavy1.chaos.pwnall.LeaderElectionBlock;
import heavy1.chaos.pwnall.PwningColonistBlock;
import heavy1.chaos.pwnall.VikingBuildBlock;
import heavy1.core.D;
import heavy1.core.Role;
import heavy1.core.S;
import heavy1.core.X;
import heavy1.core.xconst.XChassis;
import battlecode.common.ComponentType;
import battlecode.common.MapLocation;
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
        while (true) {
          if (S.builderController != null && S.sensorController != null) {
            PwningColonistBlock.sync();
          }
          else {
            X.yield();
          }          
        }
      case XChassis.BUILDING_INT:
        boolean isHive = (S.builderType == ComponentType.RECYCLER &&
            LeaderElectionBlock.isLeader());
        if (isHive) {
          VikingBuildBlock.setBuildOrder(new Role[] {Role.COLONIST,
              Role.TANK, Role.VOIDRAY},
              new MapLocation[] {null, null, null});
        }
        while (true) {
          VikingBuildBlock.async();
          if (isHive && !VikingBuildBlock.busy) {
            D.debug_pv("Build order complete");
            isHive = false;            
          }
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
