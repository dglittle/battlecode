package team050.chaos;

import team050.blocks.pathfinding.BugPathfindingBlock;
import team050.core.D;
import team050.core.M;
import team050.core.S;
import team050.core.X;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class BuildRecyclerStrategyBlock implements Strategy {
  // --- Utility variables --------------------------------------------------------
  
  // --- Constants ----------------------------------------------------------------
  public static final int ROUNDS_UNTIL_START_EXPLORING = 0;
  
  // --- State information --------------------------------------------------------
  public static Mine targetMine; public static MapLocation goal; public static boolean foundMine;
  public static Direction nextDirection = Direction.NONE;
  public static StrategyState strategyState = StrategyState.FIND_GOAL;

  public static enum StrategyState {
    IDLE, FIND_GOAL, NAVIGATE_TO_GOAL, BUILDER_ELECTION, BUILD_BUILDING, BUILD_RECYCLER, EXPLORE
  };
  
  /**
   * Prints out state debug information.
   * 
   * @param prefix state specifier (e.g. old or new state)
   */
  public static void checkRep(String prefix) {
    // Check assertions.
    D.debug_assert((goal != null) == foundMine, "Goal and foundMine don't match: goal is " + goal + " and foundMine is " + foundMine);
    
    // Output the state.
    String tag = prefix + "_state";
    D.debug_pl(tag, "----------------------------------------------------");
    D.debug_pl(tag, "We're " + S.chassis + " " + S.id + ", born on round " + S.birthRound + ".");
    D.debug_pl(tag, "We're in the " + strategyState + " state.");
    D.debug_pl(tag, "MINE: targeting mine " + targetMine + " located at " + (targetMine == null ? "NULL" : targetMine.getLocation()) + " and the goal is " + goal);

    setIndicatorStrings();
  }
  
  public static final void setIndicatorStrings() {
    S.rc.setIndicatorString(0, "We're in the " + strategyState + " state.");
    S.rc.setIndicatorString(1, "Goal dist: " + goal == null ? "NULL" : S.location.distanceSquaredTo(goal) + " " + goal == null ? "" : S.location.directionTo(goal) + "; allowed: " + S.allowedToBuildMine(Chassis.BUILDING, ComponentType.RECYCLER));
  }
  
  // --- Methods ------------------------------------------------------------------

  /**
   * Check the conditions and perform any required state changes.
   * 
   * @throws GameActionException
   */
  public static final void stateCheckConditions() throws GameActionException {
    switch (strategyState) {
    case EXPLORE:
      // If we don't have a goal, then we need to find a mine.
      setMine();
      if (foundMine == false) {
        stateTransition(StrategyState.EXPLORE);
      }
      else {
        stateTransition(StrategyState.NAVIGATE_TO_GOAL);
      }
      break;
    case FIND_GOAL:
      break;
    }
  }

  /**
   * Perform a state transition to state.
   * 
   * @param state
   */
  public static final void stateTransition(StrategyState state) {
//    if (state != strategyState) checkRep("changed_state");
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
    setIndicatorStrings();
    stateCheckConditions();
    switch (strategyState) {
    case FIND_GOAL:
      // If we don't have a goal, then we need to find a mine.
      for (int i = 0; i < 7; i++) {

        // Find a mine... if found, set a goal; otherwise set a null goal;
        setMine();

        // If we found a mine, navigate to a goal.
        if (foundMine == true) {
          D.debug_assert(goal != null, "Trying to navigate to a null goal.");
          stateTransition(StrategyState.NAVIGATE_TO_GOAL);
          return;
        }
        else {
          while (!S.motorReady) {
            X.yield();
          }
          X.setDirection(S.direction.rotateRight());
        }
      }
      stateTransition(StrategyState.EXPLORE);
      break;
    case NAVIGATE_TO_GOAL:
      D.debug_assert(goal != null, "in NAVIGATE_TO_GOAL and goal is null=(" + goal + ") (foundMine is " + foundMine + "); our location is " + S.location + ".");
      // Check if we're already sitting on it.
      if (S.location.equals(goal)) {
        while (!S.motorReady) {
          X.yield();
        }
        D.debug_pl("sync", "sync method called in round " + Clock.getRoundNum() + " location = " + S.location);
        LandaUtil.sync_moveToAdjacentEmpty();
        D.debug_pl("sync", "sync method exited in round " + Clock.getRoundNum() + " location = " + S.location);
//        stateTransition(StrategyState.BUILDER_ELECTION); // TODO(landa): Why is this being called right away if there's a sync function above this line?
      }
      else {
        if (S.location.distanceSquaredTo(goal) > 2) {
          nextDirection = BugPathfindingBlock.nextDirection(goal);
          if (nextDirection.ordinal() < 8 && S.movementController.canMove(nextDirection) && S.motorReady) 
            X.moveTowardsAsync(nextDirection);
          X.yield();
        }
        else {
          // We're adjacent to the build location.
          stateTransition(StrategyState.BUILDER_ELECTION);
          step();
        }
      }
      break;
    case BUILDER_ELECTION:
      while (!S.motorReady) {
        checkRep("election_movement_wait");
        X.yield();
      }
      D.debug_assert(S.location.directionTo(goal) != Direction.NONE && S.location.directionTo(goal) != Direction.OMNI, "Direction=" + S.location.directionTo(goal));
      X.setDirection(S.location.directionTo(goal));
      
      // TODO(landa): Test and reincorporate the commented code below.
      stateTransition(StrategyState.BUILD_BUILDING);
//      if (buildLeader()) {
//        D.debug_pl("build_leader", "I am the builder! --> BUILD_BUILDING.");
//        stateTransition(StrategyState.BUILD_BUILDING);
//      }
//      else {
//        D.debug_pl("build_leader", "I am NOT the builder --> EXPLORE.");
//        stateTransition(StrategyState.IDLE);
//      }
      break;
    case BUILD_BUILDING:
      if (S.allowedToBuildMine(Chassis.BUILDING, ComponentType.RECYCLER))
        try_build(Chassis.BUILDING, goal);
      else
        X.yield();
      break;
    case BUILD_RECYCLER:
      try_equip(ComponentType.RECYCLER, goal, RobotLevel.ON_GROUND);
      break;
    case EXPLORE:
      SurveyNavigationBlock.tryNavigating();
      break;
    }
    setIndicatorStrings();
  }

  public static final void try_build(Chassis chassis, MapLocation goal) throws GameActionException {
    if (!S.builderController.isActive()) {
      try {
        S.builderController.build(chassis, goal);
      }
      catch (Exception e) {
        stateTransition(StrategyState.FIND_GOAL);
        return;
      }
      stateTransition(StrategyState.BUILD_RECYCLER);
      X.yield();
    }
    else {
      X.yield();
    }
  }

  public final void try_equip(ComponentType component, MapLocation goal, RobotLevel level) throws GameActionException {
    if (S.flux >= component.cost && !S.builderController.isActive()) {
      S.builderController.build(component, goal, level);
      stateTransition(StrategyState.FIND_GOAL);
    }
    X.yield();
  }

  public final void init() throws GameActionException {
    strategyState = StrategyState.FIND_GOAL;
    SurveyNavigationBlock.setNavigationParameters(M.nearestEdgeDirection().opposite(), 16, 440);
  }

  // --- Utilities --------------------------------------------------------

  /**
   * Sets a mine as the targetMine and its location as the goal.
   * 
   * @return true if we set a mine; false otherwise
   * @throws GameActionException
   */
  public static final boolean setMine() throws GameActionException {
    targetMine = _findMine();
    foundMine = targetMine != null;
    if (foundMine) {
      goal = targetMine.getLocation();
    }
    else {
      goal = null;
    }
    return foundMine;
  }
  
  /**
   * Checks whether a mine is occupied.
   * 
   * @param mine the mine to check for availability
   * @return true if the mine is occupied; false otherwise
   * @throws GameActionException
   */
  public static final boolean mineOccupied(Mine mine) throws GameActionException {
    GameObject objectOnMine = S.sensorController.senseObjectAtLocation(mine.getLocation(), RobotLevel.ON_GROUND);
    if (objectOnMine == null)
      return false;
    if (objectOnMine instanceof Robot) {
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
  public static final Mine _findMine() throws GameActionException {
    if (S.birthRound != 0 && S.round < ROUNDS_UNTIL_START_EXPLORING)
      return null;
    Mine[] mines = S.sensorController.senseNearbyGameObjects(Mine.class);
    if (mines.length == 0) { // There are no mines nearby
      return null;
    }
    else {
      for (Mine mine : mines) {
        if (!mineOccupied(mine)) {
          return mine;
        }
      }
    }
    return null;
  }
  
  /**
   * Checks if we are the build leader.
   * 
   * @return this robot is the leader
   * @throws GameActionException
   */
  public static final boolean buildLeader() throws GameActionException {
    Robot[] robots = S.sensorController.senseNearbyGameObjects(Robot.class);
    if (robots.length == 0) { // There are no mines nearby
      return true;
    }
    else {
      // There are other robots nearby.
      RobotInfo ri = null;
      for (Robot robot : robots) {
        
        // If we can sense the robot, then we should get info on it.
        if (S.sensorController.canSenseObject(robot))
          ri = S.sensorController.senseRobotInfo(robot);
        else continue;
        
        if (ri == null) return true;
        
        // We have a robot in range...
        
        for (ComponentType component : ri.components) {
          if (component.equals(ComponentType.CONSTRUCTOR)) {
            // Okay, it's a constructor...
            // If it's in the leader zone, then we are not the leader.
            return !robotInLeaderZone(ri);
          }
          else continue;
        }
        
      }
    }
    return true;
  }
  
  /**
   * @param ri the robot to check for placement in the leader zone
   * @return true if the robot corresponding to the ri is in the leader zone; false otherwise
   */
  public static final boolean robotInLeaderZone(RobotInfo ri) {
    int x = S.locationX;
    int y = S.locationY;
    MapLocation robotLocation = ri.location;
    /*
     * If there's a constructor in an X, go away -- he's the leader:
     * 
     *    OXOXO
     *    XOXOX
     *    XXCXO
     *    XXXOX
     *    XXXXX
     */
    MapLocation[] leaderZone = new MapLocation[] {
      new MapLocation(x-2,y-1), new MapLocation(x-2,y), new MapLocation(x-2,y+1),
      new MapLocation(x-2,y+2), new MapLocation(x-1,y-2), new MapLocation(x-1,y), new MapLocation(x-1,y+1),
      new MapLocation(x-1,y+2), new MapLocation(x,y-1), new MapLocation(x,y+1),
      new MapLocation(x,y+2), new MapLocation(x+1,y-2), new MapLocation(x+1,y),
      new MapLocation(x+1,y+2), new MapLocation(x+2,y-1), new MapLocation(x+2,y+1),
      new MapLocation(x+2,y+2)
    };
    for (MapLocation loc : leaderZone) {
      if (robotLocation.equals(loc)) return true;
    }
    return false;
  }

}
