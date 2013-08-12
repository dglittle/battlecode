package team235;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
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

public class RobotPlayer implements Runnable {

	public static int id;
	public static Team team;
	public static MapLocation here;
	public static Direction facing;
	public static int round;
	public static RobotController rc;
	public static Robot r;
	public static RobotInfo ri;
	public static RobotType type;
	public static boolean isArchon;
	public static boolean isWout;
	public static boolean isSoldier;
	public static boolean isTele;
	public static double energon;
	public static double eventualEnergon;
	public static double maxEnergon;
	public static double reserve;
	public static MapLocation[] archons;
	public static int archonIndex;
	public static int birthRound;
	public static int age;
	public static Random rand;
	public static double flux;
	public static double maxFlux;
	public static boolean canMove;
	public static boolean canAttack;
	public static MapLocation badness;
	public static double badnessSize;
	public static int badnessRound;
	public static MapLocation unarmedTower;
	public static int unarmedTowerRound;
	public static int lastFunTeleport = -100;
	public static int lastFunTeleportRequest = -100;
	public static int fleeTillRound = 0;

	// turrets
	public static MapLocation target;
	public static boolean targetInAir;
	public static int bestTargetDist;

	// map extents
	public static int xMin;
	public static int xMax;
	public static int yMin;
	public static int yMax;

	// non-archons only
	public static int parentIndex = -1;
	public static int prevArchonsAlive;
	public static MapLocation parentLoc;

	// archons only
	public static int originalArchonIndex = -1;
	public static Direction originalEnemyDir;
	public static Set<Integer> children = new HashSet();
	public static int[] childrenCounts = new int[RobotType.values().length];
	public static int birthDoneRound = 0;

	// message types
	public static final int message_OED = 1;
	public static final int message_OWN = 2;
	public static final int message_TELE_LOC = 3;
	public static final int message_MAP_EXTENTS = 4;
	public static final int message_NEXT_TOWER = 5;
	public static final int message_BEAM_ME_UP = 6;
	public static final int message_BADNESS = 7;
	public static final int message_KILL_TOWER = 8;
	public static final int message_BEST_CAMP = 9;
	public static final int message_TELE_FAR = 10;
	public static final int message_BREAK_CAMP = 11;

	public static final int magic = 1528675437;

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

	public static int hash(int hash, int[] ints) {
		for (int i = 0; i < ints.length; i++) {
			hash = (hash + 100207) * (ints[i] + 75403);
		}
		return hash;
	}

	public static void sendMessage(int a, int b, int c, int d, int e, int f)
			throws Exception {
		Message m = new Message();
		m.ints = new int[] { a, b, c, d, e, f, round, magic - a + e,
				magic - b + f, magic - c, magic - d, magic - round };
		rc.broadcast(m);
	}

	public static MapLocation add(MapLocation loc, int x, int y) {
		return new MapLocation(loc.getX() + x, loc.getY() + y);
	}

	public static Direction[] dirs = new Direction[] { Direction.NORTH,
			Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
			Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
			Direction.NORTH_WEST };

	public static Direction[] orthDirs = new Direction[] { Direction.NORTH,
			Direction.EAST, Direction.SOUTH, Direction.WEST, };

	// [java] [A:ARCHON#53@176]d: NORTH = 0
	// [java] [A:ARCHON#53@176]d: NORTH_EAST = 1
	// [java] [A:ARCHON#53@176]d: EAST = 2
	// [java] [A:ARCHON#53@176]d: SOUTH_EAST = 3
	// [java] [A:ARCHON#53@176]d: SOUTH = 4
	// [java] [A:ARCHON#53@176]d: SOUTH_WEST = 5
	// [java] [A:ARCHON#53@176]d: WEST = 6
	// [java] [A:ARCHON#53@176]d: NORTH_WEST = 7
	// [java] [A:ARCHON#53@176]d: NONE = 8
	// [java] [A:ARCHON#53@176]d: OMNI = 9

	public static int[][] directionToOffset = new int[][] { { 0, -1 },
			{ 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 },
			{ -1, -1 }, { 0, 0 }, { 0, 0 } };

	public static MapLocation add(MapLocation loc, Direction d, int times) {
		int[] off = directionToOffset[d.ordinal()];
		return new MapLocation(loc.getX() + off[0] * times, loc.getY() + off[1]
				* times);
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
		if (dist == 0)
			return false;
		if (canMove) {
			Direction dir = here.directionTo(loc);

			if (tryMove(dir))
				return true;

			Direction left = dir.rotateLeft().rotateLeft();
			Direction right = dir.rotateRight().rotateRight();

			MapLocation there = here.add(left);
			if (there.distanceSquaredTo(loc) < dist) {
				if (move(left))
					return true;
			}

			there = here.add(right);
			if (there.distanceSquaredTo(loc) < dist) {
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
			if (facing != dir) {
				rc.setDirection(dir);
				return true;
			}
			build(type);
			return true;
		}
		return false;
	}

	public static int getArchonIndex() throws Exception {

		// work here
		rc.setIndicatorString(0, "got here.");

		for (int i = 0; i < archons.length; i++) {
			if (archons[i].equals(here)) {

				// work here
				rc.setIndicatorString(0, "end here.");

				return i;
			}
		}

		// work here
		System.out.println("archons: " + archons.length);

		throw new Exception("no archon index?");
	}

	public static boolean dirNear(int a, int b) {
		int diff = (a - b) % 8;
		return diff == -1 || diff == 0 || diff == 1 || diff == 7;
	}

	public static void build(RobotType type) throws Exception {
		rc.spawn(type);
		rc.yield();
		MapLocation front = here.add(facing);
		Robot r = rc.senseGroundRobotAtLocation(front);
		if (r != null) {
			int childId = r.getID();
			if (type != RobotType.WOUT && !type.isBuilding()) {
				children.add(childId);
				childrenCounts[type.ordinal()]++;
				sendMessage(message_OWN, childId, archonIndex, 0, 0, 0);
			}
			birthDoneRound = round + (type.isBuilding() ? 5 : 30);
		}
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
		if (facing != dir && dir != Direction.OMNI) {
			rc.setDirection(dir);
			return true;
		}
		return false;
	}

	public static boolean face(MapLocation loc) throws Exception {
		return face(here.directionTo(loc));
	}

	public static boolean seekAndDestroy(MapLocation loc) throws Exception {
		RobotInfo ri = nearestEnemy();
		if (ri != null) {
			if (attack(ri))
				return true;
			int dist = here.distanceSquaredTo(ri.location);
			if (canMove) {
				if (dist > 2) {
					if (tryMove(ri.location))
						return true;
				} else {
					if (face(ri.location))
						return true;
				}
			}
			return false;
		} else {
			if (loc != null) {
				if (canMove) {
					if (tryMove(loc))
						return true;
				}
			}
			return false;
		}
	}

	public static Direction randomDir_prevValue = null;

	public static Direction randomDir() {
		if (randomDir_prevValue != null) {
			Direction d = randomDir_prevValue;
			randomDir_prevValue = null;
			return d;
		} else {
			randomDir_prevValue = Direction.values()[rand.nextInt(8)];
			return randomDir_prevValue;
		}
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

	public static MapLocation sub(MapLocation a, MapLocation b) {
		return new MapLocation(a.getX() - b.getX(), a.getY() - b.getY());
	}

	public static MapLocation bugGoal;
	public static int bugging;
	public static int bugDistToBeat;
	public static Direction bugDir;

	public static boolean bugUpTo(MapLocation goal) throws Exception {
		if (!canMove)
			return false;

		int dist = here.distanceSquaredTo(goal);
		if (dist <= 2) {
			if (dist != 0) {
				return face(goal);
			}
			return false;
		} else {
			return bugTo(goal);
		}
	}

	public static boolean bugTo(MapLocation goal) throws Exception {
		if (!canMove)
			return false;

		int dist = here.distanceSquaredTo(goal);
		if (dist == 0) {
			return false;
		}

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

	public static void teleport(Robot r, RobotLevel lvl, MapLocation tele,
			MapLocation dest) throws Exception {
		int dist = dest.distanceSquaredTo(tele);
		if (dist > 9) {
			Direction d = tele.directionTo(dest);
			dest = add(tele, d, d.isDiagonal() ? 2 : 3);
		}
		if (rc.canTeleport(tele, dest, lvl)) {
			rc.teleport(r, tele, dest);
			return;
		}
		int left = dest.getX() - 1;
		int right = dest.getX() + 1;
		int top = dest.getY() - 1;
		int bottom = dest.getY() + 1;
		for (int i = 0; i < 20; i++) {
			if (left < tele.getX() - 3)
				left = tele.getX() - 3;
			if (right > tele.getX() + 3)
				right = tele.getX() + 3;
			if (top < tele.getY() - 3)
				top = tele.getY() - 3;
			if (bottom > tele.getY() + 3)
				bottom = tele.getY() + 3;

			int x = rand.nextInt(right - left + 1) + left;
			int y = rand.nextInt(bottom - top + 1) + bottom;
			dest = new MapLocation(x, y);

			if (tele.distanceSquaredTo(dest) > 9) {
				i--;
				continue;
			}

			if (rc.canTeleport(tele, dest, lvl)) {
				rc.teleport(r, tele, dest);
				return;
			}

			if (i % 3 == 0) {
				left--;
				right++;
				top--;
				bottom++;
			}
		}
	}

	public static void teleport(Robot r, RobotLevel lvl, MapLocation dest)
			throws Exception {
		MapLocation bestTele = null;
		int best = Integer.MAX_VALUE;
		for (MapLocation tele : rc.senseAlliedTeleporters()) {
			int dist = dest.distanceSquaredTo(tele);
			if (dist < best) {
				best = dist;
				bestTele = tele;
			}
		}
		if (bestTele != null) {
			teleport(r, lvl, bestTele, dest);
		}
	}

	public static void teleport(Robot r, RobotLevel lvl) throws Exception {
		MapLocation[] ts = rc.senseAlliedTeleporters();
		if (ts.length == 1)
			return;
		for (int i = 0; i < 10; i++) {
			MapLocation t = ts[rand.nextInt(ts.length)];
			if (!t.equals(here)) {
				teleport(r, lvl, t, add(t, randomDir(), 3));
			}
		}
	}

	public static Direction getAwayFromOtherArchons() {
		Direction bestDir = null;
		double best = Double.MAX_VALUE;
		for (Direction d : dirs) {
			MapLocation loc = here.add(d);
			double dist = 0;
			for (MapLocation arch : archons) {
				if (!arch.equals(here)) {
					dist += 1.0 / loc.distanceSquaredTo(arch);
				}
			}
			for (Direction dd : orthDirs) {
				MapLocation tile = add(here, dd, 6);
				TerrainTile tt = rc.senseTerrainTile(tile);
				if (tt == TerrainTile.OFF_MAP) {
					dist += 1.0 / loc.distanceSquaredTo(tile);
				}
			}
			if (dist < best) {
				best = dist;
				bestDir = d;
			}
		}
		return bestDir;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////

	public RobotPlayer(RobotController rc) throws Exception {
		RobotPlayer.rc = rc;
		r = rc.getRobot();
		id = r.getID();

		rand = new Random((long) Math.pow(id + 7919, 6829));
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
			archonIndex = getArchonIndex();

			// get original archon index
			originalArchonIndex = archonIndex;

			// get direction to enemy
			MapLocation toHere = here;
			if (rc.senseTerrainTile(add(here, 0, -6)) == TerrainTile.OFF_MAP) {
				toHere = add(toHere, 0, 100);
			}
			if (rc.senseTerrainTile(add(here, 6, 0)) == TerrainTile.OFF_MAP) {
				toHere = add(toHere, -100, 0);
			}
			if (rc.senseTerrainTile(add(here, 0, 6)) == TerrainTile.OFF_MAP) {
				toHere = add(toHere, 0, -100);
			}
			if (rc.senseTerrainTile(add(here, -6, 0)) == TerrainTile.OFF_MAP) {
				toHere = add(toHere, 100, 0);
			}
			if (toHere.equals(here)) {
				originalEnemyDir = Direction.NORTH;
			} else {
				originalEnemyDir = here.directionTo(toHere);
			}
			sendMessage(message_OED, originalEnemyDir.ordinal(), 0, 0, 0, 0);
		}

		xMin = here.getX();
		xMax = here.getX();
		yMin = here.getY();
		yMax = here.getY();
	}

	public static Set<Integer> receivedTargets = new HashSet();

	public void run() {
		while (true) {
			try {
				ri = rc.senseRobotInfo(r);
				round = Clock.getRoundNum();
				age = round - birthRound;
				here = rc.getLocation();
				facing = rc.getDirection();
				energon = rc.getEnergonLevel();
				MapLocation[] newArchons = rc.senseAlliedArchons();
				if (isArchon && (newArchons.length != archons.length)) {
					archons = newArchons;
					archonIndex = getArchonIndex();
				} else {
					archons = newArchons;
				}
				eventualEnergon = rc.getEventualEnergonLevel();
				canMove = !rc.isMovementActive();
				canAttack = !rc.isAttackActive();
				flux = rc.getFlux();
				isWout = type == RobotType.WOUT;
				isSoldier = type == RobotType.SOLDIER;
				isTele = type == RobotType.TELEPORTER;
				reserve = rc.getEnergonReserve();

				if (type == RobotType.TURRET) {
					target = null;
					bestTargetDist = Integer.MAX_VALUE;
				}

				for (Message m : rc.getAllMessages()) {

					if (type == RobotType.TURRET) {
						if (m.ints != null && m.ints.length == 2
								&& m.locations != null
								&& m.locations.length < 144) {
							if (m.ints[0] + 552 == m.ints[1]) {
								int roundSent = m.ints[0] % 10000;
								if (roundSent >= round - 2
										&& roundSent <= round) {
									if (receivedTargets.add(m.ints[0])) {
										boolean inAir = false;
										for (MapLocation loc : m.locations) {
											if (loc == null) {
												inAir = true;
												continue;
											}
											int dist = here
													.distanceSquaredTo(loc);
											if (inAir)
												dist += 50;
											if (dist < type
													.attackRadiusMinSquared())
												continue;
											if (rc.canAttackSquare(loc)) {
												dist -= 1000;
											}
											if (dist < bestTargetDist) {
												bestTargetDist = dist;
												target = loc;
												targetInAir = inAir;
											}
										}
									}
								}
							}
						}
					}

					// 33 bcs
					if (m.locations != null)
						continue;
					if (m.strings != null)
						continue;
					int[] ints = m.ints;
					if (ints == null)
						continue;
					if (ints.length != 12)
						continue;
					int roundSent = ints[6];
					if (round > roundSent + 3)
						continue;
					if (roundSent > round)
						continue;

					if (ints[7] != magic - ints[0] + ints[4])
						continue;
					if (ints[8] != magic - ints[1] + ints[5])
						continue;
					if (ints[9] != magic - ints[2])
						continue;
					if (ints[10] != magic - ints[3])
						continue;
					if (ints[11] != magic - ints[6])
						continue;

					// 50 bcs
					// int hash = hash(expires, ints);
					// if (hash != loc.getY())
					// continue;

					switch (ints[0]) {
					case message_OED:
						if (isArchon) {
							Direction dir = Direction.values()[ints[1]];
							if (dir.isDiagonal()
									|| (originalEnemyDir == Direction.NORTH)) {
								originalEnemyDir = dir;
							}
						}
						break;
					case message_OWN:
						if (id == ints[1]) {
							parentIndex = ints[2];
							prevArchonsAlive = archons.length;
							parentLoc = archons[parentIndex];
						}
						break;
					case message_TELE_LOC:
						if (isArchon || isWout) {
							MapLocation pos = new MapLocation(ints[1], ints[2]);
							int round = ints[3];
							if ((nearestTele == null)
									|| (nearestTeleRound < round - 50)
									|| pos.distanceSquaredTo(here) < here
											.distanceSquaredTo(nearestTele)) {
								nearestTele = pos;
								nearestTeleRound = round;
							}
						}
						break;
					case message_MAP_EXTENTS:
						if (isTele || isWout) {
							if (ints[1] < xMin)
								xMin = ints[1];
							if (ints[2] > xMax)
								xMax = ints[2];
							if (ints[3] < yMin)
								yMin = ints[3];
							if (ints[4] > yMax)
								yMax = ints[4];
						}
						break;
					case message_NEXT_TOWER:
						if (isArchon || isWout) {
							int round = ints[4];
							MapLocation pt = new MapLocation(ints[1], ints[2]);
							if ((prevTower == null)
									|| (here.distanceSquaredTo(pt) < here
											.distanceSquaredTo(prevTower))
									|| (round > prevTowerRound + 120)) {
								Direction td = Direction.values()[ints[3]];
								if ((prevTower == null)
										|| !pt.equals(prevTower)
										|| !td.equals(towerDir)) {
									prevTower = pt;
									towerDir = td;
									nextTower = null;
								}
								prevTowerRound = round;
							}
						}
						break;
					case message_BEAM_ME_UP:
						if (isTele) {
							if (flux > GameConstants.TELEPORT_FLUX_COST) {
								MapLocation src = new MapLocation(ints[1],
										ints[2]);
								if (rc.canSenseSquare(src)) {
									int id = ints[5];
									RobotLevel lvl = RobotLevel.ON_GROUND;
									Robot r = rc
											.senseGroundRobotAtLocation(src);
									if ((r == null) || (r.getID() != id)) {
										r = rc.senseAirRobotAtLocation(src);
										lvl = RobotLevel.IN_AIR;
									}
									if ((r != null) && (r.getID() == id)) {
										RobotInfo ri = rc.senseRobotInfo(r);
										if (!ri.teleporting) {
											teleport(r, lvl, new MapLocation(
													ints[3], ints[4]));
										}
									}
								}
							}
						}
						break;
					case message_TELE_FAR: {
						if (isTele) {
							if (flux > GameConstants.TELEPORT_FLUX_COST) {
								MapLocation src = new MapLocation(ints[1],
										ints[2]);
								if (rc.canSenseSquare(src)) {
									int id = ints[3];
									RobotLevel lvl = RobotLevel.ON_GROUND;
									Robot r = rc
											.senseGroundRobotAtLocation(src);
									if ((r == null) || (r.getID() != id)) {
										r = rc.senseAirRobotAtLocation(src);
										lvl = RobotLevel.IN_AIR;
									}
									if ((r != null) && (r.getID() == id)) {
										RobotInfo ri = rc.senseRobotInfo(r);
										if (!ri.teleporting) {
											teleport(r, lvl);
										}
									}
								}
							}
						}
						break;
					}
					case message_BADNESS: {
						MapLocation pos = new MapLocation(ints[1], ints[2]);
						double size = (double) ints[3] / 100;
						int round = ints[4];
						if ((badness == null)
								|| (here.distanceSquaredTo(pos) < here
										.distanceSquaredTo(badness))
								|| (badnessRound < round - 50)) {
							badness = pos;
							badnessRound = round;
							badnessSize = size;
						}
						break;
					}
					case message_KILL_TOWER: {
						MapLocation pos = new MapLocation(ints[1], ints[2]);
						int round = ints[3];
						if ((unarmedTower == null)
								|| (here.distanceSquaredTo(pos) < here
										.distanceSquaredTo(unarmedTower))
								|| (unarmedTowerRound < round - 50)) {
							unarmedTower = pos;
							unarmedTowerRound = round;
						}
						break;
					}
					case message_BEST_CAMP: {
						if (isArchon) {
							MapLocation pos = new MapLocation(ints[1], ints[2]);
							double score = (double) ints[3] / 100;
							int id = ints[4];
							if (score > bestCampScore
									|| (score == bestCampScore && id > bestCampId)) {
								bestCampScore = score;
								bestCamp = pos;
							}
						}
						break;
					}
					case message_BREAK_CAMP: {
						if (type == RobotType.TURRET) {
							rc.transform(RobotType.CHAINER);
							type = RobotType.CHAINER;
							rc.yield();
							continue;
						}
						break;
					}
					}
				}

				// update map extents
				if (isWout || isArchon || isTele) {
					if (rand.nextInt(10) == 0) {
						for (MapLocation pos : archons) {
							int x = pos.getX();
							int y = pos.getY();
							if (x < xMin)
								xMin = x;
							if (x > xMax)
								xMax = x;
							if (y < yMin)
								yMin = y;
							if (y > yMax)
								yMax = y;
						}
						for (Direction d : orthDirs) {
							for (int i = type.sensorRadius(); i >= 1; i--) {
								MapLocation pos = add(here, d, i);
								TerrainTile tt = rc.senseTerrainTile(pos);
								if (tt == TerrainTile.OFF_MAP) {
								} else {
									int x = pos.getX();
									int y = pos.getY();
									if (x < xMin)
										xMin = x;
									if (x > xMax)
										xMax = x;
									if (y < yMin)
										yMin = y;
									if (y > yMax)
										yMax = y;
									break;
								}
							}
						}
					}
				}

				// keep track of parent
				if (!isArchon) {
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
							int id = r.getID();
							if (children.contains(id)) {
								childrenCounts[rc.senseRobotInfo(r).type
										.ordinal()]++;
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

	public static int prevTowerRound = 0;

	public static int nearestTeleRound = 0;
	public static MapLocation nearestTele = null;

	// wouts only
	public static int woutState = 0;
	public static MapLocation bestFluxLoc;
	public static double bestFluxAmount = -1;

	// archon only
	public static int moveAwayFromArchonsUntilRound = 600;

	// teles only
	public static int teleCount = 0;

	public static MapLocation oldBestCamp = null;
	public static int campTillRound = 600;
	public static boolean changedToNotCamping = false;

	public static void ai() throws Exception {

		// work here
		if (false) {
			rc.setIndicatorString(0, "next: "
					+ (prevTower != null ? sub(prevTower, here) : "null"));
			rc.setIndicatorString(1, "dir: " + towerDir);
		}
		if (false) {
			String ext = "[" + (xMin - here.getX()) + ", "
					+ (xMax - here.getX()) + "] - [" + (yMin - here.getY())
					+ ", " + (yMax - here.getY()) + "]";
			rc.setIndicatorString(0, "ext: " + ext);
		}

		MapLocation mommy = null;
		if (isWout) {
			mommy = getNearestArchon();
		}

		// max extents update
		if (rand.nextInt(20) == 0) {
			if (!rc.hasBroadcastMessage()) {
				sendMessage(message_MAP_EXTENTS, xMin, xMax, yMin, yMax, 0);
			}
		}

		// update prev tower to default location
		if (round > 200 && prevTower == null) {
			prevTower = archons[0];
			towerDir = towardExpanse(prevTower);
		}

		// tele update
		if (isTele || isArchon || isWout) {
			if (rand.nextInt(10) == 0) {
				if (!rc.hasBroadcastMessage()) {
					if (isArchon || isWout) {
						if (nearestTele != null) {
							sendMessage(message_TELE_LOC, nearestTele.getX(),
									nearestTele.getY(), nearestTeleRound, 0, 0);
						}
					} else {
						sendMessage(message_TELE_LOC, here.getX(), here.getY(),
								round, 0, 0);
					}
				}
			}
		}

		// buildings
		if (type.isBuilding()) {

			if (isTele) {
				MapLocation[] a = rc.senseAlliedTeleporters();
				if (age > 10
						&& (teleCount != a.length || rand.nextInt(30) == 0)) {
					teleCount = a.length;
					updatePrevTower(rc.senseAlliedTeleporters());
				}

				if (prevTower != null) {
					if (rand.nextInt(10) == 0) {
						if (!rc.hasBroadcastMessage()) {
							sendMessage(message_NEXT_TOWER, prevTower.getX(),
									prevTower.getY(), towerDir.ordinal(),
									round, 0);
						}
					}
				}
			}

			return;
		}

		// no parent? maybe they don't have one yet? or maybe they've lost one
		// :(
		if (!isArchon && !isWout) {
			if (parentLoc == null) {
				seekAndDestroy(null);
				return;
			}
		}

		// transfer energon & flux
		RobotInfo nearestLoadedWout = null;
		int nearestLoadedWoutDist = Integer.MAX_VALUE;
		int turretCount = 0;
		double goodnessOutFront = 0;
		double goodness = 0;
		{
			RobotInfo weakestRI = null;
			double weakest = eventualEnergon;

			RobotInfo bestWout = null;
			double bestWoutEnergon = 0;

			RobotInfo lowestReserveRI = ri;
			double lowestReserve = reserve;

			RobotInfo closestEnemySoldiers = null;
			int closestEnemySoldiersDist = Integer.MAX_VALUE;
			double enemySize = 0;

			RobotInfo closestEnemyTower = null;
			int closestEnemyTowerDist = Integer.MAX_VALUE;

			RobotInfo bestTower = null;
			double bestTowerReserve = Double.MAX_VALUE;

			for (int i = 0; i < 2; i++) {
				for (Robot r : (i == 0) ? rc.senseNearbyGroundRobots() : rc
						.senseNearbyAirRobots()) {
					RobotInfo ri = rc.senseRobotInfo(r);
					if (ri.team == team) {

						int dist = here.distanceSquaredTo(ri.location);

						if (ri.type == RobotType.WOUT && ri.flux > 1500) {
							if (dist < nearestLoadedWoutDist) {
								nearestLoadedWoutDist = dist;
								nearestLoadedWout = ri;
							}
						}

						if (ri.type == RobotType.TURRET)
							turretCount++;

						if (dist <= 2) {
							if (!ri.type.isBuilding()) {
								if (ri.energonReserve < 9) {
									double e = ri.eventualEnergon;
									if (ri.type != RobotType.WOUT) {
										if (e < weakest) {
											weakest = e;
											weakestRI = ri;
										}
									} else {
										if (e > bestWoutEnergon) {
											bestWoutEnergon = e;
											bestWout = ri;
										}
									}
									e = ri.energonReserve;
									if (e < lowestReserve) {
										lowestReserve = e;
										lowestReserveRI = ri;
									}
								}
							} else {
								double res = ri.energonReserve;
								if (res < bestTowerReserve) {
									bestTowerReserve = res;
									bestTower = ri;
								}
							}
						}
					} else {
						if (!ri.type.isBuilding()) {
							if (ri.type.canAttackAir()
									&& (ri.type != RobotType.WOUT)) {
								enemySize += ri.type.energonUpkeep();
								int dist = here.distanceSquaredTo(ri.location);
								if (dist < closestEnemySoldiersDist) {
									closestEnemySoldiersDist = dist;
									closestEnemySoldiers = ri;
								}
							}
						} else {
							int dist = here.distanceSquaredTo(ri.location);
							if (dist < closestEnemyTowerDist) {
								closestEnemyTowerDist = dist;
								closestEnemyTower = ri;
							}
						}
					}
				}
			}
			if (energon > 1.0
					&& weakestRI != null
					&& ((!isArchon || (eventualEnergon > 30)) && (type != RobotType.WOUT))) {
				double amount = Math.min(1, GameConstants.ENERGON_RESERVE_SIZE
						- weakestRI.energonReserve);
				rc.transferUnitEnergon(amount, weakestRI.location,
						weakestRI.type.isAirborne() ? RobotLevel.IN_AIR
								: RobotLevel.ON_GROUND);
				energon -= amount;
			}
			if (isArchon) {
				if (bestWout != null) {
					double amount = Math.min(1,
							GameConstants.ENERGON_RESERVE_SIZE
									- bestWout.energonReserve);
					rc.transferUnitEnergon(amount, bestWout.location,
							RobotLevel.ON_GROUND);
					energon -= amount;

					if (flux > (3200 - bestWout.flux)) {
						amount = 3200 - bestWout.flux;
						if (amount > 0) {
							double canTake = RobotType.WOUT.maxFlux()
									- bestWout.flux;
							if (amount > canTake) {
								amount = canTake;
							}
							rc.transferFlux(amount, bestWout.location,
									RobotLevel.ON_GROUND);
							flux -= amount;
						}
					}
				}

				double aboutToLose = energon + GameConstants.ARCHON_PRODUCTION
						+ Math.min(1, reserve) - maxEnergon;
				if (aboutToLose > 0) {
					rc.transferUnitEnergon(aboutToLose,
							lowestReserveRI.location, lowestReserveRI.type
									.isAirborne() ? RobotLevel.IN_AIR
									: RobotLevel.ON_GROUND);
				}
			}

			if (isArchon || isWout) {
				// flux
				if (bestTower != null && bestTower.flux < 100) {
					double amount = flux;
					if (amount > 3200) {
						amount -= 3200;
					}
					double theyCanTake = (GameConstants.ENERGON_RESERVE_SIZE - bestTowerReserve)
							* GameConstants.ENERGON_TO_FLUX_CONVERSION;
					if (amount > theyCanTake)
						amount = theyCanTake;
					rc.transferFlux(amount, bestTower.location,
							RobotLevel.ON_GROUND);
					flux -= amount;
				}

				if (isWout && woutState == 0) {
					int dist = here.distanceSquaredTo(mommy);
					if (dist <= 2) {
						RobotInfo mi = rc.senseRobotInfo(rc
								.senseAirRobotAtLocation(mommy));
						double amount = flux;
						double theyCanTake = RobotType.ARCHON.maxFlux()
								- mi.flux;
						if (amount > theyCanTake)
							amount = theyCanTake;
						rc.transferFlux(amount, mommy, RobotLevel.IN_AIR);
						flux -= amount;
					}
				}
			}

			// enemy soldiers (a badness)
			if (closestEnemySoldiers != null) {
				badness = closestEnemySoldiers.location;
				badnessSize = enemySize;
				badnessRound = round;
			}
			if ((round < badnessRound + 30) && badness != null
					&& rand.nextInt(10) == 0) {
				if (!rc.hasBroadcastMessage()) {
					sendMessage(message_BADNESS, badness.getX(),
							badness.getY(), (int) (badnessSize * 100),
							badnessRound, 0);
				}
			}
			if (isArchon && badness != null) {
				int x1 = badness.getX() - here.getX();
				int y1 = badness.getY() - here.getY();
				for (Robot r : rc.senseNearbyGroundRobots()) {
					RobotInfo ri = rc.senseRobotInfo(r);
					if (ri.team == team && !ri.type.isBuilding()
							&& ri.type != RobotType.WOUT) {
						int x2 = ri.location.getX() - here.getX();
						int y2 = ri.location.getY() - here.getY();
						double e = ri.type.energonUpkeep();
						goodness += e;
						if (x1 * x2 + y1 * y2 >= 0) {
							goodnessOutFront += e;
						}
					}
				}
			}

			// unarmed tower
			if (closestEnemyTower != null && enemySize == 0) {
				unarmedTower = closestEnemyTower.location;
				unarmedTowerRound = round;
			}
			if ((round < unarmedTowerRound + 30) && unarmedTower != null
					&& rand.nextInt(10) == 0) {
				if (!rc.hasBroadcastMessage()) {
					sendMessage(message_KILL_TOWER, unarmedTower.getX(),
							unarmedTower.getY(), unarmedTowerRound, 0, 0);
				}
			}
		}

		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////
		// Archon
		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////

		if (isArchon) {

			boolean isCamping = false; // (round <= campTillRound && archonIndex
			// < 4);

			// if (!isCamping && !changedToNotCamping) {
			// changedToNotCamping = true;
			// sendMessage(message_BREAK_CAMP, 0, 0, 0, 0, 0);
			// birthDoneRound = round + 30;
			// }

			if (isCamping) {
				if (badnessRound > round - 30) {
					if (campTillRound < round + 200) {
						campTillRound = round + 200;
					}
				}

				if (!rc.hasBroadcastMessage()) {
					Message m = new Message();
					ArrayList<MapLocation> a = new ArrayList();
					for (Robot r : rc.senseNearbyGroundRobots()) {
						RobotInfo ri = rc.senseRobotInfo(r);
						if (ri.team != team) {
							a.add(ri.location);
						}
					}
					a.add(null);
					for (Robot r : rc.senseNearbyAirRobots()) {
						RobotInfo ri = rc.senseRobotInfo(r);
						if (ri.team != team) {
							a.add(ri.location);
						}
					}
					if (a.size() > 1) {
						MapLocation[] b = new MapLocation[a.size()];
						for (int i = 0; i < a.size(); i++) {
							b[i] = a.get(i);
						}
						m.locations = b;
						int hash = id * 10000 + round;
						m.ints = new int[] { hash, hash + 552 };
						rc.broadcast(m);
					}
				}

				if (round < 25) {
					updateBestCamp();
				}
				if (oldBestCamp == null || !oldBestCamp.equals(bestCamp)) {
					if (!rc.hasBroadcastMessage()) {
						oldBestCamp = bestCamp;
						sendMessage(message_BEST_CAMP, bestCamp.getX(),
								bestCamp.getY(), (int) (bestCampScore * 100),
								bestCampId, 0);
					}
				}
				if (round > 25 && (rand.nextInt(30) == 0)) {
					if (!rc.hasBroadcastMessage()) {
						sendMessage(message_BEST_CAMP, bestCamp.getX(),
								bestCamp.getY(), (int) (bestCampScore * 100),
								bestCampId, 0);
					}
				}

				if (!canMove)
					return;

				// movement
				MapLocation leader = archons[0];
				if (round < 40) {
					if (here.distanceSquaredTo(leader) <= 36) {
						tryMove(getAwayFromOtherArchons());
					} else {
						tryMove(leader);
					}
					return;
				}
				if (bestCamp == null || here.distanceSquaredTo(leader) > 36) {
					tryMove(leader);
					return;
				}

				MapLocation goal = bestCamp;
				if (archonIndex == 0)
					goal = goal.add(Direction.NORTH_EAST);
				else if (archonIndex == 1)
					goal = goal.add(Direction.SOUTH_EAST).add(Direction.EAST);
				else if (archonIndex == 2)
					goal = goal.add(Direction.SOUTH).add(Direction.SOUTH);
				else if (archonIndex == 3)
					goal = goal.add(Direction.WEST);

				boolean needTurrets = turretCount < (archons.length > 4 ? 4
						: archons.length) * 3;

				if (!here.equals(goal)) {
					if (here.isAdjacentTo(goal)
							&& facing.equals(here.directionTo(goal))) {
						if (needTurrets && energon > 30 && canBuild(facing)) {
							build(RobotType.TURRET);
							return;
						}
					}
					tryMove(goal);
					return;
				}

				if (canBuild(facing)) {
					if (dirNear(facing.ordinal(), archonIndex * 2)) {
						// facing away
						if (!needTurrets && energon > 70) {
							build(RobotType.WOUT);
							return;
						}
					} else {
						// facing inward
						if (needTurrets && energon > 30) {
							build(RobotType.TURRET);
							return;
						}
					}
				}

				if (rand.nextInt(10) == 0) {
					if (needTurrets
							&& energon > 50
							&& rc.senseTerrainTile(here).isTraversableAtHeight(
									RobotLevel.ON_GROUND)) {
						tryMove(facing);
						return;
					}
				}
				face(randomDir());
				return;
			}

			if (!canMove)
				return;

			if (round < fleeTillRound) {
				if (tryMove(fleeDir(badness)))
					return;
			}
			if (badness != null && badnessRound > round - 30
					&& here.distanceSquaredTo(badness) <= 9) {
				if (tryMove(here.directionTo(badness).opposite()))
					return;
			}

			// we gave birth recently, so let's not do much
			if (round < birthDoneRound)
				return;

			// attack?
			// fall back?
			if (badness != null && badnessRound > round - 30) {
				if (round >= fleeTillRound && goodnessOutFront > badnessSize
						&& badness.distanceSquaredTo(here) > 9) {
					if (tryMove(here.directionTo(badness)))
						return;
				}
				if (badnessSize > goodness) {
					fleeTillRound = round + 7 * 6;
				}
			}
			if (round < fleeTillRound) {
				if (tryMove(fleeDir(badness)))
					return;
			}

			// birthing
			if (energon > 70) {
				if (round > 100) {
					// if (archonIndex < archons.length - 2) {
					// if (childrenCounts[RobotType.SOLDIER.ordinal()] < 1) {
					// if (tryBuild(RobotType.SOLDIER))
					// return;
					// }
					// if (childrenCounts[RobotType.CHAINER.ordinal()] < 2) {
					// if (tryBuild(RobotType.CHAINER))
					// return;
					// }
					// } else {
					if (childrenCounts[RobotType.SOLDIER.ordinal()] < 2) {
						if (tryBuild(RobotType.SOLDIER))
							return;
					}
					// }
				}

				if (tryBuild(RobotType.WOUT))
					return;
			}

			if (rand.nextInt(6) == 0) {
				MapLocation otherArchon = getNearestArchon();
				if (here.distanceSquaredTo(otherArchon) <= 36) {
					if (round + 30 > moveAwayFromArchonsUntilRound)
						moveAwayFromArchonsUntilRound = round + 30;
				}
			}
			if (moveAwayFromArchonsUntilRound < round - 50) {
				moveAwayFromArchonsUntilRound = round + 50;
			}

			// this is our mass of guys for attacking...
			if (archonIndex == 0) {
				if (tryMove(driftDir(true)))
					return;
			} else if (archonIndex < archons.length - 2) {
				if (here.isAdjacentTo(archons[0])) {
					if (rand.nextInt(10) == 0)
						if (tryMove(randomDir()))
							return;
				} else {
					if (tryMove(archons[0]))
						return;
				}
			}

			if (unarmedTower != null && unarmedTowerRound > round - 30
					&& here.distanceSquaredTo(unarmedTower) < 81) {
				// we hear reports of an unarmed enemy tower, ripe for the
				// destroying

				// only if we're the closest archon to it, and we have some
				// soldiers
				if (childrenCounts[RobotType.SOLDIER.ordinal()] > 0) {
					boolean bad = false;
					int ourDist = here.distanceSquaredTo(unarmedTower);
					for (MapLocation arch : archons) {
						if (arch.equals(here))
							continue;
						int dist = arch.distanceSquaredTo(unarmedTower);
						if (dist < ourDist) {
							bad = true;
							break;
						}
					}
					if (!bad) {
						if (tryMove(unarmedTower))
							return;
					}
				}
			}
			if (nearestLoadedWout != null) {
				// aww, they're trying to get to mommy
				if (dirNear(nearestLoadedWout.directionFacing.ordinal(),
						nearestLoadedWout.location.directionTo(here).ordinal())) {
					if (tryMove(nearestLoadedWout.location))
						return;
				}
			}

			// if (round < 50) {
			// if (originalArchonIndex == 4) {
			// if (tryMove(originalEnemyDir.rotateLeft()))
			// return;
			// } else if (originalArchonIndex == 5) {
			// if (tryMove(originalEnemyDir.rotateRight()))
			// return;
			// }
			// }

			if (round < moveAwayFromArchonsUntilRound) {
				// move away from other archons
				Direction bestDir = getAwayFromOtherArchons();

				if (tryMove(bestDir))
					return;
			} else {
				// move toward flux

				Direction bestDir = null;
				double best = -1;
				for (Direction d : dirs) {
					MapLocation loc = add(here, d, d.isDiagonal() ? 4 : 6);
					double flux = rc.senseFluxAtLocation(loc);
					if (flux > best) {
						best = flux;
						bestDir = d;
					}
				}

				if (tryMove(bestDir))
					return;
			}
		}

		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////
		// Turret
		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////

		if (type == RobotType.TURRET) {
			if (canMove && !rc.isDeployed()) {
				rc.deploy();
				return;
			}
			if (canAttack && (target != null)) {
				if (rc.canAttackSquare(target)) {
					if (targetInAir) {
						rc.attackAir(target);
					} else {
						rc.attackGround(target);
					}
				}
			}
			if (canMove && (target != null)) {
				if (face(target))
					return;
			}
		}

		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////
		// Turret
		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////

		if (type == RobotType.CHAINER) {
			if (canAttack) {
				MapLocation bestLoc = null;
				int best = 0;
				RobotLevel bestLvl = null;
				for (int lvl = 0; lvl < 2; lvl++) {
					for (int[] off : facing.isDiagonal() ? chainer_diagAttackables
							: chainer_orthAttackables) {
						int x = here.getX();
						int y = here.getY();
						switch (facing) {
						case NORTH:
							x += off[0];
							y -= off[1];
							break;
						case SOUTH:
							x += off[0];
							y += off[1];
							break;
						case EAST:
							x += off[1];
							y += off[0];
							break;
						case WEST:
							x -= off[1];
							y += off[0];
							break;
						case NORTH_EAST:
							x += off[0];
							y -= off[1];
							break;
						case SOUTH_EAST:
							x += off[0];
							y += off[1];
							break;
						case SOUTH_WEST:
							x -= off[0];
							y += off[1];
							break;
						case NORTH_WEST:
							x -= off[0];
							y -= off[1];
							break;
						}
						MapLocation loc = new MapLocation(x, y);
						int score = 0;
						Robot r = lvl == 0 ? rc.senseGroundRobotAtLocation(loc)
								: rc.senseAirRobotAtLocation(loc);
						if (r != null) {
							RobotInfo ri = rc.senseRobotInfo(r);
							if (ri.team != null) {
								score += 1;
							} else {
								score -= 1;
							}
						}
						for (Direction d : dirs) {
							MapLocation there = loc.add(d);
							if (rc.canSenseSquare(there)) {
								Robot ro = lvl == 0 ? rc
										.senseGroundRobotAtLocation(loc) : rc
										.senseAirRobotAtLocation(loc);
								if (ro != null) {
									RobotInfo inf = rc.senseRobotInfo(ro);
									if (inf.team == team) {
										score -= 1;
									} else {
										score += 1;
									}
								}
							}
						}
						if (score > best) {
							best = score;
							bestLoc = loc;
							bestLvl = lvl == 0 ? RobotLevel.ON_GROUND
									: RobotLevel.IN_AIR;
						}
					}
				}

				// for (Robot r : rc.senseNearbyGroundRobots()) {
				// RobotInfo ri = rc.senseRobotInfo(r);
				// if (ri.team != team) {
				// MapLocation loc = ri.location;
				// int score = 1;
				// for (Direction d : dirs) {
				// MapLocation there = loc.add(d);
				// if (rc.canSenseSquare(there)) {
				// Robot ro = rc.senseGroundRobotAtLocation(there);
				// if (ro != null) {
				// RobotInfo inf = rc.senseRobotInfo(ro);
				// if (inf.team == team) {
				// score -= 1;
				// } else {
				// score += 1;
				// }
				// }
				// }
				// }
				// if (score > best) {
				// best = score;
				// bestLoc = loc;
				// }
				// }
				// }
				if (bestLoc != null) {
					if (bestLvl == RobotLevel.IN_AIR) {
						rc.attackAir(bestLoc);
					} else {
						rc.attackGround(bestLoc);
					}
				}
			}
			if (here.distanceSquaredTo(parentLoc) > 2) {
				bugTo(parentLoc.add(driftDir(true)));
			} else if (badness != null) {
				bugTo(badness);
			}
		}

		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////
		// Soldier
		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////

		if (type == RobotType.SOLDIER) {
			RobotInfo ri = nearestEnemy();
			if (ri != null) {
				if (attack(ri))
					return;
				int dist = here.distanceSquaredTo(ri.location);
				if (canMove) {
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
							if (tryMove(Direction.values()[rand.nextInt(8)]))
								return;
						} else {
							rc.setDirection(facing.opposite());
							return;
						}
					} else {
						if (bugTo(parentLoc))
							return;
					}
				}
			}
		}

		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////
		// Wout
		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////

		if (type == RobotType.WOUT) {
			// desperation...
			if (eventualEnergon < 5 * 0.15) {
				if (mommy.distanceSquaredTo(here) > 2) {
					if (flux >= 3000) {
						if (tryBuild(RobotType.TELEPORTER))
							return;
					}
				}
			}

			// where is the best flux?
			if ((bestFluxLoc != null) && rc.canSenseSquare(bestFluxLoc)) {
				bestFluxAmount = rc.senseFluxAtLocation(bestFluxLoc);
			}
			if ((bestFluxLoc == null) || rc.getRoundsUntilMovementIdle() == 1) {
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

			// work here
			rc.setIndicatorString(0, "flux: " + bestFluxAmount);

			// build tower
			// - if we have enough flux
			// - if we have enough energon to get there
			if (flux >= 3200) {
				double energonToNextTower = Double.MAX_VALUE;
				double energonToTele = Double.MAX_VALUE;

				if (prevTower != null) {
					energonToNextTower = Math.sqrt(here.distanceSquaredTo(add(
							prevTower, towerDir, 4))) * 4.1 * 0.15;
				}

				if (nearestTele != null) {
					energonToTele = (Math.sqrt(here
							.distanceSquaredTo(nearestTele)) + 5) * 4.1 * 0.15;
				}

				// are we close enough to the next tower?
				if (prevTower != null) {
					if (energon > energonToNextTower || energon > energonToTele) {
						tryBuildNextTower();
						return;
					}
				}

				// should we build a teleporter?
				// - if we are near mommy
				// - if the nearest tower is too far
				// - if the nearest tele is too far
				if (energonToNextTower > 20 && energonToTele > 10) {
					if (here.distanceSquaredTo(mommy) <= 36) {
						tryBuild(RobotType.TELEPORTER);
					} else {
						bugTo(mommy);
					}
					return;
				}
			}

			if (woutState == 0) {
				// are we healthy enough to do stuff?
				if (eventualEnergon > 20) {
					woutState = 1;
				}
			} else if (woutState == 1) {
				// should we be heading back?
				double energonToGetToMommy = Math.sqrt(here
						.distanceSquaredTo(mommy)) * 4.1 * 0.15;

				if (eventualEnergon <= energonToGetToMommy || flux > 4000) {
					woutState = 0;
				}
			}

			if (woutState == 0) {
				// get energon
				bugTo(mommy);

			} else if (woutState == 1) {
				// if we see a wout with less energy than us, attack it
				// if we see a tower with low energy, attack it
				// if we see a real threat, run away,
				// otherwise, mine flux
				MapLocation badness = null;
				MapLocation target = null;
				int best = Integer.MAX_VALUE;
				for (Robot r : rc.senseNearbyGroundRobots()) {
					RobotInfo ri = rc.senseRobotInfo(r);
					if (ri.team != team) {
						RobotType t = ri.type;
						if ((t.isBuilding() && ri.energonLevel < 25)
								|| (t == RobotType.WOUT && ri.eventualEnergon < eventualEnergon)) {
							int dist = here.distanceSquaredTo(ri.location);
							if (dist < best) {
								best = dist;
								target = ri.location;
							}
						} else if (t != RobotType.ARCHON
								&& !t.isBuilding()
								&& (t != RobotType.WOUT || here
										.distanceSquaredTo(ri.location) > 9)) {
							badness = ri.location;
							break;
						}
					}
				}

				if (badness != null) {
					tryMove(here.directionTo(badness).opposite());
				} else if (target != null) {
					if (canAttack) {
						if (rc.canAttackSquare(target)) {
							rc.attackGround(target);
							return;
						}
					}
					bugUpTo(target);
				} else if (bestFluxLoc != null) {
					bugTo(bestFluxLoc);
				} else {
					tryMove(randomDir());
				}
			}
			return;
		}
	}

	public static MapLocation nextTower;
	public static Direction towerDir;
	public static MapLocation prevTower;
	public static int prevTowerSway = 1;

	public static final int[][] nextTowerOffsetsNorth = new int[][] { { 0, 5 },
			{ 0, 4 }, { 1, 4 }, { 2, 4 }, { 0, 3 }, { 3, 4 }, { 1, 3 },
			{ 2, 3 }, { 0, 2 }, { 3, 3 }, { 1, 2 }, { 2, 2 }, { 4, 3 },
			{ 3, 2 }, { 0, 1 }, { 4, 2 }, { 1, 1 }, { 2, 1 }, { 3, 1 },
			{ 4, 1 } };

	public static final int[][] nextTowerOffsetsNorthEast = new int[][] {
			{ 3, 4 }, { 3, 3 }, { 2, 4 }, { 2, 3 }, { 2, 2 }, { 1, 4 },
			{ 1, 3 }, { 0, 5 }, { 1, 2 }, { 0, 4 }, { 0, 3 }, { 1, 1 },
			{ 0, 2 }, { -1, 4 }, { 0, 1 }, { -1, 3 }, { -1, 2 }, { -2, 4 },
			{ -2, 3 }, { -3, 4 } };

	public static MapLocation addTowerOffset(int[] off, int sway)
			throws Exception {
		int[] dirOff = directionToOffset[towerDir.ordinal()];

		if (towerDir.isDiagonal()) {
			int x = dirOff[0] * (sway > 0 ? off[0] : off[1]);
			int y = dirOff[1] * (sway > 0 ? off[1] : off[0]);
			return add(prevTower, x, y);
		} else {
			int x = dirOff[1] * (off[0] * sway) + dirOff[0] * off[1];
			int y = dirOff[0] * (off[0] * sway) + dirOff[1] * off[1];
			return add(prevTower, x, y);
		}
	}

	public static int towerCanGoHere(MapLocation loc) throws Exception {
		if (!rc.canSenseSquare(loc))
			return -1;
		TerrainTile tt = rc.senseTerrainTile(loc);
		if (!tt.isTraversableAtHeight(RobotLevel.ON_GROUND))
			return 0;
		Robot r = rc.senseGroundRobotAtLocation(loc);
		if (r != null) {
			RobotInfo ri = rc.senseRobotInfo(r);
			if (ri.type.isBuilding()) {
				prevTower = null;
				return -2;
			}
		}
		return 1;
	}

	public static MapLocation getNextTower() throws Exception {
		int ai = 0;
		for (int[] a : (towerDir.isDiagonal() ? nextTowerOffsetsNorthEast
				: nextTowerOffsetsNorth)) {
			boolean swayCounts = towerDir.isDiagonal() ? (a[0] != a[1])
					: (a[0] != 0);
			int sway = prevTowerSway;
			for (int i = 0; i < (swayCounts ? 2 : 1); i++) {
				sway *= -1;
				MapLocation loc = addTowerOffset(a, sway);
				int ret = towerCanGoHere(loc);
				if (ret == 1) {
					if (swayCounts)
						prevTowerSway = sway;
					return loc;
				}
				// we can't even see the best locations,
				// so we probably shouldn't settle yet before we have a look
				if (ai <= 2 && ret == -1)
					return null;
				// there's already a tower there
				if (ret == -2)
					return null;
			}
			ai++;
		}
		return null;
	}

	public static boolean areTheyCamping() {
		return round < 300 || archons.length < 2
				|| archons[0].distanceSquaredTo(archons[1]) < 10;
	}

	public static Direction towardExpanse(MapLocation from) {
		MapLocation to = from;
		to = add(to, Direction.NORTH, from.getY() - yMin);
		to = add(to, Direction.EAST, xMax - from.getX());
		to = add(to, Direction.SOUTH, yMax - from.getY());
		to = add(to, Direction.WEST, from.getX() - xMin);
		return from.directionTo(to);
	}

	public static void updatePrevTower(MapLocation[] a) throws Exception {
		// give the leader a tower
		// if (areTheyCamping()) {
		// boolean found = false;
		// MapLocation leader = archons[0];
		// for (MapLocation t : a) {
		// if (leader.distanceSquaredTo(t) <= 25) {
		// found = true;
		// break;
		// }
		// }
		// if (!found && rand.nextInt(10) < 8) {
		// prevTower = leader;
		// towerDir = towardExpanse(leader);
		// return;
		// }
		// }

		double biggestArea = -1;
		MapLocation nt = null;
		Direction td = null;

		boolean[] done = new boolean[a.length];
		for (int oi = 0; oi < a.length; oi++) {
			if (done[oi])
				continue;
			done[oi] = true;
			Queue<Integer> q = new LinkedList();
			q.add(oi);

			int x = a[oi].getX();
			int y = a[oi].getY();

			int leftL = x;
			int rightL = x;
			int topL = y;
			int bottomL = y;
			int left = oi;
			int right = oi;
			int top = oi;
			int bottom = oi;

			int ur = x - y;
			int ul = -x - y;

			int neL = ur;
			int nwL = ul;
			int seL = ul;
			int swL = ur;
			int ne = oi;
			int nw = oi;
			int se = oi;
			int sw = oi;

			while (q.size() > 0) {
				int i = q.poll();
				MapLocation aa = a[i];

				x = aa.getX();
				y = aa.getY();

				if (x < leftL) {
					leftL = x;
					left = i;
				}
				if (x > rightL) {
					rightL = x;
					right = i;
				}
				if (y < topL) {
					topL = y;
					top = i;
				}
				if (y > bottomL) {
					bottomL = y;
					bottom = i;
				}

				ur = x - y;
				ul = -x - y;

				if (ur > neL) {
					neL = ur;
					ne = i;
				}
				if (ur < swL) {
					swL = ur;
					sw = i;
				}
				if (ul > nwL) {
					nwL = ul;
					nw = i;
				}
				if (ul < seL) {
					seL = ul;
					se = i;
				}

				for (int ii = 0; ii < a.length; ii++) {
					if (done[ii])
						continue;
					if (aa.distanceSquaredTo(a[ii]) <= 25) {
						done[ii] = true;
						q.add(ii);
					}
				}
			}

			double area = (rightL - leftL) * (bottomL - topL);
			double otherArea = 0.5 * (neL - swL) * (nwL - seL);
			if (area > biggestArea || otherArea > biggestArea) {
				boolean found = false;
				while (true) {
					if (areaOdds_smallerIsBetter(otherArea, area)) {
						// grow diagonally
						if (areaOdds_smallerIsBetter(neL - swL, nwL - seL)) {
							if (rand.nextBoolean()) {
								MapLocation g = a[ne];
								if (g.getX() + 4 <= xMax
										&& g.getY() - 4 >= yMin) {
									nt = g;
									td = Direction.NORTH_EAST;
									found = true;
									break;
								}
								g = a[sw];
								if (g.getX() - 4 >= xMin
										&& g.getY() + 4 <= yMax) {
									nt = g;
									td = Direction.SOUTH_WEST;
									found = true;
									break;
								}
							} else {
								MapLocation g = a[sw];
								if (g.getX() - 4 >= xMin
										&& g.getY() + 4 <= yMax) {
									nt = g;
									td = Direction.SOUTH_WEST;
									found = true;
									break;
								}
								g = a[ne];
								if (g.getX() + 4 <= xMax
										&& g.getY() - 4 >= yMin) {
									nt = g;
									td = Direction.NORTH_EAST;
									found = true;
									break;
								}
							}
						} else {
							if (rand.nextBoolean()) {
								MapLocation g = a[nw];
								if (g.getX() - 4 >= xMin
										&& g.getY() - 4 >= yMin) {
									nt = g;
									td = Direction.NORTH_WEST;
									found = true;
									break;
								}
								g = a[se];
								if (g.getX() + 4 <= xMax
										&& g.getY() + 4 <= yMax) {
									nt = g;
									td = Direction.SOUTH_EAST;
									found = true;
									break;
								}
							} else {
								MapLocation g = a[se];
								if (g.getX() + 4 <= xMax
										&& g.getY() + 4 <= yMax) {
									nt = g;
									td = Direction.SOUTH_EAST;
									found = true;
									break;
								}
								g = a[nw];
								if (g.getX() - 4 >= xMin
										&& g.getY() - 4 >= yMin) {
									nt = g;
									td = Direction.NORTH_WEST;
									found = true;
									break;
								}
							}
						}
					} else {
						// grow orthogonally
						if (areaOdds_smallerIsBetter(rightL - leftL, bottomL
								- topL)) {
							if (rand.nextBoolean()) {
								MapLocation g = a[right];
								if (g.getX() + 5 <= xMax) {
									nt = g;
									td = Direction.EAST;
									found = true;
									break;
								}
								g = a[left];
								if (g.getX() - 5 >= xMin) {
									nt = g;
									td = Direction.WEST;
									found = true;
									break;
								}
							} else {
								MapLocation g = a[left];
								if (g.getX() - 5 >= xMin) {
									nt = g;
									td = Direction.WEST;
									found = true;
									break;
								}
								g = a[right];
								if (g.getX() + 5 <= xMax) {
									nt = g;
									td = Direction.EAST;
									found = true;
									break;
								}
							}
						} else {
							if (rand.nextBoolean()) {
								MapLocation g = a[bottom];
								if (g.getY() + 5 <= yMax) {
									nt = g;
									td = Direction.SOUTH;
									found = true;
									break;
								}
								g = a[top];
								if (g.getY() - 5 >= yMin) {
									nt = g;
									td = Direction.NORTH;
									found = true;
									break;
								}
							} else {
								MapLocation g = a[top];
								if (g.getY() - 5 >= yMin) {
									nt = g;
									td = Direction.NORTH;
									found = true;
									break;
								}
								g = a[bottom];
								if (g.getY() + 5 <= yMax) {
									nt = g;
									td = Direction.SOUTH;
									found = true;
									break;
								}
							}
						}
					}
					break;
				}
				if (found) {
					biggestArea = Math.max(area, otherArea);
				}
			}
		}

		prevTower = nt;
		towerDir = td;
	}

	public static boolean areaOdds_smallerIsBetter(double a, double b) {
		if (a < b * .6)
			return true;
		if (a < b)
			return rand.nextDouble() < (b - a) / b;
		return rand.nextDouble() < (a - b) / a;
	}

	public static MapLocation bestCamp;
	public static double bestCampScore = 0;
	public static int bestCampId = -1;

	public static int[][] bestCamp_searchXs = new int[][] { { -3, 0 },
			{ -4, 1 }, { -5, 2 }, { -5, 2 }, { -5, 2 }, { -5, 2 }, { -4, 1 },
			{ -3, 0 } };

	public static Set<MapLocation> bestCamp_searchedLocs = new HashSet();

	public static MapLocation bestCamp_oldLoc = null;

	public static void updateBestCamp() {
		if (bestCamp_oldLoc != null && here.equals(bestCamp_oldLoc))
			return;
		bestCamp_oldLoc = here;

		int hereX = here.getX();
		int hereY = here.getY();
		for (int y = -5; y <= 2; y++) {
			int yy = hereY + y;
			int[] range = bestCamp_searchXs[y + 5];
			int rangeMax = range[1];
			for (int x = range[0]; x < rangeMax; x++) {
				int xx = hereX + x;
				MapLocation camp = new MapLocation(xx + 1, yy + 1);
				if (!bestCamp_searchedLocs.add(camp))
					continue;
				double score = 0;
				for (int dx = 0; dx < 4; dx++) {
					for (int dy = 0; dy < 4; dy++) {
						MapLocation pos = new MapLocation(xx + dx, yy + dy);
						TerrainTile tt = rc.senseTerrainTile(pos);
						if (tt.isTraversableAtHeight(RobotLevel.ON_GROUND)) {
							score += 1.0;
						}
					}
				}
				if (score > bestCampScore
						|| (score == bestCampScore && id > bestCampId)) {
					bestCampScore = score;
					bestCamp = camp;
					bestCampId = id;
				}
			}
		}
	}

	public static void tryBuildNextTower() throws Exception {

		// try to find the exact spot to build
		if (nextTower == null) {
			if ((rc.getRoundsUntilMovementIdle() == 2) || (round % 7 == 0)) {
				nextTower = getNextTower();
				if (prevTower == null)
					return;
			}
		}

		if (!canMove)
			return;

		// if we haven't found it yet,
		// move closer to being a little in front of the prevTower
		MapLocation goal = null;
		if (nextTower == null) {
			goal = add(prevTower, towerDir, 2);
		} else {
			int dist = here.distanceSquaredTo(nextTower);
			if (dist <= 2) {
				if (face(nextTower))
					return;
				if (rc.senseGroundRobotAtLocation(nextTower) == null)
					build(RobotType.TELEPORTER);
			}
			goal = nextTower;
		}

		if ((nearestTele != null)
				&& Math.sqrt(here.distanceSquaredTo(goal)) > Math.sqrt(here
						.distanceSquaredTo(nearestTele)) + 5) {

			if (!ri.teleporting) {
				if (((id + round) % 3 == 0) && !rc.hasBroadcastMessage()) {
					sendMessage(message_BEAM_ME_UP, here.getX(), here.getY(),
							goal.getX(), goal.getY(), id);
				}
			}

			goal = nearestTele;
		}

		bugUpTo(goal);
	}

	public static double lerp(double t0, double v0, double t1, double v1,
			double t) {
		return (t - t0) * (v1 - v0) / (t1 - t0) + v0;
	}

	public static Direction fancyDir(MapLocation from, MapLocation to) {
		double a = Math.atan2(to.getX() - from.getX(), from.getY() - to.getY());
		if (a < 0)
			a += Math.PI * 2;
		return Direction.values()[(int) Math
				.round(lerp(0, 0, Math.PI * 2, 8, a)) % 8];
	}

	public static Direction driftDir(boolean clockwise) {
		int spin = clockwise ? 1 : -1;
		double x = lerp(xMin, -5, xMax, 5, here.getX());
		double y = lerp(yMin, -5, yMax, 5, here.getY());
		MapLocation center = new MapLocation((xMin + xMax) / 2,
				(yMin + yMax) / 2);
		if (x < -3 || x > 3 || y < -3 || y > 3) {
			// outter ring
			return rotate(fancyDir(center, here), spin * 2);
		} else if (x < -1 || x > 1 || y < -1 || y > 1) {
			// middle ring
			return rotate(fancyDir(center, here), spin);
		} else {
			// center
			if (center.equals(here))
				return randomDir();
			return fancyDir(center, here);
		}
	}

	public static Direction fleeDir(MapLocation fleeMe) {
		double x = lerp(xMin, -5, xMax, 5, here.getX());
		double y = lerp(yMin, -5, yMax, 5, here.getY());
		MapLocation center = new MapLocation((xMin + xMax) / 2,
				(yMin + yMax) / 2);
		if (x < -3 || x > 3 || y < -3 || y > 3) {
			// outter ring
			Direction d = fancyDir(center, here);
			Direction ad = rotate(d, -2);
			Direction bd = rotate(d, 2);
			if (here.add(ad).distanceSquaredTo(fleeMe) > here.add(bd)
					.distanceSquaredTo(fleeMe)) {
				return ad;
			} else {
				return bd;
			}
		} else if (x < -1 || x > 1 || y < -1 || y > 1) {
			// middle ring
			Direction d = fancyDir(center, here);
			Direction ad = rotate(d, -1);
			Direction bd = rotate(d, 1);
			if (here.add(ad).distanceSquaredTo(fleeMe) > here.add(bd)
					.distanceSquaredTo(fleeMe)) {
				return ad;
			} else {
				return bd;
			}
		} else {
			return here.directionTo(fleeMe).opposite();
		}
	}

	public static int[][] chainer_orthAttackables = new int[][] { { 0, 3 },
			{ 0, 2 }, { -2, 2 }, { -1, 2 }, { 1, 2 }, { 2, 2 } };

	public static int[][] chainer_diagAttackables = new int[][] { { 0, 3 },
			{ 0, 2 }, { 1, 2 }, { 2, 2 }, { 2, 1 }, { 2, 0 }, { 3, 0 } };

}
