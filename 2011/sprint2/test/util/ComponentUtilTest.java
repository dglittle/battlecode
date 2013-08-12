package sprint2.test.util;

import sprint2.util.ComponentUtil;
import battlecode.common.ComponentType;

public final class ComponentUtilTest {
  public static void debug_Test() {
    testComponentIntMapping();
  }

  public static void testComponentIntMapping() {
    ComponentType[] goldTypes = ComponentType.values();

    int l = goldTypes.length;
    int rl = ComponentUtil.intToComponentType.length;
    if (goldTypes.length != ComponentUtil.intToComponentType.length) {
      throw new RuntimeException("intToComponentType.length is " + rl + " not " + l);
    }
    for (int i = 0; i < l; i++) {
      ComponentType t = goldTypes[i];
      ComponentType rt = ComponentUtil.intToComponentType[i];
      if (t != rt) {
        throw new RuntimeException("intToComponentType[" + i + "] is " + rt + " not " + t);
      }
            
      int ti = t.ordinal();
      if (ti != i) {
        throw new RuntimeException(t.toString() + ".ordinal() is " + ti + " not " + i);
      }
    }
    
    if (ComponentType.SHIELD.ordinal() != ComponentUtil.SHIELD_INT) { throw new RuntimeException("SHIELD.ordinal() is wrong"); }
    if (ComponentType.HARDENED.ordinal() != ComponentUtil.HARDENED_INT) { throw new RuntimeException("HARDENED.ordinal() is wrong"); }
    if (ComponentType.REGEN.ordinal() != ComponentUtil.REGEN_INT) { throw new RuntimeException("REGEN.ordinal() is wrong"); }
    if (ComponentType.PLASMA.ordinal() != ComponentUtil.PLASMA_INT) { throw new RuntimeException("PLASMA.ordinal() is wrong"); }
    if (ComponentType.IRON.ordinal() != ComponentUtil.IRON_INT) { throw new RuntimeException("IRON.ordinal() is wrong"); }
    if (ComponentType.PLATING.ordinal() != ComponentUtil.PLATING_INT) { throw new RuntimeException("PLATING.ordinal() is wrong"); }
    if (ComponentType.SMG.ordinal() != ComponentUtil.SMG_INT) { throw new RuntimeException("SMG.ordinal() is wrong"); }
    if (ComponentType.BLASTER.ordinal() != ComponentUtil.BLASTER_INT) { throw new RuntimeException("BLASTER.ordinal() is wrong"); }
    if (ComponentType.RAILGUN.ordinal() != ComponentUtil.RAILGUN_INT) { throw new RuntimeException("RAILGUN.ordinal() is wrong"); }
    if (ComponentType.HAMMER.ordinal() != ComponentUtil.HAMMER_INT) { throw new RuntimeException("HAMMER.ordinal() is wrong"); }
    if (ComponentType.BEAM.ordinal() != ComponentUtil.BEAM_INT) { throw new RuntimeException("BEAM.ordinal() is wrong"); }
    if (ComponentType.MEDIC.ordinal() != ComponentUtil.MEDIC_INT) { throw new RuntimeException("MEDIC.ordinal() is wrong"); }
    if (ComponentType.SATELLITE.ordinal() != ComponentUtil.SATELLITE_INT) { throw new RuntimeException("SATELLITE.ordinal() is wrong"); }
    if (ComponentType.TELESCOPE.ordinal() != ComponentUtil.TELESCOPE_INT) { throw new RuntimeException("TELESCOPE.ordinal() is wrong"); }
    if (ComponentType.SIGHT.ordinal() != ComponentUtil.SIGHT_INT) { throw new RuntimeException("SIGHT.ordinal() is wrong"); }
    if (ComponentType.RADAR.ordinal() != ComponentUtil.RADAR_INT) { throw new RuntimeException("RADAR.ordinal() is wrong"); }
    if (ComponentType.ANTENNA.ordinal() != ComponentUtil.ANTENNA_INT) { throw new RuntimeException("ANTENNA.ordinal() is wrong"); }
    if (ComponentType.DISH.ordinal() != ComponentUtil.DISH_INT) { throw new RuntimeException("DISH.ordinal() is wrong"); }
    if (ComponentType.NETWORK.ordinal() != ComponentUtil.NETWORK_INT) { throw new RuntimeException("NETWORK.ordinal() is wrong"); }
    if (ComponentType.PROCESSOR.ordinal() != ComponentUtil.PROCESSOR_INT) { throw new RuntimeException("PROCESSOR.ordinal() is wrong"); }
    if (ComponentType.JUMP.ordinal() != ComponentUtil.JUMP_INT) { throw new RuntimeException("JUMP.ordinal() is wrong"); }
    if (ComponentType.DUMMY.ordinal() != ComponentUtil.DUMMY_INT) { throw new RuntimeException("DUMMY.ordinal() is wrong"); }
    if (ComponentType.BUG.ordinal() != ComponentUtil.BUG_INT) { throw new RuntimeException("BUG.ordinal() is wrong"); }
    if (ComponentType.DROPSHIP.ordinal() != ComponentUtil.DROPSHIP_INT) { throw new RuntimeException("DROPSHIP.ordinal() is wrong"); }
    if (ComponentType.RECYCLER.ordinal() != ComponentUtil.RECYCLER_INT) { throw new RuntimeException("RECYCLER.ordinal() is wrong"); }
    if (ComponentType.FACTORY.ordinal() != ComponentUtil.FACTORY_INT) { throw new RuntimeException("FACTORY.ordinal() is wrong"); }
    if (ComponentType.CONSTRUCTOR.ordinal() != ComponentUtil.CONSTRUCTOR_INT) { throw new RuntimeException("CONSTRUCTOR.ordinal() is wrong"); }
    if (ComponentType.ARMORY.ordinal() != ComponentUtil.ARMORY_INT) { throw new RuntimeException("ARMORY.ordinal() is wrong"); }
    if (ComponentType.SMALL_MOTOR.ordinal() != ComponentUtil.SMALL_MOTOR_INT) { throw new RuntimeException("SMALL_MOTOR.ordinal() is wrong"); }
    if (ComponentType.MEDIUM_MOTOR.ordinal() != ComponentUtil.MEDIUM_MOTOR_INT) { throw new RuntimeException("MEDIUM_MOTOR.ordinal() is wrong"); }
    if (ComponentType.LARGE_MOTOR.ordinal() != ComponentUtil.LARGE_MOTOR_INT) { throw new RuntimeException("LARGE_MOTOR.ordinal() is wrong"); }
    if (ComponentType.FLYING_MOTOR.ordinal() != ComponentUtil.FLYING_MOTOR_INT) { throw new RuntimeException("FLYING_MOTOR.ordinal() is wrong"); }
    if (ComponentType.BUILDING_MOTOR.ordinal() != ComponentUtil.BUILDING_MOTOR_INT) { throw new RuntimeException("BUILDING_MOTOR.ordinal() is wrong"); }
    if (ComponentType.BUILDING_SENSOR.ordinal() != ComponentUtil.BUILDING_SENSOR_INT) { throw new RuntimeException("BUILDING_SENSOR.ordinal() is wrong"); }
  }
}
