package team050.blocks;

import team050.blocks.brain.BrainLarvaBlock;
import team050.blocks.building.BuildBlock;
import team050.blocks.pathfinding.BugPathfindingBlock;
import team050.core.D;
import team050.core.M;
import team050.core.Role;
import team050.core.S;
import team050.core.U;
import team050.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class Colonist {
  public static final Role[] recyclerRole = {Role.RECYCLER};
  public static final int OSCILLATION_LIMIT = 3;
  public static boolean _setRecyclerOrder = false;

  public static final void sync() {
    if (S.jumpControllers != null) {
      M.disableMapUpdates();
    }

    while (true) {
      async();
      X.yield();
    }
  }

  public static final void async() {
    // if jumper
    BrainLarvaBlock.async();
    
    if (S.jumpControllers != null) {
      JumpExplorationBlock.update();
    }

    if (!BuildBlock.startedBuilding()) {
      // Update goals if we are not already building.
      MineUpdatingBlock.update();
      // building section
      // if we're next to our goal mine, and we have enough flux, build on it
      final MapLocation goal = MineUpdatingBlock.goalMine;
      if (goal != null) {
        if (MineUpdatingBlock.isAdjacentToMine
            && MineUpdatingBlock.isFacingMine
            && S.flux > TierBlock.tieredRecyclerThreshold())
          _setRecyclerOrderAsync(goal);
      } else if (_setRecyclerOrder)
        _cancelReceylerOrderAsync();
    }

    ScvBuildDriverBlock.async();

    if (!BuildBlock.busy) {
      if (_setRecyclerOrder) {
        // Spin after finishing building a recycler to try to find surrounding
        // mines.
        _setRecyclerOrder = false;
        MineUpdatingBlock.reset();
      }
    }
    // ////////////////////////
    // movement section
    try {
      // see if we can move when we are not building
      if (S.motorReady && !BuildBlock.busy) {
        if (U.isTrapped())
          S.rc.suicide();
        // is there a mine to move toward?
        final MapLocation goal = MineUpdatingBlock.goalMine;
        if (goal != null) {
          if (S.location.isAdjacentTo(goal)
              && S.direction != S.location.directionTo(goal)) {
            X.setDirection(S.location.directionTo(goal));
          } else if (S.location.equals(goal)) {
            // if we're standing on it, move off
            U.moveOffSpotAsync();
          } else {
            if (S.jumpControllers != null) {
              // can we jump closer?
              boolean jumped = false;
              if (S.jumpReady()) {
                MapLocation loc = JumpUtil.closestPotentialJumpLocationTo_butNotOnIt(goal);
                if (loc != null) {
                  jumped = X.jump(loc);
                }
              }

              if (!jumped) {
                // if we're not close enough, move toward it
                BugPathfindingBlock.bugTowardAsync(goal, 2);
              }
            } else {
              // if we're not close enough, move toward it
              Direction d = BugPathfindingBlock.nextDirection(goal, 2);
              if (d.ordinal() < 8) {
                X.moveTowardsAsync(d);
              }
            }
          }
        } else {
          if (EnemyUpdatingBlock.hasNearbyEnemyWithWeapon()) {
            FleeBlock.async();
            return;
          } else if (MineUpdatingBlock.spin > 0) {
            // no mine, let's explore, I guess
            X.setDirection(S.direction.rotateLeft().rotateLeft());
            MineUpdatingBlock.spin--;
          } else {
            if (S.jumpControllers != null) {
              JumpExplorationBlock.step();
            } else {
              _exploreDirection = LittleExplorationBlock.explore();
              if (_exploreDirection.ordinal() < 8) {
                if (S.direction != _exploreDirection) {
                  X.setDirection(_exploreDirection);
                } else {
                  X.moveForward();
                }
              }
            }
          }
        }
      }
    } catch (GameActionException gae) {
      D.debug_logException(gae);
    }
  }

  public static final void _setRecyclerOrderAsync(MapLocation goal) {
    BuildBlock.setBuildOrder(recyclerRole, new MapLocation[]{goal}, "mine");
    _setRecyclerOrder = true;
  }

  public static final void _cancelReceylerOrderAsync() {
    BuildBlock.cancelBuildOrder();
    _setRecyclerOrder = false;
  }
  
  public static Direction _exploreDirection = Direction.NONE;
}
