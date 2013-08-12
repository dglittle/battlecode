package cavemen;

import battlecode.common.*;
import static battlecode.common.GameConstants.*;

public class RobotPlayer implements Runnable {

	public static final int change_leader_if_they_have_been_still_this_long = 8;

	public static RobotController rc;

	public static int leaderIndex = 0;
	public static MapLocation lastLeaderLocation = null;
	public static int leaderAtLocationSinceRound = 0;

	public RobotPlayer(RobotController rc) {
		RobotPlayer.rc = rc;
		try {
			if (rc.getLocation().equals(rc.senseAlliedArchons()[0])) {
			} else {
				rc.suicide();
			}

			System.out.println("height: "
					+ rc.senseTerrainTile(rc.getLocation()).getHeight());
		} catch (Exception e) {
		}
	}

	public MapLocation getLeaderLocation() {
		MapLocation[] locs = rc.senseAlliedArchons();
		MapLocation loc = locs[leaderIndex];
		if (!loc.equals(lastLeaderLocation)) {
			leaderAtLocationSinceRound = Clock.getRoundNum();
		} else {
			if (Clock.getRoundNum() - leaderAtLocationSinceRound >= change_leader_if_they_have_been_still_this_long) {
				leaderIndex = (leaderIndex + 1) % locs.length;
				lastLeaderLocation = null;
				return getLeaderLocation();
			}
		}
		lastLeaderLocation = loc;
		return loc;
	}

	public void run() {
		while (true) {
			try {
				if (true) {
					rc.yield();
					continue;
				}
				
				MapLocation leader = getLeaderLocation();
				boolean amLeader = rc.getLocation().equals(leader);
				
				if (amLeader && !rc.isMovementActive()) {
					Direction dir = rc.senseEnemyArchon();
					if (rc.getDirection() != dir) {
						rc.setDirection(dir);
					} else {
						rc.moveForward();
					}
				}

				System.out.println("==================");

				System.out.println("round: " + Clock.getRoundNum());
				System.out.println("till active: "
						+ rc.getRoundsUntilMovementIdle());
				System.out.println("dir: " + rc.getDirection());

				if (!rc.isMovementActive()) {
					if (rc.getDirection() != Direction.NORTH) {
						rc.setDirection(Direction.NORTH);
					} else {
						rc.moveForward();
					}
				}

				System.out.println("round: " + Clock.getRoundNum());
				System.out.println("till active: "
						+ rc.getRoundsUntilMovementIdle());
				System.out.println("dir: " + rc.getDirection());

				if (Clock.getRoundNum() > 10) {
					rc.suicide();
				}
				rc.yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}
}
