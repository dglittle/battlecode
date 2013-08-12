package heavy1.test.players;

import heavy1.core.D;
import heavy1.core.S;
import heavy1.core.X;
import heavy1.core.xconst.XChassis;
import heavy1.test.core.SensorTest;
import heavy1.test.xconst.MovementRangesTest;
import heavy1.test.xconst.SensorRangesTest;
import heavy1.test.xconst.XChassisTest;
import heavy1.test.xconst.XComponentClassTest;
import heavy1.test.xconst.XComponentTypeTest;
import heavy1.test.xconst.XDirectionTest;
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
