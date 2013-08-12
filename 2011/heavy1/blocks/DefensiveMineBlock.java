package heavy1.blocks;

import heavy1.chaos.pwnall.VikingBuildBlock;
import heavy1.core.D;
import heavy1.core.Role;
import heavy1.core.S;
import heavy1.core.X;
import heavy1.core.xconst.XDirection;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;

public class DefensiveMineBlock {
  /**
   * Tunes the knobs of the defensive spawning algorithm.
   * 
   * @param maxDefenseCount when there are these many defenders adjacent to the
   *                        building, no more defenders will be built
   * @param defenseSensor the type of sensor to equip to sense enemies
   */
  public static final void setDefenseParameters(int maxDefenders,
      ComponentType defenseSensor) {
    _maxDefenders = maxDefenders;
    _defenseSensor = defenseSensor;
  }
  
  /** Turns the current robot into an exclusively defensive bot. */
  public static final void sync() {
    while (true) {
      async();
      X.yield();
    }
  }
  
  /** Makes progress towards defending the mine if necessary. */
  public static final void async() {
    _equipSensorAsync();
    VikingBuildBlock.async();
    
    RobotInfo weakestEnemy = _weakestNearbyEnemy();
    if (weakestEnemy != null) {
      _senseAdjacentDefense();
      _buildDefenseAsync();
    } else {
      _turnSensorAsync();      
    }
  }

  /** The build order for a defensive unit. */
  public static Role[] defenseRoles = {Role.SOLDIER};
  
  /** The weakest enemy in sensor range. */
  public static final RobotInfo _weakestNearbyEnemy() {
    final RobotInfo[] nearbyRobotInfos = S.nearbyRobotInfos();
    RobotInfo weakestEnemy = null;
    double minHP = Double.MAX_VALUE;
    for (int i = nearbyRobotInfos.length - 1; i >= 0; i--) {
      RobotInfo info = nearbyRobotInfos[i];
      if (info == null) { continue; }
      Robot robot = info.robot;
      if (robot.getTeam() != S.enemyTeam) { continue; }
      
      if (info.hitpoints < minHP) {
        minHP = info.hitpoints;
        weakestEnemy = info;
      }
    }
    return weakestEnemy;
  }

 /** Updates _adjacentDefenseInfo and _adjacentDefenseCount. */
  public static final void _senseAdjacentDefense() {
    _adjacentDefenseCount = 0;
    final Robot[] robots = 
      S.buildingSensorController.senseNearbyGameObjects(Robot.class);
    for (int i = robots.length - 1; i >= 0; i--) {
      final Robot robot = robots[i];
      if (robot.getTeam() != S.team) { continue; }
      
      try {
        final RobotInfo info = S.buildingSensorController.senseRobotInfo(robot);
        if (info.chassis != defenseRoles[0].chassis) { continue; }
 
        _adjacentDefenseInfo[_adjacentDefenseCount] = info;
        _adjacentDefenseCount += 1;
         } catch (GameActionException e) {
        D.debug_logException(e);
      }
    }
  }

  /** Rotate so the sensors get full coverage of the map. */
  public static final boolean _turnSensorAsync() {
    if (S.sensorIsOmnidirectional || S.movementController.isActive()) {
      return false;
    }
    
    try {
      // TODO(pwnall): generic method that turns to next sensor direction.
      X.setDirection(S.direction.opposite());
      return true;
    } catch (GameActionException e) {
      // TODO Auto-generated catch block
      D.debug_logException(e);
      return false;
    }
  }

  public static final boolean _buildDefenseAsync() {
    if (VikingBuildBlock.startedBuilding()) { return false; }
    if (_adjacentDefenseCount < _maxDefenders) {
      VikingBuildBlock.setBuildOrder(defenseRoles, null);
      return true;      
    } else {
      VikingBuildBlock.cancelBuildOrder();
      return false;
    }        
  }

  /** Equips self with a sensor if possible. */
  public static final boolean _equipSensorAsync() {
    if (S.sensorType == _defenseSensor) { return false; }
    if (S.builderController == null || S.builderController.isActive()) {
      return false;
    }
    if (S.oldFlux == 0 || S.flux <= _defenseSensor.cost + S.totalUpkeep || S.dFlux < 0) {
      return false;
    }

    try {
      S.builderController.build(_defenseSensor, S.location, S.level);
      return true;
    } catch (GameActionException e) {
      D.debug_logException(e);
      return false;
    }
  }
  
  /** RobotInfo for adjacent robots that look like our defense. */
  public static RobotInfo[] _adjacentDefenseInfo = new RobotInfo[XDirection.ADJACENT_DIRECTIONS];
  /** Number of valid entries in _adjacentDefenseInfo. */
  public static int _adjacentDefenseCount;
  /** The sensor to build to sense enemies. */
  public static ComponentType _defenseSensor = ComponentType.RADAR;
  /** The adjacent defenders count should never exceed this number. */
  public static int _maxDefenders = 2;
}
