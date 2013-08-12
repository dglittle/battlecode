package geneticAlgorithm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Collections;
import java.util.Vector;

import MyUtil.U;

public class Utils {

	public static String rootDir = "C:\\Home\\BattleCode2008";

	public static void sort(Vector<Genome> pop, String[] maps) throws Exception {
		if (pop.size() <= 1)
			return;
		for (Genome g : pop) {
			g.score = 0;
		}
		for (int i = 0; i < pop.size(); i++) {
			if (pop.size() == 2 && i > 0)
				break;
			Genome a = pop.get(i);
			Genome b = pop.get((i + 1) % pop.size());

			// work here
			System.out.print(".");

			Genome winner = getWinner(a, b, maps[U.r.nextInt(maps.length)]);
			winner.score += 1;
		}
		Vector<Genome> low = new Vector();
		Vector<Genome> med = new Vector();
		Vector<Genome> high = new Vector();
		for (Genome g : pop) {
			if (g.score == 0)
				low.add(g);
			else if (g.score == 1)
				med.add(g);
			else
				high.add(g);
		}
		if (Math.signum(low.size()) + Math.signum(med.size())
				+ Math.signum(high.size()) < 1.1) {
			return;
		}
		pop.clear();
		sort(low, maps);
		sort(med, maps);
		sort(high, maps);
		pop.addAll(high);
		pop.addAll(med);
		pop.addAll(low);
	}

	public static Genome getWinner(Genome a, Genome b, String map)
			throws Exception {

		// work here
		// if (true) {
		// if (U.sum(a.genes) > U.sum(b.genes)) {
		// return a;
		// } else {
		// return b;
		// }
		// }

		createTeamForGenome(a, "teamA");
		createTeamForGenome(b, "teamB");
		writeBcConf("teamA", "teamB", map);

		class MyBuffer extends Thread {
			BufferedInputStream in;
			StringBuffer buf;

			public MyBuffer(InputStream inputStream) {
				in = new BufferedInputStream(inputStream);
				buf = new StringBuffer();
				start();
			}

			public void run() {
				try {
					int c;
					while (!interrupted() && (c = in.read()) >= 0) {
						buf.append((char) c);
					}
				} catch (InterruptedIOException ex) {
					// this is fine, we just stop
				} catch (Exception ex) {
					ex.printStackTrace();
					System.exit(1);
				}
			}
		}

		Process p = Runtime.getRuntime().exec(rootDir + "\\run-headless.bat");
		MyBuffer inputBuf = new MyBuffer(p.getInputStream());
		MyBuffer errorBuf = new MyBuffer(p.getErrorStream());
		p.waitFor();
		String results = inputBuf.buf.toString();
		if (U.match("\\[server\\].*?\\(([AB])\\) wins", results)) {
			if (U.m.group(1).equals("A")) {
				return a;
			} else if (U.m.group(1).equals("B")) {
				return b;
			}
		}
		return null;
	}

	public static void writeBcConf(String teamA, String teamB, String map)
			throws Exception {
		File f = new File(rootDir + "\\bc.conf");
		U.saveString(f, U.slurp(f).replaceAll("bc\\.game\\.maps\\s*=\\s*.*",
				"bc.game.maps=" + map).replaceAll(
				"bc\\.game\\.team-a\\s*=\\s*.*", "bc.game.team-a=" + teamA)
				.replaceAll("bc\\.game\\.team-b\\s*=\\s*.*",
						"bc.game.team-b=" + teamB));
	}

	public static void createTeamForGenome(Genome g, String team)
			throws Exception {
		g.b.createTeam(g.genes, team);
	}
}
