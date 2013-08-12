package team050.test;

import team050.blocks.PathfindingBlock;
import team050.chaos.pwnall.ExplorationBlock;
import team050.core.S;
import team050.core.X;
import battlecode.common.MapLocation;

public class VPathfindingTestBlock {  
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
