package flyingRush.test.players;

import flyingRush.chaos.DefensiveMineTopLevel;
import flyingRush.chaos.MineDefenderTopLevel;
import flyingRush.core.D;
import flyingRush.core.S;
import flyingRush.core.X;
import flyingRush.core.xconst.XChassis;
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
