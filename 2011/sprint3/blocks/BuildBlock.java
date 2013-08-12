package sprint3.blocks;

import sprint3.core.B;
import sprint3.core.CommandType;
import sprint3.core.D;
import sprint3.core.PlayerConstants;
import sprint3.core.S;
import sprint3.core.U;
import sprint3.core.xconst.XComponentType;
import sprint3.core.xconst.XDirection;
import battlecode.common.BuildMappings;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class BuildBlock {
  
  // interface
  public static final void setParameters(int[] roles, boolean needArmory, boolean needFactory, 
      Chassis[] chassis, ComponentType[][] components) {
    _needArmory = needArmory;
    _needFactory = needFactory;
    _chassis = chassis;
    _components = components;
    _numChassisToBuild = chassis.length;
    _numComponentsToBuildPerChassis = components[0].length;
    _fleetCost = BuildUtil.chassisCost(_chassis) + BuildUtil.componentsCost(_components);
    _fleetUpkeep = BuildUtil.chassisUpkeep(chassis);
    _totalCost = (needArmory || needFactory ? _constructorCost : 0) + 
                 (needArmory? _armoryCost : 0) + (needFactory? _factoryCost : 0) + _fleetCost;
    _totalUpkeep = (needArmory || needFactory ? constructorChassis.upkeep : 0) +
                   (needArmory? Chassis.BUILDING.upkeep : 0) + 
                   (needFactory? Chassis.BUILDING.upkeep : 0) + _fleetUpkeep;
    _totalDelay = (needArmory || needFactory ? GameConstants.POWER_WAKE_DELAY : 0) +
                  (needArmory? GameConstants.POWER_WAKE_DELAY : 0) + 
                  (needFactory? GameConstants.POWER_WAKE_DELAY : 0);
  }
  
  /**
   * 
   * @return true if the job is done.
   */
  public static final boolean tryBuilding() {
    if (S.builderController == null || S.builderController.isActive())
      return false;
    try {
      switch (S.builderTypeInt) {
        case XComponentType.CONSTRUCTOR_INT:
          if (_needArmory) {
            if (!_builtArmory) {
              if (!BuildUtil.hasEnoughFlux(_armoryCost + _fleetCost, 
                                           Chassis.BUILDING.upkeep + _fleetUpkeep))
                return false;
              S.builderController.build(Chassis.BUILDING, _armoryLocation);
              _builtArmory = true;
              return false;
            }
            if (_numArmoryComponentsBuilt < armoryComponents.length) {
              if (BuildUtil.tryEquip(armoryComponents[_numArmoryComponentsBuilt], 
                  _armoryLocation, RobotLevel.ON_GROUND))
                _numArmoryComponentsBuilt++;
                return false;
            }
          }
          if (_needFactory) {
            if (!_builtFactory) {
              if (!BuildUtil.hasEnoughFlux(_factoryCost + _fleetCost, 
                                           Chassis.BUILDING.upkeep + _fleetUpkeep))
                return false;
              S.builderController.build(Chassis.BUILDING, _factoryLocation);
              _builtFactory = true;
              return false;
            }
            if (_numFactoryComponentsBuilt < factoryComponents.length) {
              if (BuildUtil.tryEquip(factoryComponents[_numFactoryComponentsBuilt], 
                    _factoryLocation, RobotLevel.ON_GROUND))
                _numFactoryComponentsBuilt++;
                return false;
            }
          }
          return true;
        case XComponentType.ARMORY_INT:
          runBuildingFollower();
          return false;
        case XComponentType.FACTORY_INT:
          runBuildingFollower();
          return false;
        case XComponentType.RECYCLER_INT:
          if ((_needArmory && !_builtArmory) || (_needFactory && !_builtFactory)) {
            if (_constructorLocation == null) {
              if (B.bc == null) {
                BuildUtil.tryEquip(ComponentType.ANTENNA, S.location, RobotLevel.ON_GROUND);
                return false;
              }
              // Recycler building land constructor
              if (!BuildUtil.hasEnoughFlux(_totalCost - _totalDelay, _totalUpkeep))
                return false;
              D.debug_py("flux = " + S.flux + " cost = " + _totalCost);
              Direction constructorDirection = searchConstructorDirection();
              _constructorLocation = S.location.add(constructorDirection);
              S.builderController.build(constructorChassis, _constructorLocation);
              for (int i = 3; i >= 0; i--)
                CommandType.BUILD.ints[i] = -1;
              if (_needArmory) {
                _armoryLocation = S.location.add(constructorDirection.rotateLeft());
                CommandType.BUILD.ints[0] = _armoryLocation.x;
                CommandType.BUILD.ints[1] = _armoryLocation.y;
              }
              if (_needFactory) {
                MapLocation _factoryLocation = S.location.add(constructorDirection.rotateRight());
                CommandType.BUILD.ints[2] = _factoryLocation.x;
                CommandType.BUILD.ints[3] = _factoryLocation.y;
              }
              CommandType.BUILD.ints[4] = _constructorLocation.x;
              CommandType.BUILD.ints[5] = _constructorLocation.y;
              
              B.send(CommandType.BUILD.ints);
              D.debug_py("Built constructor chassis.");
              return false;
            }
            if (numConstructorComponentsBuilt < constructorComponents.length) {
              D.debug_py("Try to equip: " + constructorComponents[numConstructorComponentsBuilt]);
              BuildUtil.tryEquip(constructorComponents[numConstructorComponentsBuilt], 
                  _constructorLocation, constructorChassis.level);
              numConstructorComponentsBuilt++;
              return false;
            }
          }
          if ((_needArmory && !_builtArmory))
            if ((_builtArmory = waitFor(ComponentType.ARMORY)) == false)
              return false;
          if ((_needFactory && !_builtFactory))
            if ((_builtFactory = waitFor(ComponentType.FACTORY)) == false)
              return false;
          runBuildingLeader();
          return false;
        default:
          break;
      }
      return false;
    } catch (Exception e) {
      D.debug_logException(e);
      return false;
    }
  }
  
  // util
  public static final boolean waitFor(ComponentType component) {
    RobotInfo[] ris =S.nearbyRobotInfos();
    for (int i = ris.length - 1; i >= 0; i--) {
      if (ris[i].chassis == Chassis.BUILDING && U.find(ris[i].components, component))
        return true;
    }
    return false;
  }
  public static final Direction searchConstructorDirection() {
    RobotLevel l = RobotLevel.ON_GROUND;
    int numEmptySpacesNeeded = 1 + (_needArmory ? 1 : 0) + (_needFactory ? 1 : 0);
    for (int i = 0; i < 8; i++) {
      Direction dir = XDirection.intToDirection[i];
      if (BuildUtil.canBuild(dir, l) &&  
          BuildUtil.canBuild(XDirection.intToDirection[(i + 7) % 8], l) &&
          (numEmptySpacesNeeded <= 2 || numEmptySpacesNeeded > 2 && BuildUtil.canBuild(XDirection.intToDirection[(i + 1) % 8], l)))
        return dir;
    }
    return null;
  }
  
  public static final void runBuildingLeader() throws Exception {
    if (!_startedNewChassis) {
      

      // check tier one threshold
      if (S.flux < PlayerConstants.tier1FluxThreshold)
        return;
      
      
      CommandType.EQUIP.ints[0] = _chassisIndex;
      _chassisLocation = S.location.add(Direction.NONE);
      if (!isEmpty(_chassisLocation))
        return;
//      if (!S.allowedToBuild(_chassis[_chassisIndex], _components[_chassisIndex]))
//        return;
      if (!BuildUtil.hasEnoughFlux(_chassis[_chassisIndex].cost + _recyclerCost + Chassis.BUILDING.upkeep, _fleetUpkeep))
        return;
      D.debug_py("Has enough flux for the robot.");
      CommandType.EQUIP.ints[1] = _chassisLocation.x;
      CommandType.EQUIP.ints[2] = _chassisLocation.y;
      B.send(CommandType.EQUIP.ints);
      _startedNewChassis = true;
      Chassis chassisToBuild = _chassis[_chassisIndex];
      if (BuildMappings.canBuild(S.builderType, chassisToBuild)) {
        BuildUtil.tryBuild(chassisToBuild, Direction.NONE);
        return;
      }
    }
    while (_componentIndex < _numComponentsToBuildPerChassis) {
      ComponentType component = _components[_chassisIndex][_componentIndex];
      if (component == null || !BuildMappings.canBuild(S.builderType, component)) {
        _componentIndex++;
        continue;
      }
      if (BuildUtil.tryEquip(component, _chassisLocation, _chassis[_chassisIndex].level)) {
        _componentIndex++;
      }
      return;
    }
    _componentIndex = 0;
    _startedNewChassis = false;
    _chassisIndex = (_chassisIndex + 1) % _numChassisToBuild;
  }
  
  public static final boolean isEmpty(MapLocation there) {
    try {
      return S.senseRobot(there, _chassis[_chassisIndex].level) == null;
    } catch (GameActionException e) {
      D.debug_logException(e);
      return true;
    }
  }
  public static final void runBuildingFollower() {
    if (_chassisLocation == null)
      return;
    if (!_startedNewChassis) {
      Chassis chassisToBuild = _chassis[_chassisIndex];
      if (BuildMappings.canBuild(S.builderType, chassisToBuild)) {
        if (BuildUtil.tryBuild(chassisToBuild, S.location.directionTo(_chassisLocation)) != null)
          _startedNewChassis = true;
        return;
      }
    }

    while (_componentIndex < _numComponentsToBuildPerChassis) {
      ComponentType component = _components[_chassisIndex][_componentIndex];
      if (component == null || !BuildMappings.canBuild(S.builderType, component)) {
        _componentIndex++;
        continue;
      }
      if (BuildUtil.tryEquip(component, _chassisLocation, _chassis[_chassisIndex].level)) {
        _componentIndex++;
      }
      return;
    }
    _componentIndex = 0;
    _startedNewChassis = false;
    _chassisLocation = null;
  }
  
  // state
  public static boolean _needArmory;
  public static boolean _needFactory;

  public static Chassis constructorChassis = Chassis.LIGHT;
  public static ComponentType[] constructorComponents = {ComponentType.CONSTRUCTOR, 
      ComponentType.SIGHT, ComponentType.SHIELD};
  public static ComponentType[] armoryComponents = {ComponentType.ARMORY};
  public static ComponentType[] factoryComponents = {ComponentType.FACTORY};

  public static Chassis[] _chassis;
  public static ComponentType[][] _components;
  public static int _chassisIndex = 0, _componentIndex = 0;
  public static MapLocation _chassisLocation;
  public static int _numChassisToBuild;
  public static int _numComponentsToBuildPerChassis;
  
  //For recycler
  public static MapLocation _constructorLocation;
  public static int numConstructorComponentsBuilt = 0;
  public static boolean _builtArmory = false;
  public static boolean _builtFactory = false;
  public static boolean _startedNewChassis = false;
  
  //For constructor
  public static MapLocation _armoryLocation;
  public static int _numArmoryComponentsBuilt = 0;
  public static MapLocation _factoryLocation;
  public static int _numFactoryComponentsBuilt = 0;

  public static int _fleetCost;
  public static double _fleetUpkeep;

  public static int _recyclerCost = Chassis.BUILDING.cost + ComponentType.RECYCLER.cost;
  public static int _armoryCost = Chassis.BUILDING.cost + ComponentType.ARMORY.cost;
  public static int _factoryCost = Chassis.BUILDING.cost + ComponentType.FACTORY.cost;
  public static int _constructorCost = constructorChassis.cost + 
      BuildUtil.componentsCost(constructorComponents);

  public static double _buildersUpkeep = Chassis.LIGHT.upkeep + Chassis.BUILDING.upkeep * 2;

  public static int _totalCost;
  public static double _totalUpkeep;

  public static int _totalDelay = GameConstants.POWER_WAKE_DELAY * 3;
}
