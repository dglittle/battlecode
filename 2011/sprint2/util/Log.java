package sprint2.util;

import battlecode.common.Clock;

public class Log {
  
  public static final boolean enabled = false;
  public static boolean alreadyUsed = false;
  
  public static final void out(String s) {
    if (enabled) {
      if (!alreadyUsed) {
        System.out.println("###############################################################################################");
        System.out.println("##### This is landa's log utility. Set Log.enabled = false to disable these log messages. #####");
        System.out.println("###############################################################################################");
        alreadyUsed = true;
      }
      System.out.println("!LANDA! " + Clock.getRoundNum() + "::" + s);
    }
  }
}
