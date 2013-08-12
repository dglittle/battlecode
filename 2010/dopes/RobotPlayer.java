package dopes;

import battlecode.common.RobotController;

public class RobotPlayer implements Runnable {

	public RobotPlayer(RobotController rc) throws Exception {
		while (true) {
			rc.yield();
		}
	}
	
	public void run() {
	}
}
