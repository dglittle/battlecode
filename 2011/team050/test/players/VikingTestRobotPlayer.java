package team050.test.players;

import team050.blocks.Colonist;
import team050.blocks.GenericSoldier;
import team050.blocks.brain.BrainTopLevel;
import team050.chaos.little.HeavyColonist;
import team050.core.D;
import team050.core.Role;
import team050.core.S;
import team050.core.U;
import team050.core.X;
import team050.toplevel.InitialColonistBuildTopLevel;
import battlecode.common.Chassis;
import battlecode.common.RobotController;

public class VikingTestRobotPlayer implements Runnable {

  public VikingTestRobotPlayer(RobotController controller) {
    try {
      X.init(controller);
    } catch (Exception e) {
      D.debug_logException(e);
    }
  }

  @Override
  public void run() {
    try {
      if (S.chassis == Chassis.LIGHT && S.birthRound <= 5) {
        S.role = Role.COLONIST;
        InitialColonistBuildTopLevel.go();
      }
    } catch (Exception e) {
      D.debug_logException(e);
    }

    final Role[] roles = Role.values();
    while (true) {
      if (S.role == null) {
        for (int i = roles.length - 1; i >= 0; i--) {
          final Role role = roles[i];
          if (S.chassis != role.chassis) { continue; }
          if (!U.allComponentsReady()) { continue; }
          if (!U.hasComponents(role.components)) { continue; }
          S.role = role;
          break;
        }
      }

      try {
        if (S.role != null) {
          switch (S.role) {
            case COLONIST:
              Colonist.sync();
              break;
            case SOLDIER:
              GenericSoldier.go();
              break;

            case FLYING_COLONIST:
              Colonist.sync();
              break;
            case FLYING_SOLDIER:
              GenericSoldier.go();
              break;

            case HEAVY_COLONIST:
              HeavyColonist.go();
              break;
            case HEAVY_SOLDIER:
              GenericSoldier.go();
              break;
            case RECYCLER:
            case ARMORY:
            case FACTORY:
              BrainTopLevel.sync();
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
