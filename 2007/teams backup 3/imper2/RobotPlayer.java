package imper2;

import battlecode.common.*;
import battlecode.world.signal.EvolutionSignal;
import boat.RobotPlayer.Board;
import static battlecode.common.GameConstants.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static battlecode.common.MapHeight.*;
import static battlecode.common.TerrainType.*;

import java.io.*;
import java.lang.*;
import java.math.*;
import java.util.*;

public class RobotPlayer implements Runnable {
    
    public static RobotController rc;
    public static Robot r;
    public static int id;
    public static RobotType type;
    public static Team team;
    public static boolean inAir;
    public static Random rand;
    public static final int myMagic = 0x729d8298;
        
    public static int mapOriginX;
    public static int mapOriginY;
    public static int mapStride = 92;
    public static String map = "                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $";
    // information
    public static int mapTop = Integer.MIN_VALUE;
    public static int mapBottom = Integer.MAX_VALUE;
    public static int mapLeft = Integer.MIN_VALUE;
    public static int mapRight = Integer.MAX_VALUE;
    public static HashSet<MapLocation> wallSet = new HashSet<MapLocation>();
    public static HashSet<MapLocation> upgradeSet = new HashSet<MapLocation>();
    public static HashSet<MapLocation> nogradeSet = new HashSet<MapLocation>();
    // new information
    public static HashSet<MapLocation> newWallSet = new HashSet<MapLocation>();
    public static HashSet<MapLocation> newUpgradeSet = new HashSet<MapLocation>();
    public static HashSet<MapLocation> newNogradeSet = new HashSet<MapLocation>();
    
    public static int[][][] newSquaresToSense;
    public static int[][][] allSquaresToSense;
    
    public RobotPlayer(RobotController rc) {
    	RobotPlayer.rc = rc;
        r = rc.getRobot();
        id = r.getID();
        team = r.getTeam();
        setType(rc.getRobotType());
        inAir = type.isAirborne();
        rand = new Random(id);
        rand = new Random(rand.nextInt() % 100000);
        rand = new Random(rand.nextLong() % 1000000000000000L);
        
        MapLocation loc = rc.getLocation();
        mapOriginX = loc.getX() - (mapStride - 2) / 2;
        mapOriginY = loc.getY() - (mapStride - 2) / 2;
    }
    
    // ========================================================================
    
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
    
    public static void setType(RobotType type) {
    	RobotPlayer.type = type;
    	
    	int sensorRadius = type.sensorRadius();
    	if (sensorRadius == 6) {
    		newSquaresToSense = MyConst.newSquaresToSense6();
    		allSquaresToSense = MyConst.allSquaresToSense6();
    	} else if (sensorRadius == 5) {
    		newSquaresToSense = MyConst.newSquaresToSense5();
    		allSquaresToSense = MyConst.allSquaresToSense5();
    	} else if (sensorRadius == 4) {
    		newSquaresToSense = MyConst.newSquaresToSense4();
    		allSquaresToSense = MyConst.allSquaresToSense4();
    	} else if (sensorRadius == 3) {
    		if (type == TANK) {
    			newSquaresToSense = MyConst.newSquaresToSense3Tank();
    			allSquaresToSense = MyConst.allSquaresToSense3Tank();
    		} else {
    			newSquaresToSense = MyConst.newSquaresToSense3Soldier();
    			allSquaresToSense = MyConst.allSquaresToSense3Soldier();
    		}
    	}
    }
    
    public static void updateMapTop(int mapTop) {
    	if (RobotPlayer.mapTop < mapTop) {
        	RobotPlayer.mapTop = mapTop;
        	
        	map = map.substring((mapTop - mapOriginY) * mapStride, map.length());
        	mapOriginY = mapTop;
        	
        	for (Iterator<MapLocation> it = wallSet.iterator(); it.hasNext(); ) {
        		MapLocation pos = it.next();
        		if (pos.getY() < mapTop) {
        			it.remove();
        			newWallSet.remove(pos);
        		}
        	}
    	}
    }
    
    public static void updateMapBottom(int mapBottom) {
    	if (RobotPlayer.mapBottom > mapBottom) {
        	RobotPlayer.mapBottom = mapBottom;
        	
        	map = map.substring(0, (mapBottom - mapOriginY + 1) * mapStride);
        	
        	for (Iterator<MapLocation> it = wallSet.iterator(); it.hasNext(); ) {
        		MapLocation pos = it.next();
        		if (pos.getY() > mapBottom) {
        			it.remove();
        			newWallSet.remove(pos);
        		}
        	}
    	}
    }
    
    public static void updateMapLeft(int mapLeft) {
    	if (RobotPlayer.mapLeft < mapLeft) {
        	RobotPlayer.mapLeft = mapLeft;
        	
        	int amount = mapLeft - mapOriginX;
        	map = map.replaceAll("(?<=^|\\$).{" + amount + "}", "");
        	mapOriginX = mapLeft;
        	mapStride -= amount;
        	regexCache.clear();
        	
        	for (Iterator<MapLocation> it = wallSet.iterator(); it.hasNext(); ) {
        		MapLocation pos = it.next();
        		if (pos.getX() < mapLeft) {
        			it.remove();
        			newWallSet.remove(pos);
        		}
        	}
    	}
    }
    
    public static void updateMapRight(int mapRight) {
    	if (RobotPlayer.mapRight > mapRight) {
        	RobotPlayer.mapRight = mapRight;
        	
        	int amount = mapStride - (mapRight - mapOriginX + 2);
        	map = map.replaceAll(".{" + amount + "}(?=\\$)", "");
        	mapStride -= amount;
        	regexCache.clear();
        	
        	for (Iterator<MapLocation> it = wallSet.iterator(); it.hasNext(); ) {
        		MapLocation pos = it.next();
        		if (pos.getX() > mapRight) {
        			it.remove();
        			newWallSet.remove(pos);
        		}
        	}
    	}
    }
    
    public static void setMapSquare(StringBuffer buf, MapLocation pos, char c) {
		buf.setCharAt((pos.getY() - mapOriginY) * mapStride + (pos.getX() - mapOriginX), c);
    }
    
    public static void clearNewSets() {
    	newWallSet.clear();
    	newUpgradeSet.clear();
    	newNogradeSet.clear();
    }
    
    public static void updateMap() {
    	StringBuffer buf = new StringBuffer(map);
    	for (MapLocation pos : newWallSet) {
        	int x = pos.getX();
        	int y = pos.getY();
        	if (x >= mapLeft && x <= mapRight && y >= mapTop && y <= mapBottom) {
    			setMapSquare(buf, pos, '#');
    		} else {
    			wallSet.remove(pos);
    		}
    	}
    	for (MapLocation pos : newUpgradeSet) {
    		setMapSquare(buf, pos, 'u');
    	}
    	for (MapLocation pos : newNogradeSet) {
    		setMapSquare(buf, pos, ' ');
    	}
    	map = buf.toString();
    }
    
    public static void checkForNograde(MapLocation pos) throws Exception {
		if (upgradeSet.contains(pos)) {
    		if (rc.senseUpgradeAtLocation(pos) == null) {
    			if (upgradeSet.remove(pos)) {
    				newUpgradeSet.remove(pos);
    			}
    			if (nogradeSet.add(pos)) {
    				newNogradeSet.add(pos);
    			}        			
    		}
		}
    }
    
    public static void explore(boolean everything) throws Exception {
    	int[][] squares = null;
    	if (everything) {
    		squares = allSquaresToSense[rc.getDirection().ordinal()];
    	} else {
    		squares = newSquaresToSense[rc.getDirection().ordinal()];
    	}
    	
    	MapLocation loc = rc.getLocation();
    	int locX = loc.getX();
    	int locY = loc.getY();
    	
    	// deal with upgrades
    	for (Upgrade u : rc.senseNearbyUpgrades()) {
    		MapLocation pos = rc.senseLocationOf(u);
    		if (upgradeSet.add(pos)) {
    			newUpgradeSet.add(pos);
    		}
    	}
		
		// handle case where we picked up an upgrade
    	if (type == ARCHON) {
    		checkForNograde(loc);
    	}
    	
    	// deal with the map
    	for (int[] squareOffset : squares) {
    		int squareX = locX + squareOffset[0];
    		int squareY = locY + squareOffset[1];
    		MapLocation square = new MapLocation(squareX, squareY);
    		
    		// deal with nogrades (i.e. the location of upgrades that are no more)
    		checkForNograde(square);
    		
    		TerrainType t = rc.senseTerrainType(square);
    		if (t == WATER) {
    			if (squareX >= mapLeft && squareX <= mapRight && squareY >= mapTop && squareY <= mapBottom) {
	    			if (wallSet.add(square)) {
	    				newWallSet.add(square);
	    			}
    			}
    		} else if (t == OFF_MAP) {
    			if (squareX == locX) {
    				if (squareY < locY) {
    					if (mapTop == Integer.MIN_VALUE) {
    						if (rc.senseTerrainType(new MapLocation(
    								squareX, squareY + 1)) != OFF_MAP) {
    							updateMapTop(squareY + 2);
    						}
    					}
    				} else {
    					if (mapBottom == Integer.MAX_VALUE) {
    						if (rc.senseTerrainType(new MapLocation(
    								squareX, squareY - 1)) != OFF_MAP) {
    							updateMapBottom(squareY - 2);
    						}
    					}
    				}
    			} else if (squareY == locY) {
    				if (squareX < locX) {
    					if (mapLeft == Integer.MIN_VALUE) {
    						if (rc.senseTerrainType(new MapLocation(
    								squareX + 1, squareY)) != OFF_MAP) {
    							updateMapLeft(squareX + 2);
    						}
    					}
    				} else {
    					if (mapRight == Integer.MAX_VALUE) {
    						if (rc.senseTerrainType(new MapLocation(
    								squareX - 1, squareY)) != OFF_MAP) {
    							updateMapRight(squareX - 2);
    						}
    					}
    				}
    			}
    		}
    	}
    }
    
    public static HashMap<String, String> regexCache = new HashMap<String, String>();
    
    public static String regex(String regexKey) {
    	String regex = regexCache.get(regexKey);
    	if (regex != null) {
    		return regex;
    	} else {
    		regex = regexKey.
		    		replaceAll("A", "" + (mapStride - 2)).
					replaceAll("B", "" + (mapStride - 1)).
					replaceAll("C", "" + mapStride);
    		regexCache.put(regexKey, regex);
    		return regex;
    	}
    }
    
    public static Direction dirToNearestUpgrade() {
    	StringBuffer buf = new StringBuffer(map);
		
    	// make this archon favor the upgrades in her quadrant
    	{
			int leftMost = Integer.MAX_VALUE;
			int rightMost = Integer.MIN_VALUE;
			int topMost = Integer.MAX_VALUE;
			int bottomMost = Integer.MIN_VALUE;
			for (MapLocation u : upgradeSet) {
				leftMost = Math.min(leftMost, u.getX());
				rightMost = Math.max(rightMost, u.getX());
				topMost = Math.min(topMost, u.getY());
				bottomMost = Math.max(bottomMost, u.getY());
			}
			int xDivide = (leftMost + rightMost) / 2;
			int yDivide = (topMost + bottomMost) / 2;
			boolean good = false;
			for (MapLocation u : upgradeSet) {
				int x = u.getX() - xDivide;
				int y = u.getY() - yDivide;
				if (MyConst.dirDs[archonQuadrant.ordinal()][0] * x >= 0 &&
						MyConst.dirDs[archonQuadrant.ordinal()][1] * y >= 0) {
					good = true;
				} else {
					setMapSquare(buf, u, 'Y');
				}
			}
			if (!good) {
				buf = new StringBuffer(map);
			}
    	}
		
		setMapSquare(buf, rc.getLocation(), 'x');
    	String s = buf.toString();
    	
    	if (!s.contains("u")) return null;
    	
    	for (int i = 0; i < 40; i++) {
    		if (s.matches(regex(".*(x(?=.{A,C}u)|(?<=u.{A})x|(?<=u.{B})x|(?<=u.{C})x|x(?=u)|(?<=u)x).*"))) {
	    		if (s.matches(regex(".*x(?=.{B}u).*"))) {
	    			return SOUTH;
	    		} else if (s.matches(regex(".*(?<=u.{B})x.*"))) {
	    			return NORTH;
	    		} else if (s.matches(regex(".*x(?=u).*"))) {
	    			return EAST;
	    		} else if (s.matches(regex(".*(?<=u)x.*"))) {
	    			return WEST;
	    		} else if (s.matches(regex(".*(?<=u.{A})x.*"))) {
	    			return NORTH_EAST;
	    		} else if (s.matches(regex(".*(?<=u.{C})x.*"))) {
	    			return NORTH_WEST;
	    		} else if (s.matches(regex(".*x(?=.{C}u).*"))) {
	    			return SOUTH_EAST;
	    		} else if (s.matches(regex(".*x(?=.{A}u).*"))) {
	    			return SOUTH_WEST;
	    		}
    		}
    		
    		String ss = s.replaceAll(regex(" (?=.{A,C}u)|(?<=u.{A}) |(?<=u.{B}) |(?<=u.{C}) | (?=u)|(?<=u) "), "u");
    		if (s.equals(ss)) break;
    		s = ss;
    	}
    	
    	return null;
    }
    
    public static Direction dirToUsingAStar(MapLocation pos) {
    	StringBuffer buf = new StringBuffer(map);
		
		setMapSquare(buf, pos, 'a');
		
		setMapSquare(buf, rc.getLocation(), 'x');
    	String s = buf.toString();
    	
    	if (!s.contains("a")) return null;
    	
    	for (int i = 0; i < 40; i++) {
    		if (s.matches(regex(".*(x(?=.{A,C}a)|(?<=a.{A})x|(?<=a.{B})x|(?<=a.{C})x|x(?=a)|(?<=a)x).*"))) {
	    		if (s.matches(regex(".*x(?=.{B}a).*"))) {
	    			return SOUTH;
	    		} else if (s.matches(regex(".*(?<=a.{B})x.*"))) {
	    			return NORTH;
	    		} else if (s.matches(regex(".*x(?=a).*"))) {
	    			return EAST;
	    		} else if (s.matches(regex(".*(?<=a)x.*"))) {
	    			return WEST;
	    		} else if (s.matches(regex(".*(?<=a.{A})x.*"))) {
	    			return NORTH_EAST;
	    		} else if (s.matches(regex(".*(?<=a.{C})x.*"))) {
	    			return NORTH_WEST;
	    		} else if (s.matches(regex(".*x(?=.{C}a).*"))) {
	    			return SOUTH_EAST;
	    		} else if (s.matches(regex(".*x(?=.{A}a).*"))) {
	    			return SOUTH_WEST;
	    		}
    		}
    		
    		String ss = s.replaceAll(regex(" (?=.{A,C}a)|(?<=a.{A}) |(?<=a.{B}) |(?<=a.{C}) | (?=a)|(?<=a) "), "a");
    		if (s.equals(ss)) break;
    		s = ss;
    	}
    	
    	return null;
    }
    
    public static int hashRound(int round) {
    	return (round * 333) ^ myMagic;
    }
    
    public static void sendInfo(boolean everything) throws Exception {
    	Set<MapLocation> wallSet = everything ? RobotPlayer.wallSet : newWallSet;
    	Set<MapLocation> upgradeSet = everything ? RobotPlayer.upgradeSet : newUpgradeSet;
    	Set<MapLocation> nogradeSet = everything ? RobotPlayer.nogradeSet : newNogradeSet;
    	
    	int newWalls = wallSet.size();
    	int newUpgrades = upgradeSet.size();
    	int newNogrades = nogradeSet.size();
    	
    	MapLocation[] locs = new MapLocation[newWalls + newUpgrades + newNogrades];
    	
    	System.arraycopy(wallSet.toArray(), 0, locs, 0, newWalls);
    	System.arraycopy(upgradeSet.toArray(), 0, locs, newWalls, newUpgrades);
    	System.arraycopy(nogradeSet.toArray(), 0, locs, newWalls + newUpgrades, newNogrades);
    	
    	Message m = new Message();
    	m.locations = locs;
    	m.ints = new int[] {hashRound(Clock.getRoundNum()), 72, newWalls, newUpgrades, newNogrades,
    			mapLeft, mapRight, mapTop, mapBottom};
    	rc.queueBroadcast(m);
    }
    
    public static void debug_printMap() {
    	StringBuffer buf = new StringBuffer(map);
		setMapSquare(buf, rc.getLocation(), 'x');
    	String s = buf.toString();   	
    	
    	System.out.println();
    	System.out.print(s.replaceAll(" ", ".").replaceAll("\\$", "\n"));
    }
    
    public static void face(Direction dir) {
    	Direction currentDir = rc.getDirection();
    	if (currentDir != dir) {
    		rc.queueSetDirection(dir);
    		rc.yield();
    	}
    }
    
    public static void move() {
    	rc.queueMoveForward();
    	rc.yield();
    }
    
    public static void move(Direction dir) {
    	face(dir);
    	move();
    }
    
    // ========================================================================
    
    public static Direction dir2(MapLocation pos) {
    	MapLocation loc = rc.getLocation();
    	int d = (int)((Math.round(Math.atan2(
    			pos.getY() - loc.getY(),
    			pos.getX() - loc.getX()) / 
    			Math.PI * 4.0) + 2) % 8);
    	if (d < 0) d += 8;
    	return Direction.values()[d];
    }
    
    public static Direction dir3(MapLocation pos) {
    	Direction bestDir = NORTH;
    	int bestDist = Integer.MAX_VALUE;
    	MapLocation loc = rc.getLocation();
    	for (int i = 0; i < 8; i++) {
    		Direction d = Direction.values()[i];
    		MapLocation loc2 = loc.add(d);
    		int dist = loc2.distanceSquaredTo(pos);
    		if (dist < bestDist) {
    			bestDist = dist;
    			bestDir = d;
    		}
    	}
    	return bestDir;
    }
    
    public static Direction dir4(MapLocation pos) {
    	MapLocation loc = rc.getLocation();
    	int dx = Math.abs(loc.getX() - pos.getX());
    	int dy = Math.abs(loc.getY() - pos.getY());
    	int maxD = Math.max(dx, dy);
    	int minD = Math.min(dx, dy);
    	if (rand.nextDouble() < (double)minD / maxD) {
        	Direction bestDir = NORTH;
        	int bestDist = Integer.MAX_VALUE;
        	for (int i = 0; i < 8; i++) {
        		Direction d = Direction.values()[i];
        		MapLocation loc2 = loc.add(d);
        		int dist = loc2.distanceSquaredTo(pos);
        		if (dist < bestDist) {
        			bestDist = dist;
        			bestDir = d;
        		}
        	}
        	return bestDir;
    	} else {
        	Direction bestDir = NORTH;
        	int bestDist = Integer.MAX_VALUE;
        	for (int i = 0; i < 8; i += 2) {
        		Direction d = Direction.values()[i];
        		MapLocation loc2 = loc.add(d);
        		int dist = loc2.distanceSquaredTo(pos);
        		if (dist < bestDist) {
        			bestDist = dist;
        			bestDir = d;
        		}
        	}
        	return bestDir;
    	}
    }
    
    public static MapLocation add(MapLocation a, MapLocation b) {
    	return new MapLocation(a.getX() + b.getX(), a.getY() + b.getY());
    }
    
    public static MapLocation sub(MapLocation a, MapLocation b) {
    	return new MapLocation(a.getX() - b.getX(), a.getY() - b.getY());
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
    
    public static boolean trySpawnRobot(RobotType t) throws Exception {
    	boolean inAir = t.isAirborne();
		Direction currentDir = rc.getDirection();
		MapLocation loc = rc.getLocation();
    	if (empty(inAir, loc.add(currentDir))) {
    		rc.queueSpawn(t);
    		rc.yield();
    		return true;
    	} else {
    		Direction d = currentDir.rotateRight();
    		do {
				if (empty(inAir, loc.add(d))) {
					face(d);
		    		rc.queueSpawn(t);
		    		rc.yield();
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
    
    public static void giveUp() {
		while (true) {
			rc.yield();
		}
    }
    
    public static MapLocation nearestArchonLocation() {
    	MapLocation loc = rc.getLocation();
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
    
    public static boolean tryMove(Direction d) {
    	if (rc.canMove(d)) {
    		move(d);
    		return true;
    	} else if (rc.canMove(d.rotateRight())) {
    		move(d.rotateRight());
    		return true;
    	} else if (rc.canMove(d.rotateLeft())) {
    		move(d.rotateLeft());
    		return true;
    	}
    	for (int i = 0; i < 6; i++) {
    		d = Direction.values()[rand.nextInt(8)];
    		if (rc.canMove(d)) {
    			move(d);
    			return true;
    		}
    	}
    	return false;
    }
    
    public static void evolve(RobotType type) {
    	rc.queueEvolve(type);
    	rc.yield();
    	setType(type);
    }
    
    public static MapLocation scoutGoal = null;
    
    public static void sendScoutGoal() throws Exception {
    	Message m = new Message();
    	m.ints = new int[] {hashRound(Clock.getRoundNum()), 85};
    	m.locations = new MapLocation[] {scoutGoal};
    	rc.queueBroadcast(m);
    }
    
    public static void processMessages(boolean includeMapUpdates) {
    	int roundHash = hashRound(Clock.getRoundNum() - 1);
    	while (true) {
        	Message m = rc.getNextMessage();
        	if (m == null) break;
        	
        	if (m.ints != null && m.ints.length >= 2) {
        		if (m.ints[0] == roundHash) {
        			
        			if (m.ints[1] == 85) {
        				scoutGoal = m.locations[0];
        			}
        			
        			if (m.ints[1] == 72 && includeMapUpdates) {
        		    	int newWalls = m.ints[2];
        		    	int newUpgrades = m.ints[3];
        		    	int newNogrades = m.ints[4];
        		    	
        		    	updateMapLeft(m.ints[5]);
        		    	updateMapRight(m.ints[6]);
        		    	updateMapTop(m.ints[7]);
        		    	updateMapBottom(m.ints[8]);
        		    	
        		    	if (newWalls > 0) {
	        		    	MapLocation[] locs = new MapLocation[newWalls];
	        		    	System.arraycopy(m.locations, 0, locs, 0, newWalls);
	        		    	HashSet<MapLocation> locSet = new HashSet<MapLocation>(Arrays.asList(locs));
	        		    	locSet.removeAll(wallSet);
	        		    	wallSet.addAll(locSet);
	        		    	newWallSet.addAll(locSet);
        		    	}
        		    	
        		    	if (newUpgrades > 0) {
        		    		MapLocation[] locs = new MapLocation[newUpgrades];
            		    	System.arraycopy(m.locations, newWalls, locs, 0, newUpgrades);
            		    	HashSet<MapLocation> locSet = new HashSet<MapLocation>(Arrays.asList(locs));
            		    	locSet.removeAll(upgradeSet);
            		    	locSet.removeAll(nogradeSet);
            		    	upgradeSet.addAll(locSet);
            		    	newUpgradeSet.addAll(locSet);
        		    	}
        		    	
        		    	if (newNogrades > 0) {
        		    		MapLocation[] locs = new MapLocation[newNogrades];
            		    	System.arraycopy(m.locations, newWalls + newUpgrades, locs, 0, newNogrades);
            		    	HashSet<MapLocation> locSet = new HashSet<MapLocation>(Arrays.asList(locs));
            		    	locSet.removeAll(nogradeSet);
            		    	nogradeSet.addAll(locSet);
            		    	newNogradeSet.addAll(locSet);
            		    	upgradeSet.removeAll(locSet);
            		    	newUpgradeSet.removeAll(locSet);
        		    	}
        			}
        		}
        	}
    	}
    }
    
    public boolean tryAttack() throws Exception {
    	if (type.canAttackAir()) {
    		for (Robot r : rc.senseNearbyAirRobots()) {
    			if (r.getTeam() != team) {
    				MapLocation pos = rc.senseLocationOf(r);
    				if (rc.canAttackSquare(pos)) {
    					rc.queueAttackAir(pos);
    					rc.yield();
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
    					rc.yield();
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
    
    public boolean tryAttackAir(MapLocation pos) {
    	if (rc.canAttackSquare(pos)) {
    		rc.queueAttackAir(pos);
    		rc.yield();
    		return true;
    	} else {
    		face(dir(pos));
        	if (rc.canAttackSquare(pos)) {
        		rc.queueAttackAir(pos);
        		rc.yield();
        		return true;
        	}
    	}
    	return false;
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
    
    public static Vector<Robot> enemies() {
    	Vector<Robot> v = new Vector<Robot>();
    	for (Robot r : rc.senseNearbyAirRobots()) {
    		if (r.getTeam() != team) {
    			v.add(r);
    		}
    	}
    	for (Robot r : rc.senseNearbyGroundRobots()) {
    		if (r.getTeam() != team) {
    			v.add(r);
    		}
    	}
    	return v;
    }
    
    public static Direction dir(MapLocation to) {
    	return dir(rc.getLocation(), to);
    }
    
    public static Direction archonQuadrant;
    
    public static boolean moveAway = false;
    
    public static boolean attacking = false;
    
    public void run() {
    	try {
    		if (type == ARCHON) {
    			MapLocation loc = rc.getLocation();
    			for (int dirIndex = 1; dirIndex < 8; dirIndex += 2) {
    				Direction dir = Direction.values()[dirIndex];
    				if (rc.senseGroundRobotAtLocation(loc.add(dir)) != null) {
    					archonQuadrant = dir.opposite();
    					break;
    				}
    			}
    			if (archonQuadrant == null) {
    				throw new Error("how could this happen?");
    			}
    		}
    		
			explore(true);
			updateMap();
			
			boolean waiting = true;
			boolean sendMessage = false;
			
			MapLocation home = rc.getLocation();
			
			boolean scoutType = rand.nextBoolean();
			
			while (rc.isActive()) {
				processMessages(true);
        		updateMap();
        		clearNewSets();
				rc.yield();
			}
			
			int lastBroadcast = Clock.getRoundNum();
    		
	        while (true) {
            	rc.clearActionQueue();
	            try {
	            	if (type == ARCHON) {
	            		processMessages(true);
		            	rc.setIndicatorString(0, "__");
		            	rc.setIndicatorString(1, "__");
		            	rc.setIndicatorString(2, "__");
		            	
		            	// spawn scout, if we can
		            	if (rc.getEnergonLevel() >= rc.getMaxEnergonLevel() - 1) {
	            			if (trySpawnRobot(SCOUT)) continue;
		            	} else {
		            		tryArchonHealEveryone(40);
		            	}
		            	
		            	// send messages
		            	if (lastBroadcast < Clock.getRoundNum() - 10) {
			            	if (newWallSet.size() > 0 || newUpgradeSet.size() > 0 || newNogradeSet.size() > 0) {
			            		sendInfo(true);
				            	rc.setIndicatorString(0, "send info");
			            		updateMap();
				            	rc.setIndicatorString(1, "update map");
			            		clearNewSets();
			            		rc.yield();
			            		
			            		
			            		// work here
//			            		if (id == 48) {
//			            			debug_printMap();
//			            		}
			            		
			            		
			            		
			            		lastBroadcast = Clock.getRoundNum();
			            	}
		            	}
		            	
		            	// decide what to do
		            	if (rc.isActive()) {
			            	rc.setIndicatorString(0, "inactive");
			            	rc.yield();
		            	} else {
		            		
		            		Direction d = null;
		            		
		            		if (enemies().size() > 0) {
		            			d = dirToUsingAStar(home);
		            		} else {
		            			d = dirToNearestUpgrade();
		            		}
			            	rc.setIndicatorString(0, "a star");
		            		if (d == null) {
		            			rc.yield();
		            			continue;
		            		}
		            		tryMove(d);
		            		explore(false);
		            	}
	            	}
	            	
	            	if (type == EMP) {
            			MapLocation loc = rc.getLocation();
	            		if (rand.nextInt(10) == 0) {
	            			sendScoutGoal();
	            		}
	            		if (rc.isActive()) {
	            			rc.yield();
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
		            	rc.setIndicatorString(0, "__");
		            	rc.setIndicatorString(1, "__");
		            	rc.setIndicatorString(2, "__");
	            		rc.setIndicatorString(2, "in queue = " + rc.getActionQueueSize());
	            		
	            		MapLocation loc = rc.getLocation();
            			
	            		if (scoutGoal == null) {
	            			if (rc.getEnergonLevel() > type.maxEnergon() - 1) {
	            				int locX = loc.getX();
	            				int locY = loc.getY();
	            				
	            				double angle = (rc.senseEnemyArchon().ordinal() - 2) * (Math.PI / 4.0);
	            				angle += rand.nextGaussian() * (Math.PI / 4);
	            				double mag = 20;
	            				
	            				scoutGoal = new MapLocation((int)(locX + mag * Math.cos(angle)), (int)(locY + mag * Math.sin(angle)));
	            			}
	            		} else {
	            			if (rc.getEnergonLevel() < type.maxEnergon() / 2 || newUpgradeSet.size() > 0) {
	            				scoutGoal = null;
	            				sendMessage = true;
	            			}
	            		}
	            		
	            		if (sendMessage) {
	            			MapLocation goal = nearestArchonLocation();
	            			if (rc.getLocation().distanceSquaredTo(goal) <= 64) {
	            				sendInfo(false);
	            				clearNewSets();
	            				sendMessage = false;
	            			}
	            		}
	            		
	            		if (rc.isActive()) {
	            			rc.yield();
	            		} else {
	            			
		            		
	            			// if we are near base, and there are enemy scouts, kill them
	        				int bestDist = Integer.MAX_VALUE;
	        				MapLocation best = null;
	        				boolean bestIsEMP = false;
	        				for (Robot r : rc.senseNearbyAirRobots()) {
	        					if (r.getTeam() != team) {
	        						MapLocation pos = rc.senseLocationOf(r);
	        						int dist = loc.distanceSquaredTo(pos);
	        						boolean isEMP = rc.senseRobotTypeOf(r) == EMP;
	        						if (bestIsEMP) {
	        							if (isEMP && dist < bestDist) {
	        								best = pos;
	        							}
	        						} else {
		        						if (dist < bestDist || isEMP) {
		        							best = pos;
		        							bestIsEMP = isEMP;
		        						}		            						
	        						}
	        					}
	        				}
	        				
	        				if (best != null) {
	        					if (loc.isAdjacentTo(best)) {
	        						rc.setIndicatorString(1, "attack A");
	        						if (!tryAttackAir(best)) {
	        							rc.setIndicatorString(1, "attack B");
	        							tryAttack360();
	        						}
	        						continue;
	        					} else {
	        						tryMove(dir(best));
	        						continue;
	        					}
	        				}
	            			
	            			
	            			
	            			
	            			MapLocation goal = scoutGoal;
	            			if (goal == null) {
	            				goal = nearestArchonLocation();
	            			}
	            			tryMove(dir4(goal));
		            		explore(false);
	            		}
	            		
	            	}
	            	
	            } catch(Exception e) {
	                System.out.println("caught exception:");
	                e.printStackTrace();
	            }
	        }
		} catch (Exception e1) {
		}
    }
}
