package team050;

import team050.blocks.Colonist;
import team050.blocks.GenericSoldier;
import team050.blocks.brain.BrainTopLevel;
import team050.chaos.pwnall.RoleDetectionBlock;
import team050.core.D;
import team050.core.S;
import team050.core.X;
import team050.toplevel.InitialColonistBuildTopLevel;
import battlecode.common.Chassis;
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
          case FLYING_COLONIST:
          case HEAVY_COLONIST:
            Colonist.sync();
            break;
            
          case SOLDIER:
          case FLYING_SOLDIER:
          case HEAVY_SOLDIER:
          case HEAVY_SOLDIER_HAMMERS:
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
