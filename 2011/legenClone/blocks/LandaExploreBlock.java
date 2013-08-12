package legenClone.blocks;

import legenClone.core.M;
import legenClone.core.S;
import legenClone.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

/**
 * Based on VExploreTopLevel.
 */
public class LandaExploreBlock {
  public static void step() {
    // Phase 1: go away.
    MapLocation startLocation = S.location;
    targetDirection = M.nearestEdgeDirection().opposite();

    System.out.println("in ExploreTopLevel - " + startLocation + " -> "
        + targetDirection);

    if (S.location.distanceSquaredTo(startLocation) >= 400) {
      return;
    }

    explore(targetDirection); // V old
    // if (SurveyNavigationBlock.tryNavigating()) continue;
    // If we don't have a goal, then we need to find a mine.
    try {
      boolean foundMine;
      foundMine = BuildRecyclerStrategyBlock.findMine();
      if (foundMine == true) {
        BuildRecyclerStrategyBlock.strategyState = BuildRecyclerStrategyBlock.StrategyState.FIND_GOAL;
      }
    } catch (GameActionException e) {
      e.printStackTrace();
    }
    X.yield();
    return;
  }

  public static MapLocation currentTarget = S.location;
  public static Direction oldTargetDirection = Direction.OMNI;
  public static Direction targetDirection = Direction.OMNI;

  public static void explore(Direction targetDirection) {
    if (NavigationBlock.bug == null)
      NavigationBlock.init();
    currentTarget = S.location.add(targetDirection, 30);
    //Log.out(new Boolean(!oldTargetDirection.equals(targetDirection)).toString());
    //Log.out(new Boolean(NavigationBlock.goal == null).toString());
    //Log.out(new Boolean(NavigationBlock.goal.distanceSquaredTo(S.location) < 4).toString());
    if (
        !oldTargetDirection.equals(targetDirection) ||
        NavigationBlock.goal == null ||
        NavigationBlock.goal.distanceSquaredTo(S.location) < 4)
//      if (Utilities.rdm.nextDouble() < 0.3) targetDirection = Utilities.randomCompassDirection();
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
