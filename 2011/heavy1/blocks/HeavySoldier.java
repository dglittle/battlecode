package heavy1.blocks;

import heavy1.blocks.pathfinding.BugPathfindingBlock;
import heavy1.chaos.little.JumpExplorer;
import heavy1.core.D;
import heavy1.core.M;
import heavy1.core.S;
import heavy1.core.U;
import heavy1.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.JumpController;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

public class HeavySoldier {

  public static final void go() throws GameActionException {
    M.disableMapUpdates();
    
    while (true) {
      JumpExplorer.update();

      //////////////////////////
      // Combat

      boolean performedAttackAction = CombatBlock.attackWeakestRobotOrBuildingAsync();
      if (performedAttackAction) {
        X.yield();
        continue;
      }
      
      // Tactics
      
      boolean tacticUsed = CombatBlock.tactic_checkForAttackFromBehind();
      if (tacticUsed) {
        X.yield();
        continue;
      }

      //////////////////////////
      // Movement

      JumpExplorer.step();
      X.yield();
    }
  }

}
