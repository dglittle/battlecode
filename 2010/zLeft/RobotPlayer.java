package zLeft;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import battlecode.common.AuraType;
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


 - make wouts attack other wouts if they are close


 Attack
 - if I spot something to attach, attack it en-mass with an attack message


 Messages
 - broadcast the towerDir, towerPos stuff in case the leader dies
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
	public static MapLocation leave;
	public static int leaveTime;
	public static boolean canMove;
	public static boolean canAttack;
	public static MapLocation badness;
	public static int badnessRound;

	// non-archons only
	public static int parentIndex = -1;
	public static int prevArchonsAlive;
	public static MapLocation parentLoc;

	// archons only
	public static int originalArchonIndex = -1;
	public static Direction originalEnemyDir;
	public static Map<Integer, Integer> childrenLastSeen = new HashMap();
	public static Map<Integer, RobotType> childrenTypes = new HashMap();
	public static int[] childrenCounts = new int[RobotType.values().length];
	public static int lastBirth;

	// message types
	public static final int message_OED = 1;
	public static final int message_OWN = 2;
	public static final int message_MOV = 3;
	public static final int message_BAD = 4;
	public static final int message_TOW = 5;

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

	public static void sendMessage(int a, int b, int c, int d, int e)
			throws Exception {
		Message m = new Message();
		m.ints = new int[] { a, b, c, d, e, round, magic - a, magic - b,
				magic - c, magic - d, magic - e, magic - round };
		rc.broadcast(m);
	}

	public static MapLocation add(MapLocation loc, int x, int y) {
		return new MapLocation(loc.getX() + x, loc.getY() + y);
	}

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
		for (int i = 0; i < archons.length; i++) {
			if (archons[i].equals(here))
				return i;
		}
		throw new Exception("no archon index?");
	}

	public static void build(RobotType type) throws Exception {
		rc.spawn(type);
		rc.yield();
		MapLocation front = here.add(facing);
		Robot r = rc.senseGroundRobotAtLocation(front);
		if (r != null) {
			int childId = r.getID();
			childrenLastSeen.put(childId, round);
			childrenTypes.put(childId, type);
			childrenCounts[type.ordinal()]++;
			sendMessage(message_OWN, childId, getArchonIndex(), leftGroup ? 1
					: 0, 0);
			lastBirth = round;
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
		if (facing != dir) {
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

	public static Direction randomDir() {
		return Direction.values()[rand.nextInt(8)];
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
			// get original archon index
			for (int i = 0; i < archons.length; i++) {
				if (archons[i].equals(here)) {
					originalArchonIndex = i;
					break;
				}
			}
			if (originalArchonIndex == -1)
				throw new Exception("bad!");

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
			sendMessage(message_OED, originalEnemyDir.ordinal(), 0, 0, 0);
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

				for (Message m : rc.getAllMessages()) {
					// 33 bcs
					if (m.locations != null)
						continue;
					if (m.strings != null)
						continue;
					int[] ints = m.ints;
					if (ints == null)
						continue;
					int roundSent = ints[5];
					if (round > roundSent + 3)
						continue;
					if (roundSent > round)
						continue;

					// work here
					if (ints[6] != magic - ints[0])
						continue;
					if (ints[7] != magic - ints[1])
						continue;
					if (ints[8] != magic - ints[2])
						continue;
					if (ints[9] != magic - ints[3])
						continue;
					if (ints[10] != magic - ints[4])
						continue;
					if (ints[11] != magic - ints[5])
						continue;

					// 50 bcs
					// int hash = hash(expires, ints);
					// if (hash != loc.getY())
					// continue;

					switch (ints[0]) {
					case message_OED:
						if (isArchon) {
							Direction dir = Direction.values()[ints[1]];
							if (dir.isDiagonal()) {
								if (originalEnemyDir.isDiagonal()
										&& (originalEnemyDir != dir))
									throw new Exception("bad!!");
								originalEnemyDir = dir;
							}
						}
						break;
					case message_OWN:
						if (id == ints[1]) {
							parentIndex = ints[2];
							prevArchonsAlive = archons.length;
							parentLoc = archons[parentIndex];

							leftGroup = ints[3] == 1;
						}
						break;
					case message_MOV:
						if (id == ints[1]) {
							if (leave == null) {
								leave = here;
								leaveTime = 10;
							}
						}
						break;
					case message_BAD:
						badness = new MapLocation(ints[1], ints[2]);
						badnessRound = roundSent;
						break;
					case message_TOW:
						if (isArchon) {
							if (leftGroup == (ints[4] == 1)) {
								if (groupPosition != 0) {
									prevTower = new MapLocation(ints[1],
											ints[2]);
									towerDir = Direction.values()[ints[3]];
								}
							}
						}
						break;
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
						for (Robot r : rc.senseNearbyGroundRobots()) {
							int id = r.getID();
							if (childrenLastSeen.containsKey(id)) {
								childrenLastSeen.put(id, round);
							}
						}

						Arrays.fill(childrenCounts, 0);
						for (Map.Entry<Integer, Integer> k : childrenLastSeen
								.entrySet()) {
							RobotType t = childrenTypes.get(k.getKey());
							int lastSeen = k.getValue();
							if (t == RobotType.WOUT && lastSeen > round - 120) {
								childrenCounts[t.ordinal()]++;
							} else if (lastSeen > round - 10) {
								childrenCounts[t.ordinal()]++;
							}
						}

						// work here
						rc.setIndicatorString(0, "wouts: "
								+ childrenCounts[RobotType.WOUT.ordinal()]);
						rc.setIndicatorString(1, "soldi: "
								+ childrenCounts[RobotType.SOLDIER.ordinal()]);

					}
				}

				// leave
				if (leave != null) {
					if (!here.equals(leave)) {
						leave = null;
					} else {
						leaveTime--;
						if (leaveTime == 0)
							rc.suicide();
						if (canMove) {
							if (tryMove(Direction.values()[rand.nextInt(8)])) {
								rc.yield();
								continue;
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

	// /////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////

	public static boolean leftGroup;
	public static MapLocation leaderLoc;

	// wouts only
	public static int woutState = 1;
	public static double woutHalfEnergon;
	public static MapLocation bestFluxLoc;
	public static double bestFluxAmount = -1;

	// archon only
	public static boolean firstTime = true;
	public static int groupPosition = -1;
	public static MapLocation oldLeaderLoc = new MapLocation(0, 0);
	public static MapLocation followLoc;
	public static MapLocation oldLoc = new MapLocation(0, 0);
	public static int roundsHere = 0;
	public static Direction archonDir;

	public static Direction[] dirs = new Direction[] { Direction.NORTH,
			Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
			Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
			Direction.NORTH_WEST };

	public static MapLocation bestLoc = null;

	public static MapLocation goal;

	public static void ai() throws Exception {
		if (firstTime) {
			if (isArchon) {
				leftGroup = originalArchonIndex < 3;
			}

			firstTime = false;
		}

		MapLocation leaderLoc = archons[leftGroup ? 0 : archons.length - 1];

		// buildings
		if (type.isBuilding()) {
			return;
		}

		// transfer energon & flux
		{
			// energon
			if (type != RobotType.WOUT) {
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
					double amount = Math.min(1,
							GameConstants.ENERGON_RESERVE_SIZE
									- weakestRI.energonReserve);
					rc.transferUnitEnergon(amount, weakestRI.location,
							weakestRI.type.isAirborne() ? RobotLevel.IN_AIR
									: RobotLevel.ON_GROUND);
					energon -= amount;
				}
				if (lowestReserveRI != null && (maxEnergon - energon < 2.0)) {
					double amount = Math.min(2,
							GameConstants.ENERGON_RESERVE_SIZE
									- lowestReserveRI.energonReserve);
					rc
							.transferUnitEnergon(
									amount,
									lowestReserveRI.location,
									lowestReserveRI.type.isAirborne() ? RobotLevel.IN_AIR
											: RobotLevel.ON_GROUND);
					energon -= amount;
				}
			}

			// flux
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
					int dist = here.distanceSquaredTo(leaderLoc);
					if (dist <= 2) {
						RobotInfo ri = rc.senseRobotInfo(rc
								.senseAirRobotAtLocation(leaderLoc));
						double amount = flux;
						double theyCanTake = ri.type.maxFlux() - ri.flux;
						if (amount > theyCanTake)
							amount = theyCanTake;
						rc.transferFlux(amount, leaderLoc, RobotLevel.IN_AIR);
						flux -= amount;
					}
				}
			}
		}

		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////
		// Archon
		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////

		
		if (isArchon) {
			if (goal == null) {
				int x = 0;
				int y = 0;
				for (MapLocation loc : rc.senseAlliedArchons()) {
					x += loc.getX();
					y += loc.getY();
				}
				x /= rc.senseAlliedArchons().length;
				y /= rc.senseAlliedArchons().length;
				MapLocation there = new MapLocation(x, y);
				if (there.getY() < here.getY()) {
					goal = add(here, Direction.NORTH, 2);
				} else {
					goal = add(here, Direction.SOUTH, 2);
				}
			}
			tryMove(goal);
			
//			int x = 0;
//			int y = 0;
//			for (MapLocation loc : rc.senseAlliedArchons()) {
//				x += loc.getX();
//				y += loc.getY();
//			}
//			x /= rc.senseAlliedArchons().length;
//			y /= rc.senseAlliedArchons().length;
//			tryMove(new MapLocation(x, y));
		}

		if (round < 300)
			return;

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
					tryMove(Direction.EAST);
				}
			}
		}

		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////
		// Chainer
		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////

		if (type == RobotType.CHAINER) {
			if (canAttack) {
				bestLoc = null;
				int best = 0;
				for (Robot r : rc.senseNearbyGroundRobots()) {
					RobotInfo ri = rc.senseRobotInfo(r);
					if (ri.team != team) {
						MapLocation loc = ri.location;
						int score = 1;
						for (Direction d : dirs) {
							MapLocation there = loc.add(d);
							if (rc.canSenseSquare(there)) {
								Robot ro = rc.senseGroundRobotAtLocation(there);
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
						}
					}
				}
				if (bestLoc != null) {
					rc.attackGround(bestLoc);
				}
			}

			if (canMove) {
				if (bestLoc != null) {
					if (here.distanceSquaredTo(bestLoc) > 2) {
						tryMove(bestLoc);
					}
				} else {
					if (tryMove(Direction.EAST))
						return;
				}
			}
		}

		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////
		// Wout
		// ///////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////

		if (type == RobotType.WOUT) {

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

			if (canMove) {
				// state transitions
				if (woutState == 0) {
					if (here.distanceSquaredTo(leaderLoc) <= 2) {
						woutState = 1;
					}
				} else if (woutState == 1) {
					if (eventualEnergon > 25) {
						woutHalfEnergon = (energon + reserve) / 2 + 1;
						woutState = 2;
					}
				} else if (woutState == 2) {
					woutHalfEnergon = Math.max(woutHalfEnergon,
							(energon + reserve) / 2 + 1);
					// are we hurt enough, or have enough flux?
					if (eventualEnergon < woutHalfEnergon || flux > 4500) {
						woutState = 0;
					}
				}

				if (woutState == 0) {
					if (bugTo(leaderLoc))
						return;
				} else if (woutState == 1) {
					if (bugTo(parentLoc))
						return;
				} else if (woutState == 2) {
					if (bugTo(bestFluxLoc))
						return;
				}
			}
		}
	}

	public static Direction towerDir;
	public static MapLocation prevTower;
	public static MapLocation nextTower;
	public static int prevTowerSway = 1;

	public static final int[][] nextTowerOffsetsNorth = new int[][] { { 0, 5 },
			{ 0, 4 }, { 1, 4 }, { 2, 4 }, { 0, 3 }, { 1, 3 }, { 0, 2 },
			{ 1, 2 }, { 0, 1 }, { 1, 1 } };

	public static final int[][] nextTowerOffsetsNorthEast = new int[][] {
			{ 3, 4 }, { 3, 3 }, { 2, 4 }, { 2, 3 }, { 2, 2 }, { 1, 3 },
			{ 1, 2 }, { 1, 1 }, { 0, 2 }, { 0, 1 } };

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
			return -2;
		TerrainTile tt = rc.senseTerrainTile(loc);
		if (tt == TerrainTile.OFF_MAP)
			return -1;
		if (!tt.isTraversableAtHeight(RobotLevel.ON_GROUND))
			return 0;
		return 1;
	}

	public static MapLocation getNextTower() throws Exception {
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
				if (ret == -1) {
					towerDir = leftGroup ? towerDir.rotateRight().rotateRight()
							: towerDir.rotateLeft().rotateLeft();
					return null;
				}
				if (ret == -2)
					return null;
			}
		}
		towerDir = leftGroup ? towerDir.rotateRight() : towerDir.rotateLeft();
		return null;
	}
}
