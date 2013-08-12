package sprint3.blocks;

import sprint3.core.D;
import sprint3.core.S;
import sprint3.core.X;
import sprint3.core.xconst.XDirection;
import battlecode.common.Direction;
import battlecode.common.GameActionException;

/**
 * Moves in a random direction.
 */
public final class RandomMovementBlock {
  /** Returns after having moved in a random direction. */
  public static final void sync() {    
    asyncSetup();
    while (!done) {
      async();
      X.yield();
    }
  }
  
  /** Initializes the block for asynchronous execution. */
  public static final void asyncSetup() {
    _target = null;
    done = false;
  }

  /**
   * Returns after attempting to make progress.
   * @return true if a movement action happened, false otherwise
   */
  public static final boolean async() {
    if (S.movementController.isActive()) {
      return false;
    }
    
    if (_target == null) {
      _target = randomAvailableDirection();
      if (_target == null) {
        return false;  // Nowhere to go.
      }
      _opposingTarget = _target.opposite();
    }
        
    try {
      if (S.direction == _target) {
        X.moveForward();
        done = true;
        return true;
      }
      if (S.direction == _opposingTarget) {
        X.moveBackward();
        done = true;
        return true;
      }
      X.setDirection(_target);
      return true;
    }
    catch (GameActionException e) {
      D.debug_logException(e);  // Lost the round, and someone got in the way.
      _target = null;
      return false;
    }
  }
  
  /**  
   * {@link Direction} in which it should be safe to move.
   * 
   * @return Direction to an empty adjacent square, or null if we're surrounded
   */
  public static final Direction randomAvailableDirection() {
    for (int i = 0; i < 30; i++) {
      Direction direction = 
        XDirection.intToDirection[S.rand.nextInt(XDirection.ADJACENT_DIRECTIONS)];
      if (S.movementController.canMove(direction)) {
        return direction;
      }
    }
    return null;
  }
  
  /** Set to true after a movement is completed. */
  public static boolean done;
  
  /** The direction in which we want to move. */
  public static Direction _target;
  /** The opposite of the direction in which we want to move. */
  public static Direction _opposingTarget;  
}
