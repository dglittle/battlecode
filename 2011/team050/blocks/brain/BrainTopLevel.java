package team050.blocks.brain;

import team050.blocks.HealBlock;
import team050.blocks.TierBlock;
import team050.blocks.building.BuildBlock;
import team050.blocks.building.BuildDriverBlock;
import team050.chaos.pwnall.TurnOffLonelyMineBlock;
import team050.core.D;
import team050.core.M;
import team050.core.Role;
import team050.core.S;
import team050.core.U;
import team050.core.X;
import team050.core.xconst.XComponentType;
import team050.core.xconst.XDirection;
import battlecode.common.GameActionException;

/** Wiring for the blocks that make up the brain. */
public class BrainTopLevel {
  /** Runs the brain code. */
  public static final void sync() {
    while (S.builderController == null) {
      X.yield();
    }
    
    switch (S.builderTypeInt) {
    case XComponentType.RECYCLER_INT:
      if (S.birthRound < 100) {  // Treshold for the first 3 recyclers.
        initialRecyclerSync();
      }
      embryoSync();
      recyclerSync();
      break;
    case XComponentType.ARMORY_INT:
      armorySync();
      break;
    case XComponentType.FACTORY_INT:
      factorySync();
      break;
    default:
      D.debug_logException("Brain code shouldn't be running on " + S.builderType);
    }
  }
  
  /** Scouts mines and enemies. */
  public static final void factorySync() {
    M.disableMapUpdates();
    BrainFactoryBlock.setupAsync();
    while (true) {
      BuildBlock.async();
      BrainFactoryBlock.async();
      X.yield();
    }
  }

  /** Matches attackers with defenders and advertises mines. */
  public static final void armorySync() {
    BrainArmoryBlock.setupAsync();
    while (true) {
      BuildBlock.async();
      BrainArmoryBlock.async();
      X.yield();
    }    
  }
  
  /** Builds counters to enemy units. */
  public static final void recyclerSync() {
    BuildDriverBlock.setBuildOrder(
        new Role[] {Role.FLYING_COLONIST, Role.HEAVY_COLONIST,
                    Role.HEAVY_SOLDIER, Role.HEAVY_SOLDIER_HAMMERS},
        new int[] {1, 1, 5, 2},
        new int[] {600, 1000, 250, 350}, TierBlock.tieredUnitThreshold());

    while (true) {
      HealBlock.async();
      BrainRecyclerBlock.async();
      BuildDriverBlock.async();
      X.yield();
    }
  }
  
  /** This code is run by a recycler until it decides to become a brain. */
  public static final void embryoSync() {
    if (TurnOffLonelyMineBlock.async()) {
      X.yield();
    }
    
    EmbryoRecyclerBlock.setupAsync();
    while (true) {
      if (EmbryoRecyclerBlock._hasEvolved()) {
        // D.debug_pv("Exiting embryo state");
        return;
      }
      
      EmbryoRecyclerBlock.async();      
      X.yield();
    }
  }
  
  /** This code is run by the initial three recyclers. */
  public static final void initialRecyclerSync() {
    while (true) {
      if (S.motorReady) {
        try {
          final int emptySpaces = U.adjacentBuildingLots();
          X.setDirectionChecked(XDirection.intToDirection[emptySpaces / 2]);
          break;
        } catch (GameActionException e) {
          D.debug_logException(e);
          // No good reason not be able to turn.
        }
      }
      X.yield();
    }
    S.rc.turnOff();
    X.yield();
  }
}
