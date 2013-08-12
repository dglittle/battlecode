package huns;

import static battlecode.common.Direction.EAST;
import static battlecode.common.Direction.NORTH;
import static battlecode.common.Direction.NORTH_EAST;
import static battlecode.common.Direction.NORTH_WEST;
import static battlecode.common.Direction.OMNI;
import static battlecode.common.Direction.SOUTH;
import static battlecode.common.Direction.SOUTH_EAST;
import static battlecode.common.Direction.SOUTH_WEST;
import static battlecode.common.Direction.WEST;
import static battlecode.common.Direction.NONE;
import static battlecode.common.MapHeight.IN_AIR;
import static battlecode.common.MapHeight.ON_GROUND;
import static battlecode.common.RobotType.ARCHON;
import static battlecode.common.RobotType.EMP;
import static battlecode.common.RobotType.SAM;
import static battlecode.common.RobotType.SCOUT;
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
import java.util.Collection;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
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
    static final int GROUND_LOCS_MSG_TAG = 889;
    static final int AIR_LOCS_MSG_TAG = 23456;
    static final int EMP_MODE_MSG_TAG = 7525;
	public static RobotController rc;
    public static Robot r;
    public static int id;
    public static RobotType type;
    public static Team team;
    public static boolean inAir;
    public static Random rand;
    public static int myMagic;
        
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
        
        myMagic = (team == Team.A) ? 0x729d8298 : 0x471af928;
        
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
    
    public static Direction dirToNearestEnemyArchon() throws Exception {
    	MapLocation loc = rc.getLocation();
    	int bestDist = Integer.MAX_VALUE;
    	MapLocation nearestArchon = null;
    	for (Robot r : rc.senseNearbyGroundRobots()) {
    		if (r.getTeam() != team && rc.senseRobotTypeOf(r) == ARCHON) {
    			MapLocation pos = rc.senseLocationOf(r);
    			int dist = pos.distanceSquaredTo(loc);
    			if (dist < bestDist) {
    				bestDist = dist;
    				nearestArchon = pos;
    			}
    		}
    	}
    	if (nearestArchon == null) {
    		nearestArchon = loc;
    		int x = loc.getX();
    		int y = loc.getY();
    		int[] ds = MyConst.dirDs[rc.senseEnemyArchon().ordinal()];
    		x += ds[0] * 10;
    		y += ds[1] * 10;
    		if (x < mapLeft) x = mapLeft;
    		if (x > mapRight) x = mapRight;
    		if (y < mapTop) y = mapTop;
    		if (y > mapBottom) y = mapBottom;
    		nearestArchon = new MapLocation(x, y);
    	}
    	
    	return dirToSpot(nearestArchon);
    }
    
    public static Direction[] tempDirs = new Direction[2];
    
    public static Direction dirToNearestUpgrade() {
    	StringBuffer buf = new StringBuffer(map);
		
    	// make this archon favor the upgrades in her quadrant
//    	{
//			int leftMost = Integer.MAX_VALUE;
//			int rightMost = Integer.MIN_VALUE;
//			int topMost = Integer.MAX_VALUE;
//			int bottomMost = Integer.MIN_VALUE;
//			for (MapLocation u : upgradeSet) {
//				leftMost = Math.min(leftMost, u.getX());
//				rightMost = Math.max(rightMost, u.getX());
//				topMost = Math.min(topMost, u.getY());
//				bottomMost = Math.max(bottomMost, u.getY());
//			}
//			int xDivide = (leftMost + rightMost) / 2;
//			int yDivide = (topMost + bottomMost) / 2;
//			boolean good = false;
//			for (MapLocation u : upgradeSet) {
//				int x = u.getX() - xDivide;
//				int y = u.getY() - yDivide;
//				if (MyConst.dirDs[archonQuadrant.ordinal()][0] * x >= 0 &&
//						MyConst.dirDs[archonQuadrant.ordinal()][1] * y >= 0) {
//					good = true;
//				} else {
//					setMapSquare(buf, u, 'Y');
//				}
//			}
//			if (!good) {
//				buf = new StringBuffer(map);
//			}
//    	}
		
		setMapSquare(buf, rc.getLocation(), 'x');
    	String s = buf.toString();
    	
    	if (!s.contains("u")) return null;
    	
    	for (int t = 0; t < 40; t++) {
    		if (s.matches(regex(".*(x(?=.{A,C}u)|(?<=u.{A})x|(?<=u.{B})x|(?<=u.{C})x|x(?=u)|(?<=u)x).*"))) {
    			int i = 0;
    			
	    		if (s.matches(regex(".*x(?=.{B}u).*"))) {
	    			tempDirs[i++] = SOUTH;
	    		} else if (s.matches(regex(".*(?<=u.{B})x.*"))) {
	    			tempDirs[i++] = NORTH;
	    		} else if (s.matches(regex(".*x(?=u).*"))) {
	    			tempDirs[i++] = EAST;
	    		} else if (s.matches(regex(".*(?<=u)x.*"))) {
	    			tempDirs[i++] = WEST;
	    		}
	    		
	    		if (s.matches(regex(".*(?<=u.{A})x.*"))) {
	    			tempDirs[i++] = NORTH_EAST;
	    		} else if (s.matches(regex(".*(?<=u.{C})x.*"))) {
	    			tempDirs[i++] = NORTH_WEST;
	    		} else if (s.matches(regex(".*x(?=.{C}u).*"))) {
	    			tempDirs[i++] = SOUTH_EAST;
	    		} else if (s.matches(regex(".*x(?=.{A}u).*"))) {
	    			tempDirs[i++] = SOUTH_WEST;
	    		}
	    		
	    		Direction d0 = tempDirs[0];
	    		if (rc.canMove(d0)) {
	    			return tempDirs[0];
	    		} else if (i > 1) {
	    			Direction d = tempDirs[1];
	    			if (rc.canMove(d)) {
	    				return d;
	    			} else {
	    				if (rc.canMove(d.rotateLeft())) {
	    					return d.rotateLeft();
	    				} else if (rc.canMove(d.rotateRight())) {
	    					return d.rotateRight();
	    				}
	    			}
	    		}
				if (rc.canMove(d0.rotateLeft())) {
					return d0.rotateLeft();
				} else if (rc.canMove(d0.rotateRight())) {
					return d0.rotateRight();
				}	    	
				return null;
    		}
    		
    		String ss = s.replaceAll(regex(" (?=.{A,C}u)|(?<=u.{A}) |(?<=u.{B}) |(?<=u.{C}) | (?=u)|(?<=u) "), "u");
    		if (s.equals(ss)) break;
    		s = ss;
    	}
    	
    	return null;
    }
    
    public static int getLeaderIndex() {
    	return leaderIndex % rc.getUnitCount(ARCHON);
    	//return (round() / 500) % rc.getUnitCount(ARCHON);
    }
    
    public static Direction dirToGrandMother() {
    	StringBuffer buf = new StringBuffer(map);
		
    	MapLocation loc = rc.getLocation();
    	
    	MapLocation pos = rc.senseAlliedArchons()[getLeaderIndex()];
    	if (pos.isAdjacentTo(loc)) {
    		return null;
    	}
		setMapSquare(buf, pos, 'a');
		
		setMapSquare(buf, loc, 'x');
    	String s = buf.toString();
    	
    	if (!s.contains("a")) return null;
    	
    	for (int t = 0; t < 40; t++) {
    		if (s.matches(regex(".*(x(?=.{A,C}a)|(?<=a.{A})x|(?<=a.{B})x|(?<=a.{C})x|x(?=a)|(?<=a)x).*"))) {
    			int i = 0;
    			
	    		if (s.matches(regex(".*x(?=.{B}a).*"))) {
	    			tempDirs[i++] = SOUTH;
	    		} else if (s.matches(regex(".*(?<=a.{B})x.*"))) {
	    			tempDirs[i++] = NORTH;
	    		} else if (s.matches(regex(".*x(?=a).*"))) {
	    			tempDirs[i++] = EAST;
	    		} else if (s.matches(regex(".*(?<=a)x.*"))) {
	    			tempDirs[i++] = WEST;
	    		}
	    		
	    		if (s.matches(regex(".*(?<=a.{A})x.*"))) {
	    			tempDirs[i++] = NORTH_EAST;
	    		} else if (s.matches(regex(".*(?<=a.{C})x.*"))) {
	    			tempDirs[i++] = NORTH_WEST;
	    		} else if (s.matches(regex(".*x(?=.{C}a).*"))) {
	    			tempDirs[i++] = SOUTH_EAST;
	    		} else if (s.matches(regex(".*x(?=.{A}a).*"))) {
	    			tempDirs[i++] = SOUTH_WEST;
	    		}
	    		
	    		Direction d0 = tempDirs[0];
	    		if (rc.canMove(d0)) {
	    			return tempDirs[0];
	    		} else if (i > 1) {
	    			Direction d = tempDirs[1];
	    			if (rc.canMove(d)) {
	    				return d;
	    			} else {
	    				if (rc.canMove(d.rotateLeft())) {
	    					return d.rotateLeft();
	    				} else if (rc.canMove(d.rotateRight())) {
	    					return d.rotateRight();
	    				}
	    			}
	    		}
				if (rc.canMove(d0.rotateLeft())) {
					return d0.rotateLeft();
				} else if (rc.canMove(d0.rotateRight())) {
					return d0.rotateRight();
				}	    		
				return null;
    		}
    		
    		String ss = s.replaceAll(regex(" (?=.{A,C}a)|(?<=a.{A}) |(?<=a.{B}) |(?<=a.{C}) | (?=a)|(?<=a) "), "a");
    		if (s.equals(ss)) break;
    		s = ss;
    	}
    	
    	return null;
    }
    
    public static Direction dirToSpot(MapLocation pos) {
    	StringBuffer buf = new StringBuffer(map);
		
		setMapSquare(buf, pos, 'a');
		setMapSquare(buf, rc.getLocation(), 'x');
    	String s = buf.toString();
    	
    	if (!s.contains("a")) return null;
    	
    	for (int t = 0; t < 40; t++) {
    		if (s.matches(regex(".*(x(?=.{A,C}a)|(?<=a.{A})x|(?<=a.{B})x|(?<=a.{C})x|x(?=a)|(?<=a)x).*"))) {
    			int i = 0;
    			
	    		if (s.matches(regex(".*x(?=.{B}a).*"))) {
	    			tempDirs[i++] = SOUTH;
	    		} else if (s.matches(regex(".*(?<=a.{B})x.*"))) {
	    			tempDirs[i++] = NORTH;
	    		} else if (s.matches(regex(".*x(?=a).*"))) {
	    			tempDirs[i++] = EAST;
	    		} else if (s.matches(regex(".*(?<=a)x.*"))) {
	    			tempDirs[i++] = WEST;
	    		}
	    		
	    		if (s.matches(regex(".*(?<=a.{A})x.*"))) {
	    			tempDirs[i++] = NORTH_EAST;
	    		} else if (s.matches(regex(".*(?<=a.{C})x.*"))) {
	    			tempDirs[i++] = NORTH_WEST;
	    		} else if (s.matches(regex(".*x(?=.{C}a).*"))) {
	    			tempDirs[i++] = SOUTH_EAST;
	    		} else if (s.matches(regex(".*x(?=.{A}a).*"))) {
	    			tempDirs[i++] = SOUTH_WEST;
	    		}
	    		
	    		Direction d0 = tempDirs[0];
	    		if (rc.canMove(d0)) {
	    			return tempDirs[0];
	    		} else if (i > 1) {
	    			Direction d = tempDirs[1];
	    			if (rc.canMove(d)) {
	    				return d;
	    			} else {
	    				if (rc.canMove(d.rotateLeft())) {
	    					return d.rotateLeft();
	    				} else if (rc.canMove(d.rotateRight())) {
	    					return d.rotateRight();
	    				}
	    			}
	    		}
				if (rc.canMove(d0.rotateLeft())) {
					return d0.rotateLeft();
				} else if (rc.canMove(d0.rotateRight())) {
					return d0.rotateRight();
				}	    		
				return null;
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
    	
    	MapLocation loc = rc.getLocation();
    	
    	Message m = new Message();
    	m.locations = locs;
    	if (type == ARCHON && leader) {
	    	m.ints = new int[] {hashRound(Clock.getRoundNum()), 72, leaderIndex, newWalls, newUpgrades, newNogrades,
	    			mapLeft, mapRight, mapTop, mapBottom, 1, loc.getX(), loc.getY(), rc.senseEnemyArchon().ordinal()};
    	} else {
	    	m.ints = new int[] {hashRound(Clock.getRoundNum()), 72, leaderIndex, newWalls, newUpgrades, newNogrades,
	    			mapLeft, mapRight, mapTop, mapBottom, 0};
    	}
    	rc.queueBroadcast(m);
    }
    
    public static void broadcastAirLocations() throws Exception {
        Vector<MapLocation> vScouts = new Vector<MapLocation>();
        Vector<MapLocation> vEmps = new Vector<MapLocation>();
        for (Robot r : rc.senseNearbyAirRobots()) {
            if (r.getTeam() != team) {
                switch (rc.senseRobotTypeOf(r)) {
                case EMP: vEmps.add(rc.senseLocationOf(r)); break;
                case SCOUT: vScouts.add(rc.senseLocationOf(r)); break;
                }
            }
        }
        
        MapLocation[] locs = new MapLocation[vScouts.size() + vEmps.size()];
        System.arraycopy(vScouts.toArray(), 0, locs, 0, vScouts.size());
        System.arraycopy(vEmps.toArray(), 0, locs, vScouts.size(), vEmps.size());

        Message m = new Message();
        m.locations = locs;
        m.ints = new int[] {hashRound(Clock.getRoundNum()), AIR_LOCS_MSG_TAG, vScouts.size(), vEmps.size()};
        rc.queueBroadcast(m);
    }
    
    public static void broadcastGroundLocations() throws Exception {
    	Vector<MapLocation> v = new Vector<MapLocation>();
    	Vector<MapLocation> v2 = new Vector<MapLocation>();
    	for (Robot r : rc.senseNearbyGroundRobots()) {
    		if (r.getTeam() != team) {
    			if (rc.senseRobotTypeOf(r) == ARCHON) {
    				v2.add(rc.senseLocationOf(r));
    			}
				v.add(rc.senseLocationOf(r));
    		}
    	}
    	Message m = new Message();
    	MapLocation[] blah = new MapLocation[v.size() + v2.size()];
    	System.arraycopy(v.toArray(), 0, blah, 0, v.size());
    	System.arraycopy(v2.toArray(), 0, blah, v.size(), v2.size());
    	
    	m.locations = blah;
    	m.ints = new int[] {hashRound(Clock.getRoundNum()), GROUND_LOCS_MSG_TAG, v.size(), v2.size()};
    	rc.queueBroadcast(m);
    }
    
    public static void pingMother() throws Exception {
    	Message m = new Message();
    	m.ints = new int[] {hashRound(round()), 110, motherID, id};
    	rc.queueBroadcast(m);
    }
    
    public static boolean sendGroundEnemies() throws Exception {
    	Vector<MapLocation> locs = new Vector<MapLocation>();
    	for (Robot r : rc.senseNearbyGroundRobots()) {
    		if (r.getTeam() != team) {
    			locs.add(rc.senseLocationOf(r));
    		}
    	}
    	
    	if (locs.size() == 0) return false;
    	
    	Message m = new Message();
    	m.ints = new int[] {hashRound(round()), 112, id};
    	m.locations = locs.toArray(new MapLocation[locs.size()]);
    	rc.queueBroadcast(m);
    	return true;
    }
    
    public static HashSet<MapLocation> badGuys = new HashSet<MapLocation>(); 
    public static HashSet<MapLocation> badGuysA = new HashSet<MapLocation>(); 
    
    public static HashSet<MapLocation> badScouts = new HashSet<MapLocation>(); 
    public static HashSet<MapLocation> badEmps = new HashSet<MapLocation>(); 
    
    public static int badGuyMessageCount = 0;
    
    public static void processMessages(boolean doMap) {
    	int roundHash = hashRound(Clock.getRoundNum() - 1);
    	while (true) {
        	Message m = rc.getNextMessage();
        	if (m == null) break;
        	
        	if (m.ints != null && m.ints.length >= 2) {
        		if (m.ints[0] == roundHash) {
        			
        			if (m.ints[1] == 112 && motherID == m.ints[2]) {
        				groundEnemies = m.locations;
        				lastGroundEnemiesUpdate = round();
        			}
        			
        			if (m.ints[1] == 110) {
        				if (id == m.ints[2] && childID == m.ints[3]) {
        					lastHeardFromChild = round();
        				}
        			}
        			
        			if (m.ints[1] == GROUND_LOCS_MSG_TAG && type == TANK) {
        				{
        					MapLocation[] blah = new MapLocation[m.ints[2]];
        					System.arraycopy(m.locations, 0, blah, 0, m.ints[2]);
        					badGuys.addAll(Arrays.asList(blah));
        				}
        				{
        					MapLocation[] blah = new MapLocation[m.ints[3]];
        					System.arraycopy(m.locations, m.ints[2], blah, 0, m.ints[3]);
        					badGuysA.addAll(Arrays.asList(blah));
        				}
        			}
        			
                    if (m.ints[1] == AIR_LOCS_MSG_TAG && type == SAM) {
                        MapLocation[] scouts = new MapLocation[m.ints[2]];
                        System.arraycopy(m.locations, 0, scouts, 0, m.ints[2]);
                        badScouts.addAll(Arrays.asList(scouts));

                        MapLocation[] emps = new MapLocation[m.ints[3]];
                        System.arraycopy(m.locations, m.ints[2], emps, 0, m.ints[3]);
                        badEmps.addAll(Arrays.asList(emps));
                    }

                    if (m.ints[1] == 72 && doMap) {
        				int i = 2;
        				leaderIndex = Math.max(leaderIndex, m.ints[i++]);
        		    	int newWalls = m.ints[i++];
        		    	int newUpgrades = m.ints[i++];
        		    	int newNogrades = m.ints[i++];
        		    	
        		    	updateMapLeft(m.ints[i++]);
        		    	updateMapRight(m.ints[i++]);
        		    	updateMapTop(m.ints[i++]);
        		    	updateMapBottom(m.ints[i++]);
        		    	
        		    	if (doMap) {
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
    }
    
    public static void debug_printMap() {
    	StringBuffer buf = new StringBuffer(map);
		setMapSquare(buf, rc.getLocation(), 'x');
    	String s = buf.toString();   	
    	
    	System.out.println();
    	System.out.print(s.replaceAll(" ", ".").replaceAll("\\$", "\n"));
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
    
    public static void move() {
    	rc.queueMoveForward();
    	rc.yield();
    }
    
    public static void move(Direction dir) throws Exception {
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
    
    public static boolean trySpawnRobotNotOnUpgrade(RobotType t) throws Exception {
    	boolean inAir = t.isAirborne();
    	if (inAir) return trySpawnRobot(t);
		Direction currentDir = rc.getDirection();
		MapLocation loc = rc.getLocation();
		MapLocation forward = loc.add(currentDir);
    	if (empty(inAir, forward) && rc.senseUpgradeAtLocation(forward) == null) {
    		rc.queueSpawn(t);
    		rc.yield();
    		return true;
    	} else {
    		Direction d = currentDir.rotateRight();
    		do {
    			forward = loc.add(d);
				if (empty(inAir, forward) && rc.senseUpgradeAtLocation(forward) == null) {
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
    
    public boolean scoutTryAttack() throws Exception {
    	MapLocation loc = rc.getLocation();
		for (Robot r : rc.senseNearbyAirRobots()) {
			if (r.getTeam() != team) {
				MapLocation pos = rc.senseLocationOf(r);
				if (pos.isAdjacentTo(loc)) {
					if (rc.canAttackSquare(pos)) {
						rc.queueAttackAir(pos);
					} else {
						face(dir(pos));
						rc.queueAttackAir(pos);
					}
					rc.yield();
					return true;
				}
			}
		}
		return false;
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
    
    public static Direction dir(MapLocation to) {
    	return dir(rc.getLocation(), to);
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
    
    public static final MapLocation[] neighborLocs = new MapLocation[16];
    public static final MapHeight[] neighborHeights = new MapHeight[16];
    public static final double[] neighborCapacitys = new double[16];
    
    public static void doHealing() throws Exception {
    	MapLocation loc = rc.getLocation();
    	
    	if (type == ARCHON) {
    		
    		// work here
    		double toTanks = 0;
    		double toScouts = 0;
    		double toArchons = 0;
    		
    		try {
	    		for (Robot r : rc.senseNearbyGroundRobots()) {
	    			if (r.getTeam() == team) {
	    				MapLocation pos = rc.senseLocationOf(r);
	    				RobotType t = rc.senseRobotTypeOf(r);
	    				if (loc.isAdjacentTo(pos)) {
		    	    		double energy = rc.getEnergonLevel();
		    				double eventual = rc.senseEventualEnergonOf(r);
		    				double max = rc.senseMaxEnergonOf(r);
		    				if (energy > eventual && eventual < max) {
			    				double a = eventual - rc.senseEnergonLevelOf(r);
			    				if (a < 2) {
			    					double b = 2 - a;			    					
			    					if (eventual + b > rc.senseMaxEnergonOf(r)) {
			    						b = rc.senseMaxEnergonOf(r) - eventual;
			    					}
			    					rc.transferEnergon(b, rc.senseLocationOf(r), ON_GROUND);
			    					
			    					// work here
			    					if (t == ARCHON) {
			    						toArchons += b;
			    					} else if (t == TANK) {
			    						toTanks += b;
			    					}
			    				}
		    				}
	    				}
	    			}
	    		}
    		} catch (Exception e){ 
    			System.out.println("blah1");
    		}
    		
    		try {
    			
    			final double keepThisMuch = rc.getMaxEnergonLevel() - 20; 
    			
	    		double energy = rc.getEnergonLevel();
	    		if (energy > keepThisMuch) {
			    	int nextIndex = 0;
			    	double desired = 0;
			    	
			    	for (Robot r : rc.senseNearbyAirRobots()) {
			    		if (r.getTeam() == team) {
							MapLocation loc2 = rc.senseLocationOf(r);
				    		if (loc.isAdjacentTo(loc2)) {
				    			double capacity = rc.senseMaxEnergonOf(r) - rc.senseEventualEnergonOf(r);
				    			if (capacity > 0.001) {
					    			desired += capacity;
					    			neighborLocs[nextIndex] = loc2;
					    			neighborCapacitys[nextIndex] = capacity;
					    			nextIndex++;
				    			}
				    		}
			    		}
			    	}
			    	
			    	
			    	double fraction = Math.min(1, (energy - keepThisMuch) / desired);
			    	
			    	// work here
			    	toScouts += fraction * desired;
			    	
					for (int i = 0; i < nextIndex; i++) {
						rc.transferEnergon(neighborCapacitys[i] * fraction, neighborLocs[i], IN_AIR);
					}
	    		}
    		} catch (Exception e){ 
    			System.out.println("blah2" + e);
    		}
    		
    		
    		
	    	// work here
			rc.setIndicatorString(2, String.format("T:%.3f, A:%.3f, S:%.3f", toTanks, toArchons, toScouts));
	    	
	    	
    		
    		
			
    	} else if (type == TANK) {
    		{
	    		for (Robot r : rc.senseNearbyGroundRobots()) {
	    			if (r.getTeam() == team) {
	    				MapLocation pos = rc.senseLocationOf(r);
	    				if (loc.isAdjacentTo(pos)) {
		    	    		double energy = rc.getEnergonLevel();
		    				double eventual = rc.senseEventualEnergonOf(r);
		    				if (rc.senseRobotTypeOf(r) == ARCHON) {
			    				if (energy / 2 > eventual) {
				    				double a = rc.senseEventualEnergonOf(r) - rc.senseEnergonLevelOf(r);
				    				if (a < 2) {
				    					double b = 2 - a;			    					
				    					if (eventual + b > rc.senseMaxEnergonOf(r)) {
				    						b = rc.senseMaxEnergonOf(r) - eventual;
				    					}
				    					rc.transferEnergon(b, rc.senseLocationOf(r), ON_GROUND);
				    				}
			    				}
		    				} else {
			    				if (energy > eventual) {
				    				double a = rc.senseEventualEnergonOf(r) - rc.senseEnergonLevelOf(r);
				    				if (a < 2) {
				    					double b = 2 - a;			    					
				    					if (eventual + b > rc.senseMaxEnergonOf(r)) {
				    						b = rc.senseMaxEnergonOf(r) - eventual;
				    					}
				    					rc.transferEnergon(b, rc.senseLocationOf(r), ON_GROUND);
				    				}
			    				}
		    				}
	    				}
	    			}
	    		}
    		}
    		
    	} else if (type == SCOUT) {
    		
    	}
    	
    }
    
    public static MapLocation nearestMother() {
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
    
    public static boolean tryMove(Direction d) throws Exception {
    	if (d == null) {
    		return false;
    	}
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
    
    public static boolean tryMoveNotTooHard(Direction d) throws Exception {
    	if (d == null) {
    		return false;
    	}
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
    	return false;
    }
    
    public static boolean tankMoveNoUpgrade(Direction d) throws Exception {
    	if (rc.canMove(d)) {
    		face(d);
	    	if (rc.senseUpgradeAtLocation(rc.getLocation().add(d)) == null) {
	    		move(d);
	    		return true;
	    	}
    	}
    	return false;
    }
    
    public static boolean tryMoveNotTooHard_notOnUpgrade(Direction d) throws Exception {
    	if (d == null) {
    		return false;
    	}
    	if (tankMoveNoUpgrade(d)) {
    		return true;
    	} else if (tankMoveNoUpgrade(d.rotateRight())) {
    		return true;
    	} else if (tankMoveNoUpgrade(d.rotateLeft())) {
    		return true;
    	}
    	return false;
    }
    
    public static boolean tryKillBestGroundEnemy() {
    	MapLocation loc = rc.getLocation();
    	MapLocation bestLoc = null;
    	for (MapLocation pos : groundEnemies) {
    		int dist = loc.distanceSquaredTo(pos);
    		if (dist >= 9 && dist <= 25) {
    			bestLoc = pos;
    			break;
    		} else if (dist >= 4) {
    			bestLoc = pos.add(dir(pos));
    		} else if (dist <= 36) {
    			bestLoc = pos.subtract(dir(pos));
    		}
    	}
    	
    	if (bestLoc != null) {
    		return tryAttackGround(bestLoc);
    	} else {
    		return false;
    	}
    }
    
    public static boolean tryAttackGround(MapLocation pos) {
    	if (rc.canAttackSquare(pos)) {
    		rc.queueAttackGround(pos);
    		rc.yield();
    		return true;
    	} else {
    		face(dir(pos));
        	if (rc.canAttackSquare(pos)) {
        		rc.queueAttackGround(pos);
        		rc.yield();
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
    
    public static int round() {
    	return Clock.getRoundNum();
    }
    
    public static MapLocation forward() {
    	return rc.getLocation().add(rc.getDirection());
    }
    
    public static boolean tankBlindAttack(Direction d, int dist) {
    	face(d);
    	MapLocation pos = rc.getLocation();
		for (int i = 0; i < dist - 1; i++) {
			pos = pos.add(d);
		}
    	if (d.isDiagonal()) {
    		pos = pos.add(rand.nextBoolean() ? d.rotateLeft() : d.rotateRight());
    	} else {
    		pos = pos.add(d);
    	}
    	if (rc.canAttackSquare(pos)) {
    		rc.queueAttackGround(pos);
    		rc.yield();
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public static boolean tryAttackArchon() {
    	MapLocation us = rc.getLocation();
    	MapLocation loc = null;
    	int dist = 0;
    	for (MapLocation pos : badGuysA) {
    		int d = us.distanceSquaredTo(pos);
    		if (d >= 9 && d <= 25) {
    			if (loc == null || d < dist) {
    				dist = d;
    				loc = pos;
    			}
    		}
    	}
    	if (loc != null) {
    		return tryAttackGround(loc);
    	}
    	return false;
    }
    
    public static void tankAttack() {
    	if (!tryAttackArchon()) {
        	MapLocation us = rc.getLocation();
        	MapLocation loc = null;
        	int dist = 0;
        	boolean someoneClose = false;
        	MapLocation guyClose = null;
        	for (MapLocation pos : badGuys) {
        		int d = us.distanceSquaredTo(pos);
        		if (d >= 9 && d <= 25) {
        			if (loc == null || d < dist) {
        				dist = d;
        				loc = pos;
        			}
        		}
        		if (d < 9) {
        			someoneClose = true;
        			guyClose = pos;
        		}
        	}
        	if (loc != null) {
        		tryAttackGround(loc);
        	} else {
        		if (someoneClose) {
        			tankBlindAttack(dir(guyClose), 3);
        		} else {
        			tankBlindAttack(rc.senseEnemyArchon(), 5);
        		}
        	}
    	}
    }
    
    static boolean samAttack() throws Exception {
        // TODO incorporate our own senses? (to choose targets? to verify them?)
        // TODO how to prioritize targets? moving? etc.
        HashSet<Collection<MapLocation>> badCols = new HashSet<Collection<MapLocation>>();
        badCols.add(badEmps);
        badCols.add(badScouts);
        MapLocation closest = null;
        int closestDist = Integer.MAX_VALUE;
        for (Collection<MapLocation> bads : badCols) {
            for (MapLocation bad : bads) {
                int dist = rc.getLocation().distanceSquaredTo(bad);
                if (dist <= type.attackRadiusMaxSquared()) {
                    if (!rc.canAttackSquare(bad)) {
                        face(dir(bad));
                    }
                    rc.queueAttackAir(bad);
                    return true;
                }
                if (closestDist < dist) {
                    closestDist = dist;
                    closest = bad;
                }
            }
        }
        if (closest != null) {
            // TODO attack
            tryMove(dir(closest));
        } else {
            // this means enemy list was empty
        }
        return false;
    }
    
    public static boolean isTakenAir(MapLocation pos) throws Exception {
    	if (rc.canSenseSquare(pos)) {
    		if (rc.senseAirRobotAtLocation(pos) != null) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public static void scoutHeal(MapLocation base) throws Exception {
    	MapLocation loc = rc.getLocation();
    	int dist = base.distanceSquaredTo(loc);
        Direction startDir = dir(base);
    	Direction d = startDir;
        Vector<Robot> scouts = new Vector<Robot>();
        scan:
    	while (true) {
    		MapLocation pos = loc.add(d);
            if (rc.canSenseSquare(pos)) {
                Robot r = rc.senseAirRobotAtLocation(pos);
                if (r != null && r.getTeam() == team) {
                    switch (rc.senseRobotTypeOf(r)) {
                    case EMP:
                    {
                        double canTake = rc.senseMaxEnergonOf(r) - rc.senseEventualEnergonOf(r);
                        double canGive = rc.getEnergonLevel() - 2;
                        if (canGive <= 0) {
                        	break scan;
                        }
                        if (canTake > 0) {
                            // TODO check this behavior
                            rc.transferEnergon(Math.min(canTake, canGive), pos, IN_AIR);
                        }
                    }
                        break;
                    case SCOUT:
                        if (base.distanceSquaredTo(pos) >= dist && rc.getEnergonLevel() > 2) {
                            scouts.add(r);
                        }
                        break;
                    }
                }
            }
    		d = d.rotateLeft();
    		if (d == startDir) break;
    	}
        for (Robot r : scouts) {
            double canTake = rc.senseMaxEnergonOf(r) - rc.senseEventualEnergonOf(r);
            double canGive = rc.getEnergonLevel() - 2;
            if (canGive <= 0) {
                break;
            }
            if (canTake > 0) {
                rc.transferEnergon(Math.min(canTake, canGive), rc.senseLocationOf(r), IN_AIR);
            }
        }
    }
    
    public static Direction archonQuadrant;
    
    public static int childID = Integer.MIN_VALUE;
    public static int motherID = Integer.MIN_VALUE;
    public static MapLocation motherLoc;
	
	public static int lastHeardFromChild = Clock.getRoundNum();
	
	public static MapLocation[] groundEnemies = new MapLocation[0];
	public static int lastGroundEnemiesUpdate = Integer.MIN_VALUE;
	
	public static MapLocation attackGoal = null;
	
	public static boolean leader = false;
	
	public static int leaderIndex = 0;
	
	// emp mode vars
	static final double MIN_EMP_ENERGON = 12;
	static final int TOOMUCH = 100000;
	// scouts take 5 to move so this should be enough time for all scouts to hear it
	static final int EMP_MODE_BACKOFF = 10;
	static int roundHeardEmpMode = 0;
	static boolean shouldEvolveEmp = false;
    
    // directions
    static final String[] dirstrs = new String[] {
        "N", "NE", "E", "SE", "S", "SW", "W", "NW", "-", "O"
    };

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
    		
    		while (true) {
	    		try {
	    			explore(true);
	    			break;
	    		} catch (Exception e) {
	    			rc.yield();
	    		}
    		}
			updateMap();
			
			MapLocation scoutGoal = null;
			boolean sendMessage = false;
			
			boolean attackMode = false;
			
			if (type != SOLDIER) {
				while (rc.isActive()) {
					processMessages(true);
	        		updateMap();
	        		clearNewSets();
					rc.yield();
				}
			}
			
			MapLocation lastBroadcastLoc = rc.getLocation();
			int lastBroadcastTime = Clock.getRoundNum();
			
			int eggReady = Integer.MIN_VALUE;
			
			MapLocation lastLoc = rc.getLocation();
			
			boolean realScout = (round() < 200);
    		
			mainloop:
	        while (true) {
	            try {
	            	
	            	if (type == TANK) {
	            		
	            		MapLocation loc = rc.getLocation();

	            		processMessages(true);
	            		updateMap();
	            		clearNewSets();
	            		
	            		doHealing();
	            		
	            		if (rc.isActive()) {
	            			
	            		} else {
	            			Direction d = dirToGrandMother();
	            			if (!tryMoveNotTooHard_notOnUpgrade(d)) {
	            				
	            				// work here
	            				
            					tankAttack();
	            			}
	            			badGuys.clear();
	            			badGuysA.clear();
	            		}
	            		
        				rc.yield();
	            	}
	            	
                    if (type == SAM) {
                        
                        MapLocation loc = rc.getLocation();

                        processMessages(true);
                        updateMap();
                        clearNewSets();
                        
                        doHealing();
                        
                        if (!rc.isActive()) {
                            Direction d = dirToGrandMother();
                            if (!tryMoveNotTooHard_notOnUpgrade(d)) {
                                // TODO work here
                                samAttack();
                            }
                        }
                        
                        rc.yield();
                    }
                    
	            	if (type == SOLDIER) {
	            		processMessages(true);
	            		updateMap();
	            		clearNewSets();
	            		
	            		if (rc.getEnergonLevel() > type.maxEnergon() - 2) {
	            			evolve(TANK);
	            			continue;
	            		}
	            		
	            		if (rc.isActive()) {
	            		} else {
	            			tryMove(dirToGrandMother());
	            		}
	            		
	            		rc.yield();
	            	}
	            	
	            	if (type == ARCHON) {
	            		MapLocation loc = rc.getLocation();
	            		
	            		leader = loc.equals(rc.senseAlliedArchons()[getLeaderIndex()]);
	            		
		            	rc.setIndicatorString(0, "lead? " + leader + ", energy: " + rc.getEnergonLevel() + "/" + rc.getEventualEnergonLevel());
		            	rc.setIndicatorString(1, "max:" + rc.senseMaxEnergonOf(r) + ", prod: " + rc.senseEnergonProductionOf(r));
		            	
		            	
	            		processMessages(true);
	            		
		            	// send messages
		            	if (!loc.equals(lastBroadcastLoc) || lastBroadcastTime < Clock.getRoundNum() - 10) {
		            		sendInfo(true);
		            		updateMap();
		            		clearNewSets();
		            		rc.yield();
		            		
		            		lastBroadcastTime = Clock.getRoundNum();
		            		lastBroadcastLoc = loc;
		            	}
		            	
		            	if (round() % 7 == 2) {
		            		broadcastGroundLocations();
		            	}
		            	
	            		doHealing();
		            	
	            		if (rc.isActive()) {
	            		} else {

	            			
		            		
		            		// see if we should enter attack mode
		            		if (leader) {
		            			attackMode = false;
		            			
		            			int archonCount = 0;
		            			int tankCount = 0;
		            			int soldierCount = 0;
		            			int empCount = 0;
		            			int scoutCount = 0;
		            			
			            		for (Robot r : rc.senseNearbyGroundRobots()) {
			            			if (r.getTeam() != team) {
			            				RobotType t = rc.senseRobotTypeOf(r);
			            				MapLocation pos = rc.senseLocationOf(r);
			            				int dist = loc.distanceSquaredTo(pos);
			            				if (t == ARCHON && dist <= 16) {
			            					archonCount++;
			            				}
			            				if (t == TANK && dist <= 16) {
			            					tankCount++;
			            				}
			            				if (t == SOLDIER && dist <= 16) {
			            					soldierCount++;
			            				}		            				
			            			}
			            		}
			            		for (Robot r : rc.senseNearbyAirRobots()) {
			            			if (r.getTeam() != team) {
			            				RobotType t = rc.senseRobotTypeOf(r);
			            				MapLocation pos = rc.senseLocationOf(r);
			            				int dist = loc.distanceSquaredTo(pos);
			            				if (t == EMP) {
			            					empCount++;
			            				}
			            				if (t == SCOUT && dist <= 25) {
			            					scoutCount++;
			            				}
			            			}
			            		}
			            		
			            		// work here
//			            		if (archonCount >= 1 || tankCount >= 1 || soldierCount >= 3) {
//			            			attackMode = true;
//			            		}
			            		if (archonCount >= 1 || tankCount >= 1 || soldierCount >= 3 || scoutCount >= 1 || empCount >= 1) {
			            			attackMode = true;
			            		}
			            		
			            		if (attackMode) {
			            			rc.yield();
			            			continue;
			            		}
		            		}
	            			
	            			
	            			// spawn scout, if we can
			            	if (rc.getEnergonLevel() >= rc.getMaxEnergonLevel() - 10) {
			            		
			            		
			            		
			            		// work here : change back!
			            		
			            		if (rc.getUnitCount(SCOUT) < 20) {
			            			if (trySpawnRobot(SCOUT)) continue;
			            		} else if (rc.getUnitCount(SOLDIER) + rc.getUnitCount(TANK) < 4) {
			            			if (trySpawnRobotNotOnUpgrade(SOLDIER)) {
				            			eggReady = Clock.getRoundNum() + 75;
				            			continue;
			            			}
//		            			} else if (rc.getUnitCount(SOLDIER) < 1 || rc.getUnitCount(SAM) < 1) {
//                                    if (trySpawnRobotNotOnUpgrade(SAM)) {
//                                        eggReady = Clock.getRoundNum() + 75;
//                                        continue;
//                                    }
                                }
			            		
			            		// work here
			            		
			            		if (rc.getEnergonLevel() >= rc.getMaxEnergonLevel() - 1) {
			            			if (trySpawnRobot(SCOUT)) continue;
			            		}
			            	}
			            	
			            	if (round() > eggReady) {
			            		Direction d;
			            		if (leader) {
			            			d = dirToNearestUpgrade();
				            		if (d == null) {
				            			d = dirToNearestEnemyArchon();
				            		}
			            		} else {
			            			d = dirToGrandMother();
			            		}
			            		if (leader) {
			            			if (!tryMoveNotTooHard(d) || rc.getLocation().equals(lastLoc)) {
			            				leaderIndex++;
			            			} else {
			            				lastLoc = loc;
			            			}
			            		} else {
			            			tryMove(d);
			            		}
			            		explore(false);
			            	} else {
			            	}
	            		}
	            		rc.yield();
	            	}
	            	
	            	MapLocation loc = rc.getLocation();
	            	
	            	if (type == EMP) {
	            		if (!rc.isActive()) {
                            int startBcc = Clock.getBytecodeNum();
	            			HashSet<MapLocation> mothers = new HashSet<MapLocation>();
                            HashSet<MapLocation> enemies = new HashSet<MapLocation>();
                            boolean clear = true;
                            int enemyDistSq = Integer.MAX_VALUE;
                            for (MapLocation mother : rc.senseAlliedArchons()) {
                                if (mother.distanceSquaredTo(loc) <= 36) {
                                    mothers.add(mother);
                                    if (mother.distanceSquaredTo(loc) <= 25) {
                                        clear = false;
                                    }
                                }
                            }
                            for (Robot r : rc.senseNearbyGroundRobots()) {
                                if (r.getTeam() != team && rc.senseRobotTypeOf(r) == ARCHON) {
                                    MapLocation pos = rc.senseLocationOf(r);
                                    enemyDistSq = Math.min(enemyDistSq, loc.distanceSquaredTo(pos));
                                    enemies.add(pos);
                                }
                            }
                            Direction dirEnemy = rc.senseEnemyArchon();
                            if (dirEnemy == OMNI && clear) {
                                rc.suicide();
                            } else {
                                String ind0 = dirstrs[dirEnemy.ordinal()] + " ";
                                String ind1 = "";
                                int[] weights = new int[Direction.values().length];
                                for (Direction d : Direction.values()) {
                                    if (d == NONE) continue;
                                    int weight = 0;
                                    if (!rc.canMove(d) && d != OMNI) {
//                                        weight = -10000000;
                                    } else {
                                        MapLocation choice = loc.add(d);
                                        ind1 += dirstrs[d.ordinal()] + " ";
                                        for (MapLocation mother : mothers) {
                                            if (mother.distanceSquaredTo(choice) <= 25) {
                                                weight -= 100000 / (2 + choice.distanceSquaredTo(mother));
                                                ind1 += choice.distanceSquaredTo(mother) + " ";
                                            }
                                        }
                                        ind1 += "| ";
                                        for (MapLocation enemy : enemies) {
                                            ind1 += choice.distanceSquaredTo(enemy) + " ";
                                            weight += 1000 / (2 + choice.distanceSquaredTo(enemy));
                                        }
                                        for (Robot r : rc.senseNearbyAirRobots()) {
                                            if (choice.isAdjacentTo(rc.senseLocationOf(r))) {
                                                if (r.getTeam() == team) {
                                                    weight += 10;
                                                } else {
                                                    weight -= 10;
                                                }
                                            }
                                        }
                                        ind1 += "; ";
                                    }
                                    weights[d.ordinal()] = weight;
                                }

                                rc.setIndicatorString(1, ind1);

                                if (enemies.size() == 0) {
                                    weights[dirEnemy.ordinal()] += 1000;
                                    if (dirEnemy != OMNI) {
                                        weights[dirEnemy.rotateLeft().ordinal()] += 100;
                                        weights[dirEnemy.rotateRight()
                                                .ordinal()] += 100;
                                    }
                                }
                                Direction dmax = null;
                                int wmax = Integer.MIN_VALUE;
                                for (Direction d : Direction.values()) {
                                    if (d == NONE) continue;
                                    int w = weights[d.ordinal()];
                                    ind0 += dirstrs[d.ordinal()] + " " + w + " ";
                                    if (w > wmax && (rc.canMove(d) || d == OMNI)) {
                                        dmax = d;
                                        wmax = w;
                                    }
                                }
                                
                                ind0 += "!" + dirstrs[dmax.ordinal()];
                                rc.setIndicatorString(0, startBcc + ".." + Clock.getBytecodeNum() + " " + ind0);
                                if (dmax != OMNI) {
                                    move(dmax);
                                } else if (clear && enemyDistSq <= 4) {
                                    // this should be considerable damage; should take the opportunity
                                    rc.suicide();
                                }
                            }
	            		}
	            		rc.yield();
	            	}
	            	
	            	if (type == SCOUT) {
	            		
	            		{
		            		int startBcc = Clock.getBytecodeNum();
		            		
		            		// get emp mode messages
		            		Message[] msgs = rc.getAllMessages();
	            	    	int roundHash = hashRound(Clock.getRoundNum() - 1);
	            	    	try {
		            	    	for (Message m : msgs) {
		            	    		if (m.ints != null && m.ints.length == 3 &&
		            	    				m.ints[0] == roundHash && m.ints[1] == EMP_MODE_MSG_TAG) {
		            	    			roundHeardEmpMode = Clock.getRoundNum();
		            	    			if (m.ints[2] == id) {
			            	    			shouldEvolveEmp = true;
			            	    			break;
		            	    			}
		            	    		}
		            	    	}
	            	    	} catch (Exception ex) {
	            	    		// TODO make this actually visible
	            	    		rc.setIndicatorString(0, Clock.getRoundNum() + " EXCEPTION!");
	            	    	}
	        				rc.setIndicatorString(0,
	        						" roundHeard " + roundHeardEmpMode +
	        						" byte " + startBcc + ".." + Clock.getBytecodeNum());
	            		}
                        
	            		if (sendMessage) {
	            			MapLocation goal = nearestArchonLocation();
	            			if (loc.distanceSquaredTo(goal) <= 64) {
	            				sendInfo(false);
	            				clearNewSets();
	            				sendMessage = false;
	            				realScout = false;
	            			}
	            		}
	            		
	            		if (realScout) {
		            		if (scoutGoal == null) {
		            			if (rc.getEnergonLevel() > type.maxEnergon() - 1) {
		            				int locX = loc.getX();
		            				int locY = loc.getY();
		            				
		            				double angle = (rc.senseEnemyArchon().ordinal() - 2) * (Math.PI / 4.0);
		            				angle += rand.nextGaussian() * (2.0 * Math.PI);
		            				double mag = 40;
		            				
		            				scoutGoal = new MapLocation((int)(locX + mag * Math.cos(angle)), (int)(locY + mag * Math.sin(angle)));
		            			}
		            		} else {
		            			
		            			boolean runAway = false;
		        				for (Robot r : rc.senseNearbyAirRobots()) {
		        					if (r.getTeam() != team &&
                                            loc.distanceSquaredTo(rc.senseLocationOf(r)) <= 4) {
	        							runAway = true;
	        							break;
		        					}
		        				}
		        				
		        				if (runAway) {
		        					realScout = false;
		        					sendMessage = true;
		        					continue;
		        				}
		            			
		            			if (rc.getEnergonLevel() < type.maxEnergon() / 2) {
		            				scoutGoal = null;
		            				sendMessage = true;
		            			}
		            		}
		            		
		            		if (!rc.isActive()) {
		            			MapLocation goal = scoutGoal == null ?
                                        nearestArchonLocation() : scoutGoal;
		            			tryMove(dir4(goal));
			            		explore(false);
		            		}
		            		
		            		rc.yield();
		            		continue;
	            		}
	            		
	            		
	            		int maxDist = 49;
	            		
	            		if (!rc.isActive()) {
	            			if (shouldEvolveEmp && rc.getEnergonLevel() > EMP.spawnCost()) {
	            				// TODO duplicate enemy weighing scan, just to be sure?
		            			for (MapLocation mother : rc.senseAlliedArchons()) {
		            				if (mother.distanceSquaredTo(loc) <= 25) {
		            					shouldEvolveEmp = false;
		            					break;
		            				}
		            			}
	            				if (shouldEvolveEmp) {
	            					evolve(EMP);
	            					continue mainloop;
	            					// TODO reintroduce and fix this?
//		            				int ourWeight = 0, enemyWeight = 0;
//		            				scan:
//			            				for (Robot r : rc.senseNearbyAirRobots()) {
//			            					if (r.getTeam() == team) {
//			            						switch (rc.senseRobotTypeOf(r)) {
//			            						case SCOUT: ourWeight++; break;
//			            						case EMP: enemyWeight = TOOMUCH; break scan;
//			            						}
//			            					} else {
//			            						switch (rc.senseRobotTypeOf(r)) {
//			            						case SCOUT: enemyWeight++; break;
//			            						case EMP: enemyWeight = TOOMUCH; break scan;
//			            						}
//			            					}
//			            				}
//		            				if (ourWeight >= enemyWeight * 2 && ourWeight >= 5) {
//		            					evolve(EMP);
//		            					continue mainloop;
//		            				} else {
//		            					shouldEvolveEmp = false;
//		            				}
	            				}
	            			}
	            			
		            		Direction toEnemy = rc.senseEnemyArchon();
		            		MapLocation base = nearestMother();
		            		
		            		MapLocation towardEnemy = loc.add(toEnemy);
		            		if (base.distanceSquaredTo(towardEnemy) > maxDist || loc.equals(towardEnemy)) {
		            			towardEnemy = null;
		            		}
		            		
	        				int startBcc = Clock.getBytecodeNum();
	        				// emp mode code
		            		// TODO add check to see if we're on front lines?
	        				if (Clock.getRoundNum() - roundHeardEmpMode > EMP_MODE_BACKOFF)
	        				{
			            		int enemyWeight = 0;
			            		int ourWeight = 0;
		        				MapLocation archonLoc = null;
		        				search:
		        				for (Robot r : rc.senseNearbyGroundRobots()) {
		        					if (r.getTeam() != team) {
		        						switch (rc.senseRobotTypeOf(r)) {
		        						case SAM: enemyWeight += 5; break;
		        						case ARCHON: archonLoc = rc.senseLocationOf(r); break;
		        						}
		        					} else {
		        						if (rc.senseRobotTypeOf(r) == ARCHON) {
		        							enemyWeight = TOOMUCH;
		        							break search;
		        						}
		        					}
		        				}
		        				rc.setIndicatorString(1,
		        						Clock.getRoundNum() +
		        						" enemy " + enemyWeight +
		        						" found " + (archonLoc != null) +
		        						" byte " + startBcc + ".." + Clock.getBytecodeNum());
		        				if (enemyWeight < TOOMUCH && archonLoc != null) {
		        					Robot candidate = null;
		        					double candidateE = MIN_EMP_ENERGON;
			        				search: // more tallying + emp candidate search
			        				for (Robot r : rc.senseNearbyAirRobots()) {
			        					if (r.getTeam() != team) {
											switch (rc.senseRobotTypeOf(r)) {
											case SCOUT:
												enemyWeight++;
												break;
											case EMP:
												enemyWeight = TOOMUCH;
												break search;
											}
			        					} else {
											switch (rc.senseRobotTypeOf(r)) {
											case SCOUT:
												ourWeight++;
												// look for emp candidate
												// TODO make this more sophisticated? eg:
												// - will stop sooner (incorporated this now)
												// - farther away from parent
												// - father away from enemy (behind front line)
												// - not surrounded by enemies
												// - surrounded by healthy friendlies
												double e = rc.senseEnergonLevelOf(r) - rc.senseRoundsUntilIdle(r);
												if (e > candidateE) {
													// choose someone not too close to any mother
													// TODO bug must be here!
													boolean nearMother = false;
													MapLocation rloc = rc.senseLocationOf(r);
													for (MapLocation mother : rc.senseAlliedArchons()) {
														if (rloc.distanceSquaredTo(mother) <= 25) {
															nearMother = true;
															break;
														}
													}
													if (!nearMother) {
														candidate = r;
														candidateE = e;
													}
												}
												break;
											case EMP:
												enemyWeight = TOOMUCH;
												break search;
											}
										}
			        				}
			        				rc.setIndicatorString(2,
			        						Clock.getRoundNum() +
			        						(candidate == null ? "" : " cand " + candidate.getID() + "!") +
			        						" enemy " + enemyWeight +
			        						" us " + ourWeight +
			        						" found " + (archonLoc != null) +
			        						" candE " + candidateE +
			        						" byte " + Clock.getBytecodeNum());
			        				if (candidate != null && ourWeight >= 5 &&
			        						ourWeight >= 2 * enemyWeight && archonLoc != null) {
			        					// send message announcing EMP mode
			        					Message m = new Message();
			        					m.ints = new int[] {
			        							hashRound(Clock.getRoundNum()),
			        							EMP_MODE_MSG_TAG,
			        							candidate.getID() };
			        					try {
			        						rc.queueBroadcast(m);
			        					} catch (GameActionException ex) {
			        					}
			        					roundHeardEmpMode = Clock.getRoundNum();
			        				}
		        				}
	        				}
	        				
                            int bestScoutDist = Integer.MAX_VALUE;
                            MapLocation bestScout = null;
                            int bestEMPDist = Integer.MAX_VALUE;
                            MapLocation bestEMP = null;
                            for (Robot r : rc.senseNearbyAirRobots()) {
                                if (r.getTeam() != team) {
                                    RobotType t = rc.senseRobotTypeOf(r);
                                    MapLocation pos = rc.senseLocationOf(r);
                                    int dist = loc.distanceSquaredTo(pos);
                                    if (t == SCOUT) {
                                        if (dist < bestScoutDist && base.distanceSquaredTo(pos) <= maxDist) {
                                            bestScoutDist = dist;
                                            bestScout = pos;
                                        }
                                    } else {
                                        if (dist < bestEMPDist) {
                                            bestEMPDist = dist;
                                            bestEMP = pos;
                                        }
                                    }
                                }
                            }
                            
	        				scoutHeal(base);
	        				
	        				if (rc.getEnergonLevel() > 5) {	        				
		        				while (true) {
			        				if (bestEMP != null) {
			        					if (loc.isAdjacentTo(bestEMP)) {
			        						if (tryAttackAir(bestEMP)) break;
			        					}
			        					if (tryMove(dir(bestEMP))) break;
			        				}
			        				// TODO is the combination of the following
									// two things (moving back to base and
									// moving toward a scout) a good idea? it
									// seems like we could have ships just
									// bouncing back and forth toward/from a
									// nearby enemy
		        					if (loc.distanceSquaredTo(base) > maxDist) {
		        						if (tryMove(dir(base))) break;
		        					}
			        				if (bestScout != null) {
			        					if (loc.isAdjacentTo(bestScout)) {
			        						if (tryAttackAir(bestScout)) break;
			        					}
			        					if (tryMove(dir(bestScout))) break;
			        				}
			        				if (towardEnemy != null) {
			        					if (tryMove(toEnemy)) break;
			        				}
			        				scoutTryAttack();
			        				break;
		        				}
	        				} else {
	        					if (bestEMP != null && loc.isAdjacentTo(bestEMP)) {
	        						tryAttackAir(bestEMP);
	        					} else {
	        						tryMove(dir(base));
	        					}
	        				}
	            		}
	            		
	            		rc.yield();
	            	}
	            	
	            } catch(Exception e) {
	                System.out.println("caught exception:");
	                e.printStackTrace();
	            }
	        }
		} catch (Exception e1) {
            System.out.println("caught exception2:");
            e1.printStackTrace();
		}
    }
}
