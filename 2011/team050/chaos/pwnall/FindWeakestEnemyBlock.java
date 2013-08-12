package team050.chaos.pwnall;

import team050.core.S;
import battlecode.common.Chassis;
import battlecode.common.RobotInfo;

/** Smarts for finding the weakest enemy, so we can blow their heads off. */
public class FindWeakestEnemyBlock {
  /** 
   * Recomputes the weakest enemy.
   * 
   * Don't call more than once per round.
   */
  public static final void update() {
    currentTargetInfo = nextTargetInfo = null;
    
    /** The best thing we can shoot right now. */
    double minCurrentScore = Double.MAX_VALUE;
    /** The absolute best thing we can shoot. */
    double minScore = Double.MAX_VALUE;
    /** The HP we assume we can take down in one shot. */
    double oneShot = S.totalAttackPower * 0.8;
    
    final RobotInfo[] infos = S.nearbyRobotInfos();
    for (int i = infos.length - 1; i >= 0; i--) {
      final RobotInfo info = infos[i];
      if (info == null || info.robot.getTeam() != S.enemyTeam) { continue; }

      // Lowest scores are better (weaker enemies).
      final double hitpoints = info.hitpoints;
      double enemyScore = hitpoints;
      final int enemyRange = info.location.distanceSquaredTo(S.location);
      final boolean inMinRange = enemyRange < S.minWeaponRange;
      
      if (hitpoints <= oneShot && inMinRange) {
        // We can take this down in one hit.
        if (info.chassis == Chassis.BUILDING) {
          enemyScore -= 400.0;
        }
      } else {
        if (info.chassis == Chassis.BUILDING) {
          enemyScore += 5.0;
        }
      }
      final boolean inRange = S.weaponControllers[0].withinRange(info.location);
      // HPs will never exceed 100, so we can use numbers above 100 to create
      // priorities. Multiples should be powers of 2 so that the math works out.
      // if (inMinRange) { enemyScore -= 100; }
      if (info.on) { enemyScore -= 200.0; }

      if (enemyScore < minScore) {
        minScore = enemyScore;
        nextTargetInfo = info;
      }
      if (enemyScore < minCurrentScore && inRange) {
        minCurrentScore = enemyScore;
        currentTargetInfo = info;
      }
    }
  }
  
  /** If not null, we can attack this target right now. */
  public static RobotInfo currentTargetInfo;
  
  /** If null, we should attack this target next, but can't do it right now. */
  public static RobotInfo nextTargetInfo;
}
