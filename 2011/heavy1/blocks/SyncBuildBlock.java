package heavy1.blocks;

import heavy1.core.Role;
import heavy1.core.S;
import heavy1.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class SyncBuildBlock {

  /**
   * assumes you have enough flux, and that you can build there
   * 
   * will do the sequence of builds
   * 
   * throws an exception if anything goes wrong
   * 
   * DOES NOT YIELD AT THE END!
   * 
   * @param there
   * @param chassis
   * @param cts
   * @return
   * @throws GameActionException
   */
  public static final void build(MapLocation there, Chassis chassis,
      ComponentType... cts) throws GameActionException {
    try {
      S.builderController.build(chassis, there);
      for (ComponentType ct : cts) {
        X.yield();
        S.builderController.build(ct, there, chassis.level);
      }
    } catch (GameActionException e) {
      //D.debug_logException(e);
    }
  }
  
  public static final void build(MapLocation there, Role role) throws GameActionException {
    build(there, role.chassis, role.components);
  }

  /**
   * Builds a colonist in an empty adjacent location.
   * 
   * @throws GameActionException
   */
  public static final void buildColonist() throws GameActionException {
    while (S.builderController.isActive()
        || !S.allowedToBuildNonMine(Role.COLONIST.chassis, Role.COLONIST.components)) {
      X.yield();
    }
    MapLocation buildLocation = S.location.add(RandomMovementBlock.randomAvailableDirection());
    build(buildLocation, Role.COLONIST.chassis, Role.COLONIST.components);
  }

}
