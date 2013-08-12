package nothing;

import battlecode.common.*;
import battlecode.world.signal.EvolutionSignal;
import static battlecode.common.GameConstants.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static battlecode.common.MapHeight.*;
import static battlecode.common.TerrainType.*;
import imper4.RobotPlayer.Dist;

import java.io.*;
import java.lang.*;
import java.math.*;
import java.util.*;

public class RobotPlayer implements Runnable {
    
    public static RobotController rc;
    
    public RobotPlayer(RobotController _rc) {
        rc = _rc;
    }
    
    public void run() {
    	while (true) {
    		rc.yield();
    	}
    }
}
