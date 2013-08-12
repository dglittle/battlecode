package team050.test.players;

import team050.blocks.DefensiveMineBlock;
import team050.chaos.pwnall.MineDefenderTopLevel;
import team050.core.D;
import team050.core.S;
import team050.core.X;
import team050.core.xconst.XChassis;
import battlecode.common.ComponentType;
import battlecode.common.RobotController;

/**
 * Overrides RobotPlayer's run method to test the base defending code.
 */
public class MineDefenderTestRobotPlayer implements Runnable {
  public MineDefenderTestRobotPlayer(RobotController controller) {
    X.init(controller);
  }

  public void run() {
    try {
      if (S.chassisInt == XChassis.LIGHT_INT) {
        MineDefenderTopLevel.run();
      }
      else {
        DefensiveMineBlock.setDefenseParameters(2, ComponentType.RADAR);
        DefensiveMineBlock.sync();
      }
    }
    catch(Exception e) {
      D.debug_logException(e);  // This'll make us fail tests.
    }
  }
}
