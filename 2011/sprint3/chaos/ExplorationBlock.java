package sprint3.chaos;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import sprint3.blocks.PathfindingBlock;
import sprint3.core.D;
import sprint3.core.M;
import sprint3.core.S;
import sprint3.core.xconst.XDirection;

/**
 * Attempts to explore the map.
 * 
 * There is no synchronous version of this, because it's probably pointless to explore forever.
 */
public class ExplorationBlock {
  /**
   * Returns after attempting to make progress.
   * @return true if a movement action happened, false otherwise
   */
  public static final boolean async() {
    if (_targetLocation == null || !M.unknownOnMap(_targetLocation) || PathfindingBlock.done) {
      _targetLocation = null;
      if (!_findTarget()) {
        return false;
      }
      D.debug_setIndicator(1, "Exploring from " + S.location + " to " + _targetLocation);
    }
    return PathfindingBlock.async();    
  }
  
  /** Finds an unknown location on the map to explore towards. */
  public static final boolean _findTarget() {
    if (_targetDirection == null) {
      _targetDirection = M.nearestEdgeDirection().opposite();
    }
    for (int distance = S.sensorType.range * 2; distance <= GameConstants.MAP_MAX_WIDTH;
         distance += S.sensorType.range) {
      for (int i = XDirection.ADJACENT_DIRECTIONS; i >= 0; i--) {
        _targetLocation = S.location.add(_targetDirection, distance);
        if (M.unknownOnMap(_targetLocation)) {
          PathfindingBlock.setParameters(_targetLocation, 150);
          D.debug_pv("_findTarget: " + _targetLocation);
          return true;
        }
        _targetDirection = _targetDirection.rotateRight();
      }
    }
    return false;
  }
  
  /** Exploration target. */
  public static MapLocation _targetLocation;
  /** The general direction that we're exploring towards. */
  public static Direction _targetDirection;  
}
