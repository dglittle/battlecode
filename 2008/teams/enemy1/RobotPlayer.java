package enemy1;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;

public class RobotPlayer implements Runnable {

	// -------------------------------------
	// util
	public static RobotController rc;
	public static Robot robot;
	public static int id;
	public static Random r;
	public static Map<MapLocation, Robot> towers = new HashMap();

	// -------------------------------------
	// desires
	public static int soldierOrdinal = RobotType.SOLDIER.ordinal(); // 0;
	public static int mortarOrdinal = RobotType.MORTAR.ordinal(); // 1;
	public static int scoutOrdinal = RobotType.SCOUT.ordinal(); // 2;
	public static int archonOrdinal = RobotType.ARCHON.ordinal(); // 3;
	public static int bomberOrdinal = RobotType.BOMBER.ordinal(); // 4;
	public static int sniperOrdinal = RobotType.SNIPER.ordinal(); // 5;
	public static double[] desired_unit_percent = new double[] { 0.1, 0.1, 0.1,
			0.1, 0.5, 0.1 };
	public static double energon_to_consider_spawning_scout = 30;
	public static double energon_to_consider_spawning_soldier = 30;
	public static double energon_to_consider_spawning_bomber = 6;
	public static double energon_to_consider_spawning_mortar = 6;
	public static double energon_to_consider_spawning_sniper = 6;
	public static double desire_to_turn_given_spawn_desire = 1;

	public static double desire_to_move_toward_enemy = 0.5;
	public static double desire_to_move_given_spawn_desire = 1;
	public static double willingness_to_move_backwards = 0.9; // max is 1.0

	public static double generosity = 2.0;

	// -------------------------------------

	public RobotPlayer(RobotController rc) throws Exception {
		RobotPlayer.rc = rc;
		robot = rc.getRobot();
		id = robot.getID();
		r = new Random(rc.getRobot().getID());
	}

	public static void makeMovementDecision() throws Exception {
		// --------------------------
		double[] moveDesire = new double[9];
		double[] faceDesire = new double[8];

		double spawnA = 0;
		double spawnB = 0;

		// --------------------------
		// self
		RobotInfo info = rc.senseRobotInfo(robot);
		Team team = info.team;
		RobotType type = rc.getRobotType();
		MapLocation loc = rc.getLocation();
		Direction forward = rc.getDirection();
		int forwardOrdinal = forward.ordinal();
		int locX = loc.getX();
		int locY = loc.getY();
		boolean isArchon = type == RobotType.ARCHON;
		double energon = info.energonLevel;
		double energonReserve = info.energonReserve;
		double maxEnergon = info.maxEnergon;
		double upkeep = type.energonUpkeep();
		boolean inAir = type.isAirborne();
		RobotLevel level = inAir ? RobotLevel.IN_AIR : RobotLevel.ON_GROUND;
		MapLocation front = loc.add(forward);

		// nearby robots
		Vector<RobotInfo> adjacentFriends = new Vector();
		Vector<RobotInfo> attackables = new Vector();
		boolean canAttackGround = type.canAttackGround();
		for (Robot r : rc.senseNearbyGroundRobots()) {
			RobotInfo i = rc.senseRobotInfo(r);
			if (team.equals(i.team)) {
				if (loc.isAdjacentTo(i.location)) {
					adjacentFriends.add(i);
				}
			} else if (canAttackGround && rc.canAttackSquare(i.location)) {
				attackables.add(i);
			}
		}
		boolean canAttackAir = type.canAttackAir();
		for (Robot r : rc.senseNearbyAirRobots()) {
			RobotInfo i = rc.senseRobotInfo(r);
			if (loc.isAdjacentTo(i.location)) {
				if (team.equals(i.team)) {
					adjacentFriends.add(i);
				}
			} else if (canAttackAir && rc.canAttackSquare(i.location)) {
				attackables.add(i);
			}
		}

		// archons
		double archonCenterX = 0.0;
		double archonCenterY = 0.0;
		MapLocation[] archons = rc.senseAlliedArchons();
		int numArchons = archons.length;
		boolean nearArchon = false;
		for (MapLocation there : archons) {
			if (U.near(loc, there)) {
				nearArchon = true;
			}
			archonCenterX += there.getX();
			archonCenterY += there.getY();
		}
		MapLocation archonCenter = new MapLocation((int) Math
				.round(archonCenterX / numArchons), (int) Math
				.round(archonCenterY / numArchons));

		// attacking
		RobotInfo bestTarget = null;
		{
			int closestDist = Integer.MAX_VALUE;
			for (RobotInfo i : attackables) {
				MapLocation pos = i.location;
				int dist = archonCenter.distanceSquaredTo(pos);
				if (dist < closestDist) {
					closestDist = dist;
					bestTarget = i;
				}
			}
		}
		if (!rc.isAttackActive()) {
			if (bestTarget != null) {
				if (bestTarget.type.isAirborne()) {
					rc.attackAir(bestTarget.location);
				} else {
					rc.attackGround(bestTarget.location);
				}
				return;
			}
		}

		// healing
		double greatestNeed = -Double.MAX_VALUE;
		RobotInfo mostInNeed = null;
		for (RobotInfo i : adjacentFriends) {
			double need = 101.0 - (100.0 * (i.energonLevel / i.maxEnergon))
					- (i.energonReserve / GameConstants.ENERGON_RESERVE_SIZE);
			if (need > greatestNeed) {
				greatestNeed = need;
				mostInNeed = i;
			}
		}
		if (mostInNeed != null) {
			double myNeed = 101.0 - (100.0 * (energon / maxEnergon))
					- (energonReserve / GameConstants.ENERGON_RESERVE_SIZE);
			double expectedIncrease = Math.min(
					GameConstants.ENERGON_TRANSFER_RATE, energonReserve)
					- upkeep
					+ (isArchon ? GameConstants.ARCHON_PRODUCTION : 0.0);
			double willingToGive = Math.max(0.0, (energon + expectedIncrease)
					- maxEnergon);

			double mostInNeedReserve = mostInNeed.energonReserve;
			if (greatestNeed > myNeed) {
				willingToGive = Math
						.max(willingToGive, GameConstants.ENERGON_TRANSFER_RATE
								- mostInNeedReserve);
			}

			// be generous
			willingToGive *= generosity;

			// hard limits
			willingToGive = Math.min(willingToGive,
					GameConstants.ENERGON_RESERVE_SIZE - mostInNeedReserve);
			willingToGive = Math.min(willingToGive, energon / 2.0);

			if (willingToGive > 0) {
				rc.transferEnergon(willingToGive, mostInNeed.location,
						mostInNeed.type.isAirborne() ? RobotLevel.IN_AIR
								: RobotLevel.ON_GROUND);
				energon -= willingToGive;
			}
		}

		// surrounding squares
		MapLocation[] locs = new MapLocation[8];
		boolean[] couldGo = new boolean[8];
		boolean[] canGo = new boolean[8];
		for (int i = 0; i < 8; i++) {
			Direction dir = Direction.values()[i];
			MapLocation there = loc.add(dir);
			locs[i] = there;
			if (!rc.canSenseSquare(there))
				continue;
			TerrainTile t = rc.senseTerrainTile(there);
			boolean could = t.isTraversableAtHeight(level);
			couldGo[i] = could;
			if (could) {
				if (inAir) {
					canGo[i] = rc.senseAirRobotAtLocation(there) == null;
				} else {
					canGo[i] = rc.senseGroundRobotAtLocation(there) == null;
				}
			}
		}

		// towers
		double closestTowerDist = Double.MAX_VALUE;
		MapLocation closestTowerPos = null;
		for (MapLocation pos : rc.senseAlliedTowers()) {
			Robot tower = towers.get(pos);
			if (tower == null) {
				if (rc.canSenseSquare(pos)) {
					tower = rc.senseGroundRobotAtLocation(pos);
					towers.put(pos, tower);
				}
			}
			double dist = Math.sqrt(loc.distanceSquaredTo(pos));
			if (tower != null) {
				dist -= rc.senseTowerSpawnRadius(tower);
			} else {
				dist -= GameConstants.MIN_SPAWN_RADIUS;
			}
			if (dist < closestTowerDist) {
				closestTowerDist = dist;
				closestTowerPos = pos;
			}
		}
		boolean canSpawn = closestTowerDist <= 0;

		// units
		double production = numArchons * GameConstants.ARCHON_PRODUCTION;
		int[] unitCount = new int[6];
		double[] unitCost = new double[6];
		int[] desiredCount = new int[6];
		int airCount = 0;
		int groundCount = 0;
		int desiredAirCount = 0;
		int desiredGroundCount = 0;
		for (int i = 0; i < 6; i++) {
			RobotType t = RobotType.values()[i];
			if (t == RobotType.ARCHON)
				continue;
			int count = rc.getUnitCount(t);
			double cost = t.energonUpkeep();
			unitCount[i] = count;
			unitCost[i] = cost * count;
			int desired = (int) Math.round(desired_unit_percent[i] * production
					/ cost);
			if (t.isAirborne()) {
				airCount += count;
				desiredAirCount += desired;
			} else {
				groundCount += count;
				desiredGroundCount += desired;
			}
			desiredCount[i] = desired;
		}

		// spawning
		if (isArchon) {
			if (canSpawn) {
				if (energon >= energon_to_consider_spawning_scout) {
					int count = desiredAirCount - airCount;
					if (count > 0) {
						if (rc.senseAirRobotAtLocation(front) == null) {
							spawnA = count;
						} else {
							double desire = desire_to_turn_given_spawn_desire
									* count;
							for (int i = 0; i < 8; i++) {
								if (rc.senseAirRobotAtLocation(locs[i]) == null) {
									faceDesire[i] += desire;
								}
							}
						}
					}
				}
				if (energon >= energon_to_consider_spawning_soldier) {
					int count = desiredGroundCount - groundCount;
					if (count > 0) {
						if (canGo[forwardOrdinal]) {
							spawnB = count;
						} else {
							double desire = desire_to_turn_given_spawn_desire
									* count;
							for (int i = 0; i < 8; i++) {
								if (canGo[i]) {
									faceDesire[i] += desire;
								}
							}
						}
					}
				}
			} else {
				int countA = desiredAirCount - airCount;
				int countB = desiredGroundCount - groundCount;
				int count = countA + countB;
				if (count > 0) {
					double desire = count * desire_to_move_given_spawn_desire;
					Direction dir = rc.senseClosestUnknownTower();
					if (closestTowerPos != null) {
						dir = U.dirTo(loc, closestTowerPos);
					}
					moveDesire[dir.ordinal()] += desire;
					moveDesire[dir.rotateLeft().ordinal()] += desire / 2;
					moveDesire[dir.rotateRight().ordinal()] += desire / 2;
				}
			}
		} else if (type == RobotType.SCOUT) {
			if (nearArchon && energon >= energon_to_consider_spawning_bomber) {
				int count = desiredCount[bomberOrdinal]
						- unitCount[bomberOrdinal];
				if (count > 0) {
					spawnA = count;
				}
			}
		} else if (type == RobotType.SOLDIER) {
			if (nearArchon) {
				if (energon >= energon_to_consider_spawning_mortar) {
					int count = desiredCount[mortarOrdinal]
							- unitCount[mortarOrdinal];
					if (count > 0) {
						spawnA = count;
					}
				}
				if (energon >= energon_to_consider_spawning_sniper) {
					int count = desiredCount[sniperOrdinal]
							- unitCount[sniperOrdinal];
					if (count > 0) {
						spawnB = count;
					}
				}
			}
		}

		// want to be near enemy
		{
			double desire = desire_to_move_toward_enemy;
			Direction dir = rc.senseClosestUnknownTower();
			moveDesire[dir.ordinal()] += desire;
			moveDesire[dir.rotateLeft().ordinal()] += desire / 2;
			moveDesire[dir.rotateRight().ordinal()] += desire / 2;
		}

		// --------------------------

		// spawn
		if (spawnA > 0 || spawnB > 0) {
			if (spawnA > spawnB) {
				if (isArchon) {
					rc.spawn(RobotType.SCOUT);
				} else if (inAir) {
					rc.evolve(RobotType.BOMBER);
				} else {
					rc.evolve(RobotType.MORTAR);
				}
			} else {
				if (isArchon) {
					rc.spawn(RobotType.SOLDIER);
				} else {
					rc.evolve(RobotType.SNIPER);
				}
			}
			return;
		}

		// face / move
		if (!rc.isMovementActive()) {
			for (int i = 0; i < 8; i++) {
				if (!canGo[i])
					moveDesire[i] = 0.0;
			}

			int bestIndex = 0;
			double bestValue = -Double.MAX_VALUE;
			for (int i = 0; i < 8; i++) {
				double desire = faceDesire[i];
				int oppositeDir = U.oppositeDir(i);
				desire += moveDesire[i];
				desire += (moveDesire[U.oppositeDir(i)] * willingness_to_move_backwards);
				if (desire > bestValue) {
					bestValue = desire;
					bestIndex = i;
				}
			}
			Direction newFace = Direction.values()[bestIndex];
			if (forward != newFace) {
				rc.setDirection(newFace);
			} else {
				double forwardDesire = moveDesire[forwardOrdinal];
				double backwardDesire = moveDesire[U
						.oppositeDir(forwardOrdinal)];
				double stayDesire = moveDesire[Direction.NONE.ordinal()];
				double goDesire = Math.max(forwardDesire, backwardDesire);
				if (goDesire > stayDesire) {
					if (forwardDesire > backwardDesire) {
						rc.moveForward();
					} else {
						rc.moveBackward();
					}
				}
			}
		}
	}

	public void run() {
		while (true) {
			try {
				makeMovementDecision();
			} catch (Exception e) {
				e.printStackTrace();
			}
			rc.yield();
		}
	}
}
