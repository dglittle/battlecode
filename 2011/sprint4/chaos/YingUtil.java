package sprint4.chaos;

import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import sprint4.core.D;
import sprint4.core.S;
import sprint4.core.X;

public class YingUtil {
  public static final void debug_p(String s) {
    System.out.println("==YY==: " + s);
  }
  
  public static final void p() {
    System.out.println("flux = " + S.flux);
  }
  
  public static void waitForFlux(double fluxCost, double upkeep) throws GameActionException {
    while (S.flux < fluxCost || S.dFlux < upkeep) {
      D.debug_setIndicator(0, "dFlux = " + S.dFlux + " upkeep = " + upkeep);
      X.yield();
    }
  }
  
  public static int componentCost(ComponentType[] components) {
    int cost = 0;
    for (ComponentType component : components) {
      cost += component.cost;
    }
    return cost;
  }
}

