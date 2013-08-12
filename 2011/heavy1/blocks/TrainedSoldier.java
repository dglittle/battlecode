package heavy1.blocks;

import heavy1.blocks.pathfinding.BugPathfindingBlock;
import heavy1.core.S;
import heavy1.core.X;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class TrainedSoldier {
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
          Direction nextDirection = BugPathfindingBlock.nextDirection(bestTarget, ComponentType.BLASTER.range);
          // Training -->
          Direction directionToTarget = S.location.directionTo(bestTarget);
          Direction directionAwayFromTarget = directionToTarget.opposite();
          // If we're turning, then turn.
          if (nextDirection.ordinal() < 8 && directionToTarget != nextDirection) {
            X.moveTowardsAsync(nextDirection);
          }
          // If we're not scheduled to turn, then we need to analyze the distances.
          else {
            // If the direction is NONE, then we're in range of the Blaster.
            // If moving backwards keeps us in range, then move backwards.
            if (nextDirection.ordinal() >= 8) {
              MapLocation nextLocation = S.location.add(directionAwayFromTarget);
              if (directionAwayFromTarget.ordinal() < 8 && 
                  nextLocation.distanceSquaredTo(bestTarget) <= ComponentType.BLASTER.range) {
                X.moveTowardsAsync(directionAwayFromTarget);
              }
            }
            // If moving forward will put us in range, then stay still. Otherwise, move forward.
            if (nextDirection.ordinal() < 8) {
//              // If the direction is not NONE, then we can still move into the range of the Blaster.
//              MapLocation nextLocation = S.location.add(nextDirection);
//              if (nextLocation.distanceSquaredTo(bestTarget) <= ComponentType.BLASTER.range) {
//                // Stay still.
//              }
//              else {
//                // Move there.
//                X.moveTowardsAsync(nextDirection);
//              }
              X.moveTowardsAsync(nextDirection);
            }
          }
          // <--
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
