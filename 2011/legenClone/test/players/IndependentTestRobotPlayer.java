package legenClone.test.players;

import legenClone.core.S;
import legenClone.core.X;
import legenClone.toplevel.IndependentDefenderTopLevel;
import legenClone.toplevel.IndependentMineTopLevel;
import legenClone.util.RobotUtil;
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
