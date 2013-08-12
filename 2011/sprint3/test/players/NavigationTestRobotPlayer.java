package sprint3.test.players;

import sprint3.chaos.VExploreTopLevel;
import sprint3.core.D;
import sprint3.core.S;
import sprint3.core.X;
import sprint3.core.xconst.XChassis;
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
      if (S.chassisInt == XChassis.LIGHT_INT) {
        VExploreTopLevel.sync();
      }
      else {
        S.rc.turnOff();
      }
    }
    catch(Exception e) {
      D.debug_logException(e);  // This'll make us fail tests.
    }
  }
}
