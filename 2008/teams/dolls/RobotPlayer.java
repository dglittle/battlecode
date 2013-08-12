package dolls;

import java.util.Arrays;
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

	// -------------------------------------
	// util
	public static RobotController rc;

	public static int[][] initial_archon_group_id_plans = new int[][] {
			{ 0, 0, 0, 0, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0 } };
	public static final int group_type_fluff = 0;
	public static int[] group_id_to_group_type = new int[] { group_type_fluff,
			group_type_fluff };

	// -------------------------------------
	public RobotPlayer(RobotController rc) throws Exception {
		RobotPlayer.rc = rc;
	}

	public static int moveIfCan_faceIfCould(Direction dir) throws Exception {
		if (rc.canMove(dir)) {
			Direction forward = rc.getDirection();
			if (forward == dir) {
				rc.moveForward();
				return 2;
			}
			// hack : it doesn't seem to be good for the bombers to go
			// backwards, most of the time, so don't let anyone go backwards
			// else if (forward.opposite() == dir) {
			// rc.moveBackward();
			// return 2;
			// }
			else {
				rc.setDirection(dir);
				return 1;
			}
		}
		return 0;
	}

	public static void faceRandomDirection(Random rand) throws Exception {
		rc.setDirection(Direction.values()[rand.nextInt(8)]);
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

	public static Robot senseRobotAtHeight(MapLocation loc, int height)
			throws Exception {
		if (rc.canSenseSquare(loc)) {
			if (height == 0) {
				return rc.senseGroundRobotAtLocation(loc);
			} else {
				return rc.senseAirRobotAtLocation(loc);
			}
		}
		return null;
	}

	public void run() {
		try {
			// ------------------------------------------------------------------------
			// init

			// general info
			RobotController rc = this.rc;
			Robot myRobot = rc.getRobot();
			int myId = myRobot.getID();
			Team team = rc.senseRobotInfo(myRobot).team;
			Random rand = new Random(0x8274871958248249L + myId);
			RobotType myType = rc.getRobotType();
			RobotInfo myInfo = rc.senseRobotInfo(myRobot);
			boolean isArchon = (myType == RobotType.ARCHON);
			boolean inAir = myType.isAirborne();
			int myHeight = inAir ? 1 : 0;
			MapLocation here = rc.getLocation();
			MapLocation lastLocation = here;
			MapLocation lastLastLocation = here;
			Map<MapLocation, Robot> towers = new HashMap();
			MapLocation startTower = null;
			int broadcastRadius = myType.broadcastRadius();
			int broadcastRadiusSq = broadcastRadius * broadcastRadius;
			int birthday = Clock.getRoundNum();

			// group info
			int groupId = -1;
			int groupType = -1;
			Set<Integer> groupMembers = new HashSet<Integer>();
			groupMembers.add(myId);
			// ... location / size
			MapLocation groupCenter_ground = here;
			MapLocation groupCenter_air = here;
			Direction groupDirection = rc.senseEnemyArchon();
			if (groupDirection == null) {
				groupDirection = Direction.NORTH;
			}
			int[] groupSizeToRadiusSq = new int[] { 2, 2, 2, 2, 2, 2, 2, 2, 2,
					2, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 8, 8, 8, 8, 9, 9, 9,
					9, 10, 10, 10, 10, 10, 10, 10, 10, 13, 13, 13, 13, 13, 13,
					13, 13, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 17, 17, 18,
					18, 18, 18, 20, 20, 20, 20, 20, 20, 20, 20 };
			int groupRadiusSq_ground = 2;
			int groupRadiusSq_air = 2;
			// ... movement
			int roundWhenGroupCanMove = 0;
			// ... state
			final int group_state_spawning = 0;
			final int group_state_seek_towers = 1;
			final int group_state_fight = 2;
			final int group_state_flee = 3;
			int groupState = group_state_spawning;
			int prevGroupState = -1; // hack : force an update
			// ... ... state specific
			boolean spawningAllowed = false;
			int roundsInThisState = 0;
			MapLocation fleeingFrom = null;
			// ... unit counts
			final int _soldier = RobotType.SOLDIER.ordinal();
			final int _mortar = RobotType.MORTAR.ordinal();
			final int _scout = RobotType.SCOUT.ordinal();
			final int _archon = RobotType.ARCHON.ordinal();
			final int _bomber = RobotType.BOMBER.ordinal();
			final int _sniper = RobotType.SNIPER.ordinal();
			int[] unitCounts = new int[6];
			boolean unitCountsChanged = false;
			// ... desired unit counts
			int[] desiredUnitCounts = new int[6];
			int desiredUnitCounts_oldArchonCount = 0;
			// ... shared notions
			MapLocation closestEnemyTower = null;
			MapLocation closestAllyTower = null;

			// tower battle stuff
			MapLocation lastTakenTower = null;
			int sameTowerRetakeCount = 0;

			// group leadership
			boolean isLeader = false;
			int updateIsLeader_previousArchonCount = 0; // for an update right
			// away, so we know if
			// we're the leader
			int updateIsLeader_countDown = 0;
			int roundsSinceGroupLeaderStatusReport = 0;
			int waitToGetToCenter = 5;

			// navigation
			MapLocation[] ripples = null;
			int rippleCountdown = 0;
			int fatWallCountdown = 0;
			// ... wind (begin by blowing each group away from each other)
			int windCountdownRounds = 300;
			int windDirection = 1;
			boolean groupMembersInRadii = false;
			int groupMembersInRadiiTimeout = 0;

			// bug mode
			boolean bugMode = false;
			int bugTurnDirection = 0;
			int bugDistSqToBeat = 0;
			Direction bugDirection = null;
			MapLocation bugTarget = null;

			// messages
			int team_channel_magic_num = team == Team.A ? 959431883
					: -1258411499;

			// initialize group ids based on archon ids (and adjust plan based
			// on archon memory)
			boolean recordArchonCounts = false;
			if (isArchon) {
				// archon memory : if we loose the first game, use plan B
				int[] initial_archon_group_ids;
				long minValue = Long.MAX_VALUE;
				long[] memory = rc.getOldArchonMemory();
				// hack : only look at first 4 values, unless we detect that the
				// bug is fixed
				int maxN = 4;
				if (memory[7] != 0) {
					maxN = 8;
				}
				for (int i = 0; i < maxN; i++) {
					long value = memory[i];
					if (value < minValue) {
						minValue = value;
					}
				}
				boolean planA;
				if (minValue == 0) {
					// use plan A
					planA = true;

					// this is the first game, so use the regular tactic,
					// and say to record archon counts
					recordArchonCounts = true;
				} else if (minValue == -1) {
					// use plan A
					planA = true;
				} else if (minValue == -2) {
					// use plan B
					planA = false;
				} else if (minValue < 3) {
					// we lost last game, so use plan B now
					planA = false;
				} else {
					// we won the last game, so stick with plan A
					planA = true;
				}
				if (planA) {
					initial_archon_group_ids = initial_archon_group_id_plans[0];
					rc.setArchonMemory(-1);
				} else {
					initial_archon_group_ids = initial_archon_group_id_plans[1];
					rc.setArchonMemory(-2);
				}

				// see which tower we started with...
				startTower = rc.senseAlliedTowers()[0];

				// all the archons should agree on a direction that the enemy is
				// in,
				// just in case this value is not the same for all of them
				Direction dirToEnemy = null;

				// if we are the first archon, find out what direction the enemy
				// is in, and tell everyone else this,
				// otherwise, wait for a message from the first archon
				boolean firstArchon = rc.senseAlliedArchons()[0].equals(here);
				if (firstArchon) {
					dirToEnemy = rc.senseEnemyArchon();
					// in the odd event that the enemy has killed themselves
					if (dirToEnemy == null)
						dirToEnemy = Direction.NORTH;
					Message m = new Message();
					int ordinal = dirToEnemy.ordinal();
					m.ints = new int[] { ordinal,
							ordinal ^ team_channel_magic_num };
					rc.broadcast(m);
					rc.yield();
				} else {
					l1: while (true) {
						for (Message m : rc.getAllMessages()) {
							if (m.ints != null) {
								if (m.ints.length == 2) {
									if (m.ints[1] == (m.ints[0] ^ team_channel_magic_num)) {
										dirToEnemy = Direction.values()[m.ints[0]];
										break l1;
									}
								}
							}
						}
						rc.yield();
					}
				}

				// hack: yield once, so that the message we send for our group
				// ids is not absorbed here
				rc.yield();

				// ok, now we want to set our archon id equal to the number of
				// times we must rotate the direction to the enemy to get the
				// direction from the start tower to us
				Direction cursor = dirToEnemy;
				int archonId = 0;
				Direction towerToHere = U.dirTo(startTower, here);
				while (!cursor.equals(towerToHere)) {
					cursor = cursor.rotateRight();
					archonId++;
				}

				// now assign our group id based on our archon id
				groupId = initial_archon_group_ids[archonId];

				// assign our group type based on our group id
				groupType = group_id_to_group_type[groupId];
			}

			// do initial exploring
			if (isArchon) {
				RegexMapModule.init(rc);
				RegexMapModule.archonExplore(Direction.OMNI);
				RegexMapModule.markTower(startTower);
			}

			// -----------------------------------------------------------------------
			// messaging

			// message format
			// ints
			// [0] = round sent
			// [1] = [0] ^ team_channel_magic_num ^ locs[0].x ^ locs[0].y
			// [2] = group id
			// [3] = group direction
			// [4] = height of robot to add (1 == in air, 0 == on ground, 2 ==
			// use sender id)
			// [5] = sender id
			// [6] = group state
			// [7] ... [12] = unit counts
			// [13] = spawningAllowed
			// [14] = roundWhenGroupCanMove
			// [15] = groupType
			// [16] = add unit of this type (-1 don't do this)
			// [17] = 1 if from leader
			// [18] = message id
			// [19] = subtract unit of type (-1 don't do this)
			final int messageIntsLength = 20;
			// locs
			// [0] = sent from here
			// [1] = location of robot to add to group (null if no robot to add)
			// [2] = group center _ ground
			// [3] = group center _ air
			// [4] = closest enemy tower
			// [5] = closest allie tower
			final int messageLocsLength = 6;

			// message variables
			Set<Integer> receivedMessages = new HashSet<Integer>();
			boolean sendMessage = false;
			int oldestValidMessage = 0;
			int addRobotAtHeight = 0;
			MapLocation addRobotAtLocation = null;
			int addUnitOfType = -1;
			int subtractUnitOfType = -1;
			Vector<MapLocation> addRobotAtLocations = new Vector<MapLocation>();
			Vector<Integer> addRobotAtHeights = new Vector<Integer>();

			// archons should all send a message adding themselves to their
			// groups
			if (isArchon) {
				sendMessage = true;
				addRobotAtHeight = 2;
				addRobotAtLocation = here;
			}

			int lastRound = Clock.getRoundNum();
			mainLoop: while (true) {
				try {
					// -------------------------------------------------------------------------
					// archon memory : record how many archons there are
					if (isArchon) {
						if (recordArchonCounts) {
							rc.setArchonMemory(rc
									.getUnitCount(RobotType.ARCHON));
						}
					}

					// -------------------------------------------------------------------------
					// check for taking too long
					int thisRound = Clock.getRoundNum();
					if (thisRound - lastRound > 2) {
						System.out.println("got here! ouch!");
					}
					lastRound = thisRound;

					// -------------------------------------------------------------------------
					// send messages

					// if we're adjusting unit count for others, do it for
					// ourselves too
					if (addUnitOfType >= 0) {
						unitCounts[addUnitOfType]++;
						if (addUnitOfType == _mortar
								|| addUnitOfType == _sniper) {
							unitCounts[_soldier]--;
						}
						if (addUnitOfType == _bomber) {
							unitCounts[_scout]--;
						}
						unitCountsChanged = true;
					}

					// if we are sending a message to add a robot,
					// add it ourselves too (we must have just spawned it)
					if (isArchon && (addRobotAtLocation != null)) {
						addRobotAtLocations.add(addRobotAtLocation);
						addRobotAtHeights.add(addRobotAtHeight);
					}

					// actually send messages
					if (Clock.getBytecodeNum() > 5500)
						rc.yield();
					while (sendMessage) {
						thisRound = Clock.getRoundNum();

						// fill out the message
						int[] ints = new int[messageIntsLength];
						MapLocation[] locs = new MapLocation[messageLocsLength];

						// hash stuff
						ints[0] = thisRound;
						locs[0] = here;
						ints[1] = ints[0] ^ team_channel_magic_num
								^ locs[0].getX() ^ locs[0].getY();

						// actual info
						// ints...
						ints[2] = groupId;
						ints[3] = groupDirection.ordinal();
						ints[4] = addRobotAtHeight;
						ints[5] = myId;
						ints[6] = groupState;
						for (int i = 0; i < 6; i++) {
							ints[7 + i] = unitCounts[i];
						}
						ints[13] = spawningAllowed ? 1 : 0;
						ints[14] = roundWhenGroupCanMove;
						ints[15] = groupType;
						ints[16] = addUnitOfType;
						ints[17] = isLeader ? 1 : 0;
						int messageId = thisRound + (rand.nextInt() << 16);
						ints[18] = messageId;
						ints[19] = subtractUnitOfType;
						// locs...
						locs[1] = addRobotAtLocation;
						locs[2] = groupCenter_ground;
						locs[3] = groupCenter_air;
						locs[4] = closestEnemyTower;
						locs[5] = closestAllyTower;

						// send the message
						Message m = new Message();
						m.ints = ints;
						m.locations = locs;
						rc.broadcast(m);
						receivedMessages.add(messageId);

						if (Clock.getRoundNum() == thisRound) {
							sendMessage = false;
						}
					}

					// if we sent a message subtracting a unit of some type,
					// kill ourselves
					if (subtractUnitOfType >= 0) {
						rc.suicide();
					}

					// -------------------------------------------------------------------------
					// yield -- clear messages if we are going to be moving
					{
						int roundA = Clock.getRoundNum();
						if (rc.hasActionSet()) {
							// clear messages since we may be moving, and we'll
							// be comparing message origins from a new place
							rc.getAllMessages();
						}
						rc.yield();
						int roundB = Clock.getRoundNum();
						if (roundB - roundA != 1) {
							// if it took more rounds to move, then we might
							// have received messages from our previous location
							// during that time, so clear the queue again
							rc.getAllMessages();
						}
					}

					// deal with adding robots... we want to ensure that robots
					// always yield at least once before trying this...
					for (int i = 0; i < addRobotAtLocations.size(); i++) {
						Robot r = senseRobotAtHeight(
								addRobotAtLocations.get(i), addRobotAtHeights
										.get(i));
						if (r != null) {
							groupMembers.add(r.getID());
						}
					}
					addRobotAtLocations.clear();
					addRobotAtHeights.clear();

					// utility variables
					here = rc.getLocation();
					myType = rc.getRobotType();
					int myTypeOrdinal = myType.ordinal();
					myInfo = rc.senseRobotInfo(myRobot);

					// -------------------------------------------------------------------------
					// receive messages
					{
						int currentRound = Clock.getRoundNum();
						Message[] ms = rc.getAllMessages();
						for (Message m : ms) {
							int[] ints = m.ints;
							if (ints == null) {
								continue;
							}
							if (ints.length != messageIntsLength) {
								continue;
							}
							MapLocation[] locs = m.locations;
							if (locs == null) {
								continue;
							}
							if (locs.length != messageLocsLength) {
								continue;
							}
							if (ints[1] != (ints[0] ^ team_channel_magic_num
									^ locs[0].getX() ^ locs[0].getY())) {
								continue;
							}
							if (ints[0] < oldestValidMessage) {
								continue;
							}
							if (here.distanceSquaredTo(locs[0]) > broadcastRadiusSq) {
								continue;
							}
							int messageId = ints[18];
							if (!receivedMessages.add(messageId)) {
								continue;
							}

							// ok, the message seems believable...
							int fromGroup = ints[2];

							// ... if it's not for our group, don't read it,
							// unless we don't know what group we're in yet
							if ((fromGroup != groupId) && (groupId >= 0))
								continue;

							// from leader?
							boolean fromLeader = (ints[17] != 0);

							// vars...
							int senderId = ints[5];

							// set this stuff if the message is from the leader,
							// in particular, this shouldn't get set by the
							// leader
							if (fromLeader) {
								groupDirection = Direction.values()[ints[3]];
								groupCenter_ground = locs[2];
								groupCenter_air = locs[3];
								groupState = ints[6];
								for (int i = 0; i < 6; i++) {
									unitCounts[i] = ints[7 + i];
								}
								unitCountsChanged = true;
								spawningAllowed = (ints[13] != 0);
								groupType = ints[15];
							}

							// vars everyone should update (including leader)
							roundWhenGroupCanMove = ints[14];
							closestEnemyTower = locs[4];
							closestAllyTower = locs[5];

							// unit count delta
							addUnitOfType = ints[16];
							if (addUnitOfType >= 0) {
								unitCounts[addUnitOfType]++;
								if (addUnitOfType == _mortar
										|| addUnitOfType == _sniper) {
									unitCounts[_soldier]--;
								}
								if (addUnitOfType == _bomber) {
									unitCounts[_scout]--;
								}
								unitCountsChanged = true;
							}
							subtractUnitOfType = ints[19];
							if (subtractUnitOfType >= 0) {
								unitCounts[subtractUnitOfType]--;
							}

							// see if we're supposed to add a new robot to our
							// group
							addRobotAtLocation = locs[1];
							if (addRobotAtLocation != null) {
								addRobotAtHeight = ints[4];
								if (addRobotAtHeight == 2) {
									groupMembers.add(senderId);
								} else {
									if (isArchon) {
										addRobotAtLocations
												.add(addRobotAtLocation);
										addRobotAtHeights.add(addRobotAtHeight);
									} else if (groupId < 0) {
										if (here.equals(addRobotAtLocation)
												&& (myHeight == addRobotAtHeight)) {
											groupId = fromGroup;
										}
									}
								}
							}
						}
						oldestValidMessage = currentRound;
					}

					// reset message variables
					sendMessage = false;
					addRobotAtLocation = null;
					addUnitOfType = -1;
					subtractUnitOfType = -1;

					// -------------------------------------------------------------------------
					// if we don't have an id yet, this is as far as we go...
					if (groupId < 0)
						continue;

					// -------------------------------------------------------------------------
					// hack: don't go past here before round 10
					if (thisRound < 10) {
						continue;
					}

					// -------------------------------------------------------------------------
					// more utility variables
					MapLocation groupCenter = inAir ? groupCenter_air
							: groupCenter_ground;
					int groupRadiusSq = inAir ? groupRadiusSq_air
							: groupRadiusSq_ground;

					// -------------------------------------------------------------------------
					// work here : indicator strings
					{

						// work here
						// rc.setIndicatorString(0, "is nothing");

						// rc.setIndicatorString(0, "center: "
						// + U.sub(groupCenter, here));
						// rc.setIndicatorString(1, "rad: " + groupRadiusSq);
						// rc.setIndicatorString(1, "group size: "
						// + groupMembers.size());
						// rc.setIndicatorString(1, "group members: "
						// + groupMembers);
						// rc.setIndicatorString(2, "group size: "
						// + groupMembers.size());
						// rc.setIndicatorString(0, "to ground: "
						// + U.sub(groupCenter_ground, here));
						// rc.setIndicatorString(0, "state: " + groupState);
						// rc.setIndicatorString(2, "leader: " + isLeader);
						// rc.setIndicatorString(1, "desired: "
						// + U.sum(desiredUnitCounts));
						//
						// {
						// String s = "";
						// for (int i = 0; i < 6; i++) {
						// s += ", " + unitCounts[i];
						// }
						// rc.setIndicatorString(0, "units: " + s);
						// }
						// {
						// String s = "";
						// for (int i = 0; i < 6; i++) {
						// s += ", " + desiredUnitCounts[i];
						// }
						// rc.setIndicatorString(1, "desired: " + s);
						// }
						// rc.setIndicatorString(0, "reserve: " +
						// myInfo.energonReserve);
						// rc.setIndicatorString(2, "group id: " + groupId);
					}

					// -------------------------------------------------------------------------
					// see if we have moved, and update stuff based on that
					boolean haveMoved = false;
					if (!here.equals(lastLocation)) {
						haveMoved = true;
						Direction movedDirection = U.dirTo(lastLocation, here);
						lastLastLocation = lastLocation;
						lastLocation = here;

						if (isArchon) {
							// update map
							RegexMapModule.archonExplore(movedDirection);

							// check for towers
							for (Robot r : rc.senseNearbyGroundRobots()) {
								RobotInfo i = rc.senseRobotInfo(r);
								if (i.type == RobotType.TOWER) {
									RegexMapModule.markTower(i.location);
									towers.put(i.location, r);
								}
							}
						}
					}

					// -------------------------------------------------------------------------
					// update stuff based on unit counts
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

						groupRadiusSq_ground = groupSizeToRadiusSq[groundUnitCount];
						groupRadiusSq_air = groupSizeToRadiusSq[airUnitCount];

						// ------------------------------------------------------------------------
						// update desiredUnitCounts
						int numArchons = unitCounts[_archon];
						if (desiredUnitCounts_oldArchonCount != numArchons) {
							desiredUnitCounts_oldArchonCount = numArchons;

							desiredUnitCounts = new int[6];
							desiredUnitCounts[_archon] = unitCounts[_archon];

							if (groupType == group_type_fluff) {
								desiredUnitCounts[_bomber] = numArchons * 2;
							}
						}
					}

					// -------------------------------------------------------------------------
					// group leadership transfers
					if (isArchon) {
						// leader update (every 5 rounds, and then only
						// when
						// the
						// total number of archons changes)
						updateIsLeader_countDown--;
						if (updateIsLeader_countDown <= 0) {
							updateIsLeader_countDown = 5;

							int totalArchons = rc
									.getUnitCount(RobotType.ARCHON);
							if (totalArchons != updateIsLeader_previousArchonCount) {
								updateIsLeader_previousArchonCount = totalArchons;

								isLeader = false;
								for (MapLocation loc : rc.senseAlliedArchons()) {
									if (rc.canSenseSquare(loc)) {
										Robot r = rc
												.senseGroundRobotAtLocation(loc);
										int theirId = r.getID();
										if (groupMembers.contains(theirId)) {
											isLeader = (myId == theirId);
											if (isLeader) {
												sendMessage = true;
											}
											break;
										}
									}
								}
							}
						}
					}

					// -------------------------------------------------------------------------
					// healing
					{
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
						if (inAir) {
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
						// find the guy who's least healthy
						double lowestHealth = Double.MAX_VALUE;
						RobotInfo guy = null;
						for (RobotInfo i : adjacentFriends) {

							// don't heal towers
							if (i.type == RobotType.TOWER)
								continue;

							double health = (i.energonLevel + i.energonReserve)
									/ (i.maxEnergon + GameConstants.ENERGON_RESERVE_SIZE);
							if (health < lowestHealth) {
								lowestHealth = health;
								guy = i;
							}
						}
						if (guy != null) {
							// take a look at our health (our real health, not
							// including reserve)
							double ourHealth = myInfo.energonLevel
									/ myInfo.maxEnergon;

							// if we are healthier than them, give them the
							// difference (note that these are percents)
							if (ourHealth > lowestHealth) {
								double percentToGive = (ourHealth - lowestHealth) / 2.0;
								double amountToGive = percentToGive
										* myInfo.maxEnergon;

								// don't give more than will fit in their
								// reserve...
								double amountWillFit = GameConstants.ENERGON_RESERVE_SIZE
										- guy.energonReserve;
								if (amountToGive > amountWillFit) {
									amountToGive = amountWillFit;
								}
								rc
										.transferEnergon(
												amountToGive,
												guy.location,
												guy.type.isAirborne() ? RobotLevel.IN_AIR
														: RobotLevel.ON_GROUND);
							}
						}
					}

					// -------------------------------------------------------------------------
					// attacking
					if (!rc.isAttackActive() && !isArchon) {
						if (myType == RobotType.BOMBER) {
							double bestScore = -Double.MAX_VALUE;
							RobotInfo bestTarget = null;
							for (Robot r : rc.senseNearbyGroundRobots()) {
								RobotInfo i = rc.senseRobotInfo(r);
								if (i.team != team && i.energonLevel > -0.9) {
									if (rc.canAttackSquare(i.location)) {
										double score = -i.energonLevel;
										if (i.type == RobotType.ARCHON) {
											score += 1000;
										}
										if (i.type == RobotType.TOWER) {
											score -= 1000;

											// hack : don't attack distant
											// towers in fight mode (it'll keep
											// us in fight mode if we do)
											if (groupState == group_state_fight) {
												if (here
														.distanceSquaredTo(i.location) > 5) {
													score = -Double.MAX_VALUE;
												}
											}
										}
										if (score > bestScore) {
											bestScore = score;
											bestTarget = i;
										}
									}
								}
							}
							if (bestTarget != null) {
								rc.attackGround(bestTarget.location);
								continue;
							}
						}
					}

					// -------------------------------------------------------------------------
					// movement
					if (isLeader) {
						// see if we should update our status
						boolean updateStatus = false;
						// ... hack : update if sendMessage is true, since it
						// means we just became the leader
						if (sendMessage) {
							updateStatus = true;
						}
						// ... update if we have moved
						if (haveMoved) {
							updateStatus = true;
						}
						// ... update if we haven't done so in a while
						roundsSinceGroupLeaderStatusReport++;
						if (roundsSinceGroupLeaderStatusReport > 20) {
							updateStatus = true;
						}
						if (updateStatus) {
							roundsSinceGroupLeaderStatusReport = 0;
							sendMessage = true;

							// the follow code sets up initial variables,
							// and then gets a lot of information from the units
							// we see around us

							// init: health
							boolean everyoneIsHealthy = true;

							// init: update unit counts
							unitCounts = new int[6];
							unitCounts[myType.ordinal()]++; // count myself
							unitCountsChanged = true;

							// init: update spawning allowed
							spawningAllowed = false;

							// init: spot enemy
							int closestEnemyDistSq = Integer.MAX_VALUE;

							// init: detect when attack mode ends
							boolean bomberAttacking = false;

							// collect information
							for (Robot r : rc.senseNearbyGroundRobots()) {
								int id = r.getID();
								RobotInfo i = rc.senseRobotInfo(r);
								if (groupMembers.contains(id)) {
									unitCounts[i.type.ordinal()]++;
									if (i.energonLevel < i.maxEnergon / 2)
										everyoneIsHealthy = false;
								}
								if (i.type == RobotType.TOWER) {
									if (i.team == team) {
										if (i.location
												.equals(closestEnemyTower)) {
											if (closestEnemyTower
													.equals(lastTakenTower)) {
												sameTowerRetakeCount++;
											} else {
												sameTowerRetakeCount = 0;
											}
											lastTakenTower = closestEnemyTower;
											closestEnemyTower = null;
										}
										if (here.distanceSquaredTo(i.location) <= 2) {
											spawningAllowed = true;
										}
										if ((closestAllyTower == null)
												|| (groupCenter_ground
														.distanceSquaredTo(i.location) < groupCenter_ground
														.distanceSquaredTo(closestAllyTower))) {
											closestAllyTower = i.location;
										}
									} else {
										if ((closestEnemyTower == null)
												|| (groupCenter_ground
														.distanceSquaredTo(i.location) < groupCenter_ground
														.distanceSquaredTo(closestEnemyTower))) {
											closestEnemyTower = i.location;
										}
									}
								} else {
									if (i.team != team) {
										int distSq = i.location
												.distanceSquaredTo(groupCenter_ground);
										if (distSq < closestEnemyDistSq) {
											closestEnemyDistSq = distSq;
										}
									}
								}
							}
							for (Robot r : rc.senseNearbyAirRobots()) {
								int id = r.getID();
								RobotInfo i = rc.senseRobotInfo(r);
								if (groupMembers.contains(id)) {
									unitCounts[i.type.ordinal()]++;
									if (i.energonLevel < i.maxEnergon / 2)
										everyoneIsHealthy = false;
									if (i.type == RobotType.BOMBER) {
										if (i.roundsUntilAttackIdle > 0) {
											bomberAttacking = true;
										}
									}
								}
								if (i.team != team) {
									int distSq = i.location
											.distanceSquaredTo(groupCenter_ground);
									if (distSq < closestEnemyDistSq) {
										closestEnemyDistSq = distSq;
									}
								}
							}

							// post: unit count
							int desiredUnitCountSum = U.sum(desiredUnitCounts);
							int unitCountSum = U.sum(unitCounts);
							// ... detect when we need to spawn more bombers
							if (groupState != group_state_spawning) {
								int diff = desiredUnitCountSum - unitCountSum;
								if (diff > 1) {
									if (groupState == group_state_seek_towers) {
										groupState = group_state_spawning;
									} else {
										groupState = group_state_flee;
									}
								} else if (diff < -1) {
									// we probably lost an archon, so let's
									// boogie
									groupState = group_state_flee;
								}
							}

							// post: health
							if (groupState == group_state_spawning) {
								if (everyoneIsHealthy) {
									int desiredCount = desiredUnitCountSum
											- unitCountSum;

									if ((desiredCount <= 0)
											&& (desiredCount >= -1)) {
										// hack : don't let this happen too
										// early in the game,
										// before unitCount values have
										// settled...
										if (thisRound > 100) {
											groupState = group_state_seek_towers;
										}
									}
								}
							}

							// post: decide when to exit fight mode
							if (groupState == group_state_fight) {
								if (!bomberAttacking) {
									groupState = group_state_seek_towers;
									groupCenter_ground = groupCenter_air;
								}
							}

							// post: spot enemy
							if (groupState == group_state_seek_towers) {
								if (closestEnemyDistSq <= 25) {
									groupState = group_state_fight;
								}
							}

							// post: detect tower duel
							if (sameTowerRetakeCount >= 3) {
								windDirection = 3;
								windCountdownRounds = 250;
							}
						}

						// -------------------------------------------------------------------------
						// leader movement
						if (!rc.isMovementActive()) {
							if (!here.equals(groupCenter)) {
								Direction toCenter = U.dirTo(here, groupCenter);
								int ret = moveIfCan_faceIfCould(toCenter);
								if (ret == 0) {
									// find out why we can't move there...
									Robot guyBlockingUs = rc
											.senseGroundRobotAtLocation(groupCenter);
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
									if (waitToGetToCenter <= 0
											&& (groupState != group_state_fight)) {
										Direction d = Direction.values()[rand
												.nextInt(8)];
										groupCenter_ground = here.add(d);
										groupCenter_air = groupCenter_ground;
										groupDirection = d;
										waitToGetToCenter = 5;
										sendMessage = true;
									}
								}
							}
						}

						// -------------------------------------------------------------------------
						// leader navigation
						if (here.equals(groupCenter)) {
							groupMembersInRadiiTimeout--;
							if (!groupMembersInRadii) {
								groupMembersInRadii = true;
								l1: do {
									for (Robot r : rc.senseNearbyGroundRobots()) {
										if (groupMembers.contains(r.getID())) {
											RobotInfo i = rc.senseRobotInfo(r);
											if (i.location
													.distanceSquaredTo(groupCenter_ground) > groupRadiusSq_ground) {
												groupMembersInRadii = false;
												break l1;
											}
										}
									}
									for (Robot r : rc.senseNearbyAirRobots()) {
										if (groupMembers.contains(r.getID())) {
											RobotInfo i = rc.senseRobotInfo(r);
											if (i.location
													.distanceSquaredTo(groupCenter_air) > groupRadiusSq_air) {
												groupMembersInRadii = false;
												break l1;
											}
										}
									}
								} while (false);
							}
						}

						boolean readyToNavigate = false;
						if (here.equals(groupCenter)
								&& (groupMembersInRadii || (groupMembersInRadiiTimeout <= 0)))
							readyToNavigate = true;
						if (thisRound < roundWhenGroupCanMove)
							readyToNavigate = false;
						if (groupState == group_state_fight)
							readyToNavigate = true;
						if (here.equals(groupCenter)
								&& (groupState == group_state_flee))
							readyToNavigate = true;

						roundsInThisState++;

						if (groupState == group_state_seek_towers) {
							windCountdownRounds--;
						}

						if (readyToNavigate) {
							// --------------------------------------------------------
							if (groupState != prevGroupState) {
								prevGroupState = groupState;
								rippleCountdown = 0;
								fatWallCountdown = 0;
								ripples = null;
								sendMessage = true;
								roundsInThisState = 0;
							}
							rippleCountdown--;
							fatWallCountdown--;

							MapLocation[] dests = null;
							if (groupState == group_state_spawning) {
								dests = rc.senseAlliedTowers();
								if (dests.length == 0) {
									groupState = group_state_seek_towers;
								}
								if (roundsInThisState > 500) {
									groupState = group_state_seek_towers;
								}
							}
							if (groupState == group_state_seek_towers) {
								MapLocation dest = closestEnemyTower;
								Direction toTower = rc
										.senseClosestUnknownTower();

								if ((toTower == null)
										|| (toTower.ordinal() >= 8)) {
									groupState = group_state_spawning;
									sendMessage = true;
								} else {
									if (rippleCountdown <= 0) {
										if (dest == null) {
											// try picking a tower we've seen
											// before
											// in the proper direction
											int bestDistSq = Integer.MAX_VALUE;
											Set<MapLocation> goodTowers = new HashSet<MapLocation>(
													Arrays
															.asList(rc
																	.senseAlliedTowers()));
											for (MapLocation pos : towers
													.keySet()) {
												// if it's in the right
												// direction,
												// and it's not a good tower...
												if ((U.dirTo(here, pos) == toTower)
														&& !goodTowers
																.contains(pos)) {
													// ... choose the closest
													// such
													// tower
													int distSq = here
															.distanceSquaredTo(pos);
													if (distSq < bestDistSq) {
														bestDistSq = distSq;
														dest = pos;
													}
												}
											}
										}
										if (dest == null) {
											// try picking a spot to go which is
											// the
											// first unrevealed location toward
											// the
											// tower
											MapLocation cursor = here
													.add(toTower);
											while (true) {
												TerrainTile t = rc
														.senseTerrainTile(cursor);
												if (t == null) {
													dest = cursor;
													break;
												}
												if (t.getType() == TerrainTile.TerrainType.OFF_MAP) {
													break;
												}
												cursor = cursor.add(toTower);
											}
										}
										if (dest == null) {
											ripples = U.addRipples(here,
													toTower, new int[] { 10,
															12, 25 });
										} else {
											if (!here.isAdjacentTo(dest)) {
												ripples = new MapLocation[] { dest };
											}
										}

										// wind...
										if (windCountdownRounds > 0) {
											Direction d = rc.senseEnemyArchon();
											if (groupId % 2 != 0) {
												for (int i = 0; i < windDirection; i++) {
													d = d.rotateLeft();
												}
											} else {
												for (int i = 0; i < windDirection; i++) {
													d = d.rotateRight();
												}
											}
											ripples = U.addRipples(here, d,
													new int[] { 10, 12, 25 });
										}

										rippleCountdown = 3;
									}
									dests = ripples;
								}
							}
							if (groupState == group_state_fight) {
								MapLocation oldGroupCenter_ground = groupCenter_ground;

								Direction awayFromEnemy = rc.senseEnemyArchon()
										.opposite();
								groupCenter_ground = groupCenter_air
										.add(awayFromEnemy);

								// if we can't retreat to this square, flee
								TerrainTile t = rc
										.senseTerrainTile(groupCenter_ground);
								if (t == null
										|| !t
												.isTraversableAtHeight(RobotLevel.ON_GROUND)) {
									groupState = group_state_flee;
									fleeingFrom = U.add(groupCenter_air, rc
											.senseEnemyArchon(), 3);
								}

								if (unitCounts[_archon] >= 3
										&& unitCounts[_bomber] >= 5) {
									groupCenter_ground = groupCenter_ground
											.add(awayFromEnemy);

									// if we can't retreat to this square, don't
									// go back this far
									t = rc.senseTerrainTile(groupCenter_ground);
									if (t == null
											|| !t
													.isTraversableAtHeight(RobotLevel.ON_GROUND)) {
										groupCenter_ground = groupCenter_ground
												.add(awayFromEnemy.opposite());
									}
								}

								// if we're still fighting, and haven't decided
								// to flee, then give directions
								if (groupState == group_state_fight) {

									// this if is put here to keep us from
									// resending the same message
									if (!oldGroupCenter_ground
											.equals(groupCenter_ground)) {
										groupDirection = U.dirTo(
												groupCenter_ground,
												groupCenter_air);
										if (groupDirection.ordinal() >= 8) {
											groupDirection = Direction.NORTH;
										}
										waitToGetToCenter = 5;
										sendMessage = true;
									}
								}
							}
							if (groupState == group_state_flee) {
								// where should we go?
								if (fleeingFrom == null) {
									fleeingFrom = U.add(here, rc
											.senseEnemyArchon(), 7);
								}

								// try adding towers that are a certain radius
								// away
								Vector<MapLocation> safeTowers = new Vector();
								MapLocation[] allTowers = rc
										.senseAlliedTowers();
								for (MapLocation t : allTowers) {
									if (t.distanceSquaredTo(fleeingFrom) > 13 * 13) {
										safeTowers.add(t);
									}
								}
								if (safeTowers.size() > 0) {
									dests = new MapLocation[safeTowers.size()];
									for (int i = 0; i < safeTowers.size(); i++) {
										dests[i] = safeTowers.get(i);
									}
								} else if (allTowers.length > 0) {
									dests = allTowers;
								} else {
									dests = U.addRipples(here, rc
											.senseClosestUnknownTower(),
											new int[] { 10, 12, 25 });
								}

								// see if we are done fleeing...
								if (roundsInThisState > 50
										&& here
												.distanceSquaredTo(closestAllyTower) <= 2) {
									groupState = group_state_spawning;
								}
							}

							Direction d = null;
							if (dests != null && dests.length > 0) {
								if (fatWallCountdown <= 0) {
									d = RegexMapModule
											.atobFatWalls(here, dests);
								}
								if (d == null) {
									d = RegexMapModule.atob(here, dests);
									if (fatWallCountdown <= 0) {
										fatWallCountdown = 10;
									}
								}
							}

							if (d != null) {
								MapLocation newPos = here.add(d);
								boolean newPositionGood = true;

								// make sure we aren't trying to move onto a
								// tower
								Robot r = rc.senseGroundRobotAtLocation(newPos);
								if (r != null) {
									RobotInfo info = rc.senseRobotInfo(r);
									if (info.type == RobotType.TOWER) {
										newPositionGood = false;
									}
								}

								if (newPositionGood) {
									groupCenter_ground = newPos;
									groupCenter_air = groupCenter_ground;
									groupDirection = d;
									waitToGetToCenter = 5;
									sendMessage = true;
									groupMembersInRadii = false;
									groupMembersInRadiiTimeout = 20;
								}
							}

						}

					} else {
						// -------------------------------------------------------------------------
						// non-leader movement
						if (!rc.isMovementActive()) {

							// if this is a bomber in flee mode, then approach
							// the enemy
							if ((myType == RobotType.BOMBER)
									&& (groupState == group_state_flee)) {
								groupCenter = U.add(here,
										rc.senseEnemyArchon(), 5);
							}

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
									continue mainLoop;
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
									MapLocation front = here
											.add(groupDirection);
									if ((inAir || !front.equals(groupCenter))
											&& groupCenter
													.distanceSquaredTo(front) <= groupRadiusSq) {
										if (moveIfCan_faceIfCould(groupDirection) > 0) {
											continue;
										}
									}
									front = here.add(groupDirection
											.rotateLeft());
									if ((inAir || !front.equals(groupCenter))
											&& groupCenter
													.distanceSquaredTo(front) <= groupRadiusSq) {
										if (moveIfCan_faceIfCould(groupDirection
												.rotateLeft()) > 0) {
											continue;
										}
									}
									front = here.add(groupDirection
											.rotateRight());
									if ((inAir || !front.equals(groupCenter))
											&& groupCenter
													.distanceSquaredTo(front) <= groupRadiusSq) {
										if (moveIfCan_faceIfCould(groupDirection
												.rotateRight()) > 0) {
											continue;
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
									continue mainLoop;
								} else {
									TerrainTile t = rc.senseTerrainTile(here
											.add(bugDirection));
									if (t == null) {
										rc.setDirection(bugDirection);
										continue mainLoop;
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

							// -------------------------------------------------------------------------
							// odd movement behaviors, if we're doing nothing
							// better

							// if we're a bomber, with nothing better to do,
							// then turn around, to keep us looking both ways,
							// so we
							// can shoot stuff
							if (myType == RobotType.BOMBER) {
								rc.setDirection(rc.getDirection().opposite());
								continue;
							}
						}
					}

					// -------------------------------------------------------------------------
					// spawning / evolution
					do {
						if (groupState != group_state_spawning)
							break;

						// if we desire less of this unit type (and nothing that
						// it evolves into),
						// then get rid of this unit
						if (unitCounts[myTypeOrdinal] > desiredUnitCounts[myTypeOrdinal]) {
							// hack : don't let them die too young, before
							// they've really had a chance to think it over
							if (thisRound - birthday > 20) {
								// add some randomness, so not everyone kills
								// themselves at once
								if (rand.nextInt(10) == 0) {
									if (myTypeOrdinal == _scout) {
										if (unitCounts[_bomber] >= desiredUnitCounts[_bomber]) {
											subtractUnitOfType = myTypeOrdinal;
											sendMessage = true;
											continue mainLoop;
										}
									} else if (myTypeOrdinal == _soldier) {
										if ((unitCounts[_mortar] >= desiredUnitCounts[_bomber])
												&& (unitCounts[_sniper] >= unitCounts[_sniper])) {
											subtractUnitOfType = myTypeOrdinal;
											sendMessage = true;
											continue mainLoop;
										}
									} else {
										subtractUnitOfType = myTypeOrdinal;
										sendMessage = true;
										continue mainLoop;
									}
								}
							}
						}

						// hack : remmed out lines below: don't care if we're
						// near the center,
						// because otherwise we wouldn't have reached this code;
						// this allows people who are not actually near the
						// center,
						// but can't get there, to build stuff
						//
						// if (here.distanceSquaredTo(groupCenter) >
						// groupRadiusSq)
						// break;

						if (!(rc.getEnergonLevel() > (isArchon ? 40 : 6)))
							break;
						if (isArchon && !rc.canSpawn())
							break;

						// ok, now we can spawn/evolve... if we want...
						if (isArchon) {
							int wantAirAmount = (desiredUnitCounts[_scout] + desiredUnitCounts[_bomber])
									- (unitCounts[_scout] + unitCounts[_bomber]);
							int wantGroundAmount = (desiredUnitCounts[_soldier]
									+ desiredUnitCounts[_mortar] + desiredUnitCounts[_sniper])
									- (unitCounts[_soldier]
											+ unitCounts[_mortar] + unitCounts[_sniper]);
							boolean wantAir = (wantAirAmount > 0);
							boolean wantGround = (wantGroundAmount > 0);
							if (wantAir && (rand.nextBoolean() || !wantGround)) {
								// try for scout
								MapLocation front = here.add(rc.getDirection());
								if (rc.senseAirRobotAtLocation(front) == null) {
									rc.spawn(RobotType.SCOUT);

									// hack : units don't *always* appear the
									// same round they are spawned, it seems
									rc.yield();
									lastRound = thisRound;

									addUnitOfType = _scout;
									sendMessage = true;
									addRobotAtHeight = 1;
									addRobotAtLocation = front;
									roundWhenGroupCanMove = Math.max(
											roundWhenGroupCanMove,
											thisRound + 50);
									continue mainLoop;
								} else if (!rc.isMovementActive()) {
									faceRandomDirection(rand);
									continue mainLoop;
								}
							} else if (wantGround) {
								// try for soldier
								if (rc.canMove(rc.getDirection())) {
									rc.spawn(RobotType.SOLDIER);

									// hack : units don't *always* appear the
									// same round they are spawned, it seems
									rc.yield();
									lastRound = thisRound;

									MapLocation front = here.add(rc
											.getDirection());
									addUnitOfType = _soldier;
									sendMessage = true;
									addRobotAtHeight = 0;
									addRobotAtLocation = front;
									roundWhenGroupCanMove = Math.max(
											roundWhenGroupCanMove,
											thisRound + 50);
									continue mainLoop;
								} else if (!rc.isMovementActive()) {
									faceRandomDirection(rand);
									continue mainLoop;
								}
							}
						} else {
							if (myType == RobotType.SCOUT) {
								if (desiredUnitCounts[_bomber] > unitCounts[_bomber]) {
									rc.evolve(RobotType.BOMBER);
									addUnitOfType = _bomber;
									roundWhenGroupCanMove = Math.max(
											roundWhenGroupCanMove,
											thisRound + 50);
									sendMessage = true;
									continue mainLoop;
								}
							} else if (myType == RobotType.SOLDIER) {
								if (desiredUnitCounts[_mortar] > unitCounts[_mortar]) {
									rc.evolve(RobotType.MORTAR);
									addUnitOfType = _mortar;
									roundWhenGroupCanMove = Math.max(
											roundWhenGroupCanMove,
											thisRound + 50);
									sendMessage = true;
									continue mainLoop;
								}
								if (desiredUnitCounts[_sniper] > unitCounts[_sniper]) {
									double energon = rc.getEnergonLevel();
									rc.evolve(RobotType.SNIPER);
									addUnitOfType = _sniper;
									roundWhenGroupCanMove = Math.max(
											roundWhenGroupCanMove,
											thisRound + 50);
									sendMessage = true;
									continue mainLoop;
								}
							}
						}
					} while (false);

				} catch (Exception e) {
					// oh well, we'll just loop around again, and hopefully
					// things will be fine now
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			// hopefully this doesn't happen, if it does, we'll die
		}
	} // run
}
