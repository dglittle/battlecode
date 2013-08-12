package farmers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class Test {
	public static void main(String[] args) {

		int xMin = -30;
		int yMin = -30;
		int xMax = 30;
		int yMax = 30;

		MapLocation[] a = new MapLocation[] { new MapLocation(0, 0), new MapLocation(0, 0)};
//		MapLocation[] a = new MapLocation[] { new MapLocation(11, 5),
//				new MapLocation(3, 0), new MapLocation(6, 0),
//				new MapLocation(0, 3), new MapLocation(10, 5), };

		if (true) {
			double biggestArea = -1;
			MapLocation nt = null;
			Direction td = null;

			boolean[] done = new boolean[a.length];
			for (int oi = 0; oi < a.length; oi++) {
				if (done[oi])
					continue;
				done[oi] = true;
				Queue<Integer> q = new LinkedList();
				q.add(oi);

				int x = a[oi].getX();
				int y = a[oi].getY();

				int leftL = x;
				int rightL = x;
				int topL = y;
				int bottomL = y;
				int left = oi;
				int right = oi;
				int top = oi;
				int bottom = oi;

				int ur = x - y;
				int ul = -x - y;

				int neL = ur;
				int nwL = ul;
				int seL = ul;
				int swL = ur;
				int ne = oi;
				int nw = oi;
				int se = oi;
				int sw = oi;

				while (q.size() > 0) {
					int i = q.poll();
					MapLocation aa = a[i];

					x = aa.getX();
					y = aa.getY();

					if (x < leftL) {
						leftL = x;
						left = i;
					}
					if (x > rightL) {
						rightL = x;
						right = i;
					}
					if (y < topL) {
						topL = y;
						top = i;
					}
					if (y > bottomL) {
						bottomL = y;
						bottom = i;
					}

					ur = x - y;
					ul = -x - y;

					if (ur > neL) {
						neL = ur;
						ne = i;
					}
					if (ur < swL) {
						swL = ur;
						sw = i;
					}
					if (ul > nwL) {
						nwL = ul;
						nw = i;
					}
					if (ul < seL) {
						seL = ul;
						se = i;
					}

					for (int ii = 0; ii < a.length; ii++) {
						if (done[ii])
							continue;
						if (aa.distanceSquaredTo(a[ii]) <= 25) {
							done[ii] = true;
							q.add(ii);
						}
					}
				}

				double area = (rightL - leftL) * (bottomL - topL);
				double otherArea = 0.5 * (neL - swL) * (nwL - seL);
				if (area > biggestArea || otherArea > biggestArea) {
					boolean found = false;
					while (true) {
						if (otherArea < area) {
							// grow diagonally
							if (neL - swL < nwL - seL) {
								MapLocation g = a[ne];
								if (g.getX() + 4 <= xMax
										&& g.getY() - 4 >= yMin) {
									nt = g;
									td = Direction.NORTH_EAST;
									found = true;
									break;
								}
								g = a[sw];
								if (g.getX() - 4 >= xMin
										&& g.getY() + 4 <= yMax) {
									nt = g;
									td = Direction.SOUTH_WEST;
									found = true;
									break;
								}
							} else {
								MapLocation g = a[nw];
								if (g.getX() - 4 >= xMin
										&& g.getY() - 4 >= yMin) {
									nt = g;
									td = Direction.NORTH_WEST;
									found = true;
									break;
								}
								g = a[se];
								if (g.getX() + 4 <= xMax
										&& g.getY() + 4 <= yMax) {
									nt = g;
									td = Direction.SOUTH_EAST;
									found = true;
									break;
								}
							}
						} else {
							// grow orthogonally
							if (rightL - leftL < bottomL - topL) {
								MapLocation g = a[right];
								if (g.getX() + 5 <= xMax) {
									nt = g;
									td = Direction.EAST;
									found = true;
									break;
								}
								g = a[left];
								if (g.getX() - 5 >= xMin) {
									nt = g;
									td = Direction.WEST;
									found = true;
									break;
								}
							} else {
								MapLocation g = a[bottom];
								if (g.getY() + 5 <= yMax) {
									nt = g;
									td = Direction.SOUTH;
									found = true;
									break;
								}
								g = a[top];
								if (g.getY() - 5 >= yMin) {
									nt = g;
									td = Direction.NORTH;
									found = true;
									break;
								}
							}
						}
						break;
					}
					if (found) {
						biggestArea = Math.max(area, otherArea);
					}
				}
			}

		}

		if (false) {
			MapLocation[] b = new MapLocation[a.length];
			Map<MapLocation, ArrayList<Integer>> m = new HashMap();
			for (int i = 0; i < a.length; i++) {
				MapLocation aa = a[i];
				MapLocation bb = new MapLocation(aa.getX() / 5, aa.getY() / 5);
				b[i] = bb;
				ArrayList<Integer> al = m.get(bb);
				if (al == null) {
					al = new ArrayList<Integer>();
					m.put(bb, al);
				}
				al.add(i);
			}

			int biggestArea = 0;
			int biggestLeft = 0;
			int biggestRight = 0;
			int biggestTop = 0;
			int biggestBottom = 0;

			boolean[] done = new boolean[a.length];
			for (int oi = 0; oi < a.length; oi++) {
				if (done[oi])
					continue;
				done[oi] = true;
				Queue<Integer> q = new LinkedList();
				q.add(oi);

				int leftL = a[oi].getX();
				int rightL = a[oi].getX();
				int topL = a[oi].getY();
				int bottomL = a[oi].getY();
				int left = oi;
				int right = oi;
				int top = oi;
				int bottom = oi;

				while (q.size() > 0) {
					int i = q.poll();
					MapLocation aa = a[i];

					if (aa.getX() < leftL) {
						leftL = aa.getX();
						left = i;
					}
					if (aa.getX() > rightL) {
						rightL = aa.getX();
						right = i;
					}
					if (aa.getY() < topL) {
						topL = aa.getY();
						top = i;
					}
					if (aa.getY() > bottomL) {
						bottomL = aa.getY();
						bottom = i;
					}

					MapLocation bb = b[i];
					for (int x = -1; x <= 1; x++) {
						for (int y = -1; y <= 1; y++) {
							ArrayList<Integer> al = m.get(RobotPlayer.add(bb,
									x, y));
							if (al != null) {
								for (int ii : al) {
									if (done[ii])
										continue;
									if (aa.distanceSquaredTo(a[ii]) <= 25) {
										done[ii] = true;
										q.add(ii);
									}
								}
							}
						}
					}
				}

				int area = (rightL - leftL) * (bottomL - topL);
				if (area > biggestArea) {
					biggestArea = area;
					biggestLeft = left;
					biggestRight = right;
					biggestTop = top;
					biggestBottom = bottom;
				}
			}
		}

	}
}
