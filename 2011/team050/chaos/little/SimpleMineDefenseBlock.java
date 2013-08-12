package team050.chaos.little;

import team050.blocks.AttackUtil;
import team050.core.D;
import team050.core.S;
import team050.core.U;
import team050.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class SimpleMineDefenseBlock {

  public static final void turnOnNeighborsDuringEmergencyAsync(boolean allowTurn)
      throws GameActionException {
    // build radar if we can afford it, and don't have it
    if (S.sensorController.type() == ComponentType.BUILDING_SENSOR) {
      if (S.flux > ComponentType.RADAR.cost) {
        try {
          S.builderController.build(ComponentType.RADAR, S.location, S.level);
        } catch (Exception e) {
        }
      }
    }

    // should we panic?
    boolean panic = false;

    // do we see the enemy?
    AttackUtil.setTargets();
    if (AttackUtil.bestTarget != null) {
      panic = true;
    }

    // are we damaged?
    if (S.hp < S.chassis.maxHp) {
      panic = true;
    }

    // in times of panic, wake up our neighbors
    if (panic) {
      for (RobotInfo ri : S.nearbyRobotInfos()) {
        if (ri.location.isAdjacentTo(S.location)
            && ri.robot.getTeam() == S.team && ri.chassis == Chassis.BUILDING
            && !ri.on) {
          S.rc.turnOn(ri.location, RobotLevel.ON_GROUND);
        }
      }
    } else {
      // turn around.. maybe there is reason to panic behind us..
      if (S.motorReady && allowTurn) {
        X.setDirection(S.direction.opposite());
      }
    }
  }

  public static int lastAttackRound = 0;

  public static final void sync(boolean sleepOverride)
      throws GameActionException {

    D.debug_setIndicator(0, "I am simple.");

    // build some defense
    try {
      U.waitForFlux(ComponentType.RADAR.cost);
      S.builderController.build(ComponentType.RADAR, S.location, S.level);
      X.yield();
      try {
        U.waitForFlux(ComponentType.BLASTER.cost);
        S.builderController.build(ComponentType.BLASTER, S.location, S.level);
        X.yield();
      } catch (Exception e) {
        try {
          U.waitForFlux(ComponentType.SMG.cost);
          S.builderController.build(ComponentType.SMG, S.location, S.level);
          X.yield();
        } catch (Exception ee) {
        }
      }
    } catch (Exception e) {
    }

    if (sleepOverride) {
      S.rc.turnOff();
    }

    // defend
    while (true) {
      if (S.rc.wasTurnedOff()) {
        lastAttackRound = S.round;
      }

      // attacking...
      if (S.weaponControllers.length > 0) {
        // if we see stuff, attack
        if (!AttackUtil.attackAsync()) {
          // otherwise, turn around and see if we see anything there
          if (S.motorReady) {
            X.setDirection(S.direction.opposite());
          }
        }
        turnOnNeighborsDuringEmergencyAsync(false);
      } else {
        turnOnNeighborsDuringEmergencyAsync(true);
      }

      // think about shutting down...
      if (lastAttackRound < S.round - 20) {
        // if we're alone, turn off if we're an even id
        boolean nextToOtherMine = false;
        for (Mine m : S.senseMines()) {
          if (m.getLocation().isAdjacentTo(S.location)) {
            nextToOtherMine = true;
            break;
          }
        }
        if (!nextToOtherMine) {
          if (S.id % 2 == 0)
            S.rc.turnOff();
        }

        // turn off if we're in a cluster, and we're not the leader
        {
          for (Direction d = Direction.WEST; d != Direction.EAST; d = d.rotateRight()) {
            RobotInfo ri = S.buildingSenseRobotInfo(S.location.add(d),
                RobotLevel.ON_GROUND);
            if (ri != null && ri.robot.getTeam() == S.team
                && ri.chassis == Chassis.BUILDING
                && ri.hitpoints >= Chassis.BUILDING.maxHp) {
              S.rc.turnOff();
              break;
            }
          }
        }
      }

      X.yield();
    }
  }
}
