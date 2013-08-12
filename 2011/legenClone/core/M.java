package legenClone.core;

import legenClone.util.MapUtil;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.TerrainTile;

/**
 * Map information.
 */
public class M {
  /** The width of the expanded map-related arrays. */
  public static final int arrayWidth = GameConstants.MAP_MAX_WIDTH * 2 + 1;
  /** The height of the map-related arrays. */
  public static final int arrayHeight = GameConstants.MAP_MAX_HEIGHT * 2 + 1;

  /** The X location for the 1st element in the map arrays. */
  public static int arrayBaseX;
  /** The Y location for the 1st element in the map arrays. */
  public static int arrayBaseY;

  /** True if the map location is passable by ground units. */
  public static final boolean land[][] = new boolean[arrayWidth][arrayHeight];
  /** True if the map location is known. */
  public static final boolean known[][] = new boolean[arrayWidth][arrayHeight];

  /**
   * True if the map location has been scanned before.
   * 
   * This is only used for units with omnidirectional sensors.
   */
  public static final boolean scanned[][] = new boolean[arrayWidth][arrayHeight];

  /**
   * True if the map location has been scanned before in a given direction.
   * 
   * This is only used for units with directional sensors.
   */
  public static final boolean scannedDirection[][][] = new boolean[arrayWidth][arrayHeight][MapUtil.ADJACENT_DIRECTIONS];

  /** Best known map boundaries. These will shrink during the game. */
  public static int mapMinX, mapMaxX, mapMinY, mapMaxY;

  /** dx for all map locations in sensor range for the robot's orientation. */
  public static int sensorDx[];
  /** dy for all map locations in sensor range for the robot's orientation. */
  public static int sensorDy[];

  /**
   * dx for all map locations on the edge of the sensor range for the robot's
   * orientation.
   */
  public static int sensorEdgeDx[];
  /**
   * dy for all map locations on the edge of the sensor range for the robot's
   * orientation.
   */
  public static int sensorEdgeDy[];

  /**
   * Updates map data after the robot gains a sensor.
   */
  public static final void updateMapFirstTime() {
    MapUtil.initSensorDxDy();
    updateMapAfterTurning();
    updateMapAfterMoving();
  }

  /**
   * Updates map data that changes when this robot turns.
   */
  public static final void updateMapAfterTurning() {
    if (S.sensorController == null) {
      return;
    }

    M.sensorDx = S.sensorDxByDirection[S.directionInt];
    M.sensorDy = S.sensorDyByDirection[S.directionInt];
    M.sensorEdgeDx = S.sensorEdgeDxByDirection[S.directionInt];
    M.sensorEdgeDy = S.sensorEdgeDyByDirection[S.directionInt];

    if (S.sensorIsOmnidirectional) {
      M.lastUpdateDirectionInt = S.directionInt;
    } else {
      updateMapFromSensors();
    }
  }

  /**
   * Updates map data that changes when this robot moves.
   */
  public static final void updateMapAfterMoving() {
    if (S.sensorController == null) {
      return;
    }

    updateMapFromSensors();
  }

  /**
   * Map initialization used if the robot doesn't receive bearings from an
   * archon for some time.
   */
  public static final void emergencyInit() {
    _initMapWithOrigin(S.locationX, S.locationY);
  }

  /**
   * Initializes the map data structures.
   * 
   * This method should not be called directly. It is called by the *Init()
   * methods.
   * 
   * @param x0 X coordinate for map center
   * @param y0 Y coordinate for map center
   */
  public static final void _initMapWithOrigin(int x0, int y0) {
    M.arrayBaseX = x0 - (GameConstants.MAP_MAX_WIDTH + 1);
    M.mapMinX = M.arrayBaseX + 1;
    M.mapMaxX = M.mapMinX + (2 * GameConstants.MAP_MAX_WIDTH - 1);

    M.arrayBaseY = y0 - (GameConstants.MAP_MAX_HEIGHT + 1);
    M.mapMinY = M.arrayBaseY + 1;
    M.mapMaxY = M.mapMinY + (2 * GameConstants.MAP_MAX_HEIGHT - 1);
  }

  // $ -stubs
  /**
   * Updates map with sensor data, only if necessary.
   */
  public static final void updateMapFromSensors() {
    // $ +stubs
    int ax = S.locationX - M.mapMinX;
    int ay = S.locationY - M.mapMinY;
    if (S.sensorIsOmnidirectional) {
      if (!scanned[ax][ay]) {
        if (M.lastUpdateLocation != null
            && M.lastUpdateLocation.isAdjacentTo(S.location)
            && M.lastUpdateDirectionInt == S.directionInt) {
          updateMapFromSensorVector(M.sensorEdgeDx, M.sensorEdgeDy);
        } else {
          updateMapFromSensorVector(M.sensorDx, M.sensorDy);
        }
        scanned[ax][ay] = true;
      }
    } else {
      if (!scannedDirection[ax][ay][S.directionInt]) {
        if (M.lastUpdateLocation != null
            && M.lastUpdateLocation.isAdjacentTo(S.location)
            && M.lastUpdateDirectionInt == S.directionInt) {
          updateMapFromSensorVector(M.sensorEdgeDx, M.sensorEdgeDy);
        } else {
          updateMapFromSensorVector(M.sensorDx, M.sensorDy);
        }
        scannedDirection[ax][ay][S.directionInt] = true;
      }
    }

    M.lastUpdateLocation = S.location;
    M.lastUpdateDirectionInt = S.directionInt;
  }

  /** Value of S.location at last map update. */
  public static MapLocation lastUpdateLocation;

  /** Value of S.directionInt at last map update. */
  public static int lastUpdateDirectionInt;

  /**
   * Updates map with sensor data.
   * 
   * This method should not be called directly. It is called by updateMapAfter*
   * methods.
   */
  public static final void updateMapFromSensorVector(int[] dxVector,
      int[] dyVector) {
    for (int i = 0; i < dxVector.length; i++) {
      int dx = dxVector[i], dy = dyVector[i];
      int mx = S.locationX + dx, my = S.locationY + dy;

      if (mx < mapMinX || mx > mapMaxX || my < mapMinY || my > mapMaxY)
        continue;

      int ax = mx - arrayBaseX, ay = my - arrayBaseY;
      if (known[ax][ay])
        continue;

      TerrainTile tile = S.rc.senseTerrainTile(new MapLocation(mx, my));
      if (tile == null) {
        // Fake an exception so we'll fail tests.
        try {
          throw new GameActionException(
              GameActionExceptionType.CANT_SENSE_THAT,
              "Can't sense a tile that we should be able to sense");
        } catch (GameActionException e) {
          e.printStackTrace();
        }
        continue;
      }

      if (tile == TerrainTile.OFF_MAP) {
        // Update bounds. At least one update should happen here.
        if (dy == 0) {
          if (dx < 0) {
            if (mx >= mapMinX) {
              mapMinX = mx + 1;
            }
          }
          if (dx > 0) {
            if (mx <= mapMaxX) {
              mapMaxX = mx - 1;
            }
          }
        }
        if (dx == 0) {
          if (dy < 0) {
            if (my >= mapMinY) {
              mapMinY = my + 1;
            }
          }
          if (dy > 0) {
            if (my <= mapMaxY) {
              mapMaxY = my - 1;
            }
          }
        }
      } else {
        land[ax][ay] = (tile == TerrainTile.LAND);
        known[ax][ay] = true;
      }
    }
  }

  /** True if the map location adjacent to the current location is land. */
  public static boolean landAtDirection(int i) {
    int dx = MapUtil.intToDeltaX[i], dy = MapUtil.intToDeltaY[i];
    int ax = S.locationX + dx - M.arrayBaseX, ay = S.locationY + dy
        - M.arrayBaseY;
    return land[ax][ay];
  }

  /** True if the map location adjacent to the current location is known. */
  public static boolean knownAtDirection(int i) {
    int dx = MapUtil.intToDeltaX[i], dy = MapUtil.intToDeltaY[i];
    int ax = S.locationX + dx - M.arrayBaseX, ay = S.locationY + dy
        - M.arrayBaseY;
    return known[ax][ay];
  }

  /** Distance to nearest edge. */
  public static int distanceToNearestEdge() {
    int minDistance = S.locationX - M.mapMinX;
    int dx = M.mapMaxX - S.locationX;
    if (dx < minDistance) {
      dx = minDistance;
    }

    int dy = M.mapMaxY - S.locationY;
    if (dy < minDistance) {
      minDistance = dy;
    }

    dy = S.locationY - M.mapMinY;
    if (dy < minDistance) {
      minDistance = dy;
    }
    return minDistance;
  }

  /** The Direction towards the nearest map edge. */
  public static Direction nearestEdgeDirection() {
    int minDistance = S.locationX - M.mapMinX;
    Direction minDirection = Direction.WEST;

    int dx = M.mapMaxX - S.locationX;
    if (dx < minDistance) {
      dx = minDistance;
      minDirection = Direction.EAST;
    }

    int dy = M.mapMaxY - S.locationY;
    if (dy < minDistance) {
      minDistance = dy;
      minDirection = Direction.SOUTH;
    }

    dy = S.locationY - M.mapMinY;
    if (dy < minDistance) {
      minDistance = dy;
      minDirection = Direction.NORTH;
    }

    return minDirection;
  }

  /**
   * Prints the known map to console. Very expensive.
   */
  public static void debug_PrintMap() {
    for (int y = M.mapMinY; y <= M.mapMaxY; y++) {
      StringBuffer lineBuffer = new StringBuffer();
      for (int x = M.mapMinX; x <= M.mapMaxX; x++) {
        if (!known[x - M.arrayBaseX][y - M.arrayBaseY]) {
          lineBuffer.append(' ');
        } else if (!land[x - M.arrayBaseX][y - M.arrayBaseY]) {
          lineBuffer.append('X');
        } else {
          lineBuffer.append('.');
        }
      }
      System.out.println(lineBuffer.toString());
    }
  }

  public static final boolean onMap(MapLocation m) {
    return m.x >= M.mapMinX && m.x <= M.mapMaxX && m.y >= M.mapMinY
        && m.y <= M.mapMaxY;
  }
}
