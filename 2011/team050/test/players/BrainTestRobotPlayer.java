package team050.test.players;

import team050.RealRobotPlayer;
import team050.blocks.Colonist;
import team050.blocks.GenericSoldier;
import team050.blocks.brain.BrainTopLevel;
import team050.chaos.little.HeavyColonist;
import team050.chaos.pwnall.RoleDetectionBlock;
import team050.core.D;
import team050.core.S;
import team050.core.X;
import team050.toplevel.InitialColonistBuildTopLevel;
import battlecode.common.Chassis;
import battlecode.common.RobotController;

public class BrainTestRobotPlayer extends RealRobotPlayer {
  public BrainTestRobotPlayer(RobotController controller) {
    super(controller);
  }

  public void run() {
    RoleDetectionBlock.waitForRoleSync();
    
    try {
      if (S.chassis == Chassis.LIGHT && S.birthRound <= 5) {
        InitialColonistBuildTopLevel.go();
      }
    } catch (Exception e) {
      D.debug_logException(e);
    }

    while (true) {
      try {
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
        X.yield();
      } catch (Exception e) {
        D.debug_logException(e);
        S.rc.suicide();
      }
    }
  }
}
