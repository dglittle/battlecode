package sprint4.core;

import sprint4.core.xconst.PrintingTags;
import battlecode.common.RobotController;

/**
 * Debug methods.
 */
public class D {
  /** Logs an exception so it shows up in the match output and so that tests fail. */
  public static final void debug_logException(Exception exception) {
    exception.printStackTrace();
  }
  
  /** Logs an exception so it shows up in the match output and so that tests fail.
   *
   * This indicates a broken precondition in the code.
   */
  public static final void debug_logException(String message) {    
    // NOTE: using throw-catch to get a stack trace.
    try {
      throw new RuntimeException(message);
    }
    catch(RuntimeException e) {
      e.printStackTrace();
    }
  }

  /**
   * Wraps {@link RobotController#setIndicatorString(int, String)} but only runs in debug mode.
   */
  public static final void debug_setIndicator(int index, String message) {
    S.rc.setIndicatorString(index, message);
  }
  
  /**
   * Logs an exception if the condition is false.
   */
  public static final void debug_assert(boolean condition, String message) {
    if (condition) { return; }
    
    // NOTE: using throw-catch to get a stack trace.
    try {
      throw new RuntimeException("Assertion failed: " + message);
    }
    catch(RuntimeException e) {
      e.printStackTrace();
    }    
  }
  
  /**
   * Wraps {@link Systme.out.println(String s)} but only runs in debug mode.
   * @param s the string to be printed
   */
  public static final void debug_pg(String s) {
    if (PrintingTags.GREG) {
      System.out.println(s);
    }
  }

  /**
   * Wraps {@link Systme.out.println(String s)} but only runs in debug mode.
   * @param s the string to be printed
   */
  public static final void debug_pl(String tag, String s) {
    if (PrintingTags.YAFIM) {
      System.out.println("!!! ASSERTION FAILED :: " + s);
      S.rc.suicide();
    }
  }

  /**
   * Wraps {@link Systme.out.println(String s)} but only runs in debug mode.
   * @param s the string to be printed
   */
  public static final void debug_pv(String s) {
    if (PrintingTags.VICTOR) {
      System.out.println(s);
    }
  }

  /**
   * Wraps {@link Systme.out.println(String s)} but only runs in debug mode.
   * @param s the string to be printed
   */
  public static final void debug_py(String s) {
    if (PrintingTags.YING) {
      System.out.println(s);
    }
  }
}
