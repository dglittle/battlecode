package team050.chaos;

import team050.core.D;
import team050.core.S;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

public class BuildUtil {

  /**
   * 1. Checks whether there is enough flux to cover the cost of the chassis plus the current upkeep
   * of the team, and whether dflux is greater than chassis's upkeep.
   * 2. Checks whether the location at direction dir is empty.
   * @param chassis
   * @param dir
   * @return
   */
  public static final MapLocation tryBuild(Chassis chassis, Direction dir) {
    if (S.flux < chassis.cost + S.totalUpkeep || S.dFlux < chassis.upkeep)
      return null;
    if (S.builderController.canBuild(chassis, S.location.add(dir))) {
      try {
        MapLocation there = S.location.add(dir);
        S.builderController.build(chassis, there);
        return there;
      } catch (GameActionException gae) {
        return null;
      }
    }
    return null;
  }

  /**
   * @param dir
   * @param level
   * @return 2 if can build in direction dir, 1 if needs to turn to check again, 0 if can't build in 
   *         direction dir.
   * @throws GameActionException
   */
  public static final boolean canBuild(Direction dir, RobotLevel level) {
    /* If the level of the robot to build is the same as this builder robot, then only need to check
     * whether this robot can move in that direction. This probably is the cheapest check.
     * This takes care of ground consructor building armory and factory, recycler building ground
     * constructor.
     */
    if (S.level == level)
      return S.movementController.canMove(dir);

    if (S.sensorController == null)
      return true;
    
    MapLocation there = S.location.add(dir);
    /* If the sensor is not omni-directional, we need to turn to the direction we want to sense if 
     * it is not in the range. This condition only applies to flying constructor building recycler.
     */
    if (!S.sensorIsOmnidirectional && !S.sensorController.withinRange(there))
      return true;

    TerrainTile tt = S.rc.senseTerrainTile(there);
    D.debug_assert(tt != null, "Terrain tile should not be null");
    if (!tt.isTraversableAtHeight(level))
      return false;
    try {
      return S.sensorController.senseObjectAtLocation(there, level) == null;
    } catch (GameActionException gae) {
      D.debug_logException(gae);
    }
    return true;
  }
  
  public static final boolean canEquip(MapLocation there, RobotLevel level) {
    if (S.sensorController == null)
      return true;
    if (!S.sensorIsOmnidirectional && !S.sensorController.withinRange(there))
      return true;
    try {
      return S.sensorController.senseObjectAtLocation(there, level) != null;
    } catch (GameActionException e) {
      D.debug_logException(e);
    }
    return true;
  }
  
  public static final boolean offMap(MapLocation there) {
    TerrainTile tt = S.rc.senseTerrainTile(there);
    if (tt == null || tt.equals(TerrainTile.OFF_MAP))
      return true;
    return false;
  }
  
  public static int componentsCost(ComponentType[][] components) {
    int cost = 0;
    for (int i = components.length - 1; i >= 0; i--) 
      for (int j = components[i].length - 1; j >= 0; j--)
        cost += components[i][j].cost;

    return cost;
  }
  
  public static int componentsCost(ComponentType[] components) {
    int cost = 0;
    for (int i = components.length - 1; i >= 0; i--) 
      cost += components[i].cost;
    return cost;
  }
  
  public static int chassisCost(Chassis[] chassis) {
    int cost = 0;
    for (int i = chassis.length - 1; i >= 0; i--)
      cost += chassis[i].cost;
    return cost;
  }
  
  public static double chassisUpkeep(Chassis[] chassis) {
    double upkeep = 0;
    for (int i = chassis.length - 1; i >= 0; i--) 
      upkeep += chassis[i].upkeep;
    return upkeep;
  }
  
  /**
   * 
   * @param fluxCost
   * @param upkeep
   * @return true if the current flux is greater than fluxCost needed to build something plus the
   *         total upkeep of the team and the dflux is greater than the upkeep of the things to 
   *         build.
   */
  public static final boolean hasEnoughFlux(double fluxCost, double upkeep) {
    return (S.flux > fluxCost + S.totalUpkeep && S.dFlux >= upkeep);
  }
  
  /**
   * 1. Checks whether there is enough flux to build the component.
   * 2. Checks whether there is a chassis there at the specified level.
   * @param component
   * @param there
   * @param level
   * @return
   */
  public static final boolean tryEquip(ComponentType component, MapLocation there, RobotLevel level) 
  {
    if (S.flux < component.cost)
      return false;
    if (canEquip(there, level)) {
      try {
        S.builderController.build(component, there, level);
        return true;
      } catch (GameActionException gae) {
        //D.debug_logException(gae.toString());
        return false;
      }
    }
    return false;
  }
}
