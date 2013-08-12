package heavy1.chaos.landa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import heavy1.blocks.pathfinding.BugPathfindingBlock;
import heavy1.chaos.Strategy;
import heavy1.core.B;
import heavy1.core.CommandType;
import heavy1.core.D;
import heavy1.core.S;
import heavy1.core.X;
import battlecode.common.Chassis;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class ManipleSargeantStrategyBlock implements Strategy {

  public static enum StrategyState { IDLE, EQUIPPED, RECRUITING, INVADING };
  
  // --- Methods ------------------------------------------------------------------

  /**
   * Check the conditions and perform any required state changes.
   * 
   * @throws GameActionException
   */
  public final void stateCheckConditions() throws GameActionException {
    switch (strategyState) {
    case IDLE:
      // If we have an antenna, a radar, and two plating, we're equipped.
      boolean haveAntenna = B.bc != null;
      boolean haveRadar = S.sensorController != null;
      boolean havePlating = S.hp == 18.0;
      if (haveAntenna && haveRadar && havePlating) {
        stateTransition(StrategyState.EQUIPPED);
      }
      break;
    case EQUIPPED:
      if (squadMembers.size() < 4) {
        stateTransition(StrategyState.RECRUITING);
      }
    case RECRUITING:
      if (squadMembers.size() >= 4) {
        stateTransition(StrategyState.INVADING);
      }
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
    case IDLE:
      break;
    }
  }

  public final void step() throws GameActionException {
    setIndicatorStrings();
    stateCheckConditions();
    switch (strategyState) {
    case IDLE:
      X.yield();
      break;
    case EQUIPPED:
      X.yield();
      break;
    case RECRUITING:
      setIndicatorStrings();
      RobotInfo[] nearbyRobots = S.nearbyRobotInfos();
      if (S.round % 3 != 0) {
        // Recruit twice out of every three rounds.
        D.debug_pl("recruit_two", "Let's see who wants to join the squad. There are " + nearbyRobots.length + " robots around.");
        for (RobotInfo robotInfo : nearbyRobots) {
          if (!robotInfo.chassis.equals(Chassis.LIGHT))
            continue;
          RobotInfo potentialRecruit = robotInfoInCollection(robotInfo, potentialRecruits);
          RobotInfo robotInSquad = robotInfoInCollection(robotInfo, squadMembers);
          if (potentialRecruit != null && robotInSquad == null && potentialRecruit.direction.equals(robotInfo.direction.opposite())) {
            D.debug_pl("recruit_two", "Adding " + robotInfo.robot.getID() + " to squad and removing from potential recruits.");
            squadMembers.add(robotInfo);
            potentialRecruits.remove(robotInfo);
          }
          else {
            D.debug_pl("recruit_two", "Couldn't recruit robot " + robotInfo.robot.getID() + ". Potential recruit: " + (potentialRecruit != null) + "; not in squad: " + (robotInSquad == null) + "; facing direction " + robotInfo.direction + " opposite of " + (potentialRecruit != null ? potentialRecruit.direction : "null"));
          }
        }
      }
      else {
        // Check for potential recruits once every three rounds.
        for (RobotInfo robotInfo : nearbyRobots) {
          boolean isPotentialRecruit = robotInfoInCollection(robotInfo, potentialRecruits) != null;
          boolean isSquadMember = robotInfoInCollection(robotInfo, squadMembers) != null;
          D.debug_pl("recruit_one", "Can we recruit Robot " + robotInfo.robot.getID() + "?");
          if (!isPotentialRecruit && !isSquadMember && (robotInfo.direction.equals(Direction.NORTH_EAST) || robotInfo.direction.equals(Direction.SOUTH_WEST))) {
            D.debug_pl("recruit_one", "Yes.");
            potentialRecruits.add(robotInfo);
          }
          else {
            D.debug_pl("recruit_one", "No. " + !isPotentialRecruit + " " + !isSquadMember + " " + (robotInfo.direction.equals(Direction.NORTH_EAST) || robotInfo.direction.equals(Direction.SOUTH_WEST)));
          }
        }
        D.debug_pl("recruit_one", "Saw all nearby robots...");
        if (!S.movementController.isActive()) {
          X.setDirection(S.direction.rotateLeft());
          X.yield();
        }
      }
      X.yield();
      break;
    case INVADING:
      try {
        if (S.round - lastRadioUpdateRound > RADIO_UPDATE_ROUND_INTERVAL)
          xmitSquadUpdate();
        // xmitTargetRobots();
      }
      catch (Exception e) {
        D.debug_logException(e);
      }
      moveTowardsEnemyTerritory();
      X.yield();
      break;
    }
    setIndicatorStrings();
  }

  public final void init() throws GameActionException {
    strategyState = StrategyState.IDLE;
  }

  // --- Utilities --------------------------------------------------------

  /**
   * Checks if a RobotInfo is in a collection and returns it if it is, or null
   * if it isn't.
   * 
   * @param robotInfo the robot we want to find in a collection
   * @param c the collection in which to find a robot
   * @return the RobotInfo if that robot is in c or null if it isn't
   */
  public static final RobotInfo robotInfoInCollection(RobotInfo robotInfo, Collection<RobotInfo> c) {
    int id = robotInfo.robot.getID();
    for (RobotInfo ri : c)
      if (ri.robot.getID() == id)
        return ri;
    return null;
  }

  public static final void getMessages() {
//    B.addOnMessageHandler(new Callback() {
//      @Override
//      public void onMessage(Message m) {
//
//      }
//    });
  }

  public static final void xmitSquadUpdate() throws Exception {
    int[] message;
    if (squadMembers.size() >= 4) {
      message = CommandType.MANIPLE_JOIN_SQUAD.ints;
      message[2] = S.id;
      for (int i = 3; i <= 6; i++) {
        message[i] = squadMembers.get(i - 3).robot.getID();
      }
    }
    else {
      message = CommandType.MANIPLE_DESTINATION.ints;
      message[2] = S.id;
      message[3] = distantLocation.x;
      message[4] = distantLocation.y;
    }
    B.send(message);
    lastRadioUpdateRound = S.round;
  }
  public static final void xmitTargetRobots() throws Exception {
//    RobotInfo[] attackTargets = getAttackTargets(S.nearbyRobotInfos());
    int[] message = CommandType.MANIPLE_NEARBY_ENEMIES.ints;
    // FIXME(landa): implement
    B.send(message);
    lastRadioUpdateRound = S.round;
  }

  public static final void moveTowardsEnemyTerritory() throws GameActionException {
    if (distantLocation == null)
      distantLocation = S.location.add(Direction.SOUTH, 20);
    Direction nextDirection = BugPathfindingBlock.nextDirection(distantLocation);
    if (nextDirection == Direction.NONE) {
      distantLocation = S.location.add(Direction.NORTH, 20);
      nextDirection = BugPathfindingBlock.nextDirection(distantLocation);
    }
    if (S.movementController.canMove(nextDirection) && !S.movementController.isActive()) {
      X.moveTowardsSync(nextDirection);
    }
  }

  public static final RobotInfo[] getAttackTargets(RobotInfo[] robotInfos) throws GameActionException {
    if (robotInfos.length == 0)
      return null;
    List<RobotInfo> enemies = new ArrayList<RobotInfo>();
    for (RobotInfo robotInfo : robotInfos) {
      // Check if it's an enemy.
      if (!robotInfo.robot.getTeam().equals(S.enemyTeam)) {
        enemies.add(robotInfo);
      }
    }
    return (RobotInfo[]) enemies.toArray();
  }
  
  // ----------------------------------------------------------------------
  
  /**
   * Prints out state debug information.
   * 
   * @param prefix state specifier (e.g. old or new state)
   */
  public static void checkRep(String prefix) {
    // Check assertions.

    // Output the state.
    String tag = prefix + "_state";
    D.debug_pl(tag, "----------------------------------------------------");
    D.debug_pl(tag, "Sgt. " + S.id + ", born " + S.birthRound + "; leading squad " + squadId);
    D.debug_pl(tag, "We're " + strategyState);

    setIndicatorStrings();
  }

  public static final void setIndicatorStrings() {
    // State
    S.rc.setIndicatorString(0, strategyState + " Sgt. " + S.id + ", born " + S.birthRound + "; leading squad " + squadId);
    // Broadcast
    S.rc.setIndicatorString(1, "[" + (S.round - lastRadioUpdateRound) + "] " + squadMembers.size() + " in squad #" + squadId);
    // Destination
    S.rc.setIndicatorString(2, distantLocation == null ? "NULL DISTANT LOCATION" : "Distant location offset: (" + (distantLocation.x - S.locationX) + ", " + (distantLocation.y - S.locationY) + ")");
  }

  public ManipleSargeantStrategyBlock() throws GameActionException {
    init();
  }
  
  // ----------------------------------------------------------------------
  
  // --- Utility variables --------------------------------------------------------
  public static MapLocation distantLocation;
  public static List<RobotInfo> potentialRecruits = new ArrayList<RobotInfo>();

  // --- Constants ----------------------------------------------------------------
  public static final int RADIO_UPDATE_ROUND_INTERVAL = 10;

  // --- State information --------------------------------------------------------
  public static int squadId = S.id;
  public static List<RobotInfo> squadMembers = new ArrayList<RobotInfo>();
  public static int lastRadioUpdateRound = S.birthRound;

  public static StrategyState strategyState = StrategyState.IDLE;

}
