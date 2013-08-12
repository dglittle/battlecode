package sprint4.blocks;

import battlecode.common.GameActionException;

public class FastConstructorStrategyBlock{
  
  //--- Member Variables ---------------------------------------------
  
  
  //--- State Machine ------------------------------------------------
  
  /**
   * Strategy state machine states.
   */
  public static enum StrategyState {
    IDLE
  }; public static StrategyState strategyState = StrategyState.IDLE;

  /**
   * Check the conditions and perform any required state changes.
   * 
   * @throws GameActionException
   */
  public final void stateCheckConditions() throws GameActionException {
    switch (strategyState) {
      case IDLE:
        /* if (condition) stateTransition(state); */
        break;
    }
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
        break;
    }
  }

  public final void step() throws GameActionException {
    stateCheckConditions();
    switch (strategyState) {
      case IDLE:
        /* Step in the IDLE state. */
        break;
    }
  }

  // --- Helper Methods -----------------------------------------------
  
}
