package flyingRush.test.players;

import flyingRush.core.D;
import flyingRush.core.S;
import flyingRush.core.X;
import flyingRush.core.xconst.XChassis;
import flyingRush.test.SensorTest;
import flyingRush.test.xconst.SensorRangesTest;
import flyingRush.test.xconst.XChassisTest;
import flyingRush.test.xconst.XComponentClassTest;
import flyingRush.test.xconst.XComponentTypeTest;
import flyingRush.test.xconst.XDirectionTest;
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
