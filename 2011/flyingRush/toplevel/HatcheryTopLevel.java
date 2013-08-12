package flyingRush.toplevel;

import flyingRush.blocks.RandomMovementBlock;
import flyingRush.blocks.SyncBuildBlock;
import flyingRush.core.PlayerConstants;
import flyingRush.core.S;
import flyingRush.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class HatcheryTopLevel {
  public static final void go() throws GameActionException {
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

    while (true) {
      // turn off if we're in a cluster, and we're not the leader
      {
        for (Direction d = Direction.WEST; d != Direction.EAST; d = d.rotateRight()) {
          RobotInfo ri = S.senseRobotInfo(S.location.add(d),
              RobotLevel.ON_GROUND);
          if (ri != null && ri.robot.getTeam() == S.team
              && ri.chassis == Chassis.BUILDING
              && ri.hitpoints >= Chassis.BUILDING.maxHp) {
            S.rc.turnOff();
            break;
          }
        }
      }

      // if we're damaged, turn on our neighbors
      {
        if (S.hp < S.chassis.maxHp) {
          for (RobotInfo ri : S.nearbyRobotInfos()) {
            if (ri.location.isAdjacentTo(S.location)
                && ri.robot.getTeam() == S.team
                && ri.chassis == Chassis.BUILDING && !ri.on) {
              S.rc.turnOn(ri.location, RobotLevel.ON_GROUND);
            }
          }
        }
      }

      // if we have enough flux, build a soldier
      {
        if (S.allowedToBuildNonMine(PlayerConstants.tier1FluxThreshold,
            Chassis.LIGHT.upkeep)) {
          Direction d = RandomMovementBlock.randomAvailableDirection();
          if (d != null) {
            MapLocation there = S.location.add(d);
            SyncBuildBlock.build(there, Chassis.LIGHT, ComponentType.BLASTER,
                ComponentType.RADAR, ComponentType.SHIELD);
          }
        }
      }

      X.yield();
    }
  }
}
