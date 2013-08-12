package sprint2.test.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import sprint2.core.S;
import sprint2.util.ComponentUtil;
import sprint2.util.MapUtil;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.SensorController;

public final class MapUtilTest {
  public static void debug_Test() {
    testDirectionIntMapping();
    testSensorDeltas();
  }

  public static void testDirectionIntMapping() {
    Direction[] goldDirections = Direction.values();
    
    for (int i = 0; i < goldDirections.length; i++) {
      Direction d = goldDirections[i];
      Direction md = MapUtil.intToDirection[i];
      if (d != md) {
        throw new RuntimeException("intToDirection[" + i + "] is " + md + " not " + d);
      }
            
      int di = d.ordinal();
      if (di != i) {
        throw new RuntimeException(d.toString() + ".ordinal() is " + di + " not " + i);
      }
      
      int dx = MapUtil.intToDeltaX[i];
      if (dx != d.dx) {
        throw new RuntimeException("int -> dx broken for " + i + " -> " + dx + " not " + d.dx);
      }

      int dy = MapUtil.intToDeltaY[i];
      if (dy != d.dy) {
        throw new RuntimeException("int -> dy broken for " + i + " -> " + dy + " not " + d.dy);
      }
    
      if ((d.dx != 0 || d.dy != 0) && MapUtil.ADJACENT_DIRECTIONS < i) {
        throw new RuntimeException("ADJACENT_DIRECTIONS too small");
      }
      if ((d.dx == 0 && d.dy == 0) && MapUtil.ADJACENT_DIRECTIONS > i) {
        throw new RuntimeException("ADJACENT_DIRECTIONS too large");
      }
    }
  }
  
  public static void testSensorDeltas() {
    for(int i = 0; i < ComponentUtil.intToComponentType.length; i++) {
      ComponentType type = ComponentUtil.intToComponentType[i];
      if (type.controller != SensorController.class) {
        continue;
      }
      
      S.sensorTypeInt = i;
      MapUtil.initSensorDxDy();

      int[][] sensorDx = new int[MapUtil.ADJACENT_DIRECTIONS][];
      int[][] sensorDy = new int[MapUtil.ADJACENT_DIRECTIONS][];
      
      generateSensorDxDy(type, sensorDx, sensorDy);
      checkSensorDeltas(sensorDx, S.sensorDxByDirection, type, "G.sensorDxByDirection");
      checkSensorDeltas(sensorDy, S.sensorDyByDirection, type, "G.sensorDyByDirection");

      generateSensorEdgeDxDy(type, sensorDx, sensorDy);    
      checkSensorDeltas(sensorDx, S.sensorEdgeDxByDirection, type, "G.sensorEdgeDxByDirection");
      checkSensorDeltas(sensorDy, S.sensorEdgeDyByDirection, type, "G.sensorEdgeDyByDirection");
    }
    // Re-build the correct state in G so the rest of the tests can pass.
    S.sensorTypeInt = S.sensorType.ordinal();
    MapUtil.initSensorDxDy();
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
    
    for (int j = 0; j < MapUtil.ADJACENT_DIRECTIONS; j++) {
      ArrayList<int[]> sensorDeltas = new ArrayList<int[]>();

      Direction d = MapUtil.intToDirection[j];
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
   */
  public static void generateSensorEdgeDxDy(ComponentType type, int[][] sensorDx, int[][] sensorDy) {
    // Maximum half-angle for sensor range.
    double mtheta = type.angle * Math.PI / 360.0;
        
    for (int j = 0; j < MapUtil.ADJACENT_DIRECTIONS; j++) {
      ArrayList<int[]> sensorDeltas = new ArrayList<int[]>();

      Direction d = MapUtil.intToDirection[j];
      final double dtheta = Math.atan2(d.dy, d.dx);
      int radius2 = type.range;
      int radius = (int)Math.ceil(Math.sqrt(radius2));
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dy = -radius; dy <= radius; dy++) {
          if (dx * dx + dy * dy > radius2) continue;
          if ((dx + d.dx) * (dx + d.dx) + (dy + d.dy) * (dy + d.dy) <= radius2) continue;
          
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
        
        if (a1 < a2 - 0.0001) return -1;
        if (a1 > a2 + 0.0001) return -1;
        
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
