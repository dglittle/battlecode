package sammies;

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
    
    
    
    
    
    
    
    
    
    
    
    static MapLocation[][] wallLocs = { 
    	new MapLocation[] {new MapLocation(-5,0),
    	new MapLocation(-5,1),
    	new MapLocation(-5,2),
    	new MapLocation(-5,3),
    	new MapLocation(-4,3),
    	new MapLocation(-4,4),
    	new MapLocation(-3,4),
    	new MapLocation(-3,5),
    	new MapLocation(-2,5),
    	new MapLocation(-1,5),
    	new MapLocation(0,5),
    	new MapLocation(1,5),
    	new MapLocation(2,5),
    	new MapLocation(3,5),
    	new MapLocation(3,4),
    	new MapLocation(4,4),
    	new MapLocation(4,3),
    	new MapLocation(5,3),
    	new MapLocation(5,2),
    	new MapLocation(5,1),
    	new MapLocation(5,0)},
    	new MapLocation[] {new MapLocation(-4,4),
    	new MapLocation(-3,4),
    	new MapLocation(-3,5),
    	new MapLocation(-2,5),
    	new MapLocation(-1,5),
    	new MapLocation(0,5),
    	new MapLocation(1,5),
    	new MapLocation(2,5),
    	new MapLocation(3,5),
    	new MapLocation(3,4),
    	new MapLocation(4,4),
    	new MapLocation(4,3),
    	new MapLocation(5,3),
    	new MapLocation(5,2),
    	new MapLocation(5,1),
    	new MapLocation(5,0),
    	new MapLocation(5,-1),
    	new MapLocation(5,-2),
    	new MapLocation(5,-3),
    	new MapLocation(4,-3),
    	new MapLocation(4,-4)},
    	new MapLocation[] {new MapLocation(0,5),
    	new MapLocation(1,5),
    	new MapLocation(2,5),
    	new MapLocation(3,5),
    	new MapLocation(3,4),
    	new MapLocation(4,4),
    	new MapLocation(4,3),
    	new MapLocation(5,3),
    	new MapLocation(5,2),
    	new MapLocation(5,1),
    	new MapLocation(5,0),
    	new MapLocation(5,-1),
    	new MapLocation(5,-2),
    	new MapLocation(5,-3),
    	new MapLocation(4,-3),
    	new MapLocation(4,-4),
    	new MapLocation(3,-4),
    	new MapLocation(3,-5),
    	new MapLocation(2,-5),
    	new MapLocation(1,-5),
    	new MapLocation(0,-5)},
    	new MapLocation[] {new MapLocation(4,4),
    	new MapLocation(4,3),
    	new MapLocation(5,3),
    	new MapLocation(5,2),
    	new MapLocation(5,1),
    	new MapLocation(5,0),
    	new MapLocation(5,-1),
    	new MapLocation(5,-2),
    	new MapLocation(5,-3),
    	new MapLocation(4,-3),
    	new MapLocation(4,-4),
    	new MapLocation(3,-4),
    	new MapLocation(3,-5),
    	new MapLocation(2,-5),
    	new MapLocation(1,-5),
    	new MapLocation(0,-5),
    	new MapLocation(-1,-5),
    	new MapLocation(-2,-5),
    	new MapLocation(-3,-5),
    	new MapLocation(-3,-4),
    	new MapLocation(-4,-4)},
    	new MapLocation[] {new MapLocation(5,0),
    	new MapLocation(5,-1),
    	new MapLocation(5,-2),
    	new MapLocation(5,-3),
    	new MapLocation(4,-3),
    	new MapLocation(4,-4),
    	new MapLocation(3,-4),
    	new MapLocation(3,-5),
    	new MapLocation(2,-5),
    	new MapLocation(1,-5),
    	new MapLocation(0,-5),
    	new MapLocation(-1,-5),
    	new MapLocation(-2,-5),
    	new MapLocation(-3,-5),
    	new MapLocation(-3,-4),
    	new MapLocation(-4,-4),
    	new MapLocation(-4,-3),
    	new MapLocation(-5,-3),
    	new MapLocation(-5,-2),
    	new MapLocation(-5,-1),
    	new MapLocation(-5,0)},
    	new MapLocation[] {new MapLocation(4,-4),
    	new MapLocation(3,-4),
    	new MapLocation(3,-5),
    	new MapLocation(2,-5),
    	new MapLocation(1,-5),
    	new MapLocation(0,-5),
    	new MapLocation(-1,-5),
    	new MapLocation(-2,-5),
    	new MapLocation(-3,-5),
    	new MapLocation(-3,-4),
    	new MapLocation(-4,-4),
    	new MapLocation(-4,-3),
    	new MapLocation(-5,-3),
    	new MapLocation(-5,-2),
    	new MapLocation(-5,-1),
    	new MapLocation(-5,0),
    	new MapLocation(-5,1),
    	new MapLocation(-5,2),
    	new MapLocation(-5,3),
    	new MapLocation(-4,3),
    	new MapLocation(-4,4)},
    	new MapLocation[] {new MapLocation(0,-5),
    	new MapLocation(-1,-5),
    	new MapLocation(-2,-5),
    	new MapLocation(-3,-5),
    	new MapLocation(-3,-4),
    	new MapLocation(-4,-4),
    	new MapLocation(-4,-3),
    	new MapLocation(-5,-3),
    	new MapLocation(-5,-2),
    	new MapLocation(-5,-1),
    	new MapLocation(-5,0),
    	new MapLocation(-5,1),
    	new MapLocation(-5,2),
    	new MapLocation(-5,3),
    	new MapLocation(-4,3),
    	new MapLocation(-4,4),
    	new MapLocation(-3,4),
    	new MapLocation(-3,5),
    	new MapLocation(-2,5),
    	new MapLocation(-1,5),
    	new MapLocation(0,5)},
    	new MapLocation[] {new MapLocation(-4,-4),
    	new MapLocation(-4,-3),
    	new MapLocation(-5,-3),
    	new MapLocation(-5,-2),
    	new MapLocation(-5,-1),
    	new MapLocation(-5,0),
    	new MapLocation(-5,1),
    	new MapLocation(-5,2),
    	new MapLocation(-5,3),
    	new MapLocation(-4,3),
    	new MapLocation(-4,4),
    	new MapLocation(-3,4),
    	new MapLocation(-3,5),
    	new MapLocation(-2,5),
    	new MapLocation(-1,5),
    	new MapLocation(0,5),
    	new MapLocation(1,5),
    	new MapLocation(2,5),
    	new MapLocation(3,5),
    	new MapLocation(3,4),
    	new MapLocation(4,4)}
    	};

    	    public static MapLocation empWallGetLocation(int index) {
    	    	
    	    	index += 21 / 4 - 1;
    	    	
    	    	MapLocation ctr = empWallCenter;
    	    	Direction dir = empWallDirection; 
    	        MapLocation wallLoc = wallLocs[dir.ordinal()][index];
    	        return new MapLocation(ctr.getX() + wallLoc.getX(), ctr.getY() - wallLoc.getY());
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
    
    public static boolean broadcastAirLocations() throws Exception {
    	Vector<MapLocation> v = new Vector<MapLocation>();
    	Vector<MapLocation> v2 = new Vector<MapLocation>();
    	for (Robot r : rc.senseNearbyAirRobots()) {
    		if (r.getTeam() != team) {
    			if (rc.senseRobotTypeOf(r) == EMP) {
    				v2.add(rc.senseLocationOf(r));
    			}
				v.add(rc.senseLocationOf(r));
    		}
    	}
    	if (v.size() > 0 || v2.size() > 0) {
	    	Message m = new Message();
	    	MapLocation[] blah = new MapLocation[v.size() + v2.size()];
	    	System.arraycopy(v.toArray(), 0, blah, 0, v.size());
	    	System.arraycopy(v2.toArray(), 0, blah, v.size(), v2.size());
	    	
	    	m.locations = blah;
	    	m.ints = new int[] {hashRound(Clock.getRoundNum()), 168, v.size(), v2.size()};
	    	rc.queueBroadcast(m);
	    	return true;
    	} else {
    		return false;
    	}
    }
    
    public static void broadcastLocations() throws Exception {
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
    	m.ints = new int[] {hashRound(Clock.getRoundNum()), 889, v.size(), v2.size()};
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
        			
        			if (m.ints[1] == 889 && type == TANK) {

	            		// work here
	            		rc.setIndicatorString(1, "received bad guys = " + (++badGuyMessageCount));
	            		
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
        			
        			if (m.ints[1] == 168 && type == SCOUT) {

	            		// work here
	            		rc.setIndicatorString(1, "received bad guys = " + (++badGuyMessageCount));
	            		
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
        			
        			if (m.ints[1] == 72) {
        				int i = 2;
        				leaderIndex = Math.max(leaderIndex, m.ints[i++]);
        		    	int newWalls = m.ints[i++];
        		    	int newUpgrades = m.ints[i++];
        		    	int newNogrades = m.ints[i++];
        		    	
        		    	updateMapLeft(m.ints[i++]);
        		    	updateMapRight(m.ints[i++]);
        		    	updateMapTop(m.ints[i++]);
        		    	updateMapBottom(m.ints[i++]);
        		    	
        		    	if (m.ints[i++] == 1) {
        		    		empWallCenter = new MapLocation(m.ints[i++], m.ints[i++]);
        		    		empWallDirection = Direction.values()[m.ints[i++]];
        		    	}
        		    	
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
						face(dir(loc));
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
    		for (Robot r : rc.senseNearbyGroundRobots()) {
    			if (r.getTeam() == team && rc.senseLocationOf(r).isAdjacentTo(rc.getLocation())) {
    				double amount = Math.min(
							rc.senseMaxEnergonOf(r) - rc.senseEventualEnergonOf(r),
							(rc.getEnergonLevel() - rc.senseEventualEnergonOf(r)) / 2);
    				if (amount > 0) {
    					rc.transferEnergon(amount, rc.senseLocationOf(r),
								ON_GROUND);
    				}
    			}
    		}
    		
    		double minAcceptable = 5;
    		if (rc.getEnergonLevel() > minAcceptable /*(rc.getMaxEnergonLevel() - 20)*/) {
		    	int nextIndex = 0;
		    	double desired = 0;
		    	
		    	for (Robot r : rc.senseNearbyAirRobots()) {
		    		if (r.getTeam() == team && rc.senseLocationOf(r).isAdjacentTo(rc.getLocation())) {
		    			double capacity = rc.senseMaxEnergonOf(r) - rc.senseEventualEnergonOf(r);
		    			if (capacity > 0.001) {
			    			desired += capacity;
			    			neighborLocs[nextIndex] = rc.senseLocationOf(r);
			    			neighborCapacitys[nextIndex] = capacity;
			    			nextIndex++;
			    		}
		    		}
		    	}
		    	
		    	
		    	double fraction = Math.min(1, (rc.getEnergonLevel() - minAcceptable) / desired);  
		    	
				for (int i = 0; i < nextIndex; i++) {
					rc.transferEnergon(neighborCapacitys[i] * fraction, neighborLocs[i], IN_AIR);
				}
    		}
			
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
				    					rc.transferEnergon(2 - a, rc.senseLocationOf(r), ON_GROUND);
				    				}
			    				}
		    				} else {
			    				if (energy > eventual) {
				    				double a = rc.senseEventualEnergonOf(r) - rc.senseEnergonLevelOf(r);
				    				if (a < 2) {
				    					rc.transferEnergon(2 - a, rc.senseLocationOf(r), ON_GROUND);
				    				}
			    				}
		    				}
	    				}
	    			}
	    		}
    		}
    		
    	} else if (type == SCOUT) {
    		
    	} else if (type == SAM) {
    		for (Robot bot : rc.senseNearbyGroundRobots()) {
    			// TODO This is only safe for sam-to-sam xfers.
    			if (bot.getTeam() == team && rc.senseLocationOf(bot).isAdjacentTo(rc.getLocation())) {
    				rc.transferEnergon(Math.max(0, (rc.getEnergonLevel() - rc
							.senseEventualEnergonOf(bot)) / 2), rc
							.senseLocationOf(bot), ON_GROUND);
    			}
    		}
    	}
    	
    }
    
    public static void empWallHealing() throws Exception {
    	MapLocation loc = rc.getLocation();
    	
    	for (int i = empWallIndex - 1; i <= empWallIndex + 1; i += 2) {
	    	if (i >= 0 && i < empWallSize) {
	    		MapLocation pos = empWallGetLocation(i);
	    		if (rc.canSenseSquare(pos) && loc.isAdjacentTo(pos)) {
		    		Robot r = rc.senseAirRobotAtLocation(pos);
		    		if (r != null && r.getTeam() == team) {
		        		double energy = rc.getEnergonLevel();
		    			double eventual = rc.senseEventualEnergonOf(r);
						if (energy > eventual) {
		    				double a = rc.senseEventualEnergonOf(r) - rc.senseEnergonLevelOf(r);
		    				if (a < 2) {
		    					rc.transferEnergon(Math.min(energy, 2 - a), pos, IN_AIR);
		    				}
						}
		    		}
	    		}
	    	}
    	}
    }
    
    public static void empWallDonate() throws Exception {
    	MapLocation loc = rc.getLocation();
    	
    	double total = 0;
    	for (int i = empWallIndex - 1; i <= empWallIndex + 1; i++) {
	    	if (i >= 0 && i < empWallSize) {
	    		MapLocation pos = empWallGetLocation(i);
	    		if (rc.canSenseSquare(pos) && loc.isAdjacentTo(pos)) {
		    		Robot r = rc.senseAirRobotAtLocation(pos);
		    		if (r != null && r.getTeam() == team) {
		    			total += rc.senseMaxEnergonOf(r) - rc.senseEventualEnergonOf(r);
		    		}
	    		}
	    	}
    	}
    	double amountToTransfer = rc.getEnergonLevel() - 2;
    	double fraction = Math.min(1, amountToTransfer / total);
    	for (int i = empWallIndex - 1; i <= empWallIndex + 1; i++) {
	    	if (i >= 0 && i < empWallSize) {
	    		MapLocation pos = empWallGetLocation(i);
	    		if (rc.canSenseSquare(pos) && loc.isAdjacentTo(pos)) {
		    		Robot r = rc.senseAirRobotAtLocation(pos);
		    		if (r != null && r.getTeam() == team) {
		    			double desire = rc.senseMaxEnergonOf(r) - rc.senseEventualEnergonOf(r);
		    			rc.transferEnergon(desire * fraction, pos, IN_AIR);
		    		}
	    		}
	    	}
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
    
    public static boolean tryMove(Direction d) {
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
    
    static HashSet<MapLocation> tethered = new HashSet<MapLocation>();
    static boolean isReturning = false;
    
    static boolean tryMoveTethered(Direction d) {
    	if (d == null) {
    		return false;
    	}
    	//debug_startTimer();
    	MapLocation myLoc = rc.getLocation();
    	MapLocation[] newLocs = new MapLocation[] {
    			myLoc.add(d),
    			myLoc.add(d.rotateRight()),
    			myLoc.add(d.rotateRight())
    	};
    	boolean[] canMoves = new boolean[newLocs.length];
    	
    	HashSet<MapLocation> tethered = new HashSet<MapLocation>();
    	
    	MapLocation archLoc = nearestArchonLocation();
    	if (myLoc.isAdjacentTo(archLoc)) {
    		isReturning = false;
    	} else {
        	double roundsTillArch = Math.sqrt(myLoc.distanceSquaredTo(archLoc)) * type.moveDelayDiagonal();
        	double roundsTillDeath = rc.getEnergonLevel() / type.energonUpkeep();
        	rc.setIndicatorString(0, "roundsDeath: "  + roundsTillDeath + " roundsLeft: " + roundsTillArch);
    		if (roundsTillDeath < 1.2 * roundsTillArch)
    			isReturning = true;
    	}
		return tryMove(isReturning ? dir(archLoc) : rc.senseEnemyArchon());
    	
/*    	try {
	    	for (Robot bot : rc.senseNearbyGroundRobots()) {
	    		MapLocation botLoc = rc.senseLocationOf(bot);
    			if (bot.getTeam() == team) {
		        	if (rc.senseRobotTypeOf(r) == ARCHON) {
	        			tethered.add(botLoc);
	        			tethered.add(botLoc.add(Direction.NORTH));
		        		int i = 0;
		        		for (MapLocation newLoc : newLocs) {
				    		if (botLoc.isAdjacentTo(newLoc) &&
				    				rc.senseCurrentActionOf(bot) != ActionType.MOVING) {
				    			canMoves[i] = true;
				    		}
				    		i++;
			    		}
		        	}
    			}
	        }
    	} catch (GameActionException ex) {
    		System.out.println("crap");
    	}
    	//debug_stopTimer();
    	if (rc.canMove(d) && canMoves[0]) {
    		move(d);
    		return true;
    	} else if (rc.canMove(d.rotateRight()) && canMoves[1]) {
    		move(d.rotateRight());
    		return true;
    	} else if (rc.canMove(d.rotateLeft()) && canMoves[2]) {
    		move(d.rotateLeft());
    		return true;
    	}
    	return false;*/
    }
    
    public static boolean tryMoveNotTooHard(Direction d) {
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

    		// work here
    		rc.setIndicatorString(0, "attacking archon");
    		
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

        		// work here
        		rc.setIndicatorString(0, "attacking in range");
        		
        		tryAttackGround(loc);
        	} else {
        		if (someoneClose) {

            		// work here
            		rc.setIndicatorString(0, "attacking close");
            		
        			tankBlindAttack(dir(guyClose), 3);
        		} else {

            		// work here
            		rc.setIndicatorString(0, "attacking far");
            		
        			tankBlindAttack(rc.senseEnemyArchon(), 5);
        		}
        	}
    	}
    }
    
    public static boolean samAttack() {
    	Robot bestBot = null;
    	RobotType bestType = null;
		MapLocation myLoc = rc.getLocation();
		Robot[] bots = rc.senseNearbyAirRobots();
		String s = "";
    	for (Robot robot : bots) {
    		if (robot.getTeam() != team) {
    			RobotType rtype;
    			MapLocation rloc;
    			try {
    				rtype = rc.senseRobotTypeOf(robot);
    				rloc = rc.senseLocationOf(robot);
    			} catch (GameActionException ex) {
    				continue;
    			}
    			s += myLoc.distanceSquaredTo(rloc) + " ";
    			if (myLoc.distanceSquaredTo(rloc) <= type.attackRadiusMaxSquared() &&
    					myLoc.distanceSquaredTo(rloc) >= type.attackRadiusMinSquared()) {
        			if (rtype == EMP) {
        				bestBot = robot;
        				bestType = EMP;
        			} else if (bestType != EMP && bestBot == null) {
        				bestBot = robot;
        				bestType = SCOUT;
        			}
    			}
    		}
    	}
    	
    	boolean result = false;
    	if (bestBot != null) {
    		try {
    			MapLocation loc = rc.senseLocationOf(bestBot);
				face(dir(loc));
				rc.queueAttackAir(loc);
				result = true;
    		} catch (GameActionException ex) {
    		}
    	}
    	rc.setIndicatorString(0, "attacking: " + result + " bots: " + bots.length + " best: " + bestType + " clock: " + Clock.getBytecodeNum() + " s: " + s);
    	rc.setIndicatorString(1, "min: " + type.attackRadiusMinSquared() + " max: " + type.attackRadiusMaxSquared());
    	return result;
/*        	for (MapLocation pos : badGuys) {
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

        		// work here
        		rc.setIndicatorString(0, "attacking in range");
        		
        		tryAttackGround(loc);
        	} else {
        		if (someoneClose) {

            		// work here
            		rc.setIndicatorString(0, "attacking close");
            		
        			tankBlindAttack(dir(guyClose), 3);
        		} else {

            		// work here
            		rc.setIndicatorString(0, "attacking far");
            		
        			tankBlindAttack(rc.senseEnemyArchon(), 5);
        		}
        	}*/
    }
    
    public static boolean isTakenAir(MapLocation pos) throws Exception {
    	if (rc.canSenseSquare(pos)) {
    		if (rc.senseAirRobotAtLocation(pos) != null) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public static Direction archonQuadrant;
    
    public static int childID = Integer.MIN_VALUE;
    public static int motherID = Integer.MIN_VALUE;
    public static MapLocation motherLoc;
	
	public static int lastHeardFromChild = Clock.getRoundNum();
	
	public static MapLocation[] groundEnemies = new MapLocation[0];
	public static int lastGroundEnemiesUpdate = Integer.MIN_VALUE;
	
	public static MapLocation attackGoal = null;
	
	public static boolean empWallScout = false;
    
    public static MapLocation empWallCenter = null;
    public static Direction empWallDirection = null;
	
	public static int empWallIndex = 0;
	public static boolean ownsWallIndex = false;
	public static int dropOffAndGoHome = 0;
	public static int empBadCount = 0;
	public static int empWallSize = 9;
	
	public static boolean leader = false;
	
	public static int leaderIndex = 0;
	
	static int sentMessageRound = -1;
	
    public void run() {
    	try {
    		empWallIndex = rand.nextInt(empWallSize);
    		
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
    		
	        while (true) {
	            try {
	            	
	            	if (type == SAM) {
	            		
	            		MapLocation loc = rc.getLocation();

	            		// work here
	            		badGuyMessageCount = 0;
	            		
	            		processMessages(true);
	            		updateMap();
	            		clearNewSets();
	            		
	            		doHealing();
	            		
	            		if (!rc.isActive()) {
	            			Direction d = dirToGrandMother();
	            			d = dirToNearestEnemyArchon();
	            			if (!samAttack()) {
	            				tryMoveTethered(d);
	            			}
		            		//explore(false);
	            			badGuys.clear();
	            			badGuysA.clear();
	            		}
	            		
        				rc.yield();
	            	}
	            	
	            	if (type == TANK) {
	            		
	            		MapLocation loc = rc.getLocation();

	            		// work here
	            		badGuyMessageCount = 0;
	            		rc.setIndicatorString(1, "none received");
	            		
	            		processMessages(true);
	            		updateMap();
	            		clearNewSets();
	            		
	            		doHealing();
	            		
	            		if (rc.isActive()) {
	            			
	            		} else {
	            			Direction d = dirToGrandMother();
	            			if (!tryMoveNotTooHard_notOnUpgrade(d)) {
            					tankAttack();
	            			}
	            			badGuys.clear();
	            			badGuysA.clear();
	            		}
	            		
        				rc.yield();
	            	}
	            	
	            	if (type == SOLDIER) {
	            		processMessages(true);
	            		updateMap();
	            		clearNewSets();
	            		
	            		if (rc.getEnergonLevel() > type.maxEnergon() - 2) {
	            			evolve(SAM);
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
	            		
		            	rc.setIndicatorString(0, "__");
		            	rc.setIndicatorString(1, "child id = " + childID);
		            	rc.setIndicatorString(2, "__");

		            	rc.setIndicatorString(0, "lead? " + leader);
		            	
		            	
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
		            		broadcastLocations();
		            	}
		            	
	            		doHealing();
	            		
	            		// see if we should enter attack mode
	            		if (leader) {
	            			attackMode = false;
	            			attackGoal = null;
		            		for (Robot r : rc.senseNearbyGroundRobots()) {
		            			if (r.getTeam() != team) {
		            				RobotType t = rc.senseRobotTypeOf(r);
		            				if (t == ARCHON) {
			            				MapLocation pos = rc.senseLocationOf(r);
			            				int dist = loc.distanceSquaredTo(pos);
			            				if (dist <= 16) {
			            					attackGoal = pos;
			            					attackMode = true;
			            				}
		            				}
		            			}
		            		}
		            		if (attackMode) {
		            			rc.setIndicatorString(2, "attack mode!");
		            			rc.yield();
		            			continue;
		            		}
	            			rc.setIndicatorString(2, "peace mode");
	            		}
		            	
	            		if (!rc.isActive()) {
	            			if (rc.getEnergonLevel() >= SCOUT.spawnCost() + 10 && rc.getUnitCount(SCOUT) < 1) {
	            				if (trySpawnRobot(SCOUT)) continue;
	            			}
			            	if (rc.getEnergonLevel() >= SOLDIER.spawnCost() + 10) {
		            			if (trySpawnRobotNotOnUpgrade(SOLDIER)) {
			            			eggReady = round() + 75;
			            			continue;
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
			            			if (round() > 500) {
				            			if (!tryMoveNotTooHard(d)) {
				            				leaderIndex++;
				            			}
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
	            	
	            	if (type == SCOUT) {
	            		
	            		
	            		if (empWallScout) {
	            			processMessages(false);
	            			
	            			MapLocation loc = rc.getLocation();
	            			
	            			if (empWallCenter != null) {
	            				
	            				if (dropOffAndGoHome > 0) {
	            					if (!rc.isActive()) {
		            					if (dropOffAndGoHome == 1) {
		            						// drop off...
		            						MapLocation goal = empWallGetLocation(empWallIndex);
		            						if (loc.isAdjacentTo(goal)) {
		            							empWallDonate();
		            							dropOffAndGoHome = 2;
		            						} else {	            						
		            							Direction d = dir(goal);
		            							tryMove(d);
		            						}
		            					} else {
		            						// go home
		            						MapLocation goal = nearestMother();
		            						tryMove(dir(goal));
		            						if (rc.getEnergonLevel() > rc.getMaxEnergonLevel() - 1) {
		            							dropOffAndGoHome = 0;
		            							empWallIndex = rand.nextInt(empWallSize);
		            						}
		            					}
	            					}
	            				} else {
	            					// heal others
	            					empWallHealing();
	            					
	            					// go to place on wall
	            					MapLocation goal = empWallGetLocation(empWallIndex);
	            					Direction d = dir(goal);
	            					
	            					if (!rc.isActive()) {
	            						if (loc.equals(goal)) {
	            							scoutTryAttack();
	            							ownsWallIndex = true;
		            						empBadCount = 0;
	            						} else if (rc.canMove(d)) {
		            						move(d);
		            						empBadCount = 0;
		            					} else {
	            							empBadCount++;
		            						if (ownsWallIndex) {
		            							if (empBadCount > 10) {
		            								ownsWallIndex = false;
		            								dropOffAndGoHome = 1;
		            							}
		            						} else {
		            							empWallIndex = (empWallIndex + 1) % empWallSize;
		            							if (empBadCount > 2) {
		            								dropOffAndGoHome = 1;
		            							}
		            						}
		            					}
	            					}
	            				}
	            			}
	            			
	            			rc.yield();
	            			
	            		} else {
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
		            				rc.setIndicatorString(0, "heading out!");
		            			}
		            		} else {
		            			if (rc.getEnergonLevel() < type.maxEnergon() / 2 /*|| newUpgradeSet.size() > 0*/) {
		            				rc.setIndicatorString(0, "returning home");
		            				scoutGoal = null;
		            			}
		            		}
		            		
		            		if (round() > sentMessageRound + 5 * type.moveDelayDiagonal()) {
		            			if (broadcastAirLocations()) {
			            			sentMessageRound = round();
		            				//clearNewSets();
		            				rc.setIndicatorString(0, "broadcasted");
		            				scoutGoal = null;
		            			}
		            		}
		            		
		            		if (rc.isActive()) {
		            			rc.yield();
		            		} else {
		            			MapLocation goal = scoutGoal == null ?
		            					nearestArchonLocation() : scoutGoal;
		            			tryMove(dir4(goal));
			            		explore(false);
		            		}
		            		
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
