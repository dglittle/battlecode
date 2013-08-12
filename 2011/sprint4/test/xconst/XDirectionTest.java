package sprint4.test.xconst;

import sprint4.core.xconst.XDirection;
import battlecode.common.Direction;

public final class XDirectionTest {
  public static void debug_Test() {
    testDirectionIntMapping();
  }

  public static void testDirectionIntMapping() {
    Direction[] goldDirections = Direction.values();
    
    for (int i = 0; i < goldDirections.length; i++) {
      Direction d = goldDirections[i];
      Direction md = XDirection.intToDirection[i];
      if (d != md) {
        throw new RuntimeException("intToDirection[" + i + "] is " + md + " not " + d);
      }
            
      int di = d.ordinal();
      if (di != i) {
        throw new RuntimeException(d.toString() + ".ordinal() is " + di + " not " + i);
      }
      
      int dx = XDirection.intToDeltaX[i];
      if (dx != d.dx) {
        throw new RuntimeException("int -> dx broken for " + i + " -> " + dx + " not " + d.dx);
      }

      int dy = XDirection.intToDeltaY[i];
      if (dy != d.dy) {
        throw new RuntimeException("int -> dy broken for " + i + " -> " + dy + " not " + d.dy);
      }
    
      if (d.dx != 0 || d.dy != 0) {
        if (XDirection.pathfindingCost[i] != (d.isDiagonal() ? 3 : 2)) {
          throw new RuntimeException("pathfindingCost broken for " + i);
        }
        if (XDirection.ADJACENT_DIRECTIONS < i) {
          throw new RuntimeException("ADJACENT_DIRECTIONS too small");
        }
        
      } else {
        if (XDirection.ADJACENT_DIRECTIONS > i) {
          throw new RuntimeException("ADJACENT_DIRECTIONS too large");
        }        
      }
    }

    if (Direction.NORTH.ordinal() != XDirection.NORTH_INT) {
      throw new RuntimeException("NORTH.ordinal() is wrong");
    }
    if (Direction.NORTH_EAST.ordinal() != XDirection.NORTH_EAST_INT) {
      throw new RuntimeException("NORTH_EAST.ordinal() is wrong");
    }
    if (Direction.EAST.ordinal() != XDirection.EAST_INT) {
      throw new RuntimeException("EAST.ordinal() is wrong");
    }
    if (Direction.SOUTH_EAST.ordinal() != XDirection.SOUTH_EAST_INT) {
      throw new RuntimeException("SOUTH_EAST.ordinal() is wrong");
    }
    if (Direction.SOUTH.ordinal() != XDirection.SOUTH_INT) {
      throw new RuntimeException("SOUTH.ordinal() is wrong");
    }
    if (Direction.SOUTH_WEST.ordinal() != XDirection.SOUTH_WEST_INT) {
      throw new RuntimeException("SOUTH_WEST.ordinal() is wrong");
    }
    if (Direction.WEST.ordinal() != XDirection.WEST_INT) {
      throw new RuntimeException("WEST.ordinal() is wrong");
    }
    if (Direction.OMNI.ordinal() != XDirection.OMNI_INT) {
      throw new RuntimeException("OMNI.ordinal() is wrong");
    }
    if (Direction.NONE.ordinal() != XDirection.NONE_INT) {
      throw new RuntimeException("NONE.ordinal() is wrong");
    }
    if (XDirection.intToDirection[XDirection.NULL_INT] != null) {
      throw new RuntimeException("NULL_INT is wrong");
    }
  }  
}
