package heavy1.toplevel;

import java.util.HashSet;
import java.util.Set;

import heavy1.blocks.RandomMovementBlock;
import heavy1.blocks.SyncBuildBlock;
import heavy1.core.D;
import heavy1.core.S;
import heavy1.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class LandaHeavyTopLevel {

  public static final void runArmory() throws GameActionException {
    l1: while (true) {
      X.yield();
      RobotInfo[] nearbyRobotInfos = S.nearbyRobotInfos();
      String whatISee = "Robots around me:";
      for (RobotInfo ri : nearbyRobotInfos) {
        whatISee += " " + ri.chassis;
      }
      D.debug_setIndicator(0, whatISee);
      for (RobotInfo ri : nearbyRobotInfos) {
        if (ignoreBuildRobotIDs.contains(ri.robot.getID()) || !ri.chassis.equals(Chassis.HEAVY)) continue;
        if (ri.location.isAdjacentTo(S.location)) {
          D.debug_pl("runArmory", "Can build equipment.");
          while (S.builderController.isActive()) X.yield();
          D.debug_pl("runArmory", "Building a jump...");
          S.builderController.build(ComponentType.JUMP, ri.location, ri.robot.getRobotLevel());
          D.debug_pl("runArmory", "...done.");
          while (S.builderController.isActive()) X.yield();
          D.debug_pl("runArmory", "Building a jump...");
          S.builderController.build(ComponentType.JUMP, ri.location, ri.robot.getRobotLevel());
          while (S.builderController.isActive()) X.yield();
          ignoreBuildRobotIDs.add(ri.robot.getID());
          continue l1;
        }
      }
    }
  }

  public static final void runFactory() throws GameActionException {
    RobotInfo[] nearbyRobotInfos = S.nearbyRobotInfos();
    for (RobotInfo ri : nearbyRobotInfos) {
      if (ri.chassis == Chassis.LIGHT) {
        buildLocation = ri.location;
      }
    }
    X.yield();
    D.debug_setIndicator(1, "Build location: " + buildLocation);
    while (true) {
      if (S.sensorController.senseObjectAtLocation(buildLocation, RobotLevel.ON_GROUND) == null)
        buildHeavyWithEquipment(buildLocation);
      else X.yield();
    }
  }
  
  public static final void runRecycler() throws GameActionException {
    l2: while (true) {
      X.yield();
      RobotInfo[] nearbyRobotInfos = S.nearbyRobotInfos();
      String whatISee = "Robots around me:";
      for (RobotInfo ri : nearbyRobotInfos) {
        whatISee += " " + ri.chassis;
      }
      D.debug_setIndicator(0, whatISee);
      for (RobotInfo ri : nearbyRobotInfos) {
        if (ignoreBuildRobotIDs.contains(ri.robot.getID()) || !ri.chassis.equals(Chassis.HEAVY)) continue;
        if (ri.location.isAdjacentTo(S.location)) {
          while (S.builderController.isActive()) X.yield();
          D.debug_pl("runRecycler", "Building a radar...");
          S.builderController.build(ComponentType.RADAR, ri.location, ri.robot.getRobotLevel());
          ignoreBuildRobotIDs.add(ri.robot.getID());
          continue l2;
        }
      }
      if (S.round > 500 && S.allowedToBuildNonMine(Chassis.LIGHT, ComponentType.CONSTRUCTOR, ComponentType.SIGHT, ComponentType.PLATING)) {
        SyncBuildBlock.buildColonist();
      }
    }
  }

  // ----------------------------------------------------------------------

  public static final void buildHeavyWithEquipment(MapLocation where) throws GameActionException {
    if (alreadySpawned++ >= MAXIMUM_NUMBER_HEAVY_SOLDIERS) return;
    while (!S.allowedToBuildNonMine(Chassis.HEAVY, ComponentType.JUMP, ComponentType.JUMP, ComponentType.RAILGUN, ComponentType.RADAR, ComponentType.REGEN)) {
      X.yield();
    }
    D.debug_pl("buildHeavyWithEquipment", "Trying to build a heavy...");
    SyncBuildBlock.build(where, Chassis.HEAVY, ComponentType.RAILGUN, ComponentType.REGEN);
  }
  
  // ----------------------------------------------------------------------
  
  public static final int MAXIMUM_NUMBER_HEAVY_SOLDIERS = 2;
  public static Set<Integer> ignoreBuildRobotIDs = new HashSet<Integer>();
  public static int alreadySpawned = 0;
  public static MapLocation buildLocation = null;

}
