package legenClone.core;

import java.util.ArrayList;

import legenClone.RobotPlayer;
import legenClone.util.ComponentUtil;
import legenClone.util.MapUtil;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.MovementController;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;
import battlecode.common.WeaponController;

public final class X {
  public static ArrayList<Callback> onNewRoundHandlers = new ArrayList<Callback>();

  /**
   * Initializes the robot's internal state.
   * 
   * @param rc the argument passed to
   *          {@link RobotPlayer#RobotPlayer(RobotController)}
   */
  public static void init(RobotController rc) {
    S.init(rc);
    B.init();
    S.updateSensorsFully();

    M.updateMapAfterTurning();
    M.updateMapAfterMoving();
    B.checkMessages();
  }

  /**
   * Wraps {@link RobotController#yield()} and updates internal state.
   */
  public static void yield() {
    S.rc.yield();
    S.updateSensorsAfterRound();
    B.checkMessages();
    for (int i = onNewRoundHandlers.size() - 1; i >= 0; i--) {
      onNewRoundHandlers.get(i).onWhatever();
    }
  }

  /**
   * Wraps {@link MovementController#setDirection(Direction)} and updates
   * internal state.
   * 
   * @param direction the direction
   * @throws GameActionException if the wrapped method throws the exception.
   */
  public static void setDirection(Direction direction)
      throws GameActionException {
    // TODO(pwnall): break this up into 2 methods.
    if (direction != S.direction) {
      S.movementController.setDirection(direction);
      S.rc.yield();
      S.updateSensorsAfterRound();
      S.updateSensorsAfterTurning();
      M.updateMapAfterTurning();
      B.checkMessages();
      for (int i = onNewRoundHandlers.size() - 1; i >= 0; i--) {
        onNewRoundHandlers.get(i).onWhatever();
      }
    }
  }

  /**
   * Wraps {@link MovementController#moveForward()} and updates internal state.
   * 
   * @throws GameActionException if the wrapped method throws the exception.
   */
  public static void moveForward() throws GameActionException {
    S.movementController.moveForward();
    S.rc.yield();
    S.updateSensorsAfterRound();
    S.updateSensorsAfterMoving();
    M.updateMapAfterMoving();
    B.checkMessages();
    for (int i = onNewRoundHandlers.size() - 1; i >= 0; i--) {
      onNewRoundHandlers.get(i).onWhatever();
    }
  }

  /**
   * Wraps {@link MovementController#moveBackward()} and updates internal state.
   * 
   * @throws GameActionException if the wrapped method throws the exception.
   */
  public static void moveBackward() throws GameActionException {
    S.movementController.moveBackward();
    S.rc.yield();
    S.updateSensorsAfterRound();
    S.updateSensorsAfterMoving();
    M.updateMapAfterMoving();
    B.checkMessages();
    for (int i = onNewRoundHandlers.size() - 1; i >= 0; i--) {
      onNewRoundHandlers.get(i).onWhatever();
    }
  }

  /**
   * Fires all weapons which can fire at some target.
   * 
   * @param target the MapLocation to shoot at
   * @param level the RobotLevel to shoot at
   */
  public static final void attack(MapLocation target, RobotLevel level) {
    boolean[] withinRangeValue = new boolean[ComponentUtil.COMPONENT_TYPES];
    boolean[] withinRangeKnown = new boolean[ComponentUtil.COMPONENT_TYPES];

    int weaponCount = S.weaponControllers.length;
    for (int i = 0; i < weaponCount; i++) {
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
      } catch (GameActionException e) {
        e.printStackTrace(); // This really shouldn't happen under any
                             // circumstance.
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

  public static void moveDirSync(Direction d) throws GameActionException {
    if (S.direction == d) {
      X.moveForward();
    } else if (S.direction.opposite() == d) {
      X.moveBackward();
    } else {
      X.setDirection(d);
      X.moveForward();
    }
  }

  public static void moveDirAsync(Direction d) throws GameActionException {
    if (S.direction == d) {
      X.moveForward();
    } else if (S.direction.opposite() == d) {
      X.moveBackward();
    } else {
      X.setDirection(d);
    }
  }

  public static void build(ComponentType type, MapLocation loc, RobotLevel level)
      throws GameActionException {
    S.builderController.build(type, loc, level);
    yield();
  }

  public static void build(Chassis chassis, MapLocation loc)
      throws GameActionException {
    S.builderController.build(chassis, loc);
    yield();
  }

  /**
   * Requires have enough flux and the controller is not active.
   * 
   * @param chassis
   * @return
   * @throws GameActionException
   */
  public static final MapLocation tryBuild(Chassis chassis)
      throws GameActionException {
    RobotLevel level = chassis.level;
    if (!S.level.equals(level)) {
      if (tryBuild(chassis, S.location) != null)
        return S.location;
    }
    for (int i = 0; i < 8; i++) {
      MapLocation there = S.location.add(MapUtil.intToDirection[i]);
      if (tryBuild(chassis, there) != null)
        return there;
    }
    return null;
  }

  public static final MapLocation tryBuild(Chassis chassis, MapLocation there)
      throws GameActionException {
    if (canBuild(there, chassis.level)) {
      S.builderController.build(chassis, there);
      yield();
      return there;
    }
    return null;
  }

  /**
   * Requires have enough flux and the controller is not active. Try to build a
   * component on chassis in the square immediately adjacent to myself.
   * 
   * @param ct ComponentType to build.
   * @return true if the build action is queued; false otherwise
   * @throws GameActionException
   */
  public static final boolean tryBuild(ComponentType ct, RobotLevel level)
      throws GameActionException {
    for (int i = 0; i < 8; i++) {
      MapLocation there = S.location.add(MapUtil.intToDirection[i]);
      if (tryBuild(ct, there, level)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Requires have enough flux and the controller is not active.
   * 
   * @param ct
   * @param there
   * @return
   * @throws GameActionException
   */
  public static final boolean tryBuild(ComponentType ct, MapLocation there,
      RobotLevel level) throws GameActionException {
    GameObject r = S.sensorController.senseObjectAtLocation(there, level);
    if (r != null && r instanceof Robot) {
      RobotInfo ri = S.sensorController.senseRobotInfo((Robot) r);
      int currentWeight = 0;
      for (ComponentType c : ri.components)
        currentWeight += c.weight;
      if (currentWeight + ct.weight <= ri.chassis.weight) {
        S.builderController.build(ct, there, level);
        yield();
        return true;
      }
    }
    return false;
  }

  public static final boolean canBuild(MapLocation there, RobotLevel level)
      throws GameActionException {
    TerrainTile tile = S.rc.senseTerrainTile(there);
    if (tile == null || !tile.isTraversableAtHeight(level))
      return false;
    return S.sensorController.senseObjectAtLocation(there, level) == null;
  }

  public static final boolean tryMove(Direction d) throws GameActionException {
    if (!S.movementController.canMove(d)) {
      return false;
    }

    if (S.direction == d) {
      X.moveForward();
    } else if (S.direction.opposite().equals(d)) {
      X.moveBackward();
    } else {
      X.setDirection(d);
    }
    return true;
  }
}
