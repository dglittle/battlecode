package sprint4.blocks;

import sprint4.blocks.pathfinding.BugPathfindingBlock;
import sprint4.core.D;
import sprint4.core.S;
import sprint4.core.X;
import battlecode.common.Clock;
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
      RobotLevel bestLevel = null;
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
            bestLevel = ri.chassis.level;
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
          if (d.ordinal() < 8) {
            X.moveTowardsAsync(d);
          }
        } else {
          // no target,
          // let's explore, I guess
          Direction d = LittleExplorationBlock.explore();
          if (d.ordinal() < 8) {
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
