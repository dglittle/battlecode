package legenClone.util;

import legenClone.core.M;
import legenClone.blocks.BugPathfinding;
import legenClone.blocks.LittleExplorer;
import legenClone.core.B;
import legenClone.core.Callback;
import legenClone.core.CommandType;
import legenClone.core.S;
import legenClone.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class LegenClone {
  public static final void go() throws GameActionException {
    while (true) {
      if (S.chassis == Chassis.BUILDING
          && LittleUtil.hasComponents(ComponentType.RECYCLER)) {
        LittleUtil.waitForComponents();
        building();
      }
      if (S.chassis == Chassis.LIGHT
          && LittleUtil.hasComponents(ComponentType.CONSTRUCTOR,
              ComponentType.SIGHT)) {
        LittleUtil.waitForComponents();
        constructor();
      }
      if (S.chassis == Chassis.LIGHT
          && LittleUtil.hasComponents(ComponentType.BLASTER,
              ComponentType.RADAR, ComponentType.ANTENNA)) {
        LittleUtil.waitForComponents();
        soldier();
      }
      X.yield();
    }
  }

  public static final void building() throws GameActionException {
    final double costForMinion = Chassis.LIGHT.cost
        + Math.max(ComponentType.CONSTRUCTOR.cost + ComponentType.SIGHT.cost,
            ComponentType.BLASTER.cost + ComponentType.RADAR.cost
                + ComponentType.ANTENNA.cost) + Chassis.BUILDING.cost
        + ComponentType.RECYCLER.cost;

    while (true) {

      // if we have enough flux for a constructor or soldier, and enough for
      // another mine,
      // build a constructor or soldier
      if (S.allowedToBuild(costForMinion, Chassis.LIGHT.upkeep)) {
        MapLocation site = LittleUtil.findPlaceByBuildingToBuild();
        if (site != null) {
          try {
            if (S.round < 1000 || S.rand.nextDouble() < 0.3) {
              LittleUtil.buildSync(site, Chassis.LIGHT,
                  ComponentType.CONSTRUCTOR, ComponentType.SIGHT);
              continue;
            } else {
              LittleUtil.buildSync(site, Chassis.LIGHT, ComponentType.BLASTER,
                  ComponentType.RADAR, ComponentType.ANTENNA);
              continue;
            }
          } catch (Exception e) {

          }
        }
      }

      // if enough rounds have passed, and we are odd indexed, turn off
      if (S.round > 200) {
        if (S.id % 2 == 1) {
          S.rc.turnOff();
        }
      }

      X.yield();
    }
  }

  public static MapLocation goalMine = null;
  public static int lookAround_rounds = 3;

  public static MapLocation prevLocation = S.location;
  public static int leaveThisPlace = 20;

  public static final void constructor() throws GameActionException {
    B.onMessageHandlers.add(new Callback() {
      @Override
      public void onMessage(Message m) {
        if (m.ints[1] == CommandType.LEGEN_MINE_HERE.ordinal()) {
          MapLocation nearestMine = new MapLocation(m.ints[2], m.ints[3]);
          if (nearestMine != null
              && (goalMine == null || nearestMine.distanceSquaredTo(S.location) < goalMine.distanceSquaredTo(S.location))) {
            goalMine = nearestMine;
          }
        }
      }
    });

    l1 : while (true) {
      
      // work here
      S.rc.setIndicatorString(0, "flux: " + S.flux);

      // see if we see a better mine to capture
      MapLocation nearestMine = LittleUtil.nearestEmptyMine();
      if (nearestMine != null
          && (goalMine == null || nearestMine.distanceSquaredTo(S.location) < goalMine.distanceSquaredTo(S.location))) {
        goalMine = nearestMine;
      }

      // if we're next to our goal mine, and we have enough flux, build on it
      if (goalMine != null && S.location.isAdjacentTo(goalMine)
          && S.allowedToBuild(Chassis.BUILDING, ComponentType.RECYCLER)) {
        try {
          LittleUtil.buildSync(goalMine, Chassis.BUILDING,
              ComponentType.RECYCLER);
        } catch (Exception e) {
        }
        goalMine = null;
      }

      // if we see a friendly mine or constructor with a lower id (older),
      // make a deadzone there
      for (RobotInfo ri : S.senseRobots()) {
        if (ri.robot.getTeam() == S.team && ri.robot.getID() < S.id) {
          MapLocation loc = MapUtil.add(ri.location,
              S.location.directionTo(ri.location), 10);
          LittleExplorer.addDeadZone(loc);
          break;
        }
      }

      // ////////////////////////
      // movement section

      // yield if we can't move right now
      if (S.movementController.isActive()) {
        X.yield();
        continue;
      }

      // move toward the goal mine
      if (goalMine != null) {
        // if we're standing on it, move off
        if (S.location.equals(goalMine)) {
          if (LittleUtil.moveOffSpotAsync()) {
          } else {
            X.yield();
          }
          continue;
        }

        // if we're not close enough, move toward it
        Direction d = BugPathfinding.bugTo(goalMine, 2);
        if (d.ordinal() < 8) {
          X.moveDirAsync(d);
        } else {
          X.yield();
        }
        continue;
      }

      // let's explore, I guess
      {
        Direction d = LittleExplorer.explore();
        if (d.ordinal() < 8) {
          if (S.direction != d) {
            X.setDirection(d);
          } else {
            X.moveForward();
          }
        } else {
          X.yield();
        }
        continue;
      }
    }
  }

  public static final void soldier() throws GameActionException {

    MapLocation prevLocation = S.location;
    int leaveThisPlace = 20;

    MapLocation goal = MapUtil.add(S.location, MapUtil.randomDirection(), 200);
    l1 : while (true) {

      // find the nearest empty mine
      MapLocation nearestMine = LittleUtil.nearestEmptyMine();
      if (nearestMine != null) {
        CommandType.LEGEN_MINE_HERE.ints[2] = nearestMine.x;
        CommandType.LEGEN_MINE_HERE.ints[3] = nearestMine.y;
        B.send(CommandType.LEGEN_MINE_HERE.ints);
      }

      // let's get out of here...
      if (S.location.distanceSquaredTo(prevLocation) <= 2) {
        leaveThisPlace--;
        if (leaveThisPlace < 0) {
          goal = MapUtil.add(S.location, MapUtil.randomDirection(), 200);
          leaveThisPlace = 20;
        }
      } else {
        prevLocation = S.location;
        leaveThisPlace = 20;
      }

      // ////////////////////////
      // attacking

      MapLocation bestTarget = null;
      RobotLevel bestLevel = null;
      double bestValue = Double.MAX_VALUE;

      MapLocation bestImmediateTarget = null;
      RobotLevel bestImmediateLevel = null;
      double bestImmediateValue = Double.MAX_VALUE;

      for (RobotInfo ri : S.senseRobots()) {
        if (ri.robot.getTeam() == S.enemyTeam) {
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

      if (bestImmediateTarget != null && !S.weaponControllers[0].isActive()) {
        S.weaponControllers[0].attackSquare(bestImmediateTarget,
            bestImmediateLevel);
      }

      // ////////////////////////
      // movement

      // yield if we can't move right now
      if (S.movementController.isActive()) {
        X.yield();
        continue;
      }

      if (bestTarget != null) {
        Direction d = BugPathfinding.bugTo(bestTarget, 16);
        if (d.ordinal() < 8) {
          X.moveDirAsync(d);
          continue;
        }
        d = S.location.directionTo(bestTarget);
        if (S.direction != d) {
          X.setDirection(d);
          continue;
        }
        X.yield();
        continue;
      }

      // move toward our goal
      Direction d = BugPathfinding.bugTo(goal, 0);
      if (d.ordinal() < 8) {
        X.moveDirAsync(d);
      }
      X.yield();
    }
  }
}
