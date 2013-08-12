package betaCorps;

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

public class RobotPlayer implements Runnable {

	public static RobotController rc;
	public static Random rand;

	public static Direction[] dirs = new Direction[] { Direction.NORTH,
			Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
			Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
			Direction.NORTH_WEST };

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

	public static void faceDirection(RobotController rc, Direction d)
			throws Exception {
		rc.setDirection(d);
	}

	public static void moveRandomDirection(RobotController rc) throws Exception {
		Direction d = Direction.values()[rand.nextInt(8)];
		faceDirection(rc, d);
		rc.yield();
		rc.moveForward();
	}

	public static void trySpawn(RobotController rc, RobotType t)
			throws Exception {
		rc.spawn(t);
	}

	public static void healIfGroundAlly(RobotController rc, MapLocation m)
			throws Exception {
		Robot r = rc.senseGroundRobotAtLocation(m);
		if (r != null) {
			RobotInfo ri = rc.senseRobotInfo(r);
			if (ri.team == rc.getTeam()) {
				double canTake = GameConstants.ENERGON_RESERVE_SIZE
						- ri.energonReserve;
				double canGive = rc.getEnergonLevel();
				double shouldGive = Math.min(canTake, canGive);
				rc.transferUnitEnergon(shouldGive, m, RobotLevel.ON_GROUND);
			}
		}
	}

	public static void healGroundStuff(RobotController rc) throws Exception {
		healIfGroundAlly(rc, rc.getLocation());
		MapLocation here = rc.getLocation();
		for (Direction d : dirs) {
			healIfGroundAlly(rc, here.add(d));
		}
	}
	
	public static void buildTowerIfPossible(RobotController rc) throws Exception {
		if (rc.getFlux() > RobotType.COMM.spawnFluxCost()) {
			trySpawn(rc, RobotType.COMM);
		}
	}
	
	public static void buildWoutIfPossible(RobotController rc) throws Exception {
		if (rc.getEnergonLevel() > RobotType.WOUT.spawnCost()) {
			trySpawn(rc, RobotType.WOUT);
		}
	}
	
	public static void sendFluxToArchon(RobotController rc) throws Exception {
		
	}

	// ////////////////////////////////////////
	// ////////////////////////////////////////

	public RobotPlayer(RobotController rc) throws Exception {
		RobotPlayer.rc = rc;
		rand = new Random((long) Math.pow(rc.getRobot().getID() + 7919, 6829));
	}

	public void run() {
		while (true) {
			try {
				if (rc.getRobotType() == RobotType.ARCHON) {
					healGroundStuff(rc);
					buildWoutIfPossible(rc);
					buildTowerIfPossible(rc);
					moveRandomDirection(rc);
				} else if (rc.getRobotType() == RobotType.WOUT) {
					buildTowerIfPossible(rc);
					moveRandomDirection(rc);
				}
			} catch (Exception e) {

			}
			rc.yield();
		}
	}
}
