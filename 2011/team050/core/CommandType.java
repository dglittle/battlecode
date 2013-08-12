/**
 * Thanks to Stephen G., ssg729.
 */
package team050.core;

import team050.blocks.brain.BrainFactoryBlock;
import team050.blocks.brain.EnemySummary;

public enum CommandType {
  // Commands
  ATTACK_TARGET(3),
  
  BUILD(27),
  BRAIN_HEARTBEAT(2),
  BRAIN_MINES(2 + 1 + BrainFactoryBlock.MINE_INFO_LENGTH * 2),
  BRAIN_ENEMY(1 + EnemySummary.SIZE),
  
  YING_BUILD(6),
  YING_EQUIP(3),

  FLEET_INIT(3),
  FLEET_GO(3),
  FLEET_BUILD_MINE(3),
  FLEET_ATTACK(4),
  FLEET_MAIN_MESSAGE(9),
  FLEET_ID(1),  
  ARMORY_LEEDER(2),
  
  MANIPLE_JOIN_SQUAD(7),
  MANIPLE_DESTINATION(5),
  MANIPLE_NEARBY_ENEMIES(9),
  
  NEXUS_CONSTRUCTOR(4),
  NEXUS_BUILD(7);

  public final int[] ints;

  CommandType(int commandLength) {
    ints = new int[commandLength + 3];
    ints[ints.length - 3] = ordinal();
  }
}
