package legenClone.util;

import java.util.ArrayList;

import legenClone.core.B;
import legenClone.core.Callback;
import legenClone.core.CommandType;
import legenClone.core.M;
import legenClone.core.S;
import legenClone.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Mine;
import battlecode.common.MineInfo;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class LittleUtil {

  public static final void printMapAroundMe(int radius) {
    for (int y = -10; y <= radius; y++) {
      String s = "";
      for (int x = -10; x <= radius; x++) {
        if (x == 0 && y == 0) {
          s += "M";
        } else if (!M.known[(S.locationX + x) - M.arrayBaseX][(S.locationY + y)
            - M.arrayBaseY]) {
          s += ".";
        } else if (M.land[(S.locationX + x) - M.arrayBaseX][(S.locationY + y)
            - M.arrayBaseY]) {
          s += "*";
        } else {
          s += "#";
        }
      }
      System.out.println(s);
    }
  }

  public static final String offsetTo(MapLocation m) {
    return "(" + (m.x - S.locationX) + ", " + (m.y - S.locationY) + ")";
  }

  public static final boolean moveOffSpotAsync() throws GameActionException {
    if (S.canMove(S.direction)) {
      X.moveForward();
      return true;
    } else if (S.canMove(S.direction.opposite())) {
      X.moveBackward();
      return true;
    } else {
      Direction d = S.direction;
      for (int i = 0; i < 7; i++) {
        d = d.rotateLeft();
        if (S.canMove(d)) {
          X.setDirection(d);
          return true;
        }
      }
    }
    return false;
  }

  public static final MapLocation nearestEmptyMine() throws GameActionException {
    MapLocation bestM = null;
    int bestDist = Integer.MAX_VALUE;
    for (Mine m : S.senseMines()) {
      MapLocation loc = m.getLocation();
      Robot r = S.senseRobot(loc, RobotLevel.ON_GROUND);
      if ((r == null) || (r.getID() == S.id)) {
        int dist = loc.distanceSquaredTo(S.location);
        if (dist < bestDist) {
          bestDist = dist;
          bestM = loc;
        }
      }
    }
    return bestM;
  }

  public static final MapLocation findPlaceByBuildingToBuild()
      throws GameActionException {
    for (Direction d : MapUtil.dirs) {
      MapLocation loc = S.location.add(d);
      Robot r = S.senseRobot(loc, RobotLevel.ON_GROUND);
      if (r == null) {
        return loc;
      }
    }
    return null;
  }

  public static final void buildSync(MapLocation here, Chassis c,
      ComponentType... cts) throws GameActionException {
    S.builderController.build(c, here);
    for (ComponentType ct : cts) {
      X.yield();
      S.builderController.build(ct, here, c.level);
    }
  }

  public static boolean hasComponents(ComponentType... types) {
    int count = 0;
    for (ComponentController comp : S.rc.components()) {
      for (int i = 0; i < types.length; i++) {
        ComponentType type = types[i];
        if (comp.type() == type) {
          count++;
          types[i] = null;
        }
      }
    }
    return count >= types.length;
  }

  public static boolean hasComponentsReady(ComponentType... types) {
    int count = 0;
    for (ComponentController comp : S.rc.components()) {
      for (int i = 0; i < types.length; i++) {
        ComponentType type = types[i];
        if (comp.type() == type && !comp.isActive()) {
          count++;
          types[i] = null;
        }
      }
    }
    return count >= types.length;
  }

  public static <T> boolean find(T[] a, T b) {
    for (T aa : a) {
      if (aa.equals(b))
        return true;
    }
    return false;
  }

  public static void waitForComponents() throws GameActionException {
    l1 : while (true) {
      for (ComponentController c : S.rc.components()) {
        if (c.isActive()) {
          X.yield();
          continue l1;
        }
      }
      break;
    }
  }

  public static void waitForever() throws GameActionException {
    while (true) {
      X.yield();
    }
  }

  public static void waitForFlux(float amount) throws GameActionException {
    while (S.flux < amount) {
      X.yield();
    }
  }
}
