package team050.blocks;

import team050.blocks.brain.BrainState;
import team050.blocks.pathfinding.BugPathfindingBlock;
import team050.core.D;
import team050.core.S;
import team050.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class FleeBlock {

  public static final int DISTANCE_AWAY_FROM_BRAIN =  25;
  /**
   * Moves towards the flee direction. The flee direction is nearest brain 
   * direction or the opposite of enemy direction if brain direction is unknown.
   * @return true is a movement action is performed.
   */
  public static final boolean async() {
    final MapLocation brainLocation = BrainState.closestBrain();
    if (brainLocation != null && 
        S.location.distanceSquaredTo(brainLocation) < DISTANCE_AWAY_FROM_BRAIN) {
      try {
        if (S.jumpControllers != null) {
          // can we jump closer?
          boolean jumped = false;
          if (S.jumpReady()) {
            final MapLocation loc = 
              JumpUtil.closestPotentialJumpLocationTo_butNotOnIt(brainLocation);
            if (loc != null) { jumped = X.jump(loc); }
          }
          if (!jumped) { BugPathfindingBlock.bugTowardAsync(brainLocation, 5); }
          return true;
        } else {
          final Direction d = BugPathfindingBlock.nextDirection(brainLocation, 5);
          if (d.ordinal() < 8) { 
            X.moveTowardsAsync(d);
            return true;
          }
        }
      } catch (GameActionException gae) {
        D.debug_logException(gae);
      }
    } else {
      final Direction enemyDirection = EnemyUpdatingBlock.enemyDirection();
      Direction fleeDirection;
      if (enemyDirection == null || enemyDirection.ordinal() >= 8) 
        fleeDirection = S.direction.rotateLeft().rotateLeft(); 
      else fleeDirection = enemyDirection.opposite();
      try {
        boolean jumped = false;
        if (S.jumpControllers != null) {
          // can we jump closer?
          if (S.jumpReady()) 
            jumped = JumpUtil.jump(fleeDirection);
        }
        if (!jumped)
          return X.moveTowardsAsync(fleeDirection);
      } catch (GameActionException e) {
        D.debug_logException(e);
      }
    }
    return false;
  }
}
