package heavy1.blocks;

import heavy1.blocks.pathfinding.BugPathfindingBlock;
import heavy1.core.S;
import heavy1.core.X;
import heavy1.core.xconst.XDirection;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class Soldier {
  public static final void go() throws GameActionException {
    while (true) {

      // ////////////////////////
      // attacking...
      MapLocation bestTarget = null;
      double bestValue = Double.MAX_VALUE;      

      MapLocation bestImmediateTarget = null;
      RobotLevel bestImmediateLevel = null;
      double bestImmediateValue = Double.MAX_VALUE;
      for (RobotInfo ri : S.nearbyRobotInfos()) {
        if (ri != null && ri.robot.getTeam() == S.enemyTeam) {
          double value = ri.hitpoints;
          MapLocation loc = ri.location;
          if (value < bestValue) {
            bestValue = value;
            bestTarget = loc;
          }
          if (S.weaponControllers[0].withinRange(ri.location)
              && value < bestImmediateValue) {
            bestImmediateValue = value;
            bestImmediateTarget = loc;
            bestImmediateLevel = ri.chassis.level;
          }
        }
      }
        
      if (bestImmediateTarget != null) {
        X.attack(bestImmediateTarget, bestImmediateLevel);
      }        
      // ////////////////////////
      // movement section

      // can we move?
      if (!S.movementController.isActive()) {
        // is there a target to move toward?
        if (bestTarget != null) {
          Direction d = BugPathfindingBlock.nextDirection(bestTarget, ComponentType.BLASTER.range);
          if (d.ordinal() < XDirection.ADJACENT_DIRECTIONS) {
            X.moveTowardsAsync(d);
          }
        } else {
          // no target,
          // let's explore, I guess
          Direction d = LittleExplorationBlock.explore();
          if (d.ordinal() < XDirection.ADJACENT_DIRECTIONS) {
            if (S.direction != d) {
              X.setDirection(d);
            } else {
              X.moveForward();
            }
          }
        }
      }
      X.yield();
    }
  }
}
