package heavy1.chaos.little;

import heavy1.blocks.DefensiveMineBlock;
import heavy1.blocks.StupidPathfinding;
import heavy1.core.B;
import heavy1.core.CommandType;
import heavy1.core.D;
import heavy1.core.S;
import heavy1.core.U;
import heavy1.core.X;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;
import battlecode.common.WeaponController;

public class FleetUtil {

  enum Role {
    CONSTRUCTOR, ARMORY, MINER
  };

  public static void go() throws Exception {
    // who am I?
    Role role = null;
    if (S.chassis == Chassis.LIGHT) {
      role = Role.CONSTRUCTOR;
    } else if (S.chassis == Chassis.BUILDING) {
      while (true) {
        if (U.hasComponents(ComponentType.ARMORY)) {
          role = Role.ARMORY;
          break;
        }
        if (U.hasComponents(ComponentType.RECYCLER)) {
          if (S.birthRound > 5) {
            DefensiveMineBlock.setDefenseParameters(1, ComponentType.RADAR);
            DefensiveMineBlock.sync();
            U.waitForever();
          }

          RobotInfo ri = S.senseRobotInfo(S.location.add(Direction.SOUTH),
              RobotLevel.ON_GROUND);
          if (ri != null && ri.components != null
              && U.find(ri.components, ComponentType.ARMORY)) {
            role = Role.MINER;
            break;
          }
        }
        X.yield();
      }
    } else if (S.chassis == Chassis.FLYING) {
      while (true) {
        if (U.hasComponentsReady(ComponentType.RADAR, ComponentType.ANTENNA)) {
          while (S.movementController.isActive())
            X.yield();

          X.setDirection(Direction.SOUTH_EAST);

          int blaster1Id = 0;
          int blaster2Id = 0;
          int constId = 0;

          while (true) {
            RobotInfo ri = S.senseRobotInfo(S.location.add(Direction.EAST),
                RobotLevel.IN_AIR);
            if (ri == null) {
              X.yield();
              continue;
            }
            Direction old = ri.direction;
            while (true) {
              ri = S.senseRobotInfo(S.location.add(Direction.EAST),
                  RobotLevel.IN_AIR);
              if (ri.direction == old) {
                X.yield();
                continue;
              }
              break;
            }
            CommandType.FLEET_INIT.ints[2] = blaster1Id = ri.robot.getID();
            CommandType.FLEET_INIT.ints[3] = 1;
            CommandType.FLEET_INIT.ints[4] = S.id;
            B.send(CommandType.FLEET_INIT.ints);
            X.yield();
            break;
          }

          while (true) {
            RobotInfo ri = S.senseRobotInfo(S.location.add(Direction.SOUTH_EAST),
                RobotLevel.IN_AIR);
            if (ri == null) {
              X.yield();
              continue;
            }
            Direction old = ri.direction;
            while (true) {
              ri = S.senseRobotInfo(S.location.add(Direction.SOUTH_EAST),
                  RobotLevel.IN_AIR);
              if (ri.direction == old) {
                X.yield();
                continue;
              }
              break;
            }
            CommandType.FLEET_INIT.ints[2] = constId = ri.robot.getID();
            CommandType.FLEET_INIT.ints[3] = 2;
            CommandType.FLEET_INIT.ints[4] = S.id;
            B.send(CommandType.FLEET_INIT.ints);
            X.yield();
            break;
          }
          while (true) {
            RobotInfo ri = S.senseRobotInfo(S.location.add(Direction.SOUTH),
                RobotLevel.IN_AIR);
            if (ri == null) {
              X.yield();
              continue;
            }
            Direction old = ri.direction;
            while (true) {
              ri = S.senseRobotInfo(S.location.add(Direction.SOUTH),
                  RobotLevel.IN_AIR);
              if (ri.direction == old) {
                X.yield();
                continue;
              }
              break;
            }
            CommandType.FLEET_INIT.ints[2] = blaster2Id = ri.robot.getID();
            CommandType.FLEET_INIT.ints[3] = 3;
            CommandType.FLEET_INIT.ints[4] = S.id;
            B.send(CommandType.FLEET_INIT.ints);
            X.yield();
            break;
          }

          fleetLeader(S.id, blaster1Id, blaster2Id, constId);
        }
        if (U.hasComponentsReady(ComponentType.BLASTER, ComponentType.BLASTER)
            || U.hasComponentsReady(ComponentType.CONSTRUCTOR)) {
//          B.onMessageHandlers.add(new Callback() {
//            @Override
//            public void onMessage(Message m) {
//              try {
//                if (m.ints[1] == CommandType.FLEET_INIT.ordinal()) {
//                  if (S.id == m.ints[2]) {
//                    if (m.ints[3] == 1) {
//                      fleetBlaster(m.ints[4], 1, 0);
//                    } else if (m.ints[3] == 2) {
//                      fleetConstructor(m.ints[4]);
//                    } else if (m.ints[3] == 3) {
//                      fleetBlaster(m.ints[4], 0, 1);
//                    }
//                  }
//                }
//              } catch (Exception e) {
//                D.debug_logException(e);
//              }
//            }
//          });
          while (S.movementController.isActive())
            X.yield();
          while (true) {
            X.setDirection(S.direction.opposite());
          }
        }
        X.yield();
      }

    } else {
      U.waitForever();
    }

    // let's do stuff
    System.out.println("I am a: " + role);
    if (role == Role.CONSTRUCTOR) {
      // build an armory to the right
      MapLocation there = S.location.add(Direction.EAST);
      U.waitForFlux(Chassis.BUILDING.cost);
      S.builderController.build(Chassis.BUILDING, there);
      X.yield();
      U.waitForFlux(ComponentType.ARMORY.cost);
      S.builderController.build(ComponentType.ARMORY, there,
          RobotLevel.ON_GROUND);
      X.yield();
      S.rc.suicide();
    } else if (role == Role.ARMORY) {
      while (S.builderController.isActive())
        X.yield();

      int[][] offsets = new int[][]{{-1, -1}, {0, -1}, {-1, 0}, {0, 0}};
      // int[][] offsets = new int[][]{{-1, -1}, {-1, 0}, {0, -1}, {0, 0}};
      for (int i = 0; i < offsets.length; i++) {
        int[] offset = offsets[i];
        MapLocation there = new MapLocation(S.locationX + offset[0],
            S.locationY + offset[1]);

        U.waitForFlux(Chassis.FLYING.cost);
        S.builderController.build(Chassis.FLYING, there);
        X.yield();
      }
    } else if (role == Role.MINER) {
      {
        MapLocation there = S.location.add(Direction.WEST);
        while (true) {
          RobotInfo ri = S.senseRobotInfo(there, RobotLevel.IN_AIR);
          if (ri != null)
            break;
        }
        U.waitForFlux(ComponentType.RADAR.cost);
        S.builderController.build(ComponentType.RADAR, there, RobotLevel.IN_AIR);
        X.yield();
        U.waitForFlux(ComponentType.ANTENNA.cost);
        S.builderController.build(ComponentType.ANTENNA, there,
            RobotLevel.IN_AIR);
        X.yield();
      }
      {
        MapLocation there = S.location.add(Direction.SOUTH_WEST);
        while (true) {
          RobotInfo ri = S.senseRobotInfo(there, RobotLevel.IN_AIR);
          if (ri != null)
            break;
        }
        U.waitForFlux(ComponentType.BLASTER.cost);
        S.builderController.build(ComponentType.BLASTER, there,
            RobotLevel.IN_AIR);
        X.yield();
        U.waitForFlux(ComponentType.BLASTER.cost);
        S.builderController.build(ComponentType.BLASTER, there,
            RobotLevel.IN_AIR);
        X.yield();
      }
      {
        MapLocation there = S.location;
        while (true) {
          RobotInfo ri = S.senseRobotInfo(there, RobotLevel.IN_AIR);
          if (ri != null)
            break;
        }
        U.waitForFlux(ComponentType.BLASTER.cost);
        S.builderController.build(ComponentType.BLASTER, there,
            RobotLevel.IN_AIR);
        X.yield();
        U.waitForFlux(ComponentType.BLASTER.cost);
        S.builderController.build(ComponentType.BLASTER, there,
            RobotLevel.IN_AIR);
        X.yield();
      }
      {
        MapLocation there = S.location.add(Direction.SOUTH);
        while (true) {
          RobotInfo ri = S.senseRobotInfo(there, RobotLevel.IN_AIR);
          if (ri != null)
            break;
        }
        U.waitForFlux(ComponentType.CONSTRUCTOR.cost);
        S.builderController.build(ComponentType.CONSTRUCTOR, there,
            RobotLevel.IN_AIR);
        X.yield();
      }
    }

    U.waitForever();
  }

  public static MapLocation fleetGoal = null;
  public static MapLocation fleetTarget = null;
  public static RobotLevel fleetTargetLevel = null;
  public static MapLocation fleetEmptyMine = null;
  public static int lastMessageFromBoss = 0;

  public static void fleetLeader(int fleetChannel, int blaster1Id,
      int blaster2Id, int constId) throws Exception {
    D.debug_setIndicator(0, "leader " + fleetChannel);

    Direction generalDir = U.directions[S.rand.nextInt(8)];
    int generalDirBirth = S.round;
    final int weHaveAConstructor_Rounds = 20;
    int weHaveAConstructor = weHaveAConstructor_Rounds;

    while (true) {
      // gather information for message
      fleetGoal = null;
      fleetTarget = null;
      fleetEmptyMine = null;

      // is there a mine?
      Mine bestMine = null;
      int bestDist = Integer.MAX_VALUE;
      boolean bestEmpty = false;
      for (Mine m : S.senseMines()) {
        RobotInfo ri = S.senseRobotInfo(m.getLocation(), RobotLevel.ON_GROUND);
        if ((weHaveAConstructor > 0 && ri == null)
            || (ri != null && ri.robot.getTeam() == S.enemyTeam)) {
          int dist = S.location.distanceSquaredTo(m.getLocation());
          if (dist < bestDist) {
            bestDist = dist;
            bestMine = m;
            bestEmpty = (ri == null);
          }
        }
      }
      if (bestMine != null) {
        fleetGoal = bestMine.getLocation();
        if (bestEmpty)
          fleetEmptyMine = bestMine.getLocation();
      }

      // let's progress forward
      if (fleetGoal == null) {
        // ...make sure we are not heading into a wall
        while (true) {
          MapLocation outFront = S.location.add(generalDir, 4);
          TerrainTile tt = S.rc.senseTerrainTile(outFront);
          if (tt == TerrainTile.OFF_MAP || S.round > generalDirBirth + 100) {
            generalDir = U.directions[S.rand.nextInt(8)];
            generalDirBirth = S.round;
            if (!S.movementController.isActive())
              X.setDirection(generalDir);
            else
              X.yield();
          } else {
            break;
          }
        }

        fleetGoal = S.location.add(generalDir);
      }

      // is there stuff to attack
      {
        RobotInfo bestRI = null;
        double bestHitpoints = Double.MAX_VALUE;
        for (RobotInfo ri : S.nearbyRobotInfos()) {
          if (ri.robot.getTeam() == S.enemyTeam) {
            if (ri.chassis != Chassis.MEDIUM) {
              if (ri.hitpoints < bestHitpoints) {
                bestHitpoints = ri.hitpoints;
                bestRI = ri;
              }
            }
          }
        }
        if (bestRI != null) {
          fleetTarget = bestRI.location;
          fleetTargetLevel = bestRI.robot.getRobotLevel();
        }
      }

      // send a message
      CommandType.FLEET_MAIN_MESSAGE.ints[10] = fleetChannel;

      CommandType.FLEET_MAIN_MESSAGE.ints[2] = fleetGoal.x;
      CommandType.FLEET_MAIN_MESSAGE.ints[3] = fleetGoal.y;

      if (fleetTarget != null) {
        CommandType.FLEET_MAIN_MESSAGE.ints[4] = fleetTargetLevel.ordinal();
        CommandType.FLEET_MAIN_MESSAGE.ints[5] = fleetTarget.x;
        CommandType.FLEET_MAIN_MESSAGE.ints[6] = fleetTarget.y;
      } else {
        CommandType.FLEET_MAIN_MESSAGE.ints[4] = -1;
      }

      if (fleetEmptyMine != null) {
        CommandType.FLEET_MAIN_MESSAGE.ints[7] = 1;
        CommandType.FLEET_MAIN_MESSAGE.ints[8] = fleetEmptyMine.x;
        CommandType.FLEET_MAIN_MESSAGE.ints[9] = fleetEmptyMine.y;
      } else {
        CommandType.FLEET_MAIN_MESSAGE.ints[7] = -1;
      }
      B.send(CommandType.FLEET_MAIN_MESSAGE.ints);

      // check to make sure the constructor is with us
      if (fleetEmptyMine != null && S.location.equals(fleetEmptyMine)) {
        MapLocation con = S.location.add(Direction.SOUTH_EAST);
        if (!S.sensorController.canSenseSquare(con)) {
          if (!S.movementController.isActive()) {
            X.setDirection(Direction.SOUTH_EAST);
            continue;
          }
        } else {
          boolean found = false;
          for (RobotInfo ri : S.nearbyRobotInfos()) {
            if (ri.robot.getID() == constId) {
              found = true;
              break;
            }
          }
          if (!found) {
            weHaveAConstructor--;
          } else {
            weHaveAConstructor = weHaveAConstructor_Rounds;
          }
        }
      }

      // fly toward the goal
      if (!S.movementController.isActive()) {
        Direction d = StupidPathfinding.bugToAsync(fleetGoal);
        if (d.ordinal() < 8) {
          if (S.direction != d) {
            X.setDirection(d);
          } else {
            X.moveForward();
          }
        } else {
          X.yield();
        }
      } else {
        X.yield();
      }
    }
  }

  public static void fleetBlaster(final int fleetChannel, final int offsetX,
      final int offsetY) throws Exception {
    D.debug_setIndicator(0, "blaster" + offsetX + " " + fleetChannel);
    fleetGoal = S.location;

//    B.onMessageHandlers.add(new Callback() {
//      @Override
//      public void onMessage(Message m) {
//        try {
//          int[] ints = m.ints;
//          if (ints[1] == CommandType.FLEET_MAIN_MESSAGE.ordinal()
//              && ints[10] == fleetChannel) {
//            fleetGoal = new MapLocation(ints[2] + offsetX, ints[3] + offsetY);
//            lastMessageFromBoss = S.round;
//
//            if (ints[4] >= 0) {
//              fleetTarget = new MapLocation(ints[5], ints[6]);
//              fleetTargetLevel = RobotLevel.values()[ints[4]];
//            } else {
//              fleetTarget = null;
//            }
//          }
//        } catch (Exception e) {
//          D.debug_logException(e);
//        }
//      }
//    });

    lastMessageFromBoss = S.round;
    while (true) {
      // have we heard from the boss in a while?
      if (lastMessageFromBoss < S.round - 20)
        S.rc.turnOff();

      // see if we can shoot anything
      if (fleetTarget != null) {
        for (WeaponController wc : S.weaponControllers) {
          if (!wc.isActive()) {
            if (wc.withinRange(fleetTarget)) {
              wc.attackSquare(fleetTarget, fleetTargetLevel);
            }
          }
        }
      }

      // see if we can progress toward the goal
      if (!S.movementController.isActive()) {
        Direction d = StupidPathfinding.bugToAsync(fleetGoal);
        if (d.ordinal() < 8) {
          if (S.direction != d) {
            X.setDirection(d);
          } else {
            X.moveForward();
          }
        } else {
          X.yield();
        }
      } else {
        X.yield();
      }
    }
  }

  public static void fleetConstructor(final int fleetChannel) throws Exception {
    D.debug_setIndicator(0, "const " + fleetChannel);
    fleetGoal = S.location;

//    B.onMessageHandlers.add(new Callback() {
//      @Override
//      public void onMessage(Message m) {
//        try {
//          int[] ints = m.ints;
//          if (ints[1] == CommandType.FLEET_MAIN_MESSAGE.ordinal()
//              && ints[10] == fleetChannel) {
//            fleetGoal = new MapLocation(ints[2] + 1, ints[3] + 1);
//            lastMessageFromBoss = S.round;
//
//            if (ints[7] >= 0) {
//              fleetEmptyMine = new MapLocation(ints[8], ints[9]);
//            } else {
//              fleetEmptyMine = null;
//            }
//          }
//        } catch (Exception e) {
//          D.debug_logException(e);
//        }
//      }
//    });

    lastMessageFromBoss = S.round;
    while (true) {
      // have we heard from the boss in a while?
      if (lastMessageFromBoss < S.round - 20) {
        S.rc.turnOff();
      }

      try {
        // see if we can build
        if (S.location.equals(fleetGoal)) {
          if (fleetEmptyMine != null && fleetEmptyMine.isAdjacentTo(S.location)) {
            if (S.allowedToBuildMine(Chassis.BUILDING, ComponentType.RECYCLER)) {
              MapLocation saveLoc = fleetEmptyMine;
              S.builderController.build(Chassis.BUILDING, saveLoc);
              X.yield();
              S.builderController.build(ComponentType.RECYCLER, saveLoc, RobotLevel.ON_GROUND);
              X.yield();
            }
          }
        }

        // see if we can progress toward the goal
        if (!S.movementController.isActive()) {
          Direction d = StupidPathfinding.bugToAsync(fleetGoal);
          if (d.ordinal() < 8) {
            if (S.direction != d) {
              X.setDirection(d);
            } else {
              X.moveForward();
            }
          } else {
            X.yield();
          }
        } else {
          X.yield();
        }
      } catch (Exception e) {
        D.debug_logException(e);
        X.yield();
      }
    }
  }
}
