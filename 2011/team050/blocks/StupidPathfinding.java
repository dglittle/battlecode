package team050.blocks;

import team050.core.S;
import team050.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class StupidPathfinding {
  
  public static boolean bugToAsync(Direction d) throws GameActionException {
    return bugToAsync(S.location.add(d, 100));
  }

  public static boolean bugUpToAsync(MapLocation goal) throws GameActionException {
    return bugToAsync(goal, 2);
  }

  public static boolean bugToAsync(MapLocation goal) throws GameActionException {
    return bugToAsync(goal, 0);
  }

  public static boolean bugToAsync(MapLocation goal, int toWithinRadiusSq) throws GameActionException {
    Direction d = nextDirection(goal, toWithinRadiusSq);
    if (d.ordinal() < 8) {
      X.moveTowardsAsync(d);
      return true;
    }
    return false;
  }

  public static Direction nextDirection(MapLocation goal, int toWithinRadiusSq) {
    // see if we made it
    int dist = S.location.distanceSquaredTo(goal);
    if (dist <= toWithinRadiusSq) {
      return Direction.NONE;
    }

    Direction d = S.location.directionTo(goal);

    if (S.movementController.canMove(d))
      return d;

    Direction left = d.rotateLeft();
    if (S.movementController.canMove(left))
      return left;

    Direction right = d.rotateRight();
    if (S.movementController.canMove(right))
      return right;

    // we might be able to get closer by turning further left
    left = left.rotateLeft();
    if (S.location.add(left).distanceSquaredTo(goal) < dist) {
      if (S.movementController.canMove(left))
        return left;
    }

    // we might be able to get closer by turning further right
    right = right.rotateRight();
    if (S.location.add(right).distanceSquaredTo(goal) < dist) {
      if (S.movementController.canMove(right))
        return right;
    }

    return Direction.OMNI;
  }
}
