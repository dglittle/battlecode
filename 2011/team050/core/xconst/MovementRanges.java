package team050.core.xconst;

import team050.core.S;

public final class MovementRanges {  
  /**
   * Initializes {@link MovementRanges#initJumpByDirection()}.
   * 
   * This is expensive, so we don't do it for all robots.
   */
  public static final void initJumpByDirection() {
    S.jumpDxByDirection = new int[][] {{0, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, 0, -1, 1}, {2, 3, 2, 1, 3, 0, 4, 1, 2, 0, 3, 1, 0, 2, 0, 1}, {4, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 1, 1, 1}, {2, 3, 2, 1, 3, 0, 4, 1, 2, 0, 3, 1, 0, 2, 0, 1}, {0, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, 0, -1, 1}, {-3, -2, -2, -3, -1, -4, 0, -2, -1, -3, 0, -1, -2, 0, -1, 0}, {-4, -3, -3, -3, -3, -3, -2, -2, -2, -2, -2, -1, -1, -1}, {-3, -2, -2, -3, -1, -4, 0, -2, -1, -3, 0, -1, -2, 0, -1, 0}};
    S.jumpDyByDirection = new int[][] {{-4, -3, -3, -3, -3, -3, -2, -2, -2, -2, -2, -1, -1, -1}, {-3, -2, -2, -3, -1, -4, 0, -2, -1, -3, 0, -1, -2, 0, -1, 0}, {0, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, 0, -1, 1}, {3, 2, 2, 3, 1, 4, 0, 2, 1, 3, 0, 1, 2, 0, 1, 0}, {4, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 1, 1, 1}, {2, 3, 2, 1, 3, 0, 4, 1, 2, 0, 3, 1, 0, 2, 0, 1}, {0, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, 0, -1, 1}, {-2, -3, -2, -1, -3, 0, -4, -1, -2, 0, -3, -1, 0, -2, 0, -1}};
  } 
}
