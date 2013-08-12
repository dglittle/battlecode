package team050.blocks;

import team050.blocks.brain.BrainFactoryBlock;
import team050.blocks.brain.BrainState;
import team050.blocks.brain.EnemySummary;
import team050.core.D;
import team050.core.S;
import battlecode.common.MapLocation;

/** Figures out where the action is. */
public final class HotspotSensor {
  /**
   * Nearest enemy location.
   * @return null if there is no hotspot, non-null means go there
   */
  public static final MapLocation hotspot() {
    final int lastHotspotDelay = S.round - _latestHotspotRound;
    if (lastHotspotDelay >= HOTSPOT_DECAY) {
      return null;
    } else {
      return _latestHotspot;
    }
  }
  
  /** If we haven't heard of a hotspot in this long, it's not interesting. */
  public static final int HOTSPOT_DECAY =
      BrainFactoryBlock.ENEMY_INFO_INTERVAL * 3;
  
  /** Called when we receive a message with enemy information. */
  public static final void _onEnemyInfoMessage(int[] message) {
    final MapLocation newHotspot = new MapLocation(
        message[BrainState.ENEMY_INFO_OFFSET + EnemySummary.X_OFFSET],
        message[BrainState.ENEMY_INFO_OFFSET + EnemySummary.Y_OFFSET]);
    final int lastHotspotDelay = S.round - _latestHotspotRound;
    if (lastHotspotDelay <= BrainFactoryBlock.ENEMY_INFO_INTERVAL + 1) {
      // If two brains broadcast at the same time
      if (newHotspot.distanceSquaredTo(S.location) >=
          _latestHotspot.distanceSquaredTo(S.location)) {        
        return;
      }
    }
    _latestHotspot = newHotspot;
    _latestHotspotRound = S.round;
    debug_setIndicator();
  }
  
  /** Sets an indicator string to indicate the hotspot. */
  public static final void debug_setIndicator() {
    D.debug_setIndicator(2, "Hotspot at " + _latestHotspot + " received at " + _latestHotspotRound);
  }
  
  /** Last hotspot we received from a brain message. */
  public static MapLocation _latestHotspot;
  /** When {@link #_latestHotspot} was updated last. */
  public static int _latestHotspotRound;
}
