package time;

import java.util.PriorityQueue;

import element.Charger;
import element.Sensor;
import main.Solver;
import path.PIndividual;
import problem.Parameters;
import problem.Problem;

public class Particle {

	private static int countEval;

	private double[] x;
	private double fitness;
	private double totalCycleTime;

	private double[] p;
	private double[] v;
	private double bestFitness;

	public Particle() {
		this.setFitness(1.0);
		this.setBestFitness(1.0);
		this.setX(new double[TSolver.path.getPath().length]);
		this.setV(new double[TSolver.path.getPath().length]);
		this.setP(new double[TSolver.path.getPath().length]);
	}

	public void greedyInitParticle() {
		double sumx = 0;
		int n = TSolver.path.getPath().length;
		for (int i = 0; i < n; i++) {
			this.setX(i, Parameters.rand.nextDouble());
			sumx += this.getX(i);
		}

		for (int i = 0; i < n; i++) {
			this.setX(i, this.getX(i) / sumx);
			this.setP(i, this.getX(i));
			this.setV(i, Parameters.INIT_C2 * Parameters.rand.nextDouble() * (Solver.w[TSolver.path.getPath(i)])
					- this.getX(i));
		}
		
		this.setFitness(this.repairAndCalculateFitness());
	}
	
	public void randomInitParticle() {
		double sumx = 0;
		int n = TSolver.path.getPath().length;
		
		for (int i = 0; i < n; i++) {
			this.setX(i, Parameters.rand.nextDouble());
			sumx += this.getX(i);
		}
		
		for (int i = 0; i < n; i++) {
			this.setX(i, this.getX(i) / sumx);
			this.setV(i, -1 + 2 * Parameters.rand.nextDouble());
		}
		
		this.setP(this.getX().clone());
		this.setFitness(this.repairAndCalculateFitness());
	}

	public double repairAndCalculateFitness() {
		Particle.setCountEval(Particle.getCountEval() + 1);

		Charger ch = Problem.charger;
		PIndividual path = TSolver.path;
		int n = path.getPath().length;
		double maxTc = path.getChargingTimeUB();
		double[] arriveTime = new double[n];

		int dead = 0;
		double sum = 0;

		arriveTime[0] = Problem.distance[Problem.serviceStation.getId()][path.getPath(0)] / ch.getSpeed();
		for (int i = 0; i < n; i++) {
			Sensor s = Problem.getSensorById(path.getPath(i));
			double eArrive = s.getE0() - arriveTime[i] * s.getPi();
			double ub = (ch.getU() - s.getPi() <= 0) ? 1 : (s.getEmax() - eArrive) / (ch.getU() - s.getPi()) / maxTc;
			if (eArrive < s.getEmin() || x[i] < Parameters.MIN_CHARGING / ch.getU() / maxTc) {
				x[i] = 0;
			} else if (x[i] > ub) {
				x[i] = ub;
			}
			sum += x[i];

			if (i < n - 1) {
				arriveTime[i + 1] = arriveTime[i]
						+ Problem.distance[path.getPath(i)][path.getPath(i + 1)] / ch.getSpeed() + x[i] * maxTc;
			}
		}
		
		if (sum > 1) {
			for (int i = 0; i < n; i++) {
				Sensor s = Problem.getSensorById(path.getPath(i));
				x[i] /= sum;
				double eArrive = s.getE0() - arriveTime[i] * s.getPi();
				double ub = (ch.getU() - s.getPi() <= 0) ? 1.0 : (s.getEmax() - eArrive) / (ch.getU() - s.getPi()) / maxTc;
				if (eArrive < s.getEmin() || x[i] < Parameters.MIN_CHARGING / ch.getU() / maxTc) {
					x[i] = 0;
				} else if (x[i] > ub) {
					x[i] = ub;
				}
				
				if (i < n - 1) {
					arriveTime[i + 1] = arriveTime[i]
							+ Problem.distance[path.getPath(i)][path.getPath(i + 1)] / ch.getSpeed() + x[i] * maxTc;
				}
			}
		}


		totalCycleTime = arriveTime[n - 1] + x[n - 1] * maxTc
				+ Problem.distance[path.getPath(n - 1)][Problem.serviceStation.getId()] / ch.getSpeed();

		double threshold = totalCycleTime + Problem.accTime >= Parameters.surveyTime ? 0 : Parameters.THRESHOLD;
		PriorityQueue<Double> deltaE = new PriorityQueue<Double>();

		for (int i = 0; i < n; i++) {
			Sensor s = Problem.getSensorById(path.getPath(i));
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
				double delta = x[i] * maxTc * ch.getU() - Math.max(path.getTotalCycleTime(), totalCycleTime) * s.getPi();
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

	public double[] getX() {
		return x;
	}

	public void setX(double[] x) {
		this.x = x;
	}

	public void setX(int index, double x) {
		this.x[index] = x;
	}

	public double getX(int index) {
		return x[index];
	}

	public double[] getP() {
		return p;
	}

	public double getP(int index) {
		return p[index];
	}

	public void setP(int index, double p) {
		this.p[index] = p;
	}

	public void setP(double[] p) {
		this.p = p;
	}

	public double[] getV() {
		return v;
	}

	public double getV(int index) {
		return v[index];
	}

	public void setV(double[] v) {
		this.v = v;
	}

	public void setV(int index, double v) {
		this.v[index] = v;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public static int getCountEval() {
		return countEval;
	}

	public static void setCountEval(int countEval) {
		Particle.countEval = countEval;
	}

	public double getTotalCycleTime() {
		return totalCycleTime;
	}

	public double getBestFitness() {
		return bestFitness;
	}

	public void setBestFitness(double bestFitness) {
		this.bestFitness = bestFitness;
	}
}
