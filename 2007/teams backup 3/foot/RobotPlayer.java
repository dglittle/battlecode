package foot;

import static battlecode.common.Direction.EAST;
import static battlecode.common.Direction.NONE;
import static battlecode.common.Direction.NORTH;
import static battlecode.common.Direction.NORTH_EAST;
import static battlecode.common.Direction.NORTH_WEST;
import static battlecode.common.Direction.OMNI;
import static battlecode.common.Direction.SOUTH;
import static battlecode.common.Direction.SOUTH_EAST;
import static battlecode.common.Direction.SOUTH_WEST;
import static battlecode.common.Direction.WEST;
import static battlecode.common.MapHeight.IN_AIR;
import static battlecode.common.MapHeight.ON_GROUND;
import static battlecode.common.RobotType.ARCHON;
import static battlecode.common.RobotType.EMP;
import static battlecode.common.RobotType.SCOUT;
import static battlecode.common.RobotType.SOLDIER;
import static battlecode.common.RobotType.TANK;
import static battlecode.common.TerrainType.LAND;
import static battlecode.common.TerrainType.OFF_MAP;
import static battlecode.common.TerrainType.WATER;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.Collections;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapHeight;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainType;

public class RobotPlayer implements Runnable {
    
    public static RobotController rc;
    public static Robot r;
    public static int id;
    public static RobotType type;
    public static int broadcastRadiusSquared;
    public static Team team;
    public static boolean inAir;
    public static final Random rand = new Random();
        
    public static final int myMagic = 0x729d8298;
    public static final int mySeed = 0x27e72954;
    public static MapLocation loc;
    public static int locX;
    public static int locY;

    public static int orth;
    public static int diag;
    
    public static MapLocation mapOrigin;
    public static int mapOriginX;
    public static int mapOriginY;
    public static final int mapXSize = 120;
    public static final int mapYSize = 120;
    // in relative coordinates
    public static int mapLeft;
    public static int mapRight;
    public static int mapTop;
    public static int mapBottom;
    public static final boolean[][] map = new boolean[mapXSize][mapYSize];
    public static Dist[][] distMap = new Dist[mapXSize][mapYSize];
    // in global coordinates
    public static Set<MapLocation> wallSet = new HashSet<MapLocation>();
    public static MapLocation[] wallArray = null;
    public static MapLocation[] dummyWallArray = new MapLocation[0];
    
    public RobotPlayer(RobotController _rc) {
        rc = _rc;
        r = rc.getRobot();
        id = r.getID();
        type = rc.getRobotType();
        team = r.getTeam();
        inAir = type.isAirborne();
        broadcastRadiusSquared = type.broadcastRadius() * type.broadcastRadius();
        mapOrigin = sub(rc.getLocation(), new MapLocation(mapXSize / 2, mapYSize / 2));
        mapOriginX = mapOrigin.getX();
        mapOriginY = mapOrigin.getY();
        updateLocation();
        mapLeft = locX - 38 - mapOriginX;
        mapRight = locX + 38 - mapOriginX;
        mapTop = locY - 38 - mapOriginY;
        mapBottom = locY + 38 - mapOriginY;
        
		orth = type.moveDelayOrthogonal();
		diag = type.moveDelayDiagonal();        
    }
    
    // ========================================================================
    
    public static int nextMessageId = 0;
    public static Set<MapLocation> receivedMessaged = new HashSet<MapLocation>();
    
    public static boolean updateLocation() {
    	MapLocation newLoc = rc.getLocation();
    	if (!newLoc.equals(loc)) {
			loc = rc.getLocation();
			locX = loc.getX();
			locY = loc.getY();
			return true;
    	}
    	return false;
    }
    
    public static boolean tryMove() throws Exception {
    	while (rc.isActive() || rc.getActionQueueSize() > 0) {
    		rc.yield();
    		processMessages();
    	}
    	rc.queueMoveForward();
		rc.getAllMessages();
    	rc.yield();
    	return updateLocation();
    }
    
    public static void sendWalls() throws Exception {
    	if (wallArray == null) {
    		wallArray = wallSet.toArray(dummyWallArray);
    	}
    	sendMessage("walls", wallArray,
    			mapLeft + mapOriginX,
    			mapRight + mapOriginX,
    			mapTop + mapOriginY,
    			mapBottom + mapOriginY);
    }
    
    public static boolean receiveWalls(Message m) {
    	if (m.strings[0].equals("walls")) {
    		mapLeft = Math.max(mapLeft, m.ints[0] - mapOriginX);
    		mapRight = Math.min(mapRight, m.ints[1] - mapOriginX);
    		mapTop = Math.max(mapTop, m.ints[2] - mapOriginY);
    		mapBottom = Math.min(mapBottom, m.ints[3] - mapOriginY);
    		
    		HashSet<MapLocation> newWalls = new HashSet<MapLocation>(Arrays.asList(m.locations));
    		newWalls.removeAll(wallSet);
    		wallSet.addAll(newWalls);
    		wallArray = null;
    		
    		for (MapLocation wall : newWalls) {
    			map[wall.getX() - mapOriginX][wall.getY() - mapOriginY] = true;
    		}
    		
    		return true;
    	}
    	return false;
    }
    
    public static void sendMessage(String msg, MapLocation[] locs, int ... ints) throws Exception {
        Message m = new Message();
        m.strings = new String[] {msg};
        m.ints = new int[ints.length + 5];
        System.arraycopy(ints, 0, m.ints, 0, ints.length);
        m.locations = locs;
        
        int hash = myMagic ^ id ^ nextMessageId ^ locX ^ locY;
        
        int i = ints.length;
        m.ints[i++] = id;
        m.ints[i++] = nextMessageId;
        m.ints[i++] = locX;
        m.ints[i++] = locY;
        m.ints[i++] = hash;
        
        nextMessageId++;
        
        rc.queueBroadcast(m);
    }
    
    public static Message getMessage() throws Exception {
        while (true) {
            Message m = rc.getNextMessage();
            if (m == null) return null;
            if (m.strings != null && m.strings.length == 1 &&
            		m.ints != null && m.ints.length >= 5) {
            	
            	int[] ints = m.ints;
            	int i = ints.length - 5;
            	MapLocation messageId = new MapLocation(ints[i++], ints[i++]);
            	MapLocation senderLoc = new MapLocation(ints[i++], ints[i++]);
            	int hash = ints[i++];
            	
                // make sure the hash checks out
            	if (hash != (myMagic ^ 
            			messageId.getX() ^ messageId.getY() ^ 
            			senderLoc.getX() ^ senderLoc.getY())) continue;
                    
                // make sure we didn't already receive it                
                if (!receivedMessaged.add(messageId)) continue;
                    
                // make sure we could have heard it
                if (rc.getLocation().distanceSquaredTo(senderLoc) > broadcastRadiusSquared) continue;
                    
                // ok, I guess we'll believe it...
                return m;
            }
        }
    }
    
    public static void debug(String s) throws Exception {
        debug(0, s);
    }
    
    public static void debug(int index, String s) throws Exception {
        rc.setIndicatorString(index, s);
    }
    
    public static int round() throws Exception {
        return Clock.getRoundNum();
    }
    
    public static MapLocation add(MapLocation a, MapLocation b) {
    	return new MapLocation(a.getX() + b.getX(), a.getY() + b.getY());
    }
    
    public static MapLocation sub(MapLocation a, MapLocation b) {
    	return new MapLocation(a.getX() - b.getX(), a.getY() - b.getY());
    }
    
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
    			return OMNI;
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
    
    public static Direction dir(MapLocation to) {
    	return dir(rc.getLocation(), to);
    }
    
    public static int timer_startRound;
    public static int timer_startByte;
    
    public static void debug_startTimer() {
    	timer_startRound = Clock.getRoundNum();
    	timer_startByte = Clock.getBytecodeNum();
    }
    
    public static int debug_stopTimer() {
    	int endByte = Clock.getBytecodeNum();
    	int endRound = Clock.getRoundNum();
    	int bytes = (6000 - timer_startByte) +
    		(endRound - timer_startRound - 1) * 6000 + endByte - 3;
    	System.out.println("bytes = " + bytes);
    	return bytes;
    }
    
    public static MapLocation getFront() {
    	return rc.getLocation().add(rc.getDirection());
    }
    
    public static Robot robotInFront(boolean inAir) throws Exception {
    	if (inAir) {
    		return rc.senseAirRobotAtLocation(getFront());
    	} else {
    		return rc.senseGroundRobotAtLocation(getFront());
    	}
    }
    
    public static boolean empty(boolean inAir, MapLocation loc) throws Exception {
    	TerrainType t = rc.senseTerrainType(loc);
    	if (t == OFF_MAP) return false;
    	if (inAir) {
    		return rc.senseAirRobotAtLocation(loc) == null;
    	} else if (t == LAND) {
    		return rc.senseGroundRobotAtLocation(loc) == null;
    	}
    	return false;
    }
    
    public final Direction[] dirArray = new Direction[] {
    		EAST, NORTH_EAST, NORTH, NORTH_WEST, WEST, SOUTH_WEST, SOUTH, SOUTH_EAST};
    
    public static boolean trySpawnRobot(RobotType t) throws Exception {
    	boolean inAir = t.isAirborne();
		Direction currentDir = rc.getDirection();
		MapLocation loc = rc.getLocation();
    	if (empty(inAir, loc.add(currentDir))) {
    		rc.queueSpawn(t);
    		return true;
    	} else {
    		Direction d = currentDir.rotateRight();
    		do {
				if (empty(inAir, loc.add(d))) {
					rc.queueSetDirection(d);
		    		rc.queueSpawn(t);
					return true;
				}
    			d = d.rotateRight();
    		} while (d != currentDir);
    	}
    	return false;
    }
    
    public static final MapLocation[] neighborLocs = new MapLocation[16];
    public static final MapHeight[] neighborHeights = new MapHeight[16];
    public static final double[] neighborCapacitys = new double[16];
    
    public static void tryArchonHealEveryone(double keepThisMuch) throws Exception {
    	try {
	    	double energy = rc.getEnergonLevel();
	    	if (energy < keepThisMuch) return;
	    	
	    	MapLocation loc = rc.getLocation();
	    	int nextIndex = 0;
	    	double desired = 0;
	    	for (Robot r : rc.senseNearbyAirRobots()) {
				RobotType t = rc.senseRobotTypeOf(r);
				MapLocation loc2 = rc.senseLocationOf(r);
	    		if (loc.isAdjacentTo(loc2) && r.getTeam() == team) {
	    			double capacity = t.maxEnergon() - rc.senseEventualEnergonOf(r);
	    			if (capacity > 0.001) {
		    			desired += capacity;
		    			
		    			neighborLocs[nextIndex] = loc2;
		    			neighborCapacitys[nextIndex] = capacity;
		    			neighborHeights[nextIndex] = IN_AIR;
		    			nextIndex++;
	    			}
	    		}
	    	}
	    	for (Robot r : rc.senseNearbyGroundRobots()) {
				RobotType t = rc.senseRobotTypeOf(r);
				MapLocation loc2 = rc.senseLocationOf(r);
	    		if (loc.isAdjacentTo(loc2) && r.getTeam() == team && t != ARCHON) {
	    			double capacity = t.maxEnergon() - rc.senseEventualEnergonOf(r);
	    			if (capacity > 0.001) {
		    			desired += capacity;
		    			
		    			neighborLocs[nextIndex] = loc2;
		    			neighborCapacitys[nextIndex] = capacity;
		    			neighborHeights[nextIndex] = ON_GROUND;
		    			nextIndex++;
	    			}
	    		}
	    	}
	    	
	    	double fraction = Math.min(1, (energy - keepThisMuch) / desired);  
	    	
			for (int i = 0; i < nextIndex; i++) {
				rc.transferEnergon(neighborCapacitys[i] * fraction, neighborLocs[i], neighborHeights[i]);
			}
    	} catch (Exception e) {
    	}
    }
    
    public static boolean tryMove(Direction dir) {
    	try {
    		if (dir == NONE || dir == OMNI) return true;
    		MapLocation loc = rc.getLocation();
    		Direction myDir = rc.getDirection();
    		
    		if (empty(inAir, loc.add(dir))) {
        		if (myDir != dir) {
        			rc.queueSetDirection(dir);
        		}
        		return tryMove();
    		} else if (empty(inAir, loc.add(dir.rotateRight()))) {
    			dir = dir.rotateRight();
        		if (myDir != dir) {
        			rc.queueSetDirection(dir);
        		}
        		return tryMove();
    		} else if (empty(inAir, loc.add(dir.rotateLeft()))) {
    			dir = dir.rotateLeft();
        		if (myDir != dir) {
        			rc.queueSetDirection(dir);
        		}
        		return tryMove();
    		} else {
    			return false;
    		}
    	} catch (Exception e) {
    		return false;
    	}
    }
    
    public static MapLocation nearestArchonLocation() {
    	MapLocation bestLoc = null;
    	int bestDist = Integer.MAX_VALUE;
    	for (MapLocation loc2 : rc.senseAlliedArchons()) {
    		int dist = loc.distanceSquaredTo(loc2);
    		if (dist < bestDist) {
    			bestDist = dist;
    			bestLoc = loc2;
    		}
    	}
    	return bestLoc;
    }
    
    public Direction dirToMyNearestArchon() {
    	return dir(nearestArchonLocation());
    }
    
    public static void updateMapBorder(int offMapX, int offMapY) {
		if 		(map[offMapX + 1][offMapY])	mapLeft = offMapX + 2;
		else if (map[offMapX - 1][offMapY])	mapRight = offMapX - 2;
		else if (map[offMapX][offMapY + 1]) mapTop = offMapY + 2;
		else if (map[offMapX][offMapY - 1])	mapBottom = offMapY - 2;
    }
    
    public static void personalExplore() {
		int radius = type.sensorRadius();
		
		MapLocation loc = rc.getLocation();
		int realX = loc.getX();
		int realY = loc.getY();
		int lowY = Math.max(mapTop + mapOriginY, realY - radius);
		int highY = Math.min(mapBottom + mapOriginY, realY + radius);
		int lowX = Math.max(mapLeft + mapOriginX, realX - radius);
		int highX = Math.min(mapRight + mapOriginX, realX + radius);
		
		for (int y = lowY; y <= highY; y++) {
			for (int x = lowX; x <= highX; x++) {
				MapLocation loc2 = new MapLocation(x, y);
				if (rc.canSenseSquare(loc2)) {
					TerrainType t = rc.senseTerrainType(loc2);
					map[x - mapOriginX][y - mapOriginY] = t == WATER;
					if (t == OFF_MAP) {
						updateMapBorder(x - mapOriginX, y - mapOriginY);
					}
				}
			}
		}
    }
    
    public static void exploreForOthers() {
		int radius = type.sensorRadius();
		
		MapLocation loc = rc.getLocation();
		int realX = loc.getX();
		int realY = loc.getY();
		int lowY = Math.max(mapTop + mapOriginY, realY - radius);
		int highY = Math.min(mapBottom + mapOriginY, realY + radius);
		int lowX = Math.max(mapLeft + mapOriginX, realX - radius);
		int highX = Math.min(mapRight + mapOriginX, realX + radius);
		
		for (int y = lowY; y <= highY; y++) {
			for (int x = lowX; x <= highX; x++) {
				MapLocation loc2 = new MapLocation(x, y);
				if (rc.canSenseSquare(loc2)) {
					TerrainType t = rc.senseTerrainType(loc2);
					if (t == WATER) {
						map[x - mapOriginX][y - mapOriginY] = true;
						wallSet.add(loc2);
					}
					if (t == OFF_MAP) {
						updateMapBorder(x - mapOriginX, y - mapOriginY);
					}
				}
			}
		}
    }
    
    public static void debug_printMap() {
		System.out.println("printing map...");
    	for (int y = 0; y < mapYSize; y++) {
			for (int x = 0; x < mapXSize; x++) {
				if (x >= mapLeft - 1 && x <= mapRight + 1 && y >= mapTop - 1 && y <= mapBottom + 1) {
	    			System.out.print(map[x][y] ? "#" : ".");
				} else {
	    			System.out.print("v");
				}
    		}
			System.out.println();
    	}
    }
    
    public static void debug_printDistMap() {
    	StringBuffer buf = new StringBuffer();
    	buf.append("printing map...\n");
    	for (int y = 0; y < mapYSize; y++) {
			for (int x = 0; x < mapXSize; x++) {
				Dist d = distMap[x][y];
				if (d != null) {
					buf.append("d");
				} else {
					if (x >= mapLeft - 1 && x <= mapRight + 1 && y >= mapTop - 1 && y <= mapBottom + 1) {
						buf.append(map[x][y] ? "#" : ".");
					} else {
						buf.append("v");
					}
				}
    		}
			buf.append("\n");
    	}
    	System.out.println(buf.toString());
    }
    
    public static void debug_printMapPath(List<MapLocation> path) {
    	StringBuffer buf = new StringBuffer();
		buf.append("printing map...\n");
		
		buf.append("path length = " + path.size());
		
    	for (int y = 0; y < mapYSize; y++) {
			for (int x = 0; x < mapXSize; x++) {
				MapLocation loc = new MapLocation(x, y);
				if (path.indexOf(loc) >= 0) {
					buf.append("p");
				} else {
					if (x >= mapLeft - 1 && x <= mapRight + 1 && y >= mapTop - 1 && y <= mapBottom + 1) {
						buf.append(map[x][y] ? "#" : ".");
					} else {
						buf.append("v");
					}
				}
    		}
			buf.append("\n");
    	}
    	System.out.println(buf.toString());
    }
    
    public static boolean mapGround(int x, int y) {
    	return x >= mapLeft && x <= mapRight && y >= mapTop && y <= mapBottom && !map[x][y];
    }
    
    public static boolean mapGround(MapLocation loc) {
    	return mapGround(loc.getX(), loc.getY());
    }
    
    public static Vector<MapLocation> getPath(MapLocation from, MapLocation to) {
    	Vector<MapLocation> path = new Vector<MapLocation>();
    	
    	MapLocation cur = from;
    	Direction dir = null;
    	while (!cur.equals(to)) {
    		path.add(cur);
    		
			dir = dir(cur, to);
			
			MapLocation loc2 = cur.add(dir); 
			if (mapGround(loc2.getX(), loc2.getY())) {
				cur = loc2;
				continue;
			}
			
			loc2 = cur.add(dir.rotateLeft());
			if (mapGround(loc2.getX(), loc2.getY())) {
				cur = loc2;
				continue;
			}
			
			loc2 = cur.add(dir.rotateRight());
			if (mapGround(loc2.getX(), loc2.getY())) {
				cur = loc2;
				continue;
			}
    		
	    	int bestDist = cur.distanceSquaredTo(to);        	
        	MapLocation cur1 = cur;
        	MapLocation cur2 = cur;
        	Direction dir1 = dir;
        	Direction dir2 = dir;
        	Vector<MapLocation> path1 = new Vector<MapLocation>();
        	Vector<MapLocation> path2 = new Vector<MapLocation>();
        	while (true) {     		
        		while (true) {
        			loc2 = cur1.add(dir1); 
        			if (mapGround(loc2.getX(), loc2.getY())) {
        				cur1 = loc2;
        				dir1 = dir1.isDiagonal() ? dir1.rotateRight().rotateRight() : dir1.rotateRight();
        				break;
        			} else {
        				dir1 = dir1.rotateLeft();
        			}
        		}
        		while (true) {
        			loc2 = cur2.add(dir2); 
        			if (mapGround(loc2.getX(), loc2.getY())) {
        				cur2 = loc2;
        				dir2 = dir2.isDiagonal() ? dir2.rotateLeft().rotateLeft() : dir2.rotateLeft();
        				break;
        			} else {
        				dir2 = dir2.rotateRight();
        			}
        		}
        		
        		int dist = cur1.distanceSquaredTo(to);
        		if (dist < bestDist) {
        			cur = cur1;
        			path.addAll(path1);
        			break;
        		}
        		dist = cur2.distanceSquaredTo(to);
        		if (dist < bestDist) {
        			cur = cur2;
        			path.addAll(path2);
        			break;
        		}
        		
        		path1.add(cur1);
        		path2.add(cur2);
        	}
    	}
    	path.add(to);
    	
    	return path;
    }
    
    public static class Dist {
    	public int dist;
    	public Direction firstDir;
    	public MapLocation loc;
    	public Direction lastDir;
    	
    	public Dist(int dist, Direction firstDir, MapLocation loc, Direction lastDir) {
    		this.dist = dist;
    		this.firstDir = firstDir;
    		this.loc = loc;
    		this.lastDir = lastDir;
    	}
    }
    
    public static void calcAstarDists() {
    	
//    	debug_startTimer();
    	
    	distMap = new Dist[mapXSize][mapYSize];
    	
    	int safety = 60;
        Vector<Dist> open = new Vector<Dist>();
        open.add(new Dist(1, null, sub(loc, mapOrigin), null));        
        while (open.size() > 0 && safety > 0) {
        	Dist d = open.remove(0);
        	MapLocation loc = d.loc;
        	int locX = loc.getX();
        	int locY = loc.getY();
        	Dist dd = distMap[locX][locY];
        	if (dd == null) {
        		
        		safety--;
        		
        		distMap[locX][locY] = d;
        		
    			Direction cur = d.lastDir;
    			MapLocation loc2;
    			if (cur == null) {
    				cur = Direction.NORTH;
    				do {
    					loc2 = loc.add(cur);
    					if (mapGround(loc2)) {
    						open.add(new Dist(d.dist + (cur.isDiagonal() ? diag : orth),
    								cur, loc2, cur));
    					}
    					cur = cur.rotateRight();
    				} while (cur != NORTH);
    			} else if (cur.isDiagonal()) {
					loc2 = loc.add(cur);
					if (mapGround(loc2)) {
						open.add(new Dist(d.dist + (cur.isDiagonal() ? diag : orth),
								d.firstDir, loc2, cur));
					}
					cur = cur.rotateLeft();
					loc2 = loc.add(cur);
					if (mapGround(loc2)) {
						open.add(new Dist(d.dist + (cur.isDiagonal() ? diag : orth),
								d.firstDir, loc2, cur));
					}
					cur = cur.rotateLeft();
					loc2 = loc.add(cur);
					if (mapGround(loc2)) {
						open.add(new Dist(d.dist + (cur.isDiagonal() ? diag : orth),
								d.firstDir, loc2, cur));
					}
					cur = cur.opposite();
					loc2 = loc.add(cur);
					if (mapGround(loc2)) {
						open.add(new Dist(d.dist + (cur.isDiagonal() ? diag : orth),
								d.firstDir, loc2, cur));
					}
					cur = cur.rotateLeft();
					loc2 = loc.add(cur);
					if (mapGround(loc2)) {
						open.add(new Dist(d.dist + (cur.isDiagonal() ? diag : orth),
								d.firstDir, loc2, cur));
					}
				} else {
					loc2 = loc.add(cur);
					if (mapGround(loc2)) {
						open.add(new Dist(d.dist + (cur.isDiagonal() ? diag : orth),
								d.firstDir, loc2, cur));
					}
					Direction cur2 = cur.rotateLeft();
					loc2 = loc.add(cur2);
					if (mapGround(loc2)) {
						open.add(new Dist(d.dist + (cur2.isDiagonal() ? diag : orth),
								d.firstDir, loc2, cur2));
					}
					cur = cur.rotateLeft();
					loc2 = loc.add(cur);
					if (mapGround(loc2)) {
						open.add(new Dist(d.dist + (cur.isDiagonal() ? diag : orth),
								d.firstDir, loc2, cur));
					}
				}
        	}
        }
    	
//    	debug_stopTimer();
        
//        debug_printDistMap();
    }   
    
    public static MapLocation getLocOfRobotWithId(int id) throws Exception {
    	for (Robot r : rc.senseNearbyGroundRobots()) {
    		if (r.getID() == id) {
    			return rc.senseLocationOf(r);
    		}
    	}
    	for (Robot r : rc.senseNearbyAirRobots()) {
    		if (r.getID() == id) {
    			return rc.senseLocationOf(r);
    		}
    	}
    	return null;
    }
    
    public static void face(Direction dir) throws Exception {
    	if (dir != rc.getDirection()) {
    		rc.queueSetDirection(dir);
    	}
    }
    
    public Vector<Robot> getEnemies() {
    	Vector<Robot> v = new Vector<Robot>();
    	for (Robot r : rc.senseNearbyGroundRobots()) {
    		if (r.getTeam() != team) {
    			v.add(r);
    		}
    	}
    	return v;
    }
    
    public boolean tryAttack() throws Exception {
    	if (type.canAttackAir()) {
    		for (Robot r : rc.senseNearbyAirRobots()) {
    			if (r.getTeam() != team) {
    				MapLocation pos = rc.senseLocationOf(r);
    				if (rc.canAttackSquare(pos)) {
    					rc.queueAttackAir(pos);
    					return true;
    				}
    			}
    		}
    	}
    	if (type.canAttackGround()) {
    		for (Robot r : rc.senseNearbyGroundRobots()) {
    			if (r.getTeam() != team) {
    				MapLocation pos = rc.senseLocationOf(r);
    				if (rc.canAttackSquare(pos)) {
    					rc.queueAttackGround(pos);
    					return true;
    				}
    			}
    		}
    	}
    	return false;
    }
    
    public boolean tryAttack360() throws Exception {
    	if (!tryAttack()) {
    		face(rc.getDirection().opposite());
    		rc.yield();
    		return tryAttack();
    	}
    	return true;
    }
    
    // ========================================================================
    
    public static MapLocation archonGoal = null;
    public static Vector<MapLocation> goalPath = null;
    public static Direction goalDir = null;
    
    public static void processMessages() throws Exception {
    	Message m;
    	while ((m = getMessage()) != null) {
    		if (receiveWalls(m)) {
    		}
    		if (type == ARCHON && archonGoal == null && m.strings[0].equals("upgrade")) {
    			archonGoal = m.locations[0];
    			
    			goalPath = getPath(sub(loc, mapOrigin), sub(archonGoal, mapOrigin));
    			
//    			debug_printMapPath(goalPath);
    		}
    	}
    }
    
    public static boolean archonLeader = false;
    
    public void run() {
    	try {
    		
    		boolean waiting = true;
    		MapLocation goalScout = null;
    		
    		MapLocation attackGoal = null;
    		
    		attackGoal.distanceSquaredTo(goalScout);
    		
    		boolean moveAway = false;
    		
    		boolean attacking = false;
    		
    		Direction away = rc.senseEnemyArchon().opposite();
    		
    		archonLeader = false;
    		
			if (type == ARCHON && rc.senseGroundRobotAtLocation(rc.getLocation().add(Direction.SOUTH_EAST)) != null) {
				archonLeader = true;
			} else {
			}
    	
	        while (true) {
	            try {
	            	if (type == ARCHON) {
	            		if (rc.canMove(away)) {
	            			tryMove(away);
	            		} else {
	            			face(away.opposite());
	            		}
	            		
	            		tryArchonHealEveryone(40);
	            		
	            		if (rc.isActive()) {            			
	            		} else {
	            			if (rc.getEnergonLevel() > 59) {
	            				if (getEnemies().size() > 0 || rc.getUnitCount(SOLDIER) < 2) {
		            				if (!trySpawnRobot(SOLDIER)) {
		            					trySpawnRobot(SCOUT);
		            				}
	            				} else {	            			
		            				trySpawnRobot(SCOUT);
		            			}
	            			}
	            		}
	            	}
	            	
	            	if (type == TANK) {
	            		Direction dir = rc.senseEnemyArchon(); 
	            		face(dir);
	            		
	            		MapLocation x;
	            		if (dir.isDiagonal()) {
	            			x = loc.add(dir).add(dir).add(dir);
	            		} else {
	            			x = loc.add(dir).add(dir).add(dir).add(dir);
	            		}
	            		rc.queueAttackGround(x);
	            	}
	            	
	            	if (type == SOLDIER) {
	            		if (waiting) {
	            			if (rc.getEnergonLevel() > type.maxEnergon() - 1) {
	            				waiting = false;
	            			}
	            		}
	            		if (!waiting) {
	            			if (rc.isActive()) {
		            			if (rc.getUnitCount(TANK) < 2 && rc.getEnergonLevel() >= 10) {
		            				rc.queueEvolve(TANK);
		            				type = TANK;
		            				rc.yield();
		            				continue;
		            			}
	            			
		            			if (!tryAttack360()) {	            			
			            			if (attackGoal != null) {
			            				tryMove(dir(attackGoal));
			            			} else {
			            				rc.yield();
			            			}
		            			}
	            			}
	            		}
	            	}
	            	
	            	if (type == EMP) {
	            		if (rand.nextInt(10) == 0) {
	            			sendMessage("goalScout", new MapLocation[] {loc});
	            		}
	            		if (rc.isActive()) {
	            		} else {
	            			boolean goodDistance = loc.distanceSquaredTo(nearestArchonLocation()) > 25;
	            			Direction dir = rc.senseEnemyArchon();
	            			
	            			if (moveAway) {
	            				if (goodDistance) {
	            					rc.suicide();
	            				}
	            				dir = dir(nearestArchonLocation()).opposite();
	            			}
	            			
	            			if (dir == OMNI) {
	            				if (goodDistance) {
	            					rc.suicide();
	            				} else {
	            					moveAway = true;
	            				}
	            			} else {
	            				tryMove(dir);
	            			}
	            		}
	            	}
	            	
	            	if (type == SCOUT) {
	            		if (waiting) {
	            			if (rc.getEnergonLevel() > type.maxEnergon() - 1) {
	            				waiting = false;
	            			}
	            		}
	            		if (!waiting) {
	            			
	            			Message m;
	            			while ((m = getMessage()) != null) {
	            				if (m.strings[0].equals("goalScout")) {
	            					goalScout = m.locations[0];
	            				}
	            			}
	            			
		            		if (rc.isActive()) {
		            			
		            		} else {
		            			
		            			// if we are near base, and there are enemy scouts, kill them
		            			MapLocation base = nearestArchonLocation();
		            			if (attacking || loc.distanceSquaredTo(base) <= 64) {
		            				attacking = true;
		            				int bestDist = Integer.MAX_VALUE;
		            				MapLocation best = null;
		            				for (Robot r : rc.senseNearbyAirRobots()) {
		            					if (r.getTeam() != team) {
		            						MapLocation pos = rc.senseLocationOf(r);
		            						int dist = loc.distanceSquaredTo(pos);
		            						if (dist < bestDist) {
		            							best = pos;
		            						}		            						
		            					}
		            				}
		            				if (best != null) {
		            					if (loc.isAdjacentTo(best)) {
		            						tryAttack360();
		            					} else {
		            						tryMove(dir(best));
		            					}
		            				}
		            			}
		            			
		            			
		            			MapLocation enemy = nearestArchonLocation();
		            			Direction enemyDir = dir(enemy);
		            			
		            			if (goalScout == null) {
			            			
			            			boolean arrived = rc.getEnergonLevel() <= 5.0 || enemyDir == OMNI;
			            			for (Robot r : rc.senseNearbyGroundRobots()) {
			            				if (r.getTeam() != team && rc.senseRobotTypeOf(r) == ARCHON) {
			            					arrived = true;
			            				}
			            			}
			            			
			            			if (loc.distanceSquaredTo(nearestArchonLocation()) > 25 &&
			            					arrived) {
			            				goalScout = loc;
			            			}
		            			}
		            			
		            			if (goalScout != null) {
		            				if (loc.equals(goalScout)) {
		            					if (rc.getEnergonLevel() >= 14.0 && 
		            							loc.distanceSquaredTo(nearestArchonLocation()) > 25) {
		            						int countGoods = 0;
		            						for (Robot r : rc.senseNearbyAirRobots()) {
		            							if (r.getTeam() == team) {
		            								countGoods++;
		            							}
		            						}
		            						if (countGoods >= 3) {
			            						rc.queueEvolve(EMP);
			            						type = EMP;
		            						}
		            					} else {
		            						sendMessage("goalScout", new MapLocation[] {loc});
		            						for (Robot r : rc.senseNearbyAirRobots()) {
		            							if (r.getTeam() != team) {
		            								MapLocation pos = rc.senseLocationOf(r);
		            								if (loc.isAdjacentTo(pos)) {
		            									if (!rc.canAttackSquare(pos)) {
		            										face(dir(pos));
		            									}
		            									rc.queueAttackAir(pos);
		            								}
		            							}
		            						}
		            					}
		            				} else if (loc.isAdjacentTo(goalScout)) {
		            					Robot r = rc.senseAirRobotAtLocation(goalScout);
		            					if (r == null || r.getTeam() != team) {
		            						goalScout = null;
		            					} else {
			            					RobotType t = rc.senseRobotTypeOf(r);
			            					double e = t.maxEnergon() - rc.senseEventualEnergonOf(r);
			            					
			            					double amount = Math.min(e, rc.getEnergonLevel());
			            					if (amount > 0) {
			            						rc.transferEnergon(amount, goalScout, IN_AIR);
			            					}
		            					}
		            				} else {
		            					tryMove(dir(goalScout));
		            				}
		            			} else {
		            				tryMove(rc.senseEnemyArchon());
		            			}
		            		}
	            		}
	            	}
	            	
	            	rc.yield();
	            	
	            } catch(Exception e) {
	                System.out.println("caught exception:");
	                e.printStackTrace();
	            }
	        }
		} catch (Exception e1) {
		}
    }
}
