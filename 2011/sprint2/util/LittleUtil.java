package sprint2.util;

import java.util.ArrayList;

import sprint2.core.B;
import sprint2.core.Callback;
import sprint2.core.CommandType;
import sprint2.core.S;
import sprint2.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.MineInfo;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class LittleUtil {

  enum Role {
    MEANINGLESS, MINER, FACTORY, EXPLORER, CONSTRUCTOR
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

  public static void bearStrategy() throws Exception {
    // find ourselves
    Role role = Role.MEANINGLESS;
    if (S.round < 2) {
      // beginning robots
      if (S.chassis == Chassis.BUILDING) {
        for (RobotInfo ri : S.senseRobots()) {
          if (ri.chassis == Chassis.LIGHT
              && !ri.location.directionTo(S.location).isDiagonal()) {
            role = Role.MINER;
            break;
          }
        }
      } else if (S.chassis == Chassis.LIGHT) {
        role = Role.CONSTRUCTOR;
      }
    } else {
      if (S.chassis == Chassis.BUILDING) {
        while (S.builderController == null)
          X.yield();
        if (S.builderController.type() == ComponentType.FACTORY) {
          role = Role.FACTORY;
        }
      } else if (S.chassis == Chassis.LIGHT) {
        while (true) {
          if (RobotUtil.hasComponents(ComponentType.TELESCOPE,
              ComponentType.ANTENNA)) {
            role = Role.EXPLORER;
            break;
          }
          X.yield();
        }
      }
    }
    waitForComponents();

    // ok, we know our role, so do what we must
    System.out.println("role: " + role);
    // ///////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////
    if (role == Role.CONSTRUCTOR) {
      MapLocation start = S.location;

      // find the mine opposite the miner
      MapLocation firstMine = null;
      for (Direction d : MapUtil.orthoDirs) {
        X.setDirection(d);
        MapLocation loc = MapUtil.add(S.location, S.direction, 2);
        MineInfo mi = S.senseMine(loc);
        if (mi != null) {
          firstMine = loc;
          break;
        }
      }

      // and go there
      BugUtil.bugTo(firstMine);

      // find empty mine
      MapLocation secondMine = null;
      for (Direction d : MapUtil.dirs) {
        X.setDirection(d);
        MineInfo mi = S.senseMine(S.front);
        if (mi != null) {
          if (S.canMove(d)) {
            secondMine = S.front;
            break;
          }
        }
      }

      // build a recycler
      buildBuilderBuilding(secondMine, ComponentType.RECYCLER);

      // move a random direction away
      MapUtil.moveRandomDirection();

      // build another recylcer
      buildBuilderBuilding(firstMine, ComponentType.RECYCLER);

      // move up to the start location, and build a factory there
      BugUtil.bugUpTo(start);
      if (S.location.equals(start)) {
        MapUtil.moveRandomDirection();
      }
      buildBuilderBuilding(start, ComponentType.FACTORY);

      // move away, so something else can be built
      MapUtil.moveRandomDirection();

      // wait for a message
      B.onMessageHandlers.add(new Callback() {
        @Override
        public void onMessage(Message m) {
          B.onMessageHandlers.remove(this);

          B.onMessageHandlers.add(new Callback() {
            @Override
            public void onMessage(Message m) {
              MapLocation[] mines = m.locations;
              System.out.println("mines!!!: " + mines.length);

              BugUtil.bugTo(mines[0]);
            }
          });

          MapLocation goHere = new MapLocation(m.ints[1], m.ints[2]);
          BugUtil.bugTo(goHere, 9);
        }
      });
      waitForever();

      // ///////////////////////////////////////////////////////////
      // ///////////////////////////////////////////////////////////
    } else if (role == Role.FACTORY) {

      // find place to build
      MapLocation buildHere = null;
      for (Direction d : MapUtil.orthoDirs) {
        MapLocation m = S.location.add(d);
        RobotInfo ri = S.senseRobot(m, RobotLevel.ON_GROUND);
        if (ri.chassis == Chassis.BUILDING) {
          buildHere = S.location.add(d.rotateLeft().rotateLeft());
          if (!S.rc.senseTerrainTile(buildHere).isTraversableAtHeight(
              RobotLevel.ON_GROUND))
            buildHere = S.location.add(d.rotateRight().rotateRight());
          break;
        }
      }

      // wait for the square to be free
      while (!X.canBuild(buildHere, RobotLevel.ON_GROUND))
        X.yield();

      // build a small chassis
      waitForFlux(Chassis.LIGHT.cost);
      X.build(Chassis.LIGHT, buildHere);

      // build a telescope in it
      waitForFlux(ComponentType.TELESCOPE.cost);
      X.build(ComponentType.TELESCOPE, buildHere, RobotLevel.ON_GROUND);

      // ///////////////////////////////////////////////////////////
      // ///////////////////////////////////////////////////////////
    } else if (role == Role.MINER) {

      // build an antenna in the explorer (the thing with the telescope)
      MapLocation m = null;
      l1 : while (true) {
        for (RobotInfo ri : S.senseRobots()) {
          if (LittleUtil.find(ri.components, ComponentType.TELESCOPE)) {
            m = ri.location;
            break l1;
          }
        }
        X.yield();
      }

      // build an antenna in it
      waitForFlux(ComponentType.ANTENNA.cost);

      X.build(ComponentType.ANTENNA, m, RobotLevel.ON_GROUND);

      // ///////////////////////////////////////////////////////////
      // ///////////////////////////////////////////////////////////
    } else if (role == Role.EXPLORER) {

      MapLocation goal = MapUtil.add(S.location, Direction.NORTH_WEST, 8);
      CommandType.GO_HERE.ints[1] = goal.x;
      CommandType.GO_HERE.ints[2] = goal.y;
      B.send(CommandType.GO_HERE.ints);

      BugUtil.bugTo(goal, 9);

      // look for mines
      ArrayList<MapLocation> mines = new ArrayList<MapLocation>();
      for (int i = 0; i < 8; i++) {
        for (MineInfo mi : S.senseMineInfos()) {
          MapLocation loc = mi.mine.getLocation();
          // make sure it is unoccupied
          if (S.senseRobot(loc, RobotLevel.ON_GROUND) == null)
            mines.add(loc);
        }
        X.setDirection(S.direction.rotateRight());
      }

      // make sure our guy is close by
      l1 : while (true) {
        for (RobotInfo ri : S.senseRobots()) {
          if (ri.robot.getTeam() == S.team
              && ri.chassis == Chassis.LIGHT
              && ri.location.distanceSquaredTo(S.location) <= ComponentType.ANTENNA.range) {
            break l1;
          }
        }
        X.setDirection(S.direction.rotateRight());
      }

      // send the mines to them
      B.send(CommandType.MINES.ints, mines.toArray(new MapLocation[0]));

      System.out.println("mines: " + mines);

    } else if (role == Role.MEANINGLESS) {
      // ///////////////////////////////////////////////////////////
      // ///////////////////////////////////////////////////////////

    }

    LittleUtil.waitForever();
  }

  public static void waitForComponents() throws Exception {
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

  public static void buildBuilderBuilding(MapLocation m, ComponentType type)
      throws Exception {
    waitForFlux(Chassis.BUILDING.cost);
    X.build(Chassis.BUILDING, m);
    waitForFlux(type.cost);
    X.build(type, m, RobotLevel.ON_GROUND);
  }

  // /////////////////////////////
  // /////////////////////////////

  public static MapLocation front() throws Exception {
    return S.location.add(S.direction);
  }

  public static void waitForever() throws Exception {
    while (true) {
      X.yield();
    }
  }

  public static boolean isAllieInFront(RobotLevel level) throws Exception {
    GameObject o = S.sensorController.senseObjectAtLocation(front(), level);
    if (o instanceof Robot) {
      return ((Robot) o).getTeam() == S.team;
    }
    return false;
  }

  public static void waitForFlux(float amount) throws Exception {
    while (S.flux < amount + 2) {
      X.yield();
    }
  }

  public static void buildAntennaInFront() throws Exception {
    // TODO(Greg): this is especially hacky, even for Greg
    try {
      X.build(ComponentType.ANTENNA, front(), RobotLevel.ON_GROUND);
    } catch (Exception e) {
    }
  }

  public static void buildAntennasOnAllNeighbors() throws Exception {
    for (int i = 0; i < 8; i++) {
      if (isAllieInFront(RobotLevel.ON_GROUND)) {
        waitForFlux(ComponentType.ANTENNA.cost);
        buildAntennaInFront();
      }
      X.setDirection(S.direction.rotateLeft());
    }
  }

  public static void waitForAntenna() throws Exception {
    while (B.bc == null) {
      X.yield();
    }
    while (B.bc.isActive()) {
      X.yield();
    }
  }
}
