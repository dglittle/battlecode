package flyingRush.chaos;

import flyingRush.core.S;
import battlecode.common.GameActionException;

public class InitialRecyclersBuildTopLevel {

  /**
   * Turns itself off.
   *  
   */
  public static final void go() throws GameActionException {
    S.rc.turnOff();
  }
}
