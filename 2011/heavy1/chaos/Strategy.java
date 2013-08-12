package heavy1.chaos;

import battlecode.common.GameActionException;

public interface Strategy {
  public void init() throws GameActionException;
  public void step() throws GameActionException;
}
