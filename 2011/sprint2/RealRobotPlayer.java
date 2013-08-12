package sprint2;

import sprint2.core.S;
import sprint2.core.X;
import sprint2.toplevel.BuildFleetTopLevel;
import sprint2.toplevel.BuildRecyclerOnVisibleMinesTopLevel;
import sprint2.toplevel.IndependentDefenderTopLevel;
import sprint2.toplevel.IndependentMineTopLevel;
import sprint2.util.FleetUtil;
import sprint2.util.LittleUtil;
import sprint2.util.RobotUtil;
import battlecode.common.ComponentType;
import battlecode.common.RobotController;

public class RealRobotPlayer implements Runnable {

  public RealRobotPlayer(RobotController controller) {
    X.init(controller);
  }

  @Override
  public void run() {
    while (true) {
      try {
        switch (S.chassisInt) {
        case RobotUtil.LIGHT_INT:
          if (LittleUtil.hasComponents(ComponentType.BLASTER)) {
            IndependentDefenderTopLevel.run();            
          }
          else if (LittleUtil.hasComponents(ComponentType.CONSTRUCTOR)) {
            if (S.birthRound <= 200 && LittleUtil.hasComponents(ComponentType.SIGHT)) {
              BuildRecyclerOnVisibleMinesTopLevel.run();
            }
            else {
              if (S.birthRound > 200)
                BuildFleetTopLevel.run();
            }
          }
          break;
        case RobotUtil.BUILDING_INT:
          if (S.birthRound <= 2) {
            BuildFleetTopLevel.run();
          }
          else {
            if (LittleUtil.hasComponents(ComponentType.RECYCLER)) {
              IndependentMineTopLevel.run();
            }
            else if (LittleUtil.hasComponents(ComponentType.ARMORY)) {
              BuildFleetTopLevel.run();
            }
          }
          break;
        case RobotUtil.FLYING_INT:
          FleetUtil.go();
          break;
        }
        X.yield();
      } catch (Exception e) {
        // This'll make us fail tests.
        e.printStackTrace();
      }
    }
  }
}
