package team022;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.FluxDeposit;
import battlecode.common.FluxDepositInfo;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile.TerrainType;

public class RobotPlayer implements Runnable {

	public static int spice = 0x782654a8;
	public static Random r;
	public static RobotController rc;
	public static RobotType type;
	public static MapLocation here;
	public static Direction dir;
	public static int id;
	public static Robot robot;
	public static Team team;
	public static MapLocation front;
	public static double energon;
	public static boolean archon;
	public static boolean scout;
	public static boolean tankLike;
	public static boolean worker;
	public static int broadcastRadiusSq;
	public static MapLocation oldHere = null;
	public static int hereSinceRound = 0;
	public static Set<Integer> receivedMessages = new HashSet<Integer>();
	public static boolean justMoved = false;
	public static int parentArchonIndex = -1;
	public static MapLocation parentArchon = null;
	public static int birthRound;
	public static int round;

	public static Message corruptMessage = null;

	public static Vector<RobotInfo> nearbyRobotInfos = null;
	public static Vector<RobotInfo> superNearRobotInfos = null;
	public static Vector<RobotInfo> enemyAirRobotInfos = null;
	public static Vector<RobotInfo> enemyGroundRobotInfos = null;

	public static Set<Integer> receivedEnemyRobotLocations = new HashSet();
	public static PriorityQueue<Integer> enemyRobotLocationsQueue = new PriorityQueue<Integer>();
	public static Vector<MapLocation> unseenEnemyLocations = new Vector();
	public static Vector<RobotType> unseenEnemyTypes = new Vector();

	public RobotPlayer(RobotController rc) {
		if (this.rc != null)
			return;
		RobotPlayer.rc = rc;

		r = new Random(rc.getRobot().getID() * 7264839 + 968278175852L);
		round = Clock.getRoundNum();
		type = rc.getRobotType();
		here = rc.getLocation();
		dir = rc.getDirection();
		robot = rc.getRobot();
		id = robot.getID();
		team = rc.getTeam();
		front = here.add(dir);
		energon = rc.getEnergonLevel();
		archon = (type == RobotType.ARCHON);
		scout = (type == RobotType.SCOUT);
		worker = (type == RobotType.WORKER);
		tankLike = (type == RobotType.CANNON || type == RobotType.SOLDIER || type == RobotType.CHANNELER);
		broadcastRadiusSq = type.broadcastRadius() * type.broadcastRadius();
		oldHere = here;
		birthRound = round;
		hereSinceRound = birthRound;
	}

	public void run() {
		RobotPlayer p = null;

		loop: while (true) {
			try {
				RobotType newType = rc.getRobotType();
				if (p == null || newType != type) {
					type = newType;
					if (type == RobotType.ARCHON)
						p = new Archon(rc);
					if (type == RobotType.SCOUT)
						p = new Scout(rc);
					if (type == RobotType.WORKER)
						p = new Worker(rc);
					if (type == RobotType.SOLDIER)
						p = new Soldier(rc);
					if (type == RobotType.CANNON)
						p = new Cannon(rc);
					if (type == RobotType.CHANNELER)
						p = new Channeler(rc);
				}

				// ////////////////////////////////////////////////////////////////////
				// ////////////////////////////////////////////////////////////////////
				// ////////////////////////////////////////////////////////////////////
				// common information

				round = Clock.getRoundNum();
				type = rc.getRobotType();
				here = rc.getLocation();
				dir = rc.getDirection();
				front = here.add(dir);
				energon = rc.getEnergonLevel();

				nearbyRobotInfos = new Vector();
				superNearRobotInfos = new Vector();
				enemyAirRobotInfos = new Vector();
				enemyGroundRobotInfos = new Vector();

				for (Robot r : rc.senseNearbyAirRobots()) {
					RobotInfo ri = rc.senseRobotInfo(r);
					if (ri.team == team) {
						nearbyRobotInfos.add(ri);
						if (superNear(ri.location)) {
							superNearRobotInfos.add(ri);
						}
					} else {
						enemyAirRobotInfos.add(ri);
					}
				}
				for (Robot r : rc.senseNearbyGroundRobots()) {
					RobotInfo ri = rc.senseRobotInfo(r);
					if (ri.team == team) {
						nearbyRobotInfos.add(ri);
						if (superNear(ri.location)) {
							superNearRobotInfos.add(ri);
						}
					} else {
						enemyGroundRobotInfos.add(ri);
					}
				}

				// ////////////////////////////////////////////////////////////////////
				// ////////////////////////////////////////////////////////////////////
				// ////////////////////////////////////////////////////////////////////
				// get messages
				//
				// locs[0] = sender location
				//
				// ints[0] = round sent
				// ints[1] = signature (locs[0].hashCode() * ints[0] + spice)
				// ints[2] = message type

				if (!archon && (!scout || (parentArchonIndex < 0))) {
					receivedEnemyRobotLocations = new HashSet();
					enemyRobotLocationsQueue = new PriorityQueue<Integer>();

					Message theirMessage = null;
					Message[] messages = rc.getAllMessages();
					if (oldHere.equals(here)) {
						for (Message m : messages) {
							MapLocation[] locs = m.locations;
							if (locs == null) {
								theirMessage = m;
								continue;
							}
							if (locs.length < 1) {
								theirMessage = m;
								continue;
							}
							MapLocation origin = locs[0];
							if (origin == null) {
								theirMessage = m;
								continue;
							}
							if (here.distanceSquaredTo(origin) > broadcastRadiusSq)
								continue;
							int[] ints = m.ints;
							if (ints == null) {
								theirMessage = m;
								continue;
							}
							if (ints.length < 3) {
								theirMessage = m;
								continue;
							}
							int roundSent = ints[0];
							if (roundSent < hereSinceRound)
								continue;
							int signature = ints[1];
							if (signature != origin.hashCode() * roundSent
									+ spice) {
								theirMessage = m;
								continue;
							}
							if (!receivedMessages.add(signature))
								continue;
							switch (ints[2]) {
							case 1:
								if (tankLike) {
									Set<Integer> s = new HashSet();
									for (int i = 3; i < ints.length; i++) {
										s.add(ints[i]);
									}
									s.removeAll(receivedEnemyRobotLocations);
									receivedEnemyRobotLocations.addAll(s);
									enemyRobotLocationsQueue.addAll(s);
								}
								break;
							case 2:
								if (id == ints[3])
									parentArchonIndex = ints[4];
								break;
							}
						}
					} else {
						hereSinceRound = Clock.getRoundNum();
					}
					if (tankLike) {
						if (theirMessage != null) {
							corruptMessage = theirMessage;
						}
						if (corruptMessage != null) {
							double cost = GameConstants.BROADCAST_FIXED_COST
									+ corruptMessage.getNumBytes()
									* GameConstants.BROADCAST_COST_PER_BYTE;
							double odds = 0.02 / cost;
							if (r.nextDouble() < odds) {
								corruptMessage(corruptMessage);
								rc.broadcast(corruptMessage);
							}
						}
					}
				}
				oldHere = here;

				if (!archon && (parentArchonIndex < 0)) {
					if (birthRound + 10 < Clock.getRoundNum()) {
						rc.suicide();
					}
					rc.yield();
					continue loop;
				}
				if (parentArchonIndex >= 0) {
					MapLocation[] moms = rc.senseAlliedArchons();
					if (moms.length > 0) {
						if (parentArchonIndex >= moms.length)
							parentArchonIndex = moms.length - 1;

						MapLocation prevParentArchon = parentArchon;
						parentArchon = moms[parentArchonIndex];

						if (prevParentArchon == null) {
						} else {
							if (!superNear(prevParentArchon, parentArchon)) {
								int bestScore = Integer.MIN_VALUE;
								for (int i = 0; i < moms.length; i++) {
									int score = -prevParentArchon
											.distanceSquaredTo(moms[i]);
									if (score > bestScore) {
										bestScore = score;
										parentArchonIndex = i;
										parentArchon = moms[i];
									}
								}
							}
						}
					}
				}

				// ////////////////////////////////////////////////////////////////////
				// ////////////////////////////////////////////////////////////////////
				// ////////////////////////////////////////////////////////////////////
				// archon tell about enemies

				if (archon || scout) {
					int airCount = enemyAirRobotInfos.size();
					int groundCount = enemyGroundRobotInfos.size();
					if (airCount > 0 || groundCount > 0) {
						int round = Clock.getRoundNum();
						int signature = here.hashCode() * round + spice;
						int[] ints = new int[3 + airCount + groundCount];
						ints[0] = round;
						ints[1] = signature;
						ints[2] = 1;

						int i = 3;
						for (RobotInfo ri : enemyAirRobotInfos) {
							MapLocation pos = ri.location;
							int x = mod(pos.getX(), 128);
							int y = mod(pos.getY(), 128);
							int t = ri.type.ordinal();
							int r = 16000 - (round + ri.roundsUntilMovementIdle);
							ints[i++] = (r << 18) | (t << 14) | (x << 7) | y;
						}
						for (RobotInfo ri : enemyGroundRobotInfos) {
							MapLocation pos = ri.location;
							int x = mod(pos.getX(), 128);
							int y = mod(pos.getY(), 128);
							int t = ri.type.ordinal();
							int r = 16000 - (round + ri.roundsUntilMovementIdle);
							ints[i++] = (r << 18) | (t << 14) | (x << 7) | y;
						}

						Message m = new Message();
						m.locations = new MapLocation[] { here };
						m.ints = ints;
						rc.broadcast(m);
					}
				}

				if (tankLike) {
					unseenEnemyLocations = new Vector();
					unseenEnemyTypes = new Vector();
					int xOffset = (here.getX() / 128) * 128;
					if (xOffset > here.getX())
						xOffset -= 128;
					int yOffset = (here.getY() / 128) * 128;
					if (yOffset > here.getY())
						yOffset -= 128;
					int myModX = mod(here.getX(), 128);
					int myModY = mod(here.getY(), 128);
					PriorityQueue<Integer> newQ = new PriorityQueue<Integer>();
					Set<MapLocation> airsAlready = new HashSet();
					Set<MapLocation> groundsAlready = new HashSet();
					while (enemyRobotLocationsQueue.size() > 0) {
						int i = enemyRobotLocationsQueue.poll();

						int r = 16000 - ((i & 0xFFFC0000) >>> 18);
						if (r + 1 < round) {
							continue;
						}
						int t = (i & 0x0003C000) >> 14;
						int modX = (i & 0x00003F80) >> 7;
						int modY = i & 0x0000007F;

						int x = xOffset + modX;
						if (modX - myModX > 60) {
							x -= 128;
						} else if (modX - myModX < -60) {
							x += 128;
						}
						int y = yOffset + modY;
						if (modY - myModY > 60) {
							y -= 128;
						} else if (modY - myModY < -60) {
							y += 128;
						}

						MapLocation pos = new MapLocation(x, y);
						RobotType rt = RobotType.values()[t];
						if (rt.isAirborne() ? airsAlready.add(pos)
								: groundsAlready.add(pos)) {
							unseenEnemyLocations.add(pos);
							unseenEnemyTypes.add(RobotType.values()[t]);

							newQ.add(i);
						}
					}
					enemyRobotLocationsQueue = newQ;
				}

				// ////////////////////////////////////////////////////////////////////
				// ////////////////////////////////////////////////////////////////////
				// ////////////////////////////////////////////////////////////////////
				// healing

				// if (archon || scout || worker) {
				for (RobotInfo ri : superNearRobotInfos) {
					double want = GameConstants.ENERGON_RESERVE_SIZE
							- ri.energonReserve;
					double willing = energon - (type.maxEnergon() * 0.75);
					if (ri.energonLevel < ri.maxEnergon * 0.25) {
						willing = energon - (type.maxEnergon() * 0.25);
					}

					/*
					 * if (enemyGroundRobotInfos.size() > 0) { double
					 * canTakeThisRound = 1 - ri.energonReserve; if (willing >
					 * canTakeThisRound) willing = canTakeThisRound; }
					 */

					if (willing >= 0) {
						double amount = Math.min(willing, want);
						if (Clock.getRoundNum() > round)
							break;
						rc.transferEnergon(amount, ri.location, getLevel(ri));
						energon -= amount;
					}
				}
				// }

				// ////////////////////////////////////////////////////////////////////
				// ////////////////////////////////////////////////////////////////////
				// ////////////////////////////////////////////////////////////////////
				// run loop

				if (!p.loop()) {

					// ////////////////////////////////////////////////////////////////////
					// ////////////////////////////////////////////////////////////////////
					// ////////////////////////////////////////////////////////////////////
					// default instincts

					while (!archon && !rc.isMovementActive()) {
						if (moveOffFluxInstinct()) {
							break;
						}
						if (moveToArchonInstinct()) {
							break;
						}
						rc.setDirection(randomDir());
						break;
					}
				}
			} catch (Exception e) {
				System.out.println("e: " + e);
				e.printStackTrace();
			}
			rc.yield();

			if ((rc.getRoundsUntilMovementIdle() + 1) == (dir.isDiagonal() ? type
					.moveDelayDiagonal()
					: type.moveDelayOrthogonal())) {
				justMoved = true;
			} else {
				justMoved = false;
			}
		}
	}

	public boolean loop() throws Exception {
		throw new IllegalArgumentException("implement me!");
	}

	// //////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////

	public static void corruptMessage(Message m) {
		if (m.ints != null) {
			if (m.ints.length > 1) {
				int i = r.nextInt(m.ints.length - 1);
				int x = r.nextInt();
				m.ints[i] += x;
				m.ints[i + 1] -= 31 * x;
			}
		}
		if (m.locations != null) {
			if (m.locations.length > 1) {
				int i = r.nextInt(m.locations.length - 1);
				if (m.locations[i] != null && m.locations[i + 1] != null) {
					int x = r.nextInt();
					int y = r.nextInt();
					m.locations[i] = new MapLocation(m.locations[i].getX() + x,
							m.locations[i].getY() + y);
					m.locations[i + 1] = new MapLocation(m.locations[i + 1]
							.getX()
							- 31 * x, m.locations[i + 1].getY() - 31 * y);
				}
			} else if (m.locations.length == 1 && m.locations[0] != null) {
				int base = r.nextInt();
				int x = base * 23;
				m.locations[0] = new MapLocation(m.locations[0].getX() + x,
						m.locations[0].getY() - 13 * base);
			}
		}
	}

	public static int sq(int a) {
		return a * a;
	}

	public static int mod(int a, int b) {
		if (a < 0)
			return a % b + b;
		return a % b;
	}

	public static Direction[] dirs = new Direction[] { Direction.NORTH,
			Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
			Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
			Direction.NORTH_WEST };

	public static int[][] workerShortVision = new int[][] { { 0, 0 },
			{ 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 }, { 1, -1 }, { 1, 1 },
			{ -1, 1 }, { -1, -1 }, { 2, 0 }, { 2, 1 }, { 2, -1 }, { -2, 0 },
			{ -2, 1 }, { -2, -1 }, { 0, 2 }, { 1, 2 }, { -1, 2 }, { 0, -2 },
			{ 1, -2 }, { -1, -2 }, { 0, 3 }, { 3, 0 }, { 0, -3 }, { -3, 0 } };

	public static int[][] workerVision = new int[][] { { 0, 0 }, { 0, -1 },
			{ 1, 0 }, { 0, 1 }, { -1, 0 }, { 1, -1 }, { 1, 1 }, { -1, 1 },
			{ -1, -1 }, { 2, 0 }, { 2, 1 }, { 2, -1 }, { -2, 0 }, { -2, 1 },
			{ -2, -1 }, { 0, 2 }, { 1, 2 }, { -1, 2 }, { 0, -2 }, { 1, -2 },
			{ -1, -2 }, { 0, 3 }, { 3, 0 }, { 0, -3 }, { -3, 0 }, { 1, 3 },
			{ -1, 3 }, { 1, -3 }, { -1, -3 }, { 3, 1 }, { 3, -1 }, { -3, 1 },
			{ -3, -1 }, { 2, 3 }, { -2, 3 }, { 2, -3 }, { -2, -3 }, { 3, 2 },
			{ 3, -2 }, { -3, 2 }, { -3, -2 }, { 0, 4 }, { 4, 0 }, { 0, -4 },
			{ -4, 0 } };

	public static boolean moveToArchonInstinct() throws Exception {
		if (!superNear(parentArchon)) {
			tryMove(parentArchon);
			return true;
		}
		return false;
	}

	public static boolean moveOffFluxInstinct() throws Exception {
		if (rc.senseFluxDepositAtLocation(here) != null) {
			Direction bestDir = null;
			int bestScore = Integer.MIN_VALUE;
			int height = rc.senseHeightOfLocation(here);
			for (Direction d : dirs) {
				if (rc.canMove(d)) {
					MapLocation pos = here.add(d);
					int score = rc.canSenseSquare(pos) ? -moveCost(height, rc
							.senseHeightOfLocation(pos), d) : -1000;
					if (score > bestScore) {
						bestScore = score;
						bestDir = d;
					}
				}
			}
			if (bestDir != null) {
				move(bestDir);
				return true;
			}
		}
		return false;
	}

	public static int moveCost(int heightA, int heightB, Direction d) {
		int dHeight = heightA - heightB; // yes we want A - B and not B - A
		return (d.isDiagonal() ? type.moveDelayDiagonal() : type
				.moveDelayOrthogonal())
				+ ((heightA < heightB) ? (GameConstants.CLIMBING_PENALTY_RATE
						* dHeight * dHeight)
						: ((heightA > heightB) ? (GameConstants.FALLING_PENALTY_RATE * dHeight)
								: 0));
	}

	public static boolean canUnloadFromTo(int hereHeight, MapLocation there)
			throws Exception {
		if (!rc.canSenseSquare(there))
			return false;
		if (rc.senseTerrainTile(there).getType() != TerrainType.LAND)
			return false;
		if (!there.equals(here)
				&& (rc.senseGroundRobotAtLocation(there) != null))
			return false;
		return rc.senseHeightOfLocation(there) < hereHeight
				+ GameConstants.WORKER_MAX_HEIGHT_DELTA;
	}

	public static boolean canLoadFromTo(int hereHeight, MapLocation there)
			throws Exception {
		if (!rc.canSenseSquare(there))
			return false;
		if (rc.senseTerrainTile(there).getType() != TerrainType.LAND)
			return false;
		if (!there.equals(here)
				&& (rc.senseGroundRobotAtLocation(there) != null))
			return false;
		if (rc.senseNumBlocksAtLocation(there) == 0)
			return false;
		return rc.senseHeightOfLocation(there) > hereHeight
				- GameConstants.WORKER_MAX_HEIGHT_DELTA;
	}

	public static boolean canCarryFromTo(int hereHeight, MapLocation there)
			throws Exception {
		if (!rc.canSenseSquare(there))
			return false;
		if (rc.senseTerrainTile(there).getType() != TerrainType.LAND)
			return false;
		if (rc.senseGroundRobotAtLocation(there) != null)
			return false;
		return rc.senseHeightOfLocation(there) <= hereHeight
				+ GameConstants.WORKER_MAX_HEIGHT_DELTA;
	}

	public static boolean inRange(MapLocation pos) {
		return inRange(type, pos);
	}

	public static boolean inRange(RobotType type, MapLocation pos) {
		int dist = here.distanceSquaredTo(pos);
		return (dist >= type.attackRadiusMinSquared())
				&& (dist <= type.attackRadiusMaxSquared());
	}

	public static Direction randomDir() {
		return Direction.values()[r.nextInt(8)];
	}

	public static void attack(RobotInfo ri) throws Exception {
		if (ri.type.isAirborne()) {
			rc.attackAir(ri.location);
		} else {
			rc.attackGround(ri.location);
		}
	}

	public static void attack(MapLocation pos, RobotLevel height)
			throws Exception {
		if (height == RobotLevel.IN_AIR) {
			rc.attackAir(pos);
		} else {
			rc.attackGround(pos);
		}
	}

	public static RobotLevel getLevel(RobotInfo ri) {
		return ri.type.isAirborne() ? RobotLevel.IN_AIR : RobotLevel.ON_GROUND;
	}

	public static boolean superNear(MapLocation there) {
		return superNear(here, there);
	}

	public static boolean superNear(MapLocation here, MapLocation there) {
		return here.isAdjacentTo(there) || here.equals(there);
	}

	public static Robot senseRobotAtLocation(RobotType t, MapLocation pos)
			throws Exception {
		if (t.isAirborne()) {
			return rc.senseAirRobotAtLocation(pos);
		} else {
			return rc.senseGroundRobotAtLocation(pos);
		}
	}

	public static MapLocation add(MapLocation start, int dx, int dy) {
		return new MapLocation(start.getX() + dx, start.getY() + dy);
	}

	public static MapLocation sub(MapLocation a, MapLocation b) {
		return new MapLocation(a.getX() - b.getX(), a.getY() - b.getY());
	}

	public static MapLocation add(MapLocation start, Direction d, int amount) {
		for (int i = 0; i < amount; i++) {
			start = start.add(d);
		}
		return start;
	}

	public static MapLocation[] getAdjacent() {
		return getAdjacent(here);
	}

	public static MapLocation[] getSides() {
		return getSides(here);
	}

	public static MapLocation[] getAdjacent(MapLocation here) {
		MapLocation[] locs = new MapLocation[8];
		for (int i = 0; i < 8; i++) {
			locs[i] = here.add(Direction.values()[i]);
		}
		return locs;
	}

	public static MapLocation[] getSides(MapLocation here) {
		MapLocation[] locs = new MapLocation[4];
		for (int i = 0; i < 8; i += 2) {
			locs[i / 2] = here.add(Direction.values()[i]);
		}
		return locs;
	}

	public static Direction simpleDirectionTo(MapLocation here,
			MapLocation there) {
		int dx = there.getX() - here.getX();
		int dy = there.getY() - here.getY();
		if (dx > 0) {
			if (dy > 0) {
				return Direction.SOUTH_EAST;
			} else if (dy < 0) {
				return Direction.NORTH_EAST;
			} else {
				return Direction.EAST;
			}
		} else if (dx < 0) {
			if (dy > 0) {
				return Direction.SOUTH_WEST;
			} else if (dy < 0) {
				return Direction.NORTH_WEST;
			} else {
				return Direction.WEST;
			}
		} else {
			if (dy > 0) {
				return Direction.SOUTH;
			} else if (dy < 0) {
				return Direction.NORTH;
			} else {
				return Direction.OMNI;
			}
		}
	}

	public static boolean face(Direction d) throws Exception {
		if (dir != d) {
			rc.setDirection(d);
			return true;
		}
		return false;
	}

	public static void move(MapLocation there) throws Exception {
		move(here.directionTo(there));
	}

	public static void move(Direction d) throws Exception {
		if (dir == d) {
			rc.moveForward();
		} else if (dir == d.opposite()) {
			rc.moveBackward();
		} else {
			rc.setDirection(d);
		}
	}

	public static void tryMove(MapLocation there) throws Exception {
		tryMove(here.directionTo(there));
	}

	public static void tryMove(Direction dir) throws Exception {
		if (!rc.canMove(dir)) {
			Direction left = dir;
			Direction right = dir;
			for (int i = 0; i < 4; i++) {
				left = left.rotateLeft();
				right = right.rotateRight();
				if (rc.canMove(left)) {
					dir = left;
					break;
				} else if (rc.canMove(right)) {
					dir = right;
					break;
				}
			}
		}
		if (rc.canMove(dir)) {
			move(dir);
		}
	}

	public static boolean tryMove3(MapLocation pos) throws Exception {
		return tryMove3(here.directionTo(pos));
	}

	public static boolean tryMove3(Direction dir) throws Exception {
		if (rc.canMove(dir)) {
			move(dir);
			return true;
		} else if (rc.canMove(dir.rotateLeft())) {
			move(dir.rotateLeft());
			return true;
		} else if (rc.canMove(dir.rotateRight())) {
			move(dir.rotateRight());
			return true;
		}
		return false;
	}

	public static MapLocation closest(MapLocation toHere, MapLocation[] locs) {
		MapLocation best = null;
		int bestSqDist = Integer.MAX_VALUE;
		for (MapLocation loc : locs) {
			int sqDist = loc.distanceSquaredTo(toHere);
			if (sqDist < bestSqDist) {
				bestSqDist = sqDist;
				best = loc;
			}
		}
		return best;
	}

	public static MapLocation senseClosestFluxDeposit() throws Exception {
		MapLocation best = null;
		int bestSqDist = Integer.MAX_VALUE;
		for (FluxDeposit f : rc.senseNearbyFluxDeposits()) {
			FluxDepositInfo fi = rc.senseFluxDepositInfo(f);
			MapLocation loc = fi.location;
			int sqDist = loc.distanceSquaredTo(here);
			if (sqDist < bestSqDist) {
				bestSqDist = sqDist;
				best = loc;
			}
		}
		return best;
	}

	public static MapLocation closestArchon() throws Exception {
		return closest(here, rc.senseAlliedArchons());
	}

	public static int getArchonIndex() {
		MapLocation[] locs = rc.senseAlliedArchons();
		for (int i = 0; i < locs.length; i++) {
			MapLocation loc = locs[i];
			if (loc == here) {
				return i;
			}
		}
		throw new IllegalArgumentException("can't find archon index");
	}

	public static double dirToDeg(Direction dir) {
		return 45.0 * dir.ordinal();
	}

	public static double lerp(double t0, double v0, double t1, double v1,
			double t) {
		return (t - t0) * (v1 - v0) / (t1 - t0) + v0;
	}

	public static double lerpCap(double t0, double v0, double t1, double v1,
			double t) {
		if (t <= t0)
			return v0;
		if (t >= t1)
			return v1;
		return (t - t0) * (v1 - v0) / (t1 - t0) + v0;
	}
}
