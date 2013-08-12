package heavy1.blocks.pathfinding;

import heavy1.core.S;
import heavy1.util.Utilities;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.TerrainTile;

/**
 * Greg's bug code with Victor's modifications for inclusion in the generic pathfinding block.
 * 
 * TODO(pwnall): rename back to BugPathfindingBlock when it's certified to be working
 */
public class VBugPathfindingBlock {

	public static MapLocation _goal;
	public static int _bugging;
	public static int _bugDistToBeat;
	public static Direction _bugDir;
	public static MapLocation _lastBugLoc;

	public static final void setNavigationTarget(MapLocation target) {
    _bugging = 0;
    _goal = target;
	}
	
	public static final void reset() {
	  _bugging = 0;
	}
	
	public static final Direction nextDirection() {
		// see if we made it
		int dist = S.location.distanceSquaredTo(_goal);

		// if we're in bug mode,
		if (_bugging != 0) {
			// see if we're close enough to exit bug mode
			if (dist < _bugDistToBeat) {
				_bugging = 0;
			} else {
				// see if we moved since last time
				if (!S.location.equals(_lastBugLoc)) {
					// we did, so we need to "face the wall" again
					_bugDir = Utilities.rotate(_bugDir, _bugDir.isDiagonal() ? -3
							* _bugging : -2 * _bugging);
					_lastBugLoc = S.location;
				} else {
					// hm.. we haven't moved,
					// so keep trying to go the same way
					if (S.movementController.canMove(_bugDir))
						return _bugDir;

					// I guess we can't.. let's pop out of bug mode and try
					// again
					_bugging = 0;
				}
			}
		}

		// if we're not in bug mode,
		// return the best direction toward our goal,
		// or enter bug mode if we need to go around something
		if (_bugging == 0) {
			Direction d = S.location.directionTo(_goal);
			if (S.movementController.canMove(d))
				return d;

			Direction left = d.rotateLeft();
			if (S.movementController.canMove(left))
				return left;

			Direction right = d.rotateRight();
			if (S.movementController.canMove(right))
				return right;

			// we might be able to get closer by turning further left
			left = left.rotateLeft();
			if (S.location.add(left).distanceSquaredTo(_goal) < dist
					&& S.movementController.canMove(left))
				return left;

			// we might be able to get closer by turning further right
			right = right.rotateRight();
			if (S.location.add(right).distanceSquaredTo(_goal) < dist
					&& S.movementController.canMove(right))
				return right;

			// alas, we must go around whatever is in our way
			_bugging = Utilities.rdm.nextBoolean() ? 1 : -1;
			_bugDistToBeat = dist;
			_bugDir = d;
			_lastBugLoc = S.location;
		}

		// we must be in bug mode,
		// so let's bug
		for (int i = 0; i < 8; i++) {
			if (S.movementController.canMove(_bugDir)) {

				// if this is the first direction we're trying,
				// then tS.location should be a wall...
				if (i == 0) {
					// hm.. no wall.. wS.location did it go?
					// the "wall" was probably another robot that has gone,
					// so let's stop bugging
					_bugging = 0;
				}

				return _bugDir;
			} else {
        MapLocation checkOffMap = S.location.add(_bugDir, 2);
				// TODO: consider optimizing this using map data
				if (S.sensorController.canSenseSquare(checkOffMap)) {
					TerrainTile tt = S.rc.senseTerrainTile(checkOffMap);
					if (tt != null && tt == TerrainTile.OFF_MAP) {
						_bugging *= -1;
					}
				}
			}
			_bugDir = Utilities.rotate(_bugDir, _bugging);
		}

		// hm.. we tries all directions, and we're stuck,
		// we could throw an exception,
		// or pretend that we made arrived
		return null;
	}
}
