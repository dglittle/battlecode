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
import battlecode.common.TerrainTile.TerrainType;

public class Scout extends RobotPlayer {
	public Scout(RobotController rc) {
		super(rc);
	}

	Vector<Message> ms = new Vector();

	public boolean loop() throws Exception {

		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// get between cannon and archon

		if (!rc.isMovementActive()) {
			MapLocation best = null;
			int bestScore = Integer.MIN_VALUE;
			for (RobotInfo ri : nearbyRobotInfos) {
				if (ri.type == RobotType.CANNON) {
					MapLocation pos = ri.location;
					if (here.equals(pos)) {
						best = null;
						break;
					}
					if (rc.senseAirRobotAtLocation(pos) == null) {
						int score = -here.distanceSquaredTo(pos);
						if (score > bestScore) {
							bestScore = score;
							best = pos;
						}
					}
				}
			}
			if (best != null) {
				tryMove(best);
				return true;
			}
		}
		if (false) {
			MapLocation best = null;
			int bestScore = Integer.MIN_VALUE;
			for (RobotInfo ri : nearbyRobotInfos) {
				if (ri.type == RobotType.CANNON) {
					MapLocation pos = ri.location;
					MapLocation archon = closest(pos, rc.senseAlliedArchons());
					MapLocation desirable = null;
					if (pos.isAdjacentTo(archon)) {
						desirable = pos;
					} else {
						Direction d = pos.directionTo(archon);
						if (d.ordinal() < 8) {
							desirable = pos.add(d);
						}
					}
					if (desirable != null) {
						if (here.equals(desirable)) {
							best = null;
							break;
						}
						if (rc.canSenseSquare(desirable)) {
							if (rc.senseAirRobotAtLocation(desirable) == null) {
								int score = -here.distanceSquaredTo(desirable);
								if (score > bestScore) {
									bestScore = score;
									best = desirable;
								}
							}
						}
					}
				}
			}
			if (best != null) {
				tryMove(best);
				return true;
			}
		}

		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// shoot stuff

		if (!rc.isAttackActive()) {
			MapLocation bestPos = null;
			int closest = Integer.MAX_VALUE;

			for (RobotInfo ri : enemyAirRobotInfos) {
				if (!rc.canAttackSquare(ri.location))
					continue;
				int dist = here.distanceSquaredTo(ri.location);
				if (ri.type == RobotType.ARCHON) {
					dist -= 10000;
				}
				if (dist < closest) {
					closest = dist;
					bestPos = ri.location;
				}
			}
			if (bestPos != null) {
				attack(bestPos, RobotLevel.IN_AIR);
				return true;
			}

			// try facing someone if they are in range
			if (!rc.isMovementActive()) {
				for (RobotInfo ri : enemyAirRobotInfos) {
					if (inRange(ri.location)) {
						rc.setDirection(here.directionTo(ri.location));
						return true;
					}
				}
			}
		}

		if (enemyGroundRobotInfos.size() > 0) {
			// if there are enemies about, then we want to stay with the cannon
			return true;
		}

		return false;
	}
}
