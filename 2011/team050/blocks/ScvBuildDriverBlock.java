package team050.blocks;

import team050.blocks.building.BuildBlock;
import team050.blocks.pathfinding.BugPathfindingBlock;
import team050.core.D;
import team050.core.S;
import team050.core.U;
import team050.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

/** All Constructor-carrying units must use this block in their while loop. 
 */
public class ScvBuildDriverBlock {
  public static final boolean async() {
    final boolean returnValue = BuildBlock.async();
    if (!BuildBlock.busy) { return false; }
    debug_buildingState();
    MapLocation buildLocation = BuildBlock.currentBuildLocation();
    if (buildLocation != null) {
      if (!S.location.isAdjacentTo(buildLocation)) {
        // Move to the buildLocation
        try {
          if (S.motorReady) {
            if (S.location.equals(buildLocation)) {
                U.moveOffSpotAsync();
                return true;
            } else {
              final Direction d = BugPathfindingBlock.nextDirection(buildLocation, 2);
              if (d.ordinal() < 8) {
                X.moveTowardsAsync(d);
                return true;
              }
//              PathfindingBlock.setParameters(buildLocation, 250);
//              return PathfindingBlock.async();
            }
          }
        } catch (GameActionException gae) {
          D.debug_logException(gae);
        }
      } else {
        // If it is adjacent to the building location, see if we need to abort.
        AbortBuildingBlock.async();
      }
    }
    return returnValue;
  }
  
  public static final void debug_buildingState() {
    D.debug_setIndicator(0, "buiding at location = " + BuildBlock.currentBuildLocation() + 
        " building = " + BuildBlock._components[BuildBlock._currentRobot][0] +
        " cost = " + BuildBlock._stepCost[BuildBlock._currentRobot]);
  }
}
