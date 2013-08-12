package legenClone.blocks;

import legenClone.core.M;
import legenClone.core.S;
import legenClone.core.X;
import legenClone.util.Log;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.Robot;
import battlecode.common.RobotLevel;

public class BuildRecyclerStrategyBlock implements Strategy {
  public static final int EXPLORATION_ROUND = 100;
  public static MapLocation goal;
  public static Mine targetMine;
  public static boolean foundMine;
  public static BugPathfinding nav;
  public static Direction nextDirection = Direction.NONE;
  int dummy = 0;

  /**
   * Strategy state machine states.
   */
  public static enum StrategyState {
    FIND_GOAL, NAVIGATE_TO_GOAL, BUILD_BUILDING, BUILD_RECYCLER, EXPLORE
  };

  public static StrategyState strategyState = StrategyState.FIND_GOAL;

  /**
   * Check the conditions and perform any required state changes.
   * 
   * @throws GameActionException
   */
  public final void stateCheckConditions() throws GameActionException {
    switch (strategyState) {
      case EXPLORE:
        // If we don't have a goal, then we need to find a mine.
        foundMine = findMine();
        if (foundMine == false) {
          stateTransition(StrategyState.EXPLORE);
        } else {
          stateTransition(StrategyState.NAVIGATE_TO_GOAL);
        }
        break;
      case FIND_GOAL:
        // If we don't have a goal, then we need to find a mine.
        for (int i = 0; i < 7; i++) {
          foundMine = findMine();
          if (foundMine == true) {
            stateTransition(StrategyState.NAVIGATE_TO_GOAL);
            return;
          } else {
            if (!S.movementController.isActive()) {
              X.setDirection(S.direction.rotateRight());
            }
            else {
              Log.out("Stuck here?");
              i--;
              X.yield();
            }
          }
        }
        stateTransition(StrategyState.EXPLORE);
        break;
    }
  }

  /**
   * Perform a state transition to state.
   * 
   * @param state
   */
  public final void stateTransition(StrategyState state) {
    strategyState = state;
    switch (state) {
      case FIND_GOAL:
      case NAVIGATE_TO_GOAL:
      case BUILD_BUILDING:
      case BUILD_RECYCLER:
      case EXPLORE:
        break;
    }
  }

  public final void step() throws GameActionException {
    stateCheckConditions();
    switch (strategyState) {
      case FIND_GOAL:
        goal = null;
        break;
      case NAVIGATE_TO_GOAL:
        // Check if we're already sitting on it.
        if (S.location.equals(goal)) {
          NavigationBlock.try_moveToAdjacentEmpty();
          System.out.println("We're sitting on the mine.");
        } else {
          if (S.location.distanceSquaredTo(goal) > 2) {
            nextDirection = nav.bugTo(goal);
            if (nextDirection.ordinal() < 8 && S.movementController.canMove(nextDirection) && !S.movementController.isActive()) {
              if (!X.tryMove(nextDirection))
                X.yield();
            }
            else {
              X.yield();
            }
          } else {
            if (S.allowedToBuild(Chassis.BUILDING, ComponentType.RECYCLER))
              stateTransition(StrategyState.BUILD_BUILDING);
            else {
              X.yield();
              stateTransition(StrategyState.FIND_GOAL);
            }
            step();
          }
        }
        break;
      case BUILD_BUILDING:
        try_build(Chassis.BUILDING, goal);
        break;
      case BUILD_RECYCLER:
        try_equip(ComponentType.RECYCLER, goal, RobotLevel.ON_GROUND);
        break;
      case EXPLORE:
        SurveyNavigationBlock.tryNavigating();
        X.yield();
        break;
    }
  }

  public final void try_build(Chassis chassis, MapLocation goal)
      throws GameActionException {
    Log.out("Trying to build... location = " + S.location);
    if (!S.builderController.isActive()) {
      try {
        Log.out("Not active, ready to build...");
        S.builderController.build(chassis, goal);
      }
      catch (Exception e) {
        Log.out("Exception " + e.getMessage() + "! Starting to spin...");
        stateTransition(StrategyState.FIND_GOAL);
        return;
      }
      Log.out("Success! Going to build a recycler...");
      stateTransition(StrategyState.BUILD_RECYCLER);
      X.yield();
    } else {
      X.yield();
    }
  }

  public final void try_equip(ComponentType component, MapLocation goal,
      RobotLevel level) throws GameActionException {
    if (S.flux >= component.cost && !S.builderController.isActive()) {
      // Log.out("We're at " + S.rc.getLocation().toString()
      // + " ready to equip at " + goal.toString() + ".");
      S.builderController.build(component, goal, level);
      stateTransition(StrategyState.FIND_GOAL);
      X.yield();
    } else {
      X.yield();
    }
  }

  public static final boolean mineOccupied(Mine mine)
      throws GameActionException {
    GameObject objectOnMine = S.sensorController.senseObjectAtLocation(
        mine.getLocation(), RobotLevel.ON_GROUND);
    if (objectOnMine == null)
      return false;
    if (objectOnMine instanceof Robot) {
      // Some robot is sitting on top of the mine. If it's it's us, the mine is
      // not occupied.
      // Log.out("Sensed object with ID=" + ((Robot) objectOnMine).getID() +
      // " (my id=" + S.id + ").");
//      Robot robotOnMine = (Robot) objectOnMine;
//      RobotInfo robotInfoOnMine = S.sensorController.senseRobotInfo(robotOnMine);
      if (((Robot) objectOnMine).getID() == S.id) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return true if found a mine nearby, false if not
   * @throws GameActionException
   */
  public static final boolean findMine() throws GameActionException {
    if (S.birthRound != 0 && S.round < EXPLORATION_ROUND) return false;
    Mine[] mines = S.sensorController.senseNearbyGameObjects(Mine.class);
    // S.rc.setIndicatorString(2, "Number of nearby mines: " + mines.length +
    // "; state=" + strategyState);
    if (mines.length == 0) { // There are no mines nearby
      return false;
    } else {
      // Log.out("There are " + mines.length + " mines nearby.");
      for (Mine mine : mines) {
        // Log.out("Mine: " + mine.toString() + " "
        // + mine.getLocation().toString());
        if (!mineOccupied(mine)) {
          // Log.out("Found an unoccupied mine at " + mine.getLocation() + ".");
          targetMine = mine;
          goal = mine.getLocation();
          // Log.out("Our target is " + mine.toString() + " at " +
          // goal.toString());
          return true;
        }
      }
    }
    return false;
  }

  public final void init() throws GameActionException {
    strategyState = StrategyState.FIND_GOAL;
    SurveyNavigationBlock.setNavigationParameters(
        M.nearestEdgeDirection().opposite(), 16, 440);
    nav = new BugPathfinding();
  }

}
