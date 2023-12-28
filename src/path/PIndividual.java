package path;

import java.util.ArrayList;
import java.util.PriorityQueue;

import element.Charger;
import element.Sensor;
import main.Solver;
import problem.Parameters;
import problem.Problem;

public class PIndividual implements Comparable {

	private static int counter;
	private static int countEval;
	private int id;

	private int[] path;
	private double fitness;

	private double[] timeRate;
	private double problemFitness;
	private double totalCycleTime;
	private int deadInT;

	private int[] index;
	private double movingTime;
	private double chargingTimeUB;

	public PIndividual() {
		PIndividual.setCounter(PIndividual.getCounter() + 1);
		this.setId(PIndividual.getCounter());
		this.setPath(new int[Problem.sensors.size()]);
		this.setFitness(Double.MAX_VALUE);
		this.setProblemFitness(Double.MAX_VALUE);
	}

	public void randomInit() {
		ArrayList<Sensor> pool = new ArrayList<Sensor>();
		pool.addAll(Problem.sensors);

		int idx = 0;
		while (!pool.isEmpty()) {
			path[idx++] = pool.remove(Parameters.rand.nextInt(pool.size())).getId();
		}
	}

	public void greedyInit(int k) {
		final int NEIGHBOUR = k;
		ArrayList<Sensor> pool = new ArrayList<Sensor>();
		pool.addAll(Problem.sensors);
		ArrayList<Integer> path = new ArrayList<Integer>();

		path.add(Problem.serviceStation.getId());
		ArrayList<Integer> neighbours = new ArrayList<Integer>();

		while (!pool.isEmpty()) {
			while (neighbours.size() < NEIGHBOUR && pool.size() > neighbours.size()) {
				Sensor nei = null;
				for (Sensor s : pool) {
					if (!neighbours.contains(s.getId())) {
						if (nei == null || Problem.distance[path.get(path.size() - 1)][nei
								.getId()] > Problem.distance[path.get(path.size() - 1)][s.getId()]) {
							nei = s;
						}
					}
				}
				neighbours.add(nei.getId());
			}

			int next = neighbours.get(Parameters.rand.nextInt(neighbours.size()));
			path.add(next);
			pool.remove(Problem.getSensorById(next));
			neighbours.clear();
		}
		path.remove(0);

		for (int i = 0; i < path.size(); i++) {
			this.path[i] = path.get(i);
		}
	}

	public double calculateFitness() {
		PIndividual.setCountEval(PIndividual.getCountEval() + 1);
		Charger ch = Problem.charger;
		int n = path.length;
		double maxTc = this.getChargingTimeUB();

		double x[] = new double[path.length];
		double arriveTime[] = new double[path.length];
		double dead = 0;

		arriveTime[0] = Problem.distance[Problem.serviceStation.getId()][path[0]] / ch.getSpeed();
		for (int i = 0; i < n; i++) {
			Sensor s = Problem.getSensorById(path[i]);
			x[i] = Solver.w[s.getId()];

			double eArrive = s.getE0() - arriveTime[i] * s.getPi();
			double ub = (ch.getU() - s.getPi() <= 0) ? 1 : (s.getEmax() - eArrive) / (ch.getU() - s.getPi()) / maxTc;
			if (eArrive < s.getEmin() || x[i] < Parameters.MIN_CHARGING / ch.getU() / maxTc) {
				x[i] = 0;
			} else if (x[i] > ub) {
				x[i] = ub;
			}

			if (i < n - 1) {
				arriveTime[i + 1] = arriveTime[i] + Problem.distance[path[i]][path[i + 1]] / ch.getSpeed()
						+ x[i] * maxTc;
			}
		}

		totalCycleTime = arriveTime[n - 1] + x[n - 1] * maxTc
				+ Problem.distance[path[n - 1]][Problem.serviceStation.getId()] / ch.getSpeed();

		double threshold = totalCycleTime + Problem.accTime >= Parameters.surveyTime ? 0 : Parameters.THRESHOLD;
		PriorityQueue<Double> deltaE = new PriorityQueue<Double>();

		for (int i = 0; i < n; i++) {
			Sensor s = Problem.getSensorById(path[i]);
			double eRemain = s.getE0() - arriveTime[i] * s.getPi();
			double eAfterCharge = eRemain + x[i] * maxTc * (ch.getU() - s.getPi());
			double eAfterRound = s.getE0() - totalCycleTime * s.getPi() + x[i] * maxTc * ch.getU();

			if (eRemain < s.getEmin() - 1e-5
					&& Problem.accTime + (s.getE0() - s.getEmin()) / s.getPi() < Parameters.surveyTime) {
				dead++;
			} else if (eAfterRound < s.getEmin() + threshold && Problem.accTime + arriveTime[i] + x[i] * maxTc
					+ Math.max(0, eAfterCharge - s.getEmin()) / s.getPi() < Parameters.surveyTime) {
				dead++;
			} else {
				// delta = E_after - E_before
				double delta = x[i] * maxTc * ch.getU() - totalCycleTime * s.getPi();
				if (delta < 0) {
					deltaE.add(delta);
				}
			}
		}

		double maxDec = 0;
		if (deltaE.size() > 0) {
			int k = Math.min(deltaE.size(), Math.max(1, Math.min(10, (int) (0.05 * n))));
			for (int i = 0; i < k; i++) {
				maxDec += deltaE.remove();
			}
			maxDec /= (-1.0 * k);
		}

		if (Problem.accTime + totalCycleTime >= Parameters.surveyTime) {
			return dead / (1.0 * n);
		} else {
			return Parameters.alpha * dead / (1.0 * n)
					+ (1.0 - Parameters.alpha) * maxDec / (Parameters.S_EMAX - Parameters.S_EMIN);
		}
	}

	public void updateChargingResult() {
		Charger ch = Problem.charger;
		int n = path.length;
		double maxTc = this.getChargingTimeUB();

		double arriveTime[] = new double[path.length];
		arriveTime[0] = Problem.distance[Problem.serviceStation.getId()][path[0]] / ch.getSpeed();
		for (int i = 0; i < n; i++) {
			Sensor s = Problem.getSensorById(path[i]);
			double eArrive = s.getE0() - arriveTime[i] * s.getPi();
			double ub = (ch.getU() - s.getPi() <= 0) ? 1 : (s.getEmax() - eArrive) / (ch.getU() - s.getPi()) / maxTc;
			if (eArrive < s.getEmin() || timeRate[i] < Parameters.MIN_CHARGING / ch.getU() / maxTc) {
				timeRate[i] = 0;
			} else if (timeRate[i] > ub) {
				timeRate[i] = ub;
			}

			if (i < n - 1) {
				arriveTime[i + 1] = arriveTime[i] + Problem.distance[path[i]][path[i + 1]] / ch.getSpeed()
						+ timeRate[i] * maxTc;
			}
		}

		totalCycleTime = arriveTime[n - 1] + timeRate[n - 1] * maxTc
				+ Problem.distance[path[n - 1]][Problem.serviceStation.getId()] / ch.getSpeed();
		this.setDeadInT(0);
		for (int i = 0; i < n; i++) {
			Sensor s = Problem.getSensorById(path[i]);
			double eRemain = s.getE0() - arriveTime[i] * s.getPi();
			double eAfterCharge = eRemain + timeRate[i] * maxTc * (ch.getU() - s.getPi());
			double eAfterRound = s.getE0() - totalCycleTime * s.getPi() + timeRate[i] * maxTc * ch.getU();

			if (eRemain < s.getEmin() - 1e-5
					&& Problem.accTime + (s.getE0() - s.getEmin()) / s.getPi() < Parameters.surveyTime) {
				this.setDeadInT(this.getDeadInT() + 1);
			} else if (eAfterRound < s.getEmin() && Problem.accTime + arriveTime[i] + timeRate[i] * maxTc
					+ Math.max(0, eAfterCharge - s.getEmin()) / s.getPi() < Parameters.surveyTime) {
				this.setDeadInT(this.getDeadInT() + 1);
			}
		}
	}

	public static int getCounter() {
		return counter;
	}

	public static void setCounter(int counter) {
		PIndividual.counter = counter;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int[] getPath() {
		return path;
	}

	public void setPath(int[] path) {
		this.path = path;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public int[] getIndex() {
		if (index == null) {
			index = new int[Problem.maxId + 1];
			for (int i = 0; i < path.length; i++) {
				index[path[i]] = i;
			}
		}
		return index;
	}

	public void setPath(int index, int value) {
		path[index] = value;
	}

	public int getPath(int index) {
		return path[index];
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return Double.valueOf(this.getFitness()).compareTo(((PIndividual) o).getFitness());
	}

	public PIndividual clonePath() {
		PIndividual cpy = new PIndividual();
		cpy.setPath(this.getPath().clone());
		return cpy;
	}

	public double getMovingTime() {
		if (movingTime == 0) {
			this.setMovingTime(Problem.getTotalTimeTravel(path));
		}
		return movingTime;
	}

	public void setMovingTime(double movingTime) {
		this.movingTime = movingTime;
	}

	public static int getCountEval() {
		return countEval;
	}

	public static void setCountEval(int countEval) {
		PIndividual.countEval = countEval;
	}

	public double getChargingTimeUB() {
		if (chargingTimeUB == 0) {
			Charger ch = Problem.charger;
			double maxTc = (ch.getE0() - this.getMovingTime() * ch.getPm()) / ch.getU();
			chargingTimeUB = Math.min(maxTc, Parameters.maximalCycleTime);
			if (Problem.sumP > ch.getU()) {
				double ub = (Problem.sumE0 - path.length * Parameters.S_EMIN - this.getMovingTime() * Problem.sumP)
						/ (Problem.sumP - ch.getU());
				chargingTimeUB = Math.min(chargingTimeUB, ub);
			} else if (Problem.sumP < ch.getU()) {
				double ub = (path.length * Parameters.S_EMAX - Problem.sumE0
						+ this.getMovingTime() * Problem.sumP) / (ch.getU() - Problem.sumP);
				chargingTimeUB = Math.min(chargingTimeUB, ub);
			}
		}
//		System.out.println(chargingTimeUB);
		return chargingTimeUB;
	}

	public double[] getTimeRate() {
		return timeRate;
	}

	public void setTimeRate(double[] timeRate) {
		this.timeRate = timeRate;
	}

	public double getProblemFitness() {
		return problemFitness;
	}

	public void setProblemFitness(double problemFitness) {
		this.problemFitness = problemFitness;
	}

	public double getTotalCycleTime() {
		return totalCycleTime;
	}

	public void setTotalCycleTime(double totalCycleTime) {
		this.totalCycleTime = totalCycleTime;
	}

	public int getDeadInT() {
		return deadInT;
	}

	public void setDeadInT(int deadInT) {
		this.deadInT = deadInT;
	}
}
