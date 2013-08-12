package legenClone.toplevel;

import legenClone.blocks.BuildBlock;
import legenClone.core.B;
import legenClone.core.Callback;
import legenClone.core.CommandType;
import legenClone.core.S;
import legenClone.core.X;
import legenClone.util.ComponentUtil;
import legenClone.util.LittleUtil;
import legenClone.util.MapUtil;
import legenClone.util.RobotUtil;
import legenClone.util.YingUtil;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Message;
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
    case ComponentUtil.RECYCLER_INT:
      if (role == ROLE_LEAD_RECYCLER) {
        if (!builtConstructor) {
          YingUtil.waitForFlux(totalCost, builderUpkeep + fleetUpkeep);
          BuildBlock.waitAndBuild(ComponentType.ANTENNA, S.location, RobotLevel.ON_GROUND);
          searchBuildLocation();
          if (buildLocation == null)
            return;
          BuildBlock.waitAndTryBuild(Chassis.LIGHT, ComponentType.CONSTRUCTOR, buildLocation);
          builtConstructor = true;
          while (B.bc == null || B.bc.isActive()) {
            X.yield();
          }
          Direction buildDir = S.location.directionTo(buildLocation).rotateRight();
          MapLocation toBuild = S.location.add(buildDir);
          CommandType.BUILD_LOC.ints[2] = toBuild.x;
          CommandType.BUILD_LOC.ints[3] = toBuild.y;
          B.send(CommandType.BUILD_LOC.ints);
          waitForArmory();
        }
        while (true) {
          buildFleet();
          YingUtil.p("Finished building");
        }
      } else {
        while (numLightConstructorBuilt < NUM_LIGHT_CONSTRUCTORS) {
          while (!S.allowedToBuild(Chassis.LIGHT, ComponentType.SIGHT, ComponentType.CONSTRUCTOR))
            X.yield();
          MapLocation loc = BuildBlock.waitAndTryBuild(Chassis.LIGHT, ComponentType.CONSTRUCTOR);
          if (loc != null) {
            BuildBlock.waitAndTryBuild(ComponentType.SIGHT, loc, RobotLevel.ON_GROUND);
            numLightConstructorBuilt++;
          }
        }
        while (true)
          X.yield();
      }
    case ComponentUtil.CONSTRUCTOR_INT:
      addConstructorMessageHandler();
      while (armoryLocation == null)
        X.yield();
      YingUtil.waitForFlux(armoryCost + fleetCost - GameConstants.POWER_WAKE_DELAY,
          Chassis.BUILDING.upkeep + fleetUpkeep);
      MapLocation there = armoryLocation;

      S.rc.setIndicatorString(0, "Armory: " + (there.x - S.locationX) + " , "
          + (there.y - S.locationY));

      BuildBlock.waitAndBuild(Chassis.BUILDING, there);
      BuildBlock.waitAndBuild(ComponentType.ARMORY, there, RobotLevel.ON_GROUND);
      S.rc.turnOff();
      break;
    case ComponentUtil.ARMORY_INT:
      addArmoryMessageHandler();
      YingUtil.p("added handler");
      while (leaderLocation == null) {
        X.yield();
      }
      while (true) {
        YingUtil.waitForFlux(fleetCost, fleetUpkeep);
        BuildBlock.waitAndTryBuild(Chassis.FLYING, S.location);
        Direction dirToLeader = S.location.directionTo(leaderLocation);
        BuildBlock.waitAndTryBuild(Chassis.FLYING, S.location.add(dirToLeader));
        if (dirToLeader.isDiagonal()) {
          BuildBlock.waitAndTryBuild(Chassis.FLYING, S.location.add(dirToLeader.rotateLeft()));
          BuildBlock.waitAndTryBuild(Chassis.FLYING, S.location.add(dirToLeader.rotateRight()));
        } else {
          if (!BuildBlock.offMap(S.location.add(dirToLeader.rotateLeft()))) {
            BuildBlock.waitAndTryBuild(Chassis.FLYING, S.location.add(dirToLeader.rotateLeft()));
            BuildBlock.waitAndTryBuild(Chassis.FLYING,
                S.location.add(dirToLeader.rotateLeft().rotateLeft()));
          } else {
            BuildBlock.waitAndTryBuild(Chassis.FLYING, S.location.add(dirToLeader.rotateRight()));
            BuildBlock.waitAndTryBuild(Chassis.FLYING,
                S.location.add(dirToLeader.rotateRight().rotateRight()));
          }
        }
      }
    default:
      break;
    }
  }

  public static void electLeader() throws GameActionException {
    for (RobotInfo ri : S.nearbyRobotInfo()) {
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
    B.addOnMessageHandler(new Callback() {
      public void onMessage(Message m) {
        if (m.ints[1] == CommandType.FLEET_ID.ordinal())
          fleetID = m.ints[2];
      }
    });
  }

  public static final void addConstructorMessageHandler() {
    B.addOnMessageHandler(new Callback() {
      public void onMessage(Message m) {
        if (m.ints[1] == CommandType.BUILD_LOC.ordinal())
          armoryLocation = new MapLocation(m.ints[2], m.ints[3]);
      }
    });
  }

  public static final void addArmoryMessageHandler() {
    B.addOnMessageHandler(new Callback() {
      public void onMessage(Message m) {
        if (m.ints[1] == CommandType.ARMORY_LEEDER.ordinal()) {
          leaderLocation = new MapLocation(m.ints[2], m.ints[3]);
          YingUtil.p("received message");
        }
      }
    });
  }

  public static void searchBuildLocation() throws GameActionException {
    RobotLevel l = RobotLevel.ON_GROUND;
    int startDir = Direction.SOUTH_WEST.ordinal();
    for (int i = 0; i < 8; i++) {
      MapLocation loc = S.location.add(MapUtil.intToDirection[(startDir + i) % 8]);
      if (BuildBlock.canBuild(loc, l)
          && BuildBlock.canBuild(S.location.add(MapUtil.intToDirection[(startDir + i + 1) % 8]), l)) {
        buildLocation = loc;
        break;
      }
    }
  }

  public static Direction dirOfDiagonalRecycler() throws GameActionException {
    Robot[] robots = S.sensorController.senseNearbyGameObjects(Robot.class);
    for (Robot r : robots) {
      RobotInfo ri = S.sensorController.senseRobotInfo(r);
      if (ri.chassis.ordinal() == RobotUtil.BUILDING_INT) {
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
      S.rc.setIndicatorString(0, "bflux = " + S.flux + " dflux = " + S.dFlux + " cost = "
          + fleetCost);
      int numFlyingRobots = 0;
      Robot[] robots = S.sensorController.senseNearbyGameObjects(Robot.class);
      for (Robot r : robots) {
        RobotInfo ri = S.sensorController.senseRobotInfo(r);
        if (ri.chassis.ordinal() == RobotUtil.FLYING_INT) {
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

    while (!S.allowedToBuild(null, ComponentType.RADAR, ComponentType.ANTENNA,
        ComponentType.BLASTER, ComponentType.BLASTER, ComponentType.CONSTRUCTOR,
        ComponentType.BLASTER, ComponentType.BLASTER)) {
      X.yield();
      S.rc.setIndicatorString(0, "flux = " + S.flux + " dflux = " + S.dFlux + " cost = "
          + fleetCost);
    }
    S.rc.setIndicatorString(0, "aflux = " + S.flux + " dflux = " + S.dFlux + " cost = " + fleetCost);
    BuildBlock.waitAndTryBuild(ComponentType.RADAR, northWest, RobotLevel.IN_AIR);
    BuildBlock.waitAndBuild(ComponentType.ANTENNA, northWest, RobotLevel.IN_AIR);
    BuildBlock.waitAndTryBuild(ComponentType.BLASTER, northWest.add(Direction.EAST),
        RobotLevel.IN_AIR);
    BuildBlock
        .waitAndBuild(ComponentType.BLASTER, northWest.add(Direction.EAST), RobotLevel.IN_AIR);
    BuildBlock.waitAndTryBuild(ComponentType.CONSTRUCTOR, northWest.add(Direction.SOUTH_EAST),
        RobotLevel.IN_AIR);
    BuildBlock.waitAndTryBuild(ComponentType.BLASTER, northWest.add(Direction.SOUTH),
        RobotLevel.IN_AIR);
    BuildBlock.waitAndBuild(ComponentType.BLASTER, northWest.add(Direction.SOUTH),
        RobotLevel.IN_AIR);
  }

  public static final void waitForArmory() throws Exception {
    while (true) {
      for (RobotInfo ri : S.senseRobots()) {
        if (LittleUtil.find(ri.components, ComponentType.ARMORY)) {
          for (int i = 0; i < 2; i++)
            X.yield();
          CommandType.ARMORY_LEEDER.ints[2] = S.locationX;
          CommandType.ARMORY_LEEDER.ints[3] = S.locationY;
          B.send(CommandType.ARMORY_LEEDER.ints);
          YingUtil.p("sent message");
          return;
        }
      }
      X.yield();
    }
  }
}
