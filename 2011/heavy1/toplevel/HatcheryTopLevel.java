package heavy1.toplevel;

import heavy1.blocks.DefensiveMineBlock;
import heavy1.blocks.RandomMovementBlock;
import heavy1.blocks.SyncBuildBlock;
import heavy1.core.D;
import heavy1.core.PlayerConstants;
import heavy1.core.Role;
import heavy1.core.S;
import heavy1.core.X;
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
    DefensiveMineBlock.setDefenseParameters(2, ComponentType.RADAR);

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
        DefensiveMineBlock.setDefenseParameters(1, ComponentType.RADAR);
        S.rc.turnOff();
      }
    }
    
    // turn off if we're in a cluster, and we're not the leader
    while (!_isClusterLeader()) {
      S.rc.turnOff();
      X.yield();
    }
    
    while (true) {
      
      DefensiveMineBlock.async();
      // if we're damaged, turn on our neighbors
      {
        if (S.hp < S.chassis.maxHp || 
            (S.birthRound < 100 && S.flux > PlayerConstants.tier1FluxThreshold)) {
          for (RobotInfo ri : S.nearbyRobotInfos()) {
            if (ri.location.isAdjacentTo(S.location)
                && ri.robot.getTeam() == S.team
                && ri.chassis == Chassis.BUILDING && !ri.on) {
              S.rc.turnOn(ri.location, RobotLevel.ON_GROUND);
            }
          }
        }
      }

      // if we have enough flux, build a constructor
      {
        if (S.allowedToBuildNonMine(PlayerConstants.tier1FluxThreshold,
            Chassis.LIGHT.upkeep) && S.rand.nextInt(2) < 1) {
          Direction d = RandomMovementBlock.randomAvailableDirection();
          if (d != null) {
            MapLocation there = S.location.add(d);
            SyncBuildBlock.build(there, Role.COLONIST.chassis, Role.COLONIST.components);
          }
        }
      }

      X.yield();
    }
  }
  
  public static final boolean _isClusterLeader() {
    try {
      for (Direction d = Direction.WEST; d != Direction.EAST; d = d.rotateRight()) {
        RobotInfo ri = S.senseRobotInfo(S.location.add(d), RobotLevel.ON_GROUND);
        if (ri != null && ri.robot.getTeam() == S.team
            && ri.chassis == Chassis.BUILDING
            && ri.hitpoints >= Chassis.BUILDING.maxHp && ri.on) {
          return false;
        }
      }
    } catch (GameActionException gae) {
      D.debug_logException(gae);
      return true;
    }
    return true;
  }
}
