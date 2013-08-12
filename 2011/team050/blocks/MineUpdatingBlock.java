package team050.blocks;

import java.util.HashMap;

import team050.blocks.brain.BrainState;
import team050.core.D;
import team050.core.S;
import team050.core.U;
import battlecode.common.Chassis;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class MineUpdatingBlock {

  /** The location of the current goal mine. */
  public static MapLocation goalMine;
  /** Caches the current position of the robot relative to the goal mine. */
  public static boolean isAdjacentToMine, isFacingMine;
  /** Number of rounds to ignore after finding a mine that is taken. */
  public static final int IGNORE_TAKEN_MINE_ROUNDS = 100;
  public static final int IGNORE_HARD_TO_REACH_MINE_ROUNDS = 50;
  public static final int IGNORE_JUST_BUILT_MINE_ROUNDS = 50;
  /** Spin counter for spinning after building a mine. */
  public static int spin = 0;

  /**
   * Updates the goal mine location, considering whether it is occupied and
   * whether other team robots are trying to build too.
   */
  public static final void update() {
    // see if our current goal is taken
    _updateIfMineOccupied();
    _updateNearbyMines();
    _updateMineFromMessage();
    if (goalMine != null && !_goalMineFromMessage && 
        EnemyUpdatingBlock.hasNearbyEnemyWithWeapon())
      goalMine = null;
    debug_goalMineState();
  }

  /**
   * Check if the current goal mine is occupied, if so, set goal mine to null.
   */
  public static final void _updateIfMineOccupied() {
    try {
      if (goalMine != null && S.sensorController.canSenseSquare(goalMine)) {
        final RobotInfo ri = S.senseRobotInfo(goalMine, RobotLevel.ON_GROUND);
        if (ri != null && ri.chassis == Chassis.BUILDING) {
          goalMine = null;
        }
      }
    } catch (GameActionException gae) {
      D.debug_logException(gae);
    }
  }

  /**
   * Updates goalMine from nearby mines in the sensor range.
   */
  public static final void _updateNearbyMines() {
    try {
      // see if we see a better mine to capture
      final MapLocation nearestMine = S.nearestEmptyMine();

      if (nearestMine != null) {
        final Integer ignoreRound = _ignoredMines.get(nearestMine);
        if (ignoreRound == null || S.round > ignoreRound) {
          if (goalMine == null
              || nearestMine.distanceSquaredTo(S.location) < goalMine
                  .distanceSquaredTo(S.location) || _goalMineFromMessage) {
            if (U.canEasilyGetTo(nearestMine)) {
              goalMine = nearestMine;
              // Set the current goal as not from message.
              _goalMineFromMessage = false;
              // Don't spin after finding a mine even if the mine is taken.
              spin = 0;

            } else {
              if (goalMine != null && nearestMine.equals(goalMine))
                goalMine = null;
              _ignoredMines.put(nearestMine, S.round
                  + IGNORE_HARD_TO_REACH_MINE_ROUNDS);
            }
          }
        }
      }
    } catch (GameActionException gae) {
      D.debug_logException(gae);
    }
    _updateGoalMineTaken();
  }

  /**
   * Check if other team robots are waiting to build at the same location, if
   * so, set goal mine to null and move on.
   */
  public static final void _updateGoalMineTaken() {
    try {
      if (goalMine != null) {
        if ((isAdjacentToMine = S.location.isAdjacentTo(goalMine)) == true) {
          final Direction towardMine = S.location.directionTo(goalMine);
          isFacingMine = S.direction == towardMine;
          if (isFacingMine && U.buildLocationTaken(goalMine)) {
            _ignoredMines.put(goalMine, S.round + IGNORE_TAKEN_MINE_ROUNDS);
            goalMine = null;
          }
        }
      }
    } catch (GameActionException gae) {
      D.debug_logException(gae);
    }
  }

  /**
   * Resets after building a recycler.
   */
  public static void reset() {
    _ignoredMines.put(goalMine, S.round + IGNORE_JUST_BUILT_MINE_ROUNDS);
    goalMine = null;
    spin = 3;
    _goalMineFromMessage = false;
    update();
  }

  /**
   * Only updates the goalMine with the nearest mine from message if goalMine is
   * null from nearby mines.
   */
  public static final void _updateMineFromMessage() {
    if (spin > 0 || BrainState.lastMineMessage == null) {
      return;
    }
    if (BrainState.lastMineMessageRound <= _previousMineMessageRound) {
      return;
    }

    _previousMineMessageRound = BrainState.lastMineMessageRound;

    int nearestDistance = (goalMine == null) ? Integer.MAX_VALUE : S.location
        .distanceSquaredTo(goalMine);

    final MapLocation[] freeMines = BrainState.freeMines();
    for (int i = freeMines.length - 1; i >= 0; i--) {
      final MapLocation freeMine = freeMines[i];
      final Integer ignoreRounds = _ignoredMines.get(freeMine);
      if (ignoreRounds == null || S.round > ignoreRounds) {
        final int distanceSquared = S.location.distanceSquaredTo(freeMine);
        if (distanceSquared < nearestDistance) {
          nearestDistance = distanceSquared;
          goalMine = freeMine;
          _goalMineFromMessage = true;
        }
      }
    }
    _updateGoalMineTaken();
  }

  public static final void debug_goalMineState() {
    D.debug_setIndicator(1, "goal mine = " + goalMine + " location = "
        + S.location + " offset = " + U.offsetTo(goalMine) + " from message ="
        + _goalMineFromMessage + " round = " + S.round + " spin = " + spin +
        " has enemy with weapon = " + EnemyUpdatingBlock.hasNearbyEnemyWithWeapon());
  }

  public static int _previousMineMessageRound = 0;
  /** True if the goal mine is from message if goal mine is not null. */
  public static boolean _goalMineFromMessage;
  public static HashMap<MapLocation, Integer> _ignoredMines = new HashMap<MapLocation, Integer>(
      GameConstants.MINES_MAX / 2);
}
