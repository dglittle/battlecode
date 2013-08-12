package team050.toplevel;

import team050.blocks.TierBlock;
import team050.blocks.building.BuildDriverBlock;
import team050.chaos.pwnall.LeaderElectionBlock;
import team050.core.D;
import team050.core.Role;
import team050.core.S;
import team050.core.U;
import team050.core.X;
import team050.core.xconst.XDirection;
import battlecode.common.GameActionException;
import battlecode.common.Mine;

public class HatcheryTopLevel {
  public static final void sync() throws GameActionException {
    if (S.birthRound < 100) {
      final int emptySpaces = U.adjacentBuildingLots();
      X.setDirectionChecked(XDirection.intToDirection[emptySpaces / 2]);
      D.debug_py("empty spaces = " + emptySpaces);      
      S.rc.turnOff();
    }

    // if we're alone, turn off if we're an even id
    boolean nextToOtherMine = false;
    for (Mine m : S.senseMines()) {
      if (m.getLocation().isAdjacentTo(S.location)) {
        nextToOtherMine = true;
        break;
      }
    }
    if (!nextToOtherMine) {
      if (S.id % 2 == 0) {
        S.rc.turnOff();
      }
    }
    
    while (true) {
      D.debug_setIndicator(0, "total upkeep = " + S.totalUpkeep);
      if (!_hasLeader) {
        if (!LeaderElectionBlock.busy) 
          LeaderElectionBlock.start();
        LeaderElectionBlock.async();
        if(!LeaderElectionBlock.busy) {
          _hasLeader = true;
          _isLeader = LeaderElectionBlock.isLeader;
          if (!_isLeader) {
            S.rc.turnOff();
            // Re-election.
            _hasLeader = false;
          }
        }
      }
      
      if (_isLeader) {
        if (TierBlock.tierSwitched()) {
          switch (TierBlock.currentTier()) {
            case 1:
              BuildDriverBlock.setBuildOrder(new Role[]{Role.COLONIST},
                  new int[]{1}, new int[] {0},
                  TierBlock.tieredUnitThreshold());
              break;
            case 2:
              BuildDriverBlock.setBuildOrder(
                  new Role[]{Role.HEAVY_SOLDIER, Role.FLYING_COLONIST}, 
                  new int[]{3, 2}, new int[] {0},
                  TierBlock.tieredUnitThreshold());
              break;
            case 3:
              BuildDriverBlock.setBuildOrder(new Role[]{Role.HEAVY_SOLDIER, 
                  Role.FLYING_COLONIST}, new int[]{3, 3}, new int[] {0}, 
                  TierBlock.tieredUnitThreshold());
              break;
          }
        }
        
        BuildDriverBlock.async();
  
        if (S.motorReady && !S.sensorIsOmnidirectional) {
          // TODO(pwnall): generic turn method
          try {
            X.setDirection(S.direction.opposite());
          } catch (GameActionException e) {
            D.debug_logException(e);  // This should not happen.
          }
        }
      }
      X.yield();
    }
  }
  
  public static boolean _hasLeader, _isLeader;
}
