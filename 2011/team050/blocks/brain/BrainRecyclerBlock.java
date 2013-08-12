package team050.blocks.brain;

import team050.blocks.building.BasePlanningBlock;
import team050.blocks.building.BuildBlock;
import team050.core.S;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.MapLocation;

/**
 * The brain functions that run in the Recycler.
 * 
 * This should be executed every round.
 */
public class BrainRecyclerBlock {
  /** Performs the brain functions that run in the Recycler. */
  public static final boolean async() {
    if (_equipPeersAsync()) { return true; }
    return _buildCounterAsync();
  }
  
  /** One-time initialization, right before running {@link #async()}. */
  public static final void asyncSetup() {
    
  }
  
  /** Builds a counter-unit to the most recently spotted enemy. */
  public static final boolean _buildCounterAsync() {
    // Counters override anything else we might be building.
    if (BuildBlock.busy) {
      if (BuildBlock._tag == _counterUnitTag) { return false; }
      if (BuildBlock.startedBuilding()) { return false; }
    }
    if (_lastCounterBoRound >= BrainState.lastEnemyMessageRound) {
      return false;
    }
    
    final int enemyID = BrainState.lastEnemyMessage[1 + EnemySummary.ID_OFFSET];
    if (_seenEnemy(enemyID)) { return false; }
    
    Chassis[] boChassis = new Chassis[] { Chassis.HEAVY };
    ComponentType[][] boComponents = new ComponentType[][] {
        EnemyCounter.compute(BrainState.lastEnemyMessage,
        BrainState.ENEMY_INFO_OFFSET) };

    BuildBlock.setBuildOrder(boChassis, boComponents, null, _counterUnitTag);
    // D.debug_pv("Issued counter build order");
    _lastCounterBoRound = S.round;
    return true;
  }
  
  /** Checks if we've seen the enemy recently, and remebers the ID. */
  public static final boolean _seenEnemy(int id) {
    int oldestRound = Integer.MAX_VALUE;
    int oldestIndex = 0;
    for (int i = ID_QUEUE_SIZE - 1; i >= 0; i--) {
      if (idQueue[i] == id) {
        if (S.round - idQueueRound[i] < ID_QUEUE_DECAY) {
          // TODO(pwnall): should we update the round or not?
          return true;
        } else {
          idQueueRound[i] = S.round;
          return false;
        }
      }
      final int round = idQueueRound[i];
      if (round < oldestRound) {
        oldestRound = round;
        oldestIndex = i;
      }
    }
    idQueue[oldestIndex] = id;
    idQueueRound[oldestIndex] = S.round;
    return false;
  }
  
  /** Equips the brain parts with what they need to function. */
  public static final boolean _equipPeersAsync() {
    if (BasePlanningBlock._factoryLocation != null
        && BasePlanningBlock._armoryLocation != null) {
      if (_equipBoIssued) { return false; }
    } else {
      _equipBoIssued = false;
      return false;
    }
    
    if (BuildBlock.busy) { return false; }
    
    ComponentType[] armoryGear;
    if (BasePlanningBlock._armoryLocation.isAdjacentTo(S.location)) {
      armoryGear = new ComponentType[] {
          ComponentType.NETWORK, ComponentType.PROCESSOR };
    } else {
      armoryGear = new ComponentType[] {
          ComponentType.NETWORK };      
    }
    
    BuildBlock.setBuildOrder(
        new Chassis[] { Chassis.BUILDING, Chassis.BUILDING, Chassis.BUILDING},
        new ComponentType[][] {
            armoryGear,
            {ComponentType.TELESCOPE, ComponentType.ANTENNA},
            {S.sensorIsOmnidirectional ? ComponentType.MEDIC :
                                         ComponentType.SHIELD },
        },
        new MapLocation[] {
            BasePlanningBlock._armoryLocation,
            BasePlanningBlock._factoryLocation,
            S.location
        }, null, true, _equipBrainPeersTag);
    // D.debug_pv("issued brain equip order");
    _equipBoIssued = true;
    return true;
  }
  
  /** Last time we issued a counter build order. */
  public static int _lastCounterBoRound;

  /** How many enemy IDs do we track. */
  public static int ID_QUEUE_DECAY = 500;
  public static int ID_QUEUE_SIZE = 8;
  public static int[] idQueue = new int[ID_QUEUE_SIZE];
  public static int[] idQueueRound = new int[ID_QUEUE_SIZE];
  
  /** True if the build order for equipping peers has been issued. */
  public static boolean _equipBoIssued;
  /** The tag for the order that equips the recyler's brain peers. */
  public static final Object _equipBrainPeersTag = "equipBrainPeers";
  /** The tag for the order that builds a counter to an enemy. */
  public static final Object _counterUnitTag = "enemyCounter";
}
