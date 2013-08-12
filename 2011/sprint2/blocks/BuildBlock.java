package sprint2.blocks;

import sprint2.core.S;
import sprint2.core.X;
import sprint2.util.MapUtil;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

public class BuildBlock {
  public static double fluxMargin = 2;

  public static final MapLocation waitAndTryBuild(Chassis chassis,
      ComponentType ct) throws GameActionException {
    return waitAndTryBuild(chassis, ct, 0);
  }

  public static final MapLocation waitAndTryBuild(Chassis chassis,
      ComponentType ct, int startDirection) throws GameActionException {
    MapLocation loc = waitAndTryBuild(chassis, startDirection);
    if (loc == null)
      return null;
    while (S.flux < ct.cost + fluxMargin || S.builderController.isActive())
      X.yield();
    tryBuild(ct, loc, chassis.level);
    return loc;
  }

  public static final MapLocation waitAndTryBuild(Chassis chassis,
      ComponentType ct, MapLocation there) throws GameActionException {
    MapLocation loc = waitAndTryBuild(chassis, there);
    if (loc == null)
      return null;
    while (S.flux < ct.cost + fluxMargin || S.builderController.isActive())
      X.yield();
    tryBuild(ct, loc, chassis.level);
    return loc;
  }

  public static final MapLocation waitAndTryBuild(Chassis chassis)
      throws GameActionException {
    while (S.flux < chassis.cost + fluxMargin || S.dFlux < chassis.upkeep || S.builderController.isActive()) {
      X.yield();
    }
    return tryBuild(chassis, 0);
  }

  public static final MapLocation waitAndTryBuild(Chassis chassis,
      int startDirection) throws GameActionException {
    while (S.flux < chassis.cost + fluxMargin || S.dFlux < chassis.upkeep || S.builderController.isActive()) {
      X.yield();
    }
    return tryBuild(chassis, startDirection);
  }

  public static final MapLocation waitAndTryBuild(Chassis chassis,
      MapLocation loc) throws GameActionException {
    while (S.flux < chassis.cost + fluxMargin || S.dFlux < chassis.upkeep || 
        S.builderController.isActive()) {
      X.yield();
    }
    return tryBuild(chassis, loc);
  }

  public static final void waitAndBuild(Chassis chassis, MapLocation loc)
      throws GameActionException {
    while (S.flux < chassis.cost + fluxMargin || S.dFlux < chassis.upkeep || S.builderController.isActive()) {
      X.yield();
    }
    X.build(chassis, loc);
  }

  public static final void waitAndBuild(ComponentType ct, MapLocation loc,
      RobotLevel level) throws GameActionException {
    while (S.flux < ct.cost + fluxMargin || S.builderController.isActive()) {
      X.yield();
    }
    X.build(ct, loc, level);
  }

  public static final void waitAndTryBuild(ComponentType ct, RobotLevel level)
      throws GameActionException {
    while (S.flux < ct.cost + fluxMargin || S.builderController.isActive()
        || !tryBuild(ct, level)) {
      X.yield();
    }
  }

  public static final void waitAndTryBuild(ComponentType ct, MapLocation there,
      RobotLevel level) throws GameActionException {
    while (S.flux < ct.cost + fluxMargin || S.builderController.isActive()
        || !tryBuild(ct, there, level)) {
      X.yield();
    }
  }

  /**
   * Requires have enough flux and the controller is not active.
   * 
   * @param chassis
   * @return
   * @throws GameActionException
   */
  public static final MapLocation tryBuild(Chassis chassis, int startDirection)
      throws GameActionException {
    RobotLevel level = chassis.level;
    if (!S.level.equals(level)) {
      if (tryBuild(chassis, S.location) != null)
        return S.location;
    }
    for (int i = 0; i < 8; i++) {
      MapLocation there = S.location.add(MapUtil.intToDirection[(startDirection + i) % 8]);
      if (tryBuild(chassis, there) != null)
        return there;
    }
    return null;
  }

  public static final MapLocation tryBuild(Chassis chassis, MapLocation there)
      throws GameActionException {
    if (canBuild(there, chassis.level)) {
      X.build(chassis, there);
      return there;
    }
    return null;
  }

  /**
   * Requires have enough flux and the controller is not active. Try to build a
   * component on chassis in the square immediately adjacent to myself.
   * 
   * @param ct ComponentType to build.
   * @param chassis Chassis to build the component on.
   * @return true if the build action is queued; false otherwise
   * @throws GameActionException
   */
  public static final boolean tryBuild(ComponentType ct, RobotLevel level)
      throws GameActionException {
    for (int i = 0; i < 8; i++) {
      MapLocation there = S.location.add(MapUtil.intToDirection[i]);
      if (tryBuild(ct, there, level)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Requires have enough flux and the controller is not active.
   * 
   * @param ct
   * @param there
   * @return
   * @throws GameActionException
   */
  public static final boolean tryBuild(ComponentType ct, MapLocation there,
      RobotLevel level) throws GameActionException {
    GameObject r = S.sensorController.senseObjectAtLocation(there, level);
    if (r != null && r instanceof Robot) {
      RobotInfo ri = S.sensorController.senseRobotInfo((Robot) r);
      int currentWeight = 0;
      for (ComponentType c : ri.components)
        currentWeight += c.weight;
      if (currentWeight + ct.weight <= ri.chassis.weight) {
        X.build(ct, there, level);
        return true;
      }
    }
    return false;
  }

  public static final boolean canBuild(MapLocation there, RobotLevel level)
      throws GameActionException {
    TerrainTile tt = S.rc.senseTerrainTile(there);
    if (tt == null || !tt.isTraversableAtHeight(level))
      return false;
    return S.sensorController.senseObjectAtLocation(there, level) == null;
  }
  
  public static final boolean offMap(MapLocation there) {
    TerrainTile tt = S.rc.senseTerrainTile(there);
    if (tt == null || tt.equals(TerrainTile.OFF_MAP))
      return true;
    return false;
  }
}
