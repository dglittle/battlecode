package team050.blocks;

import team050.core.S;
import team050.core.U;
import team050.core.X;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.TerrainTile;

public class SimpleMapExtents {

  public static int minX;
  public static int minY;
  public static int maxX;
  public static int maxY;

  public static void init() {
    minX = S.locationX - GameConstants.MAP_MAX_WIDTH;
    minY = S.locationY - GameConstants.MAP_MAX_HEIGHT;
    maxX = S.locationX + GameConstants.MAP_MAX_WIDTH;
    maxY = S.locationY + GameConstants.MAP_MAX_HEIGHT;
  }

  public static void update() {
    if (S.sensorType == null) {
      X.yield();
    }
    if (S.sensorType != null) {
      int range = (int) Math.round(Math.sqrt(S.sensorType.range));
      for (Direction d : U.orthogonalDirections) {
        MapLocation loc = S.location.add(d, range);
        TerrainTile tt = S.rc.senseTerrainTile(loc);
        if (tt == TerrainTile.OFF_MAP) {
          if (d == Direction.NORTH && loc.y > minY)
            minY = loc.y;
          if (d == Direction.EAST && loc.x < maxX)
            maxX = loc.x;
          if (d == Direction.SOUTH && loc.y < maxY)
            maxY = loc.y;
          if (d == Direction.WEST && loc.x > minX)
            minX = loc.x;
        }
      }
    }
  }

  public static boolean onMap(MapLocation m) {
    return m.x >= minX && m.x <= maxX && m.y >= minY && m.y <= maxY;
  }

  public static int distFromEdge() {
    int bestDist = Integer.MAX_VALUE;
    {
      int dist = S.locationX - minX;
      if (dist < bestDist)
        bestDist = dist;
    }
    {
      int dist = S.locationY - minY;
      if (dist < bestDist)
        bestDist = dist;
    }
    {
      int dist = maxX - S.locationX;
      if (dist < bestDist)
        bestDist = dist;
    }
    {
      int dist = maxY - S.locationY;
      if (dist < bestDist)
        bestDist = dist;
    }
    return bestDist;
  }

  public static Direction randomDirAwayFromEdge() {
    for (int i = 0; i < 13; i++) {
      Direction d = U.directions[S.randomInt(8)];
      if (onMap(S.location.add(d, 10)))
        return d;
    }
    return U.directions[S.randomInt(8)];
  }
}
