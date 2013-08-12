package sprint3.chaos;

import sprint3.core.S;
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
