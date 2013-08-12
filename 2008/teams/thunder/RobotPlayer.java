package thunder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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

	public static double energon_transfer_cap = 4;

	// -------------------------------------
	// util
	public static RobotController rc;

	public static int[] initial_archon_group_ids = new int[] { 0, 0, 0, 0, 0,
			0, 1, 2 }; // [0, 8]
	public static final int group_type_tower_ninja = 0;
	public static final int group_type_thunder_cloud = 1;
	public static int[] group_id_to_group_type = new int[] {
			group_type_thunder_cloud, group_type_tower_ninja,
			group_type_tower_ninja };
	public static double[][] desired_unit_percent_for_group_type = new double[][] {
			{ 0, 0.4, 0, 0.6, 0, 0 }, { 0, 0, .2, .2, .6, 0 } };

	// <<< Genetic Algorithm begin

	// group movement
	public static double leader_patience_time = 5; // [0, 20]

	// spawning
	public static double[] energon_to_consider_spawning_unit = new double[] {
			33, 6, 33, 0, 6, 6 }; // [0, 50]
	public static double army_size_threshold_to_want_to_spawn = 0.5; // [0,
	// 1]
	public static double army_size_threshold_to_be_done_spawning = 0.9; // [0,
	// 1]

	// attacking
	public static double[] enemy_type_value_as_target = new double[] { 1, 1, 1,
			1, 1, 1, 1 }; // [-10, 10]
	public static double[] enemy_type_value_as_target_for_mortars = new double[] {
			1, 1, 1, 1, 1, 1, -5 }; // [-10, 10]
	public static double fight_if_enemy_in_this_range = 4.0; // [0, 10]
	public static double flee_if_enemy_in_this_range = 6.0; // [0, 10]

	// healing
	public static double generosity = 2.8; // [0, 10]
	public static double willingness_to_give_to_tower = 0.38; // [0, 1]

	// update frequencies
	public static double freq_check_evolution_counter = 5; // [0, 20]
	public static double freq_check_if_we_should_spawn = 6; // [0, 20]
	public static double freq_update_nearby_robot_info = 5; // [0, 20]

	// messaging
	public static double max_shared_concept_age = 5; // [0, 20]
	public static double group_update_min_period = 10; // [0, 20]

	// navigation
	public static double flee_countdown_init = 15; // [0, 100]
	public static double fight_countdown_init = 100; // [0, 1000]
	public static double see_if_we_took_tower_period = 10; // [0, 20]

	// Genetic Algorithm end >>>

	// -------------------------------------
	public RobotPlayer(RobotController rc) throws Exception {
		try {
			RobotPlayer.rc = rc;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int moveIfCan_faceIfCould(Direction dir) throws Exception {
		if (rc.canMove(dir)) {
			Direction forward = rc.getDirection();
			if (forward == dir) {
				rc.moveForward();
				return 2;
			} else if (forward.opposite() == dir) {
				rc.moveBackward();
				return 2;
			} else {
				rc.setDirection(dir);
				return 1;
			}
		}
		return 0;
	}

	public static boolean nearArchon() throws Exception {
		MapLocation here = rc.getLocation();
		for (MapLocation archon : rc.senseAlliedArchons()) {
			if (archon.isAdjacentTo(here) || archon.equals(here)) {
				return true;
			}
		}
		return false;
	}

	public static void towerNinjaHealing(MapLocation here, RobotInfo me,
			RobotType type, Team team, Set<Integer> members) throws Exception {
		if (type == RobotType.ARCHON) {
			Vector<RobotInfo> adjacentFriends = new Vector();
			Direction cursor = Direction.NORTH;
			for (int i = 0; i < 8; i++) {
				MapLocation spot = here.add(cursor);
				if (!rc.canSenseSquare(spot))
					continue;
				Robot r = rc.senseAirRobotAtLocation(spot);
				if (r != null && members.contains(r.getID())) {
					RobotInfo info = rc.senseRobotInfo(r);
					if (info.team == team) {
						adjacentFriends.add(info);
					}
				}
				r = rc.senseGroundRobotAtLocation(spot);
				if (r != null && members.contains(r.getID())) {
					RobotInfo info = rc.senseRobotInfo(r);
					if (info.team == team) {
						adjacentFriends.add(info);
					}
				}
				cursor = cursor.rotateLeft();
			}
			if (rc.getRobotType().isAirborne()) {
				Robot r = rc.senseGroundRobotAtLocation(here);
				if (r != null && members.contains(r.getID())) {
					RobotInfo info = rc.senseRobotInfo(r);
					if (info.team == team) {
						adjacentFriends.add(info);
					}
				}
			} else {
				Robot r = rc.senseAirRobotAtLocation(here);
				if (r != null && members.contains(r.getID())) {
					RobotInfo info = rc.senseRobotInfo(r);
					if (info.team == team) {
						adjacentFriends.add(info);
					}
				}
			}
			// fill the reserve of the guy with the lowest reserve
			double lowestReserve = Double.MAX_VALUE;
			RobotInfo guy = null;
			for (RobotInfo i : adjacentFriends) {
				if (i.energonReserve < lowestReserve) {
					lowestReserve = i.energonReserve;
					guy = i;
				}
			}
			if (guy != null) {
				// we're willing to transfer everything, except our last 30
				// energon
				double willingToTransfer = me.energonLevel - 30;
				// but we don't want to transfer more than can fit in their
				// reserve
				double willFit = GameConstants.ENERGON_RESERVE_SIZE
						- guy.energonReserve;
				if (willingToTransfer > willFit) {
					willingToTransfer = willFit;
				}
				if (willingToTransfer > 0) {
					rc.transferEnergon(willingToTransfer, guy.location,
							guy.type.isAirborne() ? RobotLevel.IN_AIR
									: RobotLevel.ON_GROUND);
				}
			}
		}
	}

	public static void thunderCloudHealing(MapLocation here, RobotInfo me,
			RobotType type, Team team, Set<Integer> members,
			Direction groupDirection) throws Exception {
		Vector<RobotInfo> adjacentFriends = new Vector();
		Direction cursor = Direction.NORTH;
		int groupDirectionOrdinal = groupDirection.ordinal();
		for (int i = 0; i < 8; i++) {
			if (type != RobotType.ARCHON) {
				// make sure they are in front of us
				if (U.absDirectionDiff(cursor.ordinal(), groupDirectionOrdinal) > 1)
					continue;
			}
			MapLocation spot = here.add(cursor);
			if (!rc.canSenseSquare(spot))
				continue;
			Robot r = rc.senseAirRobotAtLocation(spot);
			if (r != null) {
				RobotInfo info = rc.senseRobotInfo(r);
				if ((info.team == team) && (info.type != RobotType.ARCHON)) {
					adjacentFriends.add(info);
				}
			}
			r = rc.senseGroundRobotAtLocation(spot);
			if (r != null) {
				RobotInfo info = rc.senseRobotInfo(r);
				if ((info.team == team) && (info.type != RobotType.ARCHON)) {
					adjacentFriends.add(info);
				}
			}
			cursor = cursor.rotateLeft();
		}
		if (type == RobotType.ARCHON) {
			if (rc.getRobotType().isAirborne()) {
				Robot r = rc.senseGroundRobotAtLocation(here);
				if (r != null) {
					RobotInfo info = rc.senseRobotInfo(r);
					if ((info.team == team) && (info.type != RobotType.ARCHON)) {
						adjacentFriends.add(info);
					}
				}
			} else {
				Robot r = rc.senseAirRobotAtLocation(here);
				if (r != null) {
					RobotInfo info = rc.senseRobotInfo(r);
					if ((info.team == team) && (info.type != RobotType.ARCHON)) {
						adjacentFriends.add(info);
					}
				}
			}
		}
		// fill the reserve of the guy with the lowest reserve
		double lowestReserve = Double.MAX_VALUE;
		RobotInfo guy = null;
		for (RobotInfo i : adjacentFriends) {
			if (i.energonReserve < lowestReserve) {
				lowestReserve = i.energonReserve;
				guy = i;
			}
		}
		if (guy != null) {
			// we're willing to transfer thing, except our last 30
			// energon
			double keepThisMuch = 30;
			if (type != RobotType.ARCHON) {
				keepThisMuch = 2;
			}
			double willingToTransfer = me.energonLevel - keepThisMuch;
			// but we don't want to transfer more than can fit in their
			// reserve
			double willFit = GameConstants.ENERGON_RESERVE_SIZE
					- guy.energonReserve;
			if (willingToTransfer > willFit) {
				willingToTransfer = willFit;
			}
			if (willingToTransfer > 0) {
				rc.transferEnergon(willingToTransfer, guy.location, guy.type
						.isAirborne() ? RobotLevel.IN_AIR
						: RobotLevel.ON_GROUND);
			}
		}
	}

	public static void doHealing() throws Exception {
		// rc.setIndicatorString(1, "" + rc.getEnergonReserve());
		if (rc.getEnergonLevel() < rc.getRobotType().maxEnergon() / 2)
			return;
		MapLocation here = rc.getLocation();
		Team team = rc.getTeam();
		boolean isArchon = rc.getRobotType() == RobotType.ARCHON;
		Vector<RobotInfo> adjacentFriends = new Vector();
		Direction cursor = Direction.NORTH;
		for (int i = 0; i < 8; i++) {
			MapLocation spot = here.add(cursor);
			if (!rc.canSenseSquare(spot))
				continue;
			Robot r = rc.senseAirRobotAtLocation(spot);
			if (r != null) {
				RobotInfo info = rc.senseRobotInfo(r);
				if (info.team == team) {
					adjacentFriends.add(info);
				}
			}
			r = rc.senseGroundRobotAtLocation(spot);
			if (r != null) {
				RobotInfo info = rc.senseRobotInfo(r);
				if (info.team == team) {
					adjacentFriends.add(info);
				}
			}
			cursor = cursor.rotateLeft();
		}
		if (rc.getRobotType().isAirborne()) {
			Robot r = rc.senseGroundRobotAtLocation(here);
			if (r != null) {
				RobotInfo info = rc.senseRobotInfo(r);
				if (info.team == team) {
					adjacentFriends.add(info);
				}
			}
		} else {
			Robot r = rc.senseAirRobotAtLocation(here);
			if (r != null) {
				RobotInfo info = rc.senseRobotInfo(r);
				if (info.team == team) {
					adjacentFriends.add(info);
				}
			}
		}
		// charge up weakest
		double minE = Double.MAX_VALUE;
		RobotInfo minI = null;
		Robot minR = null;
		for (RobotInfo i : adjacentFriends) {
			if (i.eventualEnergon < minE
					&& 10 - i.energonReserve > energon_transfer_cap) {
				minE = i.eventualEnergon;
				minI = i;
			}
		}
		// don't bother with micro-transfers
		if (minI != null && minI.eventualEnergon < rc.getEnergonLevel() - .1) {
			// rc.setIndicatorString(0, ""
			// + (rc.getEnergonLevel() - minI.eventualEnergon) / 2 + " "
			// + minI.location);
			rc.transferEnergon(Math.min(energon_transfer_cap, (rc
					.getEnergonLevel() - minI.eventualEnergon) / 2),
					minI.location, minI.type.isAirborne() ? RobotLevel.IN_AIR
							: RobotLevel.ON_GROUND);
		}
	}

	public static void doHealing2() throws Exception {
		MapLocation here = rc.getLocation();
		Team team = rc.getTeam();
		boolean isArchon = rc.getRobotType() == RobotType.ARCHON;
		Vector<RobotInfo> adjacentFriends = new Vector();
		Direction cursor = Direction.NORTH;
		for (int i = 0; i < 8; i++) {
			MapLocation spot = here.add(cursor);
			if (!rc.canSenseSquare(spot))
				continue;
			Robot r = rc.senseAirRobotAtLocation(spot);
			if (r != null) {
				RobotInfo info = rc.senseRobotInfo(r);
				if (info.team == team) {
					adjacentFriends.add(info);
				}
			}
			r = rc.senseGroundRobotAtLocation(spot);
			if (r != null) {
				RobotInfo info = rc.senseRobotInfo(r);
				if (info.team == team) {
					adjacentFriends.add(info);
				}
			}
			cursor = cursor.rotateLeft();
		}
		if (rc.getRobotType().isAirborne()) {
			Robot r = rc.senseGroundRobotAtLocation(here);
			if (r != null) {
				RobotInfo info = rc.senseRobotInfo(r);
				if (info.team == team) {
					adjacentFriends.add(info);
				}
			}
		} else {
			Robot r = rc.senseAirRobotAtLocation(here);
			if (r != null) {
				RobotInfo info = rc.senseRobotInfo(r);
				if (info.team == team) {
					adjacentFriends.add(info);
				}
			}
		}
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
			double energon = rc.getEnergonLevel();
			double maxEnergon = rc.getMaxEnergonLevel();
			double energonReserve = rc.getEnergonReserve();
			double upkeep = rc.getRobotType().energonUpkeep();
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
	}

	public void run() {
		try {
			// ------------------------------------------------------------------------
			// init

			// general info
			RobotController rc = this.rc;
			Robot robot = rc.getRobot();
			int id = robot.getID();
			Team team = rc.senseRobotInfo(robot).team;
			Random rand = new Random(0x8274871958248249L + id);
			boolean isArchon = rc.getRobotType() == RobotType.ARCHON;
			boolean isLeader = false;
			boolean inAir = rc.getRobotType().isAirborne();
			int birthday = 0;
			MapLocation lastLocation = rc.getLocation();
			MapLocation lastLastLocation = rc.getLocation();
			Map<MapLocation, Robot> towers = new HashMap();

			// group info
			// ... utility
			int[] groupSizeToRadiusSq = new int[] { 2, 2, 2, 2, 2, 2, 2, 2, 2,
					2, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 8, 8, 8, 8, 9, 9, 9,
					9, 10, 10, 10, 10, 10, 10, 10, 10, 13, 13, 13, 13, 13, 13,
					13, 13, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 17, 17, 18,
					18, 18, 18, 20, 20, 20, 20, 20, 20, 20, 20 };
			// int[] groupSizeToRadiusSq = new int[] { 0, 0, 1, 1, 1, 1, 2, 2,
			// 2,
			// 2, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 8, 8, 8, 8, 9, 9, 9,
			// 9, 10, 10, 10, 10, 10, 10, 10, 10, 13, 13, 13, 13, 13, 13,
			// 13, 13, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 17, 17, 18,
			// 18, 18, 18, 20, 20, 20, 20, 20, 20, 20, 20 };
			// ... basic info
			int groupId = -1;
			int groupType = -1;
			MapLocation groupGroundCenter = rc.getLocation();
			Direction groupDirection = Direction.NORTH;
			// ... computed values (based on unit counts)
			boolean unitCountsChanged = false;
			int groupGroundRadiusSq = 9;
			int groupAirRadiusSq = 9;
			MapLocation groupAirCenter = rc.getLocation();
			// ... state
			final int state_spawn = 0;
			final int state_seek_tower = 1;
			final int state_seek_archon = 2;
			final int state_fight = 3;
			final int state_flee = 4;
			final int state_fight_and_seek = 5;
			final int state_go_here = 6;
			int state = state_spawn;
			MapLocation goHere = null;
			// ... members
			Set<Integer> groupMembers = new HashSet<Integer>();
			// ... targets
			MapLocation bestGroundTarget = null;
			double bestGroundTarget_score = 0;
			int bestGroundTarget_time = 0;
			MapLocation closestEnemyTower = null;
			int closestEnemyTower_distSq = Integer.MAX_VALUE;
			// ... unit counts
			final int _soldier = RobotType.SOLDIER.ordinal();
			final int _mortar = RobotType.MORTAR.ordinal();
			final int _scout = RobotType.SCOUT.ordinal();
			final int _archon = RobotType.ARCHON.ordinal();
			final int _bomber = RobotType.BOMBER.ordinal();
			final int _sniper = RobotType.SNIPER.ordinal();
			int[] unitCounts = new int[6];
			int[] desiredUnitCounts = new int[6];
			int oldArchonCount = 0;

			// tower ninja stuff
			boolean feelingHealthy = false;
			int towerNinjaWindCounter = 20;
			int towerNinjaWindDirection = 1;
			MapLocation lastSafePoint = rc.getLocation();
			int towerNinjaSafePointCounter = 0;

			// messages
			int team_channel_magic_num = team == Team.A ? 959431883
					: -1258411499;
			Set<Integer> oldMessages = new HashSet();

			// bug mode
			boolean bugMode = false;
			int bugTurnDirection = 0;
			int bugDistSqToBeat = 0;
			Direction bugDirection = null;
			MapLocation bugTarget = null;

			// misc
			MapLocation closestEnemyArchon = null;
			int checkEvolutionCounter = 0;
			int roundWhenGroupCanMove = 0;
			boolean updatedRoundWhenGroupCanMove = false;
			int seeIfWeShouldSpawn = 0;
			int seeIfWeShouldFight = 0;
			int updateNearbyRobotInfoCount = 0;
			int enemyTowersInRangeCount = 0;
			int goHereCountdown = 0;
			int stepsSinceSawEnemyTower = 100; // start with big value, as if
			// we walked here from afar
			int roundsSinceStepping = 0;

			// leader
			int waitToGetToCenter = 0;
			int updateIsLeader_countDown = 0;
			int updateIsLeader_lastTotalArchonCount = 0;

			// broadcasting
			boolean sendGroupUpdate = false;
			int sendGroupUpdateCountdown = 0;
			int addRobot_inAir = 0;
			MapLocation addRobot_location = null;

			// navigation
			Direction prevNavigationDirection = null;
			int prevNavigationState = -1;
			MapLocation[] ripples = null;
			int rippleCountdown = 0;
			int fatWallCountdown = 0;
			int fleeCountdown = 0;
			Direction fleeDirection = null;
			int fightCountdown = 0;
			int seeIfWeTookTower = 0;
			int demeanor = -1;

			// tower war detection
			MapLocation lastEnemyTower = null;
			MapLocation lastTakenTower = null;

			// type specific initialization
			if (isArchon) {
				// find out group id
				MapLocation[] locs = rc.senseAlliedArchons();
				for (int i = 0; i < locs.length; i++) {
					if (robot.equals(rc.senseGroundRobotAtLocation(locs[i]))) {
						groupId = (int) initial_archon_group_ids[i];
						groupType = group_id_to_group_type[groupId];
						break;
					}
				}

				// map (about 4 rounds to explore the map in the OMNI direction)
				RegexMapModule.init(rc);
				RegexMapModule.archonExplore(Direction.OMNI);

				sendGroupUpdate = true;
				addRobot_inAir = 2;
				addRobot_location = rc.getLocation();
			}

			// ------------------------------------------------------------------------
			// loop

			// ------------------------------------------------------------------------
			// messages
			//
			// ints:
			// [0] = (round_num << 16) & (random_int >> 16)
			// [1] = [0] ^ team_channel_magic_num
			// [2] = ...
			// [3] = group direction
			// [4] = state
			// [5] = group id
			// [6] = 0 == ground, 1 == in air (2 == just look at id in
			// 7)
			// [7] = sender id
			// [8] - [13] = unit counts (in ordinal order of
			// RobotType)
			// [14] = ground target score * 1000000
			// [15] = ground target time
			// [16] = closest tower distSq
			// [17] = ...
			// [18] = group type
			// [19] = allowedToMoveAt
			final int messageIntsLength = 20;
			//
			// locations:
			// [0] = group ground center
			// [1] = best ground target
			// [2] = best mortar target
			// [3] = new robot location (or null if no new robot)
			final int messageLocsLength = 4;

			l1: while (true) {
				try {
					if (isLeader) {
						sendGroupUpdateCountdown--;
						if (sendGroupUpdateCountdown <= 0) {
							sendGroupUpdateCountdown = (int) group_update_min_period;
							sendGroupUpdate = true;
						}
					}

					if (sendGroupUpdate) {
						Message m = new Message();
						m.ints = new int[messageIntsLength];
						m.ints[0] = (Clock.getRoundNum() << 16)
								+ (rand.nextInt() >> 16);
						m.ints[1] = m.ints[0] ^ team_channel_magic_num;
						m.ints[3] = groupDirection.ordinal();
						m.ints[4] = state;
						m.ints[5] = groupId;
						m.ints[6] = addRobot_inAir;
						m.ints[7] = id;
						for (int i = 0; i < 6; i++) {
							m.ints[8 + i] = unitCounts[i];
						}
						m.ints[14] = (int) (bestGroundTarget_score * 1000000);
						m.ints[15] = bestGroundTarget_time;
						m.ints[16] = closestEnemyTower_distSq;
						m.ints[18] = groupType;
						m.ints[19] = roundWhenGroupCanMove;

						m.locations = new MapLocation[messageLocsLength];
						m.locations[0] = groupGroundCenter;
						m.locations[1] = bestGroundTarget;
						m.locations[2] = closestEnemyTower;
						m.locations[3] = addRobot_location;
						rc.broadcast(m);
						oldMessages.add(m.ints[0]);
					}

					rc.yield();

					{
						// work here
						// rc.setIndicatorString(0, "ground center: " +
						// U.sub(groupGroundCenter,
						// rc.getLocation()));
						// rc.setIndicatorString(1, "air center: " +
						// U.sub(groupAirCenter,
						// rc.getLocation()));
						// rc.setIndicatorString(0, "group type: " + groupType);
						// rc.setIndicatorString(2, "safe: "
						// + U.sub(lastSafePoint, rc.getLocation()));
						// rc.setIndicatorString(0, "dir: " +
						// prevNavigationDirection);
						// rc.setIndicatorString(2, "air sq: " +
						// groupAirRadiusSq);
						// rc.setIndicatorString(2, "health: " +
						// feelingHealthy);
						// if (closestEnemyTower != null) {
						// rc.setIndicatorString(1, "tower: "
						// + U
						// .sub(closestEnemyTower, rc
						// .getLocation()));
						// } else {
						// rc.setIndicatorString(1, "tower: null");
						// }
						// rc.setIndicatorString(1, "leader: " + isLeader);
						// rc.setIndicatorString(2, "members: " + groupMembers);
					}

					// add the new unit ourselves,
					// that we just told everyone else to add
					if (isArchon && (addRobot_location != null)) {
						if (addRobot_inAir == 0) {
							Robot r = rc
									.senseGroundRobotAtLocation(addRobot_location);
							if (r != null) {
								groupMembers.add(r.getID());
							}
						} else if (addRobot_inAir == 1) {
							Robot r = rc
									.senseAirRobotAtLocation(addRobot_location);
							if (r != null) {
								groupMembers.add(r.getID());
							}
						} else {
							groupMembers.add(id); // adding ourselves
						}
					}

					sendGroupUpdate = false;
					addRobot_inAir = 0;
					addRobot_location = null;

					for (Message m : rc.getAllMessages()) {
						int[] ints = m.ints;
						if (ints == null)
							continue;
						if (ints.length != messageIntsLength)
							continue;
						if ((ints[0] ^ team_channel_magic_num) != ints[1])
							continue;
						int forGroup = ints[5];
						if (groupId >= 0 && groupId != forGroup)
							continue;
						if (!oldMessages.add(ints[0]))
							continue;
						int round = ints[0] >> 16;
						if (round < birthday) {
							continue;
						}

						MapLocation[] locs = m.locations;
						if (locs == null)
							continue;
						if (locs.length != messageLocsLength)
							continue;

						if (!isLeader) {
							groupType = ints[18];
							groupGroundCenter = locs[0];
							groupDirection = Direction.values()[ints[3]];
							state = ints[4];
							for (int i = 0; i < 6; i++) {
								unitCounts[i] = ints[8 + i];
							}
							unitCountsChanged = true;
						}
						if (roundWhenGroupCanMove < ints[19]) {
							// make sure this gap isn't too big, just in case
							// our messages get corrupted
							if (ints[19] - roundWhenGroupCanMove < 200) {
								roundWhenGroupCanMove = ints[19];
							}
						}

						bestGroundTarget = locs[1];
						bestGroundTarget_score = (double) ints[14] / 1000000;
						bestGroundTarget_time = ints[15];

						closestEnemyTower = locs[2];
						closestEnemyTower_distSq = ints[16];

						if (locs[3] != null) {
							if (isArchon) {
								if (rc.canSenseSquare(locs[3])) {
									int height = ints[6];
									if (height == 0) {
										Robot r = rc
												.senseGroundRobotAtLocation(locs[3]);
										if (r != null) {
											groupMembers.add(r.getID());
										}
									} else if (height == 1) {
										Robot r = rc
												.senseAirRobotAtLocation(locs[3]);
										if (r != null) {
											groupMembers.add(r.getID());
										}
									} else {
										groupMembers.add(ints[7]);
									}
								}
							} else if (groupId < 0) {
								if (rc.getLocation().equals(locs[3])) {
									groupId = forGroup;
									birthday = Clock.getRoundNum() + 1; // pretend

									// we
									// were
									// born
									// a
									// little
									// later,
									// because we may not have heard all the
									// messages sent the very
									// first round we were alive
								}
							}
						}
					}
					if (groupId < 0) {
						continue;
					}
					if (demeanor < 0) {
						if (groupType == group_type_tower_ninja) {
							demeanor = 0;
						} else if (groupType == group_type_thunder_cloud) {
							demeanor = 2;
						}
					}

					// ------------------------------------------------------------------------
					// utils
					int roundNum = Clock.getRoundNum();
					MapLocation here = rc.getLocation();
					RobotInfo myInfo = rc.senseRobotInfo(robot);
					RobotType myType = myInfo.type;

					// compute values from unit counts
					if (unitCountsChanged) {
						unitCountsChanged = false;

						int groundUnitCount = 0;
						int airUnitCount = 0;
						for (int i = 0; i < 6; i++) {
							if (i == _scout || i == _bomber) {
								airUnitCount += unitCounts[i];
							} else {
								groundUnitCount += unitCounts[i];
							}
						}

						groupGroundRadiusSq = groupSizeToRadiusSq[groundUnitCount];
						groupAirRadiusSq = groupSizeToRadiusSq[airUnitCount];
						if (groupDirection.isDiagonal()) {
							groupAirCenter = groupGroundCenter
									.add(groupDirection);
							// if (groundUnitCount >= 4 && airUnitCount >= 5) {
							// groupAirCenter =
							// groupAirCenter.add(groupDirection);
							// }
						} else {
							groupAirCenter = groupGroundCenter
									.add(groupDirection);
							if (groundUnitCount >= 4 && airUnitCount >= 5) {
								groupAirCenter = groupAirCenter
										.add(groupDirection);

								// work here

								// if (groundUnitCount >= 4 && airUnitCount >=
								// 9) {
								// groupAirCenter =
								// groupAirCenter.add(groupDirection);
								// }
							}
						}
					}

					// ------------------------------------------------------------------------
					// flush old shared concepts
					if (bestGroundTarget != null
							&& (bestGroundTarget_time < roundNum
									- (int) max_shared_concept_age)) {
						bestGroundTarget = null;
						sendGroupUpdate = true;
					}
					if (isLeader && (closestEnemyTower != null)
							&& (here.distanceSquaredTo(closestEnemyTower) > 36)) {
						closestEnemyTower = null;
						closestEnemyTower_distSq = Integer.MAX_VALUE;
						sendGroupUpdate = true;
					}

					// ------------------------------------------------------------------------
					// healing
					if (groupType == group_type_tower_ninja) {
						towerNinjaHealing(here, myInfo, myType, team,
								groupMembers);
					} else if (groupType == group_type_thunder_cloud) {
						thunderCloudHealing(here, myInfo, myType, team,
								groupMembers, groupDirection);
					} else {
						doHealing();
					}

					// ------------------------------------------------------------------------
					// attacking
					if (!isArchon && !rc.isAttackActive()) {
						MapLocation bestTarget = null;
						boolean bestTarget_inAir = false;
						double bestTarget_score = Double.MAX_VALUE;
						double[] target_value = enemy_type_value_as_target;
						if (rc.getRobotType() == RobotType.MORTAR) {
							target_value = enemy_type_value_as_target_for_mortars;
						}
						if (rc.canAttackGround()) {
							for (Robot r : rc.senseNearbyGroundRobots()) {
								RobotInfo info = rc.senseRobotInfo(r);
								MapLocation pos = info.location;
								if (info.team != team) {
									if (rc.canAttackSquare(pos)) {
										double score = Math
												.sqrt(pos
														.distanceSquaredTo(groupGroundCenter))
												+ target_value[info.type
														.ordinal()];
										if (score < bestTarget_score) {
											bestTarget_score = score;
											bestTarget = pos;
											bestTarget_inAir = false;
										}
									}
								}
							}
						}
						if (rc.canAttackAir()) {
							for (Robot r : rc.senseNearbyAirRobots()) {
								RobotInfo info = rc.senseRobotInfo(r);
								MapLocation pos = info.location;
								if (info.team != team) {
									if (rc.canAttackSquare(pos)) {
										double score = Math
												.sqrt(pos
														.distanceSquaredTo(groupGroundCenter))
												+ target_value[info.type
														.ordinal()];
										if (score < bestTarget_score) {
											bestTarget_score = score;
											bestTarget = pos;
											bestTarget_inAir = true;
										}
									}
								}
							}
						}

						if (bestTarget == null) {
							// try out our intel from the archons,
							// if we're a mortar or sniper
							if ((myType == RobotType.MORTAR)
									&& (closestEnemyTower != null)
									&& rc.canAttackSquare(closestEnemyTower)) {
								bestTarget = closestEnemyTower;
							} else if ((myType == RobotType.MORTAR)
									|| (myType == RobotType.SNIPER)) {
								if (bestGroundTarget != null) {
									if (rc.canAttackSquare(bestGroundTarget)) {
										bestTarget = bestGroundTarget;
									}
								}
							}
						}

						if (bestTarget != null) {
							if (bestTarget_inAir) {
								rc.attackAir(bestTarget);
								continue;
							} else {
								rc.attackGround(bestTarget);
								continue;
							}
						}
					}

					// ------------------------------------------------------------------------
					// update desiredUnitCounts
					int numArchons = unitCounts[_archon];
					if (oldArchonCount != numArchons) {
						oldArchonCount = numArchons;

						double[] desired_unit_percent = desired_unit_percent_for_group_type[groupType];
						desiredUnitCounts = new int[6];
						double production = numArchons
								* GameConstants.ARCHON_PRODUCTION;
						for (int i = 0; i < 6; i++) {
							if (i == _archon)
								continue;
							RobotType t = RobotType.values()[i];
							double cost = t.energonUpkeep();
							desiredUnitCounts[i] = (int) Math
									.round(desired_unit_percent[i] * production
											/ cost);
						}
					}

					// ------------------------------------------------------------------------
					// evolving
					// ...do it if you can, and you are next an archon
					checkEvolutionCounter--;
					if ((checkEvolutionCounter <= 0) && (state != state_flee)) {
						checkEvolutionCounter = (int) freq_check_evolution_counter;

						int typeOrdinal = rc.getRobotType().ordinal();

						if (typeOrdinal == _scout) {
							if (desiredUnitCounts[_bomber] > unitCounts[_bomber]) {
								double energon = rc.getEnergonLevel();
								if (energon > energon_to_consider_spawning_unit[_bomber]) {
									if (nearArchon()) {
										rc.evolve(RobotType.BOMBER);
										unitCounts[_scout]--;
										unitCounts[_bomber]++;
										roundWhenGroupCanMove += 50;
										sendGroupUpdate = true;
										continue;
									}
								}
							}

						} else if (typeOrdinal == _soldier) {
							if (desiredUnitCounts[_mortar] > unitCounts[_mortar]) {
								double energon = rc.getEnergonLevel();
								if (energon > energon_to_consider_spawning_unit[_mortar]) {
									if (nearArchon()) {
										rc.evolve(RobotType.MORTAR);
										unitCounts[_soldier]--;
										unitCounts[_mortar]++;
										roundWhenGroupCanMove += 50;
										sendGroupUpdate = true;
										continue;
									}
								}
							}
							if (desiredUnitCounts[_sniper] > unitCounts[_sniper]) {
								double energon = rc.getEnergonLevel();
								if (energon > energon_to_consider_spawning_unit[_sniper]) {
									if (nearArchon()) {
										rc.evolve(RobotType.SNIPER);
										unitCounts[_soldier]--;
										unitCounts[_sniper]++;
										roundWhenGroupCanMove += 50;
										sendGroupUpdate = true;
										continue;
									}
								}
							}
						}
					}

					// ------------------------------------------------------------------------
					// archon specific
					if (isArchon) {
						// if we moved, update stuff
						if (!here.equals(lastLocation)) {
							// update map
							RegexMapModule.archonExplore(U.dirTo(lastLocation,
									here));

							lastLastLocation = lastLocation;
							lastLocation = here;

							// artificially say that we need to update this
							// again,
							// since we've moved
							updateNearbyRobotInfoCount = 0;

							// update misc step values
							roundsSinceStepping = 0;
							stepsSinceSawEnemyTower++;
							towerNinjaSafePointCounter++;
							if (towerNinjaSafePointCounter >= 5) {
								lastSafePoint = here;
							}
						} else {
							roundsSinceStepping++;
							if (groupType == group_type_tower_ninja) {
								if (roundsSinceStepping > 200) {
									state = state_go_here;
									goHere = lastSafePoint;
									goHereCountdown = 50;
								}
							}
						}

						// update nearby robot information
						if (updateNearbyRobotInfoCount-- <= 0) {
							updateNearbyRobotInfoCount = (int) freq_update_nearby_robot_info;

							// take note of units nearby

							// ... see if a lot of towers are attacking this
							// spot
							enemyTowersInRangeCount = 0;
							int enemyTowersSeen = 0;

							// ... find closest archon
							closestEnemyArchon = null;
							int closestEnemyArchonDist = Integer.MAX_VALUE;

							// ... count types of units in our group
							unitCounts = new int[6];
							unitCounts[_archon]++; // count self
							unitCountsChanged = true;

							// ... see how long till we can move again as a
							// group
							int maxRoundsUntilMovementIdle = 0;

							// ... see if we should be in fight mode
							MapLocation nearestGroundEnemy = null;
							int rangeSqToNearestGroundEnemy = Integer.MAX_VALUE;

							for (Robot r : rc.senseNearbyGroundRobots()) {
								RobotInfo i = rc.senseRobotInfo(r);
								MapLocation pos = i.location;
								if (i.team == team) {
									if (groupMembers.contains(r.getID())) {
										unitCounts[i.type.ordinal()]++;
										if (i.roundsUntilMovementIdle > maxRoundsUntilMovementIdle) {
											maxRoundsUntilMovementIdle = i.roundsUntilMovementIdle;
										}
									}
								} else {
									int range = groupGroundCenter
											.distanceSquaredTo(pos);

									if (i.type != RobotType.TOWER) {
										if (range < rangeSqToNearestGroundEnemy) {
											rangeSqToNearestGroundEnemy = range;
											nearestGroundEnemy = pos;
										}
									}

									double score = Math.sqrt(range);
									double groundScore = score
											+ enemy_type_value_as_target[i.type
													.ordinal()];
									double mortarScore = score
											+ enemy_type_value_as_target_for_mortars[i.type
													.ordinal()];
									if (bestGroundTarget == null
											|| groundScore < bestGroundTarget_score) {
										bestGroundTarget_score = groundScore;
										bestGroundTarget = pos;
										bestGroundTarget_time = Clock
												.getRoundNum();
									}
								}

								if (i.type == RobotType.TOWER) {
									if (towers.put(pos, r) == null) {
										RegexMapModule.markTower(pos);
									}
									if (i.team != team) {
										int dist = here
												.distanceSquaredTo(i.location);
										if (dist < closestEnemyTower_distSq) {
											closestEnemyTower_distSq = dist;
											closestEnemyTower = i.location;
										}

										// if this enemy tower can attack us,
										// take note of it
										enemyTowersSeen++;
										if (dist <= RobotType.TOWER
												.attackRadiusMaxSquared()) {
											enemyTowersInRangeCount++;
										}
									} else {
										if (i.location
												.equals(closestEnemyTower)) {
											closestEnemyTower = null;
											closestEnemyTower_distSq = Integer.MAX_VALUE;

											// reset our health feeling,
											// so we feel healthy before
											// venturing out again
											feelingHealthy = false;
										}
									}
								}
								if (i.type == RobotType.ARCHON) {
									if (i.team != team) {
										int dist = here
												.distanceSquaredTo(i.location);
										if (dist < closestEnemyArchonDist) {
											closestEnemyArchonDist = dist;
											closestEnemyArchon = i.location;
										}
									}
								}
							}
							for (Robot r : rc.senseNearbyAirRobots()) {
								RobotInfo i = rc.senseRobotInfo(r);
								MapLocation pos = i.location;
								if (i.team == team) {
									if (groupMembers.contains(r.getID())) {
										unitCounts[i.type.ordinal()]++;
									}
								}
							}

							if (enemyTowersSeen > 0) {
								towerNinjaSafePointCounter = 0;
							}
							if (enemyTowersInRangeCount > 0) {
								stepsSinceSawEnemyTower = 0;
							}

							if (isLeader) {
								// ... deal with enemyTowersInRangeCount
								if (groupType == group_type_tower_ninja) {
									if (enemyTowersInRangeCount > 1) {
										state = state_go_here;
										goHere = lastLastLocation;
										goHereCountdown = 20;
										RegexMapModule.addWall(here);
									}
								}

								// ... finish group movement
								if (!updatedRoundWhenGroupCanMove) {
									updatedRoundWhenGroupCanMove = true;
									roundWhenGroupCanMove = Clock.getRoundNum()
											+ maxRoundsUntilMovementIdle;
								}

								// ... finish fight mode detection
								if (demeanor > 0) {
									if (Math.sqrt(rangeSqToNearestGroundEnemy) <= fight_if_enemy_in_this_range) {
										if ((state != state_spawn)
												&& (state != state_flee)) {
											state = state_fight;
											sendGroupUpdate = true;
										}
									} else if (state == state_fight) {
										state = -1;
										sendGroupUpdate = true;
									}
								} else {
									if (Math.sqrt(rangeSqToNearestGroundEnemy) <= flee_if_enemy_in_this_range) {
										state = state_flee;
										fleeDirection = U.dirTo(
												nearestGroundEnemy, here);
									}
								}

								// ... tower battle detection
								if (closestEnemyTower != null) {
									if (closestEnemyTower
											.equals(lastTakenTower)
											&& (state == state_seek_tower)) {
										if (demeanor < 1) {
											state = state_flee;
											fleeDirection = U.dirTo(
													lastTakenTower, here);
										} else {
											state = state_fight_and_seek;
										}
										sendGroupUpdate = true;
									}
									lastEnemyTower = closestEnemyTower;
								}
							}
							sendGroupUpdate = true;
						}

						// see if we took over the last tower we were attacking
						if (seeIfWeTookTower-- <= 0) {
							seeIfWeTookTower = (int) see_if_we_took_tower_period;

							for (MapLocation tower : rc.senseAlliedTowers()) {
								if (tower.equals(lastEnemyTower)) {
									lastTakenTower = lastEnemyTower;
									break;
								}
							}
						}

						// see if we should change spawn state
						if (seeIfWeShouldSpawn-- <= 0) {
							seeIfWeShouldSpawn = (int) freq_check_if_we_should_spawn;

							double totalCost = 0;
							double desiredCost = 0;
							for (int i = 0; i < 6; i++) {
								if (i == _archon) {
									continue;
								}
								RobotType t = RobotType.values()[i];
								double cost = t.energonUpkeep();
								totalCost += unitCounts[i] * cost;
								desiredCost += desiredUnitCounts[i] * cost;
							}
							double ratio = totalCost / desiredCost;

							if ((ratio < army_size_threshold_to_want_to_spawn)
									&& ((state != state_flee) && (state != state_go_here))) {
								state = state_spawn;
								sendGroupUpdate = true;
							} else if ((state == state_spawn)
									&& (ratio > army_size_threshold_to_be_done_spawning)) {
								state = -1;
								sendGroupUpdate = true;
							}
						}

						// spawning
						// ...do it if you can
						if (rc.canSpawn()
								&& ((state != state_flee) && (state != state_go_here))
								&& (stepsSinceSawEnemyTower > 3)) {
							// ...and we desire something...

							int wantAirAmount = (desiredUnitCounts[_scout] + desiredUnitCounts[_bomber])
									- (unitCounts[_scout] + unitCounts[_bomber]);
							int wantGroundAmount = (desiredUnitCounts[_soldier]
									+ desiredUnitCounts[_mortar] + desiredUnitCounts[_sniper])
									- (unitCounts[_soldier]
											+ unitCounts[_mortar] + unitCounts[_sniper]);

							double energon = rc.getEnergonLevel();
							boolean wantAir = (wantAirAmount > 0)
									&& (energon > energon_to_consider_spawning_unit[_scout]);
							boolean wantGround = (wantGroundAmount > 0)
									&& (energon > energon_to_consider_spawning_unit[_soldier]);
							if (wantAir && (rand.nextBoolean() || !wantGround)) {
								// try for scout
								MapLocation front = here.add(rc.getDirection());
								if (rc.senseAirRobotAtLocation(front) == null) {
									rc.spawn(RobotType.SCOUT);
									unitCounts[_scout]++;
									sendGroupUpdate = true;
									addRobot_inAir = 1;
									addRobot_location = front;
									roundWhenGroupCanMove += 50;
									continue;
								}
							} else if (wantGround) {
								// try for soldier
								if (rc.canMove(rc.getDirection())) {
									rc.spawn(RobotType.SOLDIER);
									MapLocation front = here.add(rc
											.getDirection());
									unitCounts[_soldier]++;
									sendGroupUpdate = true;
									addRobot_inAir = 0;
									addRobot_location = front;
									roundWhenGroupCanMove += 50;
									continue;
								}
							}
						}

						if (isLeader) {
							if (!rc.isMovementActive()) {
								if (!here.equals(groupGroundCenter)) {
									Direction toCenter = U.dirTo(here,
											groupGroundCenter);
									int ret = moveIfCan_faceIfCould(toCenter);
									if (ret == 0) {
										// find out why we can't move there...
										Robot guyBlockingUs = rc
												.senseGroundRobotAtLocation(groupGroundCenter);
										if (guyBlockingUs != null) {
											// someone is blocking us,
											// see if they are stuck
											RobotInfo blockerInfo = rc
													.senseRobotInfo(guyBlockingUs);
											if (blockerInfo.roundsUntilMovementIdle > 0) {
												// he's still moveing, so we'll
												// wait without decrementing our
												// counter
											} else {
												// he isn't moving, so we'll
												// decrement our counter
												waitToGetToCenter--;
											}
										} else {
											// hm... apparently we're being
											// blocked by terrain,
											// so we should not try to go this
											// way
											waitToGetToCenter = 0;
										}
										if (waitToGetToCenter <= 0) {
											Direction d = Direction.values()[rand
													.nextInt(8)];
											groupGroundCenter = here.add(d);
											groupDirection = d;
											waitToGetToCenter = (int) leader_patience_time;
											sendGroupUpdate = true;
										}
									}
								} else if (Clock.getRoundNum() >= roundWhenGroupCanMove) {
									// --------------------------------------------------------
									boolean isTowerNinja = (groupType == group_type_tower_ninja);
									RobotInfo towerNinjaMortar = null;
									if (isTowerNinja) {
										for (Robot r : rc
												.senseNearbyGroundRobots()) {
											if (groupMembers
													.contains(r.getID())) {
												RobotInfo info = rc
														.senseRobotInfo(r);
												if (info.type == RobotType.MORTAR) {
													towerNinjaMortar = info;
													break;
												}
											}
										}
										if (state == state_flee) {
											state = state_seek_tower;
											towerNinjaWindCounter = 10;
											towerNinjaWindDirection = 4;
										}
									}

									if (state < 0) {
										if (demeanor < 2) {
											state = state_seek_tower;
										} else {
											state = state_seek_archon;
										}
									}

									if (state != prevNavigationState) {
										prevNavigationState = state;
										rippleCountdown = 0;
										fatWallCountdown = 0;
										if (goHereCountdown == 0) {
											goHereCountdown = 20;
										}
										ripples = null;
										fleeCountdown = (int) flee_countdown_init;
										fightCountdown = (int) fight_countdown_init;
										sendGroupUpdate = true;
										prevNavigationDirection = null;
									}
									if (state == state_fight_and_seek) {
										state = state_fight;
										fightCountdown = 0;
									}
									rippleCountdown--;
									fatWallCountdown--;
									fleeCountdown--;
									fightCountdown--;
									goHereCountdown--;

									MapLocation[] dests = null;
									if (state == state_spawn) {
										dests = rc.senseAlliedTowers();
										if (dests.length == 0) {
											state = state_seek_tower;
										}
									}
									if (state == state_go_here) {
										if (here.equals(goHere)
												|| (goHereCountdown <= 0)) {
											state = -1;
										}
										dests = new MapLocation[] { goHere };
									}
									if (state == state_seek_tower) {
										int mortarInRange = -1;
										if (isTowerNinja) {
											boolean mortarIsHealthy = false;
											if (towerNinjaMortar != null) {
												if (towerNinjaMortar.energonLevel >= 30) {
													mortarIsHealthy = true;
												}

												if (closestEnemyTower != null) {
													int distSq = towerNinjaMortar.location
															.distanceSquaredTo(closestEnemyTower);
													if (distSq > RobotType.MORTAR
															.attackRadiusMaxSquared()) {
														mortarInRange = -1;
													} else if (distSq < RobotType.MORTAR
															.attackRadiusMinSquared()) {
														mortarInRange = 1;
													} else {
														mortarInRange = 0;
													}
												}
											}
											if (rc.getEnergonLevel() > 40
													&& mortarIsHealthy) {
												feelingHealthy = true;
											}
											if (rc.getEnergonLevel() < 20) {
												feelingHealthy = false;
											}
										}

										if (closestEnemyTower == null) {
											Direction toTower = rc
													.senseClosestUnknownTower();
											if (toTower.ordinal() < 8) {
												if (rippleCountdown <= 0) {
													ripples = U.addRipples(
															here, toTower,
															new int[] { 10, 12,
																	25 });
													rippleCountdown = 3;
												}
												dests = ripples;
											}
										} else {
											if (isTowerNinja) {
												if ((towerNinjaMortar != null)
														&& !(towerNinjaMortar.location
																.isAdjacentTo(here))) {
												} else if (mortarInRange == -1) {
													dests = new MapLocation[] { closestEnemyTower };
												} else if (mortarInRange == 1) {
													dests = U
															.addRipples(
																	here,
																	U
																			.dirTo(
																					closestEnemyTower,
																					here),
																	new int[] {
																			10,
																			12,
																			25 });
												}
											} else {
												if (!here
														.isAdjacentTo(closestEnemyTower)) {
													dests = new MapLocation[] { closestEnemyTower };
												}
											}
										}

										// wind, to blow the tower ninja
										if (dests != null) {
											if (isTowerNinja
													&& (towerNinjaWindCounter-- >= 0)) {
												Direction d = rc
														.senseEnemyArchon();
												if (groupId % 2 == 0) {
													for (int i = 0; i < towerNinjaWindDirection; i++) {
														d = d.rotateLeft();
													}
												} else {
													for (int i = 0; i < towerNinjaWindDirection; i++) {
														d = d.rotateRight();
													}
												}
												dests = U
														.addRipples(
																here,
																d,
																new int[] { 10,
																		12, 25 });
											}
										}

										if (isTowerNinja && !feelingHealthy) {
											dests = rc.senseAlliedTowers();
										}
									}
									if (state == state_fight) {
									}
									if (state == state_seek_archon
											|| (state == state_fight && fightCountdown <= 0)) {
										if (closestEnemyArchon == null) {
											Direction toThere = rc
													.senseEnemyArchon();
											if (toThere.ordinal() < 8) {
												if (rippleCountdown <= 0) {
													ripples = U.addRipples(
															here, toThere,
															new int[] { 10, 12,
																	25 });
													rippleCountdown = 3;
												}
												dests = ripples;
											}
										} else {
											dests = new MapLocation[] { closestEnemyArchon };
										}
									}
									if (state == state_flee) {
										if (fleeCountdown <= 0) {
											state = -1;
										}
										if (rippleCountdown <= 0) {
											ripples = U.addRipples(here,
													fleeDirection, new int[] {
															10, 12, 25 });
											rippleCountdown = 3;
										}
										dests = ripples;
									}

									Direction d = null;
									if (dests != null && dests.length > 0) {
										if (groupType == group_type_tower_ninja) {
											// if we are a tower ninja, don't
											// use fat walls
											fatWallCountdown = 1;
										}
										if (fatWallCountdown <= 0) {
											d = RegexMapModule.atobFatWalls(
													here, dests);
										}
										if (d == null) {
											d = RegexMapModule
													.atob(here, dests);
											if (fatWallCountdown <= 0) {
												fatWallCountdown = 10;
											}
										}
									}
									prevNavigationDirection = d;

									if (d != null) {
										groupGroundCenter = here.add(d);
										groupDirection = d;
										waitToGetToCenter = (int) leader_patience_time;
										updatedRoundWhenGroupCanMove = false;
										sendGroupUpdate = true;
									}
								}
								if (groupType == group_type_thunder_cloud) {
									Direction toEnemy = rc.senseEnemyArchon();
									if (toEnemy != null) {
										groupDirection = toEnemy;
									}
								}
							}
						} else {
							// leader update (every 5 rounds, and then only
							// when
							// the
							// total number of archons changes)
							updateIsLeader_countDown--;
							if (updateIsLeader_countDown <= 0) {
								updateIsLeader_countDown = 5;

								int totalArchons = rc
										.getUnitCount(RobotType.ARCHON);
								if (totalArchons != updateIsLeader_lastTotalArchonCount) {
									updateIsLeader_lastTotalArchonCount = totalArchons;

									// ranges from 173 to 550 (maybe up to
									// 1000)
									isLeader = false;
									for (MapLocation loc : rc
											.senseAlliedArchons()) {
										if (rc.canSenseSquare(loc)) {
											Robot r = rc
													.senseGroundRobotAtLocation(loc);
											int theirId = r.getID();
											if (groupMembers.contains(theirId)) {
												isLeader = id == theirId;
												break;
											}
										}
									}
								}
							}
						}
					}

					// ------------------------------------------------------------------------
					// specific to non-leaders
					if (!isLeader) {
						// group movement
						MapLocation groupCenter = groupGroundCenter;
						int groupRadiusSq = groupGroundRadiusSq;
						if (inAir) {
							groupCenter = groupAirCenter;
							groupRadiusSq = groupAirRadiusSq;
						}
						if (!rc.isMovementActive()) {
							Direction toCenter = U.dirTo(here, groupCenter);

							int distSq = groupCenter.distanceSquaredTo(here);

							// see if we've gotten close enough to exit bug
							// mode...
							if (bugMode
									&& (here.distanceSquaredTo(bugTarget) < bugDistSqToBeat)) {
								bugMode = false;
							}

							// check if there is terrain cutting us off from
							// the
							// group, in which case, enter bug mode
							//
							// ...do this even if we are in bug mode,
							// so that we get a larger bugDistSqToBeat,
							// so we can get out of bug mode sooner
							if (!inAir) {
								TerrainTile t = rc.senseTerrainTile(here
										.add(toCenter));
								if (t == null) {
									rc.setDirection(toCenter);
									continue l1;
								} else if (t.getType() == TerrainTile.TerrainType.WATER) {
									bugMode = true;
									bugTurnDirection = U.dot(groupDirection,
											toCenter);
									if (bugTurnDirection == 0) {
										bugTurnDirection = U.dot(
												groupDirection, U
														.dirToSecondBest(here,
																groupCenter));
									}
									bugDirection = toCenter;
									bugDistSqToBeat = distSq;
									bugTarget = groupCenter;
								}
							}

							// if we're not in bug mode,
							// try to find out place in the group normally
							if (!bugMode) {
								// if we're within the group radius, try
								// moving
								// to the front
								if (distSq <= groupRadiusSq) {

									// if we're a tower-ninja-mortar,
									// then don't move if we are within range of
									// an enemy tower
									boolean dontMove = false;
									if ((myType == RobotType.MORTAR)
											&& (groupType == group_type_tower_ninja)) {
										if ((closestEnemyTower != null)
												&& U.inRange(here,
														closestEnemyTower,
														myType)) {
											dontMove = true;
										}
									}

									if (groupCenter.equals(here)) {
										dontMove = false;
									}

									if (!dontMove) {
										MapLocation front = here
												.add(groupDirection);
										if (!front.equals(groupCenter)
												&& groupCenter
														.distanceSquaredTo(front) <= groupRadiusSq) {
											if (moveIfCan_faceIfCould(groupDirection) > 0) {
												continue;
											}
										}
										front = here.add(groupDirection
												.rotateLeft());
										if (!front.equals(groupCenter)
												&& groupCenter
														.distanceSquaredTo(front) <= groupRadiusSq) {
											if (moveIfCan_faceIfCould(groupDirection
													.rotateLeft()) > 0) {
												continue;
											}
										}
										front = here.add(groupDirection
												.rotateRight());
										if (!front.equals(groupCenter)
												&& groupCenter
														.distanceSquaredTo(front) <= groupRadiusSq) {
											if (moveIfCan_faceIfCould(groupDirection
													.rotateRight()) > 0) {
												continue;
											}
										}
									}
								} else {
									// if we're not within the group radius,
									// try
									// getting there
									if (moveIfCan_faceIfCould(toCenter) > 0) {
										continue;
									} else {
										if (inAir) {
											if (moveIfCan_faceIfCould(toCenter
													.rotateLeft()) > 0) {
												continue;
											} else if (moveIfCan_faceIfCould(toCenter
													.rotateRight()) > 0) {
												continue;
											} else {
												// oh well... we'll wait
											}
										} else {
											TerrainTile t = rc
													.senseTerrainTile(here
															.add(toCenter));
											if (t == null) {
												rc.setDirection(toCenter);
												continue;
											} else if (t.getType() == TerrainTile.TerrainType.WATER) {
												// this should never be the
												// case,
												// since then we would have
												// entered
												// bug mode
											} else {
												if (moveIfCan_faceIfCould(toCenter
														.rotateLeft()) > 0) {
													continue;
												} else if (moveIfCan_faceIfCould(toCenter
														.rotateRight()) > 0) {
													continue;
												} else {
													// oh well... we'll wait
												}
											}
										}
									}
								}
							}
							// if terrain get's between us and the group,
							// bug
							// around it
							while (bugMode) {
								int ret = moveIfCan_faceIfCould(bugDirection);
								if (ret >= 2) {
									if (bugTurnDirection > 0) {
										if (bugDirection.isDiagonal()) {
											bugDirection = bugDirection
													.rotateLeft().rotateLeft();
										} else {
											bugDirection = bugDirection
													.rotateLeft();
										}
									} else {
										if (bugDirection.isDiagonal()) {
											bugDirection = bugDirection
													.rotateRight()
													.rotateRight();
										} else {
											bugDirection = bugDirection
													.rotateRight();
										}
									}
								}
								if (ret > 0) {
									continue l1;
								} else {
									TerrainTile t = rc.senseTerrainTile(here
											.add(bugDirection));
									if (t == null) {
										rc.setDirection(bugDirection);
										continue l1;
									} else if (t.getType() == TerrainTile.TerrainType.WATER) {
										if (bugTurnDirection > 0) {
											bugDirection = bugDirection
													.rotateRight();
										} else {
											bugDirection = bugDirection
													.rotateLeft();
										}
									} else {
										bugMode = false;
									}
								}
							}

							// since we're not doing anything else,
							// if we can spawn,
							// turn in a random direction,
							// to increase our chances of a baby
							if (isArchon && rc.canSpawn()
									&& (state != state_flee)) {
								rc.setDirection(Direction.values()[rand
										.nextInt(8)]);
								continue;
							}

							// also, if we're not up to anything better,
							// let's face in the direction of a likely enemy
							RobotType type = rc.getRobotType();
							if (!inAir && !isArchon) {
								if ((type == RobotType.MORTAR)
										&& (closestEnemyTower != null)
										&& U.inRange(here, closestEnemyTower,
												myType)) {
									rc.setDirection(U.dirTo(here,
											closestEnemyTower));
									continue;
								} else if (type != RobotType.MORTAR
										&& bestGroundTarget != null) {
									rc.setDirection(U.dirTo(here,
											bestGroundTarget));
									continue;
								}

								// face away from center of group, for healing
								Direction d = U.dirTo(here, groupCenter);
								if (d.ordinal() < 8)
									rc.setDirection(d);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} // while (true)
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // run
}
