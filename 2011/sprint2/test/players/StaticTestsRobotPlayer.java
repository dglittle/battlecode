package sprint2.test.players;

import sprint2.core.S;
import sprint2.core.X;
import sprint2.test.SensorTest;
import sprint2.test.util.ComponentUtilTest;
import sprint2.test.util.MapUtilTest;
import sprint2.test.util.RobotUtilTest;
import sprint2.util.RobotUtil;
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
