package geneticAlgorithm;

import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import MyUtil.U;

public class Blueprint {
	public String teamName;
	public double[] initGenes;
	public Vector<double[]> ranges;
	public Vector<Integer> normalizeGroupEnds;
	public Vector<String> names;
	public String fileString_before;
	public String fileString_after;

	public Blueprint(String teamName) throws Exception {
		this.teamName = teamName;
		String gaSection = getGeneticAlgorithmSection(getPlayerFileString());
		gaSection = gaSection.replaceAll("//", "");

		Vector<Double> initGenesVector = new Vector();
		ranges = new Vector();
		normalizeGroupEnds = new Vector();
		names = new Vector();
		{
			Matcher m = Pattern
					.compile(
							"(?s)public static double\\[\\]\\s*(\\w+)\\s*=\\s*new\\s*double\\[\\]\\s*\\{(.+?)\\}")
					.matcher(gaSection);
			while (m.find()) {
				names.add(m.group(1));
				for (String e : m.group(2).split(",")) {
					initGenesVector.add(Double.parseDouble(e));
					ranges.add(new double[] { 0.0, 1.0 });
				}
				normalizeGroupEnds.add(initGenesVector.size());
			}
		}
		{
			Matcher m = Pattern
					.compile(
							"public static double \\s*(\\w+)\\s*=\\s*(.+?);\\s*\\[\\s*(.+?),\\s*(.+?)\\]")
					.matcher(gaSection);
			while (m.find()) {
				names.add(m.group(1));
				initGenesVector.add(Double.parseDouble(m.group(2)));
				ranges.add(new double[] { Double.parseDouble(m.group(3)),
						Double.parseDouble(m.group(4)) });
			}
		}
		initGenes = new double[initGenesVector.size()];
		for (int i = 0; i < initGenesVector.size(); i++) {
			initGenes[i] = initGenesVector.get(i);
		}
	}

	public void createTeam(double[] genes, String newTeam)
			throws Exception {
		File srcDir = new File(Utils.rootDir + "\\teams\\" + teamName);
		File destDir = new File(Utils.rootDir + "\\teams\\" + newTeam);
		destDir.mkdirs();
		for (File f : srcDir.listFiles()) {
			if (!f.getName().equals("RobotPlayer.java")) {
				U.saveString(new File(destDir.getPath() + "\\" + f.getName()),
						U.slurp(f).replaceFirst("package .*?;",
								"package " + newTeam + ";"));
			}
		}

		PrintWriter out = new PrintWriter(new File(Utils.rootDir + "\\teams\\"
				+ newTeam + "\\RobotPlayer.java"));
		out.print(fileString_before.replaceFirst("package .*?;", "package "
				+ newTeam + ";"));
		out.print(printGeneticAlgorithmSection(genes));
		out.print(fileString_after);
		out.close();
	}
	
	public void debug_print() {
		System.out.print(printGeneticAlgorithmSection(initGenes));
	}

	public String printGeneticAlgorithmSection(double[] genes) {
		StringBuffer buf = new StringBuffer();
		buf.append("<<< Genetic Algorithm begin\n");
		int cursor = 0;
		int nameCursor = 0;
		for (int end : normalizeGroupEnds) {
			buf.append("\tpublic static double[] " + names.get(nameCursor++)
					+ " = new double[] {");
			boolean first = true;
			for (; cursor < end; cursor++) {
				if (!first) {
					buf.append(", ");
				}
				buf.append(genes[cursor]);
				first = false;
			}
			buf.append("};\n");
		}
		for (; cursor < genes.length; cursor++) {
			double[] range = ranges.get(cursor);
			buf.append("\tpublic static double " + names.get(nameCursor++)
					+ " = " + genes[cursor] + "; // [" + range[0] + ", "
					+ range[1] + "]\n");
		}
		buf.append("\t// Genetic Algorithm end >>>");
		return buf.toString();
	}

	public File getPlayerFile() throws Exception {
		return new File(Utils.rootDir + "\\teams\\" + teamName
				+ "\\RobotPlayer.java");
	}

	public String getPlayerFileString() throws Exception {
		return U.slurp(getPlayerFile());
	}

	public String getGeneticAlgorithmSection(String fileString)
			throws Exception {
		Matcher m = Pattern.compile(
				"(?s)<<< Genetic Algorithm begin.*?Genetic Algorithm end >>>")
				.matcher(fileString);
		if (!m.find()) {
			throw new IllegalArgumentException("no genetic algorithm section");
		}
		fileString_before = fileString.substring(0, m.start());
		fileString_after = fileString.substring(m.end());
		return m.group();
	}

	// // <<< Genetic Algorithm begin
	// public static double[] desired_unit_percent = new double[] { 0.1, 0.1,
	// 0.1,
	// 0.1, 0.5, 0.1 };
	// public static double energon_to_consider_spawning_scout = 30; // [0, 50]
	// public static double energon_to_consider_spawning_soldier = 30; // [0,
	// 50]
	// public static double energon_to_consider_spawning_bomber = 6; // [0, 10]
	// public static double energon_to_consider_spawning_mortar = 6; // [0, 20]
	// public static double energon_to_consider_spawning_sniper = 6; // [0, 20]
	// public static double desire_to_turn_given_spawn_desire = 1; // [0, 1]
	//	
	// public static double desire_to_move_toward_enemy = 0.5; // [0, 1]
	// public static double desire_to_move_given_spawn_desire = 1; // [0, 1]
	// public static double willingness_to_move_backwards = 0.9; // [0, 1]
	//	
	// public static double generosity = 2.0; // [0, 10]
	// // Genetic Algorithm end >>>
}
