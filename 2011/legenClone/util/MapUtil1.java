package legenClone.util;

import legenClone.core.S;

/**
 * Auto-generated constants for MapUtil.
 */
public final class MapUtil1 {
  public static final void initSensorDxDy() {
    switch (S.sensorTypeInt) {
    case ComponentUtil.SATELLITE_INT:
      S.sensorDxByDirection = new int[][] {{0, -1, 0, 0, 1, -1, -1, 1, 1, -2, 0, 0, 2, -2, -2, -1, -1, 1, 1, 2, 2, -2, -2, 2, 2, -3, 0, 0, 3, -3, -3, -1, -1, 1, 1, 3, 3, -3, -3, -2, -2, 2, 2, 3, 3, -4, 0, 0, 4, -4, -4, -1, -1, 1, 1, 4, 4, -3, -3, 3, 3, -4, -4, -2, -2, 2, 2, 4, 4, -5, -4, -4, -3, -3, 0, 0, 3, 3, 4, 4, 5, -5, -5, -1, -1, 1, 1, 5, 5, -5, -5, -2, -2, 2, 2, 5, 5, -4, -4, 4, 4, -5, -5, -3, -3, 3, 3, 5, 5, -6, 0, 0, 6, -6, -6, -1, -1, 1, 1, 6, 6, -6, -6, -2, -2, 2, 2, 6, 6, -5, -5, -4, -4, 4, 4, 5, 5, -6, -6, -3, -3, 3, 3, 6, 6, -7, 0, 0, 7, -7, -7, -5, -5, -1, -1, 1, 1, 5, 5, 7, 7, -6, -6, -4, -4, 4, 4, 6, 6, -7, -7, -2, -2, 2, 2, 7, 7, -7, -7, -3, -3, 3, 3, 7, 7, -6, -6, -5, -5, 5, 5, 6, 6, -8, 0, 0, 8, -8, -8, -7, -7, -4, -4, -1, -1, 1, 1, 4, 4, 7, 7, 8, 8, -8, -8, -2, -2, 2, 2, 8, 8, -6, -6, 6, 6, -8, -8, -3, -3, 3, 3, 8, 8, -7, -7, -5, -5, 5, 5, 7, 7, -8, -8, -4, -4, 4, 4, 8, 8, -9, 0, 0, 9, -9, -9, -1, -1, 1, 1, 9, 9, -9, -9, -7, -7, -6, -6, -2, -2, 2, 2, 6, 6, 7, 7, 9, 9, -8, -8, -5, -5, 5, 5, 8, 8, -9, -9, -3, -3, 3, 3, 9, 9, -9, -9, -4, -4, 4, 4, 9, 9, -7, -7, 7, 7, -10, -8, -8, -6, -6, 0, 0, 6, 6, 8, 8, 10}, {0, -1, 0, 0, 1, -1, -1, 1, 1, -2, 0, 0, 2, -2, -2, -1, -1, 1, 1, 2, 2, -2, -2, 2, 2, -3, 0, 0, 3, -3, -3, -1, -1, 1, 1, 3, 3, -3, -3, -2, -2, 2, 2, 3, 3, -4, 0, 0, 4, -4, -4, -1, -1, 1, 1, 4, 4, -3, -3, 3, 3, -4, -4, -2, -2, 2, 2, 4, 4, -5, -4, -4, -3, -3, 0, 0, 3, 3, 4, 4, 5, -5, -5, -1, -1, 1, 1, 5, 5, -5, -5, -2, -2, 2, 2, 5, 5, -4, -4, 4, 4, -5, -5, -3, -3, 3, 3, 5, 5, -6, 0, 0, 6, -6, -6, -1, -1, 1, 1, 6, 6, -6, -6, -2, -2, 2, 2, 6, 6, -5, -5, -4, -4, 4, 4, 5, 5, -6, -6, -3, -3, 3, 3, 6, 6, -7, 0, 0, 7, -7, -7, -5, -5, -1, -1, 1, 1, 5, 5, 7, 7, -6, -6, -4, -4, 4, 4, 6, 6, -7, -7, -2, -2, 2, 2, 7, 7, -7, -7, -3, -3, 3, 3, 7, 7, -6, -6, -5, -5, 5, 5, 6, 6, -8, 0, 0, 8, -8, -8, -7, -7, -4, -4, -1, -1, 1, 1, 4, 4, 7, 7, 8, 8, -8, -8, -2, -2, 2, 2, 8, 8, -6, -6, 6, 6, -8, -8, -3, -3, 3, 3, 8, 8, -7, -7, -5, -5, 5, 5, 7, 7, -8, -8, -4, -4, 4, 4, 8, 8, -9, 0, 0, 9, -9, -9, -1, -1, 1, 1, 9, 9, -9, -9, -7, -7, -6, -6, -2, -2, 2, 2, 6, 6, 7, 7, 9, 9, -8, -8, -5, -5, 5, 5, 8, 8, -9, -9, -3, -3, 3, 3, 9, 9, -9, -9, -4, -4, 4, 4, 9, 9, -7, -7, 7, 7, -10, -8, -8, -6, -6, 0, 0, 6, 6, 8, 8, 10}, {0, -1, 0, 0, 1, -1, -1, 1, 1, -2, 0, 0, 2, -2, -2, -1, -1, 1, 1, 2, 2, -2, -2, 2, 2, -3, 0, 0, 3, -3, -3, -1, -1, 1, 1, 3, 3, -3, -3, -2, -2, 2, 2, 3, 3, -4, 0, 0, 4, -4, -4, -1, -1, 1, 1, 4, 4, -3, -3, 3, 3, -4, -4, -2, -2, 2, 2, 4, 4, -5, -4, -4, -3, -3, 0, 0, 3, 3, 4, 4, 5, -5, -5, -1, -1, 1, 1, 5, 5, -5, -5, -2, -2, 2, 2, 5, 5, -4, -4, 4, 4, -5, -5, -3, -3, 3, 3, 5, 5, -6, 0, 0, 6, -6, -6, -1, -1, 1, 1, 6, 6, -6, -6, -2, -2, 2, 2, 6, 6, -5, -5, -4, -4, 4, 4, 5, 5, -6, -6, -3, -3, 3, 3, 6, 6, -7, 0, 0, 7, -7, -7, -5, -5, -1, -1, 1, 1, 5, 5, 7, 7, -6, -6, -4, -4, 4, 4, 6, 6, -7, -7, -2, -2, 2, 2, 7, 7, -7, -7, -3, -3, 3, 3, 7, 7, -6, -6, -5, -5, 5, 5, 6, 6, -8, 0, 0, 8, -8, -8, -7, -7, -4, -4, -1, -1, 1, 1, 4, 4, 7, 7, 8, 8, -8, -8, -2, -2, 2, 2, 8, 8, -6, -6, 6, 6, -8, -8, -3, -3, 3, 3, 8, 8, -7, -7, -5, -5, 5, 5, 7, 7, -8, -8, -4, -4, 4, 4, 8, 8, -9, 0, 0, 9, -9, -9, -1, -1, 1, 1, 9, 9, -9, -9, -7, -7, -6, -6, -2, -2, 2, 2, 6, 6, 7, 7, 9, 9, -8, -8, -5, -5, 5, 5, 8, 8, -9, -9, -3, -3, 3, 3, 9, 9, -9, -9, -4, -4, 4, 4, 9, 9, -7, -7, 7, 7, -10, -8, -8, -6, -6, 0, 0, 6, 6, 8, 8, 10}, {0, -1, 0, 0, 1, -1, -1, 1, 1, -2, 0, 0, 2, -2, -2, -1, -1, 1, 1, 2, 2, -2, -2, 2, 2, -3, 0, 0, 3, -3, -3, -1, -1, 1, 1, 3, 3, -3, -3, -2, -2, 2, 2, 3, 3, -4, 0, 0, 4, -4, -4, -1, -1, 1, 1, 4, 4, -3, -3, 3, 3, -4, -4, -2, -2, 2, 2, 4, 4, -5, -4, -4, -3, -3, 0, 0, 3, 3, 4, 4, 5, -5, -5, -1, -1, 1, 1, 5, 5, -5, -5, -2, -2, 2, 2, 5, 5, -4, -4, 4, 4, -5, -5, -3, -3, 3, 3, 5, 5, -6, 0, 0, 6, -6, -6, -1, -1, 1, 1, 6, 6, -6, -6, -2, -2, 2, 2, 6, 6, -5, -5, -4, -4, 4, 4, 5, 5, -6, -6, -3, -3, 3, 3, 6, 6, -7, 0, 0, 7, -7, -7, -5, -5, -1, -1, 1, 1, 5, 5, 7, 7, -6, -6, -4, -4, 4, 4, 6, 6, -7, -7, -2, -2, 2, 2, 7, 7, -7, -7, -3, -3, 3, 3, 7, 7, -6, -6, -5, -5, 5, 5, 6, 6, -8, 0, 0, 8, -8, -8, -7, -7, -4, -4, -1, -1, 1, 1, 4, 4, 7, 7, 8, 8, -8, -8, -2, -2, 2, 2, 8, 8, -6, -6, 6, 6, -8, -8, -3, -3, 3, 3, 8, 8, -7, -7, -5, -5, 5, 5, 7, 7, -8, -8, -4, -4, 4, 4, 8, 8, -9, 0, 0, 9, -9, -9, -1, -1, 1, 1, 9, 9, -9, -9, -7, -7, -6, -6, -2, -2, 2, 2, 6, 6, 7, 7, 9, 9, -8, -8, -5, -5, 5, 5, 8, 8, -9, -9, -3, -3, 3, 3, 9, 9, -9, -9, -4, -4, 4, 4, 9, 9, -7, -7, 7, 7, -10, -8, -8, -6, -6, 0, 0, 6, 6, 8, 8, 10}, {0, -1, 0, 0, 1, -1, -1, 1, 1, -2, 0, 0, 2, -2, -2, -1, -1, 1, 1, 2, 2, -2, -2, 2, 2, -3, 0, 0, 3, -3, -3, -1, -1, 1, 1, 3, 3, -3, -3, -2, -2, 2, 2, 3, 3, -4, 0, 0, 4, -4, -4, -1, -1, 1, 1, 4, 4, -3, -3, 3, 3, -4, -4, -2, -2, 2, 2, 4, 4, -5, -4, -4, -3, -3, 0, 0, 3, 3, 4, 4, 5, -5, -5, -1, -1, 1, 1, 5, 5, -5, -5, -2, -2, 2, 2, 5, 5, -4, -4, 4, 4, -5, -5, -3, -3, 3, 3, 5, 5, -6, 0, 0, 6, -6, -6, -1, -1, 1, 1, 6, 6, -6, -6, -2, -2, 2, 2, 6, 6, -5, -5, -4, -4, 4, 4, 5, 5, -6, -6, -3, -3, 3, 3, 6, 6, -7, 0, 0, 7, -7, -7, -5, -5, -1, -1, 1, 1, 5, 5, 7, 7, -6, -6, -4, -4, 4, 4, 6, 6, -7, -7, -2, -2, 2, 2, 7, 7, -7, -7, -3, -3, 3, 3, 7, 7, -6, -6, -5, -5, 5, 5, 6, 6, -8, 0, 0, 8, -8, -8, -7, -7, -4, -4, -1, -1, 1, 1, 4, 4, 7, 7, 8, 8, -8, -8, -2, -2, 2, 2, 8, 8, -6, -6, 6, 6, -8, -8, -3, -3, 3, 3, 8, 8, -7, -7, -5, -5, 5, 5, 7, 7, -8, -8, -4, -4, 4, 4, 8, 8, -9, 0, 0, 9, -9, -9, -1, -1, 1, 1, 9, 9, -9, -9, -7, -7, -6, -6, -2, -2, 2, 2, 6, 6, 7, 7, 9, 9, -8, -8, -5, -5, 5, 5, 8, 8, -9, -9, -3, -3, 3, 3, 9, 9, -9, -9, -4, -4, 4, 4, 9, 9, -7, -7, 7, 7, -10, -8, -8, -6, -6, 0, 0, 6, 6, 8, 8, 10}, {0, -1, 0, 0, 1, -1, -1, 1, 1, -2, 0, 0, 2, -2, -2, -1, -1, 1, 1, 2, 2, -2, -2, 2, 2, -3, 0, 0, 3, -3, -3, -1, -1, 1, 1, 3, 3, -3, -3, -2, -2, 2, 2, 3, 3, -4, 0, 0, 4, -4, -4, -1, -1, 1, 1, 4, 4, -3, -3, 3, 3, -4, -4, -2, -2, 2, 2, 4, 4, -5, -4, -4, -3, -3, 0, 0, 3, 3, 4, 4, 5, -5, -5, -1, -1, 1, 1, 5, 5, -5, -5, -2, -2, 2, 2, 5, 5, -4, -4, 4, 4, -5, -5, -3, -3, 3, 3, 5, 5, -6, 0, 0, 6, -6, -6, -1, -1, 1, 1, 6, 6, -6, -6, -2, -2, 2, 2, 6, 6, -5, -5, -4, -4, 4, 4, 5, 5, -6, -6, -3, -3, 3, 3, 6, 6, -7, 0, 0, 7, -7, -7, -5, -5, -1, -1, 1, 1, 5, 5, 7, 7, -6, -6, -4, -4, 4, 4, 6, 6, -7, -7, -2, -2, 2, 2, 7, 7, -7, -7, -3, -3, 3, 3, 7, 7, -6, -6, -5, -5, 5, 5, 6, 6, -8, 0, 0, 8, -8, -8, -7, -7, -4, -4, -1, -1, 1, 1, 4, 4, 7, 7, 8, 8, -8, -8, -2, -2, 2, 2, 8, 8, -6, -6, 6, 6, -8, -8, -3, -3, 3, 3, 8, 8, -7, -7, -5, -5, 5, 5, 7, 7, -8, -8, -4, -4, 4, 4, 8, 8, -9, 0, 0, 9, -9, -9, -1, -1, 1, 1, 9, 9, -9, -9, -7, -7, -6, -6, -2, -2, 2, 2, 6, 6, 7, 7, 9, 9, -8, -8, -5, -5, 5, 5, 8, 8, -9, -9, -3, -3, 3, 3, 9, 9, -9, -9, -4, -4, 4, 4, 9, 9, -7, -7, 7, 7, -10, -8, -8, -6, -6, 0, 0, 6, 6, 8, 8, 10}, {0, -1, 0, 0, 1, -1, -1, 1, 1, -2, 0, 0, 2, -2, -2, -1, -1, 1, 1, 2, 2, -2, -2, 2, 2, -3, 0, 0, 3, -3, -3, -1, -1, 1, 1, 3, 3, -3, -3, -2, -2, 2, 2, 3, 3, -4, 0, 0, 4, -4, -4, -1, -1, 1, 1, 4, 4, -3, -3, 3, 3, -4, -4, -2, -2, 2, 2, 4, 4, -5, -4, -4, -3, -3, 0, 0, 3, 3, 4, 4, 5, -5, -5, -1, -1, 1, 1, 5, 5, -5, -5, -2, -2, 2, 2, 5, 5, -4, -4, 4, 4, -5, -5, -3, -3, 3, 3, 5, 5, -6, 0, 0, 6, -6, -6, -1, -1, 1, 1, 6, 6, -6, -6, -2, -2, 2, 2, 6, 6, -5, -5, -4, -4, 4, 4, 5, 5, -6, -6, -3, -3, 3, 3, 6, 6, -7, 0, 0, 7, -7, -7, -5, -5, -1, -1, 1, 1, 5, 5, 7, 7, -6, -6, -4, -4, 4, 4, 6, 6, -7, -7, -2, -2, 2, 2, 7, 7, -7, -7, -3, -3, 3, 3, 7, 7, -6, -6, -5, -5, 5, 5, 6, 6, -8, 0, 0, 8, -8, -8, -7, -7, -4, -4, -1, -1, 1, 1, 4, 4, 7, 7, 8, 8, -8, -8, -2, -2, 2, 2, 8, 8, -6, -6, 6, 6, -8, -8, -3, -3, 3, 3, 8, 8, -7, -7, -5, -5, 5, 5, 7, 7, -8, -8, -4, -4, 4, 4, 8, 8, -9, 0, 0, 9, -9, -9, -1, -1, 1, 1, 9, 9, -9, -9, -7, -7, -6, -6, -2, -2, 2, 2, 6, 6, 7, 7, 9, 9, -8, -8, -5, -5, 5, 5, 8, 8, -9, -9, -3, -3, 3, 3, 9, 9, -9, -9, -4, -4, 4, 4, 9, 9, -7, -7, 7, 7, -10, -8, -8, -6, -6, 0, 0, 6, 6, 8, 8, 10}, {0, -1, 0, 0, 1, -1, -1, 1, 1, -2, 0, 0, 2, -2, -2, -1, -1, 1, 1, 2, 2, -2, -2, 2, 2, -3, 0, 0, 3, -3, -3, -1, -1, 1, 1, 3, 3, -3, -3, -2, -2, 2, 2, 3, 3, -4, 0, 0, 4, -4, -4, -1, -1, 1, 1, 4, 4, -3, -3, 3, 3, -4, -4, -2, -2, 2, 2, 4, 4, -5, -4, -4, -3, -3, 0, 0, 3, 3, 4, 4, 5, -5, -5, -1, -1, 1, 1, 5, 5, -5, -5, -2, -2, 2, 2, 5, 5, -4, -4, 4, 4, -5, -5, -3, -3, 3, 3, 5, 5, -6, 0, 0, 6, -6, -6, -1, -1, 1, 1, 6, 6, -6, -6, -2, -2, 2, 2, 6, 6, -5, -5, -4, -4, 4, 4, 5, 5, -6, -6, -3, -3, 3, 3, 6, 6, -7, 0, 0, 7, -7, -7, -5, -5, -1, -1, 1, 1, 5, 5, 7, 7, -6, -6, -4, -4, 4, 4, 6, 6, -7, -7, -2, -2, 2, 2, 7, 7, -7, -7, -3, -3, 3, 3, 7, 7, -6, -6, -5, -5, 5, 5, 6, 6, -8, 0, 0, 8, -8, -8, -7, -7, -4, -4, -1, -1, 1, 1, 4, 4, 7, 7, 8, 8, -8, -8, -2, -2, 2, 2, 8, 8, -6, -6, 6, 6, -8, -8, -3, -3, 3, 3, 8, 8, -7, -7, -5, -5, 5, 5, 7, 7, -8, -8, -4, -4, 4, 4, 8, 8, -9, 0, 0, 9, -9, -9, -1, -1, 1, 1, 9, 9, -9, -9, -7, -7, -6, -6, -2, -2, 2, 2, 6, 6, 7, 7, 9, 9, -8, -8, -5, -5, 5, 5, 8, 8, -9, -9, -3, -3, 3, 3, 9, 9, -9, -9, -4, -4, 4, 4, 9, 9, -7, -7, 7, 7, -10, -8, -8, -6, -6, 0, 0, 6, 6, 8, 8, 10}};
      S.sensorDyByDirection = new int[][] {{0, 0, -1, 1, 0, -1, 1, -1, 1, 0, -2, 2, 0, -1, 1, -2, 2, -2, 2, -1, 1, -2, 2, -2, 2, 0, -3, 3, 0, -1, 1, -3, 3, -3, 3, -1, 1, -2, 2, -3, 3, -3, 3, -2, 2, 0, -4, 4, 0, -1, 1, -4, 4, -4, 4, -1, 1, -3, 3, -3, 3, -2, 2, -4, 4, -4, 4, -2, 2, 0, -3, 3, -4, 4, -5, 5, -4, 4, -3, 3, 0, -1, 1, -5, 5, -5, 5, -1, 1, -2, 2, -5, 5, -5, 5, -2, 2, -4, 4, -4, 4, -3, 3, -5, 5, -5, 5, -3, 3, 0, -6, 6, 0, -1, 1, -6, 6, -6, 6, -1, 1, -2, 2, -6, 6, -6, 6, -2, 2, -4, 4, -5, 5, -5, 5, -4, 4, -3, 3, -6, 6, -6, 6, -3, 3, 0, -7, 7, 0, -1, 1, -5, 5, -7, 7, -7, 7, -5, 5, -1, 1, -4, 4, -6, 6, -6, 6, -4, 4, -2, 2, -7, 7, -7, 7, -2, 2, -3, 3, -7, 7, -7, 7, -3, 3, -5, 5, -6, 6, -6, 6, -5, 5, 0, -8, 8, 0, -1, 1, -4, 4, -7, 7, -8, 8, -8, 8, -7, 7, -4, 4, -1, 1, -2, 2, -8, 8, -8, 8, -2, 2, -6, 6, -6, 6, -3, 3, -8, 8, -8, 8, -3, 3, -5, 5, -7, 7, -7, 7, -5, 5, -4, 4, -8, 8, -8, 8, -4, 4, 0, -9, 9, 0, -1, 1, -9, 9, -9, 9, -1, 1, -2, 2, -6, 6, -7, 7, -9, 9, -9, 9, -7, 7, -6, 6, -2, 2, -5, 5, -8, 8, -8, 8, -5, 5, -3, 3, -9, 9, -9, 9, -3, 3, -4, 4, -9, 9, -9, 9, -4, 4, -7, 7, -7, 7, 0, -6, 6, -8, 8, -10, 10, -8, 8, -6, 6, 0}, {0, 0, -1, 1, 0, -1, 1, -1, 1, 0, -2, 2, 0, -1, 1, -2, 2, -2, 2, -1, 1, -2, 2, -2, 2, 0, -3, 3, 0, -1, 1, -3, 3, -3, 3, -1, 1, -2, 2, -3, 3, -3, 3, -2, 2, 0, -4, 4, 0, -1, 1, -4, 4, -4, 4, -1, 1, -3, 3, -3, 3, -2, 2, -4, 4, -4, 4, -2, 2, 0, -3, 3, -4, 4, -5, 5, -4, 4, -3, 3, 0, -1, 1, -5, 5, -5, 5, -1, 1, -2, 2, -5, 5, -5, 5, -2, 2, -4, 4, -4, 4, -3, 3, -5, 5, -5, 5, -3, 3, 0, -6, 6, 0, -1, 1, -6, 6, -6, 6, -1, 1, -2, 2, -6, 6, -6, 6, -2, 2, -4, 4, -5, 5, -5, 5, -4, 4, -3, 3, -6, 6, -6, 6, -3, 3, 0, -7, 7, 0, -1, 1, -5, 5, -7, 7, -7, 7, -5, 5, -1, 1, -4, 4, -6, 6, -6, 6, -4, 4, -2, 2, -7, 7, -7, 7, -2, 2, -3, 3, -7, 7, -7, 7, -3, 3, -5, 5, -6, 6, -6, 6, -5, 5, 0, -8, 8, 0, -1, 1, -4, 4, -7, 7, -8, 8, -8, 8, -7, 7, -4, 4, -1, 1, -2, 2, -8, 8, -8, 8, -2, 2, -6, 6, -6, 6, -3, 3, -8, 8, -8, 8, -3, 3, -5, 5, -7, 7, -7, 7, -5, 5, -4, 4, -8, 8, -8, 8, -4, 4, 0, -9, 9, 0, -1, 1, -9, 9, -9, 9, -1, 1, -2, 2, -6, 6, -7, 7, -9, 9, -9, 9, -7, 7, -6, 6, -2, 2, -5, 5, -8, 8, -8, 8, -5, 5, -3, 3, -9, 9, -9, 9, -3, 3, -4, 4, -9, 9, -9, 9, -4, 4, -7, 7, -7, 7, 0, -6, 6, -8, 8, -10, 10, -8, 8, -6, 6, 0}, {0, 0, -1, 1, 0, -1, 1, -1, 1, 0, -2, 2, 0, -1, 1, -2, 2, -2, 2, -1, 1, -2, 2, -2, 2, 0, -3, 3, 0, -1, 1, -3, 3, -3, 3, -1, 1, -2, 2, -3, 3, -3, 3, -2, 2, 0, -4, 4, 0, -1, 1, -4, 4, -4, 4, -1, 1, -3, 3, -3, 3, -2, 2, -4, 4, -4, 4, -2, 2, 0, -3, 3, -4, 4, -5, 5, -4, 4, -3, 3, 0, -1, 1, -5, 5, -5, 5, -1, 1, -2, 2, -5, 5, -5, 5, -2, 2, -4, 4, -4, 4, -3, 3, -5, 5, -5, 5, -3, 3, 0, -6, 6, 0, -1, 1, -6, 6, -6, 6, -1, 1, -2, 2, -6, 6, -6, 6, -2, 2, -4, 4, -5, 5, -5, 5, -4, 4, -3, 3, -6, 6, -6, 6, -3, 3, 0, -7, 7, 0, -1, 1, -5, 5, -7, 7, -7, 7, -5, 5, -1, 1, -4, 4, -6, 6, -6, 6, -4, 4, -2, 2, -7, 7, -7, 7, -2, 2, -3, 3, -7, 7, -7, 7, -3, 3, -5, 5, -6, 6, -6, 6, -5, 5, 0, -8, 8, 0, -1, 1, -4, 4, -7, 7, -8, 8, -8, 8, -7, 7, -4, 4, -1, 1, -2, 2, -8, 8, -8, 8, -2, 2, -6, 6, -6, 6, -3, 3, -8, 8, -8, 8, -3, 3, -5, 5, -7, 7, -7, 7, -5, 5, -4, 4, -8, 8, -8, 8, -4, 4, 0, -9, 9, 0, -1, 1, -9, 9, -9, 9, -1, 1, -2, 2, -6, 6, -7, 7, -9, 9, -9, 9, -7, 7, -6, 6, -2, 2, -5, 5, -8, 8, -8, 8, -5, 5, -3, 3, -9, 9, -9, 9, -3, 3, -4, 4, -9, 9, -9, 9, -4, 4, -7, 7, -7, 7, 0, -6, 6, -8, 8, -10, 10, -8, 8, -6, 6, 0}, {0, 0, -1, 1, 0, -1, 1, -1, 1, 0, -2, 2, 0, -1, 1, -2, 2, -2, 2, -1, 1, -2, 2, -2, 2, 0, -3, 3, 0, -1, 1, -3, 3, -3, 3, -1, 1, -2, 2, -3, 3, -3, 3, -2, 2, 0, -4, 4, 0, -1, 1, -4, 4, -4, 4, -1, 1, -3, 3, -3, 3, -2, 2, -4, 4, -4, 4, -2, 2, 0, -3, 3, -4, 4, -5, 5, -4, 4, -3, 3, 0, -1, 1, -5, 5, -5, 5, -1, 1, -2, 2, -5, 5, -5, 5, -2, 2, -4, 4, -4, 4, -3, 3, -5, 5, -5, 5, -3, 3, 0, -6, 6, 0, -1, 1, -6, 6, -6, 6, -1, 1, -2, 2, -6, 6, -6, 6, -2, 2, -4, 4, -5, 5, -5, 5, -4, 4, -3, 3, -6, 6, -6, 6, -3, 3, 0, -7, 7, 0, -1, 1, -5, 5, -7, 7, -7, 7, -5, 5, -1, 1, -4, 4, -6, 6, -6, 6, -4, 4, -2, 2, -7, 7, -7, 7, -2, 2, -3, 3, -7, 7, -7, 7, -3, 3, -5, 5, -6, 6, -6, 6, -5, 5, 0, -8, 8, 0, -1, 1, -4, 4, -7, 7, -8, 8, -8, 8, -7, 7, -4, 4, -1, 1, -2, 2, -8, 8, -8, 8, -2, 2, -6, 6, -6, 6, -3, 3, -8, 8, -8, 8, -3, 3, -5, 5, -7, 7, -7, 7, -5, 5, -4, 4, -8, 8, -8, 8, -4, 4, 0, -9, 9, 0, -1, 1, -9, 9, -9, 9, -1, 1, -2, 2, -6, 6, -7, 7, -9, 9, -9, 9, -7, 7, -6, 6, -2, 2, -5, 5, -8, 8, -8, 8, -5, 5, -3, 3, -9, 9, -9, 9, -3, 3, -4, 4, -9, 9, -9, 9, -4, 4, -7, 7, -7, 7, 0, -6, 6, -8, 8, -10, 10, -8, 8, -6, 6, 0}, {0, 0, -1, 1, 0, -1, 1, -1, 1, 0, -2, 2, 0, -1, 1, -2, 2, -2, 2, -1, 1, -2, 2, -2, 2, 0, -3, 3, 0, -1, 1, -3, 3, -3, 3, -1, 1, -2, 2, -3, 3, -3, 3, -2, 2, 0, -4, 4, 0, -1, 1, -4, 4, -4, 4, -1, 1, -3, 3, -3, 3, -2, 2, -4, 4, -4, 4, -2, 2, 0, -3, 3, -4, 4, -5, 5, -4, 4, -3, 3, 0, -1, 1, -5, 5, -5, 5, -1, 1, -2, 2, -5, 5, -5, 5, -2, 2, -4, 4, -4, 4, -3, 3, -5, 5, -5, 5, -3, 3, 0, -6, 6, 0, -1, 1, -6, 6, -6, 6, -1, 1, -2, 2, -6, 6, -6, 6, -2, 2, -4, 4, -5, 5, -5, 5, -4, 4, -3, 3, -6, 6, -6, 6, -3, 3, 0, -7, 7, 0, -1, 1, -5, 5, -7, 7, -7, 7, -5, 5, -1, 1, -4, 4, -6, 6, -6, 6, -4, 4, -2, 2, -7, 7, -7, 7, -2, 2, -3, 3, -7, 7, -7, 7, -3, 3, -5, 5, -6, 6, -6, 6, -5, 5, 0, -8, 8, 0, -1, 1, -4, 4, -7, 7, -8, 8, -8, 8, -7, 7, -4, 4, -1, 1, -2, 2, -8, 8, -8, 8, -2, 2, -6, 6, -6, 6, -3, 3, -8, 8, -8, 8, -3, 3, -5, 5, -7, 7, -7, 7, -5, 5, -4, 4, -8, 8, -8, 8, -4, 4, 0, -9, 9, 0, -1, 1, -9, 9, -9, 9, -1, 1, -2, 2, -6, 6, -7, 7, -9, 9, -9, 9, -7, 7, -6, 6, -2, 2, -5, 5, -8, 8, -8, 8, -5, 5, -3, 3, -9, 9, -9, 9, -3, 3, -4, 4, -9, 9, -9, 9, -4, 4, -7, 7, -7, 7, 0, -6, 6, -8, 8, -10, 10, -8, 8, -6, 6, 0}, {0, 0, -1, 1, 0, -1, 1, -1, 1, 0, -2, 2, 0, -1, 1, -2, 2, -2, 2, -1, 1, -2, 2, -2, 2, 0, -3, 3, 0, -1, 1, -3, 3, -3, 3, -1, 1, -2, 2, -3, 3, -3, 3, -2, 2, 0, -4, 4, 0, -1, 1, -4, 4, -4, 4, -1, 1, -3, 3, -3, 3, -2, 2, -4, 4, -4, 4, -2, 2, 0, -3, 3, -4, 4, -5, 5, -4, 4, -3, 3, 0, -1, 1, -5, 5, -5, 5, -1, 1, -2, 2, -5, 5, -5, 5, -2, 2, -4, 4, -4, 4, -3, 3, -5, 5, -5, 5, -3, 3, 0, -6, 6, 0, -1, 1, -6, 6, -6, 6, -1, 1, -2, 2, -6, 6, -6, 6, -2, 2, -4, 4, -5, 5, -5, 5, -4, 4, -3, 3, -6, 6, -6, 6, -3, 3, 0, -7, 7, 0, -1, 1, -5, 5, -7, 7, -7, 7, -5, 5, -1, 1, -4, 4, -6, 6, -6, 6, -4, 4, -2, 2, -7, 7, -7, 7, -2, 2, -3, 3, -7, 7, -7, 7, -3, 3, -5, 5, -6, 6, -6, 6, -5, 5, 0, -8, 8, 0, -1, 1, -4, 4, -7, 7, -8, 8, -8, 8, -7, 7, -4, 4, -1, 1, -2, 2, -8, 8, -8, 8, -2, 2, -6, 6, -6, 6, -3, 3, -8, 8, -8, 8, -3, 3, -5, 5, -7, 7, -7, 7, -5, 5, -4, 4, -8, 8, -8, 8, -4, 4, 0, -9, 9, 0, -1, 1, -9, 9, -9, 9, -1, 1, -2, 2, -6, 6, -7, 7, -9, 9, -9, 9, -7, 7, -6, 6, -2, 2, -5, 5, -8, 8, -8, 8, -5, 5, -3, 3, -9, 9, -9, 9, -3, 3, -4, 4, -9, 9, -9, 9, -4, 4, -7, 7, -7, 7, 0, -6, 6, -8, 8, -10, 10, -8, 8, -6, 6, 0}, {0, 0, -1, 1, 0, -1, 1, -1, 1, 0, -2, 2, 0, -1, 1, -2, 2, -2, 2, -1, 1, -2, 2, -2, 2, 0, -3, 3, 0, -1, 1, -3, 3, -3, 3, -1, 1, -2, 2, -3, 3, -3, 3, -2, 2, 0, -4, 4, 0, -1, 1, -4, 4, -4, 4, -1, 1, -3, 3, -3, 3, -2, 2, -4, 4, -4, 4, -2, 2, 0, -3, 3, -4, 4, -5, 5, -4, 4, -3, 3, 0, -1, 1, -5, 5, -5, 5, -1, 1, -2, 2, -5, 5, -5, 5, -2, 2, -4, 4, -4, 4, -3, 3, -5, 5, -5, 5, -3, 3, 0, -6, 6, 0, -1, 1, -6, 6, -6, 6, -1, 1, -2, 2, -6, 6, -6, 6, -2, 2, -4, 4, -5, 5, -5, 5, -4, 4, -3, 3, -6, 6, -6, 6, -3, 3, 0, -7, 7, 0, -1, 1, -5, 5, -7, 7, -7, 7, -5, 5, -1, 1, -4, 4, -6, 6, -6, 6, -4, 4, -2, 2, -7, 7, -7, 7, -2, 2, -3, 3, -7, 7, -7, 7, -3, 3, -5, 5, -6, 6, -6, 6, -5, 5, 0, -8, 8, 0, -1, 1, -4, 4, -7, 7, -8, 8, -8, 8, -7, 7, -4, 4, -1, 1, -2, 2, -8, 8, -8, 8, -2, 2, -6, 6, -6, 6, -3, 3, -8, 8, -8, 8, -3, 3, -5, 5, -7, 7, -7, 7, -5, 5, -4, 4, -8, 8, -8, 8, -4, 4, 0, -9, 9, 0, -1, 1, -9, 9, -9, 9, -1, 1, -2, 2, -6, 6, -7, 7, -9, 9, -9, 9, -7, 7, -6, 6, -2, 2, -5, 5, -8, 8, -8, 8, -5, 5, -3, 3, -9, 9, -9, 9, -3, 3, -4, 4, -9, 9, -9, 9, -4, 4, -7, 7, -7, 7, 0, -6, 6, -8, 8, -10, 10, -8, 8, -6, 6, 0}, {0, 0, -1, 1, 0, -1, 1, -1, 1, 0, -2, 2, 0, -1, 1, -2, 2, -2, 2, -1, 1, -2, 2, -2, 2, 0, -3, 3, 0, -1, 1, -3, 3, -3, 3, -1, 1, -2, 2, -3, 3, -3, 3, -2, 2, 0, -4, 4, 0, -1, 1, -4, 4, -4, 4, -1, 1, -3, 3, -3, 3, -2, 2, -4, 4, -4, 4, -2, 2, 0, -3, 3, -4, 4, -5, 5, -4, 4, -3, 3, 0, -1, 1, -5, 5, -5, 5, -1, 1, -2, 2, -5, 5, -5, 5, -2, 2, -4, 4, -4, 4, -3, 3, -5, 5, -5, 5, -3, 3, 0, -6, 6, 0, -1, 1, -6, 6, -6, 6, -1, 1, -2, 2, -6, 6, -6, 6, -2, 2, -4, 4, -5, 5, -5, 5, -4, 4, -3, 3, -6, 6, -6, 6, -3, 3, 0, -7, 7, 0, -1, 1, -5, 5, -7, 7, -7, 7, -5, 5, -1, 1, -4, 4, -6, 6, -6, 6, -4, 4, -2, 2, -7, 7, -7, 7, -2, 2, -3, 3, -7, 7, -7, 7, -3, 3, -5, 5, -6, 6, -6, 6, -5, 5, 0, -8, 8, 0, -1, 1, -4, 4, -7, 7, -8, 8, -8, 8, -7, 7, -4, 4, -1, 1, -2, 2, -8, 8, -8, 8, -2, 2, -6, 6, -6, 6, -3, 3, -8, 8, -8, 8, -3, 3, -5, 5, -7, 7, -7, 7, -5, 5, -4, 4, -8, 8, -8, 8, -4, 4, 0, -9, 9, 0, -1, 1, -9, 9, -9, 9, -1, 1, -2, 2, -6, 6, -7, 7, -9, 9, -9, 9, -7, 7, -6, 6, -2, 2, -5, 5, -8, 8, -8, 8, -5, 5, -3, 3, -9, 9, -9, 9, -3, 3, -4, 4, -9, 9, -9, 9, -4, 4, -7, 7, -7, 7, 0, -6, 6, -8, 8, -10, 10, -8, 8, -6, 6, 0}};
      S.sensorEdgeDxByDirection = new int [][] {{-1, 1, -2, 2, -5, 5, -3, 3, -9, -4, 4, 9, -7, 7, -10, -8, -6, 0, 6, 8, 10}, {4, 8, 0, 9, 1, 9, -2, 2, 6, 7, 9, 9, 5, 8, -3, 3, 9, 9, -4, 4, 9, 9, 7, -6, 0, 6, 8, 8, 10}, {9, 9, 9, 9, 8, 8, 9, 9, 4, 4, 9, 9, 7, 7, 0, 0, 6, 6, 8, 8, 10}, {4, 8, 0, 9, 1, 9, -2, 2, 6, 7, 9, 9, 5, 8, -3, 3, 9, 9, -4, 4, 9, 9, 7, -6, 0, 6, 8, 8, 10}, {-1, 1, -2, 2, -5, 5, -3, 3, -9, -4, 4, 9, -7, 7, -10, -8, -6, 0, 6, 8, 10}, {-8, -4, -9, 0, -9, -1, -9, -9, -7, -6, -2, 2, -8, -5, -9, -9, -3, 3, -9, -9, -4, 4, -7, -10, -8, -8, -6, 0, 6}, {-9, -9, -9, -9, -8, -8, -9, -9, -9, -9, -4, -4, -7, -7, -10, -8, -8, -6, -6, 0, 0}, {-8, -4, -9, 0, -9, -1, -9, -9, -7, -6, -2, 2, -8, -5, -9, -9, -3, 3, -9, -9, -4, 4, -7, -10, -8, -8, -6, 0, 6}};
      S.sensorEdgeDyByDirection = new int [][] {{-9, -9, -9, -9, -8, -8, -9, -9, -4, -9, -9, -4, -7, -7, 0, -6, -8, -10, -8, -6, 0}, {-8, -4, -9, 0, -9, -1, -9, -9, -7, -6, -2, 2, -8, -5, -9, -9, -3, 3, -9, -9, -4, 4, -7, -8, -10, -8, -6, 6, 0}, {-1, 1, -2, 2, -5, 5, -3, 3, -9, 9, -4, 4, -7, 7, -10, 10, -8, 8, -6, 6, 0}, {8, 4, 9, 0, 9, 1, 9, 9, 7, 6, -2, 2, 8, 5, 9, 9, -3, 3, 9, 9, -4, 4, 7, 8, 10, 8, -6, 6, 0}, {9, 9, 9, 9, 8, 8, 9, 9, 4, 9, 9, 4, 7, 7, 0, 6, 8, 10, 8, 6, 0}, {4, 8, 0, 9, 1, 9, -2, 2, 6, 7, 9, 9, 5, 8, -3, 3, 9, 9, -4, 4, 9, 9, 7, 0, -6, 6, 8, 10, 8}, {-1, 1, -2, 2, -5, 5, -3, 3, -4, 4, -9, 9, -7, 7, 0, -6, 6, -8, 8, -10, 10}, {-4, -8, 0, -9, -1, -9, -2, 2, -6, -7, -9, -9, -5, -8, -3, 3, -9, -9, -4, 4, -9, -9, -7, 0, -6, 6, -8, -10, -8}};
      break;
    case ComponentUtil.TELESCOPE_INT:
      S.sensorDxByDirection = new int[][] {{0, 0, 0, 0, -1, 1, 0, -1, 1, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, -3, 3, 0, -1, 1, -2, 2, -3, 3, 0, -1, 1, -2, 2, -3, 3, -4, 4, 0, -1, 1, -2, 2, -3, 3, -4, 4, 0}, {0, 1, 1, 2, 2, 2, 3, 3, 2, 4, 3, 4, 4, 3, 5, 4, 5, 3, 6, 5, 4, 6, 3, 7, 5, 6, 4, 7, 6, 5, 7, 4, 8, 6, 7, 5, 8, 4, 9, 7, 6, 8, 5, 9, 7, 8, 6, 9, 5, 10, 8, 7, 9, 6, 10}, {0, 1, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 12}, {0, 1, 1, 2, 2, 2, 3, 3, 2, 4, 3, 4, 4, 3, 5, 4, 5, 3, 6, 5, 4, 6, 3, 7, 5, 6, 4, 7, 6, 5, 7, 4, 8, 6, 7, 5, 8, 4, 9, 7, 6, 8, 5, 9, 7, 8, 6, 9, 5, 10, 8, 7, 9, 6, 10}, {0, 0, 0, 0, -1, 1, 0, -1, 1, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, -3, 3, 0, -1, 1, -2, 2, -3, 3, 0, -1, 1, -2, 2, -3, 3, -4, 4, 0, -1, 1, -2, 2, -3, 3, -4, 4, 0}, {0, -1, -2, -1, -2, -3, -2, -3, -4, -2, -4, -3, -4, -5, -3, -5, -4, -6, -3, -5, -6, -4, -7, -3, -6, -5, -7, -4, -6, -7, -5, -8, -4, -7, -6, -8, -5, -9, -4, -7, -8, -6, -9, -5, -8, -7, -9, -6, -10, -5, -8, -9, -7, -10, -6}, {0, -1, -2, -3, -3, -3, -4, -4, -4, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -8, -8, -8, -8, -8, -8, -8, -9, -9, -9, -9, -9, -9, -9, -10, -10, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -11, -11, -12}, {0, -1, -2, -1, -2, -3, -2, -3, -4, -2, -4, -3, -4, -5, -3, -5, -4, -6, -3, -5, -6, -4, -7, -3, -6, -5, -7, -4, -6, -7, -5, -8, -4, -7, -6, -8, -5, -9, -4, -7, -8, -6, -9, -5, -8, -7, -9, -6, -10, -5, -8, -9, -7, -10, -6}};
      S.sensorDyByDirection = new int[][] {{0, -1, -2, -3, -3, -3, -4, -4, -4, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -8, -8, -8, -8, -8, -8, -8, -9, -9, -9, -9, -9, -9, -9, -10, -10, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -11, -11, -12}, {0, -1, -2, -1, -2, -3, -2, -3, -4, -2, -4, -3, -4, -5, -3, -5, -4, -6, -3, -5, -6, -4, -7, -3, -6, -5, -7, -4, -6, -7, -5, -8, -4, -7, -6, -8, -5, -9, -4, -7, -8, -6, -9, -5, -8, -7, -9, -6, -10, -5, -8, -9, -7, -10, -6}, {0, 0, 0, 0, -1, 1, 0, -1, 1, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, -3, 3, 0, -1, 1, -2, 2, -3, 3, 0, -1, 1, -2, 2, -3, 3, -4, 4, 0, -1, 1, -2, 2, -3, 3, -4, 4, 0}, {0, 1, 2, 1, 2, 3, 2, 3, 4, 2, 4, 3, 4, 5, 3, 5, 4, 6, 3, 5, 6, 4, 7, 3, 6, 5, 7, 4, 6, 7, 5, 8, 4, 7, 6, 8, 5, 9, 4, 7, 8, 6, 9, 5, 8, 7, 9, 6, 10, 5, 8, 9, 7, 10, 6}, {0, 1, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 12}, {0, 1, 1, 2, 2, 2, 3, 3, 2, 4, 3, 4, 4, 3, 5, 4, 5, 3, 6, 5, 4, 6, 3, 7, 5, 6, 4, 7, 6, 5, 7, 4, 8, 6, 7, 5, 8, 4, 9, 7, 6, 8, 5, 9, 7, 8, 6, 9, 5, 10, 8, 7, 9, 6, 10}, {0, 0, 0, 0, -1, 1, 0, -1, 1, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, 0, -1, 1, -2, 2, -3, 3, 0, -1, 1, -2, 2, -3, 3, 0, -1, 1, -2, 2, -3, 3, -4, 4, 0, -1, 1, -2, 2, -3, 3, -4, 4, 0}, {0, -1, -1, -2, -2, -2, -3, -3, -2, -4, -3, -4, -4, -3, -5, -4, -5, -3, -6, -5, -4, -6, -3, -7, -5, -6, -4, -7, -6, -5, -7, -4, -8, -6, -7, -5, -8, -4, -9, -7, -6, -8, -5, -9, -7, -8, -6, -9, -5, -10, -8, -7, -9, -6, -10}};
      S.sensorEdgeDxByDirection = new int [][] {{-1, 1, -2, 2, -3, 3, -4, 4, 0}, {7, 8, 6, 9, 5, 10, 8, 7, 9, 6, 10}, {11, 11, 11, 11, 11, 11, 11, 11, 12}, {7, 8, 6, 9, 5, 10, 8, 7, 9, 6, 10}, {-1, 1, -2, 2, -3, 3, -4, 4, 0}, {-8, -7, -9, -6, -10, -5, -8, -9, -7, -10, -6}, {-11, -11, -11, -11, -11, -11, -11, -11, -12}, {-8, -7, -9, -6, -10, -5, -8, -9, -7, -10, -6}};
      S.sensorEdgeDyByDirection = new int [][] {{-11, -11, -11, -11, -11, -11, -11, -11, -12}, {-8, -7, -9, -6, -10, -5, -8, -9, -7, -10, -6}, {-1, 1, -2, 2, -3, 3, -4, 4, 0}, {8, 7, 9, 6, 10, 5, 8, 9, 7, 10, 6}, {11, 11, 11, 11, 11, 11, 11, 11, 12}, {7, 8, 6, 9, 5, 10, 8, 7, 9, 6, 10}, {-1, 1, -2, 2, -3, 3, -4, 4, 0}, {-7, -8, -6, -9, -5, -10, -8, -7, -9, -6, -10}};
      break;
    case ComponentUtil.SIGHT_INT:
      S.sensorDxByDirection = new int[][] {{0, 0, -1, 1, 0, -1, 1, -2, 2, 0}, {0, 0, 1, 1, 0, 2, 1, 2, 2, 0, 3}, {0, 1, 1, 1, 2, 2, 2, 2, 2, 3}, {0, 0, 1, 1, 0, 2, 1, 2, 2, 0, 3}, {0, 0, -1, 1, 0, -1, 1, -2, 2, 0}, {0, -1, 0, -1, -2, 0, -2, -1, -2, -3, 0}, {0, -1, -1, -1, -2, -2, -2, -2, -2, -3}, {0, -1, 0, -1, -2, 0, -2, -1, -2, -3, 0}};
      S.sensorDyByDirection = new int[][] {{0, -1, -1, -1, -2, -2, -2, -2, -2, -3}, {0, -1, 0, -1, -2, 0, -2, -1, -2, -3, 0}, {0, 0, -1, 1, 0, -1, 1, -2, 2, 0}, {0, 1, 0, 1, 2, 0, 2, 1, 2, 3, 0}, {0, 1, 1, 1, 2, 2, 2, 2, 2, 3}, {0, 0, 1, 1, 0, 2, 1, 2, 2, 0, 3}, {0, 0, -1, 1, 0, -1, 1, -2, 2, 0}, {0, 0, -1, -1, 0, -2, -1, -2, -2, 0, -3}};
      S.sensorEdgeDxByDirection = new int [][] {{-1, 1, -2, 2, 0}, {0, 2, 1, 2, 2, 0, 3}, {2, 2, 2, 2, 3}, {0, 2, 1, 2, 2, 0, 3}, {-1, 1, -2, 2, 0}, {-2, 0, -2, -1, -2, -3, 0}, {-2, -2, -2, -2, -3}, {-2, 0, -2, -1, -2, -3, 0}};
      S.sensorEdgeDyByDirection = new int [][] {{-2, -2, -2, -2, -3}, {-2, 0, -2, -1, -2, -3, 0}, {-1, 1, -2, 2, 0}, {2, 0, 2, 1, 2, 3, 0}, {2, 2, 2, 2, 3}, {0, 2, 1, 2, 2, 0, 3}, {-1, 1, -2, 2, 0}, {0, -2, -1, -2, -2, 0, -3}};
      break;
    case ComponentUtil.RADAR_INT:
      S.sensorDxByDirection = new int[][] {{0, -1, 0, 1, -1, 1, -2, 0, 2, -2, -1, 1, 2, -2, 2, -3, 0, 3, -3, -1, 1, 3, -3, -2, 2, 3, -4, 0, 4, -4, -1, 1, 4, -3, 3, -4, -2, 2, 4, -5, -4, -3, 0, 3, 4, 5, -5, -1, 1, 5, -5, -2, 2, 5, -4, 4, -5, -3, 3, 5, -6, 0, 6}, {0, 0, 1, -1, 1, 1, 0, 2, -1, 1, 2, 2, -2, 2, 2, 0, 3, -1, 1, 3, 3, -2, 2, 3, 3, 0, 4, -1, 1, 4, 4, -3, 3, 3, -2, 2, 4, 4, -3, 0, 3, 4, 4, 5, -1, 1, 5, 5, -2, 2, 5, 5, -4, 4, 4, -3, 3, 5, 5, 0, 6}, {0, 0, 0, 1, 1, 1, 0, 0, 2, 1, 1, 2, 2, 2, 2, 0, 0, 3, 1, 1, 3, 3, 2, 2, 3, 3, 0, 0, 4, 1, 1, 4, 4, 3, 3, 2, 2, 4, 4, 0, 0, 3, 3, 4, 4, 5, 1, 1, 5, 5, 2, 2, 5, 5, 4, 4, 3, 3, 5, 5, 0, 0, 6}, {0, 0, 1, -1, 1, 1, 0, 2, -1, 1, 2, 2, -2, 2, 2, 0, 3, -1, 1, 3, 3, -2, 2, 3, 3, 0, 4, -1, 1, 4, 4, -3, 3, 3, -2, 2, 4, 4, -3, 0, 3, 4, 4, 5, -1, 1, 5, 5, -2, 2, 5, 5, -4, 4, 4, -3, 3, 5, 5, 0, 6}, {0, -1, 0, 1, -1, 1, -2, 0, 2, -2, -1, 1, 2, -2, 2, -3, 0, 3, -3, -1, 1, 3, -3, -2, 2, 3, -4, 0, 4, -4, -1, 1, 4, -3, 3, -4, -2, 2, 4, -5, -4, -3, 0, 3, 4, 5, -5, -1, 1, 5, -5, -2, 2, 5, -4, 4, -5, -3, 3, 5, -6, 0, 6}, {0, -1, 0, -1, -1, 1, -2, 0, -2, -2, -1, 1, -2, -2, 2, -3, 0, -3, -3, -1, 1, -3, -3, -2, 2, -4, 0, -4, -4, -1, 1, -3, -3, 3, -4, -4, -2, 2, -5, -4, -4, -3, 0, 3, -5, -5, -1, 1, -5, -5, -2, 2, -4, -4, 4, -5, -5, -3, 3, -6, 0}, {0, -1, 0, 0, -1, -1, -2, 0, 0, -2, -2, -1, -1, -2, -2, -3, 0, 0, -3, -3, -1, -1, -3, -3, -2, -2, -4, 0, 0, -4, -4, -1, -1, -3, -3, -4, -4, -2, -2, -5, -4, -4, -3, -3, 0, 0, -5, -5, -1, -1, -5, -5, -2, -2, -4, -4, -5, -5, -3, -3, -6, 0, 0}, {0, -1, 0, -1, -1, 1, -2, 0, -2, -2, -1, 1, -2, -2, 2, -3, 0, -3, -3, -1, 1, -3, -3, -2, 2, -4, 0, -4, -4, -1, 1, -3, -3, 3, -4, -4, -2, 2, -5, -4, -4, -3, 0, 3, -5, -5, -1, 1, -5, -5, -2, 2, -4, -4, 4, -5, -5, -3, 3, -6, 0}};
      S.sensorDyByDirection = new int[][] {{0, 0, -1, 0, -1, -1, 0, -2, 0, -1, -2, -2, -1, -2, -2, 0, -3, 0, -1, -3, -3, -1, -2, -3, -3, -2, 0, -4, 0, -1, -4, -4, -1, -3, -3, -2, -4, -4, -2, 0, -3, -4, -5, -4, -3, 0, -1, -5, -5, -1, -2, -5, -5, -2, -4, -4, -3, -5, -5, -3, 0, -6, 0}, {0, -1, 0, -1, -1, 1, -2, 0, -2, -2, -1, 1, -2, -2, 2, -3, 0, -3, -3, -1, 1, -3, -3, -2, 2, -4, 0, -4, -4, -1, 1, -3, -3, 3, -4, -4, -2, 2, -4, -5, -4, -3, 3, 0, -5, -5, -1, 1, -5, -5, -2, 2, -4, -4, 4, -5, -5, -3, 3, -6, 0}, {0, -1, 1, 0, -1, 1, -2, 2, 0, -2, 2, -1, 1, -2, 2, -3, 3, 0, -3, 3, -1, 1, -3, 3, -2, 2, -4, 4, 0, -4, 4, -1, 1, -3, 3, -4, 4, -2, 2, -5, 5, -4, 4, -3, 3, 0, -5, 5, -1, 1, -5, 5, -2, 2, -4, 4, -5, 5, -3, 3, -6, 6, 0}, {0, 1, 0, 1, -1, 1, 2, 0, 2, 2, -1, 1, 2, -2, 2, 3, 0, 3, 3, -1, 1, 3, 3, -2, 2, 4, 0, 4, 4, -1, 1, 3, -3, 3, 4, 4, -2, 2, 4, 5, 4, -3, 3, 0, 5, 5, -1, 1, 5, 5, -2, 2, 4, -4, 4, 5, 5, -3, 3, 6, 0}, {0, 0, 1, 0, 1, 1, 0, 2, 0, 1, 2, 2, 1, 2, 2, 0, 3, 0, 1, 3, 3, 1, 2, 3, 3, 2, 0, 4, 0, 1, 4, 4, 1, 3, 3, 2, 4, 4, 2, 0, 3, 4, 5, 4, 3, 0, 1, 5, 5, 1, 2, 5, 5, 2, 4, 4, 3, 5, 5, 3, 0, 6, 0}, {0, 0, 1, -1, 1, 1, 0, 2, -1, 1, 2, 2, -2, 2, 2, 0, 3, -1, 1, 3, 3, -2, 2, 3, 3, 0, 4, -1, 1, 4, 4, -3, 3, 3, -2, 2, 4, 4, 0, -3, 3, 4, 5, 4, -1, 1, 5, 5, -2, 2, 5, 5, -4, 4, 4, -3, 3, 5, 5, 0, 6}, {0, 0, -1, 1, -1, 1, 0, -2, 2, -1, 1, -2, 2, -2, 2, 0, -3, 3, -1, 1, -3, 3, -2, 2, -3, 3, 0, -4, 4, -1, 1, -4, 4, -3, 3, -2, 2, -4, 4, 0, -3, 3, -4, 4, -5, 5, -1, 1, -5, 5, -2, 2, -5, 5, -4, 4, -3, 3, -5, 5, 0, -6, 6}, {0, 0, -1, -1, 1, -1, 0, -2, -1, 1, -2, -2, -2, 2, -2, 0, -3, -1, 1, -3, -3, -2, 2, -3, -3, 0, -4, -1, 1, -4, -4, -3, 3, -3, -2, 2, -4, -4, 0, -3, 3, -4, -5, -4, -1, 1, -5, -5, -2, 2, -5, -5, -4, 4, -4, -3, 3, -5, -5, 0, -6}};
      S.sensorEdgeDxByDirection = new int [][] {{-1, 1, -2, 2, -4, 4, -5, -3, 3, 5, -6, 0, 6}, {0, 3, 4, 5, 1, 5, -2, 2, 5, 5, 4, -3, 3, 5, 5, 0, 6}, {5, 5, 5, 5, 4, 4, 3, 3, 5, 5, 0, 0, 6}, {0, 3, 4, 5, 1, 5, -2, 2, 5, 5, 4, -3, 3, 5, 5, 0, 6}, {-1, 1, -2, 2, -4, 4, -5, -3, 3, 5, -6, 0, 6}, {-5, -4, -3, 0, -5, -1, -5, -5, -2, 2, -4, -5, -5, -3, 3, -6, 0}, {-5, -5, -5, -5, -4, -4, -5, -5, -3, -3, -6, 0, 0}, {-5, -4, -3, 0, -5, -1, -5, -5, -2, 2, -4, -5, -5, -3, 3, -6, 0}};
      S.sensorEdgeDyByDirection = new int [][] {{-5, -5, -5, -5, -4, -4, -3, -5, -5, -3, 0, -6, 0}, {-5, -4, -3, 0, -5, -1, -5, -5, -2, 2, -4, -5, -5, -3, 3, -6, 0}, {-1, 1, -2, 2, -4, 4, -5, 5, -3, 3, -6, 6, 0}, {5, 4, 3, 0, 5, 1, 5, 5, -2, 2, 4, 5, 5, -3, 3, 6, 0}, {5, 5, 5, 5, 4, 4, 3, 5, 5, 3, 0, 6, 0}, {0, 3, 4, 5, 1, 5, -2, 2, 5, 5, 4, -3, 3, 5, 5, 0, 6}, {-1, 1, -2, 2, -4, 4, -3, 3, -5, 5, 0, -6, 6}, {0, -3, -4, -5, -1, -5, -2, 2, -5, -5, -4, -3, 3, -5, -5, 0, -6}};
      break;
    case ComponentUtil.BUILDING_SENSOR_INT:
      S.sensorDxByDirection = new int[][] {{0, -1, 0, 0, 1, -1, -1, 1, 1}, {0, -1, 0, 0, 1, -1, -1, 1, 1}, {0, -1, 0, 0, 1, -1, -1, 1, 1}, {0, -1, 0, 0, 1, -1, -1, 1, 1}, {0, -1, 0, 0, 1, -1, -1, 1, 1}, {0, -1, 0, 0, 1, -1, -1, 1, 1}, {0, -1, 0, 0, 1, -1, -1, 1, 1}, {0, -1, 0, 0, 1, -1, -1, 1, 1}};
      S.sensorDyByDirection = new int[][] {{0, 0, -1, 1, 0, -1, 1, -1, 1}, {0, 0, -1, 1, 0, -1, 1, -1, 1}, {0, 0, -1, 1, 0, -1, 1, -1, 1}, {0, 0, -1, 1, 0, -1, 1, -1, 1}, {0, 0, -1, 1, 0, -1, 1, -1, 1}, {0, 0, -1, 1, 0, -1, 1, -1, 1}, {0, 0, -1, 1, 0, -1, 1, -1, 1}, {0, 0, -1, 1, 0, -1, 1, -1, 1}};
      S.sensorEdgeDxByDirection = new int [][] {{0, -1, 1}, {0, 1, -1, 1, 1}, {1, 1, 1}, {0, 1, -1, 1, 1}, {0, -1, 1}, {-1, 0, -1, -1, 1}, {-1, -1, -1}, {-1, 0, -1, -1, 1}};
      S.sensorEdgeDyByDirection = new int [][] {{-1, -1, -1}, {-1, 0, -1, -1, 1}, {0, -1, 1}, {1, 0, 1, -1, 1}, {1, 1, 1}, {0, 1, -1, 1, 1}, {0, -1, 1}, {0, -1, -1, 1, -1}};
      break;
    }
  }
}
