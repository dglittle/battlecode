package team050.blocks;

import team050.core.S;
import team050.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.JumpController;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.TerrainTile;

public class JumpUtil {

  public static final MapLocation furthestDefiniteJumpLocationWithinRangeOf(
      MapLocation goal, int range) throws GameActionException {
    MapLocation from = S.location;
    Direction direction = S.direction;
    int bestDist = 0;
    MapLocation bestLoc = null;

    final int[] jumpDx = S.jumpDxByDirection[direction.ordinal()];
    final int[] jumpDy = S.jumpDyByDirection[direction.ordinal()];
    for (int i = jumpDx.length - 1; i >= 0; i--) {
      MapLocation possibleJumpLocation = from.add(jumpDx[i], jumpDy[i]);
      int dist = possibleJumpLocation.distanceSquaredTo(goal);
      if (definitelyJumpable(possibleJumpLocation) && dist <= range
          && dist > bestDist) {
        bestDist = dist;
        bestLoc = possibleJumpLocation;
      }
    }

    return bestLoc;
  }

  public static final boolean canJumpNextTo(MapLocation goal)
      throws GameActionException {
    MapLocation cursor = S.location;
    while (true) {
      if (cursor.isAdjacentTo(goal))
        return true;
      cursor = closestDefiniteJumpLocationTo_butNotOnIt(cursor, goal);
      if (cursor == null)
        return false;
    }
  }

  public static final MapLocation closestDefiniteJumpLocationTo_butNotOnIt(
      MapLocation from, MapLocation goal) throws GameActionException {
    Direction direction = from.directionTo(goal);
    int bestDist = from.distanceSquaredTo(goal);
    MapLocation bestLoc = null;

    final int[] jumpDx = S.jumpDxByDirection[direction.ordinal()];
    final int[] jumpDy = S.jumpDyByDirection[direction.ordinal()];
    for (int i = 0; i < jumpDx.length; i++) {
      MapLocation possibleJumpLocation = from.add(jumpDx[i], jumpDy[i]);
      if (!goal.equals(possibleJumpLocation)
          && definitelyJumpable(possibleJumpLocation)) {
        int dist = possibleJumpLocation.distanceSquaredTo(goal);
        if (dist < bestDist) {
          bestDist = dist;
          bestLoc = possibleJumpLocation;
        }
      }
    }

    return bestLoc;
  }

  public static final MapLocation closestPotentialJumpLocationTo(
      MapLocation goal) throws GameActionException {
    Direction direction = S.location.directionTo(goal);
    int bestDist = S.location.distanceSquaredTo(goal);
    MapLocation bestLoc = null;

    final int[] jumpDx = S.jumpDxByDirection[direction.ordinal()];
    final int[] jumpDy = S.jumpDyByDirection[direction.ordinal()];
    for (int i = 0; i < jumpDx.length; i++) {
      MapLocation possibleJumpLocation = S.location.add(jumpDx[i], jumpDy[i]);
      if (mightBeJumpable(possibleJumpLocation)) {
        int dist = possibleJumpLocation.distanceSquaredTo(goal);
        if (dist < bestDist) {
          bestDist = dist;
          bestLoc = possibleJumpLocation;
        }
      }
    }

    return bestLoc;
  }

  public static final MapLocation closestPotentialJumpLocationTo_butNotOnIt(
      MapLocation goal) throws GameActionException {
    Direction direction = S.location.directionTo(goal);
    int bestDist = S.location.distanceSquaredTo(goal);
    MapLocation bestLoc = null;

    final int[] jumpDx = S.jumpDxByDirection[direction.ordinal()];
    final int[] jumpDy = S.jumpDyByDirection[direction.ordinal()];
    for (int i = 0; i < jumpDx.length; i++) {
      MapLocation possibleJumpLocation = S.location.add(jumpDx[i], jumpDy[i]);
      if (!goal.equals(possibleJumpLocation)
          && mightBeJumpable(possibleJumpLocation)) {
        int dist = possibleJumpLocation.distanceSquaredTo(goal);
        if (dist < bestDist) {
          bestDist = dist;
          bestLoc = possibleJumpLocation;
        }
      }
    }

    return bestLoc;
  }

  public static final boolean jump(Direction direction)
      throws GameActionException {
    if (direction.ordinal() >= 8)
      return false;
    for (JumpController jc : S.jumpControllers) {
      if (!jc.isActive()) {
        final int[] jumpDx = S.jumpDxByDirection[direction.ordinal()];
        final int[] jumpDy = S.jumpDyByDirection[direction.ordinal()];
        for (int i = 0; i < jumpDx.length; i++) {
          MapLocation possibleJumpLocation = S.location.add(jumpDx[i],
              jumpDy[i]);
          if (mightBeJumpable(possibleJumpLocation)) {
            // let's try to jump
            // but if we can't see where we're jumping,
            // we might fail.. so.. tryyyy to jump
            try {
              X.jump(possibleJumpLocation);
              return true;
            } catch (Exception e) {
              return false;
            }
          }
        }
        return false;
      }
    }
    return false;
  }

  public static final MapLocation jumpAndReturnLocation(Direction direction)
      throws GameActionException {
    for (JumpController jc : S.jumpControllers) {
      if (!jc.isActive()) {
        final int[] jumpDx = S.jumpDxByDirection[direction.ordinal()];
        final int[] jumpDy = S.jumpDyByDirection[direction.ordinal()];
        for (int i = 0; i < jumpDx.length; i++) {
          MapLocation possibleJumpLocation = S.location.add(jumpDx[i],
              jumpDy[i]);
          if (mightBeJumpable(possibleJumpLocation)) {
            // let's try to jump
            // but if we can't see where we're jumping,
            // we might fail.. so.. tryyyy to jump
            try {
              X.jump(possibleJumpLocation);
              return possibleJumpLocation;
            } catch (Exception e) {
              return null;
            }
          }
        }
        return null;
      }
    }
    return null;
  }

  public static final boolean mightBeJumpable(MapLocation m)
      throws GameActionException {
    // don't jump into voids or off the map
    TerrainTile tt = S.rc.senseTerrainTile(m);
    if (tt != null && !tt.isTraversableAtHeight(S.level))
      return false;

    // don't jump into other robots
    if (S.sensorController.canSenseSquare(m)) {
      Robot r = S.senseRobot(m, S.level);
      if (r != null)
        return false;
    }

    return true;
  }

  public static final boolean definitelyJumpable(MapLocation m)
      throws GameActionException {
    // don't jump into voids or off the map
    TerrainTile tt = S.rc.senseTerrainTile(m);
    if (tt == null || !tt.isTraversableAtHeight(S.level))
      return false;

    // don't jump into other robots
    if (S.sensorController.canSenseSquare(m)) {
      Robot r = S.senseRobot(m, S.level);
      if (r == null)
        return true;
    }

    return false;
  }
}
