package team050.blocks;

import team050.blocks.brain.BrainState;
import team050.core.D;
import team050.core.S;
import team050.core.U;
import team050.core.xconst.XComponentType;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class EnemyUpdatingBlock {
  
  public static final int JUMP_UNIT_FLEE_ROUNDS = 10;
  public static final int LAND_UNIT_FLEE_ROUNDS = 30;
  /**
   * Threshold for deciding the enemy is close and we need to flee.
   */
  public static final int ENEMY_DISTANCE_THRESHOLD = 64;
  public static final int BRAIN_DISTANCE_TRESHOLD = 25;

  /**
   * 
   * @return True is here is a nearby enemy with a weapon.
   */
  public static final boolean hasNearbyEnemyWithWeapon() {
    final MapLocation brainLocation = BrainState.closestBrain();
    if (S.round > _fleeUntilRound || (brainLocation != null && 
        S.location.distanceSquaredTo(brainLocation) < BRAIN_DISTANCE_TRESHOLD))
      _updateNearbyEnemy();
    return _hasNearbyEnemyWithWeapon || S.dHp < 0;
  }
  
  public static final Direction enemyDirection() {
    final MapLocation brainLocation = BrainState.closestBrain();
    if (S.round > _fleeUntilRound || (brainLocation != null && 
        S.location.distanceSquaredTo(brainLocation) < BRAIN_DISTANCE_TRESHOLD))
      _updateNearbyEnemy();
    return _enemyDirection;
  }
  
  public static final void _updateNearbyEnemy() {
    _fleeUntilRound = S.round; 
    
    _hasNearbyEnemyWithWeapon = false;  
    _hasNearbyEnemy = false;
    RobotInfo[] ris = S.nearbyRobotInfos();
    if (S.sensorTypeInt == XComponentType.SATELLITE_INT) {
      robotinfos: for (int i = ris.length - 1; i >= 0; i--) {
        final RobotInfo ri = ris[i];
        if (ri != null && ri.robot.getTeam() == S.enemyTeam && ri.on) {
          if (S.location.distanceSquaredTo(ri.location) > 
              ENEMY_DISTANCE_THRESHOLD) { continue; }
          final ComponentType[] components = ri.components;
          D.debug_assert(components != null, 
              "Satellite should be able to sense components");
          for (int j = components.length - 1; j >= 0; j--) {
            if (U.isWeapon(components[j])) {
              _hasNearbyEnemyWithWeapon = true;
              _fleeUntilRound += JUMP_UNIT_FLEE_ROUNDS;
              _enemyDirection = S.location.directionTo(ri.location); 
              break robotinfos;
            }
          }
        }
      }
    } else {
      for (int i = ris.length - 1; i >= 0; i--) {
        final RobotInfo ri = ris[i];
        if (ri != null && ri.robot.getTeam() == S.enemyTeam && ri.on) {
          if (S.location.distanceSquaredTo(ri.location) > 
              ENEMY_DISTANCE_THRESHOLD) { continue; }
          _hasNearbyEnemy = true;
          _enemyDirection = S.location.directionTo(ri.location);
          break;
        }
      }
      if (S.dHp < 0) { 
        _fleeUntilRound += LAND_UNIT_FLEE_ROUNDS;
        _hasNearbyEnemyWithWeapon = true; 
        _enemyDirection = _enemyDirection == null ? 
            S.direction : _enemyDirection;
      }
    }
    
  }
  
  public static boolean _hasNearbyEnemyWithWeapon;
  public static boolean _hasNearbyEnemy;
  public static Direction _enemyDirection;
  public static int _fleeUntilRound = 0;
}
