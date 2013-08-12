package sprint3.test.players;

import sprint3.chaos.DefensiveMineTopLevel;
import sprint3.chaos.MineDefenderTopLevel;
import sprint3.core.D;
import sprint3.core.S;
import sprint3.core.X;
import sprint3.core.xconst.XChassis;
import battlecode.common.RobotController;

/**
 * Overrides RobotPlayer's run method to test Independent* top-level blocks.
 */
public class IndependentTestRobotPlayer implements Runnable {
  public IndependentTestRobotPlayer(RobotController controller) {
    X.init(controller);
  }

  public void run() {
    try {
      if (S.chassisInt == XChassis.LIGHT_INT) {
        MineDefenderTopLevel.run();
      }
      else {
        DefensiveMineTopLevel.run();
      }
    }
    catch(Exception e) {
      D.debug_logException(e);  // This'll make us fail tests.
    }
  }
}
