/**
 * Thanks to Stephen G., ssg729.
 */
package sprint2.core;

public enum CommandType {
  // Commands
  STUPID(2),
  ATTACK_TARGET(3),
  MINE(2),
  BUILD(5),
  BUILD_LOC(2),
  MINES(2),
  GO_HERE(3),
  FLEET_INIT(3),
  FLEET_GO(3),
  FLEET_BUILD_MINE(3),
  FLEET_ATTACK(4),
  FLEET_MAIN_MESSAGE(9),
  FLEET_ID(1),
  ARMORY_LEEDER(2);
  

  public final int[] ints;

  CommandType(int commandLength) {
    ints = new int[commandLength + 2];
    ints[1] = ordinal();
  }
}
