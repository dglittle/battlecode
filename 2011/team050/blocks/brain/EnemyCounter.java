package team050.blocks.brain;

import java.util.ArrayList;
import java.util.Arrays;

import team050.core.D;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameConstants;

/** The algorithm for countering enemies. */
public final class EnemyCounter {

  public static ArrayList<ComponentType> ourComps;
  public static Chassis ourChassis;
  public static int ourWeight;

  public static final ComponentType[] helperCompArray = new ComponentType[0];

  public static boolean addOur(ComponentType comp, int amount) {
    if (ourWeight + comp.weight * amount <= ourChassis.weight) {
      for (int i = 0; i < amount; i++) {
        ourComps.add(comp);
        ourWeight += comp.weight;
      }
      return true;
    }
    return false;
  }

  /** Computes the best counter for a unit. */
  public static final ComponentType[] compute(int[] source, int offset) {
    final int smgs = source[offset + EnemySummary.SMG_OFFSET];
    final int blasters = source[offset + EnemySummary.BLASTER_OFFSET];
    final int railguns = source[offset + EnemySummary.RAILGUN_OFFSET];
    final int hammers = source[offset + EnemySummary.HAMMER_OFFSET];
    final int beams = source[offset + EnemySummary.BEAM_OFFSET];
    final int shields = source[offset + EnemySummary.SHIELD_OFFSET];
    final boolean hardened = source[offset + EnemySummary.HARDENED_OFFSET] > 0;
    final int regens = source[offset + EnemySummary.REGEN_OFFSET];
    final int plasmas = source[offset + EnemySummary.PLASMA_OFFSET];
    final boolean iron = source[offset + EnemySummary.IRON_OFFSET] > 0;
    final int jumps = source[offset + EnemySummary.JUMP_OFFSET];
    final Chassis chassis = Chassis.values()[source[offset
        + EnemySummary.CHASSIS_OFFSET]];

    // init
    ourChassis = Chassis.HEAVY;
    ourComps = new ArrayList<ComponentType>();
    ourWeight = 0;

    if (chassis == Chassis.LIGHT) {
      addOur(ComponentType.RADAR, 1);
      addOur(ComponentType.JUMP, 1);
      addOur(ComponentType.SHIELD, 1);
      addOur(ComponentType.PLASMA, 1);
      addOur(ComponentType.REGEN, 1);
      addOur(ComponentType.RAILGUN, 1);
      
      if (ourWeight != ourChassis.weight) {
        D.debug_assert(false, "we made an anti-body that doesn't weigh right: "
            + ourComps);
      }      
      return ourComps.toArray(helperCompArray);
    }
    
    addOur(ComponentType.RADAR, 1);
    addOur(ComponentType.JUMP, 1);

    final int weapons = smgs + blasters + railguns + hammers + beams;
    int neededPlasmas = weapons + hammers;

    // try wall 1
    if (addOur(ComponentType.SMG, plasmas)
        && addOur(ComponentType.PLASMA, neededPlasmas)
        && (addOur(ComponentType.RAILGUN, 1) || ((regens == 0) && addOur(
            ComponentType.SMG, 1)))) {

    } else {
      ourComps.clear();
      ourWeight = 0;

      double mostPowerful;
      if (railguns > 0) {
        mostPowerful = ComponentType.RAILGUN.attackPower;
      } else if (beams > 0) {
        mostPowerful = GameConstants.BEAM_RAMP[GameConstants.BEAM_RAMP.length - 1];
      } else if (blasters > 0) {
        mostPowerful = ComponentType.BLASTER.attackPower;
      } else if (hammers > 0) {
        mostPowerful = ComponentType.HAMMER.attackPower;
      } else if (smgs > 0) {
        mostPowerful = ComponentType.SMG.attackPower;
      } else {
        mostPowerful = 0.0;
      }
      
      // try wall 2
      if (addOur(ComponentType.RADAR, 1)
          && addOur(ComponentType.JUMP, 1)
          && addOur(ComponentType.SMG, plasmas)
          && addOur(ComponentType.SHIELD,
              (int) Math.ceil((mostPowerful - 0.15) / 0.6))
          && addOur(ComponentType.REGEN, 1) && addOur(ComponentType.SMG, 1)) {

      } else {
        ourComps.clear();
        ourWeight = 0;

        addOur(ComponentType.RADAR, 1);
        addOur(ComponentType.JUMP, 1);

        addOur(ComponentType.SMG, plasmas);

        if (smgs > 0) {
          addOur(ComponentType.SHIELD, 1);

          if (smgs >= 4) {
            addOur(ComponentType.REGEN, 1);
          }
        }

        while (addOur(ComponentType.RAILGUN, 1)) {
        }
      }
    }

    // fill up
    while (ourWeight < ourChassis.weight) {
      addOur(ComponentType.SMG, 1);
      // addOur(ComponentType.SHIELD, 1);
    }
    
    
    if (ourWeight != ourChassis.weight) {
      D.debug_assert(false, "we made an anti-body that doesn't weigh right: "
          + ourComps);
    }

    // D.debug_pv("We got " + Arrays.toString(source) + " and built " + ourComps);
    return ourComps.toArray(helperCompArray);
  }
}
