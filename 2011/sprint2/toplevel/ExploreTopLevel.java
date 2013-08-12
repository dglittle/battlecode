package sprint2.toplevel;

import sprint2.blocks.BuildBlock;
import sprint2.core.B;
import sprint2.core.Callback;
import sprint2.core.CommandType;
import sprint2.core.S;
import sprint2.core.X;
import sprint2.util.ComponentUtil;
import sprint2.util.RobotUtil;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Mine;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class ExploreTopLevel {
  public static MapLocation home;
  public static Direction moveDir;
  public enum Role {CONSTRUCTOR, MINER, EXPLORER, NOTHING};
  public static Role role;
  public static boolean reached = false;
  public static MapLocation mineLoc;
  public static MapLocation constructorLoc;
  
  public static final void run() throws Exception {
    assignRoles();
    addMessgeHandlers();
    switch(S.chassisInt) {
      case RobotUtil.LIGHT_INT:
      case RobotUtil.BUILDING_INT:
        BuildTopLevel.run();
        break;
      case RobotUtil.FLYING_INT:
        home = S.location;
        moveDir = S.direction;
        while (S.sensorController == null && S.builderController == null)
          X.yield();
        if (S.sensorTypeInt == ComponentUtil.TELESCOPE_INT) {
          while(!reached) {
            if (!S.sensorController.isActive() && !S.movementController.isActive()) {
              Mine[] mines = S.sensorController.senseNearbyGameObjects(Mine.class);
              for (Mine m : mines) {
                if (m.getLocation().distanceSquaredTo(home) > 8) {
                  reached = true;
                  mineLoc = m.getLocation();
                  break;
                }
              }
              if (!X.tryMove(moveDir)) {
                moveDir = moveDir.rotateLeft();
              }
            } else {
              X.yield();
            }
          }
          reached = false;
          while(!reached) {
            if (!S.movementController.isActive()) {
              int dist = S.location.distanceSquaredTo(home);
              if ( dist < 16) {
                if (dist < 2)
                  reached = true;
                CommandType.MINE.ints[2] = mineLoc.x;
                CommandType.MINE.ints[3] = mineLoc.y;
                B.send(CommandType.MINE.ints);
              }
              if (!X.tryMove(S.location.directionTo(home)))
                X.yield();
            } else {
              X.yield();
            }
          }
          S.rc.turnOff();
        } else {
          while (mineLoc == null)
            X.yield();
          while (true) {
            if (S.location.distanceSquaredTo(mineLoc) <= 2) 
              break;
            if (!S.movementController.isActive()) {
              Direction d = S.location.directionTo(mineLoc);
              if (!X.tryMove(d))
                X.tryMove(d.rotateLeft());
            } else {
              X.yield();
            }
          }
          Chassis chassis = Chassis.BUILDING;
          BuildBlock.waitAndBuild(chassis, mineLoc);
          BuildBlock.waitAndBuild(ComponentType.RECYCLER, mineLoc, RobotLevel.ON_GROUND);
        }
        break;
      default:
        break;
    }
  }
  
  public static final void assignRoles() {
    if (S.round < 2) {
      // beginning robots
      if (S.chassisInt == RobotUtil.BUILDING_INT) {
        for (RobotInfo ri : S.nearbyRobotInfo()) {
          if (ri.chassis == Chassis.LIGHT
              && !ri.location.directionTo(S.location).isDiagonal()) {
            role = Role.MINER;
            constructorLoc = ri.location;
            break;
          }
        }
      } else if (S.chassis == Chassis.LIGHT) {
        role = Role.CONSTRUCTOR;
      }
    }
  }
  
  public static final void addMessgeHandlers() {
    B.addOnMessageHandler(new Callback() {
      @Override
      public void onMessage(Message m) {
        if (m.ints[1] == CommandType.MINE.ordinal()) {
          mineLoc = new MapLocation(m.ints[2], m.ints[3]);
          return;  
        }
        if (m.ints[1] == CommandType.BUILD.ordinal()) {
          int numLocs = (m.ints.length - 3) / 2;
          BuildTopLevel.flyLoc = new MapLocation[numLocs];
          for (int i = 0; i < numLocs; i++) {
            BuildTopLevel.flyLoc[i] = new MapLocation(m.ints[i * 2 + 3], m.ints[i * 2 + 4]);
          }
          return;
        }
      }
    });
  }
}
