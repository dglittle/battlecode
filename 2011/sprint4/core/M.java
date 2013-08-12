package sprint4.core;

import sprint4.core.xconst.SensorRanges;
import sprint4.core.xconst.XChassis;
import sprint4.core.xconst.XDirection;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.TerrainTile;

/**
 * Map information.
 */
public class M {
  /**
   * Set to false to disable checking each round, and make the simulation
   * faster.
   */
  public static final boolean MAP_DEBUG_CHECKS = false;

  /** The width of the expanded map-related arrays. */
  public static final int arrayWidth = GameConstants.MAP_MAX_WIDTH * 2 + 1;
  /** The height of the map-related arrays. */
  public static final int arrayHeight = GameConstants.MAP_MAX_HEIGHT * 2 + 1;

  /** The X location for the 1st element in the map arrays. */
  public static int arrayBaseX;
  /** The Y location for the 1st element in the map arrays. */
  public static int arrayBaseY;

  /** True if the map location is known. */
  public static final boolean known[][] = new boolean[arrayWidth][arrayHeight];
  /** True if the map location is land (not void). */
  public static final boolean land[][] = new boolean[arrayWidth][arrayHeight];
  /** True if the map location can be traversed by land units. */
  public static final boolean passable[][] = new boolean[arrayWidth][arrayHeight];

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
  public static final boolean scannedDirection[][][] = new boolean[arrayWidth][arrayHeight][XDirection.ADJACENT_DIRECTIONS];

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
    SensorRanges.initSensorDxDy();
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
    M.arrayBaseX = x0 - GameConstants.MAP_MAX_WIDTH;
    M.mapMinX = M.arrayBaseX + 1;
    M.mapMaxX = M.mapMinX + (2 * GameConstants.MAP_MAX_WIDTH - 2);

    M.arrayBaseY = y0 - GameConstants.MAP_MAX_HEIGHT;
    M.mapMinY = M.arrayBaseY + 1;
    M.mapMaxY = M.mapMinY + (2 * GameConstants.MAP_MAX_HEIGHT - 2);
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
    for (int i = dxVector.length - 1; i >= 0; i--) {
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
          D.debug_logException(e);
        }
        continue;
      }

      if (tile == TerrainTile.OFF_MAP) {
        // Update bounds. At least one update should happen here.
        if (dy == 0) {
          if (dx < 0) {
            if (mx >= mapMinX) {
              mapMinX = mx + 1;
              _drawVerticalGuard(ax);
            }
          }
          if (dx > 0) {
            if (mx <= mapMaxX) {
              mapMaxX = mx - 1;
              _drawVerticalGuard(ax);
            }
          }
        }
        if (dx == 0) {
          if (dy < 0) {
            if (my >= mapMinY) {
              mapMinY = my + 1;
              _drawHorizontalGuard(ay);
            }
          }
          if (dy > 0) {
            if (my <= mapMaxY) {
              mapMaxY = my - 1;
              _drawHorizontalGuard(ay);
            }
          }
        }
      } else {
        known[ax][ay] = true;
        passable[ax][ay] = land[ax][ay] = (tile == TerrainTile.LAND);
      }
    }

    // TODO(pwnall): consider trade-off between senseObjectAtLocation and
    // senseNearbyGameObjects
    _updateMapPassableWithNearbyRobots();

    if (MAP_DEBUG_CHECKS) {
      debug_checkMap();
    }
  }

  /**
   * Updates {@link M#passable} to reflect nearby dynamic objects.
   */
  public static final void _updateMapPassableWithNearbyRobots() {
    RobotInfo[] nearbyRobotInfos = S.nearbyRobotInfos();
    for (int i = nearbyRobotInfos.length - 1; i >= 0; i--) {
      RobotInfo info = nearbyRobotInfos[i];
      if (info == null)
        continue;
      switch (info.chassis.ordinal()) {
        case XChassis.BUILDING_INT:
          // TODO(pwnall): maybe skip some buildings
        case XChassis.DEBRIS_INT: {
          MapLocation location = info.location;
          int mx = location.x - M.arrayBaseX;
          int my = location.y - M.arrayBaseY;
          passable[mx][my] = false;
          break;
        }
        default:
          if (!info.on && info.robot.getTeam() != S.team) {
            // Dummy or sleeping enemy robot.
            MapLocation location = info.location;
            int mx = location.x - M.arrayBaseX;
            int my = location.y - M.arrayBaseY;
            passable[mx][my] = false;
          }
      }
    }
  }

  /** True if the map location adjacent to the current location is land. */
  public static boolean landAtDirection(int i) {
    int dx = XDirection.intToDeltaX[i], dy = XDirection.intToDeltaY[i];
    int ax = S.locationX + dx - M.arrayBaseX, ay = S.locationY + dy
        - M.arrayBaseY;
    return land[ax][ay];
  }

  /** True if the map location adjacent to the current location is known. */
  public static boolean knownAtDirection(int i) {
    int dx = XDirection.intToDeltaX[i], dy = XDirection.intToDeltaY[i];
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

  /** Draws the initial guard lines on the map. */
  static {
    _drawHorizontalGuard(0);
    _drawHorizontalGuard(arrayHeight - 1);
    _drawVerticalGuard(0);
    _drawVerticalGuard(arrayWidth - 1);
  }

  /**
   * Draws a horizontal guard line that path-finding won't cross.
   * 
   * To simplify path-finding code, guard lines should be drawn on map edges.
   * 
   * @param my the Y coordinate of the guard line, in map space
   */
  public static final void _drawHorizontalGuard(int my) {
    for (int mx = arrayWidth - 1; mx >= 0; mx--) {
      known[mx][my] = true;
    }
  }

  /**
   * Draws a vertical guard line that path-finding won't cross.
   * 
   * To simplify path-finding code, guard lines should be drawn on map edges.
   * 
   * @param my the X coordinate of the guard line, in map space
   */
  public static final void _drawVerticalGuard(int mx) {
    for (int my = arrayHeight - 1; my >= 0; my--) {
      known[mx][my] = true;
    }
  }

  /** Checks the mapping data structures for inconsistencies. */
  public static void debug_checkMap() {
    debug_setIndicator();
    for (int my = 0; my < M.arrayHeight; my++) {
      int y = my + M.arrayBaseY;
      for (int mx = 0; mx < M.arrayWidth; mx++) {
        int x = mx + M.arrayBaseX;

        if (!known[mx][my]) {
          D.debug_assert(!land[mx][my], "Land is true for unknown location");
          D.debug_assert(!passable[mx][my],
              "Passable is true for unknown location");
        } else if (!land[mx][my]) {
          D.debug_assert(!passable[mx][my],
              "Passable is true for non-land location");
        }
        if (x == M.mapMinX - 1 || x == M.mapMaxX + 1 || y == M.mapMinY - 1
            || y == M.mapMaxY + 1) {
          D.debug_assert(known[mx][my], "Missing guard on map edge " + x + ", "
              + y);
          D.debug_assert(!land[mx][my], "Map edge guard marked as land");
          D.debug_assert(!passable[mx][my], "Map edge guard marked as passable");
        }
      }
    }
  }

  /** Sets indicator 0 to help with map debugging. */
  public static void debug_setIndicator() {
    D.debug_setIndicator(0, "Map at: " + S.locationX + ", " + S.locationY
        + " bounds: " + M.mapMinX + ", " + M.mapMinY + " - " + M.mapMaxX + ", "
        + M.mapMaxY);
  }

  /**
   * Prints the known map to console.
   */
  public static void debug_printMap() {
    for (int y = M.mapMinY - 1; y <= M.mapMaxY + 1; y++) {
      StringBuffer lineBuffer = new StringBuffer();
      int my = y - M.arrayBaseY;
      for (int x = M.mapMinX - 1; x <= M.mapMaxX + 1; x++) {
        int mx = x - M.arrayBaseX;
        if (!known[mx][my]) {
          lineBuffer.append(' ');
        } else if (!land[mx][my]) {
          lineBuffer.append('X');
        } else {
          if (passable[mx][my]) {
            lineBuffer.append('.');
          } else {
            lineBuffer.append('@');
          }
        }
      }
      System.out.println(lineBuffer.toString());
    }
  }
  
  /**
   * returns true if the location is within the known map bounds
   */
  public static final boolean onMap(MapLocation loc) {
    return loc.x >= mapMinX && loc.x <= mapMaxX && loc.y >= mapMinY
        && loc.y <= mapMaxY;
  }
  
  /**
   * Checks if a {@link MapLocation} is within the map bounds, but unknown.
   * @param location the {@link MapLocation} to be checked
   * @return true if the location is within the map bounds and unknown
   */
  public static final boolean unknownOnMap(MapLocation location) {
    if (location.x < mapMinX || location.x > mapMaxX ||
        location.y < mapMinY || location.y > mapMaxY) {
      return false;
    }
    return !M.known[location.x - M.arrayBaseX][location.y - M.arrayBaseY];
  }
}
