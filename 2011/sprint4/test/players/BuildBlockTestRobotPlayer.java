package sprint4.test.players;

import sprint4.chaos.BuildBlockTestTopLevel;
import sprint4.core.D;
import sprint4.core.X;
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
