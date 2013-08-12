package team050.core;

import java.util.Random;

import team050.core.xconst.BaseConfigs;
import team050.core.xconst.MovementRanges;
import team050.core.xconst.XComponentClass;
import team050.core.xconst.XComponentType;
import battlecode.common.BroadcastController;
import battlecode.common.BuilderController;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.JumpController;
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
  public static Role role = null;

  /** The current robot's level (air vs land). */
  public static RobotLevel level;

  /** The round in which #{@link S#init(RobotController)} was called. */
  public static int birthRound;

  // TODO(pwnall): move randomness in its own class.
  // public static Random rand;

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

  /**
   * The income and totalUpkeep values are only valid if the robot has a
   * recycler.
   */
  /** The total amount of flux income from all the recyclers in the team. */
  public static double income;
  /**
   * The total upkeep of the team.
   * 
   * Valid only for Buildings with Recyclers.
   */
  public static double totalUpkeep;

  /** The maximum number of bytecodes that this robot can execute per round. */
  public static int bytecodeLimit = GameConstants.BYTECODE_LIMIT_BASE;

  /** The controller for this robot's sensor component. Can be null. */
  public static SensorController sensorController;
  /**
   * The controller for this robot's BuildingSensor component.
   * 
   * Buildings may receive a better sensor (e.g., a Radar), but their original
   * sensor is omni-directional, so it can be more useful, e.g., for sensing the
   * things they build.
   */
  public static SensorController buildingSensorController;
  /** The component type for this robot's sensor component. */
  public static ComponentType sensorType;  

  /** The component type for this robot's sensor component, as an integer. */
  public static int sensorTypeInt = XComponentType.INVALID_INT;
  /** The controllers for this robot's weapons. Sorted by attack power. */
  public static WeaponController[] weaponControllers = new WeaponController[0];
  /** The component types for this robot's weapons. */
  public static ComponentType[] weaponTypes = new ComponentType[0];
  /** The component types for this robot's weapons, as integers. */
  public static int[] weaponTypeInts = new int[0];
  /** The maximum range of the robot's weapons. 0 if no weapon is equipped. */
  public static int maxWeaponRange;
  /** The minimum range of the robot's weapons. 0 if no weapon is equipped. */
  public static int minWeaponRange;
  /** The controller for the maximum range of the robot's weapons. null if no weapon is equipped. */
  public static WeaponController maxWeaponRangeController;
  /** The controller for minimum range of the robot's weapons. null if no weapon is equipped. */
  public static WeaponController minWeaponRangeController;
  /** The amount of hurt that we can call down on enemies. */
  public static double totalAttackPower;

  /**
   * The controllers for this robot's medics.
   * 
   * Technically, these are also weapons, but we will always want to
   * special-case them.
   */
  public static WeaponController[] medicControllers = new WeaponController[0];

  /** The controller for this robot's motor. Guaranteed not to be null. */
  public static MovementController movementController;
  /** Caches {@link MovementController#isActive()}. */
  public static boolean motorReady;

  /** The controller for this robot's builder component. Can be null. */
  public static BuilderController builderController;
  /** The component type for this robot's builder component. */
  public static ComponentType builderType;
  /** The component type for this robot's builder component, as an integer. */
  public static int builderTypeInt = XComponentType.INVALID_INT;

  /** The controller for building dummies. Can be null. */
  public static BuilderController dummyController;

  /** The controller for the jump component. Can be null. */
  public static JumpController[] jumpControllers;
  
  /** All our components. */
  public static ComponentType[] components = new ComponentType[0];
  
  
  /**
   * dx for map locations in jump range, indexed by the robot's orientation.
   * 
   * This is null for robots without a jump component.
   */
  public static int[][] jumpDxByDirection;
  /**
   * dy for map locations in jump range, indexed by the robot's orientation.
   * 
   * This is null for robots without a jump component.
   */
  public static int[][] jumpDyByDirection;

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
   * dy for all map locations in sensor edge (not viewable from one square
   * back), indexed by the robot's orientation.
   * 
   * This is used to compute {@link M#sensorEdgeDy}, which is more convenient to
   * work with.
   */
  public static int[][] sensorEdgeDyByDirection;

  /**
   * dx for all map locations in sensor edge (not viewable from one square
   * forward), indexed by the robot's orientation.
   * 
   * This is used to compute {@link M#sensorBackEdgeDx}, which is more
   * convenient to work with.
   */
  public static int[][] sensorBackEdgeDxByDirection;
  /**
   * dy for all map locations in sensor edge (not viewable from one square
   * forward), indexed by the robot's orientation.
   * 
   * This is used to compute {@link M#sensorBackEdgeDy}, which is more
   * convenient to work with.
   */
  public static int[][] sensorBackEdgeDyByDirection;

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

    // S.rand = new Random(id * 982451653);
    S._randomState = S.id * 982451653L + S.birthRound * 25214903917L;

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
   * nearbyRobots. Some elements may be null, indicating that sensing failed.
   */
  public static RobotInfo[] nearbyRobotInfos() {
    if (S._nearbyRobotsRound != S.round) {
      S._senseRobots();
    }
    return S._nearbyRobotInfo;
  }

  public static final Mine[] senseMines() {
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

  public static final Mine senseMine(MapLocation m) throws GameActionException {
    Object mine = S.sensorController.senseObjectAtLocation(m, RobotLevel.MINE);
    if (mine != null) {
      return (Mine) mine;
    }
    return null;
  }

  public static final MineInfo senseMineInfo(MapLocation m)
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

  public static final RobotInfo buildingSenseRobotInfo(MapLocation m,
      RobotLevel level) throws GameActionException {
    Object r = S.buildingSensorController.senseObjectAtLocation(m, level);
    if (r != null && (r instanceof Robot)) {
      return S.buildingSensorController.senseRobotInfo((Robot) r);
    }
    return null;
  }

  /**
   * @return at least one jump controller is not active
   */
  public static final boolean jumpReady() {
    if (S.jumpControllers == null)
      return false;
    for (JumpController jc : S.jumpControllers) {
      if (!jc.isActive())
        return true;
    }
    return false;
  }
  
  /** Ghetto random number generator because the one in util sucks. */
  public static final int randomInt() {
    // MMIX magics
    _randomState = _randomState * 6364136223846793005L + 1442695040888963407L;
    final int randomInt = (int)(_randomState >> 32);
    return randomInt > 0 ? randomInt : -randomInt;
  }
  /** Ghetto random number generator because the one in util sucks. */
  public static final int randomInt(int maxInt) {
    return randomInt() % maxInt;
  }
  /** Ghetto random number generator because the one in util sucks. */
  public static final boolean randomBoolean() {
    return (randomInt() % 2) == 0;
  }
  
  /** State for the ghetto random number generator. */
  public static long _randomState;

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

    ComponentController[] newComponents = S.rc.newComponents();
    final int newComponentCount = newComponents.length;

    final int oldComponentCount = components.length;
    final int componentCount = oldComponentCount + newComponentCount;
    final ComponentType[] nowComponents = new ComponentType[componentCount];
    System.arraycopy(components, 0, nowComponents, 0, oldComponentCount);
    
    for (int i = newComponentCount - 1; i >= 0; i--) {
      ComponentController component = newComponents[i];
      ComponentType type = component.type();
      nowComponents[oldComponentCount + i] = type;
      S.leftoverWeight -= type.weight;

      switch (component.componentClass().ordinal()) {
        case XComponentClass.ARMOR_INT:
          break;
        case XComponentClass.BUILDER_INT:
          S.builderController = (BuilderController) component;
          S.builderType = S.builderController.type();
          S.builderTypeInt = S.builderType.ordinal();
          if (S.builderTypeInt == XComponentType.RECYCLER_INT) {
            BaseConfigs.initConfigs();
          }
          break;
        case XComponentClass.COMM_INT:
          B.bc = (BroadcastController) component;
          break;
        case XComponentClass.MISC_INT:
          switch (type.ordinal()) {
            case XComponentType.JUMP_INT:
              S._addJump((JumpController) component);
              break;
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
          if (S.sensorTypeInt == XComponentType.BUILDING_SENSOR_INT) {
            S.buildingSensorController = S.sensorController;
          }
          // TODO(pwnall): do a better map init once we have coordinated maps
          M.emergencyInit();
          M.updateMapAfterNewSensor();
          break;
        case XComponentClass.WEAPON_INT:
          switch (type.ordinal()) {
            case XComponentType.MEDIC_INT:
              S._addMedic((WeaponController) component);
              break;
            default:
              S._addWeapon((WeaponController) component);
              break;
          }
          break;
      }
    }
    components = nowComponents;
    S.motorReady = !S.movementController.isActive();    
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
    final ComponentType newWeaponType = newWeapon.type();
    final int newWeaponTypeInt = newWeaponType.ordinal();
    final int newWeaponRange = newWeaponType.range;

    final int oldWeaponCount = S.weaponControllers.length;
    final int newWeaponCount = oldWeaponCount + 1;

    final double weaponPower = newWeaponType.attackPower;
    int index = 0;
    while (index < oldWeaponCount
        && weaponPower <= S.weaponTypes[index].attackPower) {
      index++;
    }

    WeaponController[] newWeaponControllers = new WeaponController[newWeaponCount];
    System.arraycopy(S.weaponControllers, 0, newWeaponControllers, 0, index);
    newWeaponControllers[index] = newWeapon;
    System.arraycopy(S.weaponControllers, index, newWeaponControllers,
        index + 1, oldWeaponCount - index);
    S.weaponControllers = newWeaponControllers;

    ComponentType[] newWeaponTypes = new ComponentType[newWeaponCount];
    System.arraycopy(S.weaponTypes, 0, newWeaponTypes, 0, index);
    newWeaponTypes[index] = newWeaponType;
    System.arraycopy(S.weaponTypes, index, newWeaponTypes, index + 1,
        oldWeaponCount - index);
    S.weaponTypes = newWeaponTypes;

    int[] newWeaponTypeInts = new int[newWeaponCount];
    System.arraycopy(S.weaponTypeInts, 0, newWeaponTypeInts, 0, index);
    newWeaponTypeInts[index] = newWeaponTypeInt;
    System.arraycopy(S.weaponTypeInts, index, newWeaponTypeInts, index + 1,
        oldWeaponCount - index);
    S.weaponTypeInts = newWeaponTypeInts;

    if (oldWeaponCount == 0) {
      S.maxWeaponRange = newWeaponRange;
      S.minWeaponRange = newWeaponRange;
      S.minWeaponRangeController = newWeapon;
    } else {
      if (S.maxWeaponRange < newWeaponRange) {
        S.maxWeaponRange = newWeaponRange;
        S.maxWeaponRangeController = newWeapon;
      }
      if (S.minWeaponRange > newWeaponRange) {
        S.minWeaponRange = newWeaponRange;
        S.minWeaponRangeController = newWeapon;
      }
    }
    S.totalAttackPower += newWeaponType.attackPower;
  }

  /** Updates sensor data to reflect the addition of a new medic. */
  public static final void _addMedic(WeaponController newMedic) {
    int oldMedicCount = S.medicControllers.length;
    int newMedicCount = oldMedicCount + 1;

    WeaponController[] newMedicControllers = new WeaponController[newMedicCount];
    System.arraycopy(S.medicControllers, 0, newMedicControllers, 0,
        oldMedicCount);
    newMedicControllers[oldMedicCount] = newMedic;
    S.medicControllers = newMedicControllers;
  }

  /** Updates sensor data to reflect the addition of a new jump. */
  public static final void _addJump(JumpController newJump) {
    final int oldJumpCount = (S.jumpControllers == null) ? 0
        : S.jumpControllers.length;
    final int jumpCount = oldJumpCount + 1;
    final JumpController[] newJumpControllers = new JumpController[jumpCount];
    if (jumpControllers != null)
      System.arraycopy(jumpControllers, 0, newJumpControllers, 0, oldJumpCount);
    newJumpControllers[oldJumpCount] = newJump;
    S.jumpControllers = newJumpControllers;
    if (S.jumpDxByDirection == null) {
      MovementRanges.initJumpByDirection();
    }
  }

  /**
   * Called by {@link S#nearbyRobots()} and {@link S#nearbyRobotInfos()}.
   * 
   * Do not call directly. This is an expensive method, and its results are
   * cached.
   */
  public static final void _senseRobots() {
    if (S.sensorController == null) {
      S._nearbyRobots = new Robot[0];
      S._nearbyRobotInfo = new RobotInfo[0];
      S._nearbyRobotsRound = S.round;
      return;
    }

    try {
      if (S.buildingSensorController == null || S.sensorIsOmnidirectional) {
        // Fast path.
        S._nearbyRobots =
            S.sensorController.senseNearbyGameObjects(Robot.class);
        S._nearbyRobotInfo = new RobotInfo[S._nearbyRobots.length];
        for (int i = _nearbyRobots.length - 1; i >= 0; i--) {
          S._nearbyRobotInfo[i] =
              S.sensorController.senseRobotInfo(S._nearbyRobots[i]);
        }
      } else {
        // Slow path: buildings with 2 sensors, one of which isn't omni.
        final Robot[] bRobots =
            S.buildingSensorController.senseNearbyGameObjects(Robot.class);
        final int bRobotCount = bRobots.length;
        final int[] bRobotIDs = new int[bRobotCount];
        final RobotInfo[] bInfos = new RobotInfo[bRobotCount];
        for (int i = bRobotCount - 1; i >= 0; i--) {
          final Robot robot = bRobots[i];
          bRobotIDs[i] = robot.getID();
          bInfos[i] = S.buildingSensorController.senseRobotInfo(robot);
        }
        final Robot[] sRobots =
            S.sensorController.senseNearbyGameObjects(Robot.class);
        int sRobotCount = sRobots.length;
        final RobotInfo[] sInfos = new RobotInfo[sRobotCount];
        int duplicates = 0;
        for (int i = 0; i < sRobotCount; i++) {
          final int sid = sRobots[i].getID();
          boolean duplicate = false;
          for (int j = bRobotCount - 1; j >= 0; j--) {
            if (sid == bRobotIDs[j]) {
              duplicate = true;
              break;
            }
          }
          if (duplicate) {
            duplicates++;
          } else {
            sRobots[i - duplicates] = sRobots[i];
            sInfos[i - duplicates] =
              S.sensorController.senseRobotInfo(sRobots[i]);
          }
        }
        sRobotCount -= duplicates;

        final int robotCount = bRobotCount + sRobotCount;
        S._nearbyRobots = new Robot[robotCount];
        System.arraycopy(bRobots, 0, S._nearbyRobots, 0, bRobotCount);
        System.arraycopy(sRobots, 0, S._nearbyRobots, bRobotCount, sRobotCount);

        S._nearbyRobotInfo = new RobotInfo[robotCount];
        System.arraycopy(bInfos, 0, S._nearbyRobotInfo, 0, bRobotCount);
        System.arraycopy(sInfos, 0, S._nearbyRobotInfo, bRobotCount,
            sRobotCount);
      }

      S._nearbyRobotsRound = S.round;
    } catch (GameActionException e) {
      D.debug_logException(e); // Cause: bytecode overflow.
      // D.debug_py(" Byte code = " + Clock.getBytecodeNum());
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
   * 
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

  /**
   * TODO(ying): ignore depleting mines? check whether the robot is a building
   * or not.
   * 
   * @return
   * @throws GameActionException
   */
  public static MapLocation nearestEmptyMine() throws GameActionException {
    MapLocation bestMine = null;
    int bestDist = Integer.MAX_VALUE;
    Mine[] mines = S.sensorController.senseNearbyGameObjects(Mine.class);
    for (int i = mines.length - 1; i >=0; i--) {
      final MapLocation loc = mines[i].getLocation();
      final Robot r = (Robot) S.sensorController.senseObjectAtLocation(loc, 
          RobotLevel.ON_GROUND);
      if (r == null) {
        final int dist = S.location.distanceSquaredTo(loc);
        if (dist < bestDist) {
          bestDist = dist;
          bestMine = loc;
        }
      }
    }
    return bestMine;
  }

  /**
   * stolen from devs
   */
  protected boolean withinRange(WeaponController wc, MapLocation from,
      Direction facing, MapLocation target) {
    if (from.distanceSquaredTo(target) > wc.type().range)
      return false;
    return inAngleRange(from, facing, target, wc.type().cosHalfAngle);
  }

  public static MapLocation zero = new MapLocation(0, 0);

  public static boolean inAngleRange(MapLocation sensor, Direction dir,
      MapLocation target, double cosHalfTheta) {
    MapLocation dirVec = zero.add(dir);
    double dx = target.x - sensor.x;
    double dy = target.y - sensor.y;
    int a = dirVec.x;
    int b = dirVec.y;
    double dotProduct = a * dx + b * dy;

    if (dotProduct < 0) {
      if (cosHalfTheta > 0)
        return false;
    } else if (cosHalfTheta < 0)
      return true;

    double rhs = cosHalfTheta * cosHalfTheta * (dx * dx + dy * dy)
        * (a * a + b * b);

    if (dotProduct < 0)
      return (dotProduct * dotProduct <= rhs + 0.00001d);
    else
      return (dotProduct * dotProduct >= rhs - 0.00001d);
  }
}
