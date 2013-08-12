package team050.util;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class Utilities {

  public static final Direction[] ALL_DIR = new Direction[] { Direction.WEST, Direction.NORTH_WEST, Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST }; // Direction.values();

  public static final Random rdm = new Random();

  public static final Direction randomCompassDirection() {
    return ALL_DIR[rdm.nextInt(8)];
  }

  public static final Direction rotate(Direction d, int amount) {
    return Direction.values()[(d.ordinal() + amount + 8) % 8];
  }

  public static final int[][] directionToOffset = new int[][] { { 0, -1 }, { 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 }, { -1, -1 }, { 0, 0 }, { 0, 0 } };

  public static final MapLocation add(MapLocation loc, Direction d, int times) {
    int[] off = directionToOffset[d.ordinal()];
    return new MapLocation(loc.x + off[0] * times, loc.y + off[1] * times);
  }

  public static final MapLocation add(MapLocation loc, int x, int y) {
    return new MapLocation(loc.x + x, loc.y + y);
  }
}
