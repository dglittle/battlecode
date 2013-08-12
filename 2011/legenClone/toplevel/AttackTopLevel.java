package legenClone.toplevel;

import legenClone.core.S;
import legenClone.core.X;
import legenClone.util.MapUtil;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public final class AttackTopLevel {
  public static final int SEEK = 0;
  public static final int FIRE = 1;
  public static int state = SEEK;
  public static MapLocation targetLocation;
  public static RobotLevel targetLevel;
  
  public static final void step() throws GameActionException {
    if (S.maxWeaponRange == 0)
      return;
    switch(state) {
      case SEEK:
        if (S.nearbyRobots().length == 0) {
          if (!S.movementController.isActive()) {
            X.setDirection(MapUtil.intToDirection[(S.direction.ordinal() + 2) % 8]);
            return;
          }
        } else {
          state = FIRE;
          RobotInfo[] nearbyRobotInfo = S.nearbyRobotInfo();
          targetLocation = nearbyRobotInfo[0].location;
          targetLevel = nearbyRobotInfo[0].chassis.level;
        }
        break;
      case FIRE:
        if (S.weaponControllers[0].isActive())
          break;
        S.weaponControllers[0].attackSquare(targetLocation, targetLevel);
        return;
      default:
        break;
    }
  }
}
