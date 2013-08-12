package heavy1.core;

import heavy1.blocks.BuildBlock;
import heavy1.chaos.pwnall.VikingBuildBlock;
import battlecode.common.BroadcastController;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class B {
  public static BroadcastController bc;

  public static int longestMessage = 0;

  public static final void init() {
    for (CommandType ct : CommandType.values()) {
      if (ct.ints.length > longestMessage)
        longestMessage = ct.ints.length;
    }
  }

  public static final int MAGIC_1 = 797003437;
  public static final int MAGIC_2 = 899809343;

  // calculates hash -- ignores last element
  public static final int hashMessage(int[] m) {
    int a = MAGIC_1;
    for (int i = m.length - 2; i >= 0; i--) {
      a = (a ^ m[i]) * MAGIC_2;
    }
    return a;
  }

  public static Message intsMessage = new Message();

  public static final void send(int[] ints) throws GameActionException {
    ints[ints.length - 1] = hashMessage(ints);
    intsMessage.ints = ints;
    bc.broadcast(intsMessage);
  }

  public static final void checkMessages() {
    for (Message m : S.rc.getAllMessages()) {
      // make sure this message is one of ours
      int[] ints = m.ints;
      if (ints == null)
        continue;
      if (ints.length < 2)
        continue;
      if (ints.length > longestMessage)
        continue;
      if (ints[ints.length - 1] != hashMessage(ints))
        continue;

      switch (CommandType.values()[ints[ints.length - 2]]) {
        case BUILD_ORDER:
          if (ints[0] == S.id) {
            final int offset = 2;
            final int numOrders = ints[1];
            Role[] roles = new Role[numOrders];
            MapLocation[] locs = new MapLocation[numOrders];
            for (int i = 0; i < numOrders; i++) {
              roles[i] = Role.values()[ints[i * 3 + offset]];
              locs[i] = new MapLocation(ints[i * 3 + offset + 1], ints[i * 3
                  + offset + 2]);
            }
            if (!VikingBuildBlock.busy)
              VikingBuildBlock.setBuildOrder(roles, locs);
          }
          break;
        case EQUIP:
          BuildBlock._chassisIndex = ints[0];
          BuildBlock._chassisLocation = new MapLocation(ints[1], ints[2]);
          break;
      }
    }
  }
}
