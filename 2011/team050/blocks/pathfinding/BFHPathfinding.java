package team050.blocks.pathfinding;

import team050.core.M;
import team050.core.S;
import battlecode.common.Clock;
import battlecode.common.MapLocation;

// This file has auto-generated pieces. Regenerate with:
//    bcpm regen src/team050/blocks/pathfinding/BFHPathfinding.java

/**
 * BFS + heuristic bounding navigation.
 * 
 * This class is closely coupled with @link {@link M}.
 * 
 * Implementation notes:
 * 
 * The implementation is a cross between BFS and Dijkstra.
 * 1. Approximate sqrt(2) with 1.5. This holds perfectly as long as there are less than 10 diagonals
 * in the route, and it'll do reasonably well after that.
 * 
 * 2. Work with doubled distances. This way, neighbors are 2 or 3 squares away.
 * 
 * 3. Now the priority queue is four buckets: current, next, straights, diagonals. Pre-allocate
 * arrays for each of them, as well as counts. When done with current, assign straights <-
 * diagonals, next <- straights, current <- next, diagonals <- {} (count=0).
 * 
 * 4. Use an A*-style heuristic to limit the scope of the BFS. Do not explores squares where
 * d + h >=  magic_constant * h(source). A 6-7-square wide tunnel between source and destination
 * should be enough, maps don't seem to have horribly long walls.
 * 
 * 5. No 2-way BFS. Motivation: one-way BFS generates a predecessor map that can be used to get to
 * destination even if the robot falls off the golden path.
 * 
 * 6. Fall back to full BFS if the heuristic makes you miss a path. This means a long long
 * wall somewhere, so bug tracing would be lost really badly. Perhaps start going blindly towards
 * the destination to buy time.
 */
public final class BFHPathfinding {
  /**
   * Resets the navigation algorithm to a new target.
   * @param target the location where we want to get to.
   * @param precision trade-off between computation speed and path optimality (when in doubt use 1)
   */
  public static final void setNavigationTarget(MapLocation target, int precision) {
    _targetMX = target.x - M.arrayBaseX;
    _targetMY = target.y - M.arrayBaseY;
    
    _precision = precision;
    _sourceX = _sourceY = -1;
  
    PathfindingBase.completedIteration = true;
  }

  /**
   * Performs a chunk of path-finding computation.
   * 
   * @param bytecodeMargin give up and return if there are only this many
   *                       bytecodes left in the round; this is an approximate
   *                       number, the precision is ~100 bytecodes
   */
  public static final void compute(int bytecodeMargin) {    
    if (S.locationX != _sourceX || S.locationY != _sourceY) {
      _sourceX = S.locationX;
      _sourceMX = S.locationX - M.arrayBaseX;
      _sourceY = S.locationY;
      _sourceMY = S.locationY - M.arrayBaseY;
      _hTarget = _h(_targetMX, _targetMY);
      final int hMultiplier = (_precision + 1) >> 1;
      final int hFraction = 1 + 2 * (_precision & 1);
      _hTarget = PathfindingBase.COST_FLOOR + hMultiplier * _hTarget + (hFraction * _hTarget + 4) / 5;
    }

    if (PathfindingBase.completedIteration) {
      PathfindingBase.completedIteration = false;
      
      // Initialize map.
      _next_minCost = new int[M.arrayWidth][M.arrayHeight];
      _expanded = new boolean[M.arrayWidth][M.arrayHeight];
      _next_minCost[_targetMX][_targetMY] = PathfindingBase.COST_FLOOR;
      
      // Initialize queues.
      _currentMX[0] = _targetMX; _currentMY[0] = _targetMY;
      _currentHead = 1;
      _nextHead = _orthogonalHead = _diagonalHead = _unknownHead = 0;
      _orthogonalCost = PathfindingBase.COST_FLOOR + 2;
      _diagonalCost = PathfindingBase.COST_FLOOR + 3;
      _unknownCost = PathfindingBase.COST_FLOOR + 4;
    }

    // Memoized array access.
    final int[] _next_minCost_sourceMX = _next_minCost[_sourceMX];
        
    while (Clock.getBytecodesLeft() > bytecodeMargin) {
      _currentHead--;

      //
      // Expand the top of the queue.
      //
      
      final int _mx_ = _currentMX[_currentHead];
      final int _my_ = _currentMY[_currentHead];
      
      final boolean[] _expanded_mx_ = _expanded[_mx_];
      if (_expanded_mx_[_my_]) {
        // D.debug_pv("BFH: Expanded");
        ;
      }
      else {
        _expanded_mx_[_my_] = true;        

        // mx1 = mx + 1, mxm1 = mx - 1, etc.
        final int _mx1_ = _mx_ + 1;
        final int _my1_ = _my_ + 1;
        final int _mxm1_ = _mx_ - 1;
        final int _mym1_ = _my_ - 1;
        
        // Memoized array accesses.
        final int[] _next_minCost_mx_ = _next_minCost[_mx_];
        final int[] _next_minCost_mx1_ = _next_minCost[_mx1_];
        final int[] _next_minCost_mxm1_ = _next_minCost[_mxm1_];
  
        // d_mx_ = abs(mx - _sourceMX), etc.
        final int _d_mx_, _d_mx1_, _d_mxm1_, _d_my_, _d_my1_, _d_mym1_;
        final int rdx = _mx_ - _sourceMX;
        if (rdx > 0) {
          _d_mx_ = rdx;
          _d_mx1_ = _d_mx_ + 1;
          _d_mxm1_ = _d_mx_ - 1;
        }
        else if (rdx < 0) {
          _d_mx_ = -rdx;
          _d_mx1_ = _d_mx_ - 1;
          _d_mxm1_ = _d_mx_ + 1;
        }
        else {
          _d_mx_ = 0;
          _d_mx1_ = _d_mxm1_ = 1;
        }
        int rdy = _my_ - _sourceMY;
        if (rdy > 0) {
          _d_my_ = rdy;
          _d_my1_ = _d_my_ + 1;
          _d_mym1_ = _d_my_ - 1;
        }
        else if (rdy < 0) {
          _d_my_ = -rdy;
          _d_my1_ = _d_my_ - 1;
          _d_mym1_ = _d_my_ + 1;
        }
        else {
          _d_my_ = 0;
          _d_my1_ = _d_mym1_ = 1;
        }
        
        //$ +gen:source BFH.Orthogonal NORTH _mx_ _mym1_
        if (_next_minCost_mx_[_mym1_] > _orthogonalCost) {
          if (M.known[_mx_][_mym1_]) {
            if (M.passable[_mx_][_mym1_]) {
              _next_minCost_mx_[_mym1_] = _orthogonalCost;
              
              final int hDiagonal = (_d_mx_ < _d_mym1_) ? _d_mx_ : _d_mym1_;
              final int hOrthogonal = _d_mx_ + _d_mym1_;
              final int h = 2 * hOrthogonal - hDiagonal;
              if (_orthogonalCost + h < _hTarget) {
                _orthogonalMX[_orthogonalHead] = _mx_;
                _orthogonalMY[_orthogonalHead] = _mym1_;
                _orthogonalHead++;
              }
            }
            else {
              _next_minCost_mx_[_mym1_] = PathfindingBase.COST_OBSTACLE;
            }
          }
          else if (_next_minCost_mx_[_mym1_] > _unknownCost) {
            _next_minCost_mx_[_mym1_] = _unknownCost;
            final int hDiagonal = (_d_mx_ < _d_mym1_) ? _d_mx_ : _d_mym1_;
            final int hOrthogonal = _d_mx_ + _d_mym1_;
            final int h = 2 * hOrthogonal - hDiagonal;
            if (_unknownCost + h < _hTarget) {
              _unknownMX[_unknownHead] = _mx_;
              _unknownMY[_unknownHead] = _mym1_;
              _unknownHead++;
            }
          }
        }
        //$ -gen:source
        
        //$ +gen:target BFH.Orthogonal SOUTH _mx_ _my1_
        if (_next_minCost_mx_[_my1_] > _orthogonalCost) {
          if (M.known[_mx_][_my1_]) {
            if (M.passable[_mx_][_my1_]) {
              _next_minCost_mx_[_my1_] = _orthogonalCost;
              
              final int hDiagonal = (_d_mx_ < _d_my1_) ? _d_mx_ : _d_my1_;
              final int hOrthogonal = _d_mx_ + _d_my1_;
              final int h = 2 * hOrthogonal - hDiagonal;
              if (_orthogonalCost + h < _hTarget) {
                _orthogonalMX[_orthogonalHead] = _mx_;
                _orthogonalMY[_orthogonalHead] = _my1_;
                _orthogonalHead++;
              }
            }
            else {
              _next_minCost_mx_[_my1_] = PathfindingBase.COST_OBSTACLE;
            }
          }
          else if (_next_minCost_mx_[_my1_] > _unknownCost) {
            _next_minCost_mx_[_my1_] = _unknownCost;
            final int hDiagonal = (_d_mx_ < _d_my1_) ? _d_mx_ : _d_my1_;
            final int hOrthogonal = _d_mx_ + _d_my1_;
            final int h = 2 * hOrthogonal - hDiagonal;
            if (_unknownCost + h < _hTarget) {
              _unknownMX[_unknownHead] = _mx_;
              _unknownMY[_unknownHead] = _my1_;
              _unknownHead++;
            }
          }
        }
        //$ -gen:target
        
        //$ +gen:target BFH.Orthogonal WEST _mxm1_ _my_
        if (_next_minCost_mxm1_[_my_] > _orthogonalCost) {
          if (M.known[_mxm1_][_my_]) {
            if (M.passable[_mxm1_][_my_]) {
              _next_minCost_mxm1_[_my_] = _orthogonalCost;
              
              final int hDiagonal = (_d_mxm1_ < _d_my_) ? _d_mxm1_ : _d_my_;
              final int hOrthogonal = _d_mxm1_ + _d_my_;
              final int h = 2 * hOrthogonal - hDiagonal;
              if (_orthogonalCost + h < _hTarget) {
                _orthogonalMX[_orthogonalHead] = _mxm1_;
                _orthogonalMY[_orthogonalHead] = _my_;
                _orthogonalHead++;
              }
            }
            else {
              _next_minCost_mxm1_[_my_] = PathfindingBase.COST_OBSTACLE;
            }
          }
          else if (_next_minCost_mxm1_[_my_] > _unknownCost) {
            _next_minCost_mxm1_[_my_] = _unknownCost;
            final int hDiagonal = (_d_mxm1_ < _d_my_) ? _d_mxm1_ : _d_my_;
            final int hOrthogonal = _d_mxm1_ + _d_my_;
            final int h = 2 * hOrthogonal - hDiagonal;
            if (_unknownCost + h < _hTarget) {
              _unknownMX[_unknownHead] = _mxm1_;
              _unknownMY[_unknownHead] = _my_;
              _unknownHead++;
            }
          }
        }
        //$ -gen:target
        
        //$ +gen:target BFH.Orthogonal EAST _mx1_ _my_
        if (_next_minCost_mx1_[_my_] > _orthogonalCost) {
          if (M.known[_mx1_][_my_]) {
            if (M.passable[_mx1_][_my_]) {
              _next_minCost_mx1_[_my_] = _orthogonalCost;
              
              final int hDiagonal = (_d_mx1_ < _d_my_) ? _d_mx1_ : _d_my_;
              final int hOrthogonal = _d_mx1_ + _d_my_;
              final int h = 2 * hOrthogonal - hDiagonal;
              if (_orthogonalCost + h < _hTarget) {
                _orthogonalMX[_orthogonalHead] = _mx1_;
                _orthogonalMY[_orthogonalHead] = _my_;
                _orthogonalHead++;
              }
            }
            else {
              _next_minCost_mx1_[_my_] = PathfindingBase.COST_OBSTACLE;
            }
          }
          else if (_next_minCost_mx1_[_my_] > _unknownCost) {
            _next_minCost_mx1_[_my_] = _unknownCost;
            final int hDiagonal = (_d_mx1_ < _d_my_) ? _d_mx1_ : _d_my_;
            final int hOrthogonal = _d_mx1_ + _d_my_;
            final int h = 2 * hOrthogonal - hDiagonal;
            if (_unknownCost + h < _hTarget) {
              _unknownMX[_unknownHead] = _mx1_;
              _unknownMY[_unknownHead] = _my_;
              _unknownHead++;
            }
          }
        }
        //$ -gen:target
        
        //$ +gen:source BFH.Diagonal NORTH_WEST _mxm1_ _mym1_
        if (_next_minCost_mxm1_[_mym1_] > _diagonalCost) {
          if (M.known[_mxm1_][_mym1_]) {
            if (M.passable[_mxm1_][_mym1_]) {
              _next_minCost_mxm1_[_mym1_] = _diagonalCost;
              final int hDiagonal = (_d_mxm1_ < _d_mym1_) ? _d_mxm1_ : _d_mym1_;
              final int hOrthogonal = _d_mxm1_ + _d_mym1_;
              final int h = 2 * hOrthogonal - hDiagonal;
              if (_diagonalCost + h < _hTarget) {
                _diagonalMX[_diagonalHead] = _mxm1_;
                _diagonalMY[_diagonalHead] = _mym1_;
                _diagonalHead++;
              }
            }
            else {
              _next_minCost_mxm1_[_mxm1_] = PathfindingBase.COST_OBSTACLE;
            }
          }
        }
        //$ -gen:source
        
        //$ +gen:target BFH.Diagonal NORTH_EAST _mx1_ _mym1_
        if (_next_minCost_mx1_[_mym1_] > _diagonalCost) {
          if (M.known[_mx1_][_mym1_]) {
            if (M.passable[_mx1_][_mym1_]) {
              _next_minCost_mx1_[_mym1_] = _diagonalCost;
              final int hDiagonal = (_d_mx1_ < _d_mym1_) ? _d_mx1_ : _d_mym1_;
              final int hOrthogonal = _d_mx1_ + _d_mym1_;
              final int h = 2 * hOrthogonal - hDiagonal;
              if (_diagonalCost + h < _hTarget) {
                _diagonalMX[_diagonalHead] = _mx1_;
                _diagonalMY[_diagonalHead] = _mym1_;
                _diagonalHead++;
              }
            }
            else {
              _next_minCost_mx1_[_mx1_] = PathfindingBase.COST_OBSTACLE;
            }
          }
        }
        //$ -gen:target
        
        //$ +gen:target BFH.Diagonal SOUTH_WEST _mxm1_ _my1_
        if (_next_minCost_mxm1_[_my1_] > _diagonalCost) {
          if (M.known[_mxm1_][_my1_]) {
            if (M.passable[_mxm1_][_my1_]) {
              _next_minCost_mxm1_[_my1_] = _diagonalCost;
              final int hDiagonal = (_d_mxm1_ < _d_my1_) ? _d_mxm1_ : _d_my1_;
              final int hOrthogonal = _d_mxm1_ + _d_my1_;
              final int h = 2 * hOrthogonal - hDiagonal;
              if (_diagonalCost + h < _hTarget) {
                _diagonalMX[_diagonalHead] = _mxm1_;
                _diagonalMY[_diagonalHead] = _my1_;
                _diagonalHead++;
              }
            }
            else {
              _next_minCost_mxm1_[_mxm1_] = PathfindingBase.COST_OBSTACLE;
            }
          }
        }
        //$ -gen:target
        
        //$ +gen:target BFH.Diagonal SOUTH_EAST _mx1_ _my1_
        if (_next_minCost_mx1_[_my1_] > _diagonalCost) {
          if (M.known[_mx1_][_my1_]) {
            if (M.passable[_mx1_][_my1_]) {
              _next_minCost_mx1_[_my1_] = _diagonalCost;
              final int hDiagonal = (_d_mx1_ < _d_my1_) ? _d_mx1_ : _d_my1_;
              final int hOrthogonal = _d_mx1_ + _d_my1_;
              final int h = 2 * hOrthogonal - hDiagonal;
              if (_diagonalCost + h < _hTarget) {
                _diagonalMX[_diagonalHead] = _mx1_;
                _diagonalMY[_diagonalHead] = _my1_;
                _diagonalHead++;
              }
            }
            else {
              _next_minCost_mx1_[_mx1_] = PathfindingBase.COST_OBSTACLE;
            }
          }
        }
        //$ -gen:target
      }
      
      //
      // Pop the next element out of the queue.
      //

      while (_currentHead == 0) {
        final int[] tempX = _currentMX;
        final int[] tempY = _currentMY;
        
        if (_nextHead + _orthogonalHead + _diagonalHead + _unknownHead == 0) {
          if (_next_minCost_sourceMX[_sourceMY] != 0) {
            //$ +gen:source BFH.Done __
            PathfindingBase.solution = _next_minCost;
            PathfindingBase.foundPathLastIteration = true;
            PathfindingBase.completedIteration = true;
            // debug_printExpanded();
            //$ -gen:source BFH.Done
          }
          else {
            PathfindingBase.foundPathLastIteration = false;
            PathfindingBase.completedIteration = true;
          }
          return;
        }

        //$ +gen:source BFH.QueueSwap current next
        _currentMX = _nextMX;
        _currentMY = _nextMY;
        _currentHead = _nextHead;
        //$ -gen:source
        
        //$ +gen:target BFH.QueueSwap next orthogonal
        _nextMX = _orthogonalMX;
        _nextMY = _orthogonalMY;
        _nextHead = _orthogonalHead;
        //$ -gen:target
            
        //$ +gen:target BFH.QueueSwap orthogonal diagonal
        _orthogonalMX = _diagonalMX;
        _orthogonalMY = _diagonalMY;
        _orthogonalHead = _diagonalHead;
        //$ -gen:target
        _orthogonalCost += 1;
    
        //$ +gen:target BFH.QueueSwap diagonal unknown
        _diagonalMX = _unknownMX;
        _diagonalMY = _unknownMY;
        _diagonalHead = _unknownHead;
        //$ -gen:target
        _diagonalCost += 1;
        
        _unknownMX = tempX;
        _unknownMY = tempY;
        _unknownHead = 0;
        _unknownCost += 1;
      }
    }
    
    if (_next_minCost_sourceMX[_sourceMY] != 0) {
      //$ +gen:target BFH.Done __
      PathfindingBase.solution = _next_minCost;
      PathfindingBase.foundPathLastIteration = true;
      PathfindingBase.completedIteration = true;
      // debug_printExpanded();
      //$ -gen:target BFH.Done
    }
  }
  
  /**
   * Computes the heuristic function for a map location.
   * @param targetMX the location's X coordinate, in map space
   * @param targetMY the location's Y coordinate, in map space
   * @return the heuristic function.
   */
  public static final int _h(int mx, int my) {
    final int dx = mx - _sourceMX;
    final int dy = my - _sourceMY;
    final int gdx = Math.abs(dx);
    final int gdy = Math.abs(dy);
    
    if (gdx > gdy) {
      //$ +gen:source BFH.LineDraw x y X Y
      int hf = 2 * gdx + gdy;
      final int xinc = (mx > _sourceMX) ? 1 : -1;
      double fy = _sourceMY;
      final double m = (double)dy / dx;
      for (int x = _sourceMX; ; x += xinc, fy += m) {
        final int y = (int)(fy + 0.5);
        //$ +gen:off
        if (M.known[x][y]) {
          if (!M.passable[x][y]) {
            hf += 3;
          }
        } else {
          hf += 2;
        }
        //$ -gen:off
        if (x == mx) { break; }
      }
      //$ +gen:off
      // D.debug_pv("h: " + hf + " delta: " + dx + ", " + dy);
      //$ -gen:off
      return hf;
      //$ -gen:source
    }
    else {
      //$ +gen:target BFH.LineDraw y x Y X
      int hf = 2 * gdy + gdx;
      final int yinc = (my > _sourceMY) ? 1 : -1;
      double fx = _sourceMX;
      final double m = (double)dx / dy;
      for (int y = _sourceMY; ; y += yinc, fx += m) {
        final int x = (int)(fx + 0.5);
        //$ +gen:off
        if (M.known[x][y]) {
          if (!M.passable[x][y]) {
            hf += 3;
          }
        } else {
          hf += 2;
        }
        //$ -gen:off
        if (y == my) { break; }
      }
      //$ +gen:off
      // D.debug_pv("h: " + hf + " delta: " + dx + ", " + dy);
      //$ -gen:off
      return hf;
      //$ -gen:target
    }        
  }
  
  /**
   * Prints what has been expanded to the console.
   */
  public static final void debug_printExpanded() {
    for (int y = M.mapMinY - 1; y <= M.mapMaxY + 1; y++) {
      StringBuffer lineBuffer = new StringBuffer();
      int my = y - M.arrayBaseY;
      for (int x = M.mapMinX - 1; x <= M.mapMaxX + 1; x++) {
        int mx = x - M.arrayBaseX;
        if (mx == _targetMX && my == _targetMY) {
          lineBuffer.append('<');
          continue;
        }
        if (mx == _sourceMX && my == _sourceMY) {
          lineBuffer.append('>');
          continue;
        }        
          
        if (!M.known[mx][my]) {
          if (_expanded[mx][my]) {
            lineBuffer.append('?');
          } else {
            lineBuffer.append(' ');
          }
        } else if(!M.land[mx][my]) {
          if (_expanded[mx][my]) {
            lineBuffer.append('#');
          } else {            
            lineBuffer.append('X');
          }
        } else if (!M.passable[mx][my]) {
          if (_expanded[mx][my]) {
            lineBuffer.append('&');
          } else {
            lineBuffer.append('@');
          }          
        } else {
          if (_expanded[mx][my]) {
            // lineBuffer.append((char)('A' + (_minCost[mx][my] - COST_FLOOR)));
            lineBuffer.append('+');
          } else {
            lineBuffer.append('.');
          }
        }
      }
      System.out.println(lineBuffer.toString());
    }
  }
  
  /** The source X, Y, in real coordinates. */
  public static int _sourceX, _sourceY;
  /** The source X, Y, in map array coordinates. */
  public static int _sourceMX, _sourceMY;

  /** The target X, Y, in map array coordinates. */
  public static int _targetMX, _targetMY;
  /** The desired path-finding precision / speed tradeoff. */
  public static int _precision;
  
  /** The value of h for the target square. */
  public static int _hTarget;
  
  /**
   * Minimum cost to get to a location multiplied by 2, plus -COST_FLOOR.
   * 
   * This is the in-progress version of the minCost map.
   */
  public static int[][] _next_minCost;
  /** True for locations that were extracted from the queue. */
  public static boolean[][] _expanded;
  
  /** Queue segment that we're extracting squares from. X map coordinates. */
  public static int[] _currentMX = new int[M.arrayWidth * M.arrayHeight];
  /** Queue segment that we're extracting squares from. Y map coordinates. */
  public static int[] _currentMY = new int[M.arrayWidth * M.arrayHeight];
  /** Queue segment that we're extracting squares from. Pop head. */
  public static int _currentHead;
  
  /** Queue segment that we'll be extracting squares from next. X map coordinates. */
  public static int[] _nextMX = new int[M.arrayWidth * M.arrayHeight];
  /** Queue segment that we'll be extracting squares from next. Y map coordinates. */
  public static int[] _nextMY = new int[M.arrayWidth * M.arrayHeight];
  /** Queue segment that we'll be extracting squares from next. Queue size. */
  public static int _nextHead;

  /** Queue segment that receives orthogonal neighbors from current. X map coordinates. */
  public static int[] _orthogonalMX = new int[M.arrayWidth * M.arrayHeight];
  /** Queue segment that receives orthogonal neighbors from current. Y map coordinates. */
  public static int[] _orthogonalMY = new int[M.arrayWidth * M.arrayHeight];
  /** Queue segment that receives orthogonal neighbors from current. Push head. */
  public static int _orthogonalHead;
  /** The cost to reach the neighbors that are diagonal to the current square. */
  public static int _orthogonalCost;

  /** Queue segment that receives diagonal neighbors from current. X map coordinates. */
  public static int[] _diagonalMX = new int[M.arrayWidth * M.arrayHeight];
  /** Queue segment that receives diagonal neighbors from current. Y map coordinates. */
  public static int[] _diagonalMY = new int[M.arrayWidth * M.arrayHeight];
  /** Queue segment that receives diagonal neighbors from current. Push head. */
  public static int _diagonalHead;
  /** The cost to reach the neighbors that are diagonal to the current square. */
  public static int _diagonalCost;

  /** Queue segment that receives unknown orthogonal neighbors from current. X map coordinates. */
  public static int[] _unknownMX = new int[M.arrayWidth * M.arrayHeight];
  /** Queue segment that receives unknown orthogonal neighbors from current. Y map coordinates. */
  public static int[] _unknownMY = new int[M.arrayWidth * M.arrayHeight];
  /** Queue segment that receives unknown orthogonal neighbors from current. Push head. */
  public static int _unknownHead;
  /** The cost to reach the neighbors that are diagonal to the current square. */
  public static int _unknownCost;
}