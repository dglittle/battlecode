package heavy1.blocks;

import heavy1.core.D;
import heavy1.core.S;
import heavy1.core.X;
import battlecode.common.Chassis;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.WeaponController;

/**
 * Combat block for general combat.
 */
public class CombatBlock {

  // --- Parameters -------------------------------------------------------

  public static final double PROB_SHOOT_BUILDING = 0.3;

  // --- Methods ----------------------------------------------------------

  /**
   * Perform asynchronous attack actions. This means that a robot can turn and
   * shoot in the same round if it makes it positioned better for attacking.
   * Does not yield.
   * 
   * Stops searching for weaker robots after finding a building with probability
   * PROB_SHOOT_BUILDING.
   * 
   * @param allowedToMove whether this method is allowed to use the movement
   *          controller
   * @return whether an attack action was performed
   * @throws GameActionException
   */
  public static final boolean attackWeakestRobotOrBuildingAsync(
      boolean allowedToMove) throws GameActionException {
    if (_unarmed())
      return false;
    // Find the weakest target, possibly position yourself for better shooting,
    // and attack.
    _setWeakestNearbyRobotOrBuildingAsTarget();
    if (bestTarget != null) {
      positionForAttack(bestTarget);
      if (_canAttack(bestTarget))
        X.attack(bestTarget, bestLevel);
      return true;
    } else {
      // If we don't see anything, we want to see
    }
    return false;
  }

  /**
   * Wraps {@see attackWeakestRobotOrBuilding(boolean allowedToMove)} with an
   * arg value of true.
   * 
   * @return
   * @throws GameActionException
   */
  public static final boolean attackWeakestRobotOrBuildingAsync()
      throws GameActionException {
    return attackWeakestRobotOrBuildingAsync(true);
  }

  /**
   * Position yourself optimally to attack next round.
   * 
   * @param target the attack target
   * @return whether we can attack this turn
   * @throws GameActionException
   */
  public static boolean positionForAttack(MapLocation target)
      throws GameActionException {
    if (!_unarmed())
      return false;
    // Are all of our weapon controllers in range?
    boolean allControllersInRange = true;
    // TODO(landa): currently the strongest weapon is not used and this method
    // assumes that all of the weapons are the same
    // TODO(landa): prioritize SMG, Railgun, Blaster for distance
    WeaponController strongestWeapon = null;
    for (WeaponController weapon : S.weaponControllers) {
      if (strongestWeapon == null
          || weapon.type().attackPower > strongestWeapon.type().attackPower) {
        strongestWeapon = weapon;
      }
      if (!weapon.withinRange(target))
        allControllersInRange = false;
    }
    // If all of the weapons are in range, then we want to stay in range, and
    // back up if we stay in range.
    if (allControllersInRange) {
      // If we can move backwards and stay in range, then we should.
      Direction opposite = S.direction.opposite();
      MapLocation behind = S.location.add(opposite);
      if (_withinRange(behind, target, strongestWeapon)
          && !S.movementController.isActive()
          && S.movementController.canMove(opposite)) {
        // TODO(landa): back up sideways if you can't move backwards (see
        // _backupAndKeepInRangeAsync)
        X.moveBackward();
        return true; // Can attack this round.
      }
    }
    // If all of the weapons are not in range, then we want to get in range.
    X.moveTowardsAsync(S.location.directionTo(target));
    return false; // Can't attack this round.
  }

  public static int numEnemiesInSensor() {
    int numEnemies = 0;
    for (Robot groundRobot : S.nearbyRobots())
      if (groundRobot.getTeam().equals(S.enemyTeam))
        numEnemies++;
    return numEnemies;
  }

  public static boolean tactic_checkForAttackFromBehind()
      throws GameActionException {
    if (S.dHp < 0 && CombatBlock.numEnemiesInSensor() == 0) {
      if (!S.movementController.isActive())
        X.setDirection(S.direction.opposite());
      return true;
    }
    return false;
  }

  // ----------------------------------------------------------------------

  private static boolean _withinRange(MapLocation from, MapLocation to,
      WeaponController weapon) {
    return from.distanceSquaredTo(to) <= weapon.type().range;
  }

  /**
   * Makes a robot back away in a general direction.
   * 
   * @throws GameActionException
   */
  @Deprecated
  public static final void _backupAndKeepInRangeAsync()
      throws GameActionException {
    // FIXME(landa): Check that the weapon is still in range even with the
    // different location and direction.
    Direction opposite = S.direction.opposite();
    if (S.movementController.canMove(opposite))
      X.moveBackward();
    else {
      boolean left = S.rand.nextBoolean();
      if (left) {
        if (S.movementController.canMove(opposite.rotateRight()))
          X.setDirection(opposite.rotateRight());
        else if (S.movementController.canMove(opposite.rotateRight().rotateRight()))
          X.setDirection(opposite.rotateRight().rotateRight());
      } else {
        if (S.movementController.canMove(opposite.rotateLeft()))
          X.setDirection(opposite.rotateLeft());
        else if (S.movementController.canMove(opposite.rotateLeft().rotateLeft()))
          X.setDirection(opposite.rotateLeft().rotateLeft());
      }
    }
  }

  public static final boolean _unarmed() {
    return S.weaponControllers == null || S.weaponControllers.length == 0;
  }

  public static boolean _canAttack(MapLocation target) {
    if (_unarmed())
      return false;
    for (WeaponController weapon : S.weaponControllers) {
      if (!weapon.isActive() && weapon.withinRange(target))
        return true;
    }
    return false;
  }

  public static void _clearTarget() {
    bestTarget = null;
    bestLevel = null;
    bestValue = Double.MAX_VALUE;
  }

  public static void _setWeakestNearbyRobotOrBuildingAsTarget() {
    _clearTarget();
    double tempBestValue = Double.MAX_VALUE;
    for (RobotInfo ri : S.nearbyRobotInfos()) {
      if (ri != null && ri.robot.getTeam() == S.enemyTeam) {
        double curValue = ri.hitpoints;
        MapLocation curLoc = ri.location;
        if (S.weaponControllers[0].withinRange(ri.location)
            && curValue < tempBestValue) {
          bestLevel = ri.chassis.level;
          bestValue = curValue;
          bestTarget = curLoc;
          if (S.rand.nextDouble() < PROB_SHOOT_BUILDING
              && ri.chassis == Chassis.BUILDING)
            return;
        }
      }
    }
  }

  // ----------------------------------------------------------------------

  public static MapLocation bestTarget = null;
  public static RobotLevel bestLevel = null;
  public static double bestValue = Double.MAX_VALUE;

}
