package sprint3.test.players;

import sprint3.core.D;
import sprint3.core.S;
import sprint3.core.X;
import sprint3.core.xconst.XChassis;
import sprint3.test.SensorTest;
import sprint3.test.xconst.SensorRangesTest;
import sprint3.test.xconst.XChassisTest;
import sprint3.test.xconst.XComponentClassTest;
import sprint3.test.xconst.XComponentTypeTest;
import sprint3.test.xconst.XDirectionTest;
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
      XChassisTest.debug_Test();
      XComponentClassTest.debug_Test();
      XComponentTypeTest.debug_Test();
      if (S.chassisInt == XChassis.LIGHT_INT) {
        XDirectionTest.debug_Test();
      }
      SensorTest.debug_Test();
      SensorRangesTest.debug_Test();

      System.out.println("Static tests passed!");
    }
    catch (RuntimeException e) {
      D.debug_logException(e);
    }
    
    // Sleep for some time so the other robots can finish their tests too.
    for (int i = 0; i < 50; i++) { S.rc.yield(); }
    S.rc.suicide();
  }
}
