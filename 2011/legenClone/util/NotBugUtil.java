package legenClone.util;

import legenClone.core.S;
import legenClone.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.MapLocation;

public class NotBugUtil {

  public static Direction bugUpToAsync(MapLocation goal) {
    return bugToAsync(goal, 2);
  }

  public static Direction bugToAsync(MapLocation goal) {
    return bugToAsync(goal, 0);
  }

  public static Direction bugToAsync(MapLocation goal, int toWithinRadiusSq) {
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

  public static Direction bugToAsync(Direction d) {
    if (S.movementController.canMove(d))
      return d;

    Direction left = d.rotateLeft();
    if (S.movementController.canMove(left))
      return left;

    Direction right = d.rotateRight();
    if (S.movementController.canMove(right))
      return right;

    return Direction.OMNI;
  }
}
