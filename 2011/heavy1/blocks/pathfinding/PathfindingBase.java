package heavy1.blocks.pathfinding;

import heavy1.core.D;
import heavy1.core.M;
import heavy1.core.S;
import heavy1.core.X;
import heavy1.core.xconst.XDirection;
import battlecode.common.Direction;
import battlecode.common.GameActionException;


/**
 * Interface between cost-based path-finding algorithms and control logic.
 *
 * The control logic is responsible for initializing 
 */
public final class PathfindingBase {
  /**
   * Resets the navigation interface to reflect a new target.
   */
  public static final void reset() {
    solution = null;
    foundPathLastIteration = false;
    completedIteration = false;
  }
  
  /**
   * Returns the best direction to go to, if possible.
   * 
   * Should only be called after computation has completed.
   * 
   * @param bestDirection the ideal direction to move towards (e.g. the
   *                      direction we're facing now)
   * @param bytecodeMargin give up computing when there are these many bytecodes left in the round;
   *        if 0, the computation will run synchronously (only recommended for tests)
   * @return the direction to move towards; Direction.NONE means the goal is
   *         impossible to reach
   */
  public static final Direction nextDirection(Direction bestDirection) {
    // Refresh map location.
    final int sourceMX = S.locationX - M.arrayBaseX;
    final int sourceMY = S.locationY - M.arrayBaseY;
  
    Direction blockedDirection = null;
    int minCost = solution[sourceMX][sourceMY] + 1;
    int directionInt = bestDirection.ordinal();
    // TODO(pwnall): left/right sweep instead of clockwise sweep for direction
    for (int i = XDirection.ADJACENT_DIRECTIONS; i > 0; i--) {
      int mx = sourceMX + XDirection.intToDeltaX[directionInt];
      int my = sourceMY + XDirection.intToDeltaY[directionInt];
      int cost = solution[mx][my] +
          XDirection.pathfindingCost[directionInt];
      if (cost < minCost && cost > COST_OBSTACLE_THRESHOLD &&
          (!M.known[mx][my] || M.passable[mx][my])) {
        Direction direction = XDirection.intToDirection[directionInt];
        if (S.movementController.canMove(direction)) {
          return direction;
        }
        else {
          blockedDirection = direction;
        }
      }
      directionInt = (directionInt + 1) % 8;
    }
    /*
    if (S.round >= 460) {
      directionInt = bestDirection.ordinal();
      for (int i = XDirection.ADJACENT_DIRECTIONS; i > 0; i--) {
        int mx = sourceMX + XDirection.intToDeltaX[directionInt];
        int my = sourceMY + XDirection.intToDeltaY[directionInt];
        int cost = _done_minCost[mx][my] +
            XDirection.pathfindingCost[directionInt];
        if (S.round >= 460) {
          D.debug_pv("Direction " + XDirection.intToDirection[directionInt] +
              " cost: " + cost + " base: " + _done_minCost[mx][my]);
        }
        directionInt = (directionInt + 1) % 8;
      }
      
    }
    */
    // D.debug_pv("Heading to " + minDirection + " cost " +
    //     (minCost - COST_FLOOR) + " from " +
    //     (_done_minCost[_sourceMX][_sourceMY] - COST_FLOOR));      

    if (blockedDirection != null) {
      // We should have been able to move here. See what's wrong.
      if (S.direction != blockedDirection && !S.movementController.isActive()) {
        try {
          X.setDirection(blockedDirection);
        } catch(GameActionException e) {
          D.debug_logException(e);
        }
      }
    }
    return null;
  }

  /** Prints the current solution to the console. */
  public static final void debug_printSolution() {
    if (solution == null) {
      System.out.println("debug_printSolution: solution is null");
      return;
    }
    
    for (int y = M.mapMinY - 1; y <= M.mapMaxY + 1; y++) {
      StringBuffer lineBuffer = new StringBuffer();
      final int my = y - M.arrayBaseY;
      for (int x = M.mapMinX - 1; x <= M.mapMaxX + 1; x++) {
        final int mx = x - M.arrayBaseX;
        final int cost = solution[mx][my];
        final boolean valid = (cost > COST_OBSTACLE_THRESHOLD && cost < 0);
        
        if (cost == COST_FLOOR) {
          lineBuffer.append('<');
          continue;
        }
          
        if (!M.known[mx][my]) {
          lineBuffer.append(valid ? '?' : ' ');
        } else if(!M.land[mx][my]) {
          lineBuffer.append(valid ? '#' : 'X');
        } else if (!M.passable[mx][my]) {
          lineBuffer.append(valid ? '&' : '@');
        } else {
          if (valid) {
            // lineBuffer.append((char)('A' + (_minCost[mx][my] - COST_FLOOR)));
            lineBuffer.append('+');
          } else {
            lineBuffer.append('.');
          }
        }
      }
      System.out.println(lineBuffer.toString());
    }
  }
  
  /**
   * Minimum cost to get to a location multiplied by 2, plus -COST_FLOOR.
   * 
   * This is the completed version of the minCost map.
   */
  public static int[][] solution;
  /** True when the last finished iteration was successful. */
  public static boolean foundPathLastIteration;
  /** True right after a new solution is put into the solution member. */
  public static boolean completedIteration;

  /** The number used to represent a cost of 0. */
  public static final int COST_FLOOR = -5000;
  /**
   * The number used to shortcut checks for obstacles.
   * 
   * Should be smaller than {@link COST_FLOOR}.
   */
  public static final int COST_OBSTACLE = -6000;
  /** Decision boundary that clearly separates obstacles from valid places. */
  public static final int COST_OBSTACLE_THRESHOLD = -5500;
}
