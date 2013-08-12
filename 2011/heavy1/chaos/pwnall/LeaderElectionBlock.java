package heavy1.chaos.pwnall;

import heavy1.core.D;
import heavy1.core.S;
import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.SensorController;

/**
 * Elects a leader in a pack of buildings.
 * 
 * This is not an action block, but it does have its own state.
 */
public class LeaderElectionBlock {
  /** True if this robot gets to be a leader. */
  public static boolean isLeader() {
    if (_cachedAnswer) { return _isLeader; }
    _cachedAnswer = true;
    try {
      return _isLeader = _isLeaderWithoutCaching();
    } catch(GameActionException e) {
      D.debug_logException(e);
      return false;
    }
  }
  
  /** Performs the leader computation. */
  public static boolean _isLeaderWithoutCaching() throws GameActionException {
    SensorController sensor = (S.buildingSensorController != null) ?
        S.buildingSensorController : S.sensorController;
    
    Robot[] robots = sensor.senseNearbyGameObjects(Robot.class);
    for (int i = robots.length - 1; i >= 0; i--) {
      final Robot robot = robots[i];      
      RobotInfo info = sensor.senseRobotInfo(robot);
      if (info.chassis != S.chassis) { continue; }
      if (robot.getTeam() != S.team) { continue; }
      
      if (info.location.x < S.locationX) { return false; }
      if (info.location.y < S.locationY) { return false; }      
    }
    return true;
  }
  
  /** True when we already know the answer. */
  public static boolean _cachedAnswer;
  /** The cached answer. */
  public static boolean _isLeader;
}
