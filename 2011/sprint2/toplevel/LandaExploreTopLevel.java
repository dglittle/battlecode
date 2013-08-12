package sprint2.toplevel;

import sprint2.blocks.BuildRecyclerStrategyBlock;
import sprint2.blocks.NavigationBlock;
import sprint2.blocks.SurveyNavigationBlock;
import sprint2.core.M;
import sprint2.core.S;
import sprint2.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

/**
 * Based on VExploreTopLevel.
 */
public class LandaExploreTopLevel {
  public static boolean run() {
    // Phase 1: go away.
    MapLocation startLocation = S.location;
    Direction targetDirection = M.nearestEdgeDirection().opposite();
    SurveyNavigationBlock.setNavigationParameters(targetDirection, 5, 250);

    System.out.println("in ExploreTopLevel - " + startLocation + " -> "
        + targetDirection);
    M.debug_PrintMap();

    while (true) {
      if (S.location.distanceSquaredTo(startLocation) >= 400) {
        return false;
      }

      explore(targetDirection); // V old
      // if (SurveyNavigationBlock.tryNavigating()) continue;
      // If we don't have a goal, then we need to find a mine.
      try {
        boolean foundMine;
        foundMine = BuildRecyclerStrategyBlock.findMine();
        if (foundMine == true) {
          return true;
        }
      } catch (GameActionException e) {
        e.printStackTrace();
      }
      X.yield();
      return true;
    }
  }

  public static MapLocation currentTarget = S.location;
  public static Direction oldTargetDirection = Direction.OMNI;

  public static void explore(Direction targetDirection) {
    if (NavigationBlock.bug == null)
      NavigationBlock.init();
    currentTarget = S.location.add(targetDirection, 30);
    if (!oldTargetDirection.equals(targetDirection)
        || NavigationBlock.goal == null
        || NavigationBlock.goal.distanceSquaredTo(S.location) < 4)
      NavigationBlock.goal = S.location.add(targetDirection, 30);
    S.rc.setIndicatorString(2, "Trying to navigate in " + targetDirection
        + " to " + currentTarget + ".");
    try {
      NavigationBlock.try_moveInDirection();
    } catch (GameActionException e) {
      e.printStackTrace();
    }
    oldTargetDirection = targetDirection;
  }
}
