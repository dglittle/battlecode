package team050.blocks.building;

import team050.blocks.TierBlock;
import team050.core.B;
import team050.core.CommandType;
import team050.core.D;
import team050.core.Role;
import team050.core.S;
import team050.core.X;
import team050.core.xconst.XChassis;
import team050.core.xconst.XComponentType;
import team050.core.xconst.XDirection;
import battlecode.common.BuildMappings;
import battlecode.common.BuilderController;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;

// This file has some auto-generated parts. After modifying, run
//    bcpm regen src/team050/blocks/building/BuildBlock.java


/**
 * Low-level build command executor.
 * 
 * This block is capable of building a single unit or team serially. Buildings
 * should generally use high-level code like {@link BuildDriverBlock} to
 * manage the sequencing of multiple build orders.
 * 
 * The {@link #async()} method should be executed by every robot with building
 * capabilities, each round.
 * 
 * To build something, call {@link #setBuildOrder(Role[], MapLocation[])}. This
 * will abort the current build at all costs, and might leave chassis behind.
 * While {@link #startedBuilding()} is false, it safe to swich the build order,
 * or to {@link #cancelBuildOrder()} altogether. The {@link #busy} flag shows
 * whether this block has a build order at all.
 * 
 * {@link #setBuildOrder(Chassis[], ComponentType[][], MapLocation[])} can do
 * custom builds, for example if the components are computed at runtime.
 */
public class BuildBlock {
  
  /** True when a build order is in progress. */
  public static boolean busy;
  
  /** What gets build when both a factory and an armory would work. */
  public static final boolean biasTowardsFactory = false;
  
  /** Sets the current build order. Blows up any in-progress order. */
  public static final void setBuildOrder(Role[] roles, MapLocation[] locations,
                                         Object tag) {
    final int roleCount = roles.length;
    Chassis[] chassis = new Chassis[roleCount];
    ComponentType[][] components = new ComponentType[roleCount][];
    for (int i = roleCount - 1; i >= 0; i--) {
      chassis[i] = roles[i].chassis;
      components[i] = roles[i].components;
    }    
    setBuildOrder(chassis, components, locations, null, false, tag);
  }

  /** Sets the current build order. Blows up any in-progress order. */
  public static final void setBuildOrder(Chassis[] chassis,
      ComponentType[][] components, MapLocation[] locations, Object tag) {
    setBuildOrder(chassis, components, locations, null, false, tag);
  }
  
  /** Equips an existing building. Blows up any in-progress order. */
  public static final void setEquipOrder(Chassis chassis,
      ComponentType[] components, MapLocation location, Object tag) {
    BuildBlock.setBuildOrder(new Chassis[] { chassis },
        new ComponentType[][] { components }, new MapLocation[] { location },
        null, true, tag);
  }
  
  /**
   * Sets the current build order. Blows up any in-progress order.
   * @param chassis the chassis of the robots to be built
   * @param components the gear to be put in each robot
   * @param locations where each robot should be built
   * @param buildMaster the build component of the robot driving the process
   * @param equipOnly if true, the chassis were already built, and only need to
   *                  be equipped
   * @param tag an object that will be available as the _tag propery
   */
  public static final void setBuildOrder(Chassis[] chassis,
      ComponentType[][] components, MapLocation[] locations,
      ComponentType buildMaster, boolean equipOnly, Object tag) {
    busy = true;
    
    // TODO(pwnall): update state
    _chassis = chassis;
    _components = components;
    _targetLocations = locations;
    _buildMaster = buildMaster;
    _equipOnly = equipOnly;
    _tag = tag;

    _computeBuildRequirements();
    _currentRobot = 0;
    _currentComponent = -1;
    if (_targetLocations != null) {
      _currentBuildLocation = _targetLocations[_currentRobot];
    } else {
      _currentBuildLocation = null;
    }
    _robotIDs = new int[chassis.length];
    _waitingForChassis = _equipOnly;
    _waitingForComponents = false;
    _usingRemoteComponents = false;
  }
  
  /**
   * Cancels a previously placed build order.
   * 
   * This should only be called when {@link BuildBlock#startedBuilding()}
   * is false.
   */
  public static void cancelBuildOrder() {
    _deinitBuildOrder();
  }
  
  /**
   * True if the build order is in progress.
   * 
   * Setting another order while the build order is in progress would be a
   * particularly bad idea, resulting in unequipped chassis or incomplete teams.
   */
  public static final boolean startedBuilding() {
    if (_equipOnly) {
      return busy && (_currentRobot > 0 || _currentComponent > 0);
    } else {
      return busy && (_currentRobot > 0 || _currentComponent != -1 ||
          _waitingForChassis);
    }
  }
  
  /** The location where we're trying to build something. Can be null. */
  public static final MapLocation currentBuildLocation() {
    return _currentBuildLocation;
  }
  
  /** Progress towards the current building order. */
  public static final boolean async() {
    debug_setBuildIndicator();
    
    // Location caches have to be updated even when we're not building.
    BasePlanningBlock._checkPeerLocations();
    if (!busy) {
      return false;
    }
    
    // Prerequisites.
    if (_needsRadio && B.bc == null) { return _buildRadioAsync(); }
    if (_needsSensor &&
        S.sensorTypeInt == XComponentType.BUILDING_SENSOR_INT) {
      return _buildSensorAsync(); 
    }

    // Distributed build steps.
    if (!_checkDistributedBuildPeers()) {
      return _buildPeersAsync();
    }
    waitForChassis: do {  // waitForChassis goto target
      if (_waitingForChassis) {
        if (!_checkDistributedChassis()) { return false; }
        _waitingForChassis = false;
        _finishedChassisOrComponent();
      }
      do {  // waitForComponents goto target
        if (_waitingForComponents) {
          // NOTE: since we're building robots one by one, it's OK if only the
          //       build master waits
          if (_buildMaster == null && !_checkDistributedComponents()) {
            return false;
          }
          _finishedChassisOrComponent();
          // NOTE: _nextComponent() unsets _waitingForComponents
          return true;
        }
    
        // Local chassis build.
        if (_currentComponent == -1) {
          if (_buildMaster == null || _shouldBuildChassis()) {
            if (!_setBuildLocation()) { return false; }
            if (!_okToBuildChassis()) { return false; }
          }
          
          if (_stepNeedsPeers[_currentRobot] && _buildMaster == null) {
            // NOTE: this is a bit of a hack; the method will check if we can
            //       proceed with this build step, even though we're not
            //       building any chassis
            if (!_okToBuildChassis()) { return false; }
            //if (_nextRadioRound <= S.round) {
              _radioBuildOrderStep();
            //} else {
            //  return false;
            //}
          }
    
          if (_shouldBuildChassis()) {
            return _buildChassisAsync();
          } else {
            _waitingForChassis = true;
            continue waitForChassis;
          }
        } else if (_equipOnly && _currentComponent == 0) {
          if (_stepNeedsPeers[_currentRobot] && _buildMaster == null) {
            //if (_nextRadioRound <= S.round) {
              _radioBuildOrderStep();
            //} else {
            //  return false;
            //}
          }
        }
        
        // Local component build.
        if (!_checkCurrentRobot()) {
          // The robot walked away before we finished construction. Abort the team.
          D.debug_logException("Robot walked away while under construction");
          _deinitBuildOrder();
          return false;
        }
        while (!_waitingForComponents) {
          if (_shouldBuildComponent()) {
            if (!_okToBuildComponent()) { return false; }
            // NOTE: not returning all the time so we can loop through the
            //       remaining components and clear the busy flag if we can
            //       proceed to the next order
            _buildComponentAsync();
            if (!busy || _currentComponent == -1) { return true; }
          } else {
            _usingRemoteComponents = true;
            _finishedChassisOrComponent();
          }
        }
      } while (_waitingForComponents);  // ends waitForComponents goto block
      return false;
    } while (true);  // ends waitForChassis block 
  }
  
  /** Sets an indicator string to show how the build is going. */
  public static void debug_setBuildIndicator() {
    if (!busy) {
      D.debug_setIndicator(2, "Not building");
    } else {
      D.debug_setIndicator(2, "Order " + _tag + " chassis " + _currentRobot +
          " component " + _currentComponent + " _waitingFor " +
          _waitingForChassis + "/" + _waitingForComponents);
    }
  }

  /** Sets an indicator string to show where base pieces are supposed to go. */
  public static void debug_setBaseConfigIndicator() {
    D.debug_setIndicator(0, "base factory " + BasePlanningBlock._plannedFactoryLocation +
        " base armory " + BasePlanningBlock._plannedArmoryLocation +
        " base spawn " + BasePlanningBlock._plannedSpawnPoint);    
  }

  /** Processes a radio message. */
  public static final void _onMessage(int[] ints) {
    if (ints[0] != S.id && ints[1] != S.id) { return; }

    if (busy) {
      if (_tag == _radioTag) { return; }
            
      // TODO(pwnall): should stash the new order if possible
      /*
      if (startedBuilding()) { return; }
      if (Stash.busyStash) { return; }
      Stash.stash();
      */
    }
    
    final int robotCount = ints[2];
    final Chassis[] chassis = new Chassis[robotCount];
    final ComponentType[][] components = new ComponentType[robotCount][];
    final MapLocation[] locations = new MapLocation[robotCount];
    int offset = 3;
    for (int i = 0; i < robotCount; i++) {
      locations[i] = new MapLocation(ints[offset], ints[offset + 1]);
      chassis[i] = XChassis.intToChassis[ints[offset + 2]];
      final int componentCount = ints[offset + 3];
      final ComponentType[] botComponents = new ComponentType[componentCount];
      components[i] = botComponents;
      offset += 4;
      for (int j = componentCount - 1; j >= 0; j--) {
        botComponents[j] = XComponentType.intToComponentType[ints[offset + j]];
      }
      offset += componentCount;
    }
    
    BuildBlock.setBuildOrder(chassis, components, locations,
        ComponentType.RECYCLER, false, _radioTag);
  }
    
  /** Progress towards building the current chassis in the build order. */
  public static final boolean _buildChassisAsync() {
    final Chassis chassis = _chassis[_currentRobot];
    final RobotLevel level = chassis.level;

    if (!S.builderController.canBuild(chassis, _currentBuildLocation)) {
      // Something's sitting on the spawn point. Let's hope it goes away.
      
      // TODO(pwnall): try to turn it on if it's off, so it can gtfo
      return false;
    }

    try {
      X.build(chassis, _currentBuildLocation);
      _finishedChassisOrComponent();
      if (_needsRadio || S.buildingSensorController != null) {
        final SensorController sensor = (S.buildingSensorController != null) ?
            S.buildingSensorController : S.sensorController;
        Robot robot = (Robot)sensor.senseObjectAtLocation(
            _currentBuildLocation, level);
        _robotIDs[_currentRobot] = robot.getID();
      }
      return true;
    } catch (GameActionException e) {
      D.debug_logException(e);  // Bytecode overflow.
      return false;
    }
  }
  
  /** Role for the robot that can build peers housed in buildings. */
  public static Role SCV_ROLE = Role.COLONIST;
  /** Role for the factory build peer. */
  public static Role FACTORY_ROLE = Role.FACTORY;
  /** Role for the armory build peer. */
  public static Role ARMORY_ROLE = Role.ARMORY;
  /** Sensor used by the recycler to see farther and build out on tough maps. */
  public static final ComponentType[][] SENSOR_ORDER =
      { { ComponentType.RADAR } };
  /** Radio used by the recycler to transmit build commands. */
  public static final ComponentType[][] RADIO_ORDER =
      { { ComponentType.ANTENNA } };
  
  /** Checks if the robot we're waiting for has been built. */
  public static final boolean _checkDistributedChassis() {
    final Chassis chassis = _chassis[_currentRobot];
    final RobotLevel level = chassis.level;
    final SensorController sensor = (S.buildingSensorController != null &&
        _currentBuildLocation.isAdjacentTo(S.location)) ?
            S.buildingSensorController : S.sensorController;

    try {
      Robot robot = (Robot)sensor.senseObjectAtLocation(_currentBuildLocation,
                                                        level);
      if (robot == null || robot.getTeam() != S.team) { return false; }
      RobotInfo info = sensor.senseRobotInfo(robot);
      if (info == null || info.chassis != chassis) {
        return false;
      }
      
      // HACK(pwnall): one of our guys might jump into the spawn spot; detect by
      //               assuming that they can't get all their weight at once
      int leftoverWeight = chassis.weight;
      final ComponentType[] components = info.components;
      final int componentCount = components.length;
      for (int i = componentCount - 1; i >= 0; i--) {
        leftoverWeight -= components[i].weight;
      }
      if (leftoverWeight == 0) { return false; }
      
      _robotIDs[_currentRobot] = robot.getID();
      // NOTE: if we're equipping an existing unit / building, we have to
      //       account for the gear that it already has
      if (_equipOnly) {
        _stepComponentCount[_currentRobot] = componentCount
            + _components[_currentRobot].length;
      }
      return true;
    } catch (GameActionException e) {
      D.debug_logException(e);  // Bytecode exception or robot left.
    }
    return false;
  }
  
  /** Checks if the robot we're building received all its components. */
  public static final boolean _checkDistributedComponents() {
    final Chassis chassis = _chassis[_currentRobot];
    final RobotLevel level = chassis.level;
    final SensorController sensor = (S.buildingSensorController != null &&
        _currentBuildLocation.isAdjacentTo(S.location)) ?
            S.buildingSensorController : S.sensorController;

    try {
      Robot robot = (Robot)sensor.senseObjectAtLocation(
          _currentBuildLocation, level);
      if (robot == null || robot.getID() != _robotIDs[_currentRobot]) {
        D.debug_logException("Robot walked away while under construction");
        _deinitBuildOrder();
        return false;
      }      
      RobotInfo info = sensor.senseRobotInfo(robot);
      return info.components.length >= _stepComponentCount[_currentRobot];
    } catch (GameActionException e) {
      D.debug_logException(e);  // Bytecode exception or robot left.
    }
    return false;
  }
  
  /**
   * Starts building peers needed to complete this order.
   * @return true if an action is taken towards building
   */
  public static final boolean _buildPeersAsync() {
    // NOTE: sets the configuration implicitly
    if (!BasePlanningBlock.canBuildBrain()) {
      if (S.sensorTypeInt == XComponentType.BUILDING_SENSOR_INT) {
        BuildBlock._needsSensor = true; // BuildBlock._nextRadioRound <= S.round;
      }
      return false;
    }
    if (!_okToBuildPeers()) { return false; }

    if (BasePlanningBlock._scvLocation == null) {
      return _buildScvAsync();
    }
    if (B.bc.isActive() /* || S.round < _nextRadioRound */) { return false; }
    return _radioScvBuildOrder();
  }

  /** Progress towards building an SCV that will build other peers. */
  public static final boolean _buildScvAsync() {
    _nextRadioRound = S.round + 10; // GameConstants.WAKE_DELAY;
    Stash.stash();
    setBuildOrder(new Role[] { SCV_ROLE },
        new MapLocation[] { BasePlanningBlock._plannedSpawnPoint }, _scvTag);
    async();
    return true;    
  }
  
  /** Checks that we have enough resources to cover build deps. */
  public static final boolean _okToBuildPeers() {
    double cost = _stepCost[_currentRobot];
    double upkeep = _stepUpkeep[_currentRobot];
    double peerUpkeep = 0;
    
    // Peers take 10 rounds to wake up, so we account for that upkeep.
    // NOTE: we can build both a factory and an armory quickly, but we're
    //       accounting for the time it'll take the SCV to get there
    
    if (BasePlanningBlock._scvLocation == null) {
      cost += SCV_ROLE.totalCost;
      peerUpkeep += SCV_ROLE.upkeep;
    }
    if (_buildFactory) {
      cost += FACTORY_ROLE.totalCost + 11 * SCV_ROLE.upkeep;
      peerUpkeep += FACTORY_ROLE.upkeep;
    }
    if (_buildArmory) {
      cost += FACTORY_ROLE.totalCost + 11 * SCV_ROLE.upkeep;
      peerUpkeep += ARMORY_ROLE.upkeep;
    }
    cost += 11 * peerUpkeep;
    upkeep += peerUpkeep;
    // D.debug_setIndicator(1, "cost: " +  (cost + _upkeepEstimate()) + " upkeep: " + upkeep);
    return S.flux > cost + _upkeepEstimate() && S.dFlux > upkeep;
  }
  
  /** Sends a distributed build command for one step in the building order. */
  public static final void _radioBuildOrderStep() {
    final int[] message = CommandType.BUILD.ints;
    
    // NOTE: using our ID as an invalid address, since we won't be getting this
    message[0] = _willUseArmory ? BasePlanningBlock._armoryID : S.id;
    message[1] = _willUseFactory ? BasePlanningBlock._factoryID : S.id;
    message[2] = 1;  // Number of units to build
    message[3] = _currentBuildLocation.x;
    message[4] = _currentBuildLocation.y;
    message[5] = _chassis[_currentRobot].ordinal();
    final ComponentType[] components = _components[_currentRobot];
    final int componentCount = components.length;
    message[6] = componentCount;
    for (int i = componentCount - 1; i >= 0; i--) {
      message[7 + i] = components[i].ordinal();
    }
    try {
      B.send(message);
      _nextRadioRound = S.round + 3;
    } catch (GameActionException e) {
      D.debug_logException(e);  // This shouldn't happen.
    }
  }
  
  /**
   * Tells an SCV to build the peers that we need.
   * 
   * @return true for success, false in case we hit an exception
   */
  public static final boolean _radioScvBuildOrder() {
    final int[] message = CommandType.BUILD.ints;
    
    // NOTE: using our ID as an invalid address, since we won't be getting this
    message[0] = BasePlanningBlock._scvID;
    message[1] = S.id;
    if (_buildArmory && _buildFactory) {
      message[2] = 2;  // Number of units to build.
      message[3] = BasePlanningBlock._plannedArmoryLocation.x;
      message[4] = BasePlanningBlock._plannedArmoryLocation.y;
      message[5] = ARMORY_ROLE.chassis.ordinal();
      message[6] = 1;  // Components count for the unit.
      message[7] = ARMORY_ROLE.components[0].ordinal();
      
      message[8] = BasePlanningBlock._plannedFactoryLocation.x;
      message[9] = BasePlanningBlock._plannedFactoryLocation.y;      
      message[10] = FACTORY_ROLE.chassis.ordinal();
      message[11] = 1;  // Components count for the unit.
      message[12] = FACTORY_ROLE.components[0].ordinal();
    } else {
      message[2] = 1;  // Number of units to build.
      if (_buildArmory) {
        message[3] = BasePlanningBlock._plannedArmoryLocation.x;
        message[4] = BasePlanningBlock._plannedArmoryLocation.y;        
        message[5] = ARMORY_ROLE.chassis.ordinal();
        message[6] = 1;  // Components count for the unit.
        message[7] = ARMORY_ROLE.components[0].ordinal();
      } else {
        message[3] = BasePlanningBlock._plannedFactoryLocation.x;
        message[4] = BasePlanningBlock._plannedFactoryLocation.y;        
        message[5] = FACTORY_ROLE.chassis.ordinal();
        message[6] = 1;  // Components count for the unit.
        message[7] = FACTORY_ROLE.components[0].ordinal();
      }
    }
    try {
      B.send(message);
      _nextRadioRound = S.round + 8;
      return true;
    } catch (GameActionException e) {
      D.debug_logException(e);
      return false;
    }
  }
  
  public static final void _radioWakeupMessage() {
    // tell the current what it is, and what its other buddies are;
    // the IDs are in robotIDs
  }
  
  /** Progress towards equipping the current component in the build order. */
  public static final boolean _buildComponentAsync() {
    final ComponentType component =
        _components[_currentRobot][_currentComponent];
    try {
      S.builderController.build(component, _currentBuildLocation,
          _chassis[_currentRobot].level);
      _finishedChassisOrComponent();
      return true;
    } catch (GameActionException e) {
      D.debug_logException(e);
      if (e.getType() == GameActionExceptionType.NO_ROOM_IN_CHASSIS) {
        // An existing robot got into the spawn point and we didn't notice.
        cancelBuildOrder();
        return true;
      } else {
        // Bytecode overflow or resource contention.
        return false;
      }
    }    
  }
  
  /** Progress towards building a radio on ourselves. */
  public static final boolean _buildRadioAsync() {
    Stash.stash();
    setBuildOrder(_baseEquipChassis, RADIO_ORDER,
        new MapLocation[] { S.location }, null, true, _buildRadioTag);
    return async();
  }
  
  /** Progress towards building a radio on ourselves. */
  public static final boolean _buildSensorAsync() {
    Stash.stash();
    _needsSensor = false;
    setBuildOrder(_baseEquipChassis, SENSOR_ORDER,
        new MapLocation[] { S.location }, null, true, _buildSensorTag);
    return async();
  }
  
  
  /** Checks for the locations of the other builders needed for this order.
   * 
   * @return true if all the needed peers exist, false if peers must be built.
   */
  public static final boolean _checkDistributedBuildPeers() {
    // Fast path.
    if (!_needsDistributedBuild) { return true; }

    // Don't scan if we already have everything we need.
    if (_setNeededPeers()) { return true; }
    BasePlanningBlock._scanForPeers();
    return _setNeededPeers();
  }
  
  /**
   * Computes the set of needed build peers given current knowledge.
   * 
   * @return true if nothing needs to be built.
   */
  public static final boolean _setNeededPeers() {
    _buildArmory = _buildFactory = false;
    if (_needsFactoryOrArmory && BasePlanningBlock._factoryLocation == null
        && BasePlanningBlock._armoryLocation == null) {
      if (biasTowardsFactory) {
        _buildFactory = true;
      } else {
        _buildArmory = true;
      }
      return false;
    }
    if (_needsArmory && BasePlanningBlock._armoryLocation == null) { _buildArmory = true; }
    if (_needsFactory && BasePlanningBlock._factoryLocation == null) { _buildFactory = true; }
    
    final boolean stepNeedsArmory = _stepNeedsArmory[_currentRobot];
    final boolean stepNeedsFactory = _stepNeedsFactory[_currentRobot];
    if (stepNeedsArmory || stepNeedsFactory) {
      _willUseArmory = stepNeedsArmory;
      _willUseFactory = stepNeedsFactory;
    } else if (_stepNeedsFactoryOrArmory[_currentRobot]) {
      if (BasePlanningBlock._factoryLocation != null || _buildFactory) {
        _willUseFactory = true;
        _willUseArmory = false;
      } else {
        _willUseFactory = false;
        _willUseArmory = true;
      }
    }
    
    return !(_buildArmory || _buildFactory);    
  }
  
  /** 
   * Makes sure the robot under construction didn't die or walk away.
   *
   * @return true if the robot we're equipping is still where we built it
   */
  public static final boolean _checkCurrentRobot() {
    final SensorController sensor = (S.buildingSensorController != null) ?
        S.buildingSensorController : S.sensorController;        

    // SCVs don't have building sensors, and they turn around while they build.
    if (!S.sensorIsOmnidirectional &&
        !sensor.canSenseSquare(_currentBuildLocation)) {
      return true;
    }
    
    final RobotLevel level = _chassis[_currentRobot].level;
    try {
      GameObject object = sensor.senseObjectAtLocation(
          _currentBuildLocation, level);
      if (object == null) { return false; }
      final int id = _robotIDs[_currentRobot];     
      if (id == 0 || object.getID() == id) {
        final RobotInfo info = sensor.senseRobotInfo((Robot)object);
        if (!info.on) {
          S.rc.turnOn(_currentBuildLocation, level);
          D.debug_logException("Robot turned off while under construction");
        }
        return true;
      } else {
        return false;
      }
    }  catch (GameActionException e) {
      D.debug_logException(e);
      // If we couldn't sense the square, chances are the robot left.
      return false;
    }
  }
  
  /** Advances the component / chassis pointers after something is built. */
  public static final void _finishedChassisOrComponent() {
    if (_waitingForComponents) {
      _waitingForComponents = false;
    }
    else {
      _currentComponent++;
    }
    if (_currentComponent == _components[_currentRobot].length) {
      if (_usingRemoteComponents) {
        // Wait for other robots to equip the robot. This method will be called
        // again, and the other branch will be taken, when the robot has all it
        // needs.
        _usingRemoteComponents = false;
        _waitingForComponents = true;
        return;
      }
      
      _currentComponent = -1;
      if (_buildMaster == null) {
        _radioWakeupMessage();
      }
      _currentRobot++;
      if (_currentRobot == _chassis.length) {
        _deinitBuildOrder();
      } else {
        if (_targetLocations != null) {
          _currentBuildLocation = _targetLocations[_currentRobot];
        } else {
          _currentBuildLocation = null;
        }
        _waitingForChassis = _equipOnly;
      }
    }
  }

  /** Called when the build order has completed or aborted. */
  public static final void _deinitBuildOrder() {
    _currentBuildLocation = null;
    _tag = null;
    busy = false;
    Stash.apply();
  }
  
  /** Figures out a good place to build the unit. */
  public static final boolean _setBuildLocation() {
    final Chassis chassis = _chassis[_currentRobot];
    if (_targetLocations != null && _targetLocations[_currentRobot] != null) {
      return _canBuild(chassis, _currentBuildLocation);
    }
    
    Direction direction = S.direction;
    for (int i = XDirection.ADJACENT_DIRECTIONS; i > 0; i--) {
      final MapLocation buildLocation = S.location.add(direction);
      if (_canBuild(chassis, buildLocation)) {
        if (!_willUseArmory || buildLocation.isAdjacentTo(BasePlanningBlock._armoryLocation)) {
          if (!_willUseFactory ||
              buildLocation.isAdjacentTo(BasePlanningBlock._factoryLocation)) {
            _currentBuildLocation = buildLocation;
            return true;
          }
        }
      }
      direction = direction.rotateRight();
    }
    return false;
  }
  
  /**
   * Wraps {@link BuilderController#canBuild(Chassis, MapLocation)} but works.
   */
  public static final boolean _canBuild(Chassis chassis, MapLocation location) {
    if (chassis.level == RobotLevel.ON_GROUND) {
      return _canBuildOnLand(location);
    }
    try {
      return S.buildingSensorController != null &&
          S.buildingSensorController.senseObjectAtLocation(location,
              RobotLevel.IN_AIR) == null;
    } catch (GameActionException e) {
      D.debug_logException(e);
      return false;
    }
  }
  
  /** True if we can build a land unit at the given location. */
  public static final boolean _canBuildOnLand(MapLocation location) {
    switch (S.builderTypeInt) {
    case XComponentType.RECYCLER_INT:
    case XComponentType.FACTORY_INT:
    case XComponentType.ARMORY_INT:
      return S.builderController.canBuild(Chassis.LIGHT, location);
    case XComponentType.CONSTRUCTOR_INT:
      return S.builderController.canBuild(Chassis.BUILDING, location);      
    default:
      D.debug_logException("Unhandled builder type " + S.builderTypeInt);
      return false;
    }    
  }
  
  /** True if it's a good time to build the current chassis. */
  public static final boolean _okToBuildChassis() {
    if (_currentRobot == 0 && _buildMaster == null) {
      return S.flux > _stepCost[_currentRobot] + _upkeepEstimate() &&
             S.dFlux > _stepUpkeep[_currentRobot] +
                       TierBlock.tieredDFluxMargin()  &&
             !S.builderController.isActive();
    } else {
      // NOTE: The build master was responsible for checking dFlux.
      return S.flux > _stepCost[_currentRobot] + _upkeepEstimate() &&
             !S.builderController.isActive();
    }
  }
  /** True if it's a good time to build the current component. */
  public static final boolean _okToBuildComponent() {
    return S.flux > _components[_currentRobot][_currentComponent].cost +
        _upkeepEstimate() && !S.builderController.isActive();
  }
  
  /** An estimate of our current upkeep. */
  public static final double _upkeepEstimate() {
    switch (S.builderTypeInt) {
      case XComponentType.RECYCLER_INT:
        return S.totalUpkeep;
      case XComponentType.CONSTRUCTOR_INT:
        return TierBlock.tieredRecylerMargin();
      default:
        return TierBlock.tieredUnitMargin();
    }
  }

  /**
   * True if this robot should build the current chassis.
   * 
   * This is only false in distributed builds.
   */
  public static final boolean _shouldBuildChassis() {
    if (_equipOnly) { return false; }
    final Chassis chassis = _chassis[_currentRobot];
    if (_buildMaster != null) {
      if (BuildMappings.canBuild(_buildMaster, chassis)) { return false; }
    }
    return BuildMappings.canBuild(S.builderType, chassis);
  }
  
  /** 
   * True if this robot should equip the current component.
   * 
   * This is only false in distributed builds.
   */
  public static final boolean _shouldBuildComponent() {
    final ComponentType component =
        _components[_currentRobot][_currentComponent];
    if (_buildMaster != null) {
      if (BuildMappings.canBuild(_buildMaster, component)) { return false; }
    }
    return BuildMappings.canBuild(S.builderType, component);
  }
  
  /** 
   * Figures out the bill for the current build order.
   * 
   * Sets @link {@link BuildBlock#_chassisCosts} and
   * {@link BuildBlock#_chassisUpkeeps}.
   */
  public static final void _computeBuildRequirements() {
    int stepCount = _chassis.length;
    _stepCost = new double[stepCount];
    _stepUpkeep = new double[stepCount];
    _stepComponentCount = new int[stepCount];
    _stepNeedsPeers = new boolean[stepCount];
    _stepNeedsArmory = new boolean[stepCount];
    _stepNeedsFactory = new boolean[stepCount];
    _stepNeedsFactoryOrArmory = new boolean[stepCount];
    
    // Some of this code is automatically generated. After changing it, run:
    //   bcpm regen src/team050/chaos/pwnall/VBuildBlock.java

    double totalCost = 0, totalUpkeep = 0;
    _needsArmory = _needsFactory = _needsFactoryOrArmory = false;
    for (int i = _chassis.length - 1; i >= 0; i--) {
      final Chassis chassis = _chassis[i];
      final ComponentType[] components = _components[i];

      boolean needsFactory = false, needsArmory = false,
              needsFactoryOrArmory = false;
      
      final int componentCount = components.length;
      // NOTE: all robots have a motor by default, buildings have a sensor too
      _stepComponentCount[i] = componentCount +
          (chassis == Chassis.BUILDING ? 2 : 1);
      
      int stepCost = chassis.cost;
      for (int j = componentCount - 1; j >= 0; j--) {
        final ComponentType component = components[j];
        stepCost += component.cost;
        //$ +gen:source BuildBlock.Requirement component
        if (!BuildMappings.canBuild(S.builderType, component)) {
          if (BuildMappings.canBuild(ComponentType.FACTORY, component)) {
            if (BuildMappings.canBuild(ComponentType.ARMORY, component)) {
              needsFactoryOrArmory = true;
            } else {
              needsFactory = true;
            }
          } else if (BuildMappings.canBuild(ComponentType.ARMORY, component)) {
            needsArmory = true;
          } else {
            // TODO(pwnall): add needsConstructor logic to build towers
          }
        }
        //$ -gen:source
      }

      if (!_equipOnly) {
        //$ +gen:target BuildBlock.Requirement chassis
        if (!BuildMappings.canBuild(S.builderType, chassis)) {
          if (BuildMappings.canBuild(ComponentType.FACTORY, chassis)) {
            if (BuildMappings.canBuild(ComponentType.ARMORY, chassis)) {
              needsFactoryOrArmory = true;
            } else {
              needsFactory = true;
            }
          } else if (BuildMappings.canBuild(ComponentType.ARMORY, chassis)) {
            needsArmory = true;
          } else {
            // TODO(pwnall): add needsConstructor logic to build towers
          }
        }
        //$ -gen:target
      }
      
      _stepNeedsPeers[i] = needsFactory || needsArmory || needsFactoryOrArmory;

      totalCost += stepCost;
      _stepCost[i] = totalCost;
      totalUpkeep += chassis.upkeep;
      _stepUpkeep[i] = totalUpkeep;

      _stepNeedsArmory[i] = needsArmory;
      _needsArmory = _needsArmory || needsArmory;
      _stepNeedsFactory[i] = needsFactory;
      _needsFactory = _needsFactory || needsFactory;
      if (needsArmory || needsFactory) {
        needsFactoryOrArmory = false;
      }
      _stepNeedsFactoryOrArmory[i] = needsFactoryOrArmory;
      _needsFactoryOrArmory = _needsFactoryOrArmory || needsFactoryOrArmory;
    }
    if (_needsArmory || _needsFactory) {
      _needsFactoryOrArmory = false;
    }
    
    if (_buildMaster != null) {
      _needsDistributedBuild = false;
      _needsRadio = false;
    } else {
      _needsDistributedBuild = _needsFactory || _needsArmory ||
                               _needsFactoryOrArmory;
      _needsRadio = _needsDistributedBuild;
    }
  }
  
  /** The base's chassis, used in orders that equip itself. */
  public static Chassis[] _baseEquipChassis = { Chassis.BUILDING };
  
  /** The peers we'll use to satisfy the current step in the build order. */
  public static boolean _willUseArmory, _willUseFactory;
  
  /** True if we need to build some dependencies to satisfy the build order. */
  public static boolean _buildArmory, _buildFactory;
  
  /**
   * If we radio a command, this gets the round number when we expect the
   * command to be executed.
   */
  public static int _nextRadioRound;
  
  /** The tag for orders received via radio. */
  public static final Object _radioTag = "radio";
  /** The tag for orders received via radio. */
  public static final Object _scvTag = "recylerBuildScv";
  /** The tag for the Recycler's order to build itself a sensor. */
  public static final Object _buildSensorTag = "recyclerBuildSensor";
  /** The tag for the Recycler's order to build itself a radio. */
  public static final Object _buildRadioTag = "recyclerBuildRadio";  
  
  
  //$ +gen:source Building.State _    
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

  /** The build location for the robot currently under construction. */
  public static MapLocation _currentBuildLocation;

  /** The build locations specified by the user. */
  public static MapLocation[] _targetLocations;
  
  /**
   * Flux requirements for each step in the current build order.
   * 
   * These are tresholds. Each of them sums up the requirements for the current
   * step, as well as for the following steps.
   */
  public static double[] _stepCost, _stepUpkeep;

  /** The number of components expected in the robots for each step. */
  public static int[] _stepComponentCount;
  
  /** Peer requirements for each step in the current build order. */
  public static boolean[] _stepNeedsFactory, _stepNeedsArmory,
                          _stepNeedsFactoryOrArmory, _stepNeedsPeers;
  
  /** The index of the current robot in the current build order. */
  public static int _currentRobot;
  
  /** The index of the current component in the current build order. */
  public static int _currentComponent;
  
  /** True if the current robot needs some components built by other robots. */
  public static boolean _usingRemoteComponents;  
  
  /** Robot IDs for all the chassis in the last build order. */
  public static int[] _robotIDs;
  
  /** The chassis types for the robots in the current build order. */
  public static Chassis[] _chassis;

  /** Roles to be assigned to the robots in the current build order. */
  public static ComponentType[][] _components;
  
  /** True if the build order doesn't require new chassis. */
  public static boolean _equipOnly;
  
  /** Tag for the build issuer to recognize its order. */
  public static Object _tag;
  
  /** Build order dependencies. */
  public static boolean _needsRadio, _needsFactory, _needsArmory,
                        _needsConstructor, _needsRecycler, _needsSensor;
  /** True if either an armory or a factory would satisfy the deps. */
  public static boolean _needsFactoryOrArmory;
  /** True if this robot can't do the job by itself. */
  public static boolean _needsDistributedBuild;

  /** If not null, the build is driven by another robot with this component. */
  public static ComponentType _buildMaster;
    
  //$ -gen:source

  /** Stashes the building block's state. */
  public static final class Stash {
    /** True when a build order is in progress. */
    public static boolean busyStash;
    
    //$ +gen:target Building.State _    
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

  /** The build location for the robot currently under construction. */
  public static MapLocation _currentBuildLocation;

  /** The build locations specified by the user. */
  public static MapLocation[] _targetLocations;
  
  /**
   * Flux requirements for each step in the current build order.
   * 
   * These are tresholds. Each of them sums up the requirements for the current
   * step, as well as for the following steps.
   */
  public static double[] _stepCost, _stepUpkeep;
  
  /** The number of components expected in the robots for each step. */
  public static int[] _stepComponentCount;
  
  /** Peer requirements for each step in the current build order. */
  public static boolean[] _stepNeedsFactory, _stepNeedsArmory,
                          _stepNeedsFactoryOrArmory, _stepNeedsPeers;
  
  /** The index of the current robot in the current build order. */
  public static int _currentRobot;
  
  /** The index of the current component in the current build order. */
  public static int _currentComponent;
  
  /** True if the current robot needs some components built by other robots. */
  public static boolean _usingRemoteComponents;  
  
  /** Robot IDs for all the chassis in the last build order. */
  public static int[] _robotIDs;
  
  /** The chassis types for the robots in the current build order. */
  public static Chassis[] _chassis;

  /** Roles to be assigned to the robots in the current build order. */
  public static ComponentType[][] _components;
  
  /** True if the build order doesn't require new chassis. */
  public static boolean _equipOnly;
  
  /** Tag for the build issuer to recognize its order. */
  public static Object _tag;
  
  /** Build order dependencies. */
  public static boolean _needsRadio, _needsFactory, _needsArmory,
                        _needsConstructor, _needsRecycler, _needsSensor;
  /** True if either an armory or a factory would satisfy the deps. */
  public static boolean _needsFactoryOrArmory;
  /** True if this robot can't do the job by itself. */
  public static boolean _needsDistributedBuild;

  /** If not null, the build is driven by another robot with this component. */
  public static ComponentType _buildMaster;
    
    //$ -gen:target
    
    /**
     * Saves the state of the current build order into the stash.
     * 
     * The call is ignored if no current build order is active. This should not
     * be called if a state is already saved in the stash, as it will overwrite
     * that state, and leave some unfinished robots / teams behind.
     */
    public static final void stash() {
      if (!BuildBlock.busy) { return; }
      D.debug_assert(!Stash.busyStash, "Something is already stashed");
      Stash.busyStash = true;
      
      //$ +gen:source BuildingBlock.Stash BuildBlock.Stash._ BuildBlock._
      BuildBlock.Stash._waitingForChassis =  BuildBlock._waitingForChassis;
      BuildBlock.Stash._waitingForComponents =  BuildBlock._waitingForComponents;
      BuildBlock.Stash._currentBuildLocation = BuildBlock._currentBuildLocation;
      BuildBlock.Stash._targetLocations = BuildBlock._targetLocations;
      BuildBlock.Stash._stepCost = BuildBlock._stepCost;
      BuildBlock.Stash._stepUpkeep = BuildBlock._stepUpkeep;
      BuildBlock.Stash._stepComponentCount = BuildBlock._stepComponentCount;      
      BuildBlock.Stash._stepNeedsFactory = BuildBlock._stepNeedsFactory;
      BuildBlock.Stash._stepNeedsArmory = BuildBlock._stepNeedsArmory;
      BuildBlock.Stash._stepNeedsFactoryOrArmory = BuildBlock._stepNeedsFactoryOrArmory;
      BuildBlock.Stash._stepNeedsPeers = BuildBlock._stepNeedsPeers;
      BuildBlock.Stash._currentRobot = BuildBlock._currentRobot;
      BuildBlock.Stash._currentComponent = BuildBlock._currentComponent;
      BuildBlock.Stash._usingRemoteComponents = BuildBlock._usingRemoteComponents;
      BuildBlock.Stash._robotIDs = BuildBlock._robotIDs;
      BuildBlock.Stash._chassis = BuildBlock._chassis;
      BuildBlock.Stash._components = BuildBlock._components;
      BuildBlock.Stash._equipOnly = BuildBlock._equipOnly;
      BuildBlock.Stash._tag = BuildBlock._tag;
      BuildBlock.Stash._needsRadio = BuildBlock._needsRadio;
      BuildBlock.Stash._needsFactory = BuildBlock._needsFactory;
      BuildBlock.Stash._needsArmory = BuildBlock._needsArmory;
      BuildBlock.Stash._needsConstructor = BuildBlock._needsConstructor;
      BuildBlock.Stash._needsRecycler = BuildBlock._needsRecycler;
      BuildBlock.Stash._needsSensor = BuildBlock._needsSensor;
      BuildBlock.Stash._needsFactoryOrArmory = BuildBlock._needsFactoryOrArmory;
      BuildBlock.Stash._needsDistributedBuild = BuildBlock._needsDistributedBuild;
      BuildBlock.Stash._buildMaster = BuildBlock._buildMaster;
      //$ -gen:source
    }
    

    /**
     * Loads a previously saved build order state from the stash.
     * 
     * The call will be ignored if nothing is saved on the stash. This will
     * overwrite any in-progress build order, so it really should not be called
     * when {@link BuildBlock#startedBuilding()} is true.
     */
    public static final void apply() {
      if (!Stash.busyStash) { return; }
      Stash.busyStash = false;
      BuildBlock.busy = true;      
      
      //$ +gen:target BuildingBlock.Stash BuildBlock._ BuildBlock.Stash._
      BuildBlock._waitingForChassis =  BuildBlock.Stash._waitingForChassis;
      BuildBlock._waitingForComponents =  BuildBlock.Stash._waitingForComponents;
      BuildBlock._currentBuildLocation = BuildBlock.Stash._currentBuildLocation;
      BuildBlock._targetLocations = BuildBlock.Stash._targetLocations;
      BuildBlock._stepCost = BuildBlock.Stash._stepCost;
      BuildBlock._stepUpkeep = BuildBlock.Stash._stepUpkeep;
      BuildBlock._stepComponentCount = BuildBlock.Stash._stepComponentCount;      
      BuildBlock._stepNeedsFactory = BuildBlock.Stash._stepNeedsFactory;
      BuildBlock._stepNeedsArmory = BuildBlock.Stash._stepNeedsArmory;
      BuildBlock._stepNeedsFactoryOrArmory = BuildBlock.Stash._stepNeedsFactoryOrArmory;
      BuildBlock._stepNeedsPeers = BuildBlock.Stash._stepNeedsPeers;
      BuildBlock._currentRobot = BuildBlock.Stash._currentRobot;
      BuildBlock._currentComponent = BuildBlock.Stash._currentComponent;
      BuildBlock._usingRemoteComponents = BuildBlock.Stash._usingRemoteComponents;
      BuildBlock._robotIDs = BuildBlock.Stash._robotIDs;
      BuildBlock._chassis = BuildBlock.Stash._chassis;
      BuildBlock._components = BuildBlock.Stash._components;
      BuildBlock._equipOnly = BuildBlock.Stash._equipOnly;
      BuildBlock._tag = BuildBlock.Stash._tag;
      BuildBlock._needsRadio = BuildBlock.Stash._needsRadio;
      BuildBlock._needsFactory = BuildBlock.Stash._needsFactory;
      BuildBlock._needsArmory = BuildBlock.Stash._needsArmory;
      BuildBlock._needsConstructor = BuildBlock.Stash._needsConstructor;
      BuildBlock._needsRecycler = BuildBlock.Stash._needsRecycler;
      BuildBlock._needsSensor = BuildBlock.Stash._needsSensor;
      BuildBlock._needsFactoryOrArmory = BuildBlock.Stash._needsFactoryOrArmory;
      BuildBlock._needsDistributedBuild = BuildBlock.Stash._needsDistributedBuild;
      BuildBlock._buildMaster = BuildBlock.Stash._buildMaster;
      //$ -gen:target
    }    
  }
}