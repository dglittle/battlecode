package legenClone;

import legenClone.core.S;
import legenClone.core.X;
import legenClone.toplevel.BuildFleetTopLevel;
import legenClone.toplevel.BuildRecyclerOnVisibleMinesTopLevel;
import legenClone.toplevel.IndependentDefenderTopLevel;
import legenClone.toplevel.IndependentMineTopLevel;
import legenClone.util.FleetUtil;
import legenClone.util.LegenClone;
import legenClone.util.LittleUtil;
import legenClone.util.RobotUtil;
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
        LegenClone.go();
        X.yield();
      } catch (Exception e) {
        // This'll make us fail tests.
        e.printStackTrace();
      }
    }
  }
}
