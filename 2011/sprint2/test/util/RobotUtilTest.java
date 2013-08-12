package sprint2.test.util;

import sprint2.util.RobotUtil;
import battlecode.common.Chassis;
import battlecode.common.ComponentClass;

public final class RobotUtilTest {
  public static void debug_Test() {
    testChassisIntMapping();
    testComponentClassIntMapping();
  }

  public static void testChassisIntMapping() {
    Chassis[] goldTypes = Chassis.values();

    int l = goldTypes.length;
    int rl = RobotUtil.intToChassis.length;
    if (goldTypes.length != RobotUtil.intToChassis.length) {
      throw new RuntimeException("intToChassis.length is " + rl + " not " + l);
    }
    for (int i = 0; i < l; i++) {
      Chassis t = goldTypes[i];
      Chassis rt = RobotUtil.intToChassis[i];
      if (t != rt) {
        throw new RuntimeException("intToChassis[" + i + "] is " + rt + " not "
            + t);
      }

      int ti = t.ordinal();
      if (ti != i) {
        throw new RuntimeException(t.toString() + ".ordinal() is " + ti
            + " not " + i);
      }
    }

    if (Chassis.BUILDING.ordinal() != RobotUtil.BUILDING_INT) {
      throw new RuntimeException("BUILDNG.ordinal() is wrong");
    }
    if (Chassis.DEBRIS.ordinal() != RobotUtil.DEBRIS_INT) {
      throw new RuntimeException("DEBRIS.ordinal() is wrong");
    }
    if (Chassis.DUMMY.ordinal() != RobotUtil.DUMMY_INT) {
      throw new RuntimeException("DUMMY.ordinal() is wrong");
    }
    if (Chassis.FLYING.ordinal() != RobotUtil.FLYING_INT) {
      throw new RuntimeException("FLYING.ordinal() is wrong");
    }
    if (Chassis.HEAVY.ordinal() != RobotUtil.HEAVY_INT) {
      throw new RuntimeException("HEAVY.ordinal() is wrong");
    }
    if (Chassis.LIGHT.ordinal() != RobotUtil.LIGHT_INT) {
      throw new RuntimeException("LIGHT.ordinal() is wrong");
    }
    if (Chassis.MEDIUM.ordinal() != RobotUtil.MEDIUM_INT) {
      throw new RuntimeException("MEDIUM.ordinal() is wrong");
    }
  }

  public static void testComponentClassIntMapping() {
    ComponentClass[] goldTypes = ComponentClass.values();

    int l = goldTypes.length;
    int rl = RobotUtil.intToComponentClass.length;
    if (goldTypes.length != RobotUtil.intToComponentClass.length) {
      throw new RuntimeException("intToComponentClass.length is " + rl + " not " + l);
    }
    for (int i = 0; i < l; i++) {
      ComponentClass t = goldTypes[i];
      ComponentClass rt = RobotUtil.intToComponentClass[i];
      if (t != rt) {
        throw new RuntimeException("intToComponentClass[" + i + "] is " + rt + " not " + t);
      }
            
      int ti = t.ordinal();
      if (ti != i) {
        throw new RuntimeException(t.toString() + ".ordinal() is " + ti + " not " + i);
      }
    }
    
    if (ComponentClass.ARMOR.ordinal() != RobotUtil.ARMOR_INT) { throw new RuntimeException("ARMOR.ordinal() is wrong"); }
    if (ComponentClass.BUILDER.ordinal() != RobotUtil.BUILDER_INT) { throw new RuntimeException("BUILDER.ordinal() is wrong"); }
    if (ComponentClass.COMM.ordinal() != RobotUtil.COMM_INT) { throw new RuntimeException("COMM.ordinal() is wrong"); }
    if (ComponentClass.MISC.ordinal() != RobotUtil.MISC_INT) { throw new RuntimeException("MISC.ordinal() is wrong"); }
    if (ComponentClass.MOTOR.ordinal() != RobotUtil.MOTOR_INT) { throw new RuntimeException("MOTOR.ordinal() is wrong"); }
    if (ComponentClass.SENSOR.ordinal() != RobotUtil.SENSOR_INT) { throw new RuntimeException("SENSOR.ordinal() is wrong"); }
    if (ComponentClass.WEAPON.ordinal() != RobotUtil.WEAPON_INT) { throw new RuntimeException("WEAPON.ordinal() is wrong"); }
  }
}
