package tribe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

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

	public static void main(String[] args) {
		MapLocation a = new MapLocation(0, 0);
		MapLocation b = new MapLocation(1, -2);
		System.out.println(U.dirTo(a, b));
	}

	// -------------------------------------
	// util
	public static RobotController rc;
	public static Robot robot;
	public static int id;
	public static Random r;
	public static Map<MapLocation, Robot> towers = new HashMap();
	public static int consider_at_most_this_many_messages;
	public static int magic_message_number;
	public static int max_message_age;
	public static int max_shared_concept_age;

	// shared concepts
	public static final int _bestGroundEnemy = 0;
	public static final int _bestMortarEnemy = 1;
	public static final int _bestAirEnemy = 2;
	public static final int _bestEnemyTower = 3;
	public static final int _bestEnemyArchon = 4;
	public static final int _numSharedConcepts = 5;
	public static boolean improvedSharedConcept = false;

	public static class SharedConcept {
		public MapLocation loc;
		public double golfScore;
		public int round;

		public SharedConcept() {
			golfScore = Double.MAX_VALUE;
		}

		public void improve(double score, MapLocation pos, int round) {
			if ((pos != null) && (score < golfScore)) {
				this.golfScore = score;
				this.loc = pos;
				this.round = round;
				improvedSharedConcept = true;
			}
		}
	}

	public static SharedConcept[] sharedConcepts = new SharedConcept[_numSharedConcepts];

	// -------------------------------------
	// desires
	public static final int soldierOrdinal = RobotType.SOLDIER.ordinal(); // 0;
	public static final int mortarOrdinal = RobotType.MORTAR.ordinal(); // 1;
	public static final int scoutOrdinal = RobotType.SCOUT.ordinal(); // 2;
	public static final int archonOrdinal = RobotType.ARCHON.ordinal(); // 3;
	public static final int bomberOrdinal = RobotType.BOMBER.ordinal(); // 4;
	public static final int sniperOrdinal = RobotType.SNIPER.ordinal(); // 5;

	// <<< Genetic Algorithm begin
	public static double[] desired_unit_percent = new double[] { 0, 0, 0, .1,
			0, .9 };
	public static double energon_to_consider_spawning_scout = 30; // [0, 50]
	public static double energon_to_consider_spawning_soldier = 30; // [0, 50]
	public static double energon_to_consider_spawning_bomber = 6; // [0, 10]
	public static double energon_to_consider_spawning_mortar = 6; // [0, 20]
	public static double energon_to_consider_spawning_sniper = 6; // [0, 20]
	public static double desire_to_turn_given_spawn_desire = 1; // [0, 1]

	public static double desire_to_move_toward_enemy_archon = 0.5; // [0, 1]
	public static double multiplier_given_that_enemy_archon_is_seen = 1.1; // [0, 3]
	public static double desire_to_move_toward_enemy_tower = 0.5; // [0, 1]
	public static double multiplier_given_that_enemy_tower_is_seen = 1.1; // [0, 3]
	public static double desire_to_move_given_spawn_desire = 1; // [0, 1]
	public static double willingness_to_move_backwards = 0.9; // [0, 1]
	
	public static double grouping_radius = 5.0; // [0, 10]
	public static double grouping_func_exponent = 4.0; // [0, 10]
	public static double grouping_desire = 1; // [0, 10]

	public static double desire_to_get_in_range_of_enemy_target = 1; // [0,
	// 1]
	public static double desire_to_approach_enemy_target = 0.7; // [0, 1]
	public static double desire_to_face_enemy_target = 1; // [0, 1]

	public static double generosity = 2.0; // [0, 10]
	public static double willingness_to_give_to_tower = 0.5; // [0, 1]

	public static double tower_dist_penalty_for_non_mortars = 4; // [-10, 10]
	public static double tower_dist_penalty_for_mortars = -4; // [-10, 10]
	public static double consider_at_most_this_many_messages_float = 3; // [0,
	// 10]
	public static double max_message_age_float = 3; // [0, 10]
	public static double max_shared_concept_age_float = 5; // [0, 10]
	public static double auto_formatting_helper = 0; // [0, 1]

	// Genetic Algorithm end >>>

	// -------------------------------------

	public RobotPlayer(RobotController rc) throws Exception {
		RobotPlayer.rc = rc;
		robot = rc.getRobot();
		id = robot.getID();
		r = new Random(rc.getRobot().getID());
		consider_at_most_this_many_messages = (int) Math
				.round(consider_at_most_this_many_messages_float);
		max_message_age = (int) Math.round(max_message_age_float);
		max_shared_concept_age = (int) Math.round(max_shared_concept_age_float);
		if (rc.getTeam() == Team.A) {
			magic_message_number = 262792139;
		} else {
			magic_message_number = 834571531;
		}
		for (int i = 0; i < _numSharedConcepts; i++) {
			sharedConcepts[i] = new SharedConcept();
		}
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
		double tower_dist_penalty = tower_dist_penalty_for_non_mortars;
		boolean isMortar = type == RobotType.MORTAR;
		if (isMortar) {
			tower_dist_penalty = tower_dist_penalty_for_mortars;
		}
		int currentRound = Clock.getRoundNum();
		boolean canAttackAir = type.canAttackAir();
		boolean canAttackGround = type.canAttackGround();

		// shared concepts
		{
			int maxRound = currentRound - max_shared_concept_age;
			for (SharedConcept s : sharedConcepts) {
				if (s.round < maxRound) {
					s.golfScore = Double.MAX_VALUE;
					s.loc = null;
				}
			}
		}
		SharedConcept bestGroundEnemy = sharedConcepts[_bestGroundEnemy];
		SharedConcept bestMortarEnemy = sharedConcepts[_bestMortarEnemy];
		SharedConcept bestAirEnemy = sharedConcepts[_bestAirEnemy];
		SharedConcept bestEnemyTower = sharedConcepts[_bestEnemyTower];
		SharedConcept bestEnemyArchon = sharedConcepts[_bestEnemyArchon];

		// receive messages
		// message:
		// ints:
		// [0] = hash
		// [1] = magic number
		// [2] = message type
		// [3] = round number + 842994
		// [4] = reserved
		//
		// for message type 197 (shared concepts message)
		// *** these scores are multiplied by 1000000 ***
		// [5] = first shared concept score...
		// locs:
		// [0] = first shared concept location...
		//
		{
			int count = 0;
			Message[] ms = rc.getAllMessages();
			for (int i = ms.length - 1; i >= 0; i--) {
				Message m = ms[i];
				int[] ints = m.ints;
				if (ints == null)
					continue;
				if (ints.length < 5)
					continue;
				int magic = ints[1];
				if (magic != magic_message_number)
					continue;
				int roundSent = ints[3] - 842994;
				if (roundSent < Clock.getRoundNum() - max_message_age)
					continue;

				int hash = ints[0];
				ints[0] = 0;
				MapLocation[] locations = m.locations;
				int computedHash = Arrays.hashCode(ints)
						+ Arrays.hashCode(locations);
				if (hash != computedHash)
					continue;

				count++;
				if (count > consider_at_most_this_many_messages)
					break;

				int messageType = ints[2];
				if (messageType == 197) {
					if (roundSent >= Clock.getRoundNum()
							- max_shared_concept_age) {
						int ints_i = 5;
						int locs_i = 0;
						for (SharedConcept s : sharedConcepts) {
							double score = (double) ints[ints_i++] / 1000000;
							MapLocation pos = locations[locs_i++];
							s.improve(score, pos, roundSent);
						}
					}
				} else {

				}
			}

			Message[] messages = rc.getAllMessages();
			int n = messages.length;
			if (n > consider_at_most_this_many_messages) {
				n = consider_at_most_this_many_messages;
			}
			for (int i = 0; i < n; i++) {
				Message m = messages[i];

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

		// nearby robots
		currentRound = Clock.getRoundNum();
		Vector<RobotInfo> adjacentFriends = new Vector();
		MapLocation bestAttackable = null;
		boolean bestAttackableInAir = false;
		improvedSharedConcept = false;
		double bestAttackable_golfScore = Double.MAX_VALUE;
		if (canAttackGround) {
			MapLocation pos = null;
			if (isMortar) {
				pos = bestMortarEnemy.loc;
			} else {
				pos = bestGroundEnemy.loc;
			}
			if ((pos != null) && rc.canAttackSquare(pos)) {
				bestAttackable = pos;
				bestAttackableInAir = false;
				if (isMortar) {
					bestAttackable_golfScore = bestMortarEnemy.golfScore;
				} else {
					bestAttackable_golfScore = bestGroundEnemy.golfScore;
				}
			}
		}
		if (canAttackAir) {
			MapLocation pos = bestAirEnemy.loc;
			if ((pos != null) && rc.canAttackSquare(pos)) {
				double score = bestAirEnemy.golfScore;
				if (score < bestAttackable_golfScore) {
					bestAttackable_golfScore = score;
					bestAttackable = pos;
					bestAttackableInAir = true;
				}
			}
		}
		{
			for (Robot r : rc.senseNearbyGroundRobots()) {
				RobotInfo i = rc.senseRobotInfo(r);
				MapLocation pos = i.location;
				int dist = archonCenter.distanceSquaredTo(pos);
				if (team.equals(i.team)) {
					if (loc.isAdjacentTo(i.location)) {
						adjacentFriends.add(i);
					}
				} else {
					double score = Math.sqrt(dist);
					double nonMortarScore = score;
					double mortarScore = score;
					if (i.type == RobotType.TOWER) {
						nonMortarScore += tower_dist_penalty_for_non_mortars;
						mortarScore += tower_dist_penalty_for_mortars;
						
						bestEnemyTower.improve(dist, pos, currentRound);
					}
					if (i.type == RobotType.ARCHON) {
						bestEnemyArchon.improve(dist, pos, currentRound);
					}
					bestGroundEnemy.improve(nonMortarScore, pos, currentRound);
					bestMortarEnemy.improve(mortarScore, pos, currentRound);

					if (canAttackGround && rc.canAttackSquare(pos)) {
						if (isMortar) {
							if (mortarScore < bestAttackable_golfScore) {
								bestAttackable_golfScore = mortarScore;
								bestAttackable = pos;
								bestAttackableInAir = false;
							}
						} else {
							if (nonMortarScore < bestAttackable_golfScore) {
								bestAttackable_golfScore = nonMortarScore;
								bestAttackable = pos;
								bestAttackableInAir = false;
							}
						}
					}
				}
			}
			for (Robot r : rc.senseNearbyAirRobots()) {
				RobotInfo i = rc.senseRobotInfo(r);
				MapLocation pos = i.location;
				int dist = archonCenter.distanceSquaredTo(pos);
				if (team.equals(i.team)) {
					if (loc.isAdjacentTo(i.location)) {
						adjacentFriends.add(i);
					}
				} else {
					double score = Math.sqrt(dist);
					bestAirEnemy.improve(score, pos, currentRound);

					if (canAttackAir && rc.canAttackSquare(pos)) {
						if (score < bestAttackable_golfScore) {
							bestAttackable_golfScore = score;
							bestAttackable = pos;
							bestAttackableInAir = true;
						}
					}
				}
			}
		}

		// shared concepts message broadcast
		if (improvedSharedConcept) {
			Message m = new Message();
			m.ints = new int[5 + _numSharedConcepts];
			m.locations = new MapLocation[_numSharedConcepts];
			m.ints[1] = magic_message_number;
			m.ints[2] = 197;
			m.ints[3] = Clock.getRoundNum() + 842994;

			int ints_i = 5;
			int locs_i = 0;
			for (SharedConcept s : sharedConcepts) {
				m.ints[ints_i++] = (int) (s.golfScore * 1000000);
				m.locations[locs_i++] = s.loc;
			}

			m.ints[0] = Arrays.hashCode(m.ints) + Arrays.hashCode(m.locations);

			rc.broadcast(m);
		}

		// attacking
		if (!rc.isAttackActive()) {
			if (bestAttackable != null) {
				if (bestAttackableInAir) {
					rc.attackAir(bestAttackable);
				} else {
					rc.attackGround(bestAttackable);
				}
				return;
			}
		}
		// attack-moving
		{
			MapLocation target = null;
			double target_golfScore = Double.MAX_VALUE;
			if (isMortar) {
				target = bestMortarEnemy.loc;
			} else {
				if (canAttackAir) {
					double score = bestAirEnemy.golfScore;
					MapLocation pos = bestAirEnemy.loc;
					if ((pos != null) && (score < target_golfScore)) {
						target_golfScore = score;
						target = pos;
					}
				}
				if (canAttackGround) {
					double score = bestGroundEnemy.golfScore;
					MapLocation pos = bestGroundEnemy.loc;
					if ((pos != null) && (score < target_golfScore)) {
						target_golfScore = score;
						target = pos;
					}
				}
			}
			if (target != null) {
				int distSq = loc.distanceSquaredTo(target);
				if (distSq > type.attackRadiusMaxSquared()) {
					// too far, let's get closer
					Direction d = U.dirTo(loc, target);
					moveDesire[d.ordinal()] += desire_to_get_in_range_of_enemy_target;
					moveDesire[d.rotateLeft().ordinal()] += desire_to_get_in_range_of_enemy_target / 2.0;
					moveDesire[d.rotateRight().ordinal()] += desire_to_get_in_range_of_enemy_target / 2.0;
				} else if (distSq < type.attackRadiusMinSquared()) {
					// too close, let's get further
					Direction d = U.dirTo(loc, target).opposite();
					moveDesire[d.ordinal()] += desire_to_get_in_range_of_enemy_target;
					moveDesire[d.rotateLeft().ordinal()] += desire_to_get_in_range_of_enemy_target / 2.0;
					moveDesire[d.rotateRight().ordinal()] += desire_to_get_in_range_of_enemy_target / 2.0;
				} else {
					// let's face-em
					Direction d = U.dirTo(loc, target);
					faceDesire[d.ordinal()] += desire_to_face_enemy_target;
					if (type.attackAngle() > 46.0) {
						faceDesire[d.rotateLeft().ordinal()] += desire_to_face_enemy_target / 2.0;
						faceDesire[d.rotateRight().ordinal()] += desire_to_face_enemy_target / 2.0;
					}

					// approach'em anyway
					if (type.attackRadiusMinSquared() == 0) {
						moveDesire[d.ordinal()] += desire_to_approach_enemy_target;
						moveDesire[d.rotateLeft().ordinal()] += desire_to_approach_enemy_target / 2.0;
						moveDesire[d.rotateRight().ordinal()] += desire_to_approach_enemy_target / 2.0;
					}
				}
			}
		}

		// healing
		double greatestNeed = -Double.MAX_VALUE;
		RobotInfo mostInNeed = null;
		for (RobotInfo i : adjacentFriends) {
			double need = 101.0 - (100.0 * (i.energonLevel / i.maxEnergon))
					- (i.energonReserve / GameConstants.ENERGON_RESERVE_SIZE);
			if (i.type == RobotType.TOWER) {
				need *= willingness_to_give_to_tower;
			}
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

		// want to be near enemy archon
		{
			Direction dir = rc.senseEnemyArchon();
			double desire = desire_to_move_toward_enemy_archon;
			if (bestEnemyArchon.loc != null) {
				desire *= multiplier_given_that_enemy_archon_is_seen;
			}
			if (dir == Direction.OMNI) {
				moveDesire[Direction.NONE.ordinal()] = desire;
			} else {
				addBugMoveDesire(moveDesire, desire, dir);
			}
		}
		// want to be near enemy tower
		{
			Direction dir = rc.senseClosestUnknownTower();
			double desire = desire_to_move_toward_enemy_tower;
			if (bestEnemyTower.loc != null) {
				desire *= multiplier_given_that_enemy_tower_is_seen;
			}
			if (dir == Direction.OMNI) {
				moveDesire[Direction.NONE.ordinal()] = desire;
			} else {
				addBugMoveDesire(moveDesire, desire, dir);
			}
		}
		// want to group
		{
			double radius = Math.sqrt(archonCenter.distanceSquaredTo(loc));
			double desire = grouping_desire;
			if (radius < grouping_radius) {
				desire *= Math.pow(radius / grouping_radius, grouping_func_exponent);
			}
			addBugMoveDesire(moveDesire, desire, U.dirTo(loc, archonCenter));
		}

		// ----------------------------------------------------------------------------------------

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

	public static void addBugMoveDesire(double[] dest, double desire,
			Direction dir) {
		int d = dir.ordinal();
		dest[d] += desire;
		dest[(d + 1) % 8] += desire / 3;
		dest[(d + 2) % 8] += desire / 6;
		dest[(d + 3) % 8] += desire / 8;
		dest[(d + 4) % 8] += desire / 7;
		dest[(d + 5) % 8] += desire / 5;
		dest[(d + 6) % 8] += desire / 4;
		dest[(d + 7) % 8] += desire / 2;
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
