package sprint2.blocks;

import sprint2.core.S;
import sprint2.core.X;
import sprint2.util.Utilities;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class NavigationBlock {
  public static BugPathfinding bug;
  public static MapLocation goal;

  public static final void init() {
    bug = new BugPathfinding();
    goal = S.rc.getLocation();
  }

  public static final Direction nextDirection() throws GameActionException {
    //Log.out("Bugging to " + goal.toString() + ". Have bug,rc,mc,goal=" + bug + "," + S.rc + "," + S.movementController + "," + goal);
    return bug.bugTo(S.rc, S.movementController, goal);
  }

  public static final void try_moveInDirection(Direction there)
      throws GameActionException {
    //Log.out("The direction to move in is " + there + ".");
    if (!there.equals(Direction.OMNI) && !there.equals(Direction.NONE)) {
      if (S.movementController.canMove(there) && !S.movementController.isActive()) {
        //Log.out("We can move there.");
        if (S.rc.getDirection().equals(there)) {
          X.moveForward();
          X.yield();
        } else if (S.rc.getDirection().equals(there.opposite())) {
          X.moveBackward();
          X.yield();
        } else {
          //Log.out("Setting the direction to there.");
          X.setDirection(there);
          X.yield();
        }
      }
      else {
        X.yield();
      }
    }
    else {
      X.yield();
    }
  }

  public static final void try_moveInDirection() throws GameActionException {
    try_moveInDirection(nextDirection());
  }

  public static final void setLocation(MapLocation there) {
    goal = there;
  }

  public static void try_moveToAdjacentEmpty() throws GameActionException {
    try_moveInDirection(Utilities.getRandomEmptyDirection());
  }

}
