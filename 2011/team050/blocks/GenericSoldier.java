package team050.blocks;

import team050.blocks.pathfinding.BugPathfindingBlock;
import team050.core.D;
import team050.core.M;
import team050.core.S;
import team050.core.X;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class GenericSoldier {

  public static final int HOTSPOT_IGNORE_RADIUS_SQ = 25;
  public static final int HOTSPOT_IGNORE_TIMEOUT_ROUNDS = 400;

  public static MapLocation ignoreHotspotLocation = null;
  public static int ignoreHotspotTimeout = 0;

  public static final void go() throws GameActionException {
    if (S.jumpControllers != null) {
      M.disableMapUpdates();
    }

    while (true) {
      JumpExplorationBlock.update();
      D.debug_pl("go", "----- Round " + S.round + " -----");

      // Before we do anything, we want to see if we're too low in health.

      // If we're being attacked but don't see anybody, jump forward and turn
      // around if we can jump; or just turn around if we can't jump.
      if (CombatBlock.checkForAttackFromBehind()) {
        if (S.jumpControllers != null) {
          if (S.jumpReady())
            JumpUtil.jump(S.direction);
        }
        if (S.motorReady)
          X.setDirection(S.direction.opposite());
        X.yield();
        continue;
      }

      // TODO: implement jumping away tactic when health is low

      // ////////////////////////
      // Combat

      D.debug_pl("go", "Invoking combat block.");
      MapLocation hotspot = HotspotSensor.hotspot();
      boolean performedAttackAction = CombatBlock.attackBestTargetAsync();
      if (performedAttackAction) {
        D.debug_pl("go", "Performed combat action... yielding.");
        X.yield();
        continue;
      }

      // ////////////////////////
      // Movement
      boolean shouldExplore = true;
      if (hotspot != null) {
        D.debug_pl(
            "go",
            "Hotspot: "
                + (hotspot != null ? D.debug_offset(hotspot) : "null")
                + " Ignore hotspot: "
                + (ignoreHotspotLocation != null ? D.debug_offset(hotspot,
                    ignoreHotspotLocation) : "none") + " "
                + ignoreHotspotTimeout);
        if (ignoreHotspotTimeout > 0
            && ignoreHotspotLocation != null
            && hotspot.distanceSquaredTo(ignoreHotspotLocation) <= HOTSPOT_IGNORE_RADIUS_SQ) {
          // Ignore the hotspot and decrement the timeout.
          D.debug_pl("go", "Decrementing hotspot ignore timeout.");
          if (--ignoreHotspotTimeout <= 0)
            ignoreHotspotLocation = null;
        } else {
          shouldExplore = false;
          Direction directionToHotspot = S.location.directionTo(hotspot);
          // Check if the hotspot can be seen by our sensor.
          boolean canSeeHotspot = S.sensorController.canSenseSquare(hotspot);
          boolean jumped = false;
          if (!canSeeHotspot) {
            // If we're within range, turn.
            if (S.location.distanceSquaredTo(hotspot) <= S.sensorType.range) {
              if (S.motorReady)
                X.setDirection(directionToHotspot);
            }
            // If we aren't, get to it quickly.
            else {
              D.debug_pl("go",
                  "We can't see the target and we're too far away from it.");
              if (S.jumpControllers != null) {
                D.debug_pl("go", "We have jump controllers.");
                if (S.jumpReady()) {
                  D.debug_pl("go", "Our jump is ready.");
                  jumped = JumpUtil.jump(directionToHotspot);
                  if (!jumped) {
                    D.debug_pl(
                        "go",
                        "We didn't jump. Ignoring hotspot "
                            + D.debug_offset(hotspot) + ".");
                    ignoreHotspotLocation = hotspot;
                    ignoreHotspotTimeout = HOTSPOT_IGNORE_TIMEOUT_ROUNDS;
                  }
                }
                D.debug_pl("go", "Jump is not ready.");
                if (jumped) {
                  D.debug_pl("go", "We jumped and now we'll try to move.");
                  if (S.direction != directionToHotspot) {
                    D.debug_pl("go", "Our direction does not line up with the direction to the hotspot.");
                    if (S.motorReady)
                      X.setDirection(directionToHotspot);
                  }
                } else {
                  D.debug_pl("go", "We didn't jump and now we'll try to move.");
                  if (S.direction != directionToHotspot) {
                    if (S.motorReady)
                      X.setDirection(directionToHotspot);
                    else if (S.motorReady)
                      X.moveTowardsAsync(directionToHotspot);
                  }
                }
              } else {
                BugPathfindingBlock.bugTowardAsync(hotspot);
              }
            }
          } else {
            shouldExplore = true;
          }
        }
      }
      if (shouldExplore) {
        D.debug_pl("go", "Invoking exploration block (no hotspot).");
        D.debug_pl("go", "Exploring...");
        if (S.jumpControllers != null) {
          JumpExplorationBlock.step();
        } else {
          LittleExplorationBlock.exploreMove();
        }
      }

      X.yield();
    }
  }
}
