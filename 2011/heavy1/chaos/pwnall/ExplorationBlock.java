package heavy1.chaos.pwnall;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import heavy1.blocks.PathfindingBlock;
import heavy1.core.D;
import heavy1.core.M;
import heavy1.core.S;
import heavy1.core.X;
import heavy1.core.xconst.XDirection;

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
    if (S.sensorController == null) { return false; }
    
    if (_targetLocation == null || !M.unknownOnMap(_targetLocation) ||
        PathfindingBlock.done) {
      _targetLocation = null;
      if (!_findTarget()) {
        return false;
      }
      D.debug_setIndicator(2, "Exploring from " + S.location + " to " + _targetLocation);
    }
    PathfindingBlock.setParameters(_targetLocation, 25);
    if (S.rand.nextBoolean()) {
      if (_lookAroundAsync()) { return true; }      
    }
    return PathfindingBlock.async();    
  }
  
  /** Scans the map around the current location. */
  public static final boolean _lookAroundAsync() {
    if (S.movementController.isActive() || S.sensorIsOmnidirectional) {
      return false;
    }
    for (Direction direction = S.direction.rotateRight().rotateRight();
         direction != S.direction;
         direction = direction.rotateRight().rotateRight()) {
      if (!M.scannedDirection[S.locationX - M.arrayBaseX][S.locationY - M.arrayBaseY][direction.ordinal()]) {
        try {
          X.setDirection(direction);
          return true;            
        } catch(GameActionException e) {
          D.debug_logException(e);
          return false;
        }
      }
    }
    return false;
  }
  
  /** Finds an unknown location on the map to explore towards. */
  public static final boolean _findTarget() {
    if (_targetDirection == null) {
      _targetDirection = M.nearestEdgeDirection().opposite();
    }
    for (int distance = S.sensorType.range * 2;
         distance <= GameConstants.MAP_MAX_WIDTH;
         distance += S.sensorType.range) {
      for (int i = XDirection.ADJACENT_DIRECTIONS; i >= 0; i--) {
        _targetLocation = S.location.add(_targetDirection, distance);
        if (M.unknownOnMap(_targetLocation)) {
          PathfindingBlock.setParameters(_targetLocation, 25);
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
