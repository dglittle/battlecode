package heavy1.chaos;

import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import heavy1.core.D;
import heavy1.core.S;
import heavy1.core.X;

public class YingUtil {
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

