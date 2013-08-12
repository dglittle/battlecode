package geneticAlgorithm;

import java.util.Collections;
import java.util.Vector;

import MyUtil.U;

public class Main {
	public static void main(String[] args) throws Exception {

		int popSize = 10;
		double popSelectionVariance = 3;
		double jiggle = 0.2;
		int numGenerations = 10;

		Blueprint b = new Blueprint("tribe");
		Vector<Genome> pop = new Vector();
		for (int i = 0; i < popSize; i++) {
			Genome g = new Genome(b);
			g.jiggle(jiggle);
			pop.add(g);
		}

		String[] maps = new String[] { "arena", "attrition", "bowl", "box",
				"peninsula", "ripple" };
		Utils.sort(pop, maps);
		for (int i = 0; i < numGenerations; i++) {
			Vector<Genome> nextGen = new Vector();
			for (int ii = 0; ii < popSize; ii++) {
				Genome mom = null;
				Genome dad = null;
				do {
					mom = pop.get((int) Math.abs(Math.round(U.r.nextGaussian()
							* popSelectionVariance))
							% pop.size());
					dad = pop.get((int) Math.abs(Math.round(U.r.nextGaussian()
							* popSelectionVariance))
							% pop.size());
				} while (mom == dad);
				nextGen.add(new Genome(mom, dad, 0.2));
			}
			nextGen.addAll(pop);
			Collections.shuffle(nextGen);
			Utils.sort(nextGen, maps);

			Vector<Genome> temp = new Vector();
			int count = 0;
			for (int ii = 0; ii < popSize; ii++) {
				Genome g = nextGen.get(ii);
				if (pop.contains(g)) {
					count++;
				}
				temp.add(g);
			}
			System.out.println("\n[" + i + "] survived: " + count);
			pop = temp;
		}

		Utils.createTeamForGenome(pop.get(0), "teamC");
	}
}
