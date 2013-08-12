package flyingRush.blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import flyingRush.core.PlayerConstants;
import flyingRush.core.S;
import flyingRush.core.U;
import flyingRush.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class FlyingRush {
  public static final void mainRecycler() throws GameActionException {

    Set<Integer> flyers = new HashSet<Integer>();

    MapLocation[] adjacentCache = new MapLocation[9];
    for (Direction d : U.directions) {
      MapLocation loc = S.location.add(d);
      adjacentCache[d.ordinal()] = loc;
    }
    adjacentCache[8] = S.location;

    int count = 0;
    l1 : while (true) {
      try {

        // if there is a new friendly flyer near us,
        // equip it with stuff
        for (MapLocation loc : adjacentCache) {
          RobotInfo ri = S.senseRobotInfo(loc, RobotLevel.IN_AIR);
          if (ri != null && ri.robot.getTeam() == S.team) {
            if (flyers.add(ri.robot.getID())) {
              // we got a new one...

              if (count < 4 || S.rand.nextDouble() < 0.4) {
                // wait for enough flux to build
                while (S.flux < ComponentType.BLASTER.cost)
                  X.yield();
                S.builderController.build(ComponentType.BLASTER, loc,
                    RobotLevel.IN_AIR);
                X.yield();
                while (S.flux < ComponentType.RADAR.cost)
                  X.yield();
                S.builderController.build(ComponentType.RADAR, loc,
                    RobotLevel.IN_AIR);
                X.yield();
                count++;
                continue l1;
              } else {
                // wait for enough flux to build
                while (S.flux < ComponentType.CONSTRUCTOR.cost)
                  X.yield();
                S.builderController.build(ComponentType.CONSTRUCTOR, loc,
                    RobotLevel.IN_AIR);
                X.yield();
                while (S.flux < ComponentType.SIGHT.cost)
                  X.yield();
                S.builderController.build(ComponentType.SIGHT, loc,
                    RobotLevel.IN_AIR);
                X.yield();
                continue l1;
              }
            }
          }
        }
      } catch (Exception e) {

      }
      X.yield();
    }
  }

  public static final void mainArmory() throws Exception {
    U.waitForComponents();

    // where is the main recylcer?
    MapLocation mainRecycler = null;
    for (Direction d : U.directions) {
      MapLocation loc = S.location.add(d);
      RobotInfo ri = S.senseRobotInfo(loc, RobotLevel.ON_GROUND);
      if (ri != null && ri.on && ri.chassis == Chassis.BUILDING) {
        mainRecycler = loc;
        break;
      }
    }

    // what positions are near enough to the main recylcer?
    ArrayList<MapLocation> buildPositions = new ArrayList<MapLocation>();
    for (Direction d : U.directions) {
      MapLocation loc = S.location.add(d);
      if (loc.distanceSquaredTo(mainRecycler) <= 2) {
        buildPositions.add(loc);
      }
    }
    if (S.location.distanceSquaredTo(mainRecycler) <= 2) {
      buildPositions.add(S.location);
    }

    int count = 0;
    while (true) {

      // see if the conditions are ripe to build a flying guy
      {
        // first, do we have enough flux?
        if (S.allowedToBuildNonMine(PlayerConstants.t1_unitCost,
            Chassis.FLYING.upkeep)) {

          // next, is there a square available next to the main recycler?
          MapLocation buildHere = null;
          for (MapLocation loc : buildPositions) {
            Robot r = S.senseRobot(loc, RobotLevel.IN_AIR);
            if (r == null) {
              buildHere = loc;
              break;
            }
          }

          if (buildHere != null) {
            S.builderController.build(Chassis.FLYING, buildHere);
            count++;
          }
        }
      }

      X.yield();
    }
  }
}
