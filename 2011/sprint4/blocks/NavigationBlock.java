package sprint4.blocks;

import sprint4.core.S;
import sprint4.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class NavigationBlock {
  public static MapLocation goal;

  public static final void init() {
  }

  public static final void moveInDirectionNow(Direction there) throws GameActionException {
    if (S.direction.equals(there)) {
      X.moveForward();
      X.yield();
    } else if (S.direction.equals(there.opposite())) {
      X.moveBackward();
      X.yield();
    } else {
      X.setDirection(there);
      X.yield();
    }
  }

  public static void sync_moveToAdjacentEmpty() throws GameActionException {
    // FIXME(landa): Why is this method taking ~5 rounds to complete?
    while (S.movementController.isActive()) {
      X.yield();
    }
    Direction nextDirection = RandomMovementBlock.randomAvailableDirection();
    moveInDirectionNow(nextDirection);
  }

}
