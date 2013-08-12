package flyingRush.test.players;

import flyingRush.chaos.DummyTopLevel;
import flyingRush.core.D;
import flyingRush.core.X;
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
