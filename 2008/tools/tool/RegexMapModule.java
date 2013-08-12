package tool;

import static battlecode.common.Direction.EAST;
import static battlecode.common.Direction.NORTH;
import static battlecode.common.Direction.NORTH_EAST;
import static battlecode.common.Direction.NORTH_WEST;
import static battlecode.common.Direction.SOUTH;
import static battlecode.common.Direction.SOUTH_EAST;
import static battlecode.common.Direction.SOUTH_WEST;
import static battlecode.common.Direction.WEST;

import java.util.HashMap;
import java.util.Random;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class RegexMapModule {

	// doesn't change
	public static RobotController rc;
	public static Robot robot;
	public static int id;
	public static Random r;
	
	// changes
	public static RobotType prevRobotType;
	public static MapLocation prevLoc;
	public static Direction prevDir;

	// map stuff
	public static int mapOriginX;
	public static int mapOriginY;
	public static int mapStride = 96;
	public static int mapRight;
	public static int mapBottom;
	public static String map = "                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n                                                                                               \n";
	public static boolean mapTopSet = false;
	public static boolean mapBottomSet = false;
	public static boolean mapLeftSet = false;
	public static boolean mapRightSet = false;

	public static void init(RobotController rc) throws Exception {
		RegexMapModule.rc = rc;
		robot = rc.getRobot();
		id = robot.getID();
		r = new Random(rc.getRobot().getID());

		MapLocation loc = rc.getLocation();
		mapOriginX = loc.getX() - (mapStride - 2) / 2;
		mapOriginY = loc.getY() - (mapStride - 2) / 2;
		mapRight = mapOriginX + mapStride - 2;
		mapBottom = mapOriginY + mapStride - 2;
	}

	public static void setMapTop(int mapTop) {
		int amount = mapTop - mapOriginY;
		mapOriginY = mapTop;
		map = map.substring(amount * mapStride, map.length());
		mapTopSet = true;
	}

	public static void setMapBottom(int mapBottom) {
		RegexMapModule.mapBottom = mapBottom;
		int height = mapBottom - mapOriginY + 1;
		map = map.substring(0, height * mapStride);
		mapBottomSet = true;
	}

	public static void setMapLeft(int mapLeft) {
		int amount = mapLeft - mapOriginX;
		mapOriginX = mapLeft;
		mapStride -= amount;
		map = map.replaceAll("(?m)^.{" + amount + "}", "");
		regexCache.clear();
		mapLeftSet = true;
	}

	public static void setMapRight(int mapRight) {
		int amount = RegexMapModule.mapRight - mapRight;
		RegexMapModule.mapRight = mapRight;
		mapStride -= amount;
		map = map.replaceAll("(?m).{" + amount + "}$", "");
		regexCache.clear();
		mapRightSet = true;
	}

	public static void setMapSquare(StringBuffer buf, MapLocation pos, char c) {
		buf.setCharAt((pos.getY() - mapOriginY) * mapStride
				+ (pos.getX() - mapOriginX), c);
	}

	public static void setMapSquare_evenIfOffMap(StringBuffer buf,
			MapLocation pos, char c) {
		int x = pos.getX();
		int y = pos.getY();
		if (x < mapOriginX)
			x = mapOriginX;
		if (x > mapRight)
			x = mapRight;
		if (y < mapOriginY)
			y = mapOriginY;
		if (y > mapBottom)
			y = mapBottom;
		setMapSquare(buf, new MapLocation(x, y), c);
	}
	
	public static void markTower(MapLocation pos) {
		StringBuffer buf = new StringBuffer(map);
		setMapSquare(buf, pos, 'T');
		map = buf.toString();
	}

	public static void explore() throws Exception {
		RobotType robotType = rc.getRobotType();
		MapLocation loc = rc.getLocation();
		Direction dir = rc.getDirection();
		int locX = loc.getX();
		int locY = loc.getY();
		
		if (robotType == prevRobotType) {
			if (loc.equals(prevLoc)) {
				if (dir.equals(prevDir)) {
					return;				
				} else if (robotType.sensorAngle() == 360) {
					return;
				}
			}
		}
		
		prevRobotType = robotType;
		prevLoc = loc;
		prevDir = dir;		

		int r = robotType.sensorRadius();
		int x1 = Math.max(locX - r, mapOriginX);
		int x2 = Math.min(locX + r, mapRight);
		int y1 = Math.max(locY - r, mapOriginY);
		int y2 = Math.min(locY + r, mapBottom);
		StringBuffer buf = new StringBuffer(map);
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				MapLocation square = new MapLocation(x, y);
				if (!rc.canSenseSquare(square))
					continue;
				if (x < mapOriginX || x > mapRight || y < mapOriginY
						|| y > mapBottom)
					continue;
				TerrainTile t = rc.senseTerrainTile(square);
				TerrainTile.TerrainType type = t.getType();
				if (type == TerrainTile.TerrainType.WATER) {
					setMapSquare(buf, square, '#');
				} else if (type == TerrainTile.TerrainType.LAND) {
					setMapSquare(buf, square, (char) ('0' + t.getHeight()));
				} else if (type == TerrainTile.TerrainType.OFF_MAP) {
					int squareX = square.getX();
					int squareY = square.getY();
					if (squareX == locX) {
						if (squareY < locY) {
							if (!mapTopSet) {
								if (rc.senseTerrainTile(new MapLocation(
										squareX, squareY + 1)) != TerrainTile.OFF_MAP) {
									map = buf.toString();
									setMapTop(squareY + 2);
									buf = new StringBuffer(map);
								}
							}
						} else {
							if (!mapBottomSet) {
								if (rc.senseTerrainTile(new MapLocation(
										squareX, squareY - 1)) != TerrainTile.OFF_MAP) {
									map = buf.toString();
									setMapBottom(squareY - 2);
									buf = new StringBuffer(map);
								}
							}
						}
					} else if (squareY == locY) {
						if (squareX < locX) {
							if (!mapLeftSet) {
								if (rc.senseTerrainTile(new MapLocation(
										squareX + 1, squareY)) != TerrainTile.OFF_MAP) {
									map = buf.toString();
									setMapLeft(squareX + 2);
									buf = new StringBuffer(map);
								}
							}
						} else {
							if (!mapRightSet) {
								if (rc.senseTerrainTile(new MapLocation(
										squareX - 1, squareY)) != TerrainTile.OFF_MAP) {
									map = buf.toString();
									setMapRight(squareX - 2);
									buf = new StringBuffer(map);
								}
							}
						}
					}
				}
			}
		}
		map = buf.toString();
	}

	public static HashMap<String, String> regexCache = new HashMap<String, String>();

	public static String regex(String regexKey) {
		String regex = regexCache.get(regexKey);
		if (regex != null) {
			return regex;
		} else {
			regex = regexKey.replaceAll("A", "" + (mapStride - 2)).replaceAll(
					"B", "" + (mapStride - 1)).replaceAll("C", "" + mapStride);
			regexCache.put(regexKey, regex);
			return regex;
		}
	}

	public static Direction atob(MapLocation a, MapLocation b) {
		StringBuffer buf = new StringBuffer(map);

		setMapSquare(buf, a, 'a');
		setMapSquare_evenIfOffMap(buf, b, 'b');

		String s = buf.toString();

		while (true) {
			if (s
					.matches(regex("(?s).*(a(?=.{A,C}b)|(?<=b.{A})a|(?<=b.{B})a|(?<=b.{C})a|a(?=b)|(?<=b)a).*"))) {
				int i = 0;

				if (s.matches(regex("(?s).*a(?=.{B}b).*"))) {
					return SOUTH;
				} else if (s.matches(regex("(?s).*(?<=b.{B})a.*"))) {
					return NORTH;
				} else if (s.matches(regex("(?s).*a(?=b).*"))) {
					return EAST;
				} else if (s.matches(regex("(?s).*(?<=b)a.*"))) {
					return WEST;
				}

				if (s.matches(regex("(?s).*(?<=b.{A})a.*"))) {
					return NORTH_EAST;
				} else if (s.matches(regex("(?s).*(?<=b.{C})a.*"))) {
					return NORTH_WEST;
				} else if (s.matches(regex("(?s).*a(?=.{C}b).*"))) {
					return SOUTH_EAST;
				} else if (s.matches(regex("(?s).*a(?=.{A}b).*"))) {
					return SOUTH_WEST;
				}
			}

			String ss = s
					.replaceAll(
							regex("(?s)[^#\n](?=.{A,C}b)|(?<=b.{A})[^#\n]|(?<=b.{B})[^#\n]|(?<=b.{C})[^#\n]|[^#\n](?=b)|(?<=b)[^#\n]"),
							"b");
			if (s.equals(ss))
				break;
			s = ss;
		}

		return null;
	}

	public static void debug_printMap() {
		StringBuffer buf = new StringBuffer(map);
		setMapSquare(buf, rc.getLocation(), 'x');
		String s = buf.toString();

		debug_printMap(s);
	}

	public static void debug_printMap(String s) {
		System.out.println();
		System.out.print(s.replaceAll(" ", "."));
	}
}
