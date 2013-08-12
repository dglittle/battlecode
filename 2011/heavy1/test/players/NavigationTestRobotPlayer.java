package heavy1.test.players;

import heavy1.core.D;
import heavy1.core.S;
import heavy1.core.X;
import heavy1.core.xconst.XChassis;
import heavy1.test.VPathfindingTestBlock;
import battlecode.common.RobotController;

/**
 * Overrides RobotPlayer's run method to test Independent* top-level blocks.
 */
public class NavigationTestRobotPlayer implements Runnable {
  public NavigationTestRobotPlayer(RobotController controller) {
    X.init(controller);
  }

  public void run() {
    try {
      switch (S.chassisInt) {
      case XChassis.LIGHT_INT:
        VPathfindingTestBlock.sync();
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
