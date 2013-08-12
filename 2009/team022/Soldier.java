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

public class Soldier extends RobotPlayer {
	public Soldier(RobotController rc) {
		super(rc);
	}

	public boolean loop() throws Exception {
		
		for (FluxDeposit f : rc.senseNearbyFluxDeposits()) {
			FluxDepositInfo fi = rc.senseFluxDepositInfo(f);
			if (superNear(fi.location) && (fi.team == team)) {
				if (rc.getEnergonLevel() > RobotType.CANNON.spawnCost() / 2) {
					rc.transform(RobotType.CANNON);
					return true;
				}
			}
		}		

		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// shoot stuff

		MapLocation bestPos = null;
		RobotLevel bestHeight = null;
		int closest = Integer.MAX_VALUE;

		for (RobotInfo ri : enemyAirRobotInfos) {
			int dist = here.distanceSquaredTo(ri.location);
			if (rc.canAttackSquare(ri.location)) {
				dist -= 10;
				if (ri.type == RobotType.ARCHON) {
					dist -= 10000;
				}
			}
			if (dist < closest) {
				closest = dist;
				bestPos = ri.location;
				bestHeight = RobotLevel.IN_AIR;
			}
		}
		for (RobotInfo ri : enemyGroundRobotInfos) {
			int dist = here.distanceSquaredTo(ri.location);
			if (rc.canAttackSquare(ri.location)) {
				dist -= 10;
			}
			if (dist < closest) {
				closest = dist;
				bestPos = ri.location;
				bestHeight = RobotLevel.ON_GROUND;
			}
		}
		for (int i = 0; i < unseenEnemyLocations.size(); i++) {
			MapLocation pos = unseenEnemyLocations.get(i);
			int dist = here.distanceSquaredTo(pos);
			if (rc.canAttackSquare(pos)) {
				dist -= 10;
			}
			if (dist < closest) {
				closest = dist;
				bestPos = pos;
				bestHeight = unseenEnemyTypes.get(i).isAirborne() ? RobotLevel.IN_AIR
						: RobotLevel.ON_GROUND;
			}
		}

		// Direction bestDir = rc.senseDirectionToUnownedFluxDeposit();
		// if (bestPos != null) {
		// bestDir = here.directionTo(bestPos);
		// }

		if (bestPos != null) {
			if (rc.canAttackSquare(bestPos)) {
				if (!rc.isAttackActive()) {
					attack(bestPos, bestHeight);
				}
			} else if (!rc.isMovementActive()) {
				if (inRange(bestPos)) {
					rc.setDirection(here.directionTo(bestPos));
				} else {
					tryMove(bestPos);
				}
			}
			return true;
		}

		return false;
	}
}
