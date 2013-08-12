package heavy1.core;

import battlecode.common.BuildMappings;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;

// This file has some auto-generated parts. After modifying, run
// bcpm regen src/team050/core/Role.java

public enum Role {
  COLONIST(Chassis.LIGHT, new ComponentType[]{
      ComponentType.CONSTRUCTOR, ComponentType.SIGHT, ComponentType.PLATING}),

  TANK(Chassis.HEAVY, new ComponentType[]{
      ComponentType.SHIELD, ComponentType.RADAR, ComponentType.REGEN,
      ComponentType.REGEN, ComponentType.REGEN, ComponentType.PLASMA}),

  VOIDRAY(Chassis.FLYING, new ComponentType[]{
      ComponentType.BEAM, ComponentType.PLATING}),

  FLYING_COLONIST(Chassis.FLYING, ComponentType.CONSTRUCTOR,
      ComponentType.SIGHT),

  SOLDIER(Chassis.LIGHT, ComponentType.RADAR, ComponentType.BLASTER,
      ComponentType.PLATING),

  FLYING_SOLDIER(Chassis.FLYING, ComponentType.BLASTER, ComponentType.RADAR),

  HEAVY_SOLDIER(Chassis.HEAVY, ComponentType.JUMP, ComponentType.JUMP,
      ComponentType.RAILGUN, ComponentType.RADAR, ComponentType.REGEN),

  HEAVY_COLONIST(Chassis.HEAVY, ComponentType.JUMP, ComponentType.JUMP,
      ComponentType.CONSTRUCTOR, ComponentType.RADAR, ComponentType.REGEN),

  JUMPER(Chassis.HEAVY, ComponentType.JUMP, ComponentType.JUMP,
      ComponentType.RADAR),

  INITIAL_COLONIST(Chassis.LIGHT, ComponentType.CONSTRUCTOR,
      ComponentType.SIGHT),

  ARMORY(Chassis.BUILDING, ComponentType.ARMORY),

  FACTORY(Chassis.BUILDING, ComponentType.FACTORY),

  RECYCLER(Chassis.BUILDING, ComponentType.RECYCLER),

  END(Chassis.LIGHT, ComponentType.RECYCLER);

  public final Chassis chassis;
  public final ComponentType[] components;
  public double totalCost, upkeep;
  public int needsArmory = 0, needsFactory = 0, needsFactoryOrArmory = 0,
      needsRecycler = 0, needsConstructor = 0;
  public boolean needsDistributedBuild = false;

  Role(Chassis c, ComponentType... componentTypes) {
    chassis = c;
    components = componentTypes;
    if (U.find(components, ComponentType.RECYCLER)) {
      upkeep = -3;
    } else {
      upkeep = chassis.upkeep;
    }
    _computeRequirements();
  }

  public final void _computeRequirements() {
    int componentsCost = 0;
    for (int j = components.length - 1; j >= 0; j--) {
      final ComponentType component = components[j];
      componentsCost += component.cost;

      // $ +gen:source Role.BuildRequirements component
      if (BuildMappings.canBuild(ComponentType.RECYCLER, component)) {
        needsRecycler = 1;
      } else {
        if (BuildMappings.canBuild(ComponentType.FACTORY, component)) {
          if (BuildMappings.canBuild(ComponentType.ARMORY, component)) {
            needsFactoryOrArmory = 1;
          } else {
            needsFactory = 1;
          }
        } else if (BuildMappings.canBuild(ComponentType.ARMORY, component)) {
          if (BuildMappings.canBuild(ComponentType.FACTORY, component)) {
            needsFactoryOrArmory = 1;
          } else {
            needsArmory = 1;
          }
        } else {
          D.debug_assert(
              BuildMappings.canBuild(ComponentType.CONSTRUCTOR, component),
              "Broken build requirement computation.");
          needsConstructor = 1;
        }
      }
      // $ -gen:source
    }
    totalCost = chassis.cost + componentsCost;
    // $ +gen:target Role.BuildRequirements chassis
    if (BuildMappings.canBuild(ComponentType.RECYCLER, chassis)) {
      needsRecycler = 1;
    } else {
      if (BuildMappings.canBuild(ComponentType.FACTORY, chassis)) {
        if (BuildMappings.canBuild(ComponentType.ARMORY, chassis)) {
          needsFactoryOrArmory = 1;
        } else {
          needsFactory = 1;
        }
      } else if (BuildMappings.canBuild(ComponentType.ARMORY, chassis)) {
        if (BuildMappings.canBuild(ComponentType.FACTORY, chassis)) {
          needsFactoryOrArmory = 1;
        } else {
          needsArmory = 1;
        }
      } else {
        D.debug_assert(
            BuildMappings.canBuild(ComponentType.CONSTRUCTOR, chassis),
            "Broken build requirement computation.");
        needsConstructor = 1;
      }
    }
    // $ -gen:target
    if (needsFactoryOrArmory == 1 && (needsFactory + needsArmory > 0)) {
      // Cut out factory/armory accounting if at least one of them is absolutely
      // needed.
      needsFactoryOrArmory = 0;
    }
    needsDistributedBuild = needsRecycler + needsFactory + needsArmory
        + needsFactoryOrArmory > 1;
  }
}
