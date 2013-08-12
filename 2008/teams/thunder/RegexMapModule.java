package thunder;

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
import java.util.Vector;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
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

	// debug stuff
	// work here
	public static String lastPathFindingMap = null;

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

	public static void setMapSquare_ifBlank(StringBuffer buf, MapLocation pos,
			char c) {
		int offset = (pos.getY() - mapOriginY) * mapStride
				+ (pos.getX() - mapOriginX);
		char oldC = buf.charAt(offset);
		if (oldC == ' ') {
			buf.setCharAt((pos.getY() - mapOriginY) * mapStride
					+ (pos.getX() - mapOriginX), c);
		}
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

	public static void addWall(MapLocation pos) {
		StringBuffer buf = new StringBuffer(map);
		setMapSquare(buf, pos, '#');
		map = buf.toString();
	}

	public static boolean isOnMap(MapLocation loc) {
		TerrainTile t = rc.senseTerrainTile(loc);
		if (t != null) {
			return t.getType() != TerrainTile.TerrainType.OFF_MAP;
		}
		return false;
	}

	public static MapLocation[][] incrementalOffsets = new MapLocation[][] {
			{ new MapLocation(0, -6), new MapLocation(-3, -5),
					new MapLocation(-2, -5), new MapLocation(-1, -5),
					new MapLocation(1, -5), new MapLocation(2, -5),
					new MapLocation(3, -5), new MapLocation(-4, -4),
					new MapLocation(4, -4), new MapLocation(-5, -3),
					new MapLocation(5, -3), new MapLocation(-6, 0),
					new MapLocation(6, 0) },
			{ new MapLocation(0, -6), new MapLocation(-3, -5),
					new MapLocation(-2, -5), new MapLocation(0, -5),
					new MapLocation(1, -5), new MapLocation(2, -5),
					new MapLocation(3, -5), new MapLocation(3, -4),
					new MapLocation(4, -4), new MapLocation(4, -3),
					new MapLocation(5, -3), new MapLocation(5, -2),
					new MapLocation(5, -1), new MapLocation(5, 0),
					new MapLocation(6, 0), new MapLocation(5, 2),
					new MapLocation(5, 3) },
			{ new MapLocation(0, -6), new MapLocation(3, -5),
					new MapLocation(4, -4), new MapLocation(5, -3),
					new MapLocation(5, -2), new MapLocation(5, -1),
					new MapLocation(6, 0), new MapLocation(5, 1),
					new MapLocation(5, 2), new MapLocation(5, 3),
					new MapLocation(4, 4), new MapLocation(3, 5),
					new MapLocation(0, 6) },
			{ new MapLocation(5, -3), new MapLocation(5, -2),
					new MapLocation(5, 0), new MapLocation(6, 0),
					new MapLocation(5, 1), new MapLocation(5, 2),
					new MapLocation(4, 3), new MapLocation(5, 3),
					new MapLocation(3, 4), new MapLocation(4, 4),
					new MapLocation(-3, 5), new MapLocation(-2, 5),
					new MapLocation(0, 5), new MapLocation(1, 5),
					new MapLocation(2, 5), new MapLocation(3, 5),
					new MapLocation(0, 6) },
			{ new MapLocation(-6, 0), new MapLocation(6, 0),
					new MapLocation(-5, 3), new MapLocation(5, 3),
					new MapLocation(-4, 4), new MapLocation(4, 4),
					new MapLocation(-3, 5), new MapLocation(-2, 5),
					new MapLocation(-1, 5), new MapLocation(1, 5),
					new MapLocation(2, 5), new MapLocation(3, 5),
					new MapLocation(0, 6) },
			{ new MapLocation(-5, -3), new MapLocation(-5, -2),
					new MapLocation(-6, 0), new MapLocation(-5, 0),
					new MapLocation(-5, 1), new MapLocation(-5, 2),
					new MapLocation(-5, 3), new MapLocation(-4, 3),
					new MapLocation(-4, 4), new MapLocation(-3, 4),
					new MapLocation(-3, 5), new MapLocation(-2, 5),
					new MapLocation(-1, 5), new MapLocation(0, 5),
					new MapLocation(2, 5), new MapLocation(3, 5),
					new MapLocation(0, 6) },
			{ new MapLocation(0, -6), new MapLocation(-3, -5),
					new MapLocation(-4, -4), new MapLocation(-5, -3),
					new MapLocation(-5, -2), new MapLocation(-5, -1),
					new MapLocation(-6, 0), new MapLocation(-5, 1),
					new MapLocation(-5, 2), new MapLocation(-5, 3),
					new MapLocation(-4, 4), new MapLocation(-3, 5),
					new MapLocation(0, 6) },
			{ new MapLocation(0, -6), new MapLocation(-3, -5),
					new MapLocation(-2, -5), new MapLocation(-1, -5),
					new MapLocation(0, -5), new MapLocation(2, -5),
					new MapLocation(3, -5), new MapLocation(-4, -4),
					new MapLocation(-3, -4), new MapLocation(-5, -3),
					new MapLocation(-4, -3), new MapLocation(-5, -2),
					new MapLocation(-5, -1), new MapLocation(-6, 0),
					new MapLocation(-5, 0), new MapLocation(-5, 2),
					new MapLocation(-5, 3) },
			{},
			{ new MapLocation(0, -6), new MapLocation(-3, -5),
					new MapLocation(-2, -5), new MapLocation(-1, -5),
					new MapLocation(0, -5), new MapLocation(1, -5),
					new MapLocation(2, -5), new MapLocation(3, -5),
					new MapLocation(-4, -4), new MapLocation(-3, -4),
					new MapLocation(-2, -4), new MapLocation(-1, -4),
					new MapLocation(0, -4), new MapLocation(1, -4),
					new MapLocation(2, -4), new MapLocation(3, -4),
					new MapLocation(4, -4), new MapLocation(-5, -3),
					new MapLocation(-4, -3), new MapLocation(-3, -3),
					new MapLocation(-2, -3), new MapLocation(-1, -3),
					new MapLocation(0, -3), new MapLocation(1, -3),
					new MapLocation(2, -3), new MapLocation(3, -3),
					new MapLocation(4, -3), new MapLocation(5, -3),
					new MapLocation(-5, -2), new MapLocation(-4, -2),
					new MapLocation(-3, -2), new MapLocation(-2, -2),
					new MapLocation(-1, -2), new MapLocation(0, -2),
					new MapLocation(1, -2), new MapLocation(2, -2),
					new MapLocation(3, -2), new MapLocation(4, -2),
					new MapLocation(5, -2), new MapLocation(-5, -1),
					new MapLocation(-4, -1), new MapLocation(-3, -1),
					new MapLocation(-2, -1), new MapLocation(-1, -1),
					new MapLocation(0, -1), new MapLocation(1, -1),
					new MapLocation(2, -1), new MapLocation(3, -1),
					new MapLocation(4, -1), new MapLocation(5, -1),
					new MapLocation(-6, 0), new MapLocation(-5, 0),
					new MapLocation(-4, 0), new MapLocation(-3, 0),
					new MapLocation(-2, 0), new MapLocation(-1, 0),
					new MapLocation(0, 0), new MapLocation(1, 0),
					new MapLocation(2, 0), new MapLocation(3, 0),
					new MapLocation(4, 0), new MapLocation(5, 0),
					new MapLocation(6, 0), new MapLocation(-5, 1),
					new MapLocation(-4, 1), new MapLocation(-3, 1),
					new MapLocation(-2, 1), new MapLocation(-1, 1),
					new MapLocation(0, 1), new MapLocation(1, 1),
					new MapLocation(2, 1), new MapLocation(3, 1),
					new MapLocation(4, 1), new MapLocation(5, 1),
					new MapLocation(-5, 2), new MapLocation(-4, 2),
					new MapLocation(-3, 2), new MapLocation(-2, 2),
					new MapLocation(-1, 2), new MapLocation(0, 2),
					new MapLocation(1, 2), new MapLocation(2, 2),
					new MapLocation(3, 2), new MapLocation(4, 2),
					new MapLocation(5, 2), new MapLocation(-5, 3),
					new MapLocation(-4, 3), new MapLocation(-3, 3),
					new MapLocation(-2, 3), new MapLocation(-1, 3),
					new MapLocation(0, 3), new MapLocation(1, 3),
					new MapLocation(2, 3), new MapLocation(3, 3),
					new MapLocation(4, 3), new MapLocation(5, 3),
					new MapLocation(-4, 4), new MapLocation(-3, 4),
					new MapLocation(-2, 4), new MapLocation(-1, 4),
					new MapLocation(0, 4), new MapLocation(1, 4),
					new MapLocation(2, 4), new MapLocation(3, 4),
					new MapLocation(4, 4), new MapLocation(-3, 5),
					new MapLocation(-2, 5), new MapLocation(-1, 5),
					new MapLocation(0, 5), new MapLocation(1, 5),
					new MapLocation(2, 5), new MapLocation(3, 5),
					new MapLocation(0, 6) } };

	public static void archonExplore(Direction dir) throws Exception {
		MapLocation loc = rc.getLocation();
		int locX = loc.getX();
		int locY = loc.getY();

		MapLocation[] offsets = incrementalOffsets[dir.ordinal()];

		StringBuffer buf = new StringBuffer(map);
		for (MapLocation offset : offsets) {
			MapLocation square = U.add(loc, offset);
			int x = square.getX();
			int y = square.getY();
			TerrainTile t = rc.senseTerrainTile(square);
			TerrainTile.TerrainType type = t.getType();
			if (type == TerrainTile.TerrainType.WATER) {
				if (x >= mapOriginX && x <= mapRight && y >= mapOriginY
						&& y <= mapBottom) {
					setMapSquare_ifBlank(buf, square, '#');
				}
			} else if (type == TerrainTile.TerrainType.LAND) {
				setMapSquare_ifBlank(buf, square, (char) ('0' + t.getHeight()));
			} else if (type == TerrainTile.TerrainType.OFF_MAP) {
				if (!mapTopSet) {
					if (isOnMap(new MapLocation(x, y + 1))) {
						map = buf.toString();
						setMapTop(y + 2);
						buf = new StringBuffer(map);
					}
				}
				if (!mapBottomSet) {
					if (isOnMap(new MapLocation(x, y - 1))) {
						map = buf.toString();
						setMapBottom(y - 2);
						buf = new StringBuffer(map);
					}
				}
				if (!mapLeftSet) {
					if (isOnMap(new MapLocation(x + 1, y))) {
						map = buf.toString();
						setMapLeft(x + 2);
						buf = new StringBuffer(map);
					}
				}
				if (!mapRightSet) {
					if (isOnMap(new MapLocation(x - 1, y))) {
						map = buf.toString();
						setMapRight(x - 2);
						buf = new StringBuffer(map);
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

	public static Direction atob(MapLocation a, MapLocation[] bs) {
		StringBuffer buf = new StringBuffer(map);

		setMapSquare(buf, a, 'a');
		for (MapLocation b : bs) {
			setMapSquare_evenIfOffMap(buf, b, 'b');
		}

		String s = buf.toString();

		while (true) {

			lastPathFindingMap = s;

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
							regex("(?s)[^#T\n](?=.{A,C}b)|(?<=b.{A})[^#T\n]|(?<=b.{B})[^#T\n]|(?<=b.{C})[^#T\n]|[^#T\n](?=b)|(?<=b)[^#T\n]"),
							"b");
			if (s.equals(ss))
				break;
			s = ss;
		}

		return null;
	}

	public static Direction atobFatWalls(MapLocation a, MapLocation[] bs) {

		// increase wall sizes
		String s = map
				.replaceAll(
						regex("(?s)[^#\n](?=.{A,C}#)|(?<=#.{A})[^#\n]|(?<=#.{B})[^#\n]|(?<=#.{C})[^#\n]|[^#\n](?=#)|(?<=#)[^#\n]"),
						"#");

		StringBuffer buf = new StringBuffer(s);

		setMapSquare(buf, a, 'a');
		for (MapLocation b : bs) {
			setMapSquare_evenIfOffMap(buf, b, 'b');
		}

		s = buf.toString();

		while (true) {

			lastPathFindingMap = s;

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
							regex("(?s)[^#T\n](?=.{A,C}b)|(?<=b.{A})[^#T\n]|(?<=b.{B})[^#T\n]|(?<=b.{C})[^#T\n]|[^#T\n](?=b)|(?<=b)[^#T\n]"),
							"b");
			if (s.equals(ss)) {
				break;
			}
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

	public static void debug_printLastMap() {
		debug_printMap(lastPathFindingMap);
	}
}
