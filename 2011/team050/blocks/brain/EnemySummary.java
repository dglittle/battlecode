package team050.blocks.brain;

import team050.core.U;
import battlecode.common.ComponentType;
import battlecode.common.RobotInfo;

/** Summary for enemy information. */
public class EnemySummary {
  /** How fast will a robot reach us. */
  public static final int speed(RobotInfo enemy) {
    int speed = 0;
    
    // Initial speed.
    switch (enemy.chassis) {
    case LIGHT:
      speed = 33;
      break;
    case FLYING:
      speed = 25;
      break;
    case MEDIUM:
      speed = 14;
      break;
    case HEAVY:
      speed = 10;
      break;
    case BUILDING:
      speed = 1;
      break;
    case DUMMY:
      // TODO(pwnall): handle dummies
      speed = 0;
      break;
    }

    return speed + 16 * U.countEnum(enemy.components, ComponentType.JUMP);
  }

  /**
   * Computes a summary for a robot.
   * @param enemy the enemy information to be summarized
   * @param target the array that will receive the summary
   * @param offset the first element of the target that will receive the summary
   */
  public static final void summarize(RobotInfo enemy, int[] target,
      int offset) {
    // Weapon counts.
    target[offset + SMG_OFFSET] = 0;
    target[offset + BLASTER_OFFSET] = 0;
    target[offset + RAILGUN_OFFSET] = 0;
    target[offset + HAMMER_OFFSET] = 0;
    target[offset + BEAM_OFFSET] = 0;
    target[offset + SHIELD_OFFSET] = 0;
    target[offset + HARDENED_OFFSET] = 0;
    target[offset + REGEN_OFFSET] = 0;
    target[offset + PLASMA_OFFSET] = 0;
    target[offset + IRON_OFFSET] = 0;
    target[offset + JUMP_OFFSET] = 0;
    final ComponentType[] components = enemy.components;
    for (int i = components.length - 1; i >= 0; i--) {
      switch (components[i]) {
      case SMG:
        target[offset + SMG_OFFSET]++;
        break;
      case BLASTER:
        target[offset + BLASTER_OFFSET]++;
        break;
      case RAILGUN:
        target[offset + RAILGUN_OFFSET]++;
        break;
      case HAMMER:
        target[offset + HAMMER_OFFSET]++;
        break;
      case BEAM:
        target[offset + BEAM_OFFSET]++;
        break;
      case SHIELD:
        target[offset + SHIELD_OFFSET]++;
        break;
      case HARDENED:
        target[offset + HARDENED_OFFSET]++;
        break;
      case REGEN:
        target[offset + REGEN_OFFSET]++;
        break;
      case PLASMA:
        target[offset + PLASMA_OFFSET]++;
        break;        
      case IRON:
        target[offset + IRON_OFFSET]++;
        break;
      case JUMP:
        target[offset + JUMP_OFFSET]++;
        break;
      }
    }
    target[offset + CHASSIS_OFFSET] = enemy.chassis.ordinal();
    target[offset + X_OFFSET] = enemy.location.x;
    target[offset + Y_OFFSET] = enemy.location.y;
    target[offset + ID_OFFSET] = enemy.robot.getID();
  }
  
  /** How many array elements are taken up by a summary. */
  public static final int SIZE = 15;
  
  /** Indexes for the summary elements. */  
  public static final int SMG_OFFSET = 0;
  public static final int BLASTER_OFFSET = 1;
  public static final int RAILGUN_OFFSET = 2;
  public static final int HAMMER_OFFSET = 3;
  public static final int BEAM_OFFSET = 4;
  public static final int SHIELD_OFFSET = 5;
  public static final int HARDENED_OFFSET = 6;
  public static final int REGEN_OFFSET = 7;
  public static final int PLASMA_OFFSET = 8;
  public static final int IRON_OFFSET = 9;
  public static final int JUMP_OFFSET = 10;
  public static final int CHASSIS_OFFSET = 11;
  public static final int X_OFFSET = 12;
  public static final int Y_OFFSET = 13;
  public static final int ID_OFFSET = 14;
}
