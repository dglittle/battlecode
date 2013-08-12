package team050.blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import team050.blocks.pathfinding.BugPathfindingBlock;
import team050.core.D;
import team050.core.S;
import team050.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.JumpController;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;
import battlecode.common.WeaponController;

/**
 * Combat block for general combat.
 */
public class CombatBlock {

  // --- Parameters -------------------------------------------------------

  public static final double PROB_SHOOT_BUILDING = 0.5;

  public static CombatAttackTactics[] attackTactics = new CombatAttackTactics[]{CombatAttackTactics.BEHIND_BACK,};

  // --- Methods ----------------------------------------------------------

  /**
   * Find the target, position yourself for better shooting, and attack.
   * 
   * @return
   * @throws GameActionException
   */
  public static final boolean attackBestTargetAsync()
      throws GameActionException {
    // Find the target, possibly position yourself for better shooting, and
    // attack.
    setTargets();
    if (bestImmediateTarget != null)
      return attackTargetAsync(bestImmediateTarget, bestImmediateTargetLevel);
    else if (bestTarget != null)
      return attackTargetAsync(bestTarget, bestTargetLevel);
    else
      return false;
  }

  public static final boolean attackTargetAsync(MapLocation target,
      RobotLevel level) throws GameActionException {
    if (_unarmed())
      return false;
    if (target != null) {
      D.debug_pl("attackWeakestRobotOrBuildingAsync",
          "Positioning for attack...");
      positionForAttack(target);
      if (_canAttack(target)) {
        // Try an attack tactic.
        int tactic = S.randomInt(attackTactics.length);
        attackTactics[tactic].attack(target, level);
      }
      return true;
    }
    return false;
  }

  public static void setTargets() {
    bestTarget = null;
    double bestValue = -Double.MAX_VALUE;

    bestImmediateTarget = null;
    bestImmediateTargetLevel = null;
    double bestImmediateValue = -Double.MAX_VALUE;

    int id = 0;
    RobotLevel level = null;
    MapLocation loc = null;

    for (RobotInfo ri : S.nearbyRobotInfos()) {
      if (ri == null) {
        continue;
      }
      id = ri.robot.getID();
      if (ri.robot.getTeam() == S.enemyTeam && !honeypotIgnoreIDs.contains(id)) {
        if (id == lastAttackID && S.round <= lastAttackRound + 5
            && ri.hitpoints >= lastAttackHP) {
          honeypotIgnoreIDs.add(id);
          continue;
        }
        level = ri.chassis.level;
        double value = -ri.hitpoints;
        switch (ri.chassis) {
          case HEAVY:
            value += WEIGHT_HEAVY;
            break;
          case MEDIUM:
            value += WEIGHT_MEDIUM;
            break;
          case LIGHT:
            value += WEIGHT_LIGHT;
            break;
          case BUILDING:
            value += WEIGHT_BUILDING;
            break;
          case FLYING:
            value += WEIGHT_FLYING;
            break;
        }
        loc = ri.location;
        if ((loc.directionTo(S.location).ordinal() - ri.direction.ordinal() + 8) % 8 <= 1)
          value += WEIGHT_LOOKING_AT_US;
        if (value > bestValue) {
          bestValue = value;
          bestTarget = loc;
          bestTargetLevel = level;
        }
        if (S.minWeaponRangeController.withinRange(loc)
            && value > bestImmediateValue) {
          bestImmediateValue = value;
          bestImmediateTarget = loc;
          bestImmediateTargetLevel = level;
          bestImmediateTargetID = id;
          bestImmediateTargetHP = ri.hitpoints;
        }
      }
    }
  }

  /**
   * Position yourself optimally to attack next round.
   * 
   * @param target the attack target
   * @throws GameActionException
   */
  public static void positionForAttack(MapLocation targetLocation)
      throws GameActionException {
    if (_unarmed())
      return;

    Direction directionToTarget = S.location.directionTo(targetLocation);

    // -- All of the weapon controllers are in range. --
    if (S.minWeaponRangeController.withinRange(targetLocation)) {
      // We are in range. Try to kite.
      Direction opposite = S.direction.opposite();
      MapLocation behind = S.location.add(opposite);

      if (_withinShortestWeaponDistance(behind, targetLocation) && S.motorReady
          && S.movementController.canMove(opposite)) {
        D.debug_pl("positionForAttack", "Moving backwards.");
        X.moveBackward();
        return; // Can attack this round.
      } else {
        D.debug_pl("positionForAttack", "Doing nothing.");
        return;
      }
    }
    // -- Not all of the weapon controllers are in range. --
    else {
      // --- If we can jump, try to jump away and turn so that they have to turn and miss a round as well. ---
      if (S.jumpControllers != null) {
        if (S.direction != directionToTarget) {
          Direction orthogonalDirection = S.randomBoolean()
              ? S.location.directionTo(targetLocation).rotateLeft().rotateLeft()
              : S.location.directionTo(targetLocation).rotateRight().rotateRight();
          MapLocation orthogonalJumpLocation = null;
          if (S.jumpReady()) {
            orthogonalJumpLocation = JumpUtil.jumpAndReturnLocation(orthogonalDirection);
          }
          if (S.motorReady) {
            if (orthogonalJumpLocation != null)
              X.setDirectionChecked(orthogonalJumpLocation.directionTo(targetLocation));
            else
              X.setDirectionChecked(directionToTarget);
          }
        } else {
          boolean jumped = false;
          if (S.jumpReady()) {
            MapLocation possibleJumpLocation = JumpUtil.furthestDefiniteJumpLocationWithinRangeOf(
                targetLocation, S.minWeaponRange);
            if (possibleJumpLocation != null) {
              jumped = X.jump(possibleJumpLocation);
              if (jumped && S.motorReady)
                X.setDirectionChecked(possibleJumpLocation.directionTo(targetLocation));
            } else {
              // hm.. we can't jump closer to them..
              // let's turn around and explore away
              if (S.motorReady) {
                X.setDirectionChecked(directionToTarget.opposite());
                JumpExplorationBlock.macroDir = directionToTarget.opposite();
              }
            }
          }
          if (!jumped) {
            X.moveTowardsAsync(directionToTarget);
          }
        }
      }
      // --- We don't have jumps. Try to bug towards the target. ---
      else {
        BugPathfindingBlock.bugTowardAsync(targetLocation);
      }

      return;
    }
  }

  public static void shootAsync(MapLocation targetLocation) {
    if (bestImmediateTarget == targetLocation) {
      X.attack(bestImmediateTarget, bestImmediateTargetLevel);
      if (X.unloadedAll) {
        lastAttackID = bestImmediateTargetID;
        lastAttackHP = bestImmediateTargetHP;
      }
    } else if (bestTarget == targetLocation) {
      X.attack(bestTarget, bestTargetLevel);
    }
  }

  /**
   * @return the number of robots facing us in sensor range
   */
  public static int numAttackersInSensor() {
    int numEnemies = 0;
    for (RobotInfo ri : S.nearbyRobotInfos())
      if (ri.robot.getTeam().equals(S.enemyTeam)
          && (ri.location.directionTo(S.location).ordinal()
              - ri.direction.ordinal() + 8) % 8 <= 1)
        numEnemies++;
    return numEnemies;
  }

  /**
   * Checks if our HP is decreasing and there are no robots that face us in
   * view.
   * 
   * @return
   * @throws GameActionException
   */
  public static boolean checkForAttackFromBehind() throws GameActionException {
    if (S.dHp < 0 && CombatBlock.numAttackersInSensor() == 0) {
      return true;
    }
    return false;
  }

  // ----------------------------------------------------------------------

  private static boolean _withinShortestWeaponDistance(MapLocation from,
      MapLocation to) {
    return from.distanceSquaredTo(to) <= S.minWeaponRange;
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

  /**
   * Sets the attack target to be either the weakest building (with probability
   * PROB_SHOOT_BUILDING) or the weakest robot.
   */
  public static void _setAttackTarget() {
    // FIXME
  }

  // --- Tactics ----------------------------------------------------------

  public enum CombatAttackTactics {
    SIMPLE, BEHIND_BACK, DODGE;
    public final void attack(MapLocation target, RobotLevel level)
        throws GameActionException {
      D.debug_pl("attack", "Using combat tactic " + this.toString());
      switch (this) {
        case SIMPLE:
          shootAsync(target);
          break;
        case BEHIND_BACK:
          if (S.jumpReady()) {
            MapLocation[] behindEnemy = new MapLocation[]{
                target.add(S.location.directionTo(target)),
                target.add(S.location.directionTo(target).rotateLeft()),
                target.add(S.location.directionTo(target).rotateRight())};
            for (MapLocation oneFurther : behindEnemy) {
              for (JumpController jc : S.jumpControllers) {
                TerrainTile tt = S.rc.senseTerrainTile(oneFurther);
                if (S.sensorController.canSenseSquare(oneFurther)
                    && S.senseRobot(oneFurther, RobotLevel.ON_GROUND) == null
                    && (!S.movementController.isActive() || S.direction == oneFurther.directionTo(target))
                    && !jc.isActive() && jc.withinRange(oneFurther)
                    && !(tt != null && !tt.isTraversableAtHeight(S.level))) {
                  D.debug_pl("attack",
                      "All conditions met. Attacking behind the enemy's back.");
                  X.setDirectionChecked(oneFurther.directionTo(target));
                  jc.jump(oneFurther);
                  shootAsync(target);
                  return;
                }
              }
            }
          }
          D.debug_pl("attack", "Resorting to a SIMPLE attack.");
          CombatAttackTactics.SIMPLE.attack(target, level);
          break;
        case DODGE:
          Direction randDir = RandomMovementBlock.randomAvailableDirection();
          if (randDir == null) {
            shootAsync(target);
            break;
          }
          MapLocation sideways = S.location.add(randDir);
          for (JumpController jc : S.jumpControllers) {
            TerrainTile tt = S.rc.senseTerrainTile(sideways);
            if (S.sensorController.canSenseSquare(sideways)
                && S.senseRobot(sideways, RobotLevel.ON_GROUND) == null
                && !jc.isActive() && jc.withinRange(sideways)
                && !(tt != null && !tt.isTraversableAtHeight(S.level))) {
              jc.jump(sideways);
              break;
            }
          }
          shootAsync(target);
          break;
      }
    }
  };

  // --- Debug ------------------------------------------------------------

  public static int debug_numNearbyEnemyRobots() {
    List<Robot> enemies = new ArrayList<Robot>();
    for (Robot robot : S.nearbyRobots())
      if (robot.getTeam().equals(S.enemyTeam))
        enemies.add(robot);
    return enemies.size();
  }

  // --- Constants --------------------------------------------------------

  // Base rating + Bonus
  public static final double WEIGHT_FLYING = 30 + 15;
  public static final double WEIGHT_LOOKING_AT_US = 0 + 10;
  public static final double WEIGHT_HEAVY = 0 + 5;
  public static final double WEIGHT_MEDIUM = 20;
  public static final double WEIGHT_BUILDING = 10;
  public static final double WEIGHT_LIGHT = 28 - 5;

  // ----------------------------------------------------------------------

  public static MapLocation bestTarget = null;
  public static RobotLevel bestTargetLevel = null;

  public static MapLocation bestImmediateTarget = null;
  public static RobotLevel bestImmediateTargetLevel = null;
  public static int bestImmediateTargetID = 0;
  public static double bestImmediateTargetHP = 0;

  public static int lastAttackID = 0;
  public static int lastAttackRound = Integer.MIN_VALUE;
  public static double lastAttackHP = Double.MAX_VALUE;

  public static Set<Integer> honeypotIgnoreIDs = new HashSet<Integer>();

}
