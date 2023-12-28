package time;

import java.io.IOException;

import main.Solver;
import path.PIndividual;
import problem.Parameters;

public class TSolver {

	public static PIndividual path;

	public TSolver(PIndividual path) {
		TSolver.path = path;
	}

	public Swarm run() throws IOException {
		Particle.setCountEval(0);
		int n = path.getPath().length;

		Swarm swarm = new Swarm();
		double gbest[] = new double[n];
		for (int i = 0; i < n; i++) {
			gbest[i] = Solver.w[path.getPath(i)];
		}
		swarm.setBest(gbest);
		swarm.setBestFitness(path.getFitness());

		swarm.greedyInitSwarm(Parameters.SWARM_SIZE);
//		swarm.randomInitSwarm(Parameters.SWARM_SIZE);

		while (Particle.getCountEval() < Parameters.T_MAX_EVAL && swarm.getBestFitness() > 0) {
			double w = Parameters.MAX_W
					- (Parameters.MAX_W - Parameters.MIN_W) * Particle.getCountEval() / Parameters.T_MAX_EVAL;
			double c1 = 2, c2 = 2;

			for (Particle par : swarm.getParticles()) {
				for (int i = 0; i < n; i++) {
					double rp = Parameters.rand.nextDouble();
					double rg = Parameters.rand.nextDouble();

					par.setV(i, w * par.getV(i) + c1 * rp * (par.getP(i) - par.getX(i))
							+ c2 * rg * (swarm.getBest(i) - par.getX(i)));

					par.setX(i, par.getX(i) + par.getV(i) + 0.0005 * (-0.5 + Parameters.rand.nextDouble()));
				}

				par.setFitness(par.repairAndCalculateFitness());
				if (par.getBestFitness() > par.getFitness()) {
					par.setP(par.getX().clone());
					par.setBestFitness(par.getFitness());
				}

				swarm.updateBest(par);
			}

//			System.out.println(Particle.getCountEval() + "\t" + swarm.getBestFitness());
		}

		return swarm;
	}
}
