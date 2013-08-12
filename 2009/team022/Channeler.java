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

public class Channeler extends RobotPlayer {

	public Channeler(RobotController rc) {
		super(rc);
	}

	public boolean loop() throws Exception {
		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// shoot stuff

		if (!rc.isAttackActive()) {
			boolean inRange = false;
			loop: while (true) {
				for (MapLocation pos : unseenEnemyLocations) {
					if (inRange(pos)) {
						inRange = true;
						break loop;
					}
				}
				break loop;
			}

			// special case : if we are far from a archon, then we may as well
			// drain
			MapLocation archon = closestArchon();
			if ((archon == null) || !superNear(closestArchon()))
				inRange = true;

			if (inRange) {
				rc.drain();
				return true;
			}
		}
		
		return false;
	}
}
