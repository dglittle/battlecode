package sprint4.test.xconst;

import sprint4.core.xconst.XChassis;
import battlecode.common.Chassis;

public final class XChassisTest {
  public static void debug_Test() {
    testChassisIntMapping();
  }

  public static void testChassisIntMapping() {
    Chassis[] goldTypes = Chassis.values();

    int l = goldTypes.length;
    int rl = XChassis.intToChassis.length;
    if (goldTypes.length != XChassis.intToChassis.length) {
      throw new RuntimeException("intToChassis.length is " + rl + " not " + l);
    }
    for (int i = 0; i < l; i++) {
      Chassis t = goldTypes[i];
      Chassis rt = XChassis.intToChassis[i];
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

    if (Chassis.BUILDING.ordinal() != XChassis.BUILDING_INT) {
      throw new RuntimeException("BUILDING.ordinal() is wrong");
    }
    if (Chassis.DEBRIS.ordinal() != XChassis.DEBRIS_INT) {
      throw new RuntimeException("DEBRIS.ordinal() is wrong");
    }
    if (Chassis.DUMMY.ordinal() != XChassis.DUMMY_INT) {
      throw new RuntimeException("DUMMY.ordinal() is wrong");
    }
    if (Chassis.FLYING.ordinal() != XChassis.FLYING_INT) {
      throw new RuntimeException("FLYING.ordinal() is wrong");
    }
    if (Chassis.HEAVY.ordinal() != XChassis.HEAVY_INT) {
      throw new RuntimeException("HEAVY.ordinal() is wrong");
    }
    if (Chassis.LIGHT.ordinal() != XChassis.LIGHT_INT) {
      throw new RuntimeException("LIGHT.ordinal() is wrong");
    }
    if (Chassis.MEDIUM.ordinal() != XChassis.MEDIUM_INT) {
      throw new RuntimeException("MEDIUM.ordinal() is wrong");
    }
  }
}
