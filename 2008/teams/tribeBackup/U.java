package tribeBackup;

import static battlecode.common.Direction.EAST;
import static battlecode.common.Direction.NORTH;
import static battlecode.common.Direction.NORTH_EAST;
import static battlecode.common.Direction.NORTH_WEST;
import static battlecode.common.Direction.OMNI;
import static battlecode.common.Direction.SOUTH;
import static battlecode.common.Direction.SOUTH_EAST;
import static battlecode.common.Direction.SOUTH_WEST;
import static battlecode.common.Direction.WEST;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class U {

	public static int timer_startRound;
	public static int timer_startByte;

	public static void debug_startTimer() {
		timer_startRound = Clock.getRoundNum();
		timer_startByte = Clock.getBytecodeNum();
	}

	public static int debug_stopTimer() {
		int endByte = Clock.getBytecodeNum();
		int endRound = Clock.getRoundNum();
		int bytes = (6000 - timer_startByte)
				+ (endRound - timer_startRound - 1) * 6000 + endByte - 3;
		System.out.println("bytes = " + bytes);
		return bytes;
	}

	public static MapLocation add(MapLocation a, MapLocation b) {
		return new MapLocation(a.getX() + b.getX(), a.getY() + b.getY());
	}

	public static MapLocation sub(MapLocation a, MapLocation b) {
		return new MapLocation(a.getX() - b.getX(), a.getY() - b.getY());
	}

	public static double sum(double[] a) {
		double sum = 0;
		for (int i = 0; i < a.length; i++) {
			sum += a[i];
		}
		return sum;
	}

	public static boolean near(MapLocation a, MapLocation b) {
		return a.equals(b) || a.isAdjacentTo(b);
	}

	public static int oppositeDir(int dir) {
		return Direction.values()[dir].opposite().ordinal();
	}

	public static final double dirTo_divisor = (2.0 * Math.PI) / 8.0;

	public static Direction dirTo(MapLocation from, MapLocation to) {
		int dx = to.getX() - from.getX();
		int dy = from.getY() - to.getY();
		return Direction.values()[((int) Math.round(Math.atan2(dx, dy)
				/ dirTo_divisor) + 8) % 8];
	}
	// old version
	// public static Direction dirTo(MapLocation from, MapLocation to) {
	// int dx = to.getX() - from.getX();
	// int dy = from.getY() - to.getY();
	// if (dx > 0) {
	// if (dy > 0) {
	// return NORTH_EAST;
	// } else if (dy == 0) {
	// return EAST;
	// } else {
	// return SOUTH_EAST;
	// }
	// } else if (dx == 0) {
	// if (dy > 0) {
	// return NORTH;
	// } else if (dy == 0) {
	// return OMNI;
	// } else {
	// return SOUTH;
	// }
	// } else {
	// if (dy > 0) {
	// return NORTH_WEST;
	// } else if (dy == 0) {
	// return WEST;
	// } else {
	// return SOUTH_WEST;
	// }
	// }
	// }
}
