package team050.blocks;

import team050.blocks.building.BuildBlock;
import team050.core.D;
import team050.core.S;
import team050.core.U;
import team050.core.X;
import battlecode.common.Chassis;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class AbortBuildingBlock {
  public static boolean goalTaken;
  
  /**
   * Requires the current location is adjacent to the build location.
   * Aborts the build order if the build location is taken by another building.
   * @return true if build order is aborted.
   */
  public static final boolean async() {
    // If the building process is started, do not abort
    if (BuildBlock._currentComponent != -1) { return false; }
    
    MapLocation buildLocation = BuildBlock.currentBuildLocation();
    goalTaken = false;
    try {
      if (U.buildLocationTaken(buildLocation)) {
        BuildBlock.cancelBuildOrder();
        goalTaken = true;
        return true;
      }
    } catch (GameActionException e) {
      D.debug_logException(e);
    }
    
    final Direction towardGoal = S.location.directionTo(buildLocation);
    if (S.direction == towardGoal) {
      if (S.builderController.canBuild(Chassis.BUILDING, buildLocation))
        return false;
      try {
        RobotInfo ri = S.senseRobotInfo(buildLocation, _chassis.level);
        if (ri != null && ri.chassis == _chassis) {
          BuildBlock.cancelBuildOrder();
          return true;
        }
      } catch (GameActionException e) {
        D.debug_logException(e);
      }
    } else {
      if (S.motorReady) {
        try {
          X.setDirection(towardGoal);
          return false;
        } catch (GameActionException e) {
          D.debug_logException(e);
        }
      }
    }
    return false;
  }
  
  public static Chassis _chassis = Chassis.BUILDING;
}
