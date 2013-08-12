package team050.blocks.brain;

import javax.rmi.CORBA.Tie;

import team050.blocks.TierBlock;
import team050.blocks.building.BasePlanningBlock;
import team050.blocks.building.BuildBlock;
import team050.blocks.building.BuildDriverBlock;
import team050.chaos.pwnall.LeaderElectionBlock;
import team050.core.Role;
import team050.core.S;
import team050.core.xconst.XComponentType;

/** The logic running in a block that's not ready to upgrade to brains yet. */
public class EmbryoRecyclerBlock {
  /** One-time state setup. */
  public static final void setupAsync() {
    // Tier 1 build order.
    BuildDriverBlock.setBuildOrder(new Role[]{Role.COLONIST}, new int[]{1}, 
        new int[] {50}, TierBlock.tieredUnitThreshold());    

    LeaderElectionBlock.start();
  }
  
  /** Runs each round while a recycler isn't a brain. */
  public static final boolean async() {
    // Check if we're a leader, turn off if we're not.
    LeaderElectionBlock.async();
    if (LeaderElectionBlock.busy) { return true; }
    if (!LeaderElectionBlock.isLeader) {
      S.rc.turnOff();
      return true;
    }
    
    if (BrainState.inBrainRange()) {
      // Finish what we started building, but no more new stuff.
      if (BuildBlock.startedBuilding()) {
        return BuildBlock.async();
      }
      
      BuildBlock.cancelBuildOrder();
      // TODO(pwnall): consider going to bed, a brain is watching over us
      return false;
    }
    
    if (TierBlock.currentTier() == 1) {
      return BuildDriverBlock.async();
    }
    
    if (TierBlock.currentTier() > 1) {
      if (!_tryingToEvolve && !BuildBlock.startedBuilding()) {
        if (BasePlanningBlock.canBuildBrain()) {
          Role buildRole = Role.HEAVY_COLONIST;
          final int dieRoll = S.randomInt(6);
          // int dieRoll = 1;
          switch (TierBlock.currentTier()) {
            case 1:
            case 2:
              if (dieRoll < 3) {
                buildRole = Role.HEAVY_SOLDIER;
              }
              break;
            case 3:
              if (dieRoll < 3) {
                buildRole = Role.HEAVY_SOLDIER;
              } else if (dieRoll < 4) {
                buildRole = Role.HEAVY_SOLDIER_HAMMERS;
              }
              break;
            case 4:
              if (dieRoll < 4) {
                buildRole = Role.HEAVY_SOLDIER;
              } else if (dieRoll < 5) {
                buildRole = Role.HEAVY_SOLDIER_HAMMERS;
              }
              break;
          }
          
          BuildBlock.setBuildOrder(new Role[] { buildRole }, null,
              _evolvingTag);
          _tryingToEvolve = true;          
        } else {
          if (!BuildBlock.busy &&
              S.sensorTypeInt == XComponentType.BUILDING_SENSOR_INT) {
            BuildBlock._buildSensorAsync();
          }
          
          // Now we know sure we won't be a brain.
        }
      }
    }
    return BuildBlock.async();
  }
  
  
  /** Last round when DFlux */
  public static int _lastBuildRound;
  
  /** True if the embryo is ready to move up to a brain. */
  public static final boolean _hasEvolved() {
    return BasePlanningBlock._armoryLocation != null &&
           BasePlanningBlock._factoryLocation != null;
  }
  
  /** True if the build order that would evolve us was issued. */
  public static boolean _tryingToEvolve = false;
  /** Tag for the build order that would evolve the embryo into a brain. */
  public static final Object _evolvingTag = "embryoEvolving";
}
