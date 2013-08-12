package sprint3.chaos;

import sprint3.core.D;
import sprint3.core.S;
import sprint3.core.X;
import sprint3.core.xconst.XDirection;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

public class SyncBuildBlock {

  public static final MapLocation waitAndTryBuild(Chassis chassis,
      ComponentType... cts) throws GameActionException {
    return waitAndTryBuild(chassis, cts, 0);
  }

  public static final MapLocation waitAndTryBuild(Chassis chassis,
      ComponentType[] cts, int startDirection) throws GameActionException {
    MapLocation loc = waitAndTryBuild(chassis, startDirection);
    if (loc == null)
      return null;
    for (ComponentType ct : cts) {
      while (S.flux < ct.cost)
        X.yield();
      tryEquip(ct, loc, chassis.level);
    }
    return loc;
  }

  public static final MapLocation waitAndTryBuild(Chassis chassis,
      MapLocation there, ComponentType... cts) throws GameActionException {
    MapLocation loc = waitAndTryBuild(chassis, there);
    if (loc == null)
      return null;
    for (ComponentType ct : cts) {
      while (S.flux < ct.cost)
        X.yield();
      tryEquip(ct, loc, chassis.level);
    }
    return loc;
  }

  public static final MapLocation waitAndTryBuild(Chassis chassis)
      throws GameActionException {
    while (S.flux < chassis.cost || S.dFlux < chassis.upkeep) {
      X.yield();
    }
    return tryBuild(chassis, 0);
  }

  public static final MapLocation waitAndTryBuild(Chassis chassis,
      int startDirection) throws GameActionException {
    while (S.flux < chassis.cost || S.dFlux < chassis.upkeep) {
      X.yield();
    }
    return tryBuild(chassis, startDirection);
  }

  public static final MapLocation waitAndTryBuild(Chassis chassis,
      MapLocation loc) throws GameActionException {
    while (S.flux < chassis.cost || S.dFlux < chassis.upkeep)
      X.yield();
    return tryBuild(chassis, loc);
  }

  public static final void waitAndBuild(Chassis chassis, MapLocation loc)
      throws GameActionException {
    while (S.flux < chassis.cost || S.dFlux < chassis.upkeep) {
      X.yield();
    }
    S.builderController.build(chassis, loc);
    X.yield();
  }

  public static final void waitAndBuild(ComponentType ct, MapLocation loc,
      RobotLevel level) throws GameActionException {
    while (S.flux < ct.cost) {
      X.yield();
    }
    S.builderController.build(ct, loc, level);
    X.yield();
  }

  public static final void waitAndTryEquip(ComponentType ct, RobotLevel level)
      throws GameActionException {
    while (S.flux < ct.cost || !tryEquip(ct, level)) {
      X.yield();
    }
  }

  public static final void waitAndTryBuild(ComponentType ct, MapLocation there,
      RobotLevel level) throws GameActionException {
    while (S.flux < ct.cost || !tryEquip(ct, there, level)) {
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
      MapLocation there = S.location.add(XDirection.intToDirection[(startDirection + i) % 8]);
      if (tryBuild(chassis, there) != null)
        return there;
    }
    return null;
  }

  public static final MapLocation tryBuild(Chassis chassis, MapLocation there)
      throws GameActionException {
    if (canBuild(there, chassis.level)) {
      S.builderController.build(chassis, there);
      X.yield();
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
  public static final boolean tryEquip(ComponentType ct, RobotLevel level)
      throws GameActionException {
    for (int i = 0; i < 8; i++) {
      MapLocation there = S.location.add(XDirection.intToDirection[i]);
      if (tryEquip(ct, there, level)) {
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
  public static final boolean tryEquip(ComponentType ct, MapLocation there,
      RobotLevel level) throws GameActionException {
    GameObject r = S.sensorController.senseObjectAtLocation(there, level);
    if (r != null && r instanceof Robot) {
      RobotInfo ri = S.sensorController.senseRobotInfo((Robot) r);
      int currentWeight = 0;
      for (ComponentType c : ri.components)
        currentWeight += c.weight;
      if (currentWeight + ct.weight <= ri.chassis.weight) {
        S.builderController.build(ct, there, level);
        X.yield();
        return true;
      }
    }
    return false;
  }

  public static final boolean canBuild(MapLocation there, RobotLevel level)
      throws GameActionException {
    if (!S.sensorController.withinRange(there)) {
      while (S.movementController.isActive())
        X.yield();
      X.setDirection(S.location.directionTo(there));
    }
    TerrainTile tt = S.rc.senseTerrainTile(there);
    if (tt == null || !tt.isTraversableAtHeight(level))
      return false;
    try {
      GameObject go = S.sensorController.senseObjectAtLocation(there, level);
      if (go != null && go instanceof Robot)
        return false;
    } catch (GameActionException gae) {
      D.debug_logException(gae);
    }
    return true;  
  }

  public static final boolean offMap(MapLocation there) {
    TerrainTile tt = S.rc.senseTerrainTile(there);
    if (tt == null || tt.equals(TerrainTile.OFF_MAP))
      return true;
    return false;
  }
  
  public static final void buildSync(ComponentType compnent, MapLocation there, RobotLevel level) 
      throws GameActionException {
    S.builderController.build(compnent, there, level);
    X.yield();
  }
}
