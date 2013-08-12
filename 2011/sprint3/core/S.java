package sprint3.core;

import java.util.Random;

import sprint3.core.xconst.XComponentClass;
import sprint3.core.xconst.XComponentType;
import sprint3.core.xconst.XRoleType;
import battlecode.common.BroadcastController;
import battlecode.common.BuilderController;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.MineInfo;
import battlecode.common.MovementController;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;
import battlecode.common.Team;
import battlecode.common.WeaponController;

/**
 * Sensor information.
 * 
 * These are updated at the beginning of each round, so they should be available
 * to everything else.
 */
public final class S {
  /** The robot's controller. */
  public static RobotController rc;
  /** The current robot. */
  public static Robot r;
  /** The current robot's chassis. */
  public static Chassis chassis;
  /** The current robot's chassis, as an integer. */
  public static int chassisInt;
  /** The maximum weight that the current robot's chassis can take. */
  public static int chassisWeight;
  /** The amount of weight left in this robot's chassis. */
  public static int leftoverWeight;

  /** The current robot's team. */
  public static Team team;
  /** The current robot's enemy team. */
  public static Team enemyTeam;
  /** The current robot's ID. */
  public static int id;
  /** The current robot's role. */
  public static int role = XRoleType.NONE;
  
  /** The current robot's level (air vs land). */
  public static RobotLevel level;

  /** The round in which #{@link S#init(RobotController)} was called. */
  public static int birthRound;

  // TODO(pwnall): move randomness in its own class.
  public static Random rand;

  /** The robot's current location. */
  public static MapLocation location;
  /** The X coordinate of the robot's current location. */
  public static int locationX;
  /** The Y coordinate of the robot's current location. */
  public static int locationY;
  /** The direction that the robot is facing. */
  public static Direction direction;
  /** The direction that the robot is facing, in integer form. */
  public static int directionInt;

  /** The MapLocation in front of the robot. */
  public static MapLocation front;

  /** The current round. */
  public static int round;

  /** The robot's hitpoints. */
  public static double hp;
  /** The robot's hitpoints in the previous round. */
  public static double oldHp;
  /**
   * The change in hitpoints between the last round and now. Negative is bad.
   * Can't be positive.
   */
  public static double dHp;

  /** The amount of flux owned by the entire team. */
  public static double flux;
  /** The amount of flux owned by the entire team in the previous round. */
  public static double oldFlux;
  /** The change in flux between the last round and now. Negative is bad. */
  public static double dFlux;
  
  /** The income and totalUpkeep values are only valid if the robot has a recycler. */
  /** The total amount of flux income from all the recyclers in the team. */
  public static double income;
  /** The total upkeep of the team */
  public static double totalUpkeep;

  /** The maximum number of bytecodes that this robot can execute per round. */
  public static int bytecodeLimit = GameConstants.BYTECODE_LIMIT_BASE;

  /** The controller for this robot's sensor component. Can be null. */
  public static SensorController sensorController;
  /** The component type for this robot's sensor component. */
  public static ComponentType sensorType;
  /** The component type for this robot's sensor component, as an integer. */
  public static int sensorTypeInt = XComponentType.INVALID_INT;
  /** The controller for this robot's weapon components. Can be null. */
  public static WeaponController[] weaponControllers = new WeaponController[0];
  /** The component type for this robot's weapon component. */
  public static ComponentType[] weaponTypes = new ComponentType[0];
  /** The component type for this robot's weapon component, as an integer. */
  public static int[] weaponTypeInts = new int[0];
  /** The ranges of the robot's weapon components. */
  public static int[] weaponRanges = new int[0];
  /** The range of the robot's weapon component. 0 if no weapon is equipped. */
  public static int maxWeaponRange;
  public static MovementController movementController;

  /** The controller for this robot's builder component. Can be null. */
  public static BuilderController builderController;
  /** The component type for this robot's builder component. */
  public static ComponentType builderType;
  /** The component type for this robot's builder component, as an integer. */
  public static int builderTypeInt = XComponentType.INVALID_INT;

  /** The controller for building dummies. Can be null. */
  public static BuilderController dummyController;

  /**
   * dx for all map locations in sensor range, indexed by the robot's
   * orientation.
   * 
   * This is used to compute {@link M#sensorDx}, which is more convenient to
   * work with.
   */
  public static int[][] sensorDxByDirection;
  /**
   * dy for all map locations in sensor range, indexed by the robot's
   * orientation.
   * 
   * This is used to compute {@link M#sensorDy}, which is more convenient to
   * work with.
   */
  public static int[][] sensorDyByDirection;

  /**
   * dx for all map locations in sensor edge (not viewable from one square
   * back), indexed by the robot's orientation.
   * 
   * This is used to compute {@link M#sensorEdgeDx}, which is more convenient to
   * work with.
   */
  public static int[][] sensorEdgeDxByDirection;
  /**
   * dy for all map locations in sensor range, indexed by the robot's
   * orientation.
   * 
   * This is used to compute {@link M#sensorEdgeDy}, which is more convenient to
   * work with.
   */
  public static int[][] sensorEdgeDyByDirection;
  /** True if the sensor has a 360 degree angle. */
  public static boolean sensorIsOmnidirectional;
  
  /** True if the robot turned last round. */
  public static boolean hasTurnedThisRound;
  /** True if the robot moved last round. */
  public static boolean hasMovedThisRound;

  /** Initializes all the fields. */
  public static final void init(RobotController controller) {
    S.birthRound = Clock.getRoundNum();
    S.rc = controller;
    S.r = rc.getRobot();
    S.chassis = rc.getChassis();
    S.chassisInt = chassis.ordinal();
    S.leftoverWeight = S.chassisWeight = S.chassis.weight;
    S.team = rc.getTeam();
    S.enemyTeam = S.team.opponent();
    S.id = r.getID();
    S.level = chassis.level;

    S.rand = new Random(id * 982451653);

    // NOTE: makes the cache work even if we wake up in round 0.
    S._nearbyRobotsRound = -1;

    // NOTE: there's a catch-22 in the first call to updateSensors() and this
    // solves it.
    S.hasMovedThisRound = true;
    S.hasTurnedThisRound = true;
    S.location = S.rc.getLocation();    
  }

  /** Updates all sensor data. */
  public static final void updateSensors() {
    if (S.hasTurnedThisRound) {
      S._updateSensorsAfterTurning();
      M.updateMapAfterTurning();
      S.hasTurnedThisRound = false;
    }
    if (S.hasMovedThisRound) {
      S._updateSensorsAfterMoving();
      M.updateMapAfterMoving();
      S.hasMovedThisRound = false;
    }
    S._updateSensorsAfterRound();
  }
  
  /** Robots in this robot's sensor range. */
  public static Robot[] nearbyRobots() {
    if (S._nearbyRobotsRound != S.round) {
      S._senseRobots();
    }
    return S._nearbyRobots;
  }

  /**
   * RobotInfo for robots in this robot's sensor range. Indexes synced with
   * nearbyRobots.
   */
  public static RobotInfo[] nearbyRobotInfos() {
    if (S._nearbyRobotsRound != S.round) {
      S._senseRobots();
    }
    return S._nearbyRobotInfo;
  }  

  public static final Mine[] senseMines() throws GameActionException {
    return S.sensorController.senseNearbyGameObjects(Mine.class);
  }

  public static final MineInfo[] senseMineInfos() throws GameActionException {
    Mine[] mines = S.sensorController.senseNearbyGameObjects(Mine.class);
    MineInfo[] mineInfos = new MineInfo[mines.length];
    for (int i = 0; i < mines.length; i++) {
      mineInfos[i] = S.sensorController.senseMineInfo(mines[i]);
    }
    return mineInfos;
  }

  public static final MineInfo senseMine(MapLocation m)
      throws GameActionException {
    Object mine = S.sensorController.senseObjectAtLocation(m, RobotLevel.MINE);
    if (mine != null) {
      return S.sensorController.senseMineInfo((Mine) mine);
    }
    return null;
  }

  public static final Robot senseRobot(MapLocation m, RobotLevel level)
      throws GameActionException {
    return (Robot) S.sensorController.senseObjectAtLocation(m, level);
  }

  public static final RobotInfo senseRobotInfo(MapLocation m, RobotLevel level)
      throws GameActionException {
    Object r = S.sensorController.senseObjectAtLocation(m, level);
    if (r != null && (r instanceof Robot)) {
      return S.sensorController.senseRobotInfo((Robot) r);
    }
    return null;
  }
  
  /** The round when nearbyRobots was computed last. */
  public static int _nearbyRobotsRound;
  /** Robots in this robot's sensor range. */
  public static Robot[] _nearbyRobots;
  /**
   * RobotInfo for robots in this robot's sensor range. Indexes synced with
   * nearbyRobots.
   */
  public static RobotInfo[] _nearbyRobotInfo;

  /** Updates the sensor values that change every round. */
  public static final void _updateSensorsAfterRound() {
    S.round = Clock.getRoundNum();
    S.oldHp = S.hp;
    S.hp = S.rc.getHitpoints();
    S.dHp = S.hp - S.oldHp;
    S.oldFlux = S.flux;
    S.flux = S.rc.getTeamResources();
    S.dFlux = S.flux - S.oldFlux;
    if (builderTypeInt == XComponentType.RECYCLER_INT) {
      try {
        income = sensorController.senseIncome(r);
        totalUpkeep = income - dFlux;
      } catch (GameActionException e) {
        D.debug_logException(e);
      }
    }

    ComponentController[] components = S.rc.newComponents();
    for (int i = components.length - 1; i >= 0; i--) {
      ComponentController component = components[i];
      ComponentType type = component.type();
      S.leftoverWeight -= type.weight;

      switch (component.componentClass().ordinal()) {
        case XComponentClass.ARMOR_INT:
          break;
        case XComponentClass.BUILDER_INT:
          S.builderController = (BuilderController) component;
          S.builderType = S.builderController.type();
          S.builderTypeInt = S.builderType.ordinal();
          break;
        case XComponentClass.COMM_INT:
          B.bc = (BroadcastController) component;
          break;
        case XComponentClass.MISC_INT:
          switch (type.ordinal()) {
            case XComponentType.PROCESSOR_INT:
              S.bytecodeLimit += GameConstants.BYTECODE_LIMIT_ADDON;
              break;
            case XComponentType.DUMMY_INT:
              S.dummyController = (BuilderController) component;
              break;
            default:
              break;
          }
          break;
        case XComponentClass.MOTOR_INT:
          S.movementController = (MovementController) component;
          break;
        case XComponentClass.SENSOR_INT:
          S.sensorController = (SensorController) component;
          S.sensorType = S.sensorController.type();
          S.sensorTypeInt = S.sensorType.ordinal();
          S.sensorIsOmnidirectional = (S.sensorType.angle == 360.0);
          // TODO(pwnall): do a better map init once we have coordinated maps
          M.emergencyInit();
          M.updateMapFirstTime();
          break;
        case XComponentClass.WEAPON_INT:
          S._addWeapon((WeaponController) component);
          break;
      }
    }
  }

  /** Updates sensor data that changes when this robot moves. */
  public static final void _updateSensorsAfterMoving() {
    S.location = S.rc.getLocation();
    S.locationX = S.location.x;
    S.locationY = S.location.y;
    S.front = S.location.add(S.direction);
  }

  /** Updates sensor data that changes when this robot turns. */
  public static final void _updateSensorsAfterTurning() {
    S.direction = S.rc.getDirection();
    S.directionInt = S.direction.ordinal();
    S.front = S.location.add(S.direction);
  }

  /** Updates sensor data to reflect the addition of a new weapon. */
  public static final void _addWeapon(WeaponController newWeapon) {
    ComponentType newWeaponType = newWeapon.type();
    int newWeaponTypeInt = newWeaponType.ordinal();
    int newWeaponRange = newWeaponType.range;

    int oldWeaponCount = S.weaponControllers.length;
    int newWeaponCount = oldWeaponCount + 1;

    WeaponController[] newWeaponControllers = new WeaponController[newWeaponCount];
    System.arraycopy(S.weaponControllers, 0, newWeaponControllers, 0,
        oldWeaponCount);
    newWeaponControllers[oldWeaponCount] = newWeapon;
    S.weaponControllers = newWeaponControllers;

    ComponentType[] newWeaponTypes = new ComponentType[newWeaponCount];
    System.arraycopy(S.weaponTypes, 0, newWeaponTypes, 0, oldWeaponCount);
    newWeaponTypes[oldWeaponCount] = newWeaponType;
    S.weaponTypes = newWeaponTypes;

    int[] newWeaponTypeInts = new int[newWeaponCount];
    System.arraycopy(S.weaponTypeInts, 0, newWeaponTypeInts, 0, oldWeaponCount);
    newWeaponTypeInts[oldWeaponCount] = newWeaponTypeInt;
    S.weaponTypeInts = newWeaponTypeInts;

    int[] newWeaponRanges = new int[newWeaponCount];
    System.arraycopy(S.weaponRanges, 0, newWeaponRanges, 0, oldWeaponCount);
    newWeaponRanges[oldWeaponCount] = newWeaponRange;
    S.weaponRanges = newWeaponRanges;

    if (S.maxWeaponRange < newWeaponRange) {
      S.maxWeaponRange = newWeaponRange;
    }
  }
  
  /**
   * sometimes has null elements!
   * 
   * Populates {@link S#nearbyRobots} and {@link S#nearbyRobotInfo}.
   * 
   * Called by {@link S#_updateSensorsAfterRound()}. Do not call manually.
   */
  public static final void _senseRobots() {
    S._nearbyRobotsRound = S.round;

    if (S.sensorController == null) {
      S._nearbyRobots = new Robot[0];
      S._nearbyRobotInfo = new RobotInfo[0];
      return;
    }
    
    S._nearbyRobots = S.sensorController.senseNearbyGameObjects(Robot.class);
    S._nearbyRobotInfo = new RobotInfo[S._nearbyRobots.length];
    for (int i = _nearbyRobots.length - 1; i >= 0; i--) {
      try {
        S._nearbyRobotInfo[i] = S.sensorController.senseRobotInfo(S._nearbyRobots[i]);
      } catch (GameActionException e) {
        //D.debug_logException(e); // Cause: bytecode overflow.

        // TODO(pwnall): consider replacing with self's RobotInfo
        S._nearbyRobotInfo[i] = null;
      }
    }
  }

  // /////////////////////////////////////////////////////////////////////
  // /////////////////////////////////////////////////////////////////////
  // /////////////////////////////////////////////////////////////////////
  // /////////////////////////////////////////////////////////////////////
  // /////////////////////////////////////////////////////////////////////
  // S Utilities

  public static boolean allowedToBuildNonMine(Chassis chassis,
      ComponentType... components) {
    double cost;
    double upkeep;
    if (chassis == null) {
      cost = 0;
      upkeep = 0.0;
    } else {
      cost = chassis.cost;
      upkeep = chassis.upkeep;
    }
    for (ComponentType comp : components) {
      cost += comp.cost;
    }
    return allowedToBuildNonMine(cost, upkeep);
  }

  public static boolean allowedToBuildNonMine(double cost, double upkeep) {
    return (S.flux > cost + totalUpkeep && S.dFlux > upkeep);
  }
  
  /**
   * Do not check dFlux for building
   * @param chassis
   * @param components
   * @return
   */
  public static boolean allowedToBuildMine(Chassis chassis,
      ComponentType... components) {
    double cost;
    if (chassis == null) {
      cost = 0;
    } else {
      cost = chassis.cost;
    }
    for (ComponentType comp : components) {
      cost += comp.cost;
    }
    return (S.flux > cost + totalUpkeep + Chassis.BUILDING.upkeep);
  }

  public static MapLocation nearestEmptyMine() throws GameActionException {
    MapLocation bestMine = null;
    int bestDist = Integer.MAX_VALUE;
    for (Mine m : senseMines()) {
      MapLocation loc = m.getLocation();
      Robot r = senseRobot(loc, RobotLevel.ON_GROUND);
      if (r == null) {
        int dist = S.location.distanceSquaredTo(loc);
        if (dist < bestDist) {
          bestDist = dist;
          bestMine = loc;
        }
      }
    }
    return bestMine;
  }
}
