package team022;

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

public class Cannon extends RobotPlayer {
	public Cannon(RobotController rc) {
		super(rc);
	}

	public boolean loop() throws Exception {

		if (rc.isMovementActive() && rc.isAttackActive())
			return false;

		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// shoot stuff

		MapLocation enemy = here;

		MapLocation bestAttackPos = null;
		RobotLevel bestAttackHeight = null;
		int attackClosest = Integer.MAX_VALUE;

		MapLocation bestInRangePos = null;
		int inRangeClosest = Integer.MAX_VALUE;

		for (RobotInfo ri : enemyAirRobotInfos) {
			MapLocation pos = ri.location;
			int dist = here.distanceSquaredTo(pos);
			if (inRange(pos) && (dist < inRangeClosest)) {
				inRangeClosest = dist;
				bestInRangePos = pos;
			}
			if (rc.canAttackSquare(pos) && (dist < attackClosest)) {
				attackClosest = dist;
				bestAttackPos = pos;
				bestAttackHeight = RobotLevel.IN_AIR;
			}
		}
		for (RobotInfo ri : enemyGroundRobotInfos) {
			MapLocation pos = ri.location;
			int dist = here.distanceSquaredTo(pos);
			if (inRange(pos) && (dist < inRangeClosest)) {
				inRangeClosest = dist;
				bestInRangePos = pos;
			}
			if (rc.canAttackSquare(pos) && (dist < attackClosest)) {
				attackClosest = dist;
				bestAttackPos = pos;
				bestAttackHeight = RobotLevel.ON_GROUND;
			}
		}
		for (int i = 0; i < unseenEnemyLocations.size(); i++) {
			MapLocation pos = unseenEnemyLocations.get(i);
			enemy = enemy.add(here.directionTo(pos));
			int dist = here.distanceSquaredTo(pos);
			if (inRange(pos) && (dist < inRangeClosest)) {
				inRangeClosest = dist;
				bestInRangePos = pos;
			}
			if (rc.canAttackSquare(pos) && (dist < attackClosest)) {
				attackClosest = dist;
				bestAttackPos = pos;
				bestAttackHeight = unseenEnemyTypes.get(i).isAirborne() ? RobotLevel.IN_AIR
						: RobotLevel.ON_GROUND;
			}
		}

		Direction toEnemy = null;
		if (!here.equals(enemy)) {
			toEnemy = here.directionTo(enemy);
		}

		if (!rc.isAttackActive() && (bestAttackPos != null)) {
			attack(bestAttackPos, bestAttackHeight);
			return true;
		}

		// try facing one of the unseen things, if it's in range
		if (!rc.isMovementActive() && (bestInRangePos != null)) {
			if (face(here.directionTo(bestInRangePos))) {
				return true;
			}
		}

		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// move toward enemy

		if (!rc.isMovementActive()) {
			if (superNear(parentArchon)) {
				if (toEnemy == null) {
					boolean good = true;
					Direction toFlux = rc.senseDirectionToUnownedFluxDeposit();
					MapLocation pos = here.add(toFlux);
					if (rc.canSenseSquare(pos)) {
						FluxDeposit f = rc.senseFluxDepositAtLocation(pos);
						if (f != null) {
							FluxDepositInfo fi = rc.senseFluxDepositInfo(f);
							if (fi.team == team) {
								good = false;
							}
						}
					}
				}

				if (toEnemy != null) {
					for (Direction d : new Direction[] { toEnemy,
							toEnemy.rotateLeft(), toEnemy.rotateRight() }) {
						if (rc.canMove(d)) {
							MapLocation there = here.add(d);
							if (superNear(there, parentArchon)) {
								move(there);
								return true;
							}
						}
					}
				}
			}
		}
		
		/*

		if (bestInRangePos != null) {
			// there are enemies about, if we can backup closer to the archon,
			// fine,
			// otherwize, stay put
			if (!rc.isMovementActive()) {
				if (!superNear(parentArchon)) {
					Direction toArchon = here.directionTo(parentArchon);
					if (dir.opposite() == toArchon) {
						if (rc.canMove(dir.opposite())) {
							rc.moveBackward();
							return true;
						}
					}
				}
			}
			Robot r = rc.senseAirRobotAtLocation(here);
			if (r != null) {
				RobotInfo ri = rc.senseRobotInfo(r);
				if (ri.team == team) {
					return true;
				}
			}
		}
*/
		return false;
	}
}
