package flyingRush.chaos;

import flyingRush.blocks.PathfindingBlock;
import flyingRush.core.S;
import flyingRush.core.X;
import battlecode.common.MapLocation;

public class VExploreTopLevel {  
  public static final void sync() {
    MapLocation startLocation = S.location;

    // Phase 1: go away.
    while (S.location.distanceSquaredTo(startLocation) <= 900) {
      ExplorationBlock.async();
      X.yield();
    }
    
    // Phase 2: come back.
    // M.debug_printMap();
    PathfindingBlock.setParameters(startLocation, 200);
    while (!PathfindingBlock.done) {
      PathfindingBlock.async();
      X.yield();
    }
    
    // M.debug_printMap();
  }
}
