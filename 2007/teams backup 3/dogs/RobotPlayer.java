package dogs;

import static battlecode.common.Direction.EAST;
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
import static battlecode.common.RobotType.SOLDIER;
import static battlecode.common.RobotType.TANK;
import static battlecode.common.TerrainType.LAND;
import static battlecode.common.TerrainType.OFF_MAP;
import static battlecode.common.TerrainType.WATER;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import battlecode.common.Upgrade;

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
    	
    	clearNewSets();
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
    
    public static Direction dirToNearestMother() {
    	StringBuffer buf = new StringBuffer(map);
		
    	// make this archon favor the upgrades in her quadrant
    	for (MapLocation pos : rc.senseAlliedArchons()) {
    		setMapSquare(buf, pos, 'a');
    	}
		
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
    
    public static boolean formationCenterIsGood(MapLocation pos) {
    	int lowX = pos.getX() - 1;
    	int highX = pos.getX() + 1;
    	int lowY = pos.getY() - 1;
    	int highY = pos.getY() + 1;
    	for (int y = lowY; y <= highY; y++) {
    		for (int x = lowX; x <= highX; x++) {
    			if (x >= mapLeft && x <= mapRight && y >= mapTop && y <= mapBottom) {
    				MapLocation loc = new MapLocation(x, y);
    				if (wallSet.contains(loc)) {
    					return false;
    				}
    			} else {
    				return false;
    			}
    		}
    	}
    	return true;
    }
	
    public static void removeUpgradesFromUnderFormation() {
    	MapLocation pos = formationCenter;
    	int lowX = pos.getX() - 1;
    	int highX = pos.getX() + 1;
    	int lowY = pos.getY() - 1;
    	int highY = pos.getY() + 1;
    	for (int y = lowY; y <= highY; y++) {
    		for (int x = lowX; x <= highX; x++) {
    			if (x >= mapLeft && x <= mapRight && y >= mapTop && y <= mapBottom) {
    				MapLocation loc = new MapLocation(x, y);
    				if (upgradeSet.contains(loc)) {
    					if (upgradeSet.remove(loc)) {
    						newUpgradeSet.remove(loc);
    					}    					
    					if (nogradeSet.add(loc)) {
    						newNogradeSet.add(loc);
    						
    						// work here
    						System.out.println("got here!");
    					}
    				}
    			}
    		}
    	}
    }
	
    
    public static MapLocation getBestFormationCenter(Direction dir) {
    	
    	if (formationCenterIsGood(formationCenter.add(dir))) {
    		return formationCenter.add(dir);
    	}
    	if (rand.nextBoolean()) {
	    	if (formationCenterIsGood(formationCenter.add(dir.rotateLeft()))) {
	    		return formationCenter.add(dir.rotateLeft());
	    	}
	    	if (formationCenterIsGood(formationCenter.add(dir.rotateRight()))) {
	    		return formationCenter.add(dir.rotateRight());
	    	}
	    	if (formationCenterIsGood(formationCenter.add(dir.rotateLeft().rotateLeft()))) {
	    		return formationCenter.add(dir.rotateLeft().rotateLeft());
	    	}
	    	if (formationCenterIsGood(formationCenter.add(dir.rotateRight().rotateRight()))) {
	    		return formationCenter.add(dir.rotateRight().rotateRight());
	    	}
    	} else {
	    	if (formationCenterIsGood(formationCenter.add(dir.rotateRight()))) {
	    		return formationCenter.add(dir.rotateRight());
	    	}
	    	if (formationCenterIsGood(formationCenter.add(dir.rotateLeft()))) {
	    		return formationCenter.add(dir.rotateLeft());
	    	}
	    	if (formationCenterIsGood(formationCenter.add(dir.rotateRight().rotateRight()))) {
	    		return formationCenter.add(dir.rotateRight().rotateRight());
	    	}
	    	if (formationCenterIsGood(formationCenter.add(dir.rotateLeft().rotateLeft()))) {
	    		return formationCenter.add(dir.rotateLeft().rotateLeft());
	    	}
    	}
    	
    	MapLocation cur = formationCenter.add(dir);
    	for (int i = 0; i < 10; i++) {
    		cur = cur.add(dir);
    		if (formationCenterIsGood(cur)) {
    			return cur;
    		}
    	}
    	throw new Error("bad map!");
    }
    
    public static MapLocation getFormationCenter() {
    	String s = map;
    	
    	// deal with upgrades
//    	s = s.replaceAll(regex("(?<=[u ]u[u ].{A})(?<=[u ])[u ](?=[u ])(?=.{A}[u ][u ][u ])|" +
//    			"(?<=[u ][u ][u ].{A})(?<=u)[u ](?=[u ])(?=.{A}[u ][u ][u ])|" +
//    			"(?<=[u ][u ][u ].{A})(?<=[u ])[u ](?=u)(?=.{A}[u ][u ][u ])|" +
//    			"(?<=[u ][u ][u ].{A})(?<=[u ])[u ](?=[u ])(?=.{A}[u ]u[u ])"), "a").
//    			replaceAll("u", " ").
//    			replaceAll("a", "u");
    	
    	StringBuffer buf = new StringBuffer(s);
		setMapSquare(buf, formationCenter, 'x');
    	s = buf.toString();
    	
    	
    	
    	if (!s.contains("u")) return null;
    	
    	for (int i = 0; i < 40; i++) {
    		if (s.matches(regex(".*(x(?=.{A,C}u)|(?<=u.{A})x|(?<=u.{B})x|(?<=u.{C})x|x(?=u)|(?<=u)x).*"))) {
    			
    			
    			
    			// work here
    			debug_printMap(s);
    			
    			
	    		if (s.matches(regex(".*x(?=.{B}u).*"))) {
	    			return getBestFormationCenter(SOUTH);
	    		} else if (s.matches(regex(".*(?<=u.{B})x.*"))) {
	    			return getBestFormationCenter(NORTH);
	    		} else if (s.matches(regex(".*x(?=u).*"))) {
	    			return getBestFormationCenter(EAST);
	    		} else if (s.matches(regex(".*(?<=u)x.*"))) {
	    			return getBestFormationCenter(WEST);
	    		} else if (s.matches(regex(".*(?<=u.{A})x.*"))) {
	    			return getBestFormationCenter(NORTH_EAST);
	    		} else if (s.matches(regex(".*(?<=u.{C})x.*"))) {
	    			return getBestFormationCenter(NORTH_WEST);
	    		} else if (s.matches(regex(".*x(?=.{C}u).*"))) {
	    			return getBestFormationCenter(SOUTH_EAST);
	    		} else if (s.matches(regex(".*x(?=.{A}u).*"))) {
	    			return getBestFormationCenter(SOUTH_WEST);
	    		}
    		}
    		
    		String ss = s.replaceAll(regex(" (?=.{A,C}u)|(?<=u.{A}) |(?<=u.{B}) |(?<=u.{C}) | (?=u)|(?<=u) "), "u");
    		if (s.equals(ss)) break;
    		s = ss;
    	}
    	
    	return null;
    }
    
    public static int hashRound(int round) {
    	return (round * 333) ^ myMagic;
    }
    
    public static int messageHash() {
    	return hashRound(Clock.getRoundNum());
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
    	m.ints = new int[] {messageHash(), 72, newWalls, newUpgrades, newNogrades,
    			mapLeft, mapRight, mapTop, mapBottom};
    	rc.queueBroadcast(m);
    }
    
    public static void sendFormationCenter() throws Exception {
    	Message m = new Message();
    	m.ints = new int[] {messageHash(), 99};
    	m.strings = new String[] {"formationCenter"};
    	m.locations = new MapLocation[] {formationCenter};
    	rc.queueBroadcast(m);
    }
    
    public static void processMessages(boolean processMapMessages) {
    	int roundHash = hashRound(Clock.getRoundNum() - 1);
    	while (true) {
        	Message m = rc.getNextMessage();
        	if (m == null) break;
        	
        	if (m.ints != null && m.ints.length >= 2) {
        		if (m.ints[0] == roundHash) {
        			
        			if (m.ints[1] == 99) {
        				String msg = m.strings[0];
        				if (msg.equals("formationCenter")) {
        					formationCenter = m.locations[0];
        				}
        			}
        			
        			if (m.ints[1] == 72 && processMapMessages) {
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
    
    public static void debug_printMap() {
    	StringBuffer buf = new StringBuffer(map);
		setMapSquare(buf, rc.getLocation(), 'x');
    	String s = buf.toString();
    	
    	debug_printMap(s);
    }
    
    public static void debug_printMap(MapLocation pos) {
    	StringBuffer buf = new StringBuffer(map);
		setMapSquare(buf, rc.getLocation(), 'x');
		setMapSquare(buf, rc.getLocation(), 'A');
    	String s = buf.toString();
    	
    	debug_printMap(s);
    }
    
    public static void debug_printMap(String s) {
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
    
    public static boolean move() {
    	if (rc.canMove(rc.getDirection())) {
	    	rc.queueMoveForward();
	    	rc.yield();
	    	return true;
    	}
    	return false;
    }
    
    public static boolean move(Direction dir) {
    	face(dir);
    	return move();
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
    
    public static void waitForEnergy(double e) {
    	while (rc.getEnergonLevel() < e) {
    		rc.yield();
    	}
    }
    
    public static void spawn(RobotType type) {
    	rc.queueSpawn(type);
    	rc.yield();
    }
    
    public static void spawn(RobotType type, Direction dir) {
    	face(dir);
    	spawn(type);
    }
    
    public static void waitSpawn(RobotType type, Direction dir) {
    	waitForEnergy(type.spawnCost());
    	face(dir);
    	spawn(type);
    }
    
    public static void waitEvolve(RobotType type) {
    	waitForEnergy(type.spawnCost() + 1);
    	evolve(type);
    }
    
    public static void waitTillReady() {
    	while (rc.isActive()) {
    		rc.yield();
    	}
    }
    
    public static void face(MapLocation pos) {
    	face(dir(pos));
    }
    
    public static Direction dir(MapLocation pos) {
    	return dir(rc.getLocation(), pos);
    }
    
    public static boolean formationWalkToward(MapLocation pos) {
    	MapLocation loc = rc.getLocation();
    	Direction dir = dir(pos);
    	if (rc.canMove(dir)) {
    		return move(dir);
    	} else {
    		MapLocation forward = loc.add(dir);
    		if (!rc.canSenseSquare(forward)) {
    			face(forward);
    		}
    		if (rc.senseTerrainType(forward) == WATER) {
    			if (rc.canMove(dir.rotateLeft())) {
    				return move(dir.rotateLeft());
    			} else if (rc.canMove(dir.rotateRight())) {
    				return move(dir.rotateRight());
    			}
    		}
    	}
    	return false;
    }
    
    public static boolean everyoneInFormation() throws Exception {
    	
    	HashSet<MapLocation> formationLocs = new HashSet<MapLocation>();
    	for (int i = 0; i < 8; i++) {
    		formationLocs.add(formationCenter.add(Direction.values()[i]));
    	}
    	
    	int count = 0;
    	
    	{
    		MapLocation pos = rc.getLocation();
			if (formationLocs.contains(pos)) {
				count++;
			}
    	}
    	
    	for (Robot r : rc.senseNearbyGroundRobots()) {
    		if (r.getTeam() == team) {
    			MapLocation pos = rc.senseLocationOf(r);
    			if (formationLocs.contains(pos)) {
    				count++;
    			}
    		}
    	}
    	
    	return (count >= 4 + rc.getUnitCount(TANK));
    }
    
    public static Direction archonQuadrant;
    public static boolean leader = false;
    public static Direction formationDir;
    public static MapLocation formationCenter;
    
    public void run() {
    	try {
    		if (type == ARCHON) {
    			{
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
        		
        		System.out.println(archonQuadrant);
        		leader = (archonQuadrant == NORTH_EAST);
        		
        		switch (archonQuadrant) {
        		case NORTH_EAST: move(SOUTH_EAST); waitSpawn(SOLDIER, SOUTH); formationDir = EAST; break;
        		case SOUTH_EAST: move(SOUTH); waitSpawn(SOLDIER, WEST); formationDir = SOUTH; break;
        		case SOUTH_WEST: waitSpawn(SOLDIER, NORTH); formationDir = WEST; break;
        		case NORTH_WEST: rc.yield(); move(EAST); waitSpawn(SOLDIER, EAST); formationDir = NORTH; break;
        		}
        		
        		while (Clock.getRoundNum() < 100) {
        			try {
        				tryArchonHealEveryone(10);
        				rc.yield();
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
        		}
        		
        		// work here
        		explore(true);
        		updateMap();
        		
        		if (leader) {        		
	        		formationCenter = rc.getLocation().add(formationDir.opposite());
	        		sendFormationCenter();
	        		rc.yield();
        		}
        		
        		MapLocation lastExplore = rc.getLocation();
        		
        		while (true) {
        			try {
        				MapLocation loc = rc.getLocation();
        				if (!lastExplore.equals(loc)) {
        					explore(false);
        					updateMap();
        				}
        				
        				processMessages(true);
        				
        				tryArchonHealEveryone(40);
        				
        				if (leader && everyoneInFormation()) {
        					
        					removeUpgradesFromUnderFormation();
        					updateMap();
        					
        					formationCenter = getFormationCenter();
        					
        					// work here
        					debug_printMap(formationCenter);
        					System.out.println(formationCenterIsGood(formationCenter));
        					
//        					if (formationCenter == null) {
//        						System.out.println("bad bad");
//        					} else {
//        						System.out.println(dir(formationCenter));
//        					}
        					
        					
        					sendFormationCenter();
        				}        				
        				
        				if (rc.isActive()) {
        					rc.yield();
        				} else {
        					if (formationCenter != null) {
	                    		MapLocation goal = formationCenter.add(formationDir);
	                    		
	            				if (goal.equals(loc)) {
	            					rc.yield();
	            				} else {
	            					if (formationWalkToward(goal)) {
	            					} else {
	            						rc.yield();
	            					}
	            				}
        					} else {
        						rc.yield();
        					}
        				}
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
        		}
    		}
    		if (type == SOLDIER) {
    			waitEvolve(TANK);
    		}
    		if (type == TANK) {
    			{
	    			HashSet<MapLocation> archons = new HashSet<MapLocation>(Arrays.asList(rc.senseAlliedArchons()));
	    			MapLocation loc = rc.getLocation();
	    			for (int i = 1; i < 8; i += 2) {
	    				Direction d = Direction.values()[i];
	    				MapLocation m1 = loc.add(d.rotateLeft());
	    				MapLocation m2 = loc.add(d.rotateRight());
	    				if (archons.contains(m1) && archons.contains(m2)) {
	    					formationDir = d.opposite();
	    					break;
	    				}
	    			}
    			}
    			while (Clock.getRoundNum() < 100) {
    				rc.yield();
    			}
        		
        		while (true) {
        			try {
        				processMessages(false);
        				
        				if (rc.isActive()) {
        					rc.yield();
        				} else {
        					if (formationCenter != null) {
	                    		MapLocation goal = formationCenter.add(formationDir);
	            				MapLocation loc = rc.getLocation();
	            				if (goal.equals(loc)) {
	            					rc.yield();
	            				} else {
	            					if (formationWalkToward(goal)) {
	            					} else {
	            						rc.yield();
	            					}
	            				}
        					} else {
        						rc.yield();
        					}
        				}
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
        		}
    			
//        		while (true) {
//        			try {
//        				if (rc.isActive()) {
//            				rc.yield();
//        				} else {
//        					face(rc.senseEnemyArchon());
//        					Direction d = rc.getDirection();
//        					
//        					MapLocation fire = rc.getLocation();
//        					if (d.isDiagonal()) {
//        						fire = fire.add(d).add(d).add(d).add(rand.nextBoolean() ? d.rotateLeft() : d.rotateRight());
//        					} else {
//        						fire = fire.add(d).add(d).add(d).add(d);
//        					}
//        					rc.queueAttackGround(fire);
//        				}
//        			} catch (Exception e) {
//        				e.printStackTrace();
//        			}
//        		}
    		}
    	} catch (Exception e1) {
			e1.printStackTrace();
		}
    }
}
