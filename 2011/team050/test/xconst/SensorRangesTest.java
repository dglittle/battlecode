package team050.test.xconst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import team050.core.S;
import team050.core.xconst.SensorRanges;
import team050.core.xconst.XComponentType;
import team050.core.xconst.XDirection;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.SensorController;

public class SensorRangesTest {
  public static void debug_Test() {
    testMaxRange();
    testSensorDeltas();
  }
  
  public static void testMaxRange() {
    final int maxRange2 = SensorRanges.MAX_RANGE * SensorRanges.MAX_RANGE;
    boolean maxRangeReached = false;
    for(int i = XComponentType.intToComponentType.length - 1; i >= 0; i--) {
      ComponentType type = XComponentType.intToComponentType[i];
      if (type.controller != SensorController.class) {
        continue;
      }
      if (maxRange2 < type.range) {
        throw new RuntimeException("MAX_RANGE is below the range of " + type);
      } else if (maxRange2 == type.range) {
        maxRangeReached = true;
      }
    }
    if (!maxRangeReached) {
      throw new RuntimeException("MAX_RANGE is too big");
    }
  }
  
  public static void testSensorDeltas() {
    // NOTE: reverse-iterating because it's a lot easier to check correctness on
    //       the building sensor than on the satellite.
    for(int i = XComponentType.intToComponentType.length - 1; i >= 0; i--) {
      ComponentType type = XComponentType.intToComponentType[i];
      if (type.controller != SensorController.class) {
        continue;
      }
      
      S.sensorTypeInt = i;
      SensorRanges.initSensorDxDy();

      int[][] sensorDx = new int[XDirection.ADJACENT_DIRECTIONS][];
      int[][] sensorDy = new int[XDirection.ADJACENT_DIRECTIONS][];
      
      generateSensorDxDy(type, sensorDx, sensorDy);
      checkSensorDeltas(sensorDx, S.sensorDxByDirection, type,
          "S.sensorDxByDirection");
      checkSensorDeltas(sensorDy, S.sensorDyByDirection, type,
          "S.sensorDyByDirection");

      generateSensorEdgeDxDy(type, sensorDx, sensorDy, -1);    
      checkSensorDeltas(sensorDx, S.sensorEdgeDxByDirection, type,
          "S.sensorEdgeDxByDirection");
      checkSensorDeltas(sensorDy, S.sensorEdgeDyByDirection, type,
          "S.sensorEdgeDyByDirection");

      generateSensorEdgeDxDy(type, sensorDx, sensorDy, 1);    
      checkSensorDeltas(sensorDx, S.sensorBackEdgeDxByDirection, type,
          "S.sensorBackEdgeDxByDirection");
      checkSensorDeltas(sensorDy, S.sensorBackEdgeDyByDirection, type,
          "S.sensorBackEdgeDyByDirection");
    }
    // Re-build the correct state in G so the rest of the tests can pass.
    S.sensorTypeInt = S.sensorType.ordinal();
    SensorRanges.initSensorDxDy();
  }
  
  /** Common code in testSensor*Deltas. */
  public static void checkSensorDeltas(int[][] sensorDelta, int[][] rSensorDelta,
                                       ComponentType type, String label) {
    String dxs = Arrays.deepToString(sensorDelta);
    String rdxs = Arrays.deepToString(rSensorDelta);
    if (!dxs.equals(rdxs)) {
      // NOTE: trailing ; makes it easy to copy-paste the line into the MapUtil source.
      throw new RuntimeException("Mismatching " + label + " for " + type.toString() +
          ": got " + rdxs + " but wanted " + dxs.replace('[', '{').replace(']', '}') + ";");
    }    
  }
  
  /**
   * Computes sensor delta arrays for a robot.
   * 
   * @param type the RobotType to compute sensor coverage arrays for
   * @param sensorDx will be filled with sensing dx arrays for each direction
   * @param sensorDy will be filled with sensing dy arrays for each direction
   */
  public static void generateSensorDxDy(ComponentType type, int[][] sensorDx, int[][] sensorDy) {
    // Maximum half-angle for sensor range.
    double mtheta = type.angle * Math.PI / 360.0;
    
    for (int j = 0; j < XDirection.ADJACENT_DIRECTIONS; j++) {
      ArrayList<int[]> sensorDeltas = new ArrayList<int[]>();

      Direction d = XDirection.intToDirection[j];
      final double dtheta = Math.atan2(d.dy, d.dx);
      int radius2 = type.range;
      int radius = (int)Math.ceil(Math.sqrt(radius2));
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dy = -radius; dy <= radius; dy++) {
          if (dx * dx + dy * dy > radius2) continue;
          
          // Normalized angle between direction vector and square vector.
          if (dx != 0 || dy != 0) {
            double theta = Math.atan2(dy, dx) - dtheta;
            while (theta < Math.PI) theta += Math.PI * 2;
            while (theta > Math.PI) theta -= Math.PI * 2;
            if (Math.abs(theta) > mtheta + 0.0001) continue;
          }
          
          sensorDeltas.add(new int[] {dx, dy});
        }
      }
      sortStoreSensorDxDy(sensorDx, sensorDy, j, dtheta, sensorDeltas);
    }
  }

  /**
   * Computes sensor edge delta arrays for a robot.
   * 
   * @param type the RobotType to compute sensor coverage arrays for
   * @param sensorDx will be filled with sensing dx arrays for each direction
   * @param sensorDy will be filled with sensing dy arrays for each direction
   * @param dMultiplier set to -1 for a forward move, 1 for a backward move
   */
  public static void generateSensorEdgeDxDy(ComponentType type,
      int[][] sensorDx, int[][] sensorDy, int dMultiplier) {
    // Maximum half-angle for sensor range.
    double mtheta = type.angle * Math.PI / 360.0;
        
    for (int j = 0; j < XDirection.ADJACENT_DIRECTIONS; j++) {
      final ArrayList<int[]> sensorDeltas = new ArrayList<int[]>();

      final Direction d = XDirection.intToDirection[j];
      final double dtheta = Math.atan2(d.dy, d.dx);
      final int radius2 = type.range;
      final int radius = (int)Math.ceil(Math.sqrt(radius2));
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dy = -radius; dy <= radius; dy++) {
          int dx2 = dx - dMultiplier * d.dx;
          int dy2 = dy - dMultiplier * d.dy;
          
          if (dx * dx + dy * dy > radius2) continue;
          if (dx2 * dx2 + dy2 * dy2 <= radius2) {
            if (dx2 == 0 &&  dy2 == 0) { continue; }
            double theta = Math.atan2(dy2, dx2) - dtheta;
            while (theta < Math.PI) theta += Math.PI * 2;
            while (theta > Math.PI) theta -= Math.PI * 2;
            if (Math.abs(theta) <= mtheta + 0.0001) continue;            
          }
          
          // Normalized angle between direction vector and square vector.
          if (dx != 0 || dy != 0) {
            double theta = Math.atan2(dy, dx) - dtheta;
            while (theta < Math.PI) theta += Math.PI * 2;
            while (theta > Math.PI) theta -= Math.PI * 2;
            if (Math.abs(theta) > mtheta + 0.0001) continue;
          }
          
          sensorDeltas.add(new int[] {dx, dy});
        }
      }
      sortStoreSensorDxDy(sensorDx, sensorDy, j, dtheta, sensorDeltas);
    }
  }
  
  /** Common code in generate*SensorDxDy. */
  public static void sortStoreSensorDxDy(int[][] sensorDx, int[][] sensorDy, int j,
                                         double dtheta, ArrayList<int[]> sensorDeltas) {
    final double _dtheta = dtheta;
    Collections.sort(sensorDeltas, new Comparator<int[]>() {
      public int compare(int[] v1, int[] v2) {
        int d1 = v1[0] * v1[0] + v1[1] * v1[1];
        int d2 = v2[0] * v2[0] + v2[1] * v2[1];          
        if (d1 < d2) return -1;
        if (d1 > d2) return 1;
        
        double a1 = Math.atan2(v1[1], v1[0]) - _dtheta;
        while (a1 < Math.PI) a1 += Math.PI * 2;
        while (a1 > Math.PI) a1 -= Math.PI * 2;
        double a2 = Math.atan2(v2[1], v2[0]) - _dtheta;
        while (a2 < Math.PI) a2 += Math.PI * 2;
        while (a2 > Math.PI) a2 -= Math.PI * 2;

        double delta = Math.abs(a1) - Math.abs(a2);
        if (delta < -0.0001) return -1;
        if (delta > 0.0001) return 1;
        
        return 0;
      }
    });
    
    sensorDx[j] = new int[sensorDeltas.size()];
    sensorDy[j] = new int[sensorDeltas.size()];
    for (int i = 0; i < sensorDeltas.size(); i++) {
      sensorDx[j][i] = sensorDeltas.get(i)[0];
      sensorDy[j][i] = sensorDeltas.get(i)[1];
    }    
  }
}
