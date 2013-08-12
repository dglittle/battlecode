package sprint2.toplevel;

import sprint2.core.S;
import sprint2.core.X;
import sprint2.util.MapUtil;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class IndependentDefenderTopLevel {
  /** How many rounds to stay awake while no enemy is in sight. */
  public static final int IDLE_ROUNDS_UNTIL_SLEEP = 12;
  
  /** Top-level run function. */
  public static final void run() {
    _waitUntilWakeup();
    
    _lastEnemySightingRound = S.round;    
    while (true) {
      try {
        RobotInfo target = _weakestEnemy();
        if (target != null) {
          _lastEnemySightingRound = S.round;
          
          X.attack(target);

          if (_walkAwayFromMine()) {
            continue;
          }
          
          if (!S.movementController.isActive()) {
            Direction toTarget = S.location.directionTo(target.location);         
            if (S.direction != toTarget && (toTarget.dx > 0 || toTarget.dy > 0)) {
              X.setDirection(toTarget);
              continue;
            }            
          }
          
          X.yield();
          continue;
        }
        
        if (_walkAwayFromMine()) {
          continue;
        }
        
        if (S.round - _lastEnemySightingRound >= IDLE_ROUNDS_UNTIL_SLEEP) {            
          // TODO(pwnall): X.turnOff
          S.rc.turnOff();
          X.yield();
          _waitUntilWakeup();
          continue;
        }
        
        if (!S.sensorIsOmnidirectional && !S.movementController.isActive()) {
          // TODO(pwnall): generic method that turns to the next sensor direction.
          X.setDirection(S.direction.opposite());
        }
        X.yield();
      }
      catch (GameActionException e) {
        e.printStackTrace();
        X.yield();
      }
    }
  }
  
  /** If sitting on a mine, tries to go away from it. */
  public static final boolean _walkAwayFromMine() {
    try {
      GameObject maybeMine = S.sensorController.senseObjectAtLocation(S.location, RobotLevel.MINE);
      if (maybeMine == null) {
        return false;
      }
      
      if (S.movementController.isActive()) {
        return false;
      }
      
      // TODO(pwnall): walk away from parent recycler instead of walking randomly.
      if (S.canMove(S.direction)) {
        X.moveForward();
        return true;
      }
      else if (S.canMove(S.direction.opposite())) {
        X.moveBackward();
        return true;
      }
      else {
        Direction direction = S.direction.rotateRight();
        for (int i = 1; i < MapUtil.ADJACENT_DIRECTIONS; i++) {
          if (S.canMove(direction)) {
            X.setDirection(direction);
            return true;
          }
          direction = direction.rotateRight();
        }
        return false;
      }
    }
    catch (GameActionException e) {
      e.printStackTrace(); 
      return false;
    }
  }
  
  /** Last round when the enemy was seen. */
  public static int _lastEnemySightingRound;
  
  /** Selects the weakest enemy for butt-raping. */
  public static final RobotInfo _weakestEnemy() {
    RobotInfo enemy = null;
    double minimumHp = Double.MAX_VALUE;
    for(RobotInfo info : S.nearbyRobotInfo()) {
      if (info == null) { continue; }
      if (info.robot.getTeam() != S.enemyTeam) {
        continue;
      }
      if (info.location.distanceSquaredTo(S.location) > S.maxWeaponRange) {
        continue;
      }
      
      if (info.hitpoints < minimumHp) {
        minimumHp = info.hitpoints;
        enemy = info;
      }
    }
    return enemy;
  }
  
  /** Returns when the robot is awake. */
  public static final void _waitUntilWakeup() {
    while (S.sensorController == null || S.movementController.isActive()) {
      X.yield();
    }    
  }
}
