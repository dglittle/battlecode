package team050.blocks.brain;

import team050.core.B;
import team050.core.CommandType;
import team050.core.D;
import team050.core.S;
import battlecode.common.GameActionException;

/**
 * The brain functions that run in the Armory. Executed every round.
 * 
 * The armory has a Network and uses it to relay the messages sent by the
 * Factory.
 */
public class BrainArmoryBlock {
  /** How often do we broadcast mine info. */
  public static final int HEARTBEAT_INTERVAL = 15;
  
  /** Performs the brain functions that run in the Armory. */
  public static final boolean async() {
    if (B.bc == null || B.bc.isActive()) { return false; }
    
    if (_radioMineInfoAsync()) { return true; }
    if (_radioEnemyInfoAsync()) { return true; }
    return _radioHeartbeatAsync();
  }
  
  /** One-time initialization right before the block is run. */
  public static final void setupAsync() {
    // Hardwire our location into the heartbeat message.
    final int[] message = CommandType.BRAIN_HEARTBEAT.ints;
    message[0] = S.locationX;
    message[1] = S.locationY;    
  }
  
  /** Relays mine information received from the Factory. */
  public static final boolean _radioMineInfoAsync() {
    if (_lastMineMessageRound >= BrainState.lastMineMessageRound) {
      return false;
    }
    
    // Wire our location into the mine info message.
    final int[] message = CommandType.BRAIN_MINES.ints;
    message[0] = S.locationX;
    message[1] = S.locationY;

    try {
      B.send(BrainState.lastMineMessage);
      _lastMineMessageRound = _lastMessageRound = S.round;
      return true;
    } catch (GameActionException e) {
      D.debug_logException(e);
      return false;
    }
  }
  
  /** Relays enemy information received from the Factory. */
  public static final boolean _radioEnemyInfoAsync() {
    if (_lastEnemyMessageRound >= BrainState.lastEnemyMessageRound) {
      return false;
    }
    
    try {
      B.send(BrainState.lastEnemyMessage);
      _lastEnemyMessageRound = _lastMessageRound = S.round;
      return true;
    } catch (GameActionException e) {
      D.debug_logException(e);
      return false;
    }
  }
  
  /** Radios a heartbeat, we haven't sent any message in a while. */
  public static final boolean _radioHeartbeatAsync() {
    if (_lastMineMessageRound + HEARTBEAT_INTERVAL > S.round) {
      return false;
    }

    try {
      B.send(CommandType.BRAIN_HEARTBEAT.ints);
      _lastMessageRound = S.round;
      return true;
    } catch (GameActionException e) {
      D.debug_logException(e);
      return false;
    }
  }
  
  /** Last time we relayed mine info. */
  public static int _lastMineMessageRound;
  /** Last time we relayed enemy info. */
  public static int _lastEnemyMessageRound;
  /** Last time we sent out any message. */
  public static int _lastMessageRound;
}
