package sprint3.chaos;

import sprint3.blocks.RandomMovementBlock;
import sprint3.core.B;
import sprint3.core.CommandType;
import sprint3.core.D;
import sprint3.core.S;
import sprint3.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class LandaUtil {

  // ----------------------------------------------------------------------
  public static boolean alreadySent = false;
  public static boolean alreadyEquipped = false;

  /**
   * Build an Antenna on yourself and send out an attack location broadcast.
   * 
   */
  public static final void broadcastAttackLocation() {
    try {
      if (S.chassis.equals(Chassis.BUILDING)) {
        if (B.bc == null) {
          S.builderController.build(ComponentType.ANTENNA, S.location, RobotLevel.ON_GROUND);
          X.yield();
        }
        else {
          if (!alreadyEquipped) {
            MapLocation nearestConstructor = null;
            RobotInfo[] nearbyRobots = S.nearbyRobotInfos();
            for (RobotInfo robotInfo : nearbyRobots) {
              if (!robotInfo.chassis.equals(Chassis.BUILDING)) {
                for (ComponentType componentType : robotInfo.components)
                  if (componentType.equals(ComponentType.SMG))
                    alreadyEquipped = true;
                nearestConstructor = robotInfo.location;
              }
            }
            if (nearestConstructor != null) {
              S.builderController.build(ComponentType.SMG, nearestConstructor, RobotLevel.ON_GROUND);
              X.yield();
              alreadyEquipped = true;
            }
          }
          if (!alreadySent && !B.bc.isActive()) {
            int[] message = CommandType.ATTACK_TARGET.ints;
            message[0] = 0;
            message[1] = S.locationX - 1;
            message[2] = S.locationY + 42;
            B.send(message);
            alreadySent = true;
            X.yield();
          }
        }
      }
      else {
//        if (strategy == null) {
//          strategy = new ShootTargetBlock();
//          NavigationBlock.init();
//        }
//        strategy.step();
      }
    } catch (Exception e) {
      D.debug_logException(e);
    }
  }

  public static final boolean robotInfoHasComponent(RobotInfo ri, ComponentType component) {
    for (ComponentType cur : ri.components) {
      if (cur.equals(component)) {
        return true;
      }
    }
    return false;
  }
  
  public static void sync_moveToAdjacentEmpty() throws GameActionException {
    while (S.movementController.isActive()) {
      X.yield();
    }
    Direction nextDirection = RandomMovementBlock.randomAvailableDirection();
    X.moveTowardsSync(nextDirection);
  }

  public final void try_equip(ComponentType component, MapLocation goal, RobotLevel level) throws GameActionException {
    if (S.flux >= component.cost && !B.bc.isActive()) {
      SyncBuildBlock.tryEquip(component, goal, level);
    }
    else {
      X.yield();
    }
  }

  // ----------------------------------------------------------------------

}
