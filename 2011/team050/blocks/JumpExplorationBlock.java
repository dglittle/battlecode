package team050.blocks;

import team050.blocks.pathfinding.BugPathfindingBlock;
import team050.core.D;
import team050.core.S;
import team050.core.X;
import battlecode.common.Chassis;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class JumpExplorationBlock {

  public static Direction macroDir = null;
  public static boolean lefty = false;
  public static boolean inited = false;

  public static final void init() {
    SimpleMapExtents.init();
    macroDir = SimpleMapExtents.randomDirAwayFromEdge();
    lefty = S.randomBoolean();
    inited = true;
  }

  public static final void update() {
    if (!inited)
      init();
    SimpleMapExtents.update();
  }

  public static final void step() throws GameActionException {
    // update macroDir
    // if (SimpleMapExtents.distFromEdge() < 10) {
    // macroDir = SimpleMapExtents.randomDirAwayFromEdge();
    // }
    {
      int x = 0;
      int y = 0;
      int count = 0;
      for (RobotInfo ri : S.nearbyRobotInfos()) {
        if (ri != null && ri.robot.getTeam() == S.team && ri.chassis != Chassis.BUILDING) {
          x += ri.location.x;
          y += ri.location.y;
          count++;
        }
      }
      if (count > 0) {
        MapLocation them = new MapLocation(x / count, y / count);
        macroDir = them.directionTo(S.location);
        if (macroDir.ordinal() >= 8) {
          macroDir = SimpleMapExtents.randomDirAwayFromEdge();
        }
      }
    }

    // try jumping
    if (S.jumpReady()) {
      D.debug_pl("JumpExplorationBlock", "Jump is ready. Trying to jump in " + macroDir + ".");
      if (!JumpUtil.jump(macroDir)) {
//        if (lefty) {
//          macroDir = macroDir.rotateLeft().rotateLeft();
//        } else {
//          macroDir = macroDir.rotateRight().rotateRight();
//        }
        macroDir = SimpleMapExtents.randomDirAwayFromEdge();
        D.debug_pl("JumpExplorationBlock", "We jumped, now the macro direction is " + macroDir + ".");
      }
    }

    // try moving
    if (S.motorReady) {
      Direction d = BugPathfindingBlock.nextDirection(S.location.add(macroDir,
          20));
      if (d.ordinal() < 8) {
        if (S.direction != d) {
          X.setDirection(d);
        } else {
          X.moveForward();
        }
      }
    }
  }
}
