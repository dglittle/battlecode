package sprint3.core;

import java.util.ArrayList;

import sprint3.RobotPlayer;
import sprint3.core.xconst.XComponentType;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
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
   * @param rc the argument passed to {@link RobotPlayer#RobotPlayer(RobotController)}
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
    for (int i = onNewRoundHandlers.size() - 1; i >= 0; i--) {
      onNewRoundHandlers.get(i).onWhatever();
    }
  }
  
  /**
   * Wraps {@link MovementController#setDirection(Direction)} and updates internal state.
   * 
   * @param direction the {@link Direction} to turn towards
   * @throws GameActionException if the wrapped method throws
   */
  public static final void setDirection(Direction direction) throws GameActionException {
    debug_checkSetDirection(direction);    
    S.movementController.setDirection(direction);
    S.hasTurnedThisRound = true;
  }
  
  /**
   * Asserts we're not already facing the direction we're trying to turn to.
   * @param direction
   */
  public static final void debug_checkSetDirection(Direction direction) {
    D.debug_assert(S.direction != direction,
        "Trying to turn towards current direction");
  }
  
  /**
   * Wraps {@link MovementController#moveForward()} and updates internal state.
   * 
   * @throws GameActionException if the wrapped method throws
   */
  public static final void moveForward() throws GameActionException {
    S.movementController.moveForward();
    S.hasMovedThisRound = true;
  }
  
  /**
   * Wraps {@link MovementController#moveBackward()} and updates internal state.
   * 
   * @throws GameActionException if the wrapped method throws
   */
  public static final void moveBackward() throws GameActionException {
    S.movementController.moveBackward();
    S.hasMovedThisRound = true;
  }
  
  /**
   * Fires all weapons which can fire at some target.
   * 
   * @param target the MapLocation to shoot at
   * @param level the RobotLevel to shoot at
   */
  public static final void attack(MapLocation target, RobotLevel level) {
    boolean[] withinRangeValue = new boolean[XComponentType.COMPONENT_TYPES];
    boolean[] withinRangeKnown = new boolean[XComponentType.COMPONENT_TYPES];
    
    int weaponCount = S.weaponControllers.length;
    for (int i = weaponCount - 1; i >= 0; i--) {
      WeaponController weapon = S.weaponControllers[i];
      if (weapon.isActive()) { continue; }

      // Hyper-optimized version of "if(!weapon.withinRange(target)) continue;"
      int weaponTypeInt = S.weaponTypeInts[i];
      if (!withinRangeValue[weaponTypeInt]) {
        if (!withinRangeKnown[weaponTypeInt]) {
          withinRangeKnown[weaponTypeInt] = true;
          if ((withinRangeValue[weaponTypeInt] = weapon.withinRange(target)) == false) {
            continue;
          }
        }
        else {
          continue;
        }
      }
      
      try {
        weapon.attackSquare(target, level);
      }
      catch (GameActionException e) {
        D.debug_logException(e);  // This really shouldn't happen under any circumstance.
      }
    }
  }
  
  /**
   * Fires all weapons which can fire at some target.
   * 
   * @param robot RobotInfo for the robot to shoot at
   */
  public static final void attack(RobotInfo robot) {
    X.attack(robot.location, robot.chassis.level);
  }

  // TODO(pwnall): remove these and make routing code handle them
  
  public static final void moveTowardsSync(Direction d) throws GameActionException {
    MapLocation target = S.location.add(d);
    while (!S.location.equals(target)) {
      moveTowardsAsync(d);
      X.yield();
    }
  }

  public static final boolean moveTowardsAsync(Direction d) throws GameActionException {
    if (!S.movementController.canMove(d)) {
      return false;
    }
    if (S.movementController.isActive()) {
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
}
