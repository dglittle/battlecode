package alliance;

import battlecode.common.*;
import static battlecode.common.GameConstants.*;
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
    
    // ========================================================================
    
    public class Tile {
    	public boolean land = false;
    	public boolean water = false;
    	public boolean space = false;
    	public boolean unknown = true;
    }
    
    public class Board {
    	Tile[][] tiles = new Tile[79][79];
    	
    	public Board() {
    		
    	}
    }
    
    // ========================================================================
    
    public int sent = 0;
    public int got = 0;
    
    public void run() {
        while(true){
            try {
            	if (type == RobotType.ARCHON) {
            		if (sent == 0 && rc.getTeam() == Team.A) {
	            		if (rc.senseGroundRobotAtLocation(rc.getLocation().add(Direction.SOUTH_EAST)) != null) {
	                		rc.queueSpawn(RobotType.SCOUT);
	            		}
            		}
            		if (rc.getUnitCount(RobotType.SCOUT) > 0) {
            			sent = 1;
            		}
            	} else if (type == RobotType.SCOUT) {
            		debug(1, "I exist!");
            		System.out.println("energon: " + rc.getEnergonLevel() + " -- " + rc.getCurrentAction());
            	}
                
                rc.yield();
            } catch(Exception e) {
                System.out.println("caught exception:");
                e.printStackTrace();
            }
        }
    }
}
