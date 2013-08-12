package heavy1.test.players;

import heavy1.chaos.DummyTopLevel;
import heavy1.core.D;
import heavy1.core.X;
import battlecode.common.RobotController;

public class DummyTestRobotPlayer implements Runnable {

  public DummyTestRobotPlayer(RobotController controller) {
    X.init(controller);
  }

  @Override
  public void run() {
    while (true) {
     try {
       DummyTopLevel.run();
       while (true)
         X.yield();
      } catch (Exception e) {
        D.debug_logException(e);  // This'll make us fail tests.
      }
    }
  }
}
