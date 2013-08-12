package heavy1.test.players;

import heavy1.blocks.DefensiveMineBlock;
import heavy1.chaos.pwnall.MineDefenderTopLevel;
import heavy1.core.D;
import heavy1.core.S;
import heavy1.core.X;
import heavy1.core.xconst.XChassis;
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
