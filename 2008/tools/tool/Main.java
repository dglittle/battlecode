package tool;

import java.io.FileWriter;
import java.io.PrintWriter;

import javax.swing.JFrame;

import MyUtil.U;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public class Main {
	public static String map = "                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $                                                                                           $";

	/*
	 * public static void doHealing() throws Exception { MapLocation here =
	 * rc.getLocation(); Team team = rc.getTeam(); boolean isArchon =
	 * rc.getRobotType() == RobotType.ARCHON; Vector<RobotInfo> adjacentFriends =
	 * new Vector(); Direction cursor = Direction.NORTH; for (int i = 0; i < 8;
	 * i++) { MapLocation spot = here.add(cursor); if (!rc.canSenseSquare(spot))
	 * continue; Robot r = rc.senseAirRobotAtLocation(spot); if (r != null) {
	 * RobotInfo info = rc.senseRobotInfo(r); if (info.team == team) {
	 * adjacentFriends.add(info); } } r = rc.senseGroundRobotAtLocation(spot);
	 * if (r != null) { RobotInfo info = rc.senseRobotInfo(r); if (info.team ==
	 * team) { adjacentFriends.add(info); } } cursor = cursor.rotateLeft(); } if
	 * (rc.getRobotType().isAirborne()) { Robot r =
	 * rc.senseGroundRobotAtLocation(here); if (r != null) { RobotInfo info =
	 * rc.senseRobotInfo(r); if (info.team == team) { adjacentFriends.add(info); } } }
	 * else { Robot r = rc.senseAirRobotAtLocation(here); if (r != null) {
	 * RobotInfo info = rc.senseRobotInfo(r); if (info.team == team) {
	 * adjacentFriends.add(info); } } } double greatestNeed = -Double.MAX_VALUE;
	 * RobotInfo mostInNeed = null; for (RobotInfo i : adjacentFriends) { double
	 * need = 101.0 - (100.0 * (i.energonLevel / i.maxEnergon)) -
	 * (i.energonReserve / GameConstants.ENERGON_RESERVE_SIZE); if (i.type ==
	 * RobotType.TOWER) { need *= willingness_to_give_to_tower; } if (need >
	 * greatestNeed) { greatestNeed = need; mostInNeed = i; } } if (mostInNeed !=
	 * null) { double energon = rc.getEnergonLevel(); double maxEnergon =
	 * rc.getMaxEnergonLevel(); double energonReserve = rc.getEnergonReserve();
	 * double upkeep = rc.getRobotType().energonUpkeep(); double myNeed = 101.0 -
	 * (100.0 * (energon / maxEnergon)) - (energonReserve /
	 * GameConstants.ENERGON_RESERVE_SIZE); double expectedIncrease = Math.min(
	 * GameConstants.ENERGON_TRANSFER_RATE, energonReserve) - upkeep + (isArchon ?
	 * GameConstants.ARCHON_PRODUCTION : 0.0); double willingToGive =
	 * Math.max(0.0, (energon + expectedIncrease) - maxEnergon);
	 * 
	 * double mostInNeedReserve = mostInNeed.energonReserve; if (greatestNeed >
	 * myNeed) { willingToGive = Math .max(willingToGive,
	 * GameConstants.ENERGON_TRANSFER_RATE - mostInNeedReserve); }
	 *  // be generous willingToGive *= generosity;
	 *  // hard limits willingToGive = Math.min(willingToGive,
	 * GameConstants.ENERGON_RESERVE_SIZE - mostInNeedReserve); willingToGive =
	 * Math.min(willingToGive, energon / 2.0);
	 * 
	 * if (willingToGive > 0) { rc.transferEnergon(willingToGive,
	 * mostInNeed.location, mostInNeed.type.isAirborne() ? RobotLevel.IN_AIR :
	 * RobotLevel.ON_GROUND); energon -= willingToGive; } } }
	 */

	public static boolean inRange(MapLocation a, int radius) {
		int distSq = a.getX() * a.getX() + a.getY() * a.getY();
		return distSq <= radius * radius;
	}

	public static void main(String[] args) throws Exception {

		{
			for (int i = 0; i < 8; i++) {
				Direction d = Direction.values()[i];
				System.out.println("{");

				for (int y = -10; y <= 10; y++) {
					for (int x = -10; x <= 10; x++) {
						MapLocation a = new MapLocation(x, y);
						MapLocation b = a.add(d);
//						if (inRange(a, RobotType.ARCHON.sensorRadius())
//								&& !inRange(b, RobotType.ARCHON.sensorRadius())) {
//							
//							//System.out.print("x");
						if (inRange(a, RobotType.ARCHON.sensorRadius())) {
							System.out.println("new MapLocation(" + a.getX() + "," + a.getY() + "), ");
							//System.out.print(".");
						} else {
							//System.out.print(" ");
						}
					}
					//System.out.println();
				}
				System.out.println("},");
			}
			System.exit(0);
		}

		{
			for (int i = 0; i < 100; i++) {
				for (int r2 = 0; r2 < 25; r2++) {
					int count = 0;
					for (int x = -5; x <= 5; x++) {
						for (int y = -5; y <= 5; y++) {
							if ((x * x) + (y * y) <= r2) {
								count++;
							}
						}
					}
					if (count >= i) {
						System.out.println(r2 + ", ");
						break;
					}
				}
			}

			System.exit(0);
		}

		// work here
		{
			for (int i = 0; i < 18; i++) {
				System.out.println(U.r.nextInt() + ", ");
			}
			// for (int ii = 0; ii < 1000; ii++) {
			// Set<Integer> is = new HashSet();
			// for (int i = 0; i < 8000; i++ ) {
			// if (!is.add(U.r.nextInt())) {
			// System.out.println("wtf? ii = " + ii);
			// System.exit(0);
			// }
			// }
			// }
			// System.out.println("alright!");
			System.exit(0);
		}

		// work here
		{
			System.out.println("got here?");
			U.profile("all");
			PrintWriter out = new PrintWriter(new FileWriter("temp.txt"), true);
			for (int i = 0; i < 1000000; i++) {
				out.print('a');
			}
			out.close();
			U.profile("all");
			U.profilePrint();
			System.exit(0);
		}

		String blankLine = "";
		for (int i = 0; i < 95; i++) {
			blankLine += " ";
		}
		String it = "";
		for (int i = 0; i < 95; i++) {
			it += blankLine + "\\n";
		}
		System.out.println("\"" + it + "\"");

		System.out.println("length = " + map.length());

		System.exit(0);

		JFrame f = new JFrame();
		MyUtil.U.exitOnClose(f);

		MapStateDisplayPanel p = new MapStateDisplayPanel();
		f.getContentPane().add(p);

		MapState s = new MapState(10, 10);
		s.bottom[3][5] = 1;
		p.mapState = s;
		s.middle[4][5] = 1;
		s.middle[5][5] = 11;
		s.middle[6][6] = 12;

		f.setSize(600, 400);
		f.setVisible(true);
	}
}
