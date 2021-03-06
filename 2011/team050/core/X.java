package team050.core;

import java.util.ArrayList;

import team050.RobotPlayer;
import team050.core.xconst.XComponentType;
import battlecode.common.BuilderController;
import battlecode.common.Chassis;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.JumpController;
import battlecode.common.MapLocation;
import battlecode.common.MovementController;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.WeaponController;

public final class X {
  public static final ArrayList<Callback> onNewRoundHandlers = new ArrayList<Callback>();

  /**
   * Initializes the robot's internal state.
   * 
   * @param rc the argument passed to
   *          {@link RobotPlayer#RobotPlayer(RobotController)}
   */
  public static final void init(RobotController rc) {
    S.init(rc);
    B.init();
    S.updateSensors();

    B.checkMessages();
  }

  /**
   * Wraps {@link RobotController#yield()} and updates internal state.
   */
  public static final void yield() {
    S.rc.yield();
    S.updateSensors();
    B.checkMessages();
    unloadedAll = false; // Haven't fired yet!
    for (int i = onNewRoundHandlers.size() - 1; i >= 0; i--) {
      onNewRoundHandlers.get(i).onWhatever();
    }
  }

  /**
   * Wraps {@link MovementController#setDirection(Direction)} and updates
   * internal state.
   * 
   * @param direction the {@link Direction} to turn towards
   * @throws GameActionException if the wrapped method throws
   */
  public static final void setDirection(Direction direction)
      throws GameActionException {
    debug_checkSetDirection(direction);
    // $ +gen:source setDirection.core __
    S.movementController.setDirection(direction);
    S.motorReady = false;
    S.hasTurnedThisRound = true;
    // $ -gen:source
  }

  /**
   * Asserts we're not already facing the direction we're trying to turn to.
   * 
   * @param direction
   */
  public static final void debug_checkSetDirection(Direction direction) {
    D.debug_assert(S.direction != direction,
        "Trying to turn towards current direction");
  }

  /**
   * Changes the direction, if we're not already facing that direction.
   * 
   * @return true if turning happened
   * @throws GameActionException if
   *           {@link MovementController#setDirection(Direction)} throws
   */
  public static final boolean setDirectionChecked(Direction direction)
      throws GameActionException {
    if (S.direction == direction) {
      return false;
    }
    // $ +gen:target setDirection.core __
    S.movementController.setDirection(direction);
    S.motorReady = false;
    S.hasTurnedThisRound = true;
    // $ -gen:target
    return true;
  }

  /**
   * Wraps @link {@link BuilderController#build(Chassis, MapLocation)} and
   * updates internal state.
   * 
   * @param chassis the {@link Chassis} to be built
   * @param location the {@link MapLocation} to build on
   * @throws GameActionException if the wrapped method throws
   */
  public static final void build(Chassis chassis, MapLocation location)
      throws GameActionException {
    S.builderController.build(chassis, location);
    if (chassis.level == RobotLevel.ON_GROUND) {
      M.registerObstacle(location);
    }
  }

  /**
   * Wraps {@link MovementController#moveForward()} and updates internal state.
   * 
   * @throws GameActionException if the wrapped method throws
   */
  public static final void moveForward() throws GameActionException {
    S.movementController.moveForward();
    //$ +gen:source moveForwardBackward.core __
    S.motorReady = false;
    S.hasMovedThisRound = true;
    //$ -gen:source    
  }

  /**
   * Wraps {@link MovementController#moveBackward()} and updates internal state.
   * 
   * @throws GameActionException if the wrapped method throws
   */
  public static final void moveBackward() throws GameActionException {
    S.movementController.moveBackward();
    //$ +gen:target moveForwardBackward.core __
    S.motorReady = false;
    S.hasMovedThisRound = true;
    //$ -gen:target
  }

  public static boolean unloadedAll = false;

  /**
   * Fires all weapons which can fire at some target.
   * 
   * @param target the MapLocation to shoot at
   * @param level the RobotLevel to shoot at
   */
  public static final boolean attack(MapLocation target, RobotLevel level) {
    boolean[] withinRangeValue = new boolean[XComponentType.COMPONENT_TYPES];
    boolean[] withinRangeKnown = new boolean[XComponentType.COMPONENT_TYPES];

    int weaponCount = S.weaponControllers.length;
    int attackCount = 0;
    for (int i = weaponCount - 1; i >= 0; i--) {
      WeaponController weapon = S.weaponControllers[i];
      if (weapon.isActive()) {
        continue;
      }

      // Hyper-optimized version of "if(!weapon.withinRange(target)) continue;"
      int weaponTypeInt = S.weaponTypeInts[i];
      if (!withinRangeValue[weaponTypeInt]) {
        if (!withinRangeKnown[weaponTypeInt]) {
          withinRangeKnown[weaponTypeInt] = true;
          if ((withinRangeValue[weaponTypeInt] = weapon.withinRange(target)) == false) {
            continue;
          }
        } else {
          continue;
        }
      }

      try {
        weapon.attackSquare(target, level);
        ++attackCount;
      } catch (GameActionException e) {
        D.debug_logException(e); // This really shouldn't happen under any
                                 // circumstance.
      }
    }
    unloadedAll = attackCount == weaponCount;
    return attackCount > 0;
  }

  /**
   * Fires all weapons which can fire at some target.
   * 
   * @param robot RobotInfo for the robot to shoot at
   */
  public static final void attack(RobotInfo robot) {
    X.attack(robot.location, robot.chassis.level);
  }

  /**
   * Fires all medics to heal a target.
   * 
   * @param target the MapLocation of the robot to heal
   * @param level the RobotLevel of the robot to heal
   */
  public static final void heal(MapLocation target, RobotLevel level) {
    for (int i = S.medicControllers.length - 1; i >= 0; i--) {
      WeaponController medic = S.medicControllers[i];
      if (medic.isActive()) {
        continue;
      }

      try {
        medic.attackSquare(target, level);
      } catch (GameActionException e) {
        D.debug_logException(e); // This really shouldn't happen under any
                                 // circumstance.
      }
    }
  }

  /**
   * Fires all medics to heal a target.
   * 
   * @param robot RobotInfo for the robot to heal
   */
  public static final void heal(RobotInfo robot) {
    X.heal(robot.location, robot.chassis.level);
  }

  // TODO(pwnall): remove these and make routing code handle them

  public static final void moveTowardsSync(Direction d)
      throws GameActionException {
    MapLocation target = S.location.add(d);
    while (!S.location.equals(target)) {
      moveTowardsAsync(d);
      X.yield();
    }
  }

  /**
   * if we can't move this direction, we won't even turn toward it
   * 
   * @param d
   * @return
   * @throws GameActionException
   */
  public static final boolean moveTowardsAsync(Direction d)
      throws GameActionException {
    if (!S.movementController.canMove(d)) {
      return false;
    }
    if (!S.motorReady) {
      return false;
    }

    if (S.direction == d) {
      X.moveForward();
    } else if (S.direction.opposite() == d) {
      X.moveBackward();
    } else {
      X.setDirection(d);
    }
    return true;
  }

  public static final boolean jump(MapLocation loc) {
    for (JumpController jc : S.jumpControllers) {
      if (!jc.isActive()) {
        try {
          jc.jump(loc);
          //$ +gen:target moveForwardBackward.core __
          S.motorReady = false;
          S.hasMovedThisRound = true;
          //$ -gen:target
        } catch (Exception e) {
          return false;
        }
        return true;
      }
    }
    return false;
  }
}
