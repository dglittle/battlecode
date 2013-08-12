package legenClone.util;

import battlecode.common.GameActionException;
import legenClone.core.S;
import legenClone.core.X;

public class YingUtil {
  public static final void p(String s) {
    System.out.println(s);
  }
  
  public static final void p() {
    System.out.println("flux = " + S.flux);
  }
  
  public static void waitForFlux(double fluxCost, double upkeep) throws GameActionException {
    while (S.flux < fluxCost || S.dFlux < upkeep) {
      X.yield();
    }
  }
}

