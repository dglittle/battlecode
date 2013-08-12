package team050.test.xconst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import team050.core.xconst.BaseConfigs;

public class BaseConfigsTest {
  public static final void debug_Test() {
    test_configs();
  }
  
  public static final void test_configs() {
    BaseConfigs.initConfigs();
    
    // Generate all base configurations with scores.
    ArrayList<int[]> configs = new ArrayList<int[]>();
    for (int sx = -1; sx <= 1; sx++) {
      for (int sy = -1; sy <= 1; sy++) {
        if (sx == 0 && sy == 0) { continue; }
        for (int ax = -2; ax <= 2; ax++) {
          for (int ay = -2; ay <= 2; ay++) {
            if (ax == 0 && ay == 0) { continue; }
            if (!adj(sx, sy, ax, ay)) { continue; }
            for (int fx = -2; fx <= 2; fx++) {
              for (int fy = -2; fy <= 2; fy++) {
                if (fx == 0 && fy == 0) { continue; }
                if (fx == ax && fy == ay) { continue; }
                if (!adj(sx, sy, fx, fy)) { continue; }
                
                int score = 0;
                // Factory has a Telescope, so it needs Antenna from Recycler
                if (adj(0, 0, fx, fy)) { score += 100; }
                // If Armory is next to Recycler, it can get stuff from it
                if (adj(0, 0, ax, ay)) { score += 10; }
                // Ideally, they'd all be adjacent
                if (adj(fx, fy, ax, ay)) { score += 1; }
                
                configs.add(new int[] {score, sx, sy, ax, ay, fx, fy});
              }
            }
          }
        }
      }
    }   
    // Sort by score.
    Collections.sort(configs, new Comparator<int[]>() {
      public int compare(int[] v1, int[] v2) {
        if (v1[0] < v2[0]) return 1;
        if (v1[0] > v2[0]) return -1;
        // String sort to break ties.
        return Arrays.toString(v1).compareTo(Arrays.toString(v2));
      }
    });
    // Check constants.
    checkArray(configs, 1, BaseConfigs.spawnDx, "BaseConfigs.spawnDx");
    checkArray(configs, 2, BaseConfigs.spawnDy, "BaseConfigs.spawnDy");
    checkArray(configs, 3, BaseConfigs.armoryDx, "BaseConfigs.armoryDx");
    checkArray(configs, 4, BaseConfigs.armoryDy, "BaseConfigs.armoryDy");
    checkArray(configs, 5, BaseConfigs.factoryDx, "BaseConfigs.factoryDx");
    checkArray(configs, 6, BaseConfigs.factoryDy, "BaseConfigs.factoryDy");
    checkArray(configs, 0, BaseConfigs.score, "BaseConfigs.score");
  }
  
  public static final void checkArray(ArrayList<int[]> configs, int j,
      int[] array, String label) {
    int[] gold = new int[configs.size()];
    for (int i = 0; i < gold.length; i++) {
      gold[i] = configs.get(i)[j];
    }
    String gs = Arrays.toString(gold);
    String s = Arrays.toString(array);
    if (!gs.equals(s)) {
      throw new RuntimeException(label + " is incorrect; got " + s +
          "; wanted " + gs.replace('[', '{').replace(']', '}') + ";");
    }
  }
  
  /** True if (x1, y1) and (x2, y2) are adjacent but not identical. */
  public static final boolean adj(int x1, int y1, int x2, int y2) {
    if (x1 == x2 && y1 == y2) { return false; }
    return Math.abs(x1 - x2) <= 1 && Math.abs(y1 - y2) <= 1;
  }
}
