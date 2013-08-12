package team050.blocks.building;

import team050.core.D;
import team050.core.Role;
import team050.core.S;

/**
 * Issues complex build orders to the {@link VikingBuildBlock}.
 *
 * {@link #setBuildOrder(Role[], int[])} does probabilistic (bloodstream)
 * builds.
 */
public class BuildDriverBlock {
  public static final void setBuildOrder(Role[] roles, int[] ratios,
                                         int[] rates, double fluxTreshold) {
    // delegates to setBuildOrder below after wrapping roles in arrays
    Role[][] orders = new Role[roles.length][1];
    for (int i = roles.length - 1; i >= 0; i--) 
      orders[i][0] = roles[i];
    setBuildOrder(orders, ratios, rates, fluxTreshold);
  }

  public static final void setBuildOrder(Role[][] orders, int[] ratios,
                                         int[] rates, double fluxTreshold) {
    _orders = orders;
    _fluxTreshold = fluxTreshold;
    _rates = rates;
    _roundTresholds = new int[rates.length];
    _thresholds = new int[ratios.length];
    _thresholds[ratios.length - 1] = ratios[ratios.length - 1];
    for (int i = ratios.length - 2; i >= 0; i--) {
      _thresholds[i] = _thresholds[i + 1] + ratios[i];
    }
  }

  public static final boolean async() {
    if (!BuildBlock.busy && _orders != null && S.flux > _fluxTreshold) {
      final int order = _randomOrderIndex();
      if (S.round < _roundTresholds[order]) { return false; }
      BuildBlock.setBuildOrder(_orders[order], null,
                               _buildDriverTag);
      _roundTresholds[order] = S.round + _rates[order];
    }
    return BuildBlock.async();  
  }
  
  public static final int _randomOrderIndex() {
    int treshold = S.randomInt(_thresholds[0]);
    for (int i = _thresholds.length - 1; i >= 0; i--) {
      if (treshold < _thresholds[i]) {
        return i;
      }
    }
    D.debug_logException("_randomOrderIndex broken");
    return _thresholds.length - 1;
  }
    
  /** Inverted ratio sums. */
  public static int[] _thresholds;
  
  /** Next round when we're allowed to take an order. */
  public static int[] _roundTresholds;
  
  /** Rate (unit / rounds) limits for each order. */
  public static int[] _rates;
  
  /** Build orders. */
  public static Role[][] _orders;
  
  /** No build orders are issued when flux falls below this treshold. */
  public static double _fluxTreshold;
  
  /** Tag for the orders coming from the build driver. */
  public static final Object _buildDriverTag = "BuildDriver";
}
