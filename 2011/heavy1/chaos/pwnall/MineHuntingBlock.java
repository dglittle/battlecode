package heavy1.chaos.pwnall;

import heavy1.blocks.PathfindingBlock;
import heavy1.blocks.RandomMovementBlock;
import heavy1.core.D;
import heavy1.core.Role;
import heavy1.core.S;
import battlecode.common.Chassis;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

/** Tries to find mines and build on top of them. */
public class MineHuntingBlock {
  /** Maximum number of mines to remember. */
  public static final int MINE_MEMORY_SIZE = 8;
    
  /** Tries to make progress towards mine building. */
  public static final boolean async() {
    _checkGoalMine();
    _checkForMines();
    if (_goalLocation == null) {
      return false;
    } else {
      PathfindingBlock.setParameters(_goalLocation, 200);
    }
    if (_goalLocation.equals(S.location)) {
      if (!RandomMovementBlock.busy) {
        RandomMovementBlock.asyncSetup();
      }
      return RandomMovementBlock.async();
    } else if (_goalLocation.isAdjacentTo(S.location)) {
      if (!VikingBuildBlock.busy) {
        VikingBuildBlock.setBuildOrder(
            _mineBuildOrder, new MapLocation[] {_goalLocation});
        return true;
      }
    }
    return false;
  }
  
  /**
   * Makes sure that the goal mine is still buildable.
   */
  public static final void _checkGoalMine() {
    if (_goalLocation == null) { return; }
    if (!S.sensorController.canSenseSquare(_goalLocation)) { return; }
    if (_availableMine(_goalLocation)) { return; }
        
    // Pick a new goal mine.
    int minDistance = Integer.MAX_VALUE;
    int minIndex = -1;
    for (int i = _seenMinesTop - 1; i >= 0; i--) {
      final Mine mine = _seenMines[i];
      final MapLocation location = mine.getLocation();
      final int distance = location.distanceSquaredTo(S.location);
      if (distance < minDistance) {
        minDistance = distance;
        minIndex = i;
      }
    }
    if (minIndex == -1) {
      _goalMine = null;
      _goalLocation = null;
    } else {
      _goalMine = _seenMines[minIndex];
      _goalLocation = _goalMine.getLocation();
      _seenMines[minIndex] = _seenMines[--_seenMinesTop];
    }
  }
  
  /** Checks that we can build on top of a mine location. */
  public static final boolean _availableMine(MapLocation location) {
    try {
      final GameObject object = S.sensorController.senseObjectAtLocation(
          location, RobotLevel.ON_GROUND);
      if (object == null) { return true; }
      final RobotInfo info = S.sensorController.senseRobotInfo((Robot)object);
      return (info.chassis != Chassis.BUILDING);      
    } catch (GameActionException e) {
      // The round wrapped right when we were sensing, and the thing on top of
      // the mine died. So the mine should be available.
      D.debug_logException(e);
      return true;
    }
  }
  
  /**
   * Senses mines around, updates goal and memory.
   * @return true if the goal has changed
   */
  public static final boolean _checkForMines() {
    boolean newGoal = false;
    Mine[] mines = S.sensorController.senseNearbyGameObjects(Mine.class);
    mineLoop: for (int i = mines.length - 1; i >= 0; i--) {
      Mine mine = mines[i];
      MapLocation location = mine.getLocation();
      int mineID = mine.getID();
      boolean available = _availableMine(location);

      for (int j = _seenMinesTop - 1; j >= 0; j--) {
        Mine seenMine = _seenMines[j];
        if (mineID == seenMine.getID()) {
          if (!available) { _seenMines[j] = _seenMines[--_seenMinesTop]; }
          continue mineLoop;
        }        
      }
      if (_seenMinesTop == MINE_MEMORY_SIZE) {
        // NOTE: skipping mine because we expect we'll see it again when we come
        //       back from the mine we're currently chasing
        continue;
      }
      if (!available) { continue; }
      
      if (_goalLocation == null) {
        _goalLocation = location;
        _goalMine = mine;
        newGoal = true;
      } else {
        // NOTE: not replacing the goal all the time on purpose
        if (_goalLocation.distanceSquaredTo(S.location) >
            location.distanceSquaredTo(S.location) + 4) {
          _seenMines[_seenMinesTop++] = _goalMine;
          _goalMine = mine;
          _goalLocation = location;
          newGoal = true;
        } else {
          _seenMines[_seenMinesTop++] = mine;
        }
      }
    }
    return newGoal;
  }
  
  public static Role[] _mineBuildOrder = {Role.RECYCLER};
  /** The mine we're trying to hunt. */
  public static Mine _goalMine;
  /** The location of the mine we're trying to hunt. */
  public static MapLocation _goalLocation;
  /** The next free slot in {@link MineHuntingBlock#_seenMines}. */
  public static int _seenMinesTop;
  /** The mines that we've seen. */
  public static final Mine[] _seenMines = new Mine[MINE_MEMORY_SIZE];
}
