package sprint2.blocks;

import sprint2.core.S;
import sprint2.util.Utilities;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.MovementController;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class BugPathfinding {

	public MapLocation goal;
	public int bugging;
	public int bugDistToBeat;
	public Direction bugDir;
	public MapLocation lastBugLoc;

	public Direction bugTo(RobotController rc, MovementController mc, MapLocation goal) {
		MapLocation here = rc.getLocation();

		// see if we made it
		int dist = here.distanceSquaredTo(goal);
		//Log.out("The distance from here (" + here.toString() + ") to the goal (" + goal.toString() + ") is " + dist + ".");
		if (dist == 0) {
			return Direction.NONE;
		}

		// update our goal,
		// and reset our state if we have a new goal
		if (this.goal == null || !this.goal.equals(goal)) {
			bugging = 0;
			this.goal = goal;
		}

		// if we're in bug mode,
		if (bugging != 0) {
			// see if we're close enough to exit bug mode
			if (dist < bugDistToBeat) {
				bugging = 0;
			} else {
				// see if we moved since last time
				if (!here.equals(lastBugLoc)) {
					// we did, so we need to "face the wall" again
					bugDir = Utilities.rotate(bugDir, bugDir.isDiagonal() ? -3
							* bugging : -2 * bugging);
					lastBugLoc = here;
				} else {
					// hm.. we haven't moved,
					// so keep trying to go the same way
					if (mc.canMove(bugDir))
						return bugDir;

					// I guess we can't.. let's pop out of bug mode and try
					// again
					bugging = 0;
				}
			}
		}

		// if we're not in bug mode,
		// return the best direction toward our goal,
		// or enter bug mode if we need to go around something
		if (bugging == 0) {
			Direction d = here.directionTo(goal);
			if (mc.canMove(d))
				return d;

			Direction left = d.rotateLeft();
			if (mc.canMove(left))
				return left;

			Direction right = d.rotateRight();
			if (mc.canMove(right))
				return right;

			// we might be able to get closer by turning further left
			left = left.rotateLeft();
			if (here.add(left).distanceSquaredTo(goal) < dist
					&& mc.canMove(left))
				return left;

			// we might be able to get closer by turning further right
			right = right.rotateRight();
			if (here.add(right).distanceSquaredTo(goal) < dist
					&& mc.canMove(right))
				return right;

			// alas, we must go around whatever is in our way
			bugging = Utilities.rdm.nextBoolean() ? 1 : -1;
			bugDistToBeat = dist;
			bugDir = d;
			lastBugLoc = here;
		}

		// we must be in bug mode,
		// so let's bug
		for (int i = 0; i < 8; i++) {
			if (mc.canMove(bugDir)) {

				// if this is the first direction we're trying,
				// then there should be a wall...
				if (i == 0) {
					// hm.. no wall.. where did it go?
					// the "wall" was probably another robot that has gone,
					// so let's stop bugging
					bugging = 0;
				}

				return bugDir;
			} else {
				MapLocation checkOffMap = Utilities.add(here, bugDir, 2);
				if (S.sensorController.canSenseSquare(checkOffMap)) {
					TerrainTile tt = rc.senseTerrainTile(checkOffMap);
					if (tt != null && tt == TerrainTile.OFF_MAP) {
						bugging = 0;
						return Direction.OMNI;
					}
				}
			}
			bugDir = Utilities.rotate(bugDir, bugging);
		}

		// hm.. we tries all directions, and we're stuck,
		// we could throw an exception,
		// or pretend that we made arrived
		return Direction.OMNI;
	}
}
