package team050.test.players;

import team050.core.D;
import team050.core.M;
import team050.core.S;
import team050.core.X;
import team050.core.xconst.XChassis;
import battlecode.common.Direction;
import battlecode.common.RobotController;

/**
 * Overrides RobotPlayer's run method to test Independent* top-level blocks.
 */
public class MapTestRobotPlayer implements Runnable {
  public MapTestRobotPlayer(RobotController controller) {
    X.init(controller);
  }

  public void run() {
    M.disableMapUpdates();
    M.enableMapUpdates();
    try {
      switch (S.chassisInt) {
      case XChassis.LIGHT_INT:
        while (!S.motorReady) { X.yield(); }
        X.setDirection(Direction.NORTH_EAST);
        while (!S.motorReady) { X.yield(); }
        M.debug_printMap();
        X.moveBackward();
        X.yield();
        M.debug_printMap();
        while (!S.motorReady) { X.yield(); }
        X.setDirection(Direction.NORTH);
        while (!S.motorReady) { X.yield(); }
        M.debug_printMap();
        X.moveBackward();
        X.yield();
        M.debug_printMap();
        while (!S.motorReady) { X.yield(); }
        X.setDirection(Direction.WEST);
        while (!S.motorReady) { X.yield(); }
        M.debug_printMap();
        X.moveForward();
        X.yield();
        M.debug_printMap();
        break;        
      default:
        S.rc.turnOff();
      }
    }
    catch(Exception e) {
      D.debug_logException(e);  // This'll make us fail tests.
    }
  }
}
