package flyingRush.chaos;

import flyingRush.core.D;
import flyingRush.core.S;
import flyingRush.core.X;
import flyingRush.core.xconst.XDirection;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;

public class DefensiveMineTopLevel {
  /** The sensor to build on top of mines to sense enemies. */
  public static ComponentType mineSensor = ComponentType.RADAR;
  /** How many defenses to build. */
  public static int defenseCount = 2;
  /** The kind of defense to build. */
  public static Chassis defenseChassis = Chassis.LIGHT;
  /** The sensor to put on defense units. */
  public static ComponentType defenseSensor = ComponentType.RADAR;
  /** The weapon to put on defense units. */
  public static ComponentType defenseWeapon = ComponentType.BLASTER;

  /** Do not set manually. */
  public static int _mineSensorCost = mineSensor.cost;
  /** Do not set manually. */
  public static int _defenseChassisWeight = defenseChassis.weight;
  /** Do not set manually. */
  public static int _defenseChassisCost = defenseChassis.cost;
  /** Do not set manually. */
  public static double _defenseChassisUpkeep = defenseChassis.upkeep;
  /** Do not set manually. */
  public static int _defenseSensorWeight = defenseSensor.weight;
  /** Do not set manually. */
  public static int _defenseSensorCost = defenseSensor.cost;
  /** Do not set manually. */
  public static int _defenseWeaponWeight = defenseWeapon.weight;
  /** Do not set manually. */
  public static int _defenseWeaponCost = defenseWeapon.cost;

  /** Top-level run function. */
  public static final void run() {
    _waitUntilWakeup();
    if (S.leftoverWeight >= mineSensor.weight) {
      _equipSensor();
    }

    while (true) {
      try {
        if (!_enemiesSpottedNearby()) {
          // No enemies. Rotate so we get full coverage.
          if (!S.sensorIsOmnidirectional) {
            // TODO(pwnall): generic method that turns to the next sensor
            // direction.
            X.setDirection(S.direction.opposite());
          }
          X.yield();
          continue;
        }

        _senseAdjacentDefense();
        for (int i = 0; i < _adjacentDefenseCount; i++) {
          RobotInfo info = _adjacentDefenseInfo[i];

          // Priority 1: wake up sleeping defense.
          if (!info.on) {
            S.rc.turnOn(info.location, info.robot.getRobotLevel());
            X.yield();
            continue;
          }

          // Priority 2: equip living defense.
          if (_tryEquipDefense(info)) {
            continue;
          }
        }
        // Priority 3: build new defense.
        if (_adjacentDefenseCount < defenseCount) {
          if (_tryBuildDefense()) {
            continue;
          }
        }

        X.yield();
      } catch (GameActionException e) {
        D.debug_logException(e);
        X.yield();
      }
    }
  }

  /** Equips self with some sensor asap. Returns when done. */
  public static final void _equipSensor() {
    while (true) {
      if (S.builderController == null || S.builderController.isActive()) {
        X.yield();
        continue;
      }

      if (S.flux <= _mineSensorCost) {
        X.yield();
        continue;
      }
      try {
        S.builderController.build(mineSensor, S.location, S.level);
        X.yield();
        break;
      } catch (GameActionException e) {
        D.debug_logException(e);
        X.yield();
      }
    }
  }

  /** The last robot that wasn't ours. */
  public static Robot _lastSpottedEnemy;
  /** RobotInfo for the last robot that wasn't ours. */
  public static RobotInfo _lastSpottedEnemyInfo;
  /** The last direction that we've seen an enemy coming from. */
  public static Direction _lastSpottedEnemyDirection = Direction.NORTH;

  /** True if there are enemies in sensor range. */
  public static final boolean _enemiesSpottedNearby() {
    for (RobotInfo robotInfo : S.nearbyRobotInfos()) {
      if (robotInfo == null) {
        continue;
      }
      Robot robot = robotInfo.robot;
      if (robot.getTeam() == S.enemyTeam) {
        _lastSpottedEnemy = robot;
        _lastSpottedEnemyInfo = robotInfo;
        Direction toEnemy = S.location.directionTo(robotInfo.location);
        if (toEnemy.dx > 0 || toEnemy.dy > 0) {
          _lastSpottedEnemyDirection = toEnemy;
        }
        return true;
      }
    }
    return false;
  }

  /** RobotInfo for adjacent robots that look like our defense. */
  public static RobotInfo[] _adjacentDefenseInfo = new RobotInfo[XDirection.ADJACENT_DIRECTIONS];
  /** Number of valid entries in _adjacentDefenseInfo. */
  public static int _adjacentDefenseCount;

  /** Updates _adjacentDefenseInfo and _adjacentDefenseCount. */
  public static final void _senseAdjacentDefense() {
    _adjacentDefenseCount = 0;
    for (RobotInfo info : S.nearbyRobotInfos()) {
      if (info.robot.getTeam() != S.team) {
        continue;
      }
      if (info.chassis != defenseChassis) {
        continue;
      }
      if (!info.location.isAdjacentTo(S.location)) {
        continue;
      }

      _adjacentDefenseInfo[_adjacentDefenseCount] = info;
      _adjacentDefenseCount += 1;
    }
  }

  /** Equips an existing defense unit with stuff it needs (sensor, weapons). */
  public static final boolean _tryEquipDefense(RobotInfo info) {
    boolean hasSensor = false;
    int capacityLeft = _defenseChassisWeight;
    for (ComponentType component : info.components) {
      capacityLeft -= component.weight;
      if (component == defenseSensor) {
        hasSensor = true;
      }
    }

    try {
      if (!hasSensor && capacityLeft >= _defenseSensorWeight) {
        if (S.flux > _defenseSensorCost && S.dFlux > 0) {
          S.builderController.build(defenseSensor, info.location,
              info.robot.getRobotLevel());
          X.yield();
          return true;
        }
      } else if (capacityLeft >= _defenseWeaponWeight
          && S.flux > _defenseWeaponCost && S.dFlux > 0) {
        S.builderController.build(defenseWeapon, info.location,
            info.robot.getRobotLevel());
        X.yield();
        return true;
      }
      return false;
    } catch (GameActionException e) {
      D.debug_logException(e);
      return false;
    }
  }

  /** Builds a new defense unit. */
  public static final boolean _tryBuildDefense() {
    if (S.flux <= _defenseChassisCost + _defenseSensorCost + _defenseWeaponCost
        || S.dFlux <= _defenseChassisUpkeep) {
      return false;
    }

    Direction direction = _lastSpottedEnemyDirection;
    for (int i = 0; i < XDirection.ADJACENT_DIRECTIONS; i++) {
      if (S.movementController.canMove(direction)) {
        MapLocation location = S.location.add(direction);
        try {
          S.builderController.build(defenseChassis, location);
          X.yield();
        } catch (GameActionException e) {
          D.debug_logException(e);
          return false;
        }
        return true;
      }
      direction = direction.rotateRight();
    }
    return false;
  }

  /** Returns when the robot is awake. */
  public static final void _waitUntilWakeup() {
    while (S.movementController.isActive()) {
      X.yield();
    }
  }
}
