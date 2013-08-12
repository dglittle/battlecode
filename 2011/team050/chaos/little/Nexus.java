package team050.chaos.little;

import java.util.HashSet;
import java.util.Set;

import team050.blocks.LittleExplorationBlock;
import team050.blocks.SyncBuildBlock;
import team050.blocks.pathfinding.BugPathfindingBlock;
import team050.core.D;
import team050.core.PlayerConstants;
import team050.core.Role;
import team050.core.S;
import team050.core.U;
import team050.core.X;
import battlecode.common.BuildMappings;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class Nexus {

  public static int soldierCount = 0;

  public static final Role whatToBuild() {
    int rand = ((S.round / 15) * 776531419) % 100;

    if (S.dFlux < 300) {
      if (soldierCount++ < 10)
        return Role.SOLDIER;
      else
        return null;
    } else {
      return Role.HEAVY_SOLDIER;
    }
  }

  public static final void buildingGo() throws Exception {
    // what are we?
    while (true) {
      if (U.hasComponentsReady(ComponentType.RECYCLER)) {
        S.rc.turnOff();
        S.role = Role.RECYCLER;
        break;
      }
      if (U.hasComponentsReady(ComponentType.ARMORY)) {
        S.role = Role.ARMORY;
        break;
      }
      if (U.hasComponentsReady(ComponentType.FACTORY)) {
        S.role = Role.FACTORY;
        break;
      }
      X.yield();
    }
    U.waitForComponents();

    // is there some dancing guy we can look at?
    MapLocation dancer = null;
    int[] counts = new int[8];
    l1 : for (int i = 0; i < 10; i++) {
      for (RobotInfo ri : S.nearbyRobotInfos()) {
        if (ri.location.isAdjacentTo(S.location)
            && ri.chassis != Chassis.BUILDING && ri.robot.getTeam() == S.team) {
          Direction dirTo = S.location.directionTo(ri.location);
          int index = dirTo.ordinal();
          boolean lookingAtMe = ri.direction == dirTo.opposite();
          if (counts[index] == 0 && lookingAtMe)
            counts[index] = 1;
          else if (counts[index] == 1 && !lookingAtMe)
            counts[index] = 2;
          else if (counts[index] == 2 && lookingAtMe) {
            dancer = ri.location;
            break l1;
          }
        }
      }
      X.yield();
    }
    if (dancer == null) {
      dancer = S.front;
    }
    if (S.direction != S.location.directionTo(dancer)) {
      X.setDirection(S.location.directionTo(dancer));
      X.yield();
    }

    if (S.builderType.equals(ComponentType.RECYCLER)) {
      for (int i = 0; i < 3; i++) {
        while (!S.allowedToBuildNonMine(Role.COLONIST.chassis,
            Role.COLONIST.components) || S.sensorController.senseObjectAtLocation(S.front, RobotLevel.ON_GROUND) != null)
          X.yield();
        SyncBuildBlock.build(S.front, Role.COLONIST);
      }
    }

    // do our thang
    Set<Integer> ids = new HashSet<Integer>();
    while (true) {
      try {
        // build stuff
        Role role = whatToBuild();

        if (role != null) {

          // is there a chassis?
          RobotInfo ri = S.senseRobotInfo(S.front, role.chassis.level);

          // if not, build chassis
          if (ri == null && S.round % 15 == 0
              && S.flux > PlayerConstants.nexusBuildThreshold && S.dFlux > 1.0) {
            if (BuildMappings.canBuild(S.builderType, role.chassis)) {
              S.builderController.build(role.chassis, S.front);
              X.yield();
            }
          }

          // if there is a chassis,
          // put stuff in it
          if (ri != null && ids.add(ri.robot.getID())) {
            for (ComponentType ct : role.components) {
              if (BuildMappings.canBuild(S.builderType, ct)) {
                while (S.flux < ct.cost)
                  X.yield();
                if (S.builderController.isActive())
                  X.yield();
                S.builderController.build(ct, S.front, role.chassis.level);
              }
            }
          }
        }

        X.yield();
      } catch (Exception e) {
        D.debug_logException(e);
      }
    }
  }

  public static final void constructorBuildBaseSync()
      throws GameActionException {
    // step 1: find a suitable location next to a recycler
    while (true) {
      // go to a spot by a recycler
      FindSpotByRecycler.go();

      // is this spot good?
      int count = 0;
      for (Direction d : U.directions) {
        if (S.movementController.canMove(d)) {
          count++;
        }
      }
      if (count >= 3) {
        break;
      } else {
        FindSpotByRecycler.potential.remove(S.location);
        FindSpotByRecycler.ignore.add(S.location);
      }
    }

    // step 2: build the armory and factory
    while (!S.allowedToBuildNonMine(Chassis.BUILDING.cost * 2
        + ComponentType.ARMORY.cost + ComponentType.FACTORY.cost,
        Chassis.BUILDING.upkeep)) {
      X.yield();
    }
    MapLocation armory = null;
    for (Direction d : U.directions) {
      if (S.movementController.canMove(d)) {
        SyncBuildBlock.build(armory = S.location.add(d), Chassis.BUILDING,
            ComponentType.ARMORY);
        X.yield();
        break;
      }
    }
    MapLocation factory = null;
    for (Direction d : U.directions) {
      if (S.movementController.canMove(d)) {
        SyncBuildBlock.build(factory = S.location.add(d), Chassis.BUILDING,
            ComponentType.FACTORY);
        X.yield();
        break;
      }
    }

    // step 3: turn on the recycler
    MapLocation recycler = null;
    for (Direction d : U.directions) {
      if (!S.movementController.canMove(d)) {
        MapLocation loc = S.location.add(d);
        if (!S.sensorController.canSenseSquare(loc)) {
          X.setDirection(d);
          X.yield();
        }
        Mine m = S.senseMine(loc);
        if (m != null) {
          RobotInfo ri = S.senseRobotInfo(loc, RobotLevel.ON_GROUND);
          if (ri != null && ri.chassis == Chassis.BUILDING
              && ri.robot.getTeam() == S.team) {
            if (!ri.on)
              S.rc.turnOn(loc, RobotLevel.ON_GROUND);
            recycler = loc;
            break;
          }
        }
      }
    }

    // step 4: dance until everyone is looking at us
    boolean[] noticedBy = new boolean[3];
    while (true) {
      if (S.direction == S.location.directionTo(recycler)) {
        RobotInfo ri = S.senseRobotInfo(S.front, RobotLevel.ON_GROUND);
        noticedBy[0] = ri.direction == S.direction.opposite();
        X.setDirection(S.location.directionTo(armory));
      } else if (S.direction == S.location.directionTo(armory)) {
        RobotInfo ri = S.senseRobotInfo(S.front, RobotLevel.ON_GROUND);
        noticedBy[1] = ri.direction == S.direction.opposite();
        X.setDirection(S.location.directionTo(factory));
      } else if (S.direction == S.location.directionTo(factory)) {
        RobotInfo ri = S.senseRobotInfo(S.front, RobotLevel.ON_GROUND);
        noticedBy[2] = ri.direction == S.direction.opposite();
        X.setDirection(S.location.directionTo(recycler));
      } else {
        X.setDirection(S.location.directionTo(recycler));
      }
      if (noticedBy[0] && noticedBy[1] && noticedBy[2])
        break;
      X.yield();
    }
  }
}

class FindSpotByRecycler {

  public static final void go() throws GameActionException {
    MapLocation goal = null;
    int spin = 3;
    while (true) {
      // find new goal
      while (goal == null) {
        if (potential.size() == 0)
          updatePotentials();
        goal = findNearestPotential();

        // does this spot suck?
        if (goal != null) {
          if (S.sensorController.canSenseSquare(goal)
              && !S.location.equals(goal)
              && (!S.rc.senseTerrainTile(goal).isTraversableAtHeight(
                  RobotLevel.ON_GROUND) || (S.senseRobot(goal,
                  RobotLevel.ON_GROUND) != null))) {
            potential.remove(goal);
            ignore.add(goal);
            goal = null;
          } else {
            break;
          }
        } else {
          break;
        }
      }

      // are we there?
      if (S.location.equals(goal)) {
        break;
      }

      // move toward goal
      if (goal != null) {
        BugPathfindingBlock.bugTowardAsync(goal);
      } else {
        if (spin > 0) {
          spin--;
          X.setDirection(S.direction.rotateLeft().rotateLeft());
        } else {
          LittleExplorationBlock.exploreMove();
        }
      }
      X.yield();
    }
  }

  public static Set<MapLocation> recylcer = new HashSet<MapLocation>();
  public static Set<MapLocation> ignore = new HashSet<MapLocation>();
  public static Set<MapLocation> potential = new HashSet<MapLocation>();

  public static final void updatePotentials() throws GameActionException {
    for (Mine m : S.senseMines()) {
      RobotInfo ri = S.senseRobotInfo(m.getLocation(), RobotLevel.ON_GROUND);
      if (ri != null && ri.chassis == Chassis.BUILDING
          && ri.robot.getTeam() == S.team) {
        if (recylcer.add(ri.location)) {
          for (Direction d : U.directions) {
            MapLocation loc = ri.location.add(d);
            if (!ignore.contains(loc))
              potential.add(loc);
          }
        }
      }
    }
  }

  public static final MapLocation findNearestPotential() {
    MapLocation best = null;
    int bestDist = Integer.MAX_VALUE;
    for (MapLocation loc : potential) {
      int dist = S.location.distanceSquaredTo(loc);
      if (dist < bestDist) {
        bestDist = dist;
        best = loc;
      }
    }
    return best;
  }
}
