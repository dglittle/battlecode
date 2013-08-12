package team050.blocks;

import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameConstants;

public class IntelBlock {

  // --- Defense ----------------------------------------------------------
  // - SHIELD, HARDENED, REGEN, PLASMA, IRON, PLATING
  ComponentType[] defenseComponents = {
      ComponentType.SHIELD, ComponentType.HARDENED, ComponentType.REGEN,
      ComponentType.PLASMA, ComponentType.IRON, ComponentType.PLATING};

  public static final int[] defenseWeights = {1, 5, 4, 3, 3, 1};
  public static final int[] defenseCosts = {10, 23, 10, 16, 20, 8};

  // --- Offense ----------------------------------------------------------
  // - SMG, BLASTER, RAILGUN, HAMMER, BEAM
  ComponentType[] offenseComponents = {
      ComponentType.SMG, ComponentType.BLASTER, ComponentType.RAILGUN,
      ComponentType.HAMMER, ComponentType.BEAM};

  public static final int[] offenseWeights = {1, 2, 5, 2, 4};
  public static final double[] offensePowers = {0.6, 3, 6.5, 1.5};
  public static final int[] offenseCosts = {7, 18, 25, 16, 17};

  // --- Methods ----------------------------------------------------------

  /**
   * A component count array looks as follows. For each ComponentType listed
   * below, replace it how many of that component the enemy has:
   * 
   *     0         1       2         3      4      5
   * [ SHIELD, HARDENED, REGEN,   PLASMA, IRON, PLATING,
   *   SMG,    BLASTER,  RAILGUN, HAMMER, BEAM ]
   * 
   * This method returns a similar array populated with the values needed to
   * defeat the components that were passed to it.
   * 
   * @param defenseComponentCounts
   *          an int[6] array of the component counts to
   *          beat
   * @param offenseComponentCounts
   *          an int[5] array of the component counts to
   *          beat
   * @return an int[11] array of the component counts that beat the given
   *         component counts
   */
  public static final int[] getDefeatingComponentCounts(
      int[] defenseComponentCounts, int[] offenseComponentCounts) {
    return new int[11];
  }

  /**
   * Tells whether the attack force indicated by the components is too large
   * for
   * Hardened, which absorbs any attack whose power is greater than 2.
   * 
   * @param chassis
   *          the {@link Chassis} to attack
   * @param defenseComponentCounts
   * @param offenseComponentCounts
   * @return the number of rounds that are needed to defeat these defense
   *         components with these attack components
   */
  @Deprecated // FIXME
  public static final int numRoundsToDestroy(Chassis chassis,
      int[] defenseComponentCounts, int[] offenseComponentCounts) {
    double attackPower = 0;
    // Compute the total attack power, assuming all weapons in range.
    // Doesn't take BEAM into account (done later on).
    for (int i = 0; i < offenseComponentCounts.length - 1; i++) {
      attackPower += offenseComponentCounts[i] * offensePowers[i];
    }
    double maxHp = chassis.maxHp + defenseComponentCounts[5]
        * GameConstants.PLATING_HP_BONUS;
    double regenAmount = defenseComponentCounts[2] * GameConstants.REGEN_AMOUNT;
    if (attackPower + offenseComponentCounts[4] * 6 < regenAmount) return Integer.MAX_VALUE;
    int rounds = 0;
    double hp = 0;
    while (true) {
      if (hp < 0) return rounds;
      hp += regenAmount;
    }
//    return rounds;
  }

  /**
   * Checks whether the requested components can fit into a chassis with the
   * left over weight.
   * 
   * @param chassis
   *          the {@link Chassis} to fit the components into
   * @param defenseComponentCounts
   *          an int[6] array of the component counts
   * @param offenseComponentCounts
   *          an int[5] array of the component counts
   * @param leftOverWeight
   *          the weight to keep in reserve (usually 0)
   * @return whether these components fit into the chassis with the left over
   *         weight
   */
  public static final boolean canFit(Chassis chassis,
      int[] defenseComponentCounts, int[] offenseComponentCounts,
      int leftOverWeight) {
    int weightAvailable = chassis.weight - leftOverWeight;
    int currentComponentWeight = 0;
    // Defense then offense
    for (int i = 0; i < 6; i++) {
      currentComponentWeight = defenseComponentCounts[i] * defenseWeights[i];
      weightAvailable -= currentComponentWeight;
      if (weightAvailable < 0)
        return false;
    }
    for (int i = 0; i < 5; i++) {
      currentComponentWeight = offenseComponentCounts[i] * offenseWeights[i];
      weightAvailable -= currentComponentWeight;
      if (weightAvailable < 0)
        return false;
    }
    return true;
  }
}
