package sprint4.chaos;

import sprint4.core.D;
import sprint4.core.M;
import sprint4.core.S;
import sprint4.core.X;
import sprint4.core.xconst.XDirection;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;

/** Bug-like navigation towards some general direction. */
public class SurveyNavigationBlock {
  /** Configures the navigation algorithm. */  
  public static void setNavigationParameters(Direction direction, int maxDistance2,
                                             int maxRoundsInSameDirection) {
    int targetX = S.locationX + direction.dx * GameConstants.MAP_MAX_WIDTH;
    if (targetX <= M.mapMinX) {
      targetX = M.mapMinX + 1;
    } else if (targetX >= M.mapMaxX) {
      targetX = M.mapMaxX - 1;
    }
    int targetY = S.locationY + direction.dy * GameConstants.MAP_MAX_HEIGHT;
    if (targetY <= M.mapMinY) {
      targetY = M.mapMinY + 1;
    } else if (targetY >= M.mapMaxY) {
      targetY = M.mapMaxY - 1;
    }
    
    if (SurveyNavigationBlock._targetDirection != direction) {
      SurveyNavigationBlock._lastDirectionChangeRound = S.round;      
    }
    SurveyNavigationBlock._targetDirection = direction;
    SurveyNavigationBlock._maxDistance2 = maxDistance2;
    SurveyNavigationBlock.maxRoundsInSameDirection = maxRoundsInSameDirection;
    SurveyNavigationBlock._targetX = targetX;
    SurveyNavigationBlock._targetY = targetY;
    SurveyNavigationBlock._targetLocation = new MapLocation(targetX, targetY);
    SurveyNavigationBlock._lastMoveDirection = _targetDirection;
    SurveyNavigationBlock.lastTurnDirection = null;
  }
  
  /**
   * Navigates towards the target destination, if possible.
   * 
   * @return true if an action was taken, false otherwise
   */
  public static final boolean tryNavigating() {
    // Adjust location for boundaries.
    if (_targetX <= M.mapMinX) {
      _targetX = M.mapMinX + 1;
      _targetLocation = new MapLocation(_targetX, _targetY);
    } else if (_targetX >= M.mapMaxX) {
      _targetX = M.mapMaxX - 1;
      _targetLocation = new MapLocation(_targetX, _targetY);
    }
    if (_targetY <= M.mapMinY) {
      _targetY = M.mapMinY + 1;
      _targetLocation = new MapLocation(_targetX, _targetY);
    } else if (_targetY >= M.mapMaxY) {
      _targetY = M.mapMaxY - 1;
      _targetLocation = new MapLocation(_targetX, _targetY);
    }

    // Are we there yet?
    if (_targetLocation.distanceSquaredTo(S.location) <= _maxDistance2
        || S.round - _lastDirectionChangeRound >= maxRoundsInSameDirection) {
      setNavigationParameters(_targetDirection.rotateRight().rotateRight(), _maxDistance2,
                              maxRoundsInSameDirection);
      return false;
    }

    if (lastTurnDirection == null) {
      Direction targetDirection = S.location.directionTo(_targetLocation);
      Direction direction = null;
      for (int i = 0; i < 2; i++) {
        if (_lastMoveDirection == targetDirection) {
          direction = targetDirection;
          break;
        }
        direction = _lastMoveDirection.rotateLeft();
      }
      for (int i = 0; i < XDirection.ADJACENT_DIRECTIONS; i++) {
        if (M.landAtDirection(direction.ordinal()) && S.movementController.canMove(direction)) {
          lastTurnDirection = direction;          
          break;
        }
        if (!M.knownAtDirection(direction.ordinal())) {
          // Don't know what's here, so let's turn this way to see.
          try {
            X.setDirection(direction);
            return true;
          }
          catch (GameActionException e) {
            return false;
          }
        }
        direction = direction.rotateRight();
      }
      if (lastTurnDirection == null) {
        return false;
      }
    }
    
    if (S.movementController.isActive())
      return false;
    
    try {
      if (lastTurnDirection == S.direction) {
        if (!S.movementController.canMove(lastTurnDirection)) {
          lastTurnDirection = null;  // Some other robot is there.
          return false;
        }

        X.moveForward();
        _lastMoveDirection = lastTurnDirection;
        lastTurnDirection = null;
        return true;
      }
      if (lastTurnDirection.opposite() == S.direction) {
        if (!S.movementController.canMove(lastTurnDirection)) {
          lastTurnDirection = null;  // Some other robot is there.
          return false;
        }

        X.moveBackward();
        _lastMoveDirection = lastTurnDirection;
        lastTurnDirection = null;
        return true;
      }
      X.setDirection(lastTurnDirection);
      return true;
    }
    catch(GameActionException e) {
      D.debug_logException(e);  // Moving or turning failed. Make sure we fail tests.
      return false;
    }
  }
  
  
  /** How many rounds until we move randomly to get out of a corner. */
  public static int maxRoundsInSameDirection;
  
  /** Direction in which we're navigating. */
  public static Direction _targetDirection;
  /** The square of the closest distance we want to get to the map edge. */
  public static int _maxDistance2;
  /** The round when we last changed our direction. */
  public static int _lastDirectionChangeRound;
  /** MapLocation where we're trying to navigate. */
  public static MapLocation _targetLocation;
  /** Coordinates of the MapLocation where we're trying to navigate. */
  public static int _targetX, _targetY;
  /** Set to true after a turn. The robot should try to move that way. */
  public static Direction _lastMoveDirection;
  /** Set to true after a turn. The robot should try to move that way. */
  public static Direction lastTurnDirection;
}
