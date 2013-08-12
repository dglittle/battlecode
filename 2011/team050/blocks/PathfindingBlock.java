package team050.blocks;

import team050.blocks.pathfinding.BFHPathfinding;
import team050.blocks.pathfinding.PathfindingBase;
import team050.blocks.pathfinding.VBugPathfindingBlock;
import team050.core.D;
import team050.core.M;
import team050.core.S;
import team050.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

/** The main path-finding code. */
public final class PathfindingBlock {
  /** Set this to disable bytecode-intensive pathfinding. */
  public static boolean disableSmartfinding = false;
  
  public static final void setParameters(MapLocation target, int bytecodeMargin) {
    if (target.equals(S.location)) {
      done = true;
      return;
    }
    if (target.equals(_target)) {
      return;
    }
    done = false;
    _target = target;
    _chargeForward = 0;
    _precision = 1;
    // TODO: figure out the right margin shaves
    _idleBytecodeMargin = bytecodeMargin + 300;
    _activeBytecodeMargin = bytecodeMargin + 1000;    
    PathfindingBase.reset();
    BFHPathfinding.setNavigationTarget(_target, _precision);
    VBugPathfindingBlock.setNavigationTarget(_target);    
  }
    
  /** Attempts to take one action towards reaching the goal. */
  public static final boolean async() {
    if (done) { return false; }
    M._updateMapPassableWithNearbyRobots();
    
    boolean idle = !S.motorReady;
    int bytecodeMargin = idle ? _idleBytecodeMargin :_activeBytecodeMargin;
    if (S.sensorController != null && !disableSmartfinding) {
      BFHPathfinding.compute(bytecodeMargin);
      // D.debug_pv("BFH computing done at " + Clock.getBytecodeNum());
    }

    if (S.sensorController != null && !disableSmartfinding) {
      if (PathfindingBase.completedIteration &&
          !PathfindingBase.foundPathLastIteration) {
        // Didn't find a path, let's try to improve the precision.
        _precision++;
        BFHPathfinding.setNavigationTarget(_target, _precision);
        // BFHPathfinding.debug_printExpanded();
        // D.debug_pv("BFH failed - new precision: " + _precision);
      }
    }
    
    if (idle) { return false; }
    
    if (S.sensorController != null && !disableSmartfinding) {
      if (PathfindingBase.solution != null) {
        Direction nextDirection = PathfindingBase.nextDirection(S.direction);
        // D.debug_pv("nextDirection done at " + Clock.getBytecodeNum());
        if (nextDirection != null) {
          if (PathfindingBase.foundPathLastIteration) {
            // Decrease precision for faster updates, since we're getting
            // closer to target.
            int newPrecision = _precision - 1;
            if (newPrecision > 0) {
              _precision = newPrecision;
              BFHPathfinding.setNavigationTarget(_target, _precision);
            }
            PathfindingBase.foundPathLastIteration = false;
          }
          // D.debug_pv("BFH taken - precision: " + _precision);
          _chargeForward = CHARGE_FORWARD;
          return _moveTowardsAsync(nextDirection);
        }
        else {
          // The solution needs updating.
          // D.debug_pv("BFH outdated - precision: " + _precision);
          // PathfindingBase.debug_printSolution();
          PathfindingBase.solution = null;
          if (S.hasTurnedThisRound) { return true; }
        }
      }
      
      if (_chargeForward > 0) {
        VBugPathfindingBlock.setNavigationTarget(_target);
        if (S.movementController.canMove(S.direction)) {
          // D.debug_pv("Charge");
          _chargeForward--;
          return _moveTowardsAsync(S.direction);
        }
      }
    }
    
    // D.debug_pv("Bug");
    Direction nextDirection = VBugPathfindingBlock.nextDirection();
    if (nextDirection != null) {
      return _moveTowardsAsync(nextDirection);      
    } else {
      return false;
    }
  }
  
  /** Number of squares to charge forward before bugging. */
  public static final int CHARGE_FORWARD = 5;
  
  /** Makes one step or turn to end up in an adjacent square. */
  public static final boolean _moveTowardsAsync(Direction direction) {
    try {
      if (S.direction == direction) {
        X.moveForward();
        if (S.location.add(direction).equals(_target)) {
          done = true;
        }
      } else if (S.direction.opposite() == direction) {
        X.moveBackward();
        if (S.location.add(direction).equals(_target)) {
          done = true;
        }
      } else {
        X.setDirection(direction);
      }
      return true;
    }
    catch (GameActionException e) {
      D.debug_logException(e);  // Routing sent us straight into a wall.
      return false;
    }
  }
  
  /** True when we're done navigating. */
  public static boolean done = true;
  /** Where we're going. */
  public static MapLocation _target;
  /** Bytecode margin for optimal computation if we have to make a move. */
  public static int _activeBytecodeMargin;
  /** Bytecode margin for optimal computation if we don't have to make a move. */
  public static int _idleBytecodeMargin;
  /** Precision used for last BFH search. */
  public static int _precision;
  /** Set when path-finding needs to surrender to bug. */
  public static int _chargeForward;
}