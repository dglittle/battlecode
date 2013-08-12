package Fluid;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;

/*
 TODO:

 Towers
 - make leaders choose next towers to create
 - make wouts backtrack with a looping maplocation history

 Wouts
 - make wouts receive energon only if people are healthy, and but not give it
 - esp not to other wouts
 - make wouts give flux, but not receive it

 Messages
 - make messages fixed length
 - do message attacks with big messages

 */

public class RobotPlayer implements Runnable {

	public static int id;
	public static Team team;
	public static MapLocation here;
	public static Direction facing;
	public static int round;
	public static RobotController rc;
	public static Robot r;
	public static RobotType type;
	public static boolean isArchon;
	public static boolean isWout;
	public static boolean isSoldier;
	public static double energon;
	public static double eventualEnergon;
	public static double maxEnergon;
	public static double reserve;
	public static MapLocation[] archons;
	public static int birthRound;
	public static int age;
	public static Random rand;
	public static double flux;
	public static double maxFlux;
	public static boolean canMove;
	public static boolean canAttack;
	public static MapLocation nearestArchon;
	public static int lastBirth;

	// non-archons only
	public static int parentIndex = -1;
	public static int prevArchonsAlive;
	public static MapLocation parentLoc;

	// archons only
	public static Direction originalEnemyDir;
	public static Set<Integer> childrenIds = new HashSet();
	public static int[] childrenCounts = new int[RobotType.values().length];

	// ////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////

	public static int beginTime;
	public static int beginRound;

	public static void beginTimer() {
		beginTime = Clock.getBytecodeNum();
		beginRound = Clock.getRoundNum();
	}

	public static void endTimer() {
		int endTime = Clock.getBytecodeNum();
		int endRound = Clock.getRoundNum();
		int time = (endTime - beginTime) + (endRound - beginRound) * 6000;
		time -= 6;
		System.out.println("time: " + time);
	}

	public static MapLocation getNearestArchon() {
		MapLocation best = null;
		int bestDist = Integer.MAX_VALUE;
		for (MapLocation loc : rc.senseAlliedArchons()) {
			if (isArchon && loc.equals(here))
				continue;
			int dist = loc.distanceSquaredTo(here);
			if (dist < bestDist) {
				bestDist = dist;
				best = loc;
			}
		}
		return best;
	}

	public static MapLocation add(MapLocation loc, int x, int y) {
		return new MapLocation(loc.getX() + x, loc.getY() + y);
	}

	public static int[][] directionToOffset = new int[][] { { 0, -1 },
			{ 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 },
			{ -1, -1 }, { 0, 0 }, { 0, 0 } };

	public static MapLocation add(MapLocation loc, Direction d, int times) {
		int[] off = directionToOffset[d.ordinal()];
		return new MapLocation(loc.getX() + off[0] * times, loc.getY() + off[1]
				* times);
	}

	public static boolean move(Direction dir) throws Exception {
		if (canMove) {
			if (rc.canMove(dir)) {
				if (facing == dir) {
					rc.moveForward();
					return true;
				} else if (facing.opposite() == dir) {
					rc.moveBackward();
					return true;
				} else {
					rc.setDirection(dir);
					return true;
				}
			}
		}
		return false;
	}

	public static boolean moveDontTurn(Direction dir) throws Exception {
		if (canMove) {
			if (rc.canMove(dir)) {
				if (facing == dir) {
					rc.moveForward();
					return true;
				} else if (facing.opposite() == dir) {
					rc.moveBackward();
					return true;
				}
			}
		}
		return false;
	}

	public static boolean tryMove(Direction dir) throws Exception {
		if (canMove) {
			if (move(dir)) {
				return true;
			} else {
				Direction left = dir.rotateLeft();
				Direction right = dir.rotateRight();
				if (moveDontTurn(right))
					return true;
				if (move(left))
					return true;
				if (move(right))
					return true;
			}
		}
		return false;
	}

	public static boolean tryMove(MapLocation loc) throws Exception {
		int dist = here.distanceSquaredTo(loc);
		if (dist <= 2)
			return false;
		if (canMove) {
			if (tryMove(here.directionTo(loc)))
				return true;

			Direction left = facing.rotateLeft().rotateLeft();
			Direction right = facing.rotateRight().rotateRight();

			MapLocation there = here.add(left);
			if (here.distanceSquaredTo(there) < dist) {
				if (move(left))
					return true;
			}

			there = here.add(right);
			if (here.distanceSquaredTo(there) < dist) {
				if (move(right))
					return true;
			}
		}
		return false;
	}

	public static boolean canBuild(Direction dir) throws Exception {
		MapLocation there = here.add(dir);
		TerrainTile tt = rc.senseTerrainTile(there);
		if (!tt.isTraversableAtHeight(RobotLevel.ON_GROUND))
			return false;
		return rc.senseGroundRobotAtLocation(there) == null;
	}

	public static boolean tryBuild(RobotType type) throws Exception {
		Direction dir = facing;
		for (int i = 0; i < 8; i++, dir = dir.rotateRight()) {
			if (!canBuild(dir))
				continue;
			if (face(dir))
				return true;
			return build(type);
		}
		return false;
	}

	public static int getArchonIndex() throws Exception {
		for (int i = 0; i < archons.length; i++) {
			if (archons[i].equals(here))
				return i;
		}
		throw new Exception("no archon index?");
	}

	public static boolean build(RobotType type) throws Exception {
		if (canBuild(facing)) {
			rc.spawn(type);
			rc.yield();
			MapLocation front = here.add(facing);
			Robot r = rc.senseGroundRobotAtLocation(front);
			if (r != null) {
				int childId = r.getID();
				childrenIds.add(childId);
				childrenCounts[type.ordinal()]++;
			}
			lastBirth = round;
			return true;
		}
		return false;
	}

	public static Direction randomDir() {
		return Direction.values()[rand.nextInt(8)];
	}

	public static RobotInfo nearestEnemy() throws Exception {
		RobotInfo best = null;
		int bestDist = Integer.MAX_VALUE;
		for (int i = 0; i < 2; i++) {
			for (Robot r : (i == 0) ? rc.senseNearbyGroundRobots() : rc
					.senseNearbyAirRobots()) {
				RobotInfo ri = rc.senseRobotInfo(r);
				if (ri.team != team) {
					int dist = here.distanceSquaredTo(ri.location);
					if (dist < bestDist) {
						bestDist = dist;
						best = ri;
					}
				}
			}
		}
		return best;
	}

	public static boolean attack(RobotInfo ri) throws Exception {
		if (canAttack) {
			if (rc.canAttackSquare(ri.location)) {
				if (ri.type.isAirborne()) {
					rc.attackAir(ri.location);
					return true;
				} else {
					rc.attackGround(ri.location);
					return true;
				}
			}
		}
		return false;
	}

	public static boolean face(Direction dir) throws Exception {
		if (facing != dir) {
			rc.setDirection(dir);
			return true;
		}
		return false;
	}

	public static boolean face(MapLocation loc) throws Exception {
		return face(here.directionTo(loc));
	}

	public static boolean faceOrAway(Direction dir) throws Exception {
		if (facing != dir && facing != dir.opposite()) {
			rc.setDirection(dir);
			return true;
		}
		return false;
	}

	public static Direction rotate(Direction d, int amount) {
		return Direction.values()[(d.ordinal() + amount + 8) % 8];
	}

	public static MapLocation bugGoal;
	public static int bugging;
	public static int bugDistToBeat;
	public static Direction bugDir;

	public static boolean bugTo(MapLocation goal) throws Exception {
		int dist = here.distanceSquaredTo(goal);
		if (dist <= 2)
			return false;

		if (bugGoal == null || goal.distanceSquaredTo(bugGoal) > 5) {
			bugging = 0;
			bugGoal = goal;
		}

		if (!canMove)
			return false;

		if (bugging != 0) {
			if (dist < bugDistToBeat) {
				bugging = 0;
			}
		}

		if (bugging == 0) {
			if (tryMove(goal)) {
				return true;
			} else {
				bugging = rand.nextBoolean() ? 1 : -1;
				bugDistToBeat = here.distanceSquaredTo(goal);
				bugDir = here.directionTo(goal);
			}
		}

		if (bugging != 0) {
			for (int i = 0; i < 8; i++) {
				if (rc.canMove(bugDir)) {
					if (faceOrAway(bugDir))
						return true;
					if (move(bugDir)) {
						bugDir = rotate(bugDir, bugDir.isDiagonal() ? -2
								* bugging : -bugging);
						return true;
					}
				} else {
					TerrainTile tt = rc.senseTerrainTile(add(here, bugDir, 2));
					if (tt != null && tt == TerrainTile.OFF_MAP) {
						bugging *= -1;
					}
				}
				bugDir = rotate(bugDir, bugging);
			}
		}

		return false;
	}

	public static final int[][] nextTowerOffsetsNorth = new int[][] { { 0, 5 },
			{ 0, 4 }, { 1, 4 }, { 2, 4 }, { 0, 3 }, { 1, 3 }, { 0, 2 },
			{ 1, 2 }, { 0, 1 }, { 1, 1 } };

	public static final int[][] nextTowerOffsetsNorthEast = new int[][] {
			{ 3, 4 }, { 3, 3 }, { 2, 4 }, { 2, 3 }, { 2, 2 }, { 1, 3 },
			{ 1, 2 }, { 1, 1 }, { 0, 2 }, { 0, 1 } };

	public static MapLocation addTowerOffset(int[] off, int sway,
			MapLocation tower, Direction dir) throws Exception {
		int[] dirOff = directionToOffset[dir.ordinal()];

		if (dir.isDiagonal()) {
			int x = dirOff[0] * (sway > 0 ? off[0] : off[1]);
			int y = dirOff[1] * (sway > 0 ? off[1] : off[0]);
			return add(tower, x, y);
		} else {
			int x = dirOff[1] * (off[0] * sway) + dirOff[0] * off[1];
			int y = dirOff[0] * (off[0] * sway) + dirOff[1] * off[1];
			return add(tower, x, y);
		}
	}

	public static int towerCanGoHere(MapLocation loc) throws Exception {
		if (!rc.canSenseSquare(loc))
			return -2;
		TerrainTile tt = rc.senseTerrainTile(loc);
		if (tt == TerrainTile.OFF_MAP)
			return -1;
		if (!tt.isTraversableAtHeight(RobotLevel.ON_GROUND))
			return 0;
		return 1;
	}

	public static MapLocation getNextTower(MapLocation tower, Direction dir)
			throws Exception {
		for (int[] a : (dir.isDiagonal() ? nextTowerOffsetsNorthEast
				: nextTowerOffsetsNorth)) {
			boolean swayCounts = dir.isDiagonal() ? (a[0] != a[1])
					: (a[0] != 0);
			int sway = rand.nextBoolean() ? 1 : -1;
			for (int i = 0; i < (swayCounts ? 2 : 1); i++) {
				sway *= -1;
				MapLocation loc = addTowerOffset(a, sway, tower, dir);
				int ret = towerCanGoHere(loc);
				if (ret == 1) {
					return loc;
				}
				if (ret == -1) {
					return null;
				}
				if (ret == -2)
					return null;
			}
		}
		return null;
	}

	// ////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////

	public RobotPlayer(RobotController rc) throws Exception {
		RobotPlayer.rc = rc;
		r = rc.getRobot();
		id = r.getID();

		rand = new Random((id + 7919) * 6829);
		rand.nextDouble();
		rand.nextDouble();
		rand.nextDouble();

		team = rc.getTeam();
		type = rc.getRobotType();
		here = rc.getLocation();
		facing = rc.getDirection();
		round = Clock.getRoundNum();
		maxEnergon = rc.getMaxEnergonLevel();
		maxFlux = type.maxFlux();
		birthRound = round;
		age = 0;
		archons = rc.senseAlliedArchons();
		isArchon = type == RobotType.ARCHON;
		if (isArchon) {
			// get direction to enemy
			MapLocation toHere = here;
			if (rc.senseTerrainTile(add(here, 0, -6)) == TerrainTile.OFF_MAP) {
				toHere = add(toHere, 0, 6);
			}
			if (rc.senseTerrainTile(add(here, 6, 0)) == TerrainTile.OFF_MAP) {
				toHere = add(toHere, -6, 0);
			}
			if (rc.senseTerrainTile(add(here, 0, 6)) == TerrainTile.OFF_MAP) {
				toHere = add(toHere, 0, -6);
			}
			if (rc.senseTerrainTile(add(here, -6, 0)) == TerrainTile.OFF_MAP) {
				toHere = add(toHere, 6, 0);
			}
			originalEnemyDir = here.directionTo(toHere);
		}

		if (!isArchon && !isWout) {
			int bestIndex = -1;
			int bestDist = Integer.MAX_VALUE;
			for (int i = 0; i < archons.length; i++) {
				int dist = here.distanceSquaredTo(archons[i]);
				if (dist < bestDist) {
					bestDist = dist;
					bestIndex = i;
				}
			}
			parentIndex = bestIndex;
			prevArchonsAlive = archons.length;
			parentLoc = archons[parentIndex];
		}
	}

	public void run() {
		while (true) {
			try {
				round = Clock.getRoundNum();
				age = round - birthRound;
				here = rc.getLocation();
				facing = rc.getDirection();
				energon = rc.getEnergonLevel();
				archons = rc.senseAlliedArchons();
				eventualEnergon = rc.getEventualEnergonLevel();
				canMove = !rc.isMovementActive();
				canAttack = !rc.isAttackActive();
				flux = rc.getFlux();
				isWout = type == RobotType.WOUT;
				isSoldier = type == RobotType.SOLDIER;
				reserve = rc.getEnergonReserve();
				nearestArchon = getNearestArchon();

				// keep track of parent
				if (!isArchon && !isWout) {
					if (parentLoc != null) {
						if (archons.length < prevArchonsAlive) {
							int bestIndex = -1;
							int bestDist = 3;
							for (int i = 0; i < archons.length; i++) {
								int dist = parentLoc
										.distanceSquaredTo(archons[i]);
								if (dist < bestDist) {
									bestDist = dist;
									bestIndex = i;
								}
							}
							parentIndex = bestIndex;
							prevArchonsAlive = archons.length;
							if (parentIndex == -1) {
								parentLoc = null;
							}
						} else {
							parentLoc = archons[parentIndex];
						}
					}
				}

				// count children
				if (isArchon) {
					if (round % 10 == 1) {
						Arrays.fill(childrenCounts, 0);
						for (Robot r : rc.senseNearbyGroundRobots()) {
							if (childrenIds.contains(r.getID())) {
								RobotInfo ri = rc.senseRobotInfo(r);
								childrenCounts[ri.type.ordinal()]++;
							}
						}
					}
				}

				ai();

			} catch (Exception e) {
				e.printStackTrace();
			}
			rc.yield();
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////

	public static int woutState = 0;
	public static MapLocation bestFluxLoc;
	public static double bestFluxAmount;

	public static Direction archonDir;

	public static void ai() throws Exception {
		// buildings
		if (type.isBuilding()) {
			return;
		}

		// /////////////////////////////////////////////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////
		// Energon
		// /////////////////////////////////////////////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////

		if (!isWout) {
			RobotInfo weakestRI = null;
			double weakest = (eventualEnergon / maxEnergon) - 0.05;

			RobotInfo lowestReserveRI = null;
			double lowestReserve = Double.MAX_VALUE;

			for (int i = 0; i < 2; i++) {
				for (Robot r : (i == 0) ? rc.senseNearbyGroundRobots() : rc
						.senseNearbyAirRobots()) {
					RobotInfo ri = rc.senseRobotInfo(r);
					if (here.distanceSquaredTo(ri.location) <= 2) {
						if ((ri.team == team) && !ri.type.isBuilding()) {
							double e = ri.eventualEnergon / ri.maxEnergon;
							if (e < weakest) {
								weakest = e;
								weakestRI = ri;
							}
							e = ri.energonReserve;
							if (e < lowestReserve) {
								lowestReserve = e;
								lowestReserveRI = ri;
							}
						}
					}
				}
			}
			if (energon > 1.0 && weakestRI != null) {
				double amount = Math.min(1, GameConstants.ENERGON_RESERVE_SIZE
						- weakestRI.energonReserve);
				rc.transferUnitEnergon(amount, weakestRI.location,
						weakestRI.type.isAirborne() ? RobotLevel.IN_AIR
								: RobotLevel.ON_GROUND);
				energon -= amount;
			}
			if (lowestReserveRI != null && (maxEnergon - energon < 2.0)) {
				double amount = Math.min(2, GameConstants.ENERGON_RESERVE_SIZE
						- lowestReserveRI.energonReserve);
				rc.transferUnitEnergon(amount, lowestReserveRI.location,
						lowestReserveRI.type.isAirborne() ? RobotLevel.IN_AIR
								: RobotLevel.ON_GROUND);
				energon -= amount;
			}
		}

		// /////////////////////////////////////////////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////
		// Flux
		// /////////////////////////////////////////////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////

		if (isArchon || isWout) {
			RobotInfo bestTower = null;
			double bestTowerFlux = Double.MAX_VALUE;

			for (Robot r : rc.senseNearbyGroundRobots()) {
				RobotInfo ri = rc.senseRobotInfo(r);
				if (here.distanceSquaredTo(ri.location) <= 2) {
					if ((ri.team == team) && ri.type.isBuilding()) {
						double flux = ri.flux;
						if (flux < bestTowerFlux) {
							bestTowerFlux = flux;
							bestTower = ri;
						}
					}
				}
			}

			if (bestTower != null) {
				double amount = flux;
				double theyCanTake = (GameConstants.ENERGON_RESERVE_SIZE - bestTower.energonReserve)
						* GameConstants.ENERGON_TO_FLUX_CONVERSION;
				if (amount > theyCanTake)
					amount = theyCanTake;
				rc.transferFlux(amount, bestTower.location,
						RobotLevel.ON_GROUND);
				flux -= amount;
			}

			if (isWout) {
				int dist = here.distanceSquaredTo(nearestArchon);
				if (dist <= 2) {
					RobotInfo ri = rc.senseRobotInfo(rc
							.senseAirRobotAtLocation(nearestArchon));
					double amount = flux;
					double theyCanTake = ri.type.maxFlux() - ri.flux;
					if (amount > theyCanTake)
						amount = theyCanTake;
					rc.transferFlux(amount, nearestArchon, RobotLevel.IN_AIR);
					flux -= amount;
				}
			}
		}

		// /////////////////////////////////////////////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////
		// Archon
		// /////////////////////////////////////////////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////

		if (isArchon) {

			if (!canMove)
				return;

			// we gave birth recently, so let's not do much
			if (round < lastBirth + 30)
				return;

			// birthing -- we want to make something special...
			if (energon > 60) {
				if (rand.nextBoolean()) {
					if (childrenCounts[RobotType.WOUT.ordinal()] < 1) {
						if (tryBuild(RobotType.WOUT))
							return;
					}
				} else {
					if (childrenCounts[RobotType.SOLDIER.ordinal()] < 2) {
						if (tryBuild(RobotType.SOLDIER))
							return;
					}
				}
			}

			// not doing anything else, let's go in our "direction"
			if (archonDir == null) {
				int i = getArchonIndex();
				if (i < 2) {
					archonDir = originalEnemyDir.rotateLeft();
				} else if (i < 4) {
					archonDir = originalEnemyDir;
				} else {
					archonDir = originalEnemyDir.rotateRight();
				}
			}
			if (rand.nextDouble() < 0.5) {
				if (rand.nextBoolean()) {
					archonDir = archonDir.rotateLeft();
				} else {
					archonDir = archonDir.rotateRight();
				}
			}
			if (tryMove(archonDir))
				return;
		}

		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////
		// Soldier
		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////

		if (isSoldier) {
			RobotInfo ri = nearestEnemy();
			if (ri != null) {
				if (attack(ri))
					return;
				if (canMove) {
					int dist = here.distanceSquaredTo(ri.location);
					if (dist > 2) {
						if (bugTo(ri.location))
							return;
					} else {
						if (face(ri.location))
							return;
					}
				}
			} else {
				if (canMove) {
					int dist = here.distanceSquaredTo(parentLoc);
					if (dist <= 2) {
						if (rand.nextInt(5) == 0) {
							if (tryMove(randomDir()))
								return;
						} else {
							rc.setDirection(facing.opposite());
							return;
						}
					} else {
						if (bugTo(parentLoc)) {
							return;
						}
					}
				}
			}
		}

		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////
		// Wout
		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////

		if (isWout) {
			// build a tower if we're about to die
			if (flux > 3000) {
				if (tryBuild(RobotType.TELEPORTER))
					return;
			}

			// where is the best flux?
			if (woutState == 1) {
				if ((bestFluxLoc != null) && rc.canSenseSquare(bestFluxLoc)) {
					bestFluxAmount = rc.senseFluxAtLocation(bestFluxLoc);
				}
				if ((bestFluxLoc == null)
						|| rc.getRoundsUntilMovementIdle() == 1) {
					Direction dir = facing;
					for (int i = 0; i < 8; i++, dir = dir.rotateRight()) {
						MapLocation loc = add(here, dir, 3);
						double f = rc.senseFluxAtLocation(loc);
						if (f > bestFluxAmount) {
							bestFluxAmount = f;
							bestFluxLoc = loc;
						}
					}
				}
			}

			if (!canMove)
				return;

			// state transitions
			if (woutState == 0) {
				// are we healthy enough?
				if (energon + reserve > 25) {
					woutState = 1;
				}
			} else if (woutState == 1) {
				// do we have enough flux?
				if (flux > 3000) {
					woutState = 0;
				}
			}

			if (woutState == 0) {
				if (bugTo(nearestArchon))
					return;
			} else if (woutState == 1) {
				if (bugTo(bestFluxLoc))
					return;
			}
		}
	}
}
