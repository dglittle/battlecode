package fan;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class RobotPlayer implements Runnable {

	public static RobotController rc;

	public static Direction[] dirs = new Direction[] { Direction.NORTH,
			Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
			Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
			Direction.NORTH_WEST };

	public static Direction[] dirs9 = new Direction[] { Direction.NONE, Direction.NORTH,
		Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
		Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
		Direction.NORTH_WEST};

	public static Direction[] orthDirs = new Direction[] { Direction.NORTH,
			Direction.EAST, Direction.SOUTH, Direction.WEST, };

	public static int[][] directionToOffset = new int[][] { { 0, -1 },
			{ 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 },
			{ -1, -1 }, { 0, 0 }, { 0, 0 } };

	public static MapLocation add(MapLocation loc, Direction d, int times) {
		int[] off = directionToOffset[d.ordinal()];
		return new MapLocation(loc.getX() + off[0] * times, loc.getY() + off[1]
				* times);
	}
	
	public static class MapBounds {
		public static Integer xMin;
		public static Integer xMax;
		public static Integer yMin;
		public static Integer yMax;
		
		public static void init() {
			xMin = null;
			xMax = null;
			yMin = null;
			yMax = null;
		}
		
		public static void update() {
			MapLocation here = rc.getLocation();
			if (rc.senseTerrainTile(here.add(Direction.WEST)) == TerrainTile.OFF_MAP) {
				xMin = here.getX();
			}
			if (rc.senseTerrainTile(here.add(Direction.EAST)) == TerrainTile.OFF_MAP) {
				xMax = here.getX();
			}
			if (rc.senseTerrainTile(here.add(Direction.NORTH)) == TerrainTile.OFF_MAP) {
				yMin = here.getY();
			}
			if (rc.senseTerrainTile(here.add(Direction.SOUTH)) == TerrainTile.OFF_MAP) {
				yMax = here.getY();
			}
		}
	}

	public static MapLocation add(MapLocation a, MapLocation b) {
		return new MapLocation(a.getX() + b.getX(), a.getY() + b.getY());
	}

	public static MapLocation sub(MapLocation a, MapLocation b) {
		return new MapLocation(a.getX() - b.getX(), a.getY() - b.getY());
	}

	public static MapLocation getClosest(MapLocation[] locs, MapLocation here) {
		MapLocation best = null;
		double bestDist = Double.MAX_VALUE;
		for (MapLocation m : locs) {
			double dist = here.distanceSquaredTo(m);
			if (dist < bestDist) {
				bestDist = dist;
				best = m;
			}
		}
		return best;
	}

	public MapLocation getClosest_butNotRightHere(MapLocation[] ms,
			MapLocation here) {
		MapLocation best = null;
		double dist = Double.MAX_VALUE;
		for (MapLocation m : ms) {
			if (!m.equals(here)) {
				double d = here.distanceSquaredTo(m);
				if (d < dist) {
					dist = d;
					best = m;
				}
			}
		}
		return best;
	}

	public static Direction getArchonSpreadOutDir() {
		MapLocation here = rc.getLocation();
		
		Direction best = null;
		double bestValue = Double.MAX_VALUE;
		for (Direction d : dirs9) {
			if (d == Direction.NONE || rc.canMove(d)) {
				MapLocation dest = here.add(d);

				double value = 0;
				for (MapLocation m : rc.senseAlliedArchons()) {
					if (!m.equals(here)) {
						value += 1.0 / dest.distanceSquaredTo(m);
					}
				}
				if (MapBounds.xMin != null) {
					value += 1.0 / Math.pow(dest.getX() - MapBounds.xMin, 2);
				}
				if (MapBounds.xMax != null) {
					value += 1.0 / Math.pow(dest.getX() - MapBounds.xMax, 2);
				}
				if (MapBounds.yMin != null) {
					value += 1.0 / Math.pow(dest.getY() - MapBounds.yMin, 2);
				}
				if (MapBounds.yMax != null) {
					value += 1.0 / Math.pow(dest.getY() - MapBounds.yMax, 2);
				}

				if (value < bestValue) {
					bestValue = value;
					best = d;
				}
			}
		}
		return best;
	}

	public static void moveDir(Direction d) throws Exception {
		Direction curDir = rc.getDirection();
		if (curDir == d) {
			rc.moveForward();
			rc.yield();
		} else if (curDir.opposite() == d) {
			rc.moveBackward();
			rc.yield();
		} else {
			rc.setDirection(d);
			rc.yield();
			rc.moveForward();
			rc.yield();
		}
	}

	// /////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////

	public RobotPlayer(RobotController rc) throws Exception {
		RobotPlayer.rc = rc;
	}

	public void run() {
		while (true) {
			try {
				if (!rc.isMovementActive()) {
					MapBounds.update();
					
					Direction d = getArchonSpreadOutDir();
					moveDir(d);
					continue;
				}
			} catch (Exception e) {

			}
			rc.yield();
		}
	}
}
