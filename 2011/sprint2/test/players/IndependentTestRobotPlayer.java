package sprint2.test.players;

import sprint2.core.S;
import sprint2.core.X;
import sprint2.toplevel.IndependentDefenderTopLevel;
import sprint2.toplevel.IndependentMineTopLevel;
import sprint2.util.RobotUtil;
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
      if (S.chassisInt == RobotUtil.LIGHT_INT) {
        IndependentDefenderTopLevel.run();
      }
      else {
        IndependentMineTopLevel.run();
      }
    }
    catch(Exception e) {
      // This'll make us fail tests.
      e.printStackTrace();
    }
  }
}
