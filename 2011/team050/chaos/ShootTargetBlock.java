package team050.chaos;

import team050.core.B;
import team050.core.D;
import team050.core.S;
import team050.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.common.WeaponController;

@Deprecated
public class ShootTargetBlock implements Strategy {
  
  //--- Member Variables----------------------------------------------
  
  public static MapLocation target;
  public static RobotLevel targetLevel = RobotLevel.ON_GROUND;
  
  //--- State Machine ------------------------------------------------
  
  /**
   * Strategy state machine states.
   */
  public static enum StrategyState {
    IDLE, NAVIGATE_TO_TARGET, FIRE
  }; public static StrategyState strategyState = StrategyState.IDLE;

  /**
   * Check the conditions and perform any required state changes.
   * 
   * @throws GameActionException
   */
  public final void stateCheckConditions() throws GameActionException {
    switch (strategyState) {
      case IDLE:
        if (target != null && S.maxWeaponRange != 0) {
          stateTransition(StrategyState.NAVIGATE_TO_TARGET);
        }
        break;
      case NAVIGATE_TO_TARGET:
        break;
      case FIRE:
        break;
    }
    D.debug_setIndicator(0, "State: " + strategyState.toString());
    if (target != null)
      D.debug_setIndicator(1, "Target: " + target + " " + S.location.distanceSquaredTo(target) +
          " away.");
  }

  /**
   * Perform a state transition to state.
   * 
   * @param state
   */
  public final void stateTransition(StrategyState state) {
    strategyState = state;
    switch (state) {
      case IDLE:
      case NAVIGATE_TO_TARGET:
      case FIRE:
        break;
    }
  }

  public final void step() throws GameActionException {
    stateCheckConditions();
    switch (strategyState) {
      case IDLE:
        B.checkMessages();
//        B.addOnMessageHandler(new Callback() {
//          @Override
//          public void onMessage(Message m) {
//            target = new MapLocation(m.ints[1], m.ints[2]);
//          }
//        });
        X.yield();
        break;
      case NAVIGATE_TO_TARGET:
        if (S.location.distanceSquaredTo(target) > 36) {
          //Log.out("We're too far from the target: "
          //    + S.location.distanceSquaredTo(target) + ".");
//          NavigationBlock.goal = target;
          //Log.out("Moving towards the target at " + target.toString() + " (should be the same as " + NavigationBlock.goal.toString() + ").");

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// !!!!!!!! THE LINE BELOW SHOULD BE UNCOMMENTED IF YOU WANT TO USE THIS!

//          NavigationBlock.try_moveInDirection(NavigationBlock.nextDirection());
        } else {
          Direction dir = S.location.directionTo(target);
          if (!S.direction.equals(dir) && S.motorReady) {
            S.movementController.setDirection(dir);
            X.yield();
          }
          //Log.out("Short-cutting to FIRE");
          stateTransition(StrategyState.FIRE);
          //Log.out("Dead or alive, you're coming with me! " + target + " " + targetLevel);
          step();
        }
        break;
      case FIRE:
        for (WeaponController weapon : S.weaponControllers) {
          if (!weapon.isActive()) {
            weapon.attackSquare(target, targetLevel);
            break;
          }
        }
        X.yield();
        break;
    }
  }

  @Override
  public void init() throws GameActionException { }

  // --- Helper Methods -----------------------------------------------
  
}
