package team050.test.players;

import team050.chaos.DummyTopLevel;
import team050.core.D;
import team050.core.X;
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
