package heavy1.test.players;

import heavy1.chaos.pwnall.VBuildTopLevel;
import heavy1.chaos.pwnall.PwningColonistBlock;
import heavy1.core.D;
import heavy1.core.S;
import heavy1.core.X;
import heavy1.core.xconst.XChassis;
import battlecode.common.RobotController;

/**
 * Overrides RobotPlayer's run method to test Independent* top-level blocks.
 */
public class MiningTestRobotPlayer implements Runnable {
  public MiningTestRobotPlayer(RobotController controller) {
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
        S.rc.turnOff();
        VBuildTopLevel.sync();
        break;
      default:
        S.rc.turnOff();
      }
    }
    catch(Exception e) {
      D.debug_logException(e);  // This'll make us fail tests.
    }
  }
}
