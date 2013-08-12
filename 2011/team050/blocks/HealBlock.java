package team050.blocks;

import team050.core.S;
import team050.core.X;
import battlecode.common.RobotInfo;

/** Heals oneself or the nearby robot in the sensor range that has the most 
 * damage. The recycler itself should have the highest priority. 
 */
public class HealBlock {
  
  /**
   * 
   * @return true if a heal action is performed.
   */
  public static final boolean async() {
    if (S.medicControllers.length == 0) { return false; }
    if (S.chassis.maxHp - S.hp > 0) { 
      X.heal(S.location, S.level); 
      return true;
    }
    final RobotInfo[] ris = S.nearbyRobotInfos();
    RobotInfo mostDamagedRobot = null;
    double highestDamage = 0;
    for (int i = ris.length - 1; i >= 0; i--) {
      final RobotInfo ri = ris[i];
      if (ri.robot.getTeam() == S.team) {
        // Increase the damage score for the robots that are on, so they have the priority to be
        // healed.
        double damage = ri.chassis.maxHp - ri.hitpoints;
        if (damage <= 0) continue;
        damage += ri.on ? 100 : 0;
        if (damage >= highestDamage) {
           highestDamage = damage;
           mostDamagedRobot = ri;
        }
      }
    }
    if (mostDamagedRobot != null) {
      X.heal(mostDamagedRobot);
      return true;
    }
    return false;
  }
}
