package sprint4.core;

import sprint4.blocks.BuildBlock;
import sprint4.core.xconst.XRoleType;
import battlecode.common.BroadcastController;
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
    for (int i = 0; i < m.length - 1; i++) {
      a = (a ^ m[i]) * MAGIC_2;
    }
    return a;
  }

  public static Message intsMessage = new Message();

  public static final void send(int[] ints) throws Exception {
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
        case BUILD:
          if (ints[0] >= 0)
            BuildBlock._armoryLocation = new MapLocation(ints[0], ints[1]);
          if (ints[2] >= 0)
            BuildBlock._factoryLocation = new MapLocation(ints[2], ints[3]);
          if (ints[4] == S.locationX && ints[5] == S.locationY)
            S.role = XRoleType.HIVE_CONSTRUCTOR;
          break;
        case EQUIP:
          BuildBlock._chassisIndex = ints[0];
          BuildBlock._chassisLocation = new MapLocation(ints[1], ints[2]);
          break;
      }
    }
  }
}
