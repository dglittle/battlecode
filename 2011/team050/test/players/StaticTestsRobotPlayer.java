package team050.test.players;

import team050.core.D;
import team050.core.S;
import team050.core.X;
import team050.core.xconst.XChassis;
import team050.test.core.RoleTest;
import team050.test.core.SensorTest;
import team050.test.xconst.BaseConfigsTest;
import team050.test.xconst.MovementRangesTest;
import team050.test.xconst.SensorRangesTest;
import team050.test.xconst.XChassisTest;
import team050.test.xconst.XComponentClassTest;
import team050.test.xconst.XComponentTypeTest;
import team050.test.xconst.XDirectionTest;
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
      BaseConfigsTest.debug_Test();
      RoleTest.debug_Test();
      SensorTest.debug_Test();
      MovementRangesTest.debug_Test();
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
