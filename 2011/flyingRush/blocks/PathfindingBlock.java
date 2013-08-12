package flyingRush.blocks;

import flyingRush.blocks.pathfinding.BFHPathfinding;
import flyingRush.blocks.pathfinding.VBugPathfindingBlock;
import flyingRush.core.D;
import flyingRush.core.S;
import flyingRush.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

/** The main path-finding code. */
public final class PathfindingBlock {  
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
    _chargeForward = false;
    _precision = DEFAULT_PRECISION;
    // TODO: figure out the right margin shaves
    _idleBytecodeMargin = bytecodeMargin + 80;
    _activeBytecodeMargin = bytecodeMargin + 320;
    BFHPathfinding.setNavigationTarget(_target, _precision);
    VBugPathfindingBlock.setNavigationTarget(_target);
  }
    
  /** Attempts to take one action towards reaching the goal. */
  public static final boolean async() {
    if (done) { return false; }
    
    boolean idle = S.movementController.isActive();
    int bytecodeMargin = idle ? _idleBytecodeMargin :_activeBytecodeMargin;
    if (!BFHPathfinding._doneComputing) {
      BFHPathfinding.compute(bytecodeMargin);
      // if (BFHPathfinding._doneComputing) {
      //   BFHPathfinding.debug_printExpanded();
      // }
    }
    if (idle) { return false; }
    
    if (BFHPathfinding._doneComputing) {
      if (!BFHPathfinding._foundPath) {
        _precision = (_precision > 1) ? _precision - 1 : _precision;
      }
      else {
        Direction nextDirection = BFHPathfinding.nextDirection(S.direction);
        if (nextDirection != null) {
          D.debug_pv("BFH");
          _chargeForward = true;
          return _moveTowardsAsync(nextDirection);
        }        
        _precision = DEFAULT_PRECISION;
      }
      D.debug_pv("BFH reset - foundPath:" + BFHPathfinding._foundPath);
      // if (S.round >= 500) { BFHPathfinding.debug_printExpanded(); }
      // if (S.round >= 600) { S.rc.suicide(); }
      
      // Couldn't move because unknown square turned into a wall. Recompute.
      BFHPathfinding.setNavigationTarget(_target, _precision);
      BFHPathfinding.compute(bytecodeMargin);
    }
    
    if (_chargeForward) {
      if (S.movementController.canMove(S.direction)) {
        D.debug_pv("Charge");
        return _moveTowardsAsync(S.direction);
      }
      _chargeForward = false;
      VBugPathfindingBlock.reset();
    }
    
    D.debug_pv("Bug");
    Direction nextDirection = VBugPathfindingBlock.nextDirection();
    return _moveTowardsAsync(nextDirection);
  }
  
  /** Default precision for BFH search. */
  public static final int DEFAULT_PRECISION = 5;
  
  /** Makes one step or turn to end up in an adjacent square. */
  public static final boolean _moveTowardsAsync(Direction direction) {
    try {
      if (S.direction == direction) {
        X.moveForward();
        if (S.location.add(direction).equals(_target)) {
          done = true;
        }
      // TODO(pwnall): enable moveBackward after sensing works
      // } else if (S.direction.opposite() == direction) {
      //  X.moveBackward();
      //  if (S.location.add(direction).equals(_target)) {
      //    done = true;
      //  }
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
  public static boolean _chargeForward = false;
}
