package sprint2.core;

import java.util.ArrayList;

import battlecode.common.BroadcastController;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class B {
  public static BroadcastController bc;
  public static ArrayList<Callback> onMessageHandlers = new ArrayList<Callback>();

  public static int longestMessage = 0;

  public static final void init() {
    for (CommandType ct : CommandType.values()) {
      if (ct.ints.length > longestMessage)
        longestMessage = ct.ints.length;
    }
  }

  public static final int MAGIC_1 = 797003437;
  public static final int MAGIC_2 = 899809343;

  // skips element 0
  public static final int hashMessage(int[] m) {
    int a = MAGIC_1;
    for (int i = 1; i < m.length; i++) {
      a = (a ^ m[i]) * MAGIC_2;
    }
    return a;
  }

  public static Message intsMessage = new Message();

  public static final void send(int[] ints) throws Exception {
    ints[0] = hashMessage(ints);
    intsMessage.ints = ints;
    bc.broadcast(intsMessage);
  }

  public static Message intsAndMapsMessage = new Message();

  public static final void send(int[] ints, MapLocation[] locs)
      throws Exception {
    ints[0] = hashMessage(ints);
    intsAndMapsMessage.ints = ints;
    intsAndMapsMessage.locations = locs;
    bc.broadcast(intsAndMapsMessage);
  }

  public static final void addOnMessageHandler(Callback cb) {
    onMessageHandlers.add(cb);
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
      if (ints[0] != hashMessage(ints))
        continue;

      for (int i = onMessageHandlers.size() - 1; i >= 0; i--)
        onMessageHandlers.get(i).onMessage(m);
    }
  }
}
