package sprint4.chaos;

import sprint4.blocks.pathfinding.BugPathfindingBlock;
import sprint4.core.B;
import sprint4.core.D;
import sprint4.core.S;
import sprint4.core.X;
import sprint4.core.xconst.XComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class ManipleLegionStrategyBlock implements Strategy {
  // --- Utility variables
  // --------------------------------------------------------

  // --- Constants
  // ----------------------------------------------------------------
  public static final int NO_RADIO_MAX_ROUNDS = Integer.MAX_VALUE;

  // --- State information
  // --------------------------------------------------------
  public static int squadId = 0;
  public static MapLocation sargeantLocation = null;
  public static RobotInfo[] radioEnemyInfo = new RobotInfo[0];
  public static int lastRadioUpdateRound = S.birthRound;

  public static StrategyState strategyState = StrategyState.RECRUIT;

  public static enum StrategyState {
    RECRUIT, EQUIPPED, SOLDIER
  };

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
    D.debug_pl(tag, "Legion " + S.id + ", born " + S.birthRound + "; squad " + squadId);
    D.debug_pl(tag, "We're a " + strategyState);

    setIndicatorStrings();
  }

  public static final void setIndicatorStrings() {
    S.rc.setIndicatorString(0, strategyState + " Legion " + S.id + ", born " + S.birthRound + "; squad " + squadId);
    S.rc.setIndicatorString(1, sargeantLocation == null ? "NO SARGEANT LOCATION" : S.location.directionTo(sargeantLocation) + " " + S.location.distanceSquaredTo(sargeantLocation));
    S.rc.setIndicatorString(2, sargeantLocation == null ? "NO SARGEANT LOCATION" : "Sargeant location offset: (" + (sargeantLocation.x - S.locationX) + ", " + (sargeantLocation.y - S.locationY) + ")");
  }

  public ManipleLegionStrategyBlock() throws GameActionException {
    init();
  }

  // --- Methods
  // ------------------------------------------------------------------

  /**
   * Check the conditions and perform any required state changes.
   * 
   * @throws GameActionException
   */
  public final void stateCheckConditions() throws GameActionException {
    switch (strategyState) {
    case RECRUIT:
      // If we have a weapon and a sensor, we're equipped.
      if (S.maxWeaponRange != 0 && S.sensorTypeInt != XComponentType.INVALID_INT)
        stateTransition(StrategyState.EQUIPPED);
      else
        X.yield();
      break;
    case EQUIPPED:
      // If we acquired a squad, change to the soldier state.
      if (squadId > 0) {
        stateTransition(StrategyState.SOLDIER);
      }
      break;
    case SOLDIER:
      // If we lost the squad, transition into the equipped state.
      if (S.round - lastRadioUpdateRound >= NO_RADIO_MAX_ROUNDS)
        stateTransition(StrategyState.EQUIPPED);
    }
  }

  /**
   * Perform a state transition to state.
   * 
   * @param state
   */
  public final void stateTransition(StrategyState state) {
    // if (state != strategyState) checkRep("changed_state");
    strategyState = state;
    switch (state) {
    case RECRUIT:
      break;
    case EQUIPPED:
//      B.addOnMessageHandler(new Callback() {
//        @Override
//        public void onMessage(Message m) {
//          if (m.ints[2] == squadId)
//            lastRadioUpdateRound = S.round;
//          if (m.ints[1] == CommandType.MANIPLE_JOIN_SQUAD.ordinal()) {
//            for (int i = 3; i <= 6; i++) {
//              if (m.ints[i] == S.id) {
//                joinSquad(m.ints[2]);
//              }
//            }
//          }
//          else if (m.ints[1] == CommandType.MANIPLE_DESTINATION.ordinal()) {
//            D.debug_pl("destination_message", m.ints[0] + " " + m.ints[1] + " " + m.ints[2] + " " + m.ints[3] + " " + m.ints[4]);
//            sargeantLocation = new MapLocation(m.ints[3], m.ints[4]);
//            setIndicatorStrings();
//          }
//        }
//      });
      break;
    }
  }

  public final void step() throws GameActionException {
    setIndicatorStrings();
    stateCheckConditions();
    switch (strategyState) {
    case RECRUIT:
      X.yield();
      break;
    case EQUIPPED:
      // Dance.
      if (squadId == 0) {
        B.checkMessages();
        if (!S.movementController.isActive()) {
          if (S.direction.equals(Direction.SOUTH_WEST))
            X.setDirection(Direction.NORTH_EAST);
          else
            X.setDirection(Direction.SOUTH_WEST);
        }
        X.yield();
      }
      else if (squadId < 0) {
        // FIXME(landa): Go kill.
      }
      else {
        // TODO(landa): What do we do when we're equipped but have a squad?
      }
      break;
    case SOLDIER:
      // You're in the army now!
      // Follow the sergeant and shoot everything down.
//      RobotInfo[] nearbyRobots = senseAndRadioRobots();
//      RobotInfo attackTarget = getAttackTarget(nearbyRobots);
//      if (attackTarget != null)
//        attack(attackTarget);
//      else
        try_moveToFormation();
      X.yield();
      break;
    }
    setIndicatorStrings();
  }

  public final void init() throws GameActionException {
    strategyState = StrategyState.RECRUIT;
  }

  // --- Utilities --------------------------------------------------------

  public final void joinSquad(int squad) {
    squadId = squad;
    updateSquad();
  }

  public final void updateSquad() {
    RobotInfo[] nearbyRobotInfos = S.nearbyRobotInfos();
    for (RobotInfo ri : nearbyRobotInfos) {
      if (ri.robot.getID() == squadId)
        sargeantLocation = ri.location;
    }
  }

  public static final void getMessages() {
    B.checkMessages();
//    B.addOnMessageHandler(new Callback() {
//      @Override
//      public void onMessage(Message m) {
//
//      }
//    });
  }

  public static final RobotInfo[] senseAndRadioRobots() throws GameActionException {
    RobotInfo[] sensedRobots = S.nearbyRobotInfos();
    RobotInfo[] senseAndRadioRobots = new RobotInfo[sensedRobots.length + radioEnemyInfo.length];
    for (int i = 0; i < sensedRobots.length; i++)
      senseAndRadioRobots[i] = sensedRobots[i];
    for (int j = 0; j < radioEnemyInfo.length; j++)
      senseAndRadioRobots[sensedRobots.length + j] = radioEnemyInfo[j];
    return senseAndRadioRobots;
  }

  public static final void try_moveToFormation() throws GameActionException {
    if (!S.movementController.isActive()) {
      if (sargeantLocation != null && sargeantLocation.distanceSquaredTo(S.location) > 2) {
        Direction nextDirection = BugPathfindingBlock.nextDirection(sargeantLocation);
        if (nextDirection != Direction.NONE) {
          X.moveTowardsSync(nextDirection);
        }
      }
    }
  }

  public static final void attack(RobotInfo attackTarget) throws GameActionException {
    RobotInfo currentAttackTarget = attackTarget;
    while (S.weaponControllers[0].isActive()) {
      try_moveToFormation();
      X.yield();
      currentAttackTarget = getAttackTarget(senseAndRadioRobots());
      if (currentAttackTarget == null)
        return;
    }
    S.weaponControllers[0].attackSquare(attackTarget.location, attackTarget.robot.getRobotLevel());
    X.yield();
  }

  public static final RobotInfo getAttackTarget(RobotInfo[] robotInfos) throws GameActionException {
    if (robotInfos.length == 0)
      return null;

    RobotInfo lowestHitpointRobotInfo = null;
    double lowestHitpoints = Double.MAX_VALUE;

    // TODO(landa): shuffle the robot array

    for (RobotInfo robotInfo : robotInfos) {
      // Check if it's an enemy.
      if (robotInfo.robot.getTeam().equals(S.enemyTeam)) {
        // TODO(landa): check for the presence of big guns
        if (robotInfo.hitpoints < lowestHitpoints && S.weaponControllers[0].withinRange(robotInfo.location)) {
          lowestHitpointRobotInfo = robotInfo;
          lowestHitpoints = robotInfo.hitpoints;
        }
      }
    }

    return lowestHitpointRobotInfo;
  }

}
