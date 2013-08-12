package sprint2.toplevel;

import sprint2.blocks.SurveyNavigationBlock;
import sprint2.core.M;
import sprint2.core.S;
import sprint2.core.X;
import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class VExploreTopLevel {  
  public static void run() {    
    // Phase 1: go away.
    MapLocation startLocation = S.location;
    Direction targetDirection = M.nearestEdgeDirection().opposite();
    SurveyNavigationBlock.setNavigationParameters(targetDirection, 5, 250);
    
    System.out.println("in ExploreTopLevel - " + startLocation + " -> " + targetDirection);
    M.debug_PrintMap();
    
    while(true) {
      if (S.location.distanceSquaredTo(startLocation) >= 100) {
        break;
      }
      
      if (SurveyNavigationBlock.tryNavigating()) continue;
      
      X.yield();
    }
    
    // Phase 2: come back.
    M.debug_PrintMap();
    while (true) {
      X.yield();
    }
  }
}
