package geneticAlgorithm;

import MyUtil.U;

public class Genome implements Comparable<Genome> {
	public Blueprint b;
	public double[] genes;

	// used by algorithms
	public int score;

	public Genome(Blueprint b) {
		this.b = b;
		genes = b.initGenes.clone();
	}

	public Genome(Genome mom, Genome dad, double mutation) {
		U.Assert(mom.b == dad.b);
		U.Assert(mom.genes.length == dad.genes.length);
		b = mom.b;
		genes = new double[mom.genes.length];
		for (int i = 0; i < genes.length; i++) {
			if (U.r.nextDouble() < mutation) {
				double m = mom.genes[i];
				double d = dad.genes[i];
				double[] range = b.ranges.get(i);
				double a = 0;
				if (m == d) {
					a = U.lerp(0, range[0], 1, range[1], U.r.nextDouble());
				} else {
					a = (U.r.nextGaussian() * Math.abs(m - d) / 2.0)
							+ ((m + d) / 2.0);
					if (a < range[0])
						a = range[0];
					if (a > range[1])
						a = range[1];
				}
				genes[i] = a;
			} else if (U.r.nextBoolean()) {
				genes[i] = mom.genes[i];
			} else {
				genes[i] = dad.genes[i];
			}
		}
		normalize();
	}

	public void jiggle(double variancePercent) {
		for (int i = 0; i < genes.length; i++) {
			genes[i] = jiggle(genes[i], b.ranges.get(i), variancePercent);
		}
		normalize();
	}

	public int compareTo(Genome that) {
		return this.score - that.score;
	}

	public void debug_print() {
		System.out.print(b.printGeneticAlgorithmSection(genes));
	}

	public double jiggle(double a, double[] range, double variancePercent) {
		a += U.r.nextGaussian() * Math.abs(range[1] - range[0])
				* variancePercent;
		if (a < range[0])
			a = range[0];
		if (a > range[1])
			a = range[1];
		return a;
	}

	public void normalize() {
		int i = 0;
		for (int end : b.normalizeGroupEnds) {
			int begin = i;
			double sum = 0;
			for (; i < end; i++) {
				sum += genes[i];
			}
			if (sum <= Double.MIN_NORMAL) {
				for (i = begin; i < end; i++) {
					genes[i] = 1.0 / (end - begin);
				}
			} else {
				for (i = begin; i < end; i++) {
					genes[i] /= sum;
				}
			}
		}
	}
}
