package problem;

import java.util.Random;

public class Parameters {

	public static double surveyTime;
	public static double maximalCycleTime;
	public static Random rand;
	public static double alpha;

	public static final double S_EMAX = 10800;
	public static final double S_EMIN = 540;
	public static final double MIN_CHARGING = 100;
	public static final double THRESHOLD = 600;

	public static final int P_MAX_EVAL = 25000;
	public static final int P_POPULATION_SIZE = 100;
	public static final double P_MUTATION_RATIO = 0.05;

	public static final double MIN_W = 0.4;
	public static final double MAX_W = 0.9;
	public static final double INIT_C1 = 2.0;
	public static final double INIT_C2 = 2.0;
	public static final int T_MAX_EVAL = 5000;
	public static final int SWARM_SIZE = 50;
}