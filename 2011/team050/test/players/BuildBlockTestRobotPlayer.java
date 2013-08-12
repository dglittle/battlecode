package team050.test.players;

import team050.core.D;
import team050.core.X;
import team050.test.toplevel.BuildBlockTestTopLevel;
import battlecode.common.RobotController;

public class BuildBlockTestRobotPlayer implements Runnable {

  public BuildBlockTestRobotPlayer(RobotController controller) {
    X.init(controller);
  }

  @Override
  public void run() {
    while (true) {
     try {
       BuildBlockTestTopLevel.run();
       while (true)
         X.yield();
      } catch (Exception e) {
        D.debug_logException(e);  // This'll make us fail tests.
      }
    }
  }
}
