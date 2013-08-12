package flyingRush.blocks;

import flyingRush.blocks.pathfinding.BugPathfindingBlock;
import flyingRush.core.S;
import flyingRush.core.U;
import flyingRush.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

public class Soldier {
  public static final void go() throws Exception {

    U.waitForComponents();

    // which direction is the enemy?
    Direction toEnemy = null;
    if (S.chassis == Chassis.FLYING && S.round < 500) {
      MapLocation cursor = S.location;
      if (S.direction != Direction.NORTH) {
        X.setDirection(Direction.NORTH);
        X.yield();
      }
      if (S.rc.senseTerrainTile(S.location.add(Direction.WEST, 6)) == TerrainTile.OFF_MAP) {
        cursor = cursor.add(Direction.EAST);
      }
      if (S.rc.senseTerrainTile(S.location.add(Direction.EAST, 6)) == TerrainTile.OFF_MAP) {
        cursor = cursor.add(Direction.WEST);
      }
      if (S.rc.senseTerrainTile(S.location.add(Direction.NORTH, 6)) == TerrainTile.OFF_MAP) {
        cursor = cursor.add(Direction.SOUTH);
      }
      X.setDirection(Direction.SOUTH);
      X.yield();
      if (S.rc.senseTerrainTile(S.location.add(Direction.SOUTH, 6)) == TerrainTile.OFF_MAP) {
        cursor = cursor.add(Direction.NORTH);
      }
      if (!cursor.equals(S.location)) {
        toEnemy = S.location.directionTo(cursor);
      }
    }

    while (true) {
      if (toEnemy != null) {
        MapLocation loc = S.location.add(toEnemy, 4);
        if (S.rc.senseTerrainTile(loc) == TerrainTile.OFF_MAP) {
          toEnemy = null;
        }
      }

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
          Direction d = BugPathfindingBlock.nextDirection(bestTarget,
              ComponentType.BLASTER.range);
          if (d.ordinal() < 8) {
            X.moveTowardsAsync(d);
          }
        } else {

          if (toEnemy != null) {
            Direction d = StupidPathfinding.bugToAsync(S.location.add(toEnemy,
                10));
            if (d.ordinal() < 8) {
              if (S.direction != d) {
                X.setDirection(d);
              } else {
                X.moveForward();
              }
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
      }
      X.yield();
    }
  }
}
