package heavy1.chaos.pwnall;

import heavy1.core.B;
import heavy1.core.CommandType;
import heavy1.core.D;
import heavy1.core.Role;
import heavy1.core.S;
import heavy1.core.X;
import heavy1.core.xconst.XComponentType;
import heavy1.core.xconst.XDirection;
import battlecode.common.BuildMappings;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;

// This file has some auto-generated parts. After modifying, run
//    bcpm regen src/team050/blocks/chaos/pwnall/VikingBuildBlock.java


public class VikingBuildBlock {
  public static final void setBuildOrder(Role[] roles, MapLocation[] locations) {    
    busy = true;
    _fixedBuildLocations = new boolean[roles.length];
    if (locations != null) {
      _currentBuildLocations = locations;
      for (int i = roles.length - 1; i >= 0; i--)
        _fixedBuildLocations[i] = locations[i] != null;
    } else {
      _currentBuildLocations = new MapLocation[roles.length];
    }
    // TODO(pwnall): update state
    _roles = roles;
    _currentChassis = 0;
    _currentComponent = -1;
    _computeBuildRequirements();
    _robotIDs = new int[roles.length];
    _waitingForChassis = false;
    _waitingForComponents = false;
    _usingRemoteComponents = false;
  }
  
  /**
   * Used when a robot receives a build command via radio.
   * @param chassis
   * @param equipment
   * @param location
   * @param robotID
   */
  public static final void setBuildOrder(Chassis chassis,
      ComponentType[] equipment, MapLocation location, int robotID) {
    // Update state, no upkeep checks.
  }

  /**
   * Cancels a previously placed build order.
   * 
   * This should only be called when {@link VikingBuildBlock#startedBuilding()}
   * is false.
   */
  public static void cancelBuildOrder() {
    busy = false;
  }
  
  /**
   * True if the build order is in progress.
   * 
   * Setting another order while the build order is in progress would be a
   * particularly bad idea, resulting in unequipped chassis or incomplete teams.
   */
  public static final boolean startedBuilding() {
    return busy && (_currentChassis > 0 || _currentComponent != -1 ||
        _waitingForChassis);
  }
  
  /** Progress towards the current building order. */
  public static final boolean async() {
    if (!busy) { return false; }
    
    if (_needsRadio && B.bc == null) { return _buildRadioAsync(); }    
    
    if (!_checkDistributedBuildAnnexes()) {
      return _buildAnnexesAsync();
    }
        
    if (_waitingForChassis) {      
      if (!_checkDistributedChassis()) { return false; }
      _waitingForChassis = false;
      _nextComponent();
    }
    if (_waitingForComponents) {
      if (!_checkDistributedComponents()) { return false; }
      _nextComponent();
      return true;
    }
    
    if (_currentComponent == -1) {
      _radioBuildCommand();

      if (!_okToBuildChassis()) {
        return false;
      }
      if (BuildMappings.canBuild(S.builderType, _roles[_currentChassis].chassis)) {
        return _buildChassisAsync();
      }
      else {
        _waitingForChassis = true;
        return true;
      }
    }
    
    if (!_checkForCurrentRobot()) {
      // The robot walked away before we finished construction. Abort the team.
      D.debug_logException("Robot walked away while under construction");
      busy = false;
      return false;
    }
    
    if (!_okToBuildComponent()) { return false; }
    if (BuildMappings.canBuild(S.builderType,
        _roles[_currentChassis].components[_currentComponent])) {
      return _buildComponentAsync();
    }
    else {
      _usingRemoteComponents = true;
    }
    
    return false;
  }
    
  /** Progress towards building the current chassis in the build order. */
  public static final boolean _buildChassisAsync() {
    final Chassis chassis = _roles[_currentChassis].chassis;
    final RobotLevel level = chassis.level;
    if (!_setBuildLocation(level)) {
      return false;
    }
    try {
      X.build(chassis, _currentBuildLocations[_currentChassis]);
      _nextComponent();
      if (_needsRadio || S.buildingSensorController != null) {
        final SensorController sensor = (S.buildingSensorController != null) ?
            S.buildingSensorController : S.sensorController;
        Robot robot = (Robot)sensor.senseObjectAtLocation(
            _currentBuildLocations[_currentChassis], level);
        _robotIDs[_currentChassis] = robot.getID();        
      }
      return true;
    } catch (GameActionException e) {
      D.debug_logException(e);  // Bytecode overflow.
      return false;
    }
  }
  
  /**
   * Check whether a chassis is built by another builder.
   * @return true if a chassis is built; false otherwise.
   */
  public static final boolean _checkDistributedChassis() {
    try {
      RobotInfo info = S.senseRobotInfo(_currentBuildLocations[_currentChassis], 
                                      _roles[_currentChassis].chassis.level);
      if (info.chassis == _roles[_currentChassis].chassis) {
        _robotIDs[_currentChassis] = info.robot.getID();
        return true;
      }
    } catch (GameActionException e) {
      D.debug_logException(e);  // Bytecode exception or robot left.
    }
    return false;
  }
  
  public static final boolean _checkDistributedComponents() {
    // same idea as above, except abort build if robot walks away
    return false;
  }
  
  public static final boolean _buildAnnexesAsync() {
    // figure out if we need factory, armory, or both
    // check that we can afford to build whatever we need
    
    // if there is't a constructor nearby
    //   check if we can afford to build everything + constructor
    //   build constructor
    // tell constructor to build whatever we need
    return false;
  }
  
  public static final void _radioBuildCommand() {
    if (_roles[_currentChassis].needsDistributedBuild) {
      // Has armory?
      CommandType.BUILD_ORDER.ints[0] = (_armoryLocation == null) ? 0 : 1;
      // Has factory?
      CommandType.BUILD_ORDER.ints[1] = (_factoryLocation == null) ? 0 : 1;
      CommandType.BUILD_ORDER.ints[2] = 1;  // Number of units to build
      CommandType.BUILD_ORDER.ints[3] = _roles[_currentChassis].ordinal();
      
      try {
        B.send(CommandType.BUILD_ORDER.ints);
      } catch (GameActionException e) {
        D.debug_logException(e);  // This shouldn't happen.
      }
    }
  }
  
  public static final void _radioWakeupMessage() {
    // tell the current what it is, and what its other buddies are;
    // the IDs are in robotIDs
  }
  
  /** Progress towards equipping the current component in the build order. */
  public static final boolean _buildComponentAsync() {
    final ComponentType component =
      _roles[_currentChassis].components[_currentComponent];
      try {
        S.builderController.build(component,
            _currentBuildLocations[_currentChassis],
            _roles[_currentChassis].chassis.level);
        _nextComponent();
        return true;
      } catch (GameActionException e) {
        D.debug_logException(e);  // Bytecode overflow.
        return false;
      }    
  }
  
  /** Progress towards building a radio on ourselves. */
  public static final boolean _buildRadioAsync() {
    if (S.builderController.isActive()) { return false; }
    if (S.flux <= ComponentType.ANTENNA.cost + S.totalUpkeep || S.dFlux < 0) {
      return false;
    }

    try {
      S.builderController.build(ComponentType.ANTENNA, S.location, S.level);
      return true;
    } catch (GameActionException e) {
      D.debug_logException(e);
      return false;
    }
  }
  
  /** Checks for the location of build requirements. */
  public static final boolean _checkDistributedBuildAnnexes() {
    // Fast path.
    if (!_needsDistributedBuild) { return true; }
    
    // Find the build deps on the map.
    _armoryLocation = null;
    _factoryLocation = null;
    _constructorLocation = null;
    RobotInfo[] infos = S.nearbyRobotInfos();
    for (int i = infos.length - 1; i >= 0; i--) {
      RobotInfo info = infos[i];
      if (info.robot.getTeam() != S.team) { continue; }
      ComponentType[] components = info.components;
      componentLoop: for (int j = infos.length - 1; j >= 0; j--) {
        switch (components[j].ordinal()) {
        case XComponentType.ARMORY_INT:
          _armoryLocation = info.location;
          break componentLoop;
        case XComponentType.FACTORY_INT:
          _factoryLocation = info.location;
          break componentLoop;
        case XComponentType.CONSTRUCTOR_INT:
          _constructorLocation = info.location;
          break componentLoop;
        }
      }
    }
    
    if (_needsArmory && _armoryLocation == null) { return false; }
    if (_needsFactory && _factoryLocation == null) { return false; }
    return true;
  }
  
  /** Makes sure the robot under construction didn't die or walk away.
   *
   * @return true if the robot currently under construction is where it is
   *         supposed to be
   */
  public static final boolean _checkForCurrentRobot() {
    try {
      final int id = _robotIDs[_currentChassis];
      if (id != 0) {
        final SensorController sensor = (S.buildingSensorController != null) ?
            S.buildingSensorController : S.sensorController;        
        GameObject object = sensor.senseObjectAtLocation(_currentBuildLocations[_currentChassis],
            _roles[_currentChassis].chassis.level);
        if (object instanceof Robot) {
          Robot robot = (Robot)object;
          return (robot.getID() == id);          
        } else {
          return false;
        }
      } else {
        return true;
      }
    }
    catch (GameActionException e) {
      D.debug_logException(e);
      return false;
    }
  }
  
  /** Advances the component / chassis pointers after something is built. */
  public static final void _nextComponent() {
    if (_waitingForComponents) {
      _waitingForComponents = false;
    }
    else {
      _currentComponent++;
    }
    if (_currentComponent == _roles[_currentChassis].components.length) {
      if (_usingRemoteComponents) {
        // Wait for other robots to equip the robot. This method will be called
        // again, and the other branch will be taken, when the robot has all it
        // needs.
        _usingRemoteComponents = false;
        _waitingForComponents = true;
        return;
      }
      
      _currentComponent = -1;
      _radioWakeupMessage();
      _currentChassis++;
      if (_currentChassis == _roles.length) {
        busy = false;
      }        
    }
  }
  
  /** Figures out a good place to build the unit. */
  public static final boolean _setBuildLocation(RobotLevel level) {
    if (_fixedBuildLocations[_currentChassis]) {
      return S.builderController.canBuild(
          S.location.directionTo(_currentBuildLocations[_currentChassis]),
          level);
    }
    Direction direction = S.direction;
    for (int i = XDirection.ADJACENT_DIRECTIONS; i > 0; i--) {
      if (S.builderController.canBuild(direction, level)) {
        _currentBuildLocations[_currentChassis] = S.location.add(direction);
        return true;
      }
      direction = direction.rotateRight();
    }
    return false;
  }
  
  /** True if it's a good time to build the current chassis. */
  public static final boolean _okToBuildChassis() {
    return (S.flux > _chassisCosts[_currentChassis] + S.totalUpkeep &&
            S.dFlux > _chassisUpkeeps[_currentChassis] &&
            !S.builderController.isActive());
  }
  /** True if it's a good time to build the current component. */
  public static final boolean _okToBuildComponent() {
    return !S.builderController.isActive();
  }
  
  /** 
   * Figures out the bill for the current build order.
   * 
   * Sets @link {@link VikingBuildBlock#_chassisCosts} and
   * {@link VikingBuildBlock#_chassisUpkeeps}.
   */
  public static final void _computeBuildRequirements() {
    _chassisCosts = new int[_roles.length];
    _chassisUpkeeps = new int[_roles.length];
    
    // Some of this code is automatically generated. After changing it, run:
    //   bcpm regen src/team050/chaos/pwnall/VBuildBlock.java
    
    _needsArmory = _needsFactory = _needsFactoryOrArmory = _needsRecycler =
                   _needsConstructor = false;
    
    int totalCost = 0;
    int totalUpkeep = 0;
    int needsRecycler = 0, needsFactory = 0, needsArmory = 0, needsFactoryOrArmory = 0, 
        needsConstructor = 0;
    for (int i = _roles.length - 1; i >= 0; i--) {
      final Role r = _roles[i];
      needsRecycler +=  r.needsRecycler;
      needsFactoryOrArmory += r.needsFactoryOrArmory;
      needsFactory += r.needsFactoryOrArmory;            
      needsArmory +=  r.needsArmory;            
      needsConstructor += r.needsConstructor;
      totalCost += r.totalCost;
      _chassisCosts[i] = totalCost;
      totalUpkeep += r.upkeep;
      _chassisUpkeeps[i] = totalUpkeep;
    }
    _needsFactory = needsFactory > 0;
    _needsArmory = needsArmory > 0;
    _needsFactoryOrArmory = needsFactoryOrArmory > 0;
    if (_needsFactoryOrArmory && (_needsFactory || _needsArmory)) {
      // Cut out factory/armory accounting if at least one of them is absolutely
      // needed.
      _needsFactoryOrArmory = false;
    }
    _needsDistributedBuild = _needsFactory || _needsArmory || _needsFactoryOrArmory;
    _needsRadio = _needsDistributedBuild;    
  }
  
  /** Determines what to build when both a factory and an armory would satisfy the requirements. */
  public static boolean biasTowardsFactory;

  /** Locations of build order dependencies. */
  public static MapLocation _armoryLocation, _factoryLocation,
                            _constructorLocation;
  
  
  //$ +gen:source Building.State _
    
  /** True when a build order is in progress. */
  public static boolean busy;
  
  /**
   *  Set when the chassis can't be built locally and has been ordered via
   * radio.
   */
  public static boolean _waitingForChassis;
  
  /**
   * Set when some components can't be built locally and have been ordered via
   * radio.
   */
  public static boolean _waitingForComponents;
  
  /** The build location */
  public static MapLocation[] _currentBuildLocations;
  
  /** True if the user specified a build location. */
  public static boolean[] _fixedBuildLocations;
  
  /** Flux requirements for the current build order. */
  public static int[] _chassisCosts, _chassisUpkeeps;
  
  /** The index of the current chassis in the current build order. */
  public static int _currentChassis;
  
  /** The index of the current component in the current build order. */
  public static int _currentComponent;
  
  /** True if the current robot needs some components built by other robots. */
  public static boolean _usingRemoteComponents;  
  
  /** Robot IDs for all the chassis in the last build order. */
  public static int _robotIDs[];
  
  /** Roles to be assigned to the robots in the current build order. */
  public static Role _roles[];
  
  /** Build order dependencies. */
  public static boolean _needsRadio, _needsFactory, _needsArmory, _needsConstructor, _needsRecycler;
  /** True if either an armory or a factory would satisfy the deps. */
  public static boolean _needsFactoryOrArmory;
  /** True if this robot can't do the job by itself. */
  public static boolean _needsDistributedBuild;
    
  //$ -gen:source
  
  
  /** Stashes the building block's state. */
  public static class Stash {
    //$ +gen:target Building.State _
    
  /** True when a build order is in progress. */
  public static boolean busy;
  
  /**
   *  Set when the chassis can't be built locally and has been ordered via
   * radio.
   */
  public static boolean _waitingForChassis;
  
  /**
   * Set when some components can't be built locally and have been ordered via
   * radio.
   */
  public static boolean _waitingForComponents;
  
  /** The build location */
  public static MapLocation[] _currentBuildLocations;
  
  /** True if the user specified a build location. */
  public static boolean[] _fixedBuildLocations;
  
  /** Flux requirements for the current build order. */
  public static int[] _chassisCosts, _chassisUpkeeps;
  
  /** The index of the current chassis in the current build order. */
  public static int _currentChassis;
  
  /** The index of the current component in the current build order. */
  public static int _currentComponent;
  
  /** True if the current robot needs some components built by other robots. */
  public static boolean _usingRemoteComponents;  
  
  /** Robot IDs for all the chassis in the last build order. */
  public static int _robotIDs[];
  
  /** Roles to be assigned to the robots in the current build order. */
  public static Role _roles[];
  
  /** Build order dependencies. */
  public static boolean _needsRadio, _needsFactory, _needsArmory, _needsConstructor, _needsRecycler;
  /** True if either an armory or a factory would satisfy the deps. */
  public static boolean _needsFactoryOrArmory;
  /** True if this robot can't do the job by itself. */
  public static boolean _needsDistributedBuild;
    
    //$ -gen:target
    
    /**
     * Saves the state of the current build order into the stash.
     * 
     * The call is ignored if no current build order is active. This should not
     * be called if a state is already saved in the stash, as it will overwrite
     * that state, and leave some unfinished robots / teams behind.
     */
    public static final void stash() {
      if (!busy) { return; }
      D.debug_assert(!Stash.busy, "Something is already stashed");
      Stash.busy = true;
      
      //$ +gen:target Building.Stash VikingBuildBlock.Stash._ VikingBuildBlock._
      VikingBuildBlock.Stash._waitingForChassis = 
        VikingBuildBlock._waitingForChassis;
      VikingBuildBlock.Stash._waitingForComponents = 
        VikingBuildBlock._waitingForComponents;
      VikingBuildBlock.Stash._currentBuildLocations = 
        VikingBuildBlock._currentBuildLocations;
      VikingBuildBlock.Stash._fixedBuildLocations = 
        VikingBuildBlock._fixedBuildLocations;
      VikingBuildBlock.Stash._chassisCosts = VikingBuildBlock._chassisCosts;
      VikingBuildBlock.Stash._chassisUpkeeps = VikingBuildBlock._chassisUpkeeps;
      VikingBuildBlock.Stash._currentChassis = VikingBuildBlock._currentChassis;
      VikingBuildBlock.Stash._currentComponent = 
        VikingBuildBlock._currentComponent;
      VikingBuildBlock.Stash._usingRemoteComponents = 
        VikingBuildBlock._usingRemoteComponents;
      VikingBuildBlock.Stash._robotIDs = VikingBuildBlock._robotIDs;
      VikingBuildBlock.Stash._roles = VikingBuildBlock._roles;
      VikingBuildBlock.Stash._needsRadio = VikingBuildBlock._needsRadio;
      VikingBuildBlock.Stash._needsFactory = VikingBuildBlock._needsFactory;
      VikingBuildBlock.Stash._needsArmory = VikingBuildBlock._needsArmory;
      VikingBuildBlock.Stash._needsConstructor = 
        VikingBuildBlock._needsConstructor;
      VikingBuildBlock.Stash._needsRecycler = VikingBuildBlock._needsRecycler;
      VikingBuildBlock.Stash._needsFactoryOrArmory = 
        VikingBuildBlock._needsFactoryOrArmory;
      VikingBuildBlock.Stash._needsDistributedBuild = 
        VikingBuildBlock._needsDistributedBuild;
      //$ -gen:target
    }
    

    /**
     * Loads a previously saved build order state from the stash.
     * 
     * The call will be ignored if nothing is saved on the stash. This will
     * overwrite any in-progress build order, so it really should not be called
     * when {@link VikingBuildBlock#startedBuilding()} is true.
     */
    public static final void apply() {
      if (!Stash.busy) { return; }
      Stash.busy = false;
      
      VikingBuildBlock.busy = true;      
      
      //$ +gen:target Building.Stash VikingBuildBlock._ VikingBuildBlock.Stash._
      VikingBuildBlock._waitingForChassis = 
        VikingBuildBlock.Stash._waitingForChassis;
      VikingBuildBlock._waitingForComponents = 
        VikingBuildBlock.Stash._waitingForComponents;
      VikingBuildBlock._currentBuildLocations = 
        VikingBuildBlock.Stash._currentBuildLocations;
      VikingBuildBlock._fixedBuildLocations = 
        VikingBuildBlock.Stash._fixedBuildLocations;
      VikingBuildBlock._chassisCosts = VikingBuildBlock.Stash._chassisCosts;
      VikingBuildBlock._chassisUpkeeps = VikingBuildBlock.Stash._chassisUpkeeps;
      VikingBuildBlock._currentChassis = VikingBuildBlock.Stash._currentChassis;
      VikingBuildBlock._currentComponent = 
        VikingBuildBlock.Stash._currentComponent;
      VikingBuildBlock._usingRemoteComponents = 
        VikingBuildBlock.Stash._usingRemoteComponents;
      VikingBuildBlock._robotIDs = VikingBuildBlock.Stash._robotIDs;
      VikingBuildBlock._roles = VikingBuildBlock.Stash._roles;
      VikingBuildBlock._needsRadio = VikingBuildBlock.Stash._needsRadio;
      VikingBuildBlock._needsFactory = VikingBuildBlock.Stash._needsFactory;
      VikingBuildBlock._needsArmory = VikingBuildBlock.Stash._needsArmory;
      VikingBuildBlock._needsConstructor = 
        VikingBuildBlock.Stash._needsConstructor;
      VikingBuildBlock._needsRecycler = VikingBuildBlock.Stash._needsRecycler;
      VikingBuildBlock._needsFactoryOrArmory = 
        VikingBuildBlock.Stash._needsFactoryOrArmory;
      VikingBuildBlock._needsDistributedBuild = 
        VikingBuildBlock.Stash._needsDistributedBuild;
      //$ -gen:target
    }    
  }
}
