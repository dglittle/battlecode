package team050.chaos.pwnall;

import team050.core.D;
import team050.core.S;
import team050.core.U;
import team050.core.X;
import team050.core.xconst.XDirection;
import battlecode.common.Chassis;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

/**
 * Elects a leader in a pack of buildings.
 * 
 * This is not an action block, but it does have its own state.
 */
public class LeaderElectionBlock {
  /** True if the election is in the process. */
  public static boolean busy;
  /** True if I am the leader. */
  public static boolean isLeader;
  
  /**
   * Starts the election process.
   */
  public static final void start() {
    busy = true;
    _dancing = false;
  }
  
  /**
   * Progress towards a election results.
   * @return true if there is an action taken.
   */
  public static final boolean async() {
    if (!busy) return false;
    final int emptySpaces = U.adjacentBuildingLots();
      
    if (!_dancing) {
      if (!S.motorReady)
        return false;
      try {
        X.setDirectionChecked(XDirection.intToDirection[emptySpaces / 2]);
        return _dancing = true;
      } catch (GameActionException e) {
        D.debug_logException(e);
      }
    }
    isLeader = true;
    MapLocation bestLocation = S.location;
    int bestSpaces = emptySpaces / 2;
    for (int d = XDirection.ADJACENT_DIRECTIONS - 1; d >= 0; d--) {
      final MapLocation there = S.location.add(XDirection.intToDirection[d]);
      try {
        RobotInfo ri = S.senseRobotInfo(there, RobotLevel.ON_GROUND);
        if (ri != null && ri.robot.getTeam() == S.team && ri.chassis == Chassis.BUILDING &&
            ri.hitpoints >= Chassis.BUILDING.maxHp) {
          int spaces = ri.direction.ordinal();
          if (spaces > bestSpaces) {
            bestLocation = there;
            bestSpaces = spaces;
          }
          // Even I have the best location but some one is already on, I'm not the leader.
          if (ri.on)
            isLeader = false;
        }
      } catch (GameActionException e) {
        D.debug_logException(e);
      }
    }
    if (!S.location.equals(bestLocation)) {
      try {
        S.rc.turnOn(bestLocation, RobotLevel.ON_GROUND);
      } catch (GameActionException e) {
        D.debug_logException(e);
      }
      isLeader = false;
    }
    
    if (emptySpaces == 0)
      isLeader = false;
    busy = false;
    return true;
  }
  
  /** True when we set the direction. */
  public static boolean _dancing;
  
//  /** Performs the leader computation. */
//  public static boolean _isLeaderWithoutCaching() throws GameActionException {
//    SensorController sensor = (S.buildingSensorController != null) ?
//        S.buildingSensorController : S.sensorController;
//    
//    Robot[] robots = sensor.senseNearbyGameObjects(Robot.class);
//    for (int i = robots.length - 1; i >= 0; i--) {
//      final Robot robot = robots[i];      
//      RobotInfo info = sensor.senseRobotInfo(robot);
//      if (info.chassis != S.chassis) { continue; }
//      if (robot.getTeam() != S.team) { continue; }
//      
//      if (info.location.x < S.locationX) { return false; }
//      if (info.location.y < S.locationY) { return false; }      
//    }
//    return true;
//  }
}
