package team050.blocks.brain;

import team050.core.B;
import team050.core.CommandType;
import team050.core.D;
import team050.core.S;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

/**
 * The brain functions that run in larvaes. Executed every round.
 * 
 * The larvaes scan for enemies, and complain when they see them.
 */
public class BrainLarvaBlock {
  /** How often do we scan for enemies. */
  public static final int ENEMY_SCAN_INTERVAL = 4;

  /** How often do we broadcast enemy info. */
  public static final int ENEMY_INFO_INTERVAL = 4;
  
  /** Performs the brain functions that run in the larvae. */
  public static final boolean async() {
    _scanForEnemies();
    return _radioEnemyInfoAsync();
  }
  
  /** One-time initialization right before the block is run. */
  public static final void setupAsync() {
    _lastEnemyScanRound = S.round;
    _lastEnemyMessageRound = S.round + 1;
  }

  /** Discover mines in the current piece of the map. */
  public static final boolean _scanForEnemies() {
    if (S.round - _lastEnemyScanRound < ENEMY_SCAN_INTERVAL) {
      return false;
    }
    if (!S.sensorIsOmnidirectional) {
      _lastEnemyScanRound = S.round;
      return false;
    }

    final Robot[] robots = S.nearbyRobots();
    final RobotInfo[] infos = S.nearbyRobotInfos();
    _enemyInfos = new RobotInfo[infos.length];
    _enemyIDs = new int[infos.length];
    _totalEnemies = 0;

    for (int i = robots.length - 1; i >= 0; i--) {
      final RobotInfo info = infos[i];
      if (info == null) { continue; }
      final Robot robot = robots[i];
      final Team team = robot.getTeam();
      if (team == Team.NEUTRAL) { continue; }
      final boolean isEnemy = (team == S.enemyTeam);

      if (isEnemy) {
        _enemyInfos[_totalEnemies] = info;
        _enemyIDs[_totalEnemies] = robot.getID();
        _totalEnemies++;
      }
    }
    _lastEnemyScanRound = S.round;
    return true;
  }
  
  /** Sends info about enemies, to be redistributed. */
  public static final boolean _radioEnemyInfoAsync() {    
    if (_totalEnemies == 0) { return false; }
    if (S.round - _lastEnemyMessageRound < ENEMY_INFO_INTERVAL) {
      return false;
    }
    if (B.bc == null || B.bc.isActive()) { return false; }

    // Select enemy that will reach base the fastest.
    MapLocation baseLocation = BrainState._lastBrainLocation;
    if (baseLocation == null) {
      baseLocation = S.location;
    }
    
    RobotInfo closestEnemy = null;
    int minRoundsToBase = Integer.MAX_VALUE;
    for (int i = _totalEnemies - 1; i >= 0; i--) {
      final RobotInfo enemy = _enemyInfos[i];
      if (enemy.components == null) {
        // Sensed by non-fancy sensor. Tough luck.
        continue;
      }
      final int speed = EnemySummary.speed(enemy);
      if (speed == 0) { continue; }
      final int rounds2 =
        (baseLocation.distanceSquaredTo(enemy.location) * 10000) /
        (speed * speed);
      if (rounds2 < minRoundsToBase) {
        minRoundsToBase = rounds2;
        closestEnemy = enemy;
      }
    }
    
    // Tell mamma Recycler about the danger.
    final int[] message = CommandType.BRAIN_ENEMY.ints;
    EnemySummary.summarize(closestEnemy, message, BrainState.ENEMY_INFO_OFFSET);
    try {
      B.send(message);
      _lastEnemyMessageRound = S.round;
      return true;
    } catch (GameActionException e) {
      D.debug_logException(e);  // This should not happen.
      return false;
    }
  }
    
  /** RobotInfo for the enemies in each sector. */
  public static RobotInfo[] _enemyInfos;
  /** Object IDs for the enemies in each sector. */
  public static int[] _enemyIDs;
  /** Number of total enemies. */
  public static int _totalEnemies;
  
  /** Last time we scanned for enemies. */
  public static int _lastEnemyScanRound;
  /** Last time we sent a message about enemy info. */
  public static int _lastEnemyMessageRound;
}
