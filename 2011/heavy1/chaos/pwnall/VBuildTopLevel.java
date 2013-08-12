package heavy1.chaos.pwnall;

import heavy1.core.Role;
import heavy1.core.S;
import heavy1.core.X;

public class VBuildTopLevel {
  public static final void sync() {
    /*
    int[] boRoles = {1, 2};
    Chassis[] boChassis = {Chassis.LIGHT, Chassis.LIGHT};
    ComponentType[][] boComponents = {
        {ComponentType.SHIELD, ComponentType.ANTENNA, ComponentType.PROCESSOR,
         ComponentType.RADAR},
        {ComponentType.CONSTRUCTOR, ComponentType.BLASTER}};
    */
    Role[] boRoles = {Role.COLONIST};
    VikingBuildBlock.setBuildOrder(boRoles, null);        
    while (true) {
      // if (!VBuildBlock.busy) {
      // }
      if (S.flux >= 160) {
        VikingBuildBlock.async();
      }
      X.yield();
    }
  }
}
