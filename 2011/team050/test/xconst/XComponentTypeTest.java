package team050.test.xconst;

import team050.core.xconst.XComponentType;
import battlecode.common.ComponentType;

public final class XComponentTypeTest {
  public static void debug_Test() {
    testComponentIntMapping();
  }

  public static void testComponentIntMapping() {
    ComponentType[] goldTypes = ComponentType.values();

    int l = goldTypes.length;
    int rl = XComponentType.intToComponentType.length;
    if (goldTypes.length != XComponentType.intToComponentType.length) {
      throw new RuntimeException("intToComponentType.length is " + rl + " not " + l);
    }
    for (int i = 0; i < l; i++) {
      ComponentType t = goldTypes[i];
      ComponentType rt = XComponentType.intToComponentType[i];
      if (t != rt) {
        throw new RuntimeException("intToComponentType[" + i + "] is " + rt + " not " + t);
      }
            
      int ti = t.ordinal();
      if (ti != i) {
        throw new RuntimeException(t.toString() + ".ordinal() is " + ti + " not " + i);
      }
    }
    
    if (ComponentType.SHIELD.ordinal() != XComponentType.SHIELD_INT) { throw new RuntimeException("SHIELD.ordinal() is wrong"); }
    if (ComponentType.HARDENED.ordinal() != XComponentType.HARDENED_INT) { throw new RuntimeException("HARDENED.ordinal() is wrong"); }
    if (ComponentType.REGEN.ordinal() != XComponentType.REGEN_INT) { throw new RuntimeException("REGEN.ordinal() is wrong"); }
    if (ComponentType.PLASMA.ordinal() != XComponentType.PLASMA_INT) { throw new RuntimeException("PLASMA.ordinal() is wrong"); }
    if (ComponentType.IRON.ordinal() != XComponentType.IRON_INT) { throw new RuntimeException("IRON.ordinal() is wrong"); }
    if (ComponentType.PLATING.ordinal() != XComponentType.PLATING_INT) { throw new RuntimeException("PLATING.ordinal() is wrong"); }
    if (ComponentType.SMG.ordinal() != XComponentType.SMG_INT) { throw new RuntimeException("SMG.ordinal() is wrong"); }
    if (ComponentType.BLASTER.ordinal() != XComponentType.BLASTER_INT) { throw new RuntimeException("BLASTER.ordinal() is wrong"); }
    if (ComponentType.RAILGUN.ordinal() != XComponentType.RAILGUN_INT) { throw new RuntimeException("RAILGUN.ordinal() is wrong"); }
    if (ComponentType.HAMMER.ordinal() != XComponentType.HAMMER_INT) { throw new RuntimeException("HAMMER.ordinal() is wrong"); }
    if (ComponentType.BEAM.ordinal() != XComponentType.BEAM_INT) { throw new RuntimeException("BEAM.ordinal() is wrong"); }
    if (ComponentType.MEDIC.ordinal() != XComponentType.MEDIC_INT) { throw new RuntimeException("MEDIC.ordinal() is wrong"); }
    if (ComponentType.SATELLITE.ordinal() != XComponentType.SATELLITE_INT) { throw new RuntimeException("SATELLITE.ordinal() is wrong"); }
    if (ComponentType.TELESCOPE.ordinal() != XComponentType.TELESCOPE_INT) { throw new RuntimeException("TELESCOPE.ordinal() is wrong"); }
    if (ComponentType.SIGHT.ordinal() != XComponentType.SIGHT_INT) { throw new RuntimeException("SIGHT.ordinal() is wrong"); }
    if (ComponentType.RADAR.ordinal() != XComponentType.RADAR_INT) { throw new RuntimeException("RADAR.ordinal() is wrong"); }
    if (ComponentType.ANTENNA.ordinal() != XComponentType.ANTENNA_INT) { throw new RuntimeException("ANTENNA.ordinal() is wrong"); }
    if (ComponentType.DISH.ordinal() != XComponentType.DISH_INT) { throw new RuntimeException("DISH.ordinal() is wrong"); }
    if (ComponentType.NETWORK.ordinal() != XComponentType.NETWORK_INT) { throw new RuntimeException("NETWORK.ordinal() is wrong"); }
    if (ComponentType.PROCESSOR.ordinal() != XComponentType.PROCESSOR_INT) { throw new RuntimeException("PROCESSOR.ordinal() is wrong"); }
    if (ComponentType.JUMP.ordinal() != XComponentType.JUMP_INT) { throw new RuntimeException("JUMP.ordinal() is wrong"); }
    if (ComponentType.DUMMY.ordinal() != XComponentType.DUMMY_INT) { throw new RuntimeException("DUMMY.ordinal() is wrong"); }
    if (ComponentType.BUG.ordinal() != XComponentType.BUG_INT) { throw new RuntimeException("BUG.ordinal() is wrong"); }
    if (ComponentType.DROPSHIP.ordinal() != XComponentType.DROPSHIP_INT) { throw new RuntimeException("DROPSHIP.ordinal() is wrong"); }
    if (ComponentType.RECYCLER.ordinal() != XComponentType.RECYCLER_INT) { throw new RuntimeException("RECYCLER.ordinal() is wrong"); }
    if (ComponentType.FACTORY.ordinal() != XComponentType.FACTORY_INT) { throw new RuntimeException("FACTORY.ordinal() is wrong"); }
    if (ComponentType.CONSTRUCTOR.ordinal() != XComponentType.CONSTRUCTOR_INT) { throw new RuntimeException("CONSTRUCTOR.ordinal() is wrong"); }
    if (ComponentType.ARMORY.ordinal() != XComponentType.ARMORY_INT) { throw new RuntimeException("ARMORY.ordinal() is wrong"); }
    if (ComponentType.SMALL_MOTOR.ordinal() != XComponentType.SMALL_MOTOR_INT) { throw new RuntimeException("SMALL_MOTOR.ordinal() is wrong"); }
    if (ComponentType.MEDIUM_MOTOR.ordinal() != XComponentType.MEDIUM_MOTOR_INT) { throw new RuntimeException("MEDIUM_MOTOR.ordinal() is wrong"); }
    if (ComponentType.LARGE_MOTOR.ordinal() != XComponentType.LARGE_MOTOR_INT) { throw new RuntimeException("LARGE_MOTOR.ordinal() is wrong"); }
    if (ComponentType.FLYING_MOTOR.ordinal() != XComponentType.FLYING_MOTOR_INT) { throw new RuntimeException("FLYING_MOTOR.ordinal() is wrong"); }
    if (ComponentType.BUILDING_MOTOR.ordinal() != XComponentType.BUILDING_MOTOR_INT) { throw new RuntimeException("BUILDING_MOTOR.ordinal() is wrong"); }
    if (ComponentType.BUILDING_SENSOR.ordinal() != XComponentType.BUILDING_SENSOR_INT) { throw new RuntimeException("BUILDING_SENSOR.ordinal() is wrong"); }
  }
}
