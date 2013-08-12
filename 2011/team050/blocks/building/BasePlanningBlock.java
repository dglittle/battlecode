package team050.blocks.building;

import team050.core.D;
import team050.core.M;
import team050.core.S;
import team050.core.xconst.BaseConfigs;
import team050.core.xconst.SensorRanges;
import team050.core.xconst.XComponentType;
import team050.core.xconst.XDirection;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;


// This file has some auto-generated parts. After modifying, run
//    bcpm regen src/team050/blocks/BuildBlock.java


/**
 * Recycler logic for deciding where the Factory and Armory should be built.
 * 
 * The method in this class are called by {@link BuildBlock#async()}. The fields
 * are the only public interface.
 */
public final class BasePlanningBlock {
  /** Locations of this base's SCV. null if there is no SCV around. */
  public static MapLocation _scvLocation;
  /** Locations of this base's Factory. null if there is no Factory around. */
  public static MapLocation _factoryLocation;
  /** Locations of this base's Armory. null if there is no Armory around. */
  public static MapLocation _armoryLocation;
    
  /** The robot ID of our Armory. Good if {@link #_armoryLocation} != null. */
  public static int _armoryID;
  /** The robot ID of our Factory. Good if {@link #_factoryLocation} != null. */
  public static int _factoryID;
  /** The robot ID of our SCV. Good if {@link #_scvLocation} != null. */
  public static int _scvID;
  
  /** Where we'd like to spawn units. Adjacent to all bulding peers. */
  public static MapLocation _plannedSpawnPoint;
  /** Where we'd like to have our Factory. */
  public static MapLocation _plannedFactoryLocation;
  /** Where we'd like to have our Armory. */
  public static MapLocation _plannedArmoryLocation;
  /** The score for the best map configuration that we could find. */
  public static int _layoutScore;
  
  /** True if the best configuration we have allows us to build a brain. */
  public static final boolean canBuildBrain() {
    _setBaseConfig();
    return _layoutScore >= 100;
  }

  /** Verifies that known build peers are still where we remember they were. */
  public static final void _checkPeerLocations() {
    if (S.sensorController != null) {
      _checkPeersLocationsWith(S.sensorController);
      if (S.buildingSensorController != S.sensorController &&
          S.buildingSensorController != null) {
        _checkPeersLocationsWith(S.buildingSensorController);
      }
    }
  }
  
  /** Verifies that known build peers are still where we remember they were. */
  public static final void _checkPeersLocationsWith(SensorController sensor) {
    //$ +gen:source BuildBlock.checkPeers armory ArmoryLocation ARMORY
    if (_armoryLocation != null) {
      if (sensor.canSenseSquare(_armoryLocation)) {
        try {
          GameObject maybePeer = sensor.senseObjectAtLocation(_armoryLocation,
              RobotLevel.ON_GROUND);
          if (maybePeer == null || maybePeer.getID() != _armoryID) {
            _armoryLocation = null;
            _armoryDistance2 = Integer.MAX_VALUE;
          }          
        } catch (GameActionException e) {
          D.debug_logException(e);
          // We went over the bytecode limit. Nothing we can do about it.
        }
      }
    } else if (_plannedArmoryLocation != null &&
               sensor.canSenseSquare(_plannedArmoryLocation)) {
      try {
        GameObject maybePeer = sensor.senseObjectAtLocation(
            _plannedArmoryLocation, RobotLevel.ON_GROUND);
        if (maybePeer != null) {
          Robot robot = (Robot)maybePeer;
          RobotInfo info = sensor.senseRobotInfo((Robot)maybePeer);
          if (info.chassis == Chassis.BUILDING) {
            if (robot.getTeam() == S.team) {
              switch (BaseComponentClassifier.robotType(info)) {
                case BaseComponentClassifier.ARMORY:
                  _armoryLocation = _plannedArmoryLocation;
                  _armoryDistance2 =
                      _plannedArmoryLocation.distanceSquaredTo(S.location);
                  _armoryID = robot.getID();
                  break;
                case BaseComponentClassifier.EMPTY:
                  // Wait and see what happens.
                  break;
                default:
                  // We'll have to re-plan the base.
                  _plannedArmoryLocation = null;
              }
            } else {
              _plannedArmoryLocation = null;
            }
          }
        }
      } catch (GameActionException e) {
        D.debug_logException(e);
        // We went over the bytecode limit. Nothing we can do about it.
      }
    }
    //$ -gen:source
    
    //$ +gen:target BuildBlock.checkPeers factory FactoryLocation FACTORY
    if (_factoryLocation != null) {
      if (sensor.canSenseSquare(_factoryLocation)) {
        try {
          GameObject maybePeer = sensor.senseObjectAtLocation(_factoryLocation,
              RobotLevel.ON_GROUND);
          if (maybePeer == null || maybePeer.getID() != _factoryID) {
            _factoryLocation = null;
            _factoryDistance2 = Integer.MAX_VALUE;
          }          
        } catch (GameActionException e) {
          D.debug_logException(e);
          // We went over the bytecode limit. Nothing we can do about it.
        }
      }
    } else if (_plannedFactoryLocation != null &&
               sensor.canSenseSquare(_plannedFactoryLocation)) {
      try {
        GameObject maybePeer = sensor.senseObjectAtLocation(
            _plannedFactoryLocation, RobotLevel.ON_GROUND);
        if (maybePeer != null) {
          Robot robot = (Robot)maybePeer;
          RobotInfo info = sensor.senseRobotInfo((Robot)maybePeer);
          if (info.chassis == Chassis.BUILDING) {
            if (robot.getTeam() == S.team) {
              switch (BaseComponentClassifier.robotType(info)) {
                case BaseComponentClassifier.FACTORY:
                  _factoryLocation = _plannedFactoryLocation;
                  _factoryDistance2 =
                      _plannedFactoryLocation.distanceSquaredTo(S.location);
                  _factoryID = robot.getID();
                  break;
                case BaseComponentClassifier.EMPTY:
                  // Wait and see what happens.
                  break;
                default:
                  // We'll have to re-plan the base.
                  _plannedFactoryLocation = null;
              }
            } else {
              _plannedFactoryLocation = null;
            }
          }
        }
      } catch (GameActionException e) {
        D.debug_logException(e);
        // We went over the bytecode limit. Nothing we can do about it.
      }
    }
    //$ -gen:target
    
    //$ +gen:target BuildBlock.checkPeers scv SpawnPoint SCV
    if (_scvLocation != null) {
      if (sensor.canSenseSquare(_scvLocation)) {
        try {
          GameObject maybePeer = sensor.senseObjectAtLocation(_scvLocation,
              RobotLevel.ON_GROUND);
          if (maybePeer == null || maybePeer.getID() != _scvID) {
            _scvLocation = null;
            _scvDistance2 = Integer.MAX_VALUE;
          }          
        } catch (GameActionException e) {
          D.debug_logException(e);
          // We went over the bytecode limit. Nothing we can do about it.
        }
      }
    } else if (_plannedSpawnPoint != null &&
               sensor.canSenseSquare(_plannedSpawnPoint)) {
      try {
        GameObject maybePeer = sensor.senseObjectAtLocation(
            _plannedSpawnPoint, RobotLevel.ON_GROUND);
        if (maybePeer != null) {
          Robot robot = (Robot)maybePeer;
          RobotInfo info = sensor.senseRobotInfo((Robot)maybePeer);
          if (info.chassis == Chassis.BUILDING) {
            if (robot.getTeam() == S.team) {
              switch (BaseComponentClassifier.robotType(info)) {
                case BaseComponentClassifier.SCV:
                  _scvLocation = _plannedSpawnPoint;
                  _scvDistance2 =
                      _plannedSpawnPoint.distanceSquaredTo(S.location);
                  _scvID = robot.getID();
                  break;
                case BaseComponentClassifier.EMPTY:
                  // Wait and see what happens.
                  break;
                default:
                  // We'll have to re-plan the base.
                  _plannedSpawnPoint = null;
              }
            } else {
              _plannedSpawnPoint = null;
            }
          }
        }
      } catch (GameActionException e) {
        D.debug_logException(e);
        // We went over the bytecode limit. Nothing we can do about it.
      }
    }
    //$ -gen:target    
  }
  
  /**
   * Scans the map for distributed build peers.
   * 
   * This is called by {@link BuildBlock#async()} and should not be called
   * directly.
   */
  public static final void _scanForPeers() {
    RobotInfo[] infos = S.nearbyRobotInfos();
    robotLoop: for (int i = infos.length - 1; i >= 0; i--) {
      RobotInfo info = infos[i];
      if (info.robot.getTeam() != S.team) { continue; }
      ComponentType[] components = info.components;
      for (int j = components.length - 1; j >= 0; j--) {
        switch (components[j].ordinal()) {
        case XComponentType.ARMORY_INT: {
          //$ +gen:source BuildBlock.peerScan armory
          final int distance2 = info.location.distanceSquaredTo(S.location);
          if (distance2 < _armoryDistance2) {
            _armoryLocation = info.location;
            _armoryDistance2 = distance2;
            _armoryID = info.robot.getID();
          }          
          continue robotLoop;
          //$ -gen:source
        }
        case XComponentType.FACTORY_INT: {
          //$ +gen:target BuildBlock.peerScan factory
          final int distance2 = info.location.distanceSquaredTo(S.location);
          if (distance2 < _factoryDistance2) {
            _factoryLocation = info.location;
            _factoryDistance2 = distance2;
            _factoryID = info.robot.getID();
          }          
          continue robotLoop;
          //$ -gen:target
        }
        case XComponentType.CONSTRUCTOR_INT: {
          //$ +gen:target BuildBlock.peerScan scv
          final int distance2 = info.location.distanceSquaredTo(S.location);
          if (distance2 < _scvDistance2) {
            _scvLocation = info.location;
            _scvDistance2 = distance2;
            _scvID = info.robot.getID();
          }          
          continue robotLoop;
          //$ -gen:target
        }
        }
      }
    }
  }
  
  /**
   * Finds a good base configuration.
   * 
   * This is called by {@link BuildBlock#async()} and should not be called
   * directly.
   */
  public static final boolean _setBaseConfig() {
    // Fast path: if our last decision is still good, stick to it.
    if (_plannedFactoryLocation != null && _plannedArmoryLocation != null
        && _plannedSpawnPoint != null) {
      return true;
    }
    
    Map._update();
    final int mx = S.locationX - M.arrayBaseX;
    final int my = S.locationY - M.arrayBaseY;
    for (int ci = 0; ci < BaseConfigs.spawnDx.length; ci++) {
      final int fdx = BaseConfigs.factoryDx[ci];
      final int fdy = BaseConfigs.factoryDy[ci];
      final MapLocation factoryLocation = S.location.add(fdx, fdy);
      if (_factoryLocation != null) {
        if (!factoryLocation.equals(_factoryLocation)) { continue; }
      } else {
        if (Map._buildingMap[fdx + Map.MAP_BASE][fdy + Map.MAP_BASE]) {
          continue;
        }
        if (!M.land[mx + fdx][my + fdy]) { continue; }
      }
      
      final int adx = BaseConfigs.armoryDx[ci];
      final int ady = BaseConfigs.armoryDy[ci];
      final MapLocation armoryLocation = S.location.add(adx, ady);
      if (_armoryLocation != null) {
        if (!armoryLocation.equals(_armoryLocation)) { continue; }
      } else {
        if (Map._buildingMap[adx + Map.MAP_BASE][ady + Map.MAP_BASE]) {
          continue;
        }
        if (!M.land[mx + adx][my + ady]) { continue; }
      }
  
      final int sdx = BaseConfigs.spawnDx[ci];
      final int sdy = BaseConfigs.spawnDy[ci];
      if (Map._buildingMap[sdx + Map.MAP_BASE][sdy + Map.MAP_BASE]) {
        continue;
      }
      if (!M.land[mx + sdx][my + sdy]) { continue; }
      final MapLocation spawnPoint = S.location.add(sdx, sdy);
      
      // TODO(pwnall): break ties by locations that are occupied by units
      
      _plannedFactoryLocation = factoryLocation;
      _plannedArmoryLocation = armoryLocation;
      _plannedSpawnPoint = spawnPoint;
      _layoutScore = BaseConfigs.score[ci];
      BuildBlock.debug_setBaseConfigIndicator();
      return true;
    }
    
    _plannedArmoryLocation = null;
    _plannedFactoryLocation = null;
    _plannedSpawnPoint = null;
    _layoutScore = -1;
    return false;
  }
  
  /** Distance to armory at {@link _armoryLocation}. */
  public static int _armoryDistance2 = Integer.MAX_VALUE;
  /** Distance to factory at {@link _factoryLocation}. */
  public static int _factoryDistance2 = Integer.MAX_VALUE;
  /** Distance to scv at {@link _scvLocation}. */
  public static int _scvDistance2 = Integer.MAX_VALUE;

  /** Specialized classification logic for base components. */
  public static final class BaseComponentClassifier {
    /**
     * Classifies a building according to its contents.
     * @param info the building's {@link RobotInfo}
     * @return one of the {@link BaseComponentClassifier} values
     */
    public static final int robotType(RobotInfo info) {
      final ComponentType[] components = info.components;
      int shields = 0, defs = 0, weapons = 0;
      for (int i = components.length - 1; i >= 0; i--) {
        final ComponentType component = components[i];
        switch (components[i]) {
          case RECYCLER:
            return RECYCLER;
          case ARMORY:
            return ARMORY;
          case FACTORY:
            return FACTORY;
          case CONSTRUCTOR:
            return SCV;
          case SHIELD:
            shields++;
          case PLASMA:
          case HARDENED:
          case REGEN:
          case IRON:
            defs += component.weight;
            break;
          case SMG:
          case BLASTER:
          case RAILGUN:
          case HAMMER:
          case BEAM:
            weapons += component.weight;
            break;
          default:
            break;
        }
      }
      if (shields >= 10 || defs - weapons >= 8) { return HONEYPOT; }
      if (weapons >= 4 || shields >= 4) { return TOWER; }
      return EMPTY;
    }
    
    /** Extracts flux from a mine. */
    public static final int RECYCLER = 0;
    /** Building peer. */
    public static final int ARMORY = 1;
    /** Building peer. */
    public static final int FACTORY = 2;
    /** Building peer. */
    public static final int SCV = 3;
    /** Most likely under construction. */
    public static final int EMPTY = 4;
    /** Combat building. */
    public static final int TOWER = 5;
    /** Blah. */
    public static final int HONEYPOT = 6;
  }
  
  /** Specialized mapping information for base planning. */
  public static final class Map {
    /** Updates map info. */
    public static final void _update() {
      _scanMines();
      _scanBuildings();
    }
    
    /** Updates {@link #_buildingMap} to reflect surrounding buildings. */
    public static final void _scanBuildings() {
      // Clean up any dead buildings from map.
      if (!S.sensorIsOmnidirectional) {
        for (int dx = -1; dx <= 1; dx++) {
          final int ax = dx + MAP_BASE;
          for (int dy = -1; dy <= 1; dy++) {
            final int ay = dy + MAP_BASE;
            _buildingMap[ax][ay] = _mineMap[ax][ay];
          }
        }
      }
      for (int i = M.sensorDx.length - 1; i >= 0; i--) {
        final int ax = M.sensorDx[i] + MAP_BASE;
        final int ay = M.sensorDy[i] + MAP_BASE;
        _buildingMap[ax][ay] = _mineMap[ax][ay];
      }
      
      // Put buildings on map.
      final RobotInfo[] infos = S.nearbyRobotInfos();
      final int deltaX = S.locationX - MAP_BASE;
      final int deltaY = S.locationY - MAP_BASE;
      for (int i = infos.length - 1; i >= 0; i--) {
        final RobotInfo info = infos[i];
        if (info.chassis == Chassis.BUILDING ||
            info.chassis == Chassis.DEBRIS || info.on == false) {
          final int ax = info.location.x - deltaX;
          final int ay = info.location.y - deltaY;
          _buildingMap[ax][ay] = true;
        }
      }
      _buildingMap[MAP_BASE][MAP_BASE] = true;
    }    
    
    /** Updates {@link #_mineMap} to reflect surrounding mines. */
    public static final void _scanMines() {
      if (!_scannedMinesWithBSensor && S.buildingSensorController != null) {
        _scanMinesWith(S.buildingSensorController);
        _scannedMinesWithBSensor = true;
      }
      if (S.sensorTypeInt != XComponentType.BUILDING_SENSOR_INT) {
        if (!_scannedMinesWithSensor[S.directionInt]) {
          _scanMinesWith(S.sensorController);
          _scannedMinesWithSensor[S.directionInt] = true;
        }
      }
    }
    /** Updates {@link #_mineMap} to reflect surrounding mines. */
    public static final void _scanMinesWith(SensorController sensor) {
      final Mine[] mines = sensor.senseNearbyGameObjects(Mine.class);
      final int deltaX = S.locationX - MAP_BASE;
      final int deltaY = S.locationY - MAP_BASE;
      for (int i = mines.length - 1; i >= 0; i--) {
        MapLocation location = mines[i].getLocation();
        _mineMap[location.x - deltaX][location.y - deltaY] = true;
      }
    }
    
    /** The the array offset for the building's location on local maps. */
    public static final int MAP_BASE = SensorRanges.MAX_RANGE;
    /** The size of the local maps. */
    public static final int MAP_SIZE = 2 * SensorRanges.MAX_RANGE + 1;
    
    /**
     * Map of buildings around the base.
     * 
     * The mines are also burned into the building map, since they never move.
     */
    public static final boolean[][] _buildingMap =
        new boolean[MAP_SIZE][MAP_SIZE];
    
    /** Map of mines around the base. */
    public static final boolean[][] _mineMap = new boolean[MAP_SIZE][MAP_SIZE];
    /** True for each direction in which a mine scan has happened. */
    public static final boolean[] _scannedMinesWithSensor =
        new boolean[XDirection.ADJACENT_DIRECTIONS];
    /** True if the building sensor was used to scan mines. */
    public static boolean _scannedMinesWithBSensor;    
  }
}