package heavy1;

import heavy1.blocks.HeavySoldier;
import heavy1.blocks.LittleColonist;
import heavy1.blocks.LittleHeavyColonist;
import heavy1.blocks.Soldier;
import heavy1.chaos.little.Jumper;
import heavy1.chaos.little.Nexus;
import heavy1.core.D;
import heavy1.core.Role;
import heavy1.core.S;
import heavy1.core.U;
import heavy1.core.X;
import heavy1.toplevel.InitialColonistBuildTopLevel;
import battlecode.common.Chassis;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.RobotController;

public class RealRobotPlayer implements Runnable {

  public RealRobotPlayer(RobotController controller) {
    try {
      X.init(controller);
    } catch (Exception e) {
      D.debug_logException(e);
    }
  }

  @Override
  public void run() {

    try {
      if (S.chassis == Chassis.LIGHT && S.birthRound <= 2) {
        InitialColonistBuildTopLevel.go();
        Nexus.constructorBuildBaseSync();
      }
    } catch (Exception e) {
      D.debug_logException(e);
    }

    while (true) {
      try {
        if (S.chassis == Chassis.BUILDING) {
          Nexus.buildingGo();
        }

        if (S.role == null) {
          for (Role r : Role.values()) {
            if (S.chassis == r.chassis && U.allComponentsReady()
                && U.hasComponents(r.components)) {
              S.role = r;
              break;
            }
          }
        }

        if (S.role != null) {
          
          
          // work here
          System.out.println("role: " + S.role);
          
          
          switch (S.role) {
            case JUMPER:
              Jumper.go();
              break;

            case COLONIST:
            case INITIAL_COLONIST:
              LittleColonist.go();
              break;
            case SOLDIER:
              Soldier.go();
              break;

            case FLYING_COLONIST:
              LittleColonist.go();
              break;
            case FLYING_SOLDIER:
              Soldier.go();
              break;

            case HEAVY_COLONIST:
              LittleHeavyColonist.go();
              break;
            case HEAVY_SOLDIER:
              HeavySoldier.go();
              break;
          }
        }

        X.yield();
      } catch (Exception e) {
        D.debug_logException(e);
        S.rc.suicide();
      }
    }
  }
}
