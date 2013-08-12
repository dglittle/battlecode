package sprint2.util;

import sprint2.blocks.NavigationBlock;
import sprint2.blocks.ShootTargetBlock;
import sprint2.blocks.Strategy;
import sprint2.core.B;
import sprint2.core.CommandType;
import sprint2.core.S;
import sprint2.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class LandaUtil {

  // ----------------------------------------------------------------------
  public static boolean alreadySent = false;
  public static boolean alreadyEquipped = false;
  public static Strategy strategy;

  // Build an Antenna on yourself and send out an attack location broadcast.
  public static final void broadcastAttackLocation() {
    try {
      if (S.chassis.equals(Chassis.BUILDING)) {
        if (B.bc == null) {
          X.build(ComponentType.ANTENNA, S.location, RobotLevel.ON_GROUND);
          Log.out("Building an antenna for myself.");
        } else {
          if (!alreadyEquipped) {
            MapLocation nearestConstructor = null;
            RobotInfo[] nearbyRobots = S.nearbyRobotInfo();
            for (RobotInfo robotInfo : nearbyRobots) {
              if (!robotInfo.chassis.equals(Chassis.BUILDING)) {
                for (ComponentType componentType : robotInfo.components)
                  if (componentType.equals(ComponentType.SMG))
                    alreadyEquipped = true;
                nearestConstructor = robotInfo.location;
              }
            }
            if (nearestConstructor != null) {
              X.build(ComponentType.SMG, nearestConstructor,
                  RobotLevel.ON_GROUND);
              alreadyEquipped = true;
            }
          }
          if (!alreadySent && !B.bc.isActive()) {
            Log.out("Preparing to send location " + S.location);
            int[] message = CommandType.ATTACK_TARGET.ints;
            message[0] = 0;
            message[1] = S.locationX - 1;
            message[2] = S.locationY + 42;
            B.send(message);
            alreadySent = true;
            X.yield();
          }
        }
      } else {
        if (strategy == null) {
          strategy = new ShootTargetBlock();
          NavigationBlock.init();
        }
        strategy.step();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public final void try_equip(ComponentType component, MapLocation goal,
      RobotLevel level) throws GameActionException {
    if (S.flux >= component.cost && !B.bc.isActive()) {
      Log.out("We're at " + S.rc.getLocation().toString()
          + " ready to build at " + goal.toString() + ".");
      X.tryBuild(component, goal, level);
      S.rc.yield();
    } else {
      S.rc.yield();
    }
  }

  // ----------------------------------------------------------------------

}
