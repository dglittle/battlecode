package team050.chaos.pwnall;

import battlecode.common.Chassis;
import team050.core.D;
import team050.core.Role;
import team050.core.S;
import team050.core.U;
import team050.core.X;
import team050.core.xconst.XChassis;

/** Unit role computation. */
public class RoleDetectionBlock {
  /** Yields until receives a role. */
  public static final void waitForRoleSync() {
    while (true) {
      try {
        if (RoleDetectionBlock.setRole()) { return; }
        X.yield();
      } catch (Exception e) {
        D.debug_logException(e);        
      }
    }
  }
  
  /** Sets the role in S. */
  public static final boolean setRole() {
    if (S.role != null) { return true; }
    
    // Initial colonists.
    if (S.chassis == Chassis.LIGHT && S.birthRound <= 5) {
      S.role = Role.COLONIST;
      return true;
    }
    
    // Static roles.
    for (int i = _roles.length - 1; i >= 0; i--) {
      final Role role = _roles[i];
      if (S.chassis != role.chassis) { continue; }
      if (!U.hasComponents(role.components)) { continue; }
      S.role = role;
      return true;
    }
    
    // Custom-built robots.
    if (S.leftoverWeight == 0) {
      // Custom-built robot.
      switch (S.chassisInt) {
      case XChassis.HEAVY_INT:
        S.role = Role.HEAVY_SOLDIER;
        return true;
      case XChassis.LIGHT_INT:
        S.role = Role.SOLDIER;
        return true;
      }
    }
    
    // TODO(pwnall): assign role if we've been waiting for too long
    
    // Wait for more components.
    return false;
  }
  
  /** All roles. */
  public static final Role[] _roles = Role.values();
}
