package sprint4.chaos;

import sprint4.core.D;
import sprint4.core.S;
import sprint4.core.X;
import sprint4.core.xconst.XComponentType;
import sprint4.core.xconst.XDirection;
import battlecode.common.BuildMappings;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;

/**
 * Top level for coordinating recycler, constructor, armory and factory to build required armys.
 * @author yingyin
 *
 */
public class BuildOrderTopLevel {
  
  // interface

  // util

  // state
  
    
  
  /*
  static {
    buildMeThis(
        [Role.LEADER, Role.FOLLOWER],
        true, false,
        [Chassis.LIGHT, Chassis.MEDIUM],
        [
         [ComponentType.BLASTER, ComponentType.BLASTER, ComponentType.SHIELD],
         [ComponentType.RADAR, ComponentType.ANTENNA, ComponentType.PROCESSOR],
        ]
    );
  }
  */
  
  
  // Parameters that set the type of robots to build
  public static Chassis chassis = Chassis.FLYING;
  public static ComponentType[] components = {ComponentType.DUMMY, ComponentType.SIGHT};
  public static boolean needArmory = true;
  public static boolean needFactory = true;
  
  public static ComponentType[] constructorComponents = {
    ComponentType.CONSTRUCTOR, ComponentType.SIGHT, ComponentType.SHIELD};

  public static int _fleetCost = chassis.cost + YingUtil.componentCost(components);

  public static int _armoryCost = Chassis.BUILDING.cost
      + ComponentType.ARMORY.cost;
  public static int _factoryCost = Chassis.BUILDING.cost
      + ComponentType.FACTORY.cost;
  public static int _constructorCost = Chassis.LIGHT.cost
      + YingUtil.componentCost(constructorComponents);

  public static double _buildersUpkeep = Chassis.LIGHT.upkeep
      + Chassis.BUILDING.upkeep * 2;
  public static double _fleetUpkeep = Chassis.FLYING.upkeep * 4;

  public static int _totalCost = _constructorCost + _armoryCost + _factoryCost
      + _fleetCost;

  public static int _totalDelay = GameConstants.POWER_WAKE_DELAY * 3
      + ComponentType.SMALL_MOTOR.delay;
  
  // For recycler
  public static boolean _builtArmory = false;
  public static boolean _builtFactory = false;

  public static final boolean run() {
    while (S.builderController == null || S.builderController.isActive()) {
      X.yield();
    }
    try {
      switch (S.builderTypeInt) {
        case XComponentType.CONSTRUCTOR_INT:
          if (needArmory && buildArmory()) {
            _builtArmory = true;
            YingUtil.debug_p("Built armory");
          }
          if (needFactory && buildFactory()) {
            _builtFactory = true;
            YingUtil.debug_p("Built factory");
          }
          S.rc.turnOff();
          break;
        case XComponentType.ARMORY_INT:
          if (chassis == Chassis.FLYING)
            buildFlyer();
          break;
        case XComponentType.FACTORY_INT:
          equipBy(ComponentType.FACTORY);
          S.rc.turnOff();
          break;
        case XComponentType.RECYCLER_INT:
          if ((needArmory && !_builtArmory) || (needFactory && !_builtFactory)) {
            if (buildConstructor()) {
              YingUtil.debug_p("Built constructor.");
            }
          }
          equipBy(ComponentType.RECYCLER);
          break;
        default:
          break;
      }
      return true;
    } catch (Exception e) {
      D.debug_logException(e);
      return false;
    }
  }
  
  public static final void equipBy(ComponentType builderType) throws GameActionException {
    for (ComponentType type : components)
      if (BuildMappings.canBuild(builderType, type))
        SyncBuildBlock.waitAndTryEquip(type, chassis.level);
  }

  /**
   * 
   * @return the MapLocation of the flyer built; null if build is not successful
   * @throws GameActionException
   */
  public static final MapLocation buildFlyer() throws GameActionException {
    YingUtil.waitForFlux(_fleetCost, _fleetUpkeep);
    return SyncBuildBlock.waitAndTryBuild(Chassis.FLYING, S.location);
  }

  public static final boolean buildConstructor() throws Exception {
    YingUtil.debug_p("total cost = " + _totalCost);
    YingUtil.waitForFlux(_totalCost - _totalDelay, _buildersUpkeep + _fleetUpkeep);
    MapLocation buildLocation = SyncBuildBlock.waitAndTryBuild(Chassis.LIGHT,
        constructorComponents);
    if (buildLocation == null)
      return false;
    return true;
  }

  public static final boolean buildArmory() throws GameActionException {
    YingUtil.waitForFlux(_armoryCost + _factoryCost + _fleetCost
        - GameConstants.POWER_WAKE_DELAY, Chassis.BUILDING.upkeep * 2
        + _fleetUpkeep);
    return tryMoveAndBuild(ComponentType.ARMORY);
  }

  public static final boolean buildFactory() throws GameActionException {
    YingUtil.waitForFlux(_factoryCost + _fleetCost
        - GameConstants.POWER_WAKE_DELAY, Chassis.BUILDING.upkeep + _fleetUpkeep);
    return tryMoveAndBuild(ComponentType.FACTORY);
  }

  public static final boolean tryMoveAndBuild(ComponentType type)
      throws GameActionException {
    MapLocation prevLocation = S.location;
    while (S.movementController.isActive())
      X.yield();
    int startDir = S.direction.opposite().ordinal();
    for (int i = 0; i < 8; i++) {
      Direction d = XDirection.intToDirection[(startDir + i) % 8];
      if (tryMove(d)) {
        SyncBuildBlock.waitAndTryBuild(Chassis.BUILDING, prevLocation, type);
        return true;
      }
    }
    return false;
  }

  /**
   * Requires movement controller not active.
   * 
   * @param d
   * @return true if moves in direction d; false otherwise.
   * @throws GameActionException
   */
  public static final boolean tryMove(Direction d) throws GameActionException {
    if (!S.movementController.canMove(d)) {
      return false;
    }
    if (S.direction == d) {
      X.moveForward();
    } else if (S.direction.opposite() == d) {
      X.moveBackward();
    } else {
      X.setDirection(d);
      X.moveForward();
    }
    return true;
  }
}
