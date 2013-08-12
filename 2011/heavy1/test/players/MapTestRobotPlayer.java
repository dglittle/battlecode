package heavy1.test.players;

import heavy1.core.D;
import heavy1.core.M;
import heavy1.core.S;
import heavy1.core.X;
import heavy1.core.xconst.XChassis;
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
        while (S.movementController.isActive()) { X.yield(); }
        X.setDirection(Direction.NORTH_EAST);
        while (S.movementController.isActive()) { X.yield(); }
        M.debug_printMap();
        X.moveBackward();
        X.yield();
        M.debug_printMap();
        while (S.movementController.isActive()) { X.yield(); }
        X.setDirection(Direction.NORTH);
        while (S.movementController.isActive()) { X.yield(); }
        M.debug_printMap();
        X.moveBackward();
        X.yield();
        M.debug_printMap();
        while (S.movementController.isActive()) { X.yield(); }
        X.setDirection(Direction.WEST);
        while (S.movementController.isActive()) { X.yield(); }
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
