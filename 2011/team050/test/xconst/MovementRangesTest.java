package team050.test.xconst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import team050.core.S;
import team050.core.xconst.MovementRanges;
import team050.core.xconst.XDirection;
import battlecode.common.ComponentType;
import battlecode.common.Direction;

public class MovementRangesTest {
  public static void debug_Test() {
    testJumpRange();
  }
  
  public static void testJumpRange() {
    int[][] rangeDx = new int[XDirection.ADJACENT_DIRECTIONS][];
    int[][] rangeDy = new int[XDirection.ADJACENT_DIRECTIONS][];
    generateRangeDxDy(rangeDx, rangeDy, ComponentType.JUMP.range, 90);
    
    MovementRanges.initJumpByDirection();
    checkRangeDeltas(rangeDx, S.jumpDxByDirection, "S.jumpDxByDirection");
    checkRangeDeltas(rangeDy, S.jumpDyByDirection, "S.jumpDyByDirection");
    S.jumpDxByDirection = null;
    S.jumpDyByDirection = null;
  }
  
  /** Common code in testSensor*Deltas. */
  public static void checkRangeDeltas(int[][] goldenDeltas, int[][] rangeDeltas,
                                      String label) {
    String dxs = Arrays.deepToString(goldenDeltas);
    String rdxs = Arrays.deepToString(rangeDeltas);
    if (!dxs.equals(rdxs)) {
      // NOTE: trailing ; makes it easy to copy-paste into source code.
      throw new RuntimeException("Mismatching " + label + ": got " + rdxs +
          " but wanted " + dxs.replace('[', '{').replace(']', '}') + ";");
    }    
  }
  
  /**
   * Computes sensor delta arrays for a robot.
   * 
   * @param type the RobotType to compute sensor coverage arrays for
   * @param rangeDx will be filled with a range dx array
   * @param rangeDy will be filled with a range dy array
   */
  public static void generateRangeDxDy(int[][] rangeDx, int[][] rangeDy,
                                       int range, int angle) {
    // Maximum half-angle for movement range.
    double mtheta = angle * Math.PI / 360.0;
    for (int j = 0; j < XDirection.ADJACENT_DIRECTIONS; j++) {
      ArrayList<int[]> rangeDeltas = new ArrayList<int[]>();

      final Direction d = XDirection.intToDirection[j];
      final double dtheta = Math.atan2(d.dy, d.dx);
      int radius2 = range;
      int radius = (int)Math.ceil(Math.sqrt(radius2));
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dy = -radius; dy <= radius; dy++) {
          final int pradius2 = dx * dx + dy * dy;
          if (pradius2 == 0 || pradius2 > radius2) continue;
          
          // Normalized angle between direction vector and square vector.
          if (dx != 0 || dy != 0) {
            double theta = Math.atan2(dy, dx) - dtheta;
            while (theta < Math.PI) theta += Math.PI * 2;
            while (theta > Math.PI) theta -= Math.PI * 2;
            if (Math.abs(theta) > mtheta + 0.0001) continue;
          }
          
          rangeDeltas.add(new int[] {dx, dy});
        }
      }
      sortStoreRangeDxDy(rangeDx, rangeDy, j, d.dx, d.dy, rangeDeltas);
    }
  }
  
  /** Common code in generate*RangeDxDy. */
  public static void sortStoreRangeDxDy(int[][] rangeDx, int[][] rangeDy, int j, 
      int ddx, int ddy, ArrayList<int[]> rangeDeltas) {
    final int px = ddx * 1000;
    final int py = ddy * 1000;
    Collections.sort(rangeDeltas, new Comparator<int[]>() {
      public int compare(int[] v1, int[] v2) {               
        int d1 = (px - v1[0]) * (px - v1[0]) + (py - v1[1]) * (py - v1[1]);
        int d2 = (px - v2[0]) * (px - v2[0]) + (py - v2[1]) * (py - v2[1]);
        if (d1 < d2) return -1;
        if (d1 > d2) return 1;
        
        return 0;
      }
    });
    
    rangeDx[j] = new int[rangeDeltas.size()];
    rangeDy[j] = new int[rangeDeltas.size()];
    for (int i = 0; i < rangeDeltas.size(); i++) {
      rangeDx[j][i] = rangeDeltas.get(i)[0];
      rangeDy[j][i] = rangeDeltas.get(i)[1];
    }    
  }
}
