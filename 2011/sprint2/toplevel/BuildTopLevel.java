package sprint2.toplevel;

import sprint2.blocks.BuildBlock;
import sprint2.core.B;
import sprint2.core.CommandType;
import sprint2.core.S;
import sprint2.core.X;
import sprint2.toplevel.ExploreTopLevel.Role;
import sprint2.util.ComponentUtil;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;

/** Top-level strategy for auras. */
public class BuildTopLevel {
  public static MapLocation[] flyLoc;
  
  public static final void run() throws Exception {
    while (S.builderController == null) {
      X.yield();
    }
    switch(S.builderTypeInt) {
      case ComponentUtil.CONSTRUCTOR_INT:
        BuildBlock.waitAndTryBuild(Chassis.BUILDING, ComponentType.ARMORY);
        BuildBlock.waitAndTryBuild(Chassis.BUILDING, ComponentType.FACTORY);
        CommandType.BUILD.ints[2] = ComponentUtil.ARMORY_INT;
        CommandType.BUILD.ints[3] = S.locationX;
        CommandType.BUILD.ints[4] = S.locationY;
        CommandType.BUILD.ints[5] = S.locationX + 1;
        CommandType.BUILD.ints[6] = S.locationY;
        B.send(CommandType.BUILD.ints);
        S.rc.turnOff();
        break;
      case ComponentUtil.ARMORY_INT:
        while (flyLoc == null) 
          X.yield();
        for (MapLocation loc : flyLoc) {
          BuildBlock.waitAndTryBuild(Chassis.FLYING, loc);
        }
        S.rc.turnOff();
        break;
      case ComponentUtil.FACTORY_INT:
        BuildBlock.waitAndTryBuild(ComponentType.TELESCOPE, RobotLevel.IN_AIR);
        S.rc.turnOff();
        break;
      case ComponentUtil.RECYCLER_INT:
        if (ExploreTopLevel.role == Role.MINER) {
          BuildBlock.waitAndBuild(ComponentType.ANTENNA, ExploreTopLevel.constructorLoc, 
              RobotLevel.ON_GROUND);
          BuildBlock.waitAndTryBuild(ComponentType.ANTENNA, RobotLevel.IN_AIR);
          BuildBlock.waitAndTryBuild(ComponentType.CONSTRUCTOR, RobotLevel.IN_AIR);
        }
        
        break;
      default:
        break;
    }
  }
  
  public static final void wait(int numRounds) throws GameActionException {
    int r = 0;
    while (r < numRounds) {
      X.yield();
      r++;
    }
  }
}
