package team050.test.players;

import team050.core.D;
import team050.core.S;
import team050.core.X;
import team050.core.xconst.XChassis;
import team050.test.VPathfindingTestBlock;
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
