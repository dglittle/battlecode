package flyingRush.chaos;

import flyingRush.blocks.RandomMovementBlock;
import flyingRush.blocks.pathfinding.BugPathfindingBlock;
import flyingRush.core.S;
import flyingRush.core.U;
import flyingRush.core.X;
import flyingRush.core.xconst.XComponentClass;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;

public final class ManipleTopLevel {

  public static Strategy strategy;

  /**
   * The run method for the Maniple legion (soldiers).
   */
  public static final void runLegion() {
    while (true) {
      try {
        if (strategy == null)
          strategy = new ManipleLegionStrategyBlock();
        strategy.step();
      } catch (Exception e) {

      }
    }
  }

  /**
   * The run method for the Maniple sargeant.
   */
  public static final void runSargeant() {
    while (true) {
      try {
        if (strategy == null)
          strategy = new ManipleSargeantStrategyBlock();
        strategy.step();
      } catch (Exception e) {

      }
    }
  }

  /**
   * The run method for the Maniple buildings.
   * 
   * @throws GameActionException
   */
  public static final void runBuilding() throws GameActionException {
    while (true) {
      if (XComponentClass.hasComponents(ComponentType.RECYCLER)) {
        if (chassisCount == 0) {
          while (S.builderController.isActive()
              || !S.allowedToBuildNonMine(Chassis.LIGHT, ComponentType.BLASTER,
                  ComponentType.SIGHT))
            X.yield();
          MapLocation adjacentEmpty = U.getRandomAdjacentEmpty(RobotLevel.ON_GROUND);
          S.builderController.build(Chassis.LIGHT, adjacentEmpty);
          X.yield();
          S.builderController.build(ComponentType.BLASTER, adjacentEmpty,
              RobotLevel.ON_GROUND);
          X.yield();
          S.builderController.build(ComponentType.SIGHT, adjacentEmpty,
              RobotLevel.ON_GROUND);
          chassisCount++;
          continue;
        } else if (chassisCount == 1) {
          if (U.isBuildingLeader()) {
            while (S.builderController.isActive()
                || !S.allowedToBuildNonMine(Chassis.LIGHT, ComponentType.ANTENNA,
                    ComponentType.RADAR, ComponentType.PLATING,
                    ComponentType.PLATING))
              X.yield();
            X.yield();
            MapLocation adjacentEmpty = U.getRandomAdjacentEmpty(RobotLevel.ON_GROUND);
            S.builderController.build(Chassis.LIGHT, adjacentEmpty);
            X.yield();
            S.builderController.build(ComponentType.ANTENNA, adjacentEmpty,
                RobotLevel.ON_GROUND);
            X.yield();
            S.builderController.build(ComponentType.RADAR, adjacentEmpty,
                RobotLevel.ON_GROUND);
            X.yield();
            S.builderController.build(ComponentType.PLATING, adjacentEmpty,
                RobotLevel.ON_GROUND);
            X.yield();
            S.builderController.build(ComponentType.PLATING, adjacentEmpty,
                RobotLevel.ON_GROUND);
          }
          chassisCount++;
          continue;
        } else {
          X.yield();
        }
      }
    }
  }

  public static int chassisCount = 0;

  /**
   * The run method for the Maniple constructor. He needs to build a Factory and
   * get out of the way.
   * 
   * @throws GameActionException
   */
  public static final void runConstructor() throws GameActionException {
    while (true) {
      if (!finishedBuilding) {
        while (S.movementController.isActive()) {
          X.yield();
        }
        X.setDirection(RandomMovementBlock.randomAvailableDirection());
        X.yield();
        while (S.builderController.isActive()
            || !S.allowedToBuildNonMine(Chassis.BUILDING, ComponentType.FACTORY)) {
          X.yield();
        }
        S.builderController.build(Chassis.BUILDING, S.front);
        X.yield();
        while (S.builderController.isActive()
            || !S.allowedToBuildNonMine(Chassis.BUILDING, ComponentType.FACTORY)) {
          X.yield();
        }
        S.builderController.build(ComponentType.FACTORY, S.front,
            RobotLevel.ON_GROUND);
        X.yield();
        finishedBuilding = true;
        continue;
      } else {
        if (distantLocation == null) {
          distantLocation = S.location.add(Direction.EAST, 30);
          goingEast = true;
        }
        Direction nextDirection = BugPathfindingBlock.nextDirection(distantLocation);
        if (nextDirection.equals(Direction.NONE)
            || nextDirection.equals(Direction.OMNI)) {
          if (goingEast) {
            distantLocation = S.location.add(Direction.WEST, 30);
            goingEast = false;
          } else {
            distantLocation = S.location.add(Direction.EAST, 30);
            goingEast = true;
          }
          nextDirection = BugPathfindingBlock.nextDirection(distantLocation);
        }
        S.rc.suicide();
        // if (S.canMove(nextDirection) && !S.movementController.isActive()) {
        // NavigationBlock.moveInDirectionNow(nextDirection);
        // continue;
        // }
        // else {
        // X.yield();
        // continue;
        // }
      }
    }
  }

  public static boolean finishedBuilding = false;
  public static MapLocation distantLocation;
  public static boolean goingEast = true;

}
