package team022;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile.TerrainType;

public class Worker extends RobotPlayer {
	MapLocation workerFlux = null;
	int workerState = 0;
	int exploreCountdown = 0;
	Direction exploreDirection = null;

	Map<MapLocation, Integer> heights = new HashMap<MapLocation, Integer>();
	Set<MapLocation> blocks = new HashSet<MapLocation>();

	MapLocation plan_unloadThere;
	Vector<MapLocation> plan_carryThere;
	MapLocation plan_pickupThere;
	Vector<MapLocation> plan_goThere;

	public Worker(RobotController rc) {
		super(rc);
	}

	public boolean makePlans() throws Exception {
		plan_unloadThere = null;
		plan_carryThere = null;
		plan_pickupThere = null;
		plan_goThere = null;

		return planToUnloadThere(workerFlux, new HashSet<MapLocation>(), null);
	}

	public boolean myIsGoodSquare(MapLocation loc) throws Exception {
		if (rc.canSenseSquare(loc)) {
			return rc.senseTerrainTile(loc).isTraversableAtHeight(
					RobotLevel.ON_GROUND)
					&& ((loc.equals(here)) || (rc
							.senseGroundRobotAtLocation(loc) == null));
		} else {
			return heights.containsKey(loc);
		}
	}

	public int mySenseHeight(MapLocation loc) throws Exception {
		if (rc.canSenseSquare(loc))
			return rc.senseHeightOfLocation(loc);
		else
			return heights.get(loc);
	}

	public boolean mySenseBlocks(MapLocation loc) throws Exception {
		if (rc.canSenseSquare(loc))
			return (rc.senseNumBlocksAtLocation(loc) > 0);
		else
			return blocks.contains(loc);
	}

	public boolean planToUnloadThere(MapLocation there,
			Set<MapLocation> blackList, Direction tendThisWay) throws Exception {
		if (!myIsGoodSquare(there))
			return false;
		int height = mySenseHeight(there);
		blackList.add(there);

		if (tendThisWay == null) {
			double x = 0;
			double y = 0;
			int count = 0;
			for (MapLocation loc : rc.senseNearbyBlocks()) {
				if (workerFlux.distanceSquaredTo(loc) > 9) {
					x += loc.getX();
					y += loc.getY();
					count++;
				}
			}
			if (count > 0) {
				x = (int) ((x / count - workerFlux.getX()) * 100);
				y = (int) ((y / count - workerFlux.getY()) * 100);
				tendThisWay = workerFlux.directionTo(new MapLocation((int) x,
						(int) y));
				if (tendThisWay.ordinal() >= 8) {
					tendThisWay = randomDir();
				}
			} else {
				tendThisWay = randomDir();
			}
		}

		while (true) {
			MapLocation best = null;
			int bestScore = Integer.MIN_VALUE;
			boolean canUnloadThere = false;
			Direction dir = tendThisWay;
			do {
				MapLocation loc = there.add(dir);
				if (!blackList.contains(loc) && myIsGoodSquare(loc)) {
					int score = 0;
					int dHeight = mySenseHeight(loc) - height;
					if (dHeight <= -GameConstants.WORKER_MAX_HEIGHT_DELTA) {
						score = dHeight;
					} else {
						score = 1000 - dHeight;
						canUnloadThere = true;
					}
					if (score > bestScore) {
						bestScore = score;
						best = loc;
					}
				}
				dir = dir.rotateLeft();
			} while (dir != tendThisWay);
			if (best == null)
				return false;
			if (canUnloadThere) {
				if (planToCarryThere(best, new HashSet<MapLocation>(blackList))) {
					plan_unloadThere = there;
					return true;
				}
			} else {
				if (planToUnloadThere(best,
						new HashSet<MapLocation>(blackList), tendThisWay)) {
					return true;
				}
			}

			blackList.add(best);
		}
	}

	public boolean planToCarryThere(MapLocation there,
			Set<MapLocation> blackList) throws Exception {
		if (!myIsGoodSquare(there))
			return false;
		if (rc.getNumBlocks() > 0) {
			return planToGoThere(there);
		}

		Vector<MapLocation> squares = new Vector<MapLocation>();
		Vector<Integer> squareParents = new Vector<Integer>();
		PriorityQueue<Integer> queue = new PriorityQueue<Integer>();

		squares.add(there);
		squareParents.add(-1);
		queue.add((0 << 16) | (squares.size() - 1));
		blackList.add(there);

		while (queue.size() > 0) {
			int q = queue.poll();
			int cost = q >> 16;
			int squareIndex = q & 0x0000ffff;
			there = squares.get(squareIndex);
			int height = mySenseHeight(there);

			MapLocation best = null;
			int bestScore = Integer.MIN_VALUE;
			MapLocation[] adjacents = getAdjacent(there);
			for (MapLocation loc : adjacents) {
				if (blackList.contains(loc) || !myIsGoodSquare(loc))
					continue;
				int dHeight = mySenseHeight(loc) - height;
				boolean canPickup = (dHeight > -GameConstants.WORKER_MAX_HEIGHT_DELTA)
						&& mySenseBlocks(loc);
				if (canPickup) {
					int score = -Math.abs(dHeight);
					if (score > bestScore) {
						bestScore = score;
						best = loc;
					}
				} else {
					if (dHeight >= -GameConstants.WORKER_MAX_HEIGHT_DELTA) {
						blackList.add(loc);
						squares.add(loc);
						squareParents.add(squareIndex);
						queue.add(((cost + 3 + 4 * (dHeight * dHeight)) << 16)
								| (squares.size() - 1));
					}
				}
			}
			if (best != null) {
				if (planToGoThere(there)) {
					plan_pickupThere = best;
					plan_carryThere = new Vector<MapLocation>();
					int parentIndex = squareParents.get(squareIndex);
					while (parentIndex >= 0) {
						plan_carryThere.add(squares.get(parentIndex));
						parentIndex = squareParents.get(parentIndex);
					}
					return true;
				}
			}
		}
		return false;
	}

	public boolean planToGoThere(MapLocation there) throws Exception {
		if (!myIsGoodSquare(there))
			return false;

		boolean carrying = rc.getNumBlocks() > 0;

		Vector<MapLocation> squares = new Vector<MapLocation>();
		Vector<Integer> squareParents = new Vector<Integer>();
		Vector<Integer> costs = new Vector<Integer>();
		PriorityQueue<Integer> queue = new PriorityQueue<Integer>();
		Set<MapLocation> blackList = new HashSet<MapLocation>();

		squares.add(here);
		squareParents.add(-1);
		costs.add(0);
		queue.add((0 << 16) | (squares.size() - 1));
		blackList.add(here);

		while (queue.size() > 0) {
			int q = queue.poll();
			int i = q & 0x0000ffff;
			MapLocation step = squares.get(i);
			int cost = costs.get(i);

			if (step.equals(there)) {
				plan_goThere = new Vector<MapLocation>();
				while (i >= 1) {
					plan_goThere.add(squares.get(i));
					i = squareParents.get(i);
				}
				Collections.reverse(plan_goThere);
				if (carrying) {
					plan_carryThere = plan_goThere;
					plan_goThere = null;
				}
				return true;
			} else {
				int height = mySenseHeight(step);
				for (Direction d : dirs) {
					MapLocation loc = step.add(d);
					if (myIsGoodSquare(loc)) {
						int locHeight = mySenseHeight(loc);
						int dHeight = locHeight - height;
						if ((!carrying || (dHeight <= GameConstants.WORKER_MAX_HEIGHT_DELTA))
								&& blackList.add(loc)) {
							int newCost = cost + moveCost(height, locHeight, d);
							squares.add(loc);
							squareParents.add(i);
							costs.add(newCost);
							queue.add(((newCost + (int) Math.sqrt(loc
									.distanceSquaredTo(there))) << 16)
									| (squares.size() - 1));
						}
					}
				}
			}
		}
		return false;
	}

	public boolean loop() throws Exception {

		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// sense terrain

		if (justMoved) {
			for (int[] d : workerVision) {
				MapLocation loc = add(here, d[0], d[1]);
				if (!rc.senseTerrainTile(loc).isTraversableAtHeight(
						RobotLevel.ON_GROUND)) {
					continue;
				}
				int height = rc.senseHeightOfLocation(loc);
				heights.put(loc, height);
				if (rc.senseNumBlocksAtLocation(loc) > 0) {
					blocks.add(loc);
				} else {
					blocks.remove(loc);
				}
			}
		}

		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////
		// worker instinct

		// sense flux deposit we want to ship toward
		if (workerFlux == null) {
			workerFlux = senseClosestFluxDeposit();
		}
		if (workerFlux == null) {
			rc.suicide();
		}

		if (workerState == 0) {
			if (makePlans()) {
				workerState = 5;
			} else {
				workerState = 6;
				exploreCountdown = 15;
				exploreDirection = randomDir();
			}
		}

		if (workerState == 6) {
			if (exploreCountdown > 0) {
				exploreCountdown--;
				if (!rc.isMovementActive()) {
					tryMove(exploreDirection);
					return true;
				}
			} else {
				workerState = 0;
			}
		}

		if (rc.isMovementActive())
			return false;

		if (workerState == 5) {
			try {
				loop: while (true) {
					if (plan_goThere != null) {
						if (plan_goThere.size() > 0) {
							MapLocation next = plan_goThere.firstElement();
							if (here.equals(next)) {
								plan_goThere.remove(0);
								continue loop;
							} else {
								move(next);
								return true;
							}
						} else {
							plan_goThere = null;
							continue loop;
						}
					} else if (plan_pickupThere != null) {
						rc.loadBlockFromLocation(plan_pickupThere);
						plan_pickupThere = null;
						return true;
					} else if (plan_carryThere != null) {
						if (plan_carryThere.size() > 0) {
							MapLocation next = plan_carryThere.firstElement();
							if (here.equals(next)) {
								plan_carryThere.remove(0);
								continue loop;
							} else {
								move(next);
								return true;
							}
						} else {
							plan_carryThere = null;
							continue loop;
						}
					} else if (plan_unloadThere != null) {
						rc.unloadBlockToLocation(plan_unloadThere);
						plan_unloadThere = null;
						workerState = 4;
						return true;
					} else {
						workerState = 0;
						break;
					}
				}
			} catch (Exception e) {
				workerState = 0;
			}
		}

		if (workerState == 4) {
			// get energon
			if (energon > 0.7 * type.maxEnergon()) {
				workerState = 0;
			}
		}

		return false;
	}
}
