package team050.test.core;

import team050.core.Role;
import team050.core.xconst.XComponentType;
import battlecode.common.ComponentType;

public final class RoleTest {
  public static void debug_Test() {
    testComponents();
  }
  
  /** Makes sure that the roles' components are unambiguous. */
  public static void testComponents() {
    Role[] roles = Role.values();
    for (int i = 0; i < roles.length; i++) {
      for (int j = 0; j < roles.length; j++) {
        if (i == j) { continue; }
        
        if (roles[i].chassis != roles[j].chassis) { continue; }
        if (_included(roles[i].components, roles[j].components)) {
          throw new RuntimeException("Role " + roles[j] + " is a strict subset of " + roles[i]);
        }
      }
    }
  }
  
  /** True if small is a strict subset of big. */
  public static boolean _included(ComponentType[] big, ComponentType[] small) {
    int[] typeCount = new int[XComponentType.COMPONENT_TYPES];
    for (int i = big.length - 1; i >= 0; i--) {
      typeCount[big[i].ordinal()]++;
    }
    for (int i = small.length - 1; i >= 0; i--) {
      final int typeInt = small[i].ordinal();
      if (typeCount[typeInt] > 0) {
        typeCount[typeInt]--;
      } else {
        return false;
      }
    }
    return true;
  }
}
