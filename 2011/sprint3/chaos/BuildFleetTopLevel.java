package sprint3.chaos;

import sprint3.core.B;
import sprint3.core.CommandType;
import sprint3.core.D;
import sprint3.core.S;
import sprint3.core.U;
import sprint3.core.X;
import sprint3.core.xconst.XChassis;
import sprint3.core.xconst.XComponentType;
import sprint3.core.xconst.XDirection;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class BuildFleetTopLevel {
  public static final int ROLE_UNKNOW = 0;
  public static final int ROLE_LEAD_RECYCLER = 1;
  public static final int ROLE_OTHER_RECEYCLER = 2;

  public static final int NUM_LIGHT_CONSTRUCTORS = 2;

  public static int role = ROLE_UNKNOW;
  public static int fleetID;
  public static MapLocation armoryLocation;
  public static MapLocation buildLocation;
  public static boolean builtConstructor = false;
  public static MapLocation leaderLocation;
  public static int numLightConstructorBuilt = 0;

  public static int fleetCost = Chassis.FLYING.cost * 4 + ComponentType.RADAR.cost
      + ComponentType.ANTENNA.cost + ComponentType.BLASTER.cost * 4
      + ComponentType.CONSTRUCTOR.cost;

  public static int armoryCost = Chassis.BUILDING.cost + ComponentType.ARMORY.cost;

  public static int builderCost = Chassis.LIGHT.cost + ComponentType.CONSTRUCTOR.cost + armoryCost;

  public static double builderUpkeep = Chassis.LIGHT.upkeep + Chassis.BUILDING.upkeep;

  public static double fleetUpkeep = Chassis.FLYING.upkeep * 4;

  public static int totalCost = builderCost + fleetCost - GameConstants.POWER_WAKE_DELAY * 2;

  public static final void run() throws Exception {
    if (S.round <= 10) {
      electLeader();
    }
    // Builders
    while (S.builderController == null)
      X.yield();
    switch (S.builderTypeInt) {
    case XComponentType.RECYCLER_INT:
      if (role == ROLE_LEAD_RECYCLER) {
        if (!builtConstructor) {
          YingUtil.waitForFlux(totalCost, builderUpkeep + fleetUpkeep);
          SyncBuildBlock.waitAndBuild(ComponentType.ANTENNA, S.location, RobotLevel.ON_GROUND);
          searchBuildLocation();
          if (buildLocation == null)
            return;
          SyncBuildBlock.waitAndTryBuild(Chassis.LIGHT,  buildLocation, ComponentType.CONSTRUCTOR);
          builtConstructor = true;
          while (B.bc == null || B.bc.isActive()) {
            X.yield();
          }
          Direction buildDir = S.location.directionTo(buildLocation).rotateRight();
          MapLocation toBuild = S.location.add(buildDir);
          CommandType.BUILD.ints[2] = toBuild.x;
          CommandType.BUILD.ints[3] = toBuild.y;
          B.send(CommandType.BUILD.ints);
          waitForArmory();
        }
        while (true) {
          buildFleet();
          YingUtil.debug_p("Finished building");
        }
      } else {
        while (numLightConstructorBuilt < NUM_LIGHT_CONSTRUCTORS) {
          while (!S.allowedToBuildNonMine(Chassis.LIGHT, ComponentType.SIGHT, ComponentType.CONSTRUCTOR))
            X.yield();
          MapLocation loc = SyncBuildBlock.waitAndTryBuild(Chassis.LIGHT, ComponentType.CONSTRUCTOR);
          if (loc != null) {
            SyncBuildBlock.waitAndTryBuild(ComponentType.SIGHT, loc, RobotLevel.ON_GROUND);
            numLightConstructorBuilt++;
          }
        }
        while (true)
          X.yield();
      }
    case XComponentType.CONSTRUCTOR_INT:
      addConstructorMessageHandler();
      while (armoryLocation == null)
        X.yield();
      YingUtil.waitForFlux(armoryCost + fleetCost - GameConstants.POWER_WAKE_DELAY,
          Chassis.BUILDING.upkeep + fleetUpkeep);
      MapLocation there = armoryLocation;

      D.debug_setIndicator(0, "Armory: " + (there.x - S.locationX) + " , "
          + (there.y - S.locationY));

      SyncBuildBlock.waitAndBuild(Chassis.BUILDING, there);
      SyncBuildBlock.waitAndBuild(ComponentType.ARMORY, there, RobotLevel.ON_GROUND);
      S.rc.turnOff();
      break;
    case XComponentType.ARMORY_INT:
      addArmoryMessageHandler();
      YingUtil.debug_p("added handler");
      while (leaderLocation == null) {
        X.yield();
      }
      while (true) {
        YingUtil.waitForFlux(fleetCost, fleetUpkeep);
        SyncBuildBlock.waitAndTryBuild(Chassis.FLYING, S.location);
        Direction dirToLeader = S.location.directionTo(leaderLocation);
        SyncBuildBlock.waitAndTryBuild(Chassis.FLYING, S.location.add(dirToLeader));
        if (dirToLeader.isDiagonal()) {
          SyncBuildBlock.waitAndTryBuild(Chassis.FLYING, S.location.add(dirToLeader.rotateLeft()));
          SyncBuildBlock.waitAndTryBuild(Chassis.FLYING, S.location.add(dirToLeader.rotateRight()));
        } else {
          if (!SyncBuildBlock.offMap(S.location.add(dirToLeader.rotateLeft()))) {
            SyncBuildBlock.waitAndTryBuild(Chassis.FLYING, S.location.add(dirToLeader.rotateLeft()));
            SyncBuildBlock.waitAndTryBuild(Chassis.FLYING,
                S.location.add(dirToLeader.rotateLeft().rotateLeft()));
          } else {
            SyncBuildBlock.waitAndTryBuild(Chassis.FLYING, S.location.add(dirToLeader.rotateRight()));
            SyncBuildBlock.waitAndTryBuild(Chassis.FLYING,
                S.location.add(dirToLeader.rotateRight().rotateRight()));
          }
        }
      }
    default:
      break;
    }
  }

  public static void electLeader() throws GameActionException {
    for (RobotInfo ri : S.nearbyRobotInfos()) {
      if (ri.chassis != Chassis.BUILDING) {
        continue;
      }
      Direction d = S.location.directionTo(ri.location);      
      if (d == Direction.EAST || d == Direction.SOUTH) {
        role = ROLE_LEAD_RECYCLER;
        return;
      }
    }
    role = ROLE_OTHER_RECEYCLER;
  }

  public static final void addFleetMessageHandler() {
//    B.addOnMessageHandler(new Callback() {
//      public void onMessage(Message m) {
//        if (m.ints[1] == CommandType.FLEET_ID.ordinal())
//          fleetID = m.ints[2];
//      }
//    });
  }

  public static final void addConstructorMessageHandler() {
//    B.addOnMessageHandler(new Callback() {
//      public void onMessage(Message m) {
//        if (m.ints[1] == CommandType.BUILD_LOC.ordinal())
//          armoryLocation = new MapLocation(m.ints[2], m.ints[3]);
//      }
//    });
  }

  public static final void addArmoryMessageHandler() {
//    B.addOnMessageHandler(new Callback() {
//      public void onMessage(Message m) {
//        if (m.ints[1] == CommandType.ARMORY_LEEDER.ordinal()) {
//          leaderLocation = new MapLocation(m.ints[2], m.ints[3]);
//          YingUtil.debug_p("received message");
//        }
//      }
//    });
  }

  public static void searchBuildLocation() throws GameActionException {
    RobotLevel l = RobotLevel.ON_GROUND;
    int startDir = Direction.SOUTH_WEST.ordinal();
    for (int i = 0; i < 8; i++) {
      MapLocation loc = S.location.add(XDirection.intToDirection[(startDir + i) % 8]);
      if (SyncBuildBlock.canBuild(loc, l)
          && SyncBuildBlock.canBuild(S.location.add(XDirection.intToDirection[(startDir + i + 1) % 8]), l)) {
        buildLocation = loc;
        break;
      }
    }
  }

  public static Direction dirOfDiagonalRecycler() throws GameActionException {
    Robot[] robots = S.sensorController.senseNearbyGameObjects(Robot.class);
    for (Robot r : robots) {
      RobotInfo ri = S.sensorController.senseRobotInfo(r);
      if (ri.chassis.ordinal() == XChassis.BUILDING_INT) {
        Direction d = S.location.directionTo(ri.location);
        if (d.isDiagonal())
          return d;
      }
    }
    return null;
  }

  public static final void buildFleet() throws GameActionException {
    MapLocation northWest = null;
    while (true) {
      D.debug_setIndicator(0, "bflux = " + S.flux + " dflux = " + S.dFlux + " cost = "
          + fleetCost);
      int numFlyingRobots = 0;
      Robot[] robots = S.sensorController.senseNearbyGameObjects(Robot.class);
      for (Robot r : robots) {
        RobotInfo ri = S.sensorController.senseRobotInfo(r);
        if (ri.chassis.ordinal() == XChassis.FLYING_INT) {
          numFlyingRobots++;
          if (northWest == null) {
            northWest = ri.location;
          } else {
            if (ri.location.x <= northWest.x && ri.location.y <= northWest.y)
              northWest = ri.location;
          }
        }
      }
      if (numFlyingRobots < 4)
        X.yield();
      else
        break;
    }

    while (!S.allowedToBuildNonMine(null, ComponentType.RADAR, ComponentType.ANTENNA,
        ComponentType.BLASTER, ComponentType.BLASTER, ComponentType.CONSTRUCTOR,
        ComponentType.BLASTER, ComponentType.BLASTER)) {
      X.yield();
      D.debug_setIndicator(0, "flux = " + S.flux + " dflux = " + S.dFlux + " cost = "
          + fleetCost);
    }
    D.debug_setIndicator(0, "aflux = " + S.flux + " dflux = " + S.dFlux + " cost = " + fleetCost);
    SyncBuildBlock.waitAndTryBuild(ComponentType.RADAR, northWest, RobotLevel.IN_AIR);
    SyncBuildBlock.waitAndBuild(ComponentType.ANTENNA, northWest, RobotLevel.IN_AIR);
    SyncBuildBlock.waitAndTryBuild(ComponentType.BLASTER, northWest.add(Direction.EAST),
        RobotLevel.IN_AIR);
    SyncBuildBlock
        .waitAndBuild(ComponentType.BLASTER, northWest.add(Direction.EAST), RobotLevel.IN_AIR);
    SyncBuildBlock.waitAndTryBuild(ComponentType.CONSTRUCTOR, northWest.add(Direction.SOUTH_EAST),
        RobotLevel.IN_AIR);
    SyncBuildBlock.waitAndTryBuild(ComponentType.BLASTER, northWest.add(Direction.SOUTH),
        RobotLevel.IN_AIR);
    SyncBuildBlock.waitAndBuild(ComponentType.BLASTER, northWest.add(Direction.SOUTH),
        RobotLevel.IN_AIR);
  }

  public static final void waitForArmory() throws Exception {
    while (true) {
      for (RobotInfo ri : S.nearbyRobotInfos()) {
        if (U.find(ri.components, ComponentType.ARMORY)) {
          for (int i = 0; i < 2; i++)
            X.yield();
          CommandType.ARMORY_LEEDER.ints[2] = S.locationX;
          CommandType.ARMORY_LEEDER.ints[3] = S.locationY;
          B.send(CommandType.ARMORY_LEEDER.ints);
          YingUtil.debug_p("sent message");
          return;
        }
      }
      X.yield();
    }
  }
}
