package team050.test.xconst;

import team050.core.xconst.XComponentClass;
import battlecode.common.ComponentClass;

public class XComponentClassTest {
  public static void debug_Test() {
    testComponentClassIntMapping();
  }
  
  public static void testComponentClassIntMapping() {
    ComponentClass[] goldTypes = ComponentClass.values();

    int l = goldTypes.length;
    int rl = XComponentClass.intToComponentClass.length;
    if (goldTypes.length != XComponentClass.intToComponentClass.length) {
      throw new RuntimeException("intToComponentClass.length is " + rl + " not " + l);
    }
    for (int i = 0; i < l; i++) {
      ComponentClass t = goldTypes[i];
      ComponentClass rt = XComponentClass.intToComponentClass[i];
      if (t != rt) {
        throw new RuntimeException("intToComponentClass[" + i + "] is " + rt + " not " + t);
      }
            
      int ti = t.ordinal();
      if (ti != i) {
        throw new RuntimeException(t.toString() + ".ordinal() is " + ti + " not " + i);
      }
    }
    
    if (ComponentClass.ARMOR.ordinal() != XComponentClass.ARMOR_INT) {
      throw new RuntimeException("ARMOR.ordinal() is wrong");
    }
    if (ComponentClass.BUILDER.ordinal() != XComponentClass.BUILDER_INT) {
      throw new RuntimeException("BUILDER.ordinal() is wrong");
    }
    if (ComponentClass.COMM.ordinal() != XComponentClass.COMM_INT) {
      throw new RuntimeException("COMM.ordinal() is wrong");
    }
    if (ComponentClass.MISC.ordinal() != XComponentClass.MISC_INT) {
      throw new RuntimeException("MISC.ordinal() is wrong");
    }
    if (ComponentClass.MOTOR.ordinal() != XComponentClass.MOTOR_INT) {
      throw new RuntimeException("MOTOR.ordinal() is wrong");
    }
    if (ComponentClass.SENSOR.ordinal() != XComponentClass.SENSOR_INT) {
      throw new RuntimeException("SENSOR.ordinal() is wrong");
    }
    if (ComponentClass.WEAPON.ordinal() != XComponentClass.WEAPON_INT) {
      throw new RuntimeException("WEAPON.ordinal() is wrong");
    }
  }  
}
