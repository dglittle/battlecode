package team050.chaos.pwnall;

import team050.blocks.building.BuildBlock;
import team050.core.Role;
import team050.core.S;
import team050.core.X;

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
    BuildBlock.setBuildOrder(boRoles, null, "VBuildTopLevel");        
    while (true) {
      // if (!VBuildBlock.busy) {
      // }
      if (S.flux >= 160) {
        BuildBlock.async();
      }
      X.yield();
    }
  }
}
