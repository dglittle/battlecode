package team050.core.xconst;

/** Base configurations, ordered by desirability. */
public class BaseConfigs {
  /** Spawn point X coordinate, relative to Recycler. */
  public static int[] spawnDx;
  /** Spawn point Y coordinate, relative to Recycler. */
  public static int[] spawnDy;
  /** Armory X coordinate, relative to Recycler. */
  public static int[] armoryDx;
  /** Armory Y coordinate, relative to Recycler. */
  public static int[] armoryDy;
  /** Factory X coordinate, relative to Recycler. */
  public static int[] factoryDx;
  /** Factory Y coordinate, relative to Recycler. */
  public static int[] factoryDy;
  /** Score for each configuration. */
  public static int[] score;
  
  /** Populates the constants in {@link BaseConfigs}. */
  public static void initConfigs() {
    spawnDx = new int[] {-1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    spawnDy = new int[] {-1, -1, 0, 0, 0, 0, 1, 1, -1, -1, -1, -1, 1, 1, 1, 1, -1, -1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, 0, 0, 0, 0, 1, 1, 1, 1, -1, -1, -1, -1, 1, 1, 1, 1, -1, -1, -1, -1, 0, 0, 0, 0, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 0, 0, 0, 0, 1, 1, 1, 1, -1, -1, -1, -1, 1, 1, 1, 1, -1, -1, -1, -1, 0, 0, 0, 0, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    armoryDx = new int[] {-1, 0, -1, -1, 0, 0, -1, 0, -1, -1, 1, 1, -1, -1, 1, 1, 0, 1, 0, 0, 1, 1, 0, 1, -1, -1, -1, -1, 0, 0, 0, 0, -1, -1, -1, -1, 1, 1, 1, 1, -1, -1, -1, -1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, -1, -2, -2, 0, -2, -2, -2, -2, -1, -2, -2, 0, -1, 0, 0, 1, -1, 0, 0, 1, 0, 1, 2, 2, 2, 2, 2, 2, 0, 1, 2, 2, -1, -2, -2, -2, -2, 0, -2, -2, -2, -2, -2, -2, -2, -2, -1, -2, -2, -2, -2, 0, -1, -1, -1, 0, 0, 1, 1, 1, -1, -1, -1, 0, 0, 1, 1, 1, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 2, 2, 2, 2, -1, -1, 0, 0, -1, -1, -1, -1, -1, -1, 0, 0, -1, -1, 1, 1, -1, -1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, -1, -1, -1, 0, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, -1, -1, -1, -1, 1, 1, 1, 1, -1, -1, -1, -1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, -1, -1, -1, -2, -2, -2, -2, -2, -2, 0, -2, -2, -2, -2, -1, -1, -1, -2, -2, -2, -2, -2, -2, 0, -1, 0, 0, 1, -1, 0, 0, 1, 0, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 1, 1, 2, 2, 2, 2, 2, 2, -1, -2, -2, -2, -2, -2, -2, 0, 0, 0, -2, -2, -1, -2, -2, -2, -2, -2, -2, 0, 0, 0, -1, 1, -1, 1, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2};
    armoryDy = new int[] {0, -1, -1, 1, -1, 1, 0, 1, -1, 0, -1, 0, 0, 1, 0, 1, -1, 0, -1, 1, -1, 1, 1, 0, -1, -1, 1, 1, -1, -1, 1, 1, -1, -1, 0, 0, -1, -1, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, -1, -1, 1, 1, -1, -1, 1, 1, -2, -1, 0, -2, -1, 0, 0, 1, 2, 0, 1, 2, -2, -2, -2, -2, 2, 2, 2, 2, -2, -2, -1, 0, -1, 0, 0, 1, 2, 2, 0, 1, -2, -1, -2, -2, 0, -2, -1, -1, -1, 0, 0, 1, 1, 1, 2, 0, 1, 2, 2, 2, -2, -2, -2, -2, -2, -2, -2, -2, 2, 2, 2, 2, 2, 2, 2, 2, -2, -2, -1, -2, -2, 0, -1, -1, -1, 0, 0, 1, 1, 1, 2, 2, 0, 1, 2, 2, 0, 0, -1, -1, -1, -1, 1, 1, 0, 0, 1, 1, -1, -1, -1, -1, 1, 1, 1, 1, -1, -1, 0, 0, -1, -1, 1, 1, 1, 1, 0, 0, 0, 0, 0, -1, -1, -1, -1, 1, -1, -1, -1, 1, 1, 1, 0, 0, 0, 1, 1, 1, -1, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, -1, -1, -1, 0, 0, 0, -1, -1, -1, 1, 1, 1, -1, 1, 1, 1, 1, 0, 0, 0, -2, -2, -2, -1, -1, -1, -2, -2, 0, -2, -1, 0, 0, 1, 2, 2, 2, 0, 1, 1, 1, 2, 2, 2, -2, -2, -2, -2, 2, 2, 2, 2, -2, -2, -2, -2, -1, -1, -1, -2, -2, 0, -1, 0, 0, 1, 2, 2, 2, 2, 0, 1, 1, 1, 2, 2, -2, -1, -2, -2, 0, 0, 0, -2, -2, -2, -1, 1, 2, 0, 0, 0, 1, 2, 2, 2, 2, 2, -2, -2, 2, 2, -2, -2, -2, -2, -1, -2, -2, 0, 0, 0, -1, 1, 2, 2, 2, 2, 0, 0, 0, 1, 2, 2};
    factoryDx = new int[] {0, -1, 0, 0, -1, -1, 0, -1, -1, -1, 1, 1, -1, -1, 1, 1, 1, 0, 1, 1, 0, 0, 1, 0, -1, 0, -1, 0, -1, 0, -1, 0, 1, 1, 1, 1, -1, -1, -1, -1, 1, 1, 1, 1, -1, -1, -1, -1, 0, 1, 0, 1, 0, 1, 0, 1, 0, -1, -1, 0, -1, -1, -1, -1, 0, -1, -1, 0, -1, -1, 1, 1, -1, -1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, -1, 0, -1, 0, 0, -1, -1, 0, 0, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, -1, -1, 1, 1, -1, 1, -1, -1, 1, -1, 1, 1, -1, 1, -1, -1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, -2, -2, -1, 0, -2, -2, -2, -2, -2, -2, -1, 0, -1, 0, 0, 1, -1, 0, 0, 1, 0, 1, 2, 2, 2, 2, 2, 2, 0, 1, 2, 2, -1, -2, 0, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -1, -2, 0, -2, -2, -2, 1, -1, 0, 1, -1, -1, 0, 1, -1, 0, 1, 1, -1, 0, 1, -1, 2, 2, 2, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 2, -2, -2, 0, -1, -2, -2, -1, -2, -2, -1, -2, -2, -2, -2, -2, -2, 0, -2, -1, -2, -2, -1, -2, -1, 0, -1, 1, 0, 0, -1, 1, 0, 1, 0, 2, 2, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1, 0, 2, 2, 2, 1, 2, 2, 1, 2, -2, 0, -2, 0, -1, -2, 0, -2, -2, -2, -2, -2, -2, -1, -2, 0, 0, -2, 0, -2, -2, -2, 1, -1, 1, -1, 2, 2, 2, 2, 0, 0, 2, 0, 1, 2, 2, 2, 2, 2, 2, 2, 0, 1, 2, 0, 0, 2};
    factoryDy = new int[] {-1, 0, -1, 1, -1, 1, 1, 0, 0, -1, 0, -1, 1, 0, 1, 0, 0, -1, -1, 1, -1, 1, 0, 1, 1, 1, -1, -1, 1, 1, -1, -1, -1, 0, -1, 0, -1, 0, -1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, -1, -1, 1, 1, -1, -1, -1, 0, 0, -1, -1, -1, 1, 1, 1, 0, 0, 1, -1, -1, -1, -1, 1, 1, 1, 1, -1, -1, 0, 0, -1, -1, 1, 1, 1, 1, 0, 0, 0, -1, 0, -1, -1, 0, 1, -1, 1, -1, 1, -1, -1, 1, 0, 1, 1, 0, 1, 0, 0, -1, 0, 0, 0, -1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, -1, -1, 0, -1, -1, 1, 1, -1, 1, -1, 1, -1, 0, 0, 1, 1, 1, 0, -1, 0, -2, -2, -1, 0, 0, 1, 0, 1, 2, 2, -2, -2, -2, -2, 2, 2, 2, 2, -2, -2, -1, 0, -1, 0, 0, 1, 2, 2, 0, 1, -2, -2, -2, -1, -2, 0, 1, -1, -1, 0, 1, -1, 0, 1, 2, 2, 2, 0, 1, 2, -2, -2, -2, -2, -2, -2, -2, -2, 2, 2, 2, 2, 2, 2, 2, 2, -1, -2, 0, -2, -2, -2, -1, 0, 1, -1, 0, 1, 1, -1, 0, 1, 2, 2, 2, 2, -1, -2, -2, -2, -2, 0, -2, -1, -1, -2, 0, -1, 1, 0, 1, 2, 2, 1, 2, 0, 2, 2, 1, 2, -2, -2, -2, -2, 2, 2, 2, 2, -2, -2, -1, -2, -2, -2, 0, -2, -1, -1, 0, -1, 1, 0, 2, 2, 1, 2, 1, 2, 0, 2, 2, 1, 0, -2, 0, -2, -2, -2, -2, -1, -2, 0, 1, -1, 0, 2, 2, 2, 2, 0, 2, 0, 1, 2, -2, -2, 2, 2, -1, -2, 0, 0, -2, -2, 0, -2, -2, -2, 1, -1, 0, 1, 2, 0, 2, 2, 2, 2, 2, 0};
    score = new int[] {111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  }
}