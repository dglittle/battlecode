package team022;

import java.util.Arrays;
import java.util.HashSet;
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
import battlecode.common.TerrainTile;
import battlecode.common.TerrainTile.TerrainType;

public class Archon extends RobotPlayer {
	Direction initialToEnemy = null;
	MapLocation initialGoal;
	int state;

	int birthCountdown = 0;
	RobotType birthType = null;
	MapLocation birthLocation = null;

	RobotType tryToSpawn = null;
	int tryToSpawnCountdown = 0;

	Set<Integer> children = new HashSet<Integer>();

	int fleeCountdown = 0;
	Direction fleeDirection = null;

	boolean dogMode = false;
	boolean wasDogMode = false;

	public Archon(RobotController rc) {
		super(rc);

		state = 0;
		MapLocation[] locs = new MapLocation[] { add(here, 0, -6),
				add(here, 6, 0), add(here, 0, 6), add(here, -6, 0), };
		int ax = 0;
		int ay = 0;
		int count = 0;
		for (MapLocation loc : locs) {
			if (rc.senseTerrainTile(loc).getType() != TerrainType.OFF_MAP) {
				ax += loc.getX();
				ay += loc.getY();
				count++;
			}
		}
		ax /= count;
		ay /= count;
		initialToEnemy = here.directionTo(new MapLocation(ax, ay));
		double spread = count > 2 ? 180 : 90;

		double angle = dirToDeg(initialToEnemy);
		int i = getArchonIndex();
		angle = lerp(0, angle - (spread / 2), 6, angle + (spread / 2), 0.5 + i);
		angle = Math.toRadians(angle);
		initialGoal = add(here, (int) (Math.sin(angle) * 1000), (int) (-Math
				.cos(angle) * 1000));

		int numOldArchons = 6;
		for (long num : rc.getOldArchonMemory()) {
			if (num < numOldArchons) {
				numOldArchons = (int) num;
			}
		}
		if (numOldArchons > 0 && numOldArchons < 3) {
			dogMode = true;
			wasDogMode = true;
		}
	}

	public boolean loop() throws Exception {

		if (wasDogMode) {
			rc.setArchonMemory(1);
		} else {
			rc.setArchonMemory(rc.senseAlliedArchons().length);
		}

		if (round > 1000)
			dogMode = false;

		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// flee

		if (fleeCountdown <= 0) {
			if ((enemyGroundRobotInfos.size() > 0) && (round % 3 == 0)) {
				if (false) {
					boolean cannon = false;
					for (RobotInfo ri : nearbyRobotInfos) {
						if (ri.roundsUntilAttackIdle > ri.type.attackDelay())
							continue;
						if (ri.type == RobotType.CANNON) {
							if (superNear(ri.location)) {
								cannon = true;
								break;
							}
						}
					}
					int enemies = 0;
					int soldiers = 0;
					for (RobotInfo ri : enemyGroundRobotInfos) {
						if (ri.directionFacing == ri.location.directionTo(here)) {
							if (ri.type == RobotType.SOLDIER) {
								soldiers++;
							}
							if (ri.type != RobotType.WORKER) {
								enemies++;
							}
						}
					}
					if ((cannon && (soldiers > 3))
							|| (!cannon && (enemies > 0))) {
						fleeCountdown = 100;
						fleeDirection = null;
					}
				} else {
					int tide = 0;
					for (RobotInfo ri : nearbyRobotInfos) {
						if (ri.type == RobotType.CANNON) {
							tide += 2;
						} else if (ri.type == RobotType.SOLDIER) {
							tide += 1;
						}
					}
					for (RobotInfo ri : enemyGroundRobotInfos) {
						if (ri.type == RobotType.CANNON) {
							tide -= 2;
						} else if (ri.type != RobotType.WORKER) {
							tide -= 1;
						}
					}
					if (tide < 0) {
						fleeCountdown = 100;
						fleeDirection = null;
					}
				}
			}
		}

		if (!rc.isMovementActive()) {
			MapLocation away = here;
			for (RobotInfo ri : enemyGroundRobotInfos) {
				MapLocation pos = ri.location;
				int dist = here.distanceSquaredTo(pos);
				int range = (int) Math.sqrt(ri.type.attackRadiusMaxSquared());
				if (dist <= sq(range + 1)) {
					away = away.add(here.directionTo(pos).opposite());
				}
			}
			if (!away.equals(here)) {
				if (tryMove3(away))
					return true;
			}
		}

		if (fleeCountdown > 0) {
			fleeCountdown--;
			if (!rc.isMovementActive()) {
				int[] dirScores = new int[8];

				Direction dir = null;

				MapLocation enemy = here;
				for (RobotInfo ri : enemyGroundRobotInfos) {
					if (ri.type != RobotType.WORKER) {
						enemy = enemy.add(here.directionTo(ri.location));
					}
				}
				dir = here.directionTo(enemy);
				if (dir.ordinal() < 8) {
					dirScores[dir.ordinal()] -= 4;
					dirScores[dir.rotateLeft().ordinal()] -= 2;
					dirScores[dir.rotateRight().ordinal()] -= 2;
				}

				if (fleeDirection != null) {
					dir = fleeDirection.opposite();
					dirScores[dir.ordinal()] -= 2;
					dirScores[dir.rotateLeft().ordinal()] -= 1;
					dirScores[dir.rotateRight().ordinal()] -= 1;
				}

				for (int i = 0; i < 8; i += 2) {
					dir = Direction.values()[i];
					if (rc.senseTerrainTile(add(here, dir, 6)) == TerrainTile.OFF_MAP) {
						dirScores[dir.ordinal()] -= 10;
						dirScores[dir.rotateLeft().ordinal()] -= 10;
						dirScores[dir.rotateRight().ordinal()] -= 10;
					}
				}

				Direction bestDir = null;
				int bestScore = Integer.MIN_VALUE;
				for (int i = 0; i < 8; i++) {
					int score = dirScores[i];
					if (score > bestScore) {
						bestScore = score;
						bestDir = Direction.values()[i];
					}
				}
				fleeDirection = bestDir;

				tryMove(fleeDirection);
				return true;
			}
			return true;
		}

		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// have babies instinct

		while ((tryToSpawnCountdown == 0) && (r.nextDouble() < 0.1)) {
			// do we have enough energon?
			if (energon <= 0.9 * type.maxEnergon())
				break;

			// what are we supporting?
			double totalUpkeep = 0;
			int[] counts = new int[RobotType.values().length];
			for (Robot r : rc.senseNearbyAirRobots()) {
				if (children.contains(r.getID())) {
					RobotInfo ri = rc.senseRobotInfo(r);
					counts[ri.type.ordinal()]++;
					totalUpkeep += ri.type.energonUpkeep();
				}
			}
			for (Robot r : rc.senseNearbyGroundRobots()) {
				if (children.contains(r.getID())) {
					RobotInfo ri = rc.senseRobotInfo(r);
					counts[ri.type.ordinal()]++;
					totalUpkeep += ri.type.energonUpkeep();
				}
			}
			double spare = rc.getEnergonProduction() - totalUpkeep;

			RobotType t = null;

			if (dogMode) {
				if (counts[RobotType.SOLDIER.ordinal()] < 3) {
					t = RobotType.SOLDIER;
				}
			} else {
				if ((rc.senseFluxDepositAtLocation(here) != null)
						&& counts[RobotType.WORKER.ordinal()] < 1) {
					t = RobotType.WORKER;
				} else if (counts[RobotType.SOLDIER.ordinal()] == 0
						&& counts[RobotType.CANNON.ordinal()] < 2) {
					t = RobotType.CANNON;
				} else if (counts[RobotType.SCOUT.ordinal()] < 2) {
					t = RobotType.SCOUT;
				}
			}

			if (t != null) {
				tryToSpawn = t;
				tryToSpawnCountdown = 10;
			}
			break;
		}
		if (tryToSpawnCountdown > 0) {
			tryToSpawnCountdown--;
			while (true) {
				// need to be facing an empty square
				TerrainType tt = rc.senseTerrainTile(front).getType();
				if (tt == TerrainType.OFF_MAP)
					break;
				if ((tt == TerrainType.VOID) && !tryToSpawn.isAirborne())
					break;
				if (senseRobotAtLocation(tryToSpawn, front) != null)
					break;

				// ok, try to spawn it
				rc.spawn(tryToSpawn);
				rc.yield();
				{
					int targetId = senseRobotAtLocation(tryToSpawn, front)
							.getID();
					children.add(targetId);
					int round = Clock.getRoundNum();
					Message m = new Message();
					m.locations = new MapLocation[] { here };
					m.ints = new int[] { round,
							here.hashCode() * round + spice, 2, targetId,
							getArchonIndex() };
					rc.broadcast(m);
				}
				birthCountdown = tryToSpawn.wakeDelay();
				birthType = tryToSpawn;
				birthLocation = front;
				tryToSpawnCountdown = 0;
				return true;
			}
			if (!rc.isMovementActive()) {
				rc.setDirection(randomDir());
				return true;
			}
		}

		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// sleep while moving instinct

		if (rc.isMovementActive()) {
			return true;
		}

		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// wait for babies

		if (birthCountdown > 0) {
			birthCountdown--;

			if (!superNear(birthLocation)) {
				tryMove(birthLocation);
				return true;
			} else {
				Robot r = senseRobotAtLocation(birthType, birthLocation);
				if ((r == null) || (rc.senseRobotInfo(r).team != team)) {
					birthCountdown = 0;
				} else {
					return true;
				}
			}
		}

		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// call of the flux

		if (true) {
			// should we go after some flux?
			MapLocation ourFlux = null;
			for (FluxDeposit f : rc.senseNearbyFluxDeposits()) {
				FluxDepositInfo fi = rc.senseFluxDepositInfo(f);
				if (here.equals(closest(fi.location, rc.senseAlliedArchons()))) {
					ourFlux = fi.location;
					if (getArchonIndex() != 0) {
						dogMode = false;
					} else {
						boolean tooClose = false;
						for (MapLocation pos : rc.senseAlliedArchons()) {
							if (!pos.equals(here)) {
								if (pos.distanceSquaredTo(here) <= 36) {
									tooClose = true;
								}
							}
						}
						if (!tooClose) {
							dogMode = false;
						}
					}
					break;
				}
			}
			if (!dogMode) {
				if (initialGoal != null) {
					if (ourFlux != null) {
						initialGoal = null;
					} else if (here.equals(initialGoal)
							|| (rc.senseTerrainTile(
									here.add(here.directionTo(initialGoal)))
									.getType() == TerrainType.OFF_MAP)) {
						initialGoal = null;
					} else if (Clock.getRoundNum() > 1000) {
						initialGoal = null;
					}
					if (initialGoal != null) {
						tryMove(initialGoal);
						return true;
					}
				}
				if (initialGoal == null) {
					if (ourFlux != null) {
						if (!here.equals(ourFlux)) {
							tryMove(ourFlux);
							return true;
						}
					} else {
						tryMove(rc.senseDirectionToUnownedFluxDeposit());
						return true;
					}
				}
			}
		}

		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// super plus

		if (dogMode) {
			int archonIndex = getArchonIndex();
			if (archonIndex == 0) {
				// see if we should wait for any babies
				boolean waking = false;
				for (RobotInfo ri : nearbyRobotInfos) {
					if (ri.roundsUntilAttackIdle > ri.type.attackDelay()) {
						waking = true;
						break;
					}
				}
				if (true || !waking) {
					tryMove(rc.senseDirectionToUnownedFluxDeposit());
					// tryMove(initialToEnemy);
					return true;
				}
			} else {
				int dx = 0;
				int dy = 0;
				if (archonIndex == 1) {
					dx = 0;
					dy = 2;
				} else if (archonIndex == 2) {
					dx = 2;
					dy = 0;
				} else if (archonIndex == 3) {
					dx = 0;
					dy = -2;
				} else if (archonIndex == 4) {
					dx = -2;
					dy = 0;
				} else if (archonIndex == 5) {
					dx = 2;
					dy = 2;
				}
				MapLocation goal = add(rc.senseAlliedArchons()[0], dx, dy);
				tryMove(goal);
				return true;
			}
		}

		return false;
	}
}
