package team050.blocks.brain;

import java.util.Arrays;

import team050.core.D;
import team050.core.S;
import battlecode.common.MapLocation;

/** Sensor-like information for brain-related stuffs. */
public class BrainState {
  /** True if this robot is in the range of a functioning brain. */
  public static final boolean inBrainRange() {
    return S.round - lastMessageRound <=
           BrainArmoryBlock.HEARTBEAT_INTERVAL + 2;
  }
  
  /** Location of the closest brain. */
  public static final MapLocation closestBrain() {
    return _lastBrainLocation;
  }
  
  /** Locations for mines in the last free mine message. */
  public static final MapLocation[] freeMines() {
    if (_freeMineLocations == null) {
      final int mineCount = lastMineMessage[2];      
      int offset = 2 * mineCount + 1;  // 2 + 1 + 2 * (mineCount - 1)
      _freeMineLocations = new MapLocation[mineCount];
      for (int i = mineCount - 1; i >= 0; i--) {
        _freeMineLocations[i] = new MapLocation(lastMineMessage[offset],
            lastMineMessage[offset + 1]);
        offset -= 2;
      }
    }
    return _freeMineLocations;
  }
  
  
  /** Called when the brain sends a mine update message. */
  public static final void _onMineInfoMessage(int[] message) {
    lastMineMessageRound = lastMessageRound = S.round;
    lastMineMessage = message;
    _freeMineLocations = null;
    _updateBrainLocation(message);
    
    // debug_printMines();
  }
  /** Prints the most recent free mines. */
  public static final void debug_printMines() {
    D.debug_pv("brain mines " + Arrays.deepToString(freeMines()));
  }

  /**
   * Updates the closest brain location.
   * 
   * We always put the brain location first in messages.
   */
  public static final void _updateBrainLocation(int[] message) {
    final MapLocation newLocation = new MapLocation(message[0], message[1]);
    if (S.round - _lastBrainLocationRound <= BRAIN_LOCATION_DECAY) {
      if (newLocation.distanceSquaredTo(S.location) >=
          _lastBrainLocation.distanceSquaredTo(S.location)) {
        return;
      }
    }
    _lastBrainLocation = newLocation;
    _lastBrainLocationRound = S.round;
    // D.debug_setIndicator(1, "closest brain at " + _lastBrainLocation + " heard " + _lastBrainLocationRound);
  }
  
  /** Called when the brain sends an enemy info message. */
  public static final void _onEnemyInfoMessage(int[] message) {
    lastEnemyMessageRound = lastMessageRound = S.round;
    lastEnemyMessage = message;
    
    // debug_printEnemy();
  }
  /** Prints the most recent enemy info message. */
  public static final void debug_printEnemy() {
    D.debug_pv("brain enemy " + Arrays.toString(lastEnemyMessage));
  }
  
  /** Called when the brain sends a heartbeat message. */
  public static final void _onHeartbeatMessage(int[] message) {
    lastMessageRound = S.round;
    _updateBrainLocation(message);
  }
  
  /** Memoized mine location information. */
  public static MapLocation[] _freeMineLocations;
  
  /** Where's the brain that we last heard of. */
  public static MapLocation _lastBrainLocation;
  /** Last time we received a brain location. */
  public static int _lastBrainLocationRound;
  /** How long until we forget a brain location. */
  public static int BRAIN_LOCATION_DECAY =
      BrainArmoryBlock.HEARTBEAT_INTERVAL * 2;
  
  /** The most recent free mines message. */
  public static int[] lastMineMessage;
  /** The time when we got the most recent free mines message. */
  public static int lastMineMessageRound;

  /** The most recent enemy information message. */
  public static int[] lastEnemyMessage;
  /** The time when we got the most recent enemy information message. */
  public static int lastEnemyMessageRound;
  
  /** Last time we heard from a brain. */
  public static int lastMessageRound;

  /** Location of the unit summary in the enemy message. */
  public static final int ENEMY_INFO_OFFSET = 0;
}
