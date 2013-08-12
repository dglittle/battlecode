package team050.blocks.brain;

import team050.core.B;
import team050.core.CommandType;
import team050.core.D;
import team050.core.S;
import team050.core.U;
import team050.core.X;
import team050.core.xconst.XDirection;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

/**
 * The brain functions that run in the Factory. Executed every round.
 * 
 * The factory sports a Telescope, and scans for mines and enemies. It
 * radios the information every once in a while using its Antenna, and its
 * messages are relayed by the Network in a neighboring Armory.
 */
public class BrainFactoryBlock {
  /** How often do we broadcast mine info. */
  public static final int MINE_INFO_INTERVAL = 10;

  /** How often do we broadcast enemy info. */
  public static final int ENEMY_INFO_INTERVAL = 3;
  
  /** Number of mines in a single message. */
  public static final int MINE_INFO_LENGTH = 5;
  
  /** Performs the brain functions that run in the Factory. */
  public static final boolean async() {
    if (S.sensorType == ComponentType.BUILDING_SENSOR) { return false; }
    
    _scanSector();
    boolean turnValue = _turnAsync();
    
    if (_radioEnemyInfoAsync()) { return true; }
    if (_radioMineInfoAsync()) { return true; }
    return turnValue;
  }
  
  /** One-time initialization right before the block is run. */
  public static final void setupAsync() {
    // Allow the sensor to scan fully before broadcasting mines.
    _lastMineMessageRound = S.round + 8;
    // Broadcast enemies now.
    _lastEnemyMessageRound = S.round - 1;
    // Hardwire our location into the mine info message.
    final int[] message = CommandType.BRAIN_MINES.ints;
    message[0] = S.locationX;
    message[1] = S.locationY;
  }

  /** Discover mines in the current piece of the map. */
  public static final void _scanSector() {
    // Mine locations. Want to find free mines.
    if (!_scannedMines[S.directionInt]) { _scanMines(); }
    final Mine[] mines = _mines[S.directionInt];
    final int mineCount = mines.length;
    int freeMines = mineCount;
    int enemyMines = 0;
    final MapLocation[] mineLocations = _mineLocations[S.directionInt];
    final boolean[] takenMine = new boolean[mineCount];
            
    // Enemies.
    final Robot[] robots = S.nearbyRobots();
    final RobotInfo[] infos = S.nearbyRobotInfos();
    final RobotInfo[] enemyInfos = new RobotInfo[infos.length];
    _enemyInfos[S.directionInt] = enemyInfos;
    final int[] enemyIDs = new int[infos.length];
    _enemyIDs[S.directionInt] = enemyIDs;
    int enemies = 0;

    for (int i = robots.length - 1; i >= 0; i--) {
      final RobotInfo info = infos[i];
      if (info == null) { continue; }
      final Robot robot = robots[i];
      final Team team = robot.getTeam();
      if (team == Team.NEUTRAL) { continue; }
      final boolean isEnemy = (team == S.enemyTeam);

      if (isEnemy) {
        enemyInfos[enemies] = info;
        enemyIDs[enemies] = robot.getID();
        enemies++;
      } else {
        // TODO(pwnall): look at hurt friendlies
      }
      
      // Compare the robot with mines.
      if (info.chassis == Chassis.BUILDING) {
        final MapLocation location = info.location;
        for (int j = mineCount - 1; j >= 0; j--) {
          if (mineLocations[j].equals(location)) {
            // NOTE: no need to worry about double-counting; can't have two
            //       buildings on the same location
            takenMine[j] = true;
            freeMines--;
            if (isEnemy) {
              // Avoid honeypots by making sure mines come with recyclers.
              if (info.components != null &&
                  U.findEnum(info.components, ComponentType.RECYCLER)) {                
                enemyMines++;
              }
            }
          }
        }
      }
    }
    
    // Enemies.
    _totalEnemies += enemies - _enemyCounts[S.directionInt];
    _enemyCounts[S.directionInt] = enemies;
    
    // Free mines.
    final MapLocation[] freeMineLocations = new MapLocation[freeMines];
    for (int i = mineCount - 1; i >= 0; i--) {
      if (!takenMine[i]) {
        freeMineLocations[--freeMines] = mineLocations[i];
      }
    }
    _freeMineLocations[S.directionInt] = freeMineLocations;
  }
  
  /** Scans the mines in the sector that we're currently facing. */
  public static final void _scanMines() {
    final Mine[] mines =
        S.sensorController.senseNearbyGameObjects(Mine.class);
    final int mineCount = mines.length;
    final MapLocation[] locations = new MapLocation[mineCount];
    for (int i = mineCount - 1; i >= 0; i--) {
      final MapLocation location = mines[i].getLocation();
      if (location.equals(S.location)) { continue; }
      locations[i] = location;
    }
    _mines[S.directionInt] = mines;
    _mineLocations[S.directionInt] = locations;
    _scannedMines[S.directionInt] = true;    
  }
  
  /** Turns the Factory so it can scan a new sector. */
  public static final boolean _turnAsync() {
    if (!S.motorReady) { return false; }
    
    try {
      X.setDirection(S.direction.rotateRight());
      return true;
    } catch (GameActionException e) {
      D.debug_logException(e);  // This should never happen.
      return false;
    }
  }

  /** Sends info about mines, to be redistributed. */
  public static final boolean _radioMineInfoAsync() {
    if (S.round - _lastMineMessageRound < MINE_INFO_INTERVAL) {
      return false;
    }
    if (B.bc == null || B.bc.isActive()) { return false; }
    
    final int[] message = CommandType.BRAIN_MINES.ints;
    int mineCount = 0;
    int offset = 3;
    
    // Tries to fill each message with mines from various directions.
    // Takes mine 0 from each direction, then mine 1 from each direction, etc.
    // For fun, the state is memoized across messages, so it picks up where it
    // left.
    while (offset < MINE_INFO_LENGTH) {
      final int sectorCount =
        (_freeMineLocations[_mineMessageSector] == null ?
            0 : _freeMineLocations[_mineMessageSector].length);
      if (_mineMessageIndex < sectorCount) {
        final MapLocation location =
            _freeMineLocations[_mineMessageSector][_mineMessageIndex];
        message[offset] = location.x;
        message[offset + 1] = location.y;
        offset += 2;
        mineCount++;
        _mineMessageBlanks = 0;
      } else {
        _mineMessageBlanks++;
      }
      _mineMessageSector++;
      if (_mineMessageSector == XDirection.ADJACENT_DIRECTIONS) {
        _mineMessageSector = 0;
        _mineMessageIndex += 1;
        if (_mineMessageBlanks >= XDirection.ADJACENT_DIRECTIONS) {
          _mineMessageIndex = 0;
          _mineMessageBlanks = 0;
          break;
        }
      }
    }
    if (mineCount == 0) { return false; }    
    message[2] = mineCount;
    
    try {
      B.send(message);
      _lastMineMessageRound = S.round;
      return true;
    } catch(GameActionException e) {
      D.debug_logException(e);
      return false;
    }
  }
  
  /** Sends info about enemies, to be redistributed. */
  public static final boolean _radioEnemyInfoAsync() {    
    if (_totalEnemies == 0) { return false; }
    if (S.round - _lastEnemyMessageRound < ENEMY_INFO_INTERVAL) {
      return false;
    }
    if (B.bc == null || B.bc.isActive()) { return false; }

    // Select enemy that will reach us the fastest.
    RobotInfo closestEnemy = null;
    int minRoundsToHere = Integer.MAX_VALUE;
    for (int i = XDirection.ADJACENT_DIRECTIONS - 1; i >= 0; i--) {
      final RobotInfo[] enemyInfos = _enemyInfos[i];
      for (int j = _enemyCounts[i] - 1; j >= 0; j--) {
        final RobotInfo enemy = enemyInfos[j];
        if (enemy.components == null) {
          // Sensed by building controller and not telescope. Tough luck.
          continue;
        }
        final int speed = EnemySummary.speed(enemy);
        if (speed == 0) { continue; }
        final int rounds2 =
          (S.location.distanceSquaredTo(enemy.location) * 10000) /
          (speed * speed);
        if (rounds2 < minRoundsToHere) {
          minRoundsToHere = rounds2;
          closestEnemy = enemy;
        }
      }
    }
    if (closestEnemy == null) { return false; }
    
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
  
  /** Mines in each sector. */
  public static final Mine[][] _mines =
      new Mine[XDirection.ADJACENT_DIRECTIONS][];
  /** Locations of mines in each sector. */
  public static final MapLocation[][] _mineLocations = 
      new MapLocation[XDirection.ADJACENT_DIRECTIONS][];

  /** Location of free mines in each sector. */
  public static final MapLocation[][] _freeMineLocations = 
      new MapLocation[XDirection.ADJACENT_DIRECTIONS][];
  
  /** True if the mines in a certain direction have been scanned. */
  public static final boolean[] _scannedMines =
      new boolean[XDirection.ADJACENT_DIRECTIONS];
  
  /** RobotInfo for the enemies in each sector. */
  public static final RobotInfo[][] _enemyInfos =
      new RobotInfo[XDirection.ADJACENT_DIRECTIONS][];
  /** Object IDs for the enemies in each sector. */
  public static final int[][] _enemyIDs =
      new int[XDirection.ADJACENT_DIRECTIONS][];
  /** Number of enemy robots in each sector. */
  public static final int[] _enemyCounts =
      new int[XDirection.ADJACENT_DIRECTIONS];
  /** Number of total enemies. */
  public static int _totalEnemies;

  
  /** State for assembling the message containing mines. */
  public static int _mineMessageSector, _mineMessageIndex, _mineMessageBlanks;
  /** Last time we sent a message about mine info. */
  public static int _lastMineMessageRound;
  /** Last time we sent a message about enemy info. */
  public static int _lastEnemyMessageRound;
}
