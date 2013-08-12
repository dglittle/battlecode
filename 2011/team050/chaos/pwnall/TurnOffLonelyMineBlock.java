package team050.chaos.pwnall;

import team050.core.S;
import battlecode.common.Mine;
import battlecode.common.SensorController;

/** Turns off lonely mines selectively. */
public class TurnOffLonelyMineBlock {
  /** Progress towards turning itself off. */
  public static final boolean async() {
    if (!_isLonelyMine()) { return true; }
    if (!_shouldBeTurnedOff()) { return false; }
    
    S.rc.turnOff();
    return true;
  }
    
  /** True if the current recycler should be turned off, provided it's alone. */
  public static final boolean _shouldBeTurnedOff() {
    return S.id % 2 == 0;
  }
  
  /** True if the current robot is not surrounded by any other mine. */
  public static final boolean _isLonelyMine() {
    SensorController sensor = (S.buildingSensorController != null) ?
        S.buildingSensorController : S.sensorController;

    Mine[] mines = sensor.senseNearbyGameObjects(Mine.class);
    for (Mine mine : mines) {
      if (mine.getLocation().isAdjacentTo(S.location)) {
        return false;
      }
    }
    return true;
  }  
}
