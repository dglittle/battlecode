package heavy1.chaos.little;

import heavy1.blocks.pathfinding.BugPathfindingBlock;
import heavy1.core.D;
import heavy1.core.M;
import heavy1.core.S;
import heavy1.core.U;
import heavy1.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.JumpController;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

class MapExtents {

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

    // work here
    D.debug_setIndicator(1, "min: " + U.offsetTo(new MapLocation(minX, minY))
        + ", max: " + U.offsetTo(new MapLocation(maxX, maxY)));
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
      Direction d = U.directions[S.rand.nextInt(8)];
      if (onMap(S.location.add(d, 10)))
        return d;
    }
    return U.directions[S.rand.nextInt(8)];
  }
}

public class JumpExplorer {

  public static Direction macroDir = MapExtents.randomDirAwayFromEdge();
  public static boolean inited = false;

  public static final void init() {
    MapExtents.init();
    inited = true;
  }

  public static final void update() {
    if (!inited)
      init();
    MapExtents.update();
  }

  public static final void step() throws GameActionException {
    // update macroDir
    if (MapExtents.distFromEdge() < 10) {
      macroDir = MapExtents.randomDirAwayFromEdge();
    }

    // try jumping
    if (S.jumpControllerAvailable()) {
      if (!jump(macroDir)) {
        macroDir = MapExtents.randomDirAwayFromEdge();
      }
    }

    // try moving
    if (!S.movementController.isActive()) {
      Direction d = BugPathfindingBlock.nextDirection(S.location.add(macroDir,
          20));
      if (d.ordinal() < 8) {
        if (S.direction != d) {
          X.setDirection(d);
        } else {
          X.moveForward();
        }
      }
    }
  }

  public static final boolean jump(Direction direction)
      throws GameActionException {
    for (JumpController jc : S.jumpControllers) {
      if (!jc.isActive()) {
        final int[] jumpDx = S.jumpDxByDirection[direction.ordinal()];
        final int[] jumpDy = S.jumpDyByDirection[direction.ordinal()];
        for (int i = 0; i < jumpDx.length; i++) {
          MapLocation possibleJumpLocation = S.location.add(jumpDx[i],
              jumpDy[i]);

          // don't jump into voids or off the map
          TerrainTile tt = S.rc.senseTerrainTile(possibleJumpLocation);
          if (tt != null && !tt.isTraversableAtHeight(S.level))
            continue;

          // don't jump into other robots
          if (S.sensorController.canSenseSquare(possibleJumpLocation)) {
            Robot r = S.senseRobot(possibleJumpLocation, RobotLevel.ON_GROUND);
            if (r != null)
              continue;
          }

          // let's try to jump
          // but if we can't see where we're jumping,
          // we might fail.. so.. tryyyy to jump
          try {
            X.jump(possibleJumpLocation);
            return true;
          } catch (Exception e) {
            return false;
          }
        }
        return false;
      }
    }
    return false;
  }
}
