package boat;

import battlecode.common.*;
import static battlecode.common.GameConstants.*;
import static battlecode.common.Direction.*;
import java.io.*;
import java.lang.*;
import java.math.*;
import java.util.*;

public class RobotPlayer implements Runnable {
    
    public final RobotController rc;
    public final Robot r;
    public final int id;
    public final RobotType type;
    public final int broadcastRadiusSquared;
        
    public final int myMagic = 0x729d8298;
    public final int mySeed = 0x27e72954;
    
    public RobotPlayer(RobotController rc) {
        this.rc = rc;
        r = rc.getRobot();
        id = r.getID();
        type = rc.getRobotType();
        broadcastRadiusSquared = type.broadcastRadius() * type.broadcastRadius();
    }
    
    // ========================================================================
    
    public int nextMessageId = 0;
    public Set<MapLocation> receivedMessaged = new HashSet<MapLocation>();
    
    public void sendMessage(String msg, int[] data) throws Exception {
        Message m = new Message();
        m.strings = new String[] {msg};
        m.ints = data;
        
        MapLocation loc = rc.getLocation();
        
        int hash1 = myMagic ^ id ^ loc.getY();
        int hash2 = mySeed ^ nextMessageId ^ loc.getX();
        
        m.locations = new MapLocation[] {
            new MapLocation(id, nextMessageId),
            loc,
            new MapLocation(hash1, hash2)            
        };
        
        nextMessageId++;
        
        rc.queueBroadcast(m);
    }
    
    public Message getMessage() throws Exception {
        while (true) {
            Message m = rc.getNextMessage();
            if (m == null) return null;
            if (m.strings != null && m.strings.length == 1 && m.locations != null && m.locations.length == 3) {
                MapLocation m1 = m.locations[0];
                if (m1 == null) continue;
                MapLocation m2 = m.locations[1];
                if (m2 == null) continue;
                MapLocation m3 = m.locations[2];
                if (m3 == null) continue;
                
                // make sure the hash checks out
                int hash1 = myMagic ^ m1.getX() ^ m2.getY();
                int hash2 = mySeed ^ m1.getY() ^ m2.getX();
                if (m3.getX() != hash1) continue;
                if (m3.getY() != hash2) continue;
                    
                // make sure we didn't already receive it                
                if (!receivedMessaged.add(m1)) continue;
                    
                // make sure we could have heard it
                if (rc.getLocation().distanceSquaredTo(m2) > broadcastRadiusSquared) continue;
                    
                // ok, I guess we'll believe it...
                return m;
            }
        }
    }
    
    public void debug(String s) throws Exception {
        debug(0, s);
    }
    
    public void debug(int index, String s) throws Exception {
        rc.setIndicatorString(index, s);
    }
    
    public int round() throws Exception {
        return Clock.getRoundNum();
    }
    
    public MapLocation add(MapLocation a, MapLocation b) {
    	return new MapLocation(a.getX() + b.getX(), a.getY() + b.getY());
    }
    
    public MapLocation sub(MapLocation a, MapLocation b) {
    	return new MapLocation(a.getX() - b.getX(), a.getY() - b.getY());
    }
    
//    public final double dir_const = 8.0 / (Math.PI * 2);
//    public final Direction[] dirArray = new Direction[] {WEST, SOUTH_WEST, SOUTH, SOUTH_EAST, EAST, NORTH_EAST, NORTH, NORTH_WEST, WEST, SOUTH_WEST, SOUTH, SOUTH_EAST, EAST};
//    public final double dir_const = 8.0 / (Math.PI * 2);
//    public final Direction[] dirArray = new Direction[] {EAST, NORTH_EAST, NORTH, NORTH_WEST, WEST, SOUTH_WEST, SOUTH, SOUTH_EAST, EAST};
//    public Direction dir(MapLocation from, MapLocation to) {
//    	return dirArray[4 + (int)Math.round(dir_const * Math.atan2(from.getY() - to.getY(), to.getX() - from.getX()))];
//    	int i = (int)Math.round(dir_const * Math.atan2(from.getY() - to.getY(), to.getX() - from.getX()));
//    	if (i > 0) return dirArray[i];
//    	return dirArray[8 + i];
//    }
    
    public static Direction dir(MapLocation from, MapLocation to) {
    	int dx = to.getX() - from.getX(); // 12
    	int dy = from.getY() - to.getY(); // 12
    	if (dx > 0) { // 2
    		if (dy > 0) { // 2
    			return NORTH_EAST; // 2
    		} else if (dy == 0) {
    			return EAST;
    		} else {
    			return SOUTH_EAST;
    		}    		
    	} else if (dx == 0) {
    		if (dy > 0) {
    			return NORTH;
    		} else if (dy == 0) {
    			return Direction.NORTH;
    		} else {
    			return SOUTH;
    		}
    	} else {
    		if (dy > 0) {
    			return NORTH_WEST;
    		} else if (dy == 0) {
    			return WEST;
    		} else {
    			return SOUTH_WEST;
    		}
    	}
    }
    
    public static Direction dir(int fromX, int fromY, int toX, int toY) {
    	int dx = toX - fromX; // 12
    	int dy = fromY - toY; // 12
    	if (dx > 0) { // 2
    		if (dy > 0) { // 2
    			return NORTH_EAST; // 2
    		} else if (dy == 0) {
    			return EAST;
    		} else {
    			return SOUTH_EAST;
    		}    		
    	} else if (dx == 0) {
    		if (dy > 0) {
    			return NORTH;
    		} else if (dy == 0) {
    			return Direction.NORTH;
    		} else {
    			return SOUTH;
    		}
    	} else {
    		if (dy > 0) {
    			return NORTH_WEST;
    		} else if (dy == 0) {
    			return WEST;
    		} else {
    			return SOUTH_WEST;
    		}
    	}
    }
    
    public int timer_startRound;
    public int timer_startByte;
    
    public void debug_startTimer() {
    	timer_startRound = Clock.getRoundNum();
    	timer_startByte = Clock.getBytecodeNum();
    }
    
    public int debug_endTimer() {
    	return debug_stopTimer();
    }
    
    public int debug_stopTimer() {
    	int endByte = Clock.getBytecodeNum();
    	int endRound = Clock.getRoundNum();
    	int bytes = (6000 - timer_startByte) +
    		(endRound - timer_startRound - 1) * 6000 + endByte - 3;
    	System.out.println("bytes = " + bytes);
    	return bytes;
    }
    
    public int distSq(int x1, int y1, int x2, int y2) {
    	int dx = x1 - x2;
    	int dy = y1 - y2;
    	return dx * dx + dy * dy;
    }
    
    // ========================================================================
    
    public class Dist {
    	public int dist;
    	public Direction dir;
    	
    	public Dist(int dist, Direction dir) {
    		this.dist = dist;
    		this.dir = dir;
    	}
    }

    public class Board {
    	MapLocation origin;
    	TerrainType[][] tiles = new TerrainType[79][79];
    	Dist[][] dists = new Dist[79][79];
    	
    	Map<Direction, Double> dirPoints = new HashMap<Direction, Double>();
    	
    	public Board(MapLocation center) {
    		origin = new MapLocation(center.getX() - 39, center.getY() - 39);
    		
//    		double a = 2;
//    		double b = 3;
//    		double c;
//    		
//    		Vector<Double> a = new Vector<Double>();
//    		for (int i = 0; i < 1000; i++) {
//    			a.add(1000.0 - i);
//    		}
//    		
//    		int beginTime = Clock.getBytecodeNum();
//    		
//    		Collections.sort(a);
//    		
//    		int endTime = Clock.getBytecodeNum();
//    		System.out.println("time = " + (endTime - beginTime - 2));
    	}
    	
    	public Direction getBestDir() {
    		Direction bestDir = Direction.NORTH;
    		double best = 0.0;
    		for (Direction d : dirPoints.keySet()) {
    			double points = dirPoints.get(d); 
    			if (points > best) {
    				best = points;
    				bestDir = d;
    			}
    		}
    		return bestDir;
    	}
    	
    	public void exploreSurroundings() {
//    		int beginTime = Clock.getBytecodeNum();
    		
//    		System.out.println();
    		MapLocation loc = rc.getLocation();
			for (int y = loc.getY() - type.sensorRadius(); y <= loc.getY() + type.sensorRadius(); y++) {
				for (int x = loc.getX() - type.sensorRadius(); x <= loc.getX() + type.sensorRadius(); x++) {
					TerrainType t = rc.senseTerrainType(new MapLocation(x, y));
    				tiles[x - origin.getX()][y - origin.getY()] = t;
//    				
//    				System.out.print(t == null ? "." : t == TerrainType.OFF_MAP ? "v" : t == TerrainType.WATER ? "#" : " ");
    			}
//				System.out.println();
    		}
			
//    		int endTime = Clock.getBytecodeNum();
//    		System.out.println("time = " + (endTime - beginTime));
    	}
    	
    	public void astar() throws Exception {
    		int beginTime = Clock.getRoundNum();
    		
    		dists = new Dist[79][79];
    		dirPoints = new HashMap<Direction, Double>();
    		boolean callOfNature = false;
    		
    		int orth = type.moveDelayOrthogonal();
    		int diag = type.moveDelayDiagonal();
    		
    		LinkedList<Pair<Integer, Pair<MapLocation, Direction>>> v = new LinkedList<Pair<Integer, Pair<MapLocation, Direction>>>();
    		MapLocation robotLoc = sub(rc.getLocation(), origin);
    		v.add(new Pair<Integer, Pair<MapLocation, Direction>>(
					0, new Pair<MapLocation, Direction>(robotLoc, null)));
    		
    		debug_startTimer();
    		
    		l1: while (v.size() > 0) {
//				Collections.sort((List)v);
    			Pair<Integer, Pair<MapLocation, Direction>> p = v.remove(0);
    			int dist = p.left;
    			if (dist > 6 * orth && callOfNature) {
    				break;
    			}
    			MapLocation loc = p.right.left;
    			int locX = loc.getX();
    			int locY = loc.getY();
    			Direction dir = p.right.right;
    			
    			if (dists[locX][locY] == null) {
    				dists[locX][locY] = new Dist(dist, dir);
	    			
	    			Direction cur = Direction.NORTH;
	    			do {
	    				MapLocation loc2 = loc.add(cur);
	        			TerrainType t = tiles[loc2.getX()][loc2.getY()];
	        			if (t == null && callOfNature == false) {
	        				callOfNature = true;
	        				dirPoints.put(dir, 10.0);
	        			}
	        			if (t != null && t == TerrainType.LAND && dists[loc2.getX()][loc2.getY()] == null) {
	        				Pair<Integer, Pair<MapLocation, Direction>> p2 = new Pair<Integer, Pair<MapLocation, Direction>>(
	        						dist + (cur.isDiagonal() ? diag : orth), new Pair<MapLocation, Direction>(
	        								loc2, dir != null ? dir : cur));
	        				
	        				v.add(p2);
	        			}
	    				
	    				cur = cur.rotateRight();
	    			} while (cur != Direction.NORTH);
    			}
    		}
    		
    		{
    			for (Upgrade u : rc.senseNearbyUpgrades()) {
    				MapLocation m = sub(rc.senseLocationOf(u), origin);
    				Dist d = dists[m.getX()][m.getY()];
    				if (d != null) {
    					Double points = dirPoints.get(d.dir);
    					if (points == null) {
    						points = 0.0;
    					}
    					dirPoints.put(d.dir, points + 100.0);
    				}
    			}
    		}
    		
    		debug_endTimer();
    		
    	}
    }
    
    // ========================================================================
    
    public static class Blah implements Comparable {
    	public double dist;
    	public double heuristic;
    	public MapLocation loc;
    	
    	public Blah(double dist, MapLocation loc, MapLocation dest) {
    		this.dist = dist;
    		this.heuristic = dist + Math.sqrt(loc.distanceSquaredTo(dest));
    		this.loc = loc;    		
    	}    	
    	
    	public int compareTo(Object o) {
    		Blah that = (Blah)o;
    		return new Double(heuristic).compareTo(that.heuristic);
    	}    	
    }
    
    public static class MyPoint {
    	int x;
    	int y;
    	
    	public int getX() {
    		return x;    		
    	}
    	
    	public int getY() {
    		return y;
    	}
    }
    
    public void astarTest() {
    	
    	System.out.println("got here!");
    	
    	Random rand = new Random();
    	
    	{
    		System.out.println("loc = " + rc.getLocation());
    		
    	}
    	
    	{
    		Random r = new Random(100);
    		
    		for (int i = 0; i < 15; i++) {
    			System.out.println("next = " + r.nextGaussian());
    		}
    	}
    	
    	{
    		HashSet<MapLocation> s1 = new HashSet<MapLocation>();
    		for (int i = 0; i < 100; i++) {
    			s1.add(new MapLocation(rand.nextInt(1000), rand.nextInt(1000)));
    		}
    		HashSet<MapLocation> s2 = new HashSet<MapLocation>();
    		for (int i = 0; i < 100; i++) {
    			s2.add(new MapLocation(rand.nextInt(1000), rand.nextInt(1000)));
    		}
    		
	    	debug_startTimer();
	    	
	    	s1.addAll(s2);
	    	
	    	debug_endTimer();
    	}
//    	{
//	    	Vector<Integer> v = new Vector<Integer>();
//	    	
//	    	for (int i = 0; i < 1000; i++) {
//	    		v.add(100 - i);
//	    	}
//	    	
//	    	System.out.println("doing test");
//	    	
//	    	debug_startTimer();
//	    	
//	    	Collections.sort(v);
//	    	Collections.sort(v);
//	    	
//	    	debug_endTimer();
//    	}
    	
//    	
//    	HashSet<MapLocation> blahS = new HashSet<MapLocation>(Arrays.asList(blah));
//    	
//    	int[] here = new int[2000];
//    	
//    	debug_startTimer();
//    	
//    	MapLocation[] here2 = blahS.toArray(new MapLocation[0]);
//    	
//    	debug_stopTimer();
//    	debug_startTimer();
//    	
//    	int i = 0;
//    	for (MapLocation lo : blahS) {
//    		here[i++] = lo.getX();
//    		here[i++] = lo.getY();
//    	}    	
//    	
//    	debug_stopTimer();
//    	
//    	System.out.println(here2[0]);
    	
    	
    	
    	
    	
    	
    	
//        String a = "########################################     #x           #+x          #     ## AA  #            #x           #   + ## AA  #            #            #  +  ##         #       ##        #         ##         #      ##         #         #####      #                 #      #####x     ####       # #       ####      ##      #+x         +         x+#      ##      #x         # #         x#      ##   ####                       ####   ##                     #               ##                     #               ##                    ##               ##      #      #########        #      ##     ##      ##               ##     ##    ##       #         ####    ##    ##x  ##        #         ##       ##  x##+x #  # #    #    x    #    # #  # x+######   +     #   x+x   #     +   ######+x #  # #    #    x    #    # #  # x+##x  ##       ##         #        ##  x##    ##    ####         #       ##    ##     ##               ##      ##     ##      #        #########      #      ##               ##                    ##               #                     ##               #                     ##   ####                       ####   ##      #x         # #         x#      ##      #+x         +         x+#      ##      ####       # #       ####     x#####      #                 #      #####         #         ##      #         ##         #        ##       #         ##  +  #            #            #  BB ## +   #           x#            #  BB ##     #          x+#           x#     ########################################";
//        
//        final int size = 39;
//		int orth = type.moveDelayOrthogonal();
//		int diag = type.moveDelayDiagonal();
//        
//        System.out.println();
//        boolean[][] map = new boolean[size][size];
//        {
//        	for (int y = 0; y < 39; y++) {
//        		for (int x = 0; x < 39; x++) {
//        			map[x][y] = (a.charAt(y * 39 + x) != '#');
//        			
//        			System.out.print(map[x][y] ? "." : "#");
//        		}
//        		System.out.println();
//        	}
//        }
//        
//        {
//        	MapLocation start = new MapLocation(1, 1);
//        	MapLocation end = new MapLocation(2, 0);
//        	
//        	int i = 1;
//        	int ii = 9;
//        	
//        	debug_startTimer();
//        	
//        	int d = start.distanceSquaredTo(end);
//        	
//        	debug_endTimer();
//        	System.out.println("dd = " + d);
//        }
//        
//        {
//        	MapLocation start = new MapLocation(1, 1);
//        	int endX = 18;
//        	int endY = 22;
//        	
//        	int[][] dists = new int[size][size];
//        	
//        	
//        	
//        	
//        	debug_startTimer();
        	
//        	int curX = start.getX();
//        	int curY = start.getY();
//        	Direction dir = dir(curX, curY, endX, endY);
//        	int bestDist = Integer.MAX_VALUE;        	
//        	while (curX != endX || curY != endY) {
//        		dists[curX][curY] = 1; // 14
//        		
//        		int dist = distSq(curX, curY, endX, endY); // 14
//        		if (dist < bestDist) { // 3
//        			bestDist = dist; // 2
//        			dir = dir(curX, curY, endX, endY); // 5 + 30 = 35
//        			
//        			while (true) {
//        				MapLocation loc2 = cur.add(dir);
//        				if (map[loc2.getX()][loc2.getY()]) {
//        					cur = loc2;
//        				} else {
//        					break;
//        				}
//        			}
//        		}
//        		
//        		while (true) { // 1
//        			int loc2X = curX + dirDx
//        			MapLocation loc2 = cur.add(dir); // 4 + 
//        			if (map[loc2.getX()][loc2.getY()]) {
//        				cur = loc2;
//        				dir = dir.isDiagonal() ? dir.rotateRight().rotateRight() : dir.rotateRight();
//        				break;
//        			} else {
//        				dir = dir.rotateLeft();
//        			}
//        		}
//        	}
        	
    		
//        	int time = debug_endTimer();
//        	
//    		System.out.println();
//    		int count = 0;
//        	for (int y = 0; y < 39; y++) {
//        		for (int x = 0; x < 39; x++) {
//        			
//        			if (dists[x][y] != 0) {
//        				System.out.print("v");
//        				count++;
//        			} else {
//            			System.out.print(map[x][y] ? "." : "#");
//        			}
//        		}
//        		System.out.println();
//        	}
//        	System.out.println("count = " + count + " : " + (time / count));
//        }
        
        
        
//        {
//        	MapLocation start = new MapLocation(1, 1);
//        	MapLocation end = new MapLocation(19, 19);
//        	
//        	MapLocation cur = start;
//        	double bestDist = start.distanceSquaredTo(end);        	
//        	while (!cur.equals(end)) {
//        		
//        	}
//        	
//	        double[][] dists = new double[size][size];
//	        Vector<Blah> open = new Vector<Blah>();
//	        open.add(new Blah(1, start, end));
//	        
//	        while (open.size() > 0) {
//	        	Collections.sort((List)open);
//	        	Blah b = open.remove(0);
//	        	if (dists[b.loc.getX()][b.loc.getY()] == 0) {
//	        		dists[b.loc.getX()][b.loc.getY()] = b.dist;
//	        		
//	        		if (b.loc.equals(end)) {
//	        			break;
//	        		}
//	        		
//	    			Direction cur = Direction.NORTH;
//	    			do {
//	    				MapLocation loc2 = b.loc.add(cur);
//	        			boolean t = map[loc2.getX()][loc2.getY()];
//	        			if (t && dists[loc2.getX()][loc2.getY()] == 0) {
//	        				open.add(new Blah(b.dist + (cur.isDiagonal() ? diag : orth), loc2, end));
//	        			}
//	    				cur = cur.rotateRight();
//	    			} while (cur != Direction.NORTH);
//	        	}
//	        	System.out.print(".");
//	        	System.out.flush();
//	        }
//    		System.out.println();
//        	for (int y = 0; y < 39; y++) {
//        		for (int x = 0; x < 39; x++) {
//        			
//        			if (dists[x][y] != 0) {
//        				System.out.print("v");
//        			} else {
//            			System.out.print(map[x][y] ? "." : "#");
//        			}
//        		}
//        		System.out.println();
//        	}
//        }   
        
        
        
        
        
        
        
        
//        {
//        	MapLocation start = new MapLocation(1, 1);
//        	MapLocation end = new MapLocation(19, 19);
//        	
//	        double[][] dists = new double[size][size];
//	        Vector<Blah> open = new Vector<Blah>();
//	        open.add(new Blah(1, start, end));
//	        
//	        while (open.size() > 0) {
//	        	Collections.sort((List)open);
//	        	Blah b = open.remove(0);
//	        	if (dists[b.loc.getX()][b.loc.getY()] == 0) {
//	        		dists[b.loc.getX()][b.loc.getY()] = b.dist;
//	        		
//	        		if (b.loc.equals(end)) {
//	        			break;
//	        		}
//	        		
//	    			Direction cur = Direction.NORTH;
//	    			do {
//	    				MapLocation loc2 = b.loc.add(cur);
//	        			boolean t = map[loc2.getX()][loc2.getY()];
//	        			if (t && dists[loc2.getX()][loc2.getY()] == 0) {
//	        				open.add(new Blah(b.dist + (cur.isDiagonal() ? diag : orth), loc2, end));
//	        			}
//	    				cur = cur.rotateRight();
//	    			} while (cur != Direction.NORTH);
//	        	}
//	        	System.out.print(".");
//	        	System.out.flush();
//	        }
//    		System.out.println();
//        	for (int y = 0; y < 39; y++) {
//        		for (int x = 0; x < 39; x++) {
//        			
//        			if (dists[x][y] != 0) {
//        				System.out.print("v");
//        			} else {
//            			System.out.print(map[x][y] ? "." : "#");
//        			}
//        		}
//        		System.out.println();
//        	}
//        }   
        
        
        
         
        
        
        
		
//		int[][] open = new int[size * size + 10][2];
//		int openBegin = 0;
//		int openEnd = 0;
//		int[][] closed = new int[size][size];
//		
//    	{
//        	rc.yield();
//			int beginTime = Clock.getRoundNum();
//	    	
//			open[openEnd][0] = 1;
//			open[openEnd][1] = 1;
//			closed[1][1] = 1;
//			openEnd++;
//			
//			for ( ; openBegin < openEnd; ) {
//				int xMin = open[openBegin][0] - 1;
//				int yMin = open[openBegin][1] - 1;
//				int xMax = xMin + 2;
//				int yMax = yMin + 2;
//				openBegin++;
//				
//				int dist = closed[xMin + 1][yMin + 1] + 1;
//				
//				for (int y = yMin; y <= yMax; y++) {
//					for (int x = xMin; x <= xMax; x++) {
//						if (closed[x][y] == 0 && map[x][y]) {
//							open[openEnd][0] = x;
//							open[openEnd][1] = y;
//							closed[x][y] = dist;
//							openEnd++;
//						}
//					}
//				}
//			}			
//	    	
//			int endTime = Clock.getRoundNum();
//			int endByte = Clock.getBytecodeNum();
//			System.out.println("rounds = " + (endTime - beginTime));
//			System.out.println("bytes = " + (((endTime - beginTime) * 6000) + endByte));
//    	}
    }    
    
    // ========================================================================
    
    public Board board;
    
    public void run() {
    	
    	// kill off all the archons except 1
    	try {
			if (rc.senseGroundRobotAtLocation(rc.getLocation().add(Direction.SOUTH_EAST)) != null) {
				board = new Board(rc.getLocation());
			} else {
				rc.suicide();
			}
		} catch (GameActionException e1) {
		}
    	
    	astarTest();
    	
    	rc.suicide();
    	
        while(true){
            try {
            	board.exploreSurroundings();
            	board.astar();
            	
            	Direction dir = board.getBestDir();
            	
            	debug(1, "dir = " + dir);
            	
            	rc.queueSetDirection(dir);
            	rc.yield();
            	rc.queueMoveForward();
            	rc.yield();
            } catch(Exception e) {
                System.out.println("caught exception:");
                e.printStackTrace();
            }
        }
    }
}

class Pair<T_left, T_right> implements Comparable {
	public T_left left;
	public T_right right;
	
	public Pair(T_left left, T_right right) {
		this.left = left;
		this.right = right;
	}
	
	public String toString() {
		return "(" + left + ", " + right + ")";
	}
	
	public boolean equals(Object o) {
		if (o instanceof Pair) {
			Pair<T_left, T_right> that = (Pair<T_left, T_right>)o;
			return
                ((left == null && that.left == null) || (left != null && left.equals(that.left))) &&
                ((right == null && that.right == null) || (right != null && right.equals(that.right)));
		}
		return false;
	}
	
	public int hashCode() {
		return left.hashCode() + right.hashCode();
	}
    
    public int compareTo(Object o) {
        Pair<T_left, T_right> that = (Pair)o;
        int i = 0;
        if (this.left instanceof Comparable) {
            int c = ((Comparable)this.left).compareTo(that.left);
            if (c == 0) {
                if (this.right instanceof Comparable) {
                    return ((Comparable)this.right).compareTo(that.right);
                } else {
                    return 0;
                }
            } else {
                return c;
            }
        } else {
            if (this.right instanceof Comparable) {
                return ((Comparable)this.right).compareTo(that.right);
            } else {
                return 0;
            }
        }
    }
}
