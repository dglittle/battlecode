package team050.core;

import team050.blocks.HotspotSensor;
import team050.blocks.brain.BrainState;
import team050.blocks.building.BuildBlock;
import team050.chaos.YingBuildBlock;
import battlecode.common.BroadcastController;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Team;

/**
 * Radio communication.
 * 
 * B stands for "broadcast".
 */
public class B {
  /** The controller we use to radio. It is null until the robot has a radio. */
  public static BroadcastController bc;

  /**
   * Wraps {@link BroadcastController#broadcast(Message)}.
   * 
   * This method adds MACing and reasonable replay protection. It should always
   * be used, because robots will drop any message without a direct .
   * 
   */
  public static final void send(int[] message) throws GameActionException {
    //$ +gen:source Broadcast.send __
    message[message.length - FOOTER_ROUND] = S.round;
    message[message.length - FOOTER_MAC] = _mac(message);
    _envelope.ints = message;
    //$ -gen:source

    bc.broadcast(_envelope);
  }

  /**
   * Sends a message like {@link #send(int[])} but catches the exception.
   * 
   * @return true if sending succeeded, false if an exception occurred
   */
  public static final boolean sendAsync(int[] message) {
    //$ +gen:target Broadcast.send __
    message[message.length - FOOTER_ROUND] = S.round;
    message[message.length - FOOTER_MAC] = _mac(message);
    _envelope.ints = message;
    //$ -gen:target
    
    try {
      bc.broadcast(_envelope);
      return true;
    } catch (GameActionException e) {
      D.debug_logException(e);
      return false;
    }
  }

  /** Cycles through incoming messages and updates state to reflect them. */
  public static final void checkMessages() {
    Message[] envelopes = S.rc.getAllMessages();    
    for (int i = envelopes.length - 1; i >= 0; i--) {
      final Message envelope = envelopes[i];
      final int[] message = envelope.ints;
      
      // Discard enemy messages.
      if (message == null) { continue; }
      final int length = message.length;
      if (length < FOOTER_COMMAND || length > _longestMessage) { continue; }
      if (S.round - message[length - FOOTER_ROUND] >= TTL) { continue; }
      if (message[length - FOOTER_MAC] != _mac(message)) { continue; }

      // Update state to reflect message.
      switch (_intToCommandType[message[length - FOOTER_COMMAND]]) {
        case BUILD:
          BuildBlock._onMessage(message);
          break;
        case BRAIN_ENEMY:
          BrainState._onEnemyInfoMessage(message);
          HotspotSensor._onEnemyInfoMessage(message);
          break;
        case BRAIN_HEARTBEAT:
          BrainState._onHeartbeatMessage(message);
          break;
        case BRAIN_MINES:
          BrainState._onMineInfoMessage(message);
          break;
        case YING_EQUIP:
          YingBuildBlock._chassisIndex = message[0];
          YingBuildBlock._chassisLocation = new MapLocation(message[1], message[2]);
          break;
      }
    }
  }

  /** Maximum number of rounds that a message is still valid for. */
  public static final int TTL = 2;
  
  /** Offset of the message type in the ints array. */
  public static final int FOOTER_COMMAND = 3;
  /** Offset of the transmission round number in the ints array. */
  public static final int FOOTER_ROUND = 2;
  /** Offset of the MAC in the ints array. */
  public static final int FOOTER_MAC = 1;

  /** First magic prime for hashing. Change before submission. */
  public static int _magic1 = 797003437;
  /** Second magic prime for hashing. Change before submission. */
  public static int _magic2 = 899809343;
  
  /** Called by {@link X#init(battlecode.common.RobotController)} to set up. */
  public static final void init() {
    // for sparing against ourselves, we need a different magic number for teamB
    if (S.team == Team.B) {
      int temp = _magic1;
      _magic1 = _magic2;
      _magic2 = temp;
    }
    
    for (int i = _intToCommandType.length - 1; i >= 0; i--) {
      final CommandType command = _intToCommandType[i];
      final int length = command.ints.length;
      
      command.ints[length - FOOTER_COMMAND] = i;
      if (length > _longestMessage) {
        _longestMessage = length;
      }
    }
  }

  /** Computes the MAC for a message. */
  public static final int _mac(int[] message) {
    int mac = _magic1;
    for (int i = message.length - 2; i >= 0; i--) {
      mac = (mac ^ message[i]) * _magic2;
    }
    return mac;
  }

  /** Converts an integer to a CommandType. */
  public static CommandType[] _intToCommandType = CommandType.values();
  
  /** The number of ints in the longest message that we use. */
  public static int _longestMessage = 0;

  /** The global envelope used to send all messages. */
  public static Message _envelope = new Message();
}
