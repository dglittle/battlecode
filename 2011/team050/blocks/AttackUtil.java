package team050.blocks;

import java.util.HashSet;
import java.util.Set;

import team050.core.S;
import team050.core.X;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class AttackUtil {

  // Base rating + Bonus
  public static final double WEIGHT_FLYING = 30 + 15;
  public static final double WEIGHT_LOOKING_AT_US = 0 + 10;
  public static final double WEIGHT_HEAVY = 0 + 5;
  public static final double WEIGHT_MEDIUM = 20;
  public static final double WEIGHT_BUILDING = 10;
  public static final double WEIGHT_LIGHT = 28 - 5;

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
      id = ri.robot.getID();
      if (ri != null && ri.robot.getTeam() == S.enemyTeam && !honeypotIgnoreIDs.contains(id)) {
        if (id == lastAttackID
            && S.round <= lastAttackRound + 5
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

  public static boolean attackAsync() {
    setTargets();

    if (bestImmediateTarget != null) {
      X.attack(bestImmediateTarget, bestImmediateTargetLevel);
      if (X.unloadedAll) {
        lastAttackID = bestImmediateTargetID;
        lastAttackHP = bestImmediateTargetHP;
      }
      return true;
    } else if (bestTarget != null) {
      X.attack(bestTarget, bestTargetLevel);
      return true;
    }
    return false;
  }

  public static RobotInfo getClosestMobileEnemy() {
    RobotInfo best = null;
    int bestDist = Integer.MAX_VALUE;
    for (RobotInfo ri : S.nearbyRobotInfos()) {
      if (ri.on && ri.robot.getTeam() == S.enemyTeam
          && ri.chassis.motor.delay > 2) {
        int dist = S.location.distanceSquaredTo(ri.location);
        if (dist < bestDist) {
          bestDist = dist;
          best = ri;
        }
      }
    }
    return best;
  }
}
