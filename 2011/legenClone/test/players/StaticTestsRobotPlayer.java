package legenClone.test.players;

import legenClone.core.S;
import legenClone.core.X;
import legenClone.test.SensorTest;
import legenClone.test.util.ComponentUtilTest;
import legenClone.test.util.MapUtilTest;
import legenClone.test.util.RobotUtilTest;
import legenClone.util.RobotUtil;
import battlecode.common.RobotController;

/**
 * Overrides RobotPlayer's run method to run static tests.
 */
public class StaticTestsRobotPlayer implements Runnable {
  public StaticTestsRobotPlayer(RobotController controller) {
    X.init(controller);
  }

  public void run() {    
    try {
      RobotUtilTest.debug_Test();
      ComponentUtilTest.debug_Test();
      if (S.chassisInt == RobotUtil.LIGHT_INT) {
        MapUtilTest.debug_Test();
      }
      RobotUtilTest.debug_Test();
      SensorTest.debug_Test();

      System.out.println("Static tests passed!");
    }
    catch (RuntimeException e) {
      e.printStackTrace();
    }
    
    // Sleep for some time so the other robots can finish their tests too.
    for (int i = 0; i < 50; i++) { S.rc.yield(); }
    S.rc.suicide();
  }
}
