package team050.blocks;

import team050.core.Role;
import team050.core.S;

/** Decides the current tier. */
public class TierBlock {
  
  /** Flux threshold for building units other than recycler. This ensures building recycler always 
   * has the priority. 
   */
  public static final int UNITS_THRESHOLD_BASE = 5;
  
  public static final double RECYCLER_THRESHOLD = Role.RECYCLER.totalCost;
  
  public static final double TIERED_BUFFER_BASE = 2;
  
  public static final int currentTier() {
    if (S.round > _previousUpdateRound) { _update(); }
    return _currentTier;
  }
  
  /**
   * @return true if tier is switched.
   */
  public static final boolean tierSwitched() {
    if (S.round > _previousUpdateRound) { _update(); }
    return _tierSwitched;
  }
  
  /** Colonist should check this threshold before building a recycler. */
  public static final double tieredRecyclerThreshold() {
    if (S.round > _previousUpdateRound) { _update(); }
    if (HotspotSensor.hotspot() == null) {
      return RECYCLER_THRESHOLD + _currentFluxThreshold;
    } else {
      return RECYCLER_THRESHOLD + _currentFluxThreshold +
             2 * UNITS_THRESHOLD_BASE;
    }
  }
  
  /** This threshold ensures the threshold for building units is always greater
   * than that of building a recycler. This gives the priority to building a 
   * recycler.
   * @return
   */
  public static final double tieredUnitThreshold() {
    if (S.round > _previousUpdateRound) { _update(); }
    return RECYCLER_THRESHOLD + _currentFluxThreshold + UNITS_THRESHOLD_BASE;
  }
  
  /**
   * 
   * @return the margin that should be added to the recycler cost to prevent
   * units from turned off. This is a rough estimate of the current total 
   * upkeep.
   */
  public static final double tieredRecylerMargin() {
    if (S.round > _previousUpdateRound) { _update(); }
    if (HotspotSensor.hotspot() == null) {
      return _currentFluxThreshold;
    } else {
      return _currentFluxThreshold + 2 * UNITS_THRESHOLD_BASE;
    }
  }
  
  public static final double tieredDFluxMargin() {
    if (S.round > _previousUpdateRound) { _update(); }
    
    switch (_currentTier) {
      case 1:
        return 0.0;
      case 2:
        return 0.5;
      case 3:
        return 1.0;
    }
    return 1.0;
  }
  
  /**
   * 
   * @return the margin that should be added to the unit cost.
   */
  public static final double tieredUnitMargin() {
    if (S.round > _previousUpdateRound) { _update(); }
    return _currentFluxThreshold + UNITS_THRESHOLD_BASE;
  }
  
  /**
   * Updates the tier information.
   */
  public static final void _update() {
    _previousTier = _currentTier;
    _tierSwitched = false;
    _previousUpdateRound = S.round;
   
    switch (_previousTier) {
    case 0:
      _currentTier = 1;
      _currentFluxThreshold = 2;
      _tierSwitched = true;
      break;
    case 1:
      if (S.round >= 140) {
        _currentTier = 2;
        _currentFluxThreshold = 5;
        _tierSwitched = true;
      }
      break;
    case 2:
      if (S.round >= 400) {
        _currentTier = 3;
        _currentFluxThreshold = 13;
        _tierSwitched = true;
      }
      break;
    case 3:
      if (S.round >= 1000) {
        _currentTier = 4;
        _currentFluxThreshold = 20;
        _tierSwitched = true;
      }
      break;
    default:
      break;
    }    
  }
  
  public static int _previousTier = 0;
  /** Previous round that the tier information was updated. */
  public static int _previousUpdateRound = -1;
  public static double _currentFluxThreshold = 0;
  public static boolean _tierSwitched;
  public static int _currentTier;

}
