package main;

import java.io.IOException;
import java.util.ArrayList;

import element.Sensor;
import path.PIndividual;
import path.PPopulation;
import problem.Parameters;
import problem.Problem;
import time.Swarm;
import time.TSolver;
import  main.PrintExcel;
public class Solver {

	private PPopulation pop;
	private PIndividual best;

	private PIndividual bestProblemSolution;
	public static double[] w;

	public PIndividual solve() throws IOException {

		w = new double[Problem.maxId + 1];
		double sum = 0;

		for (Sensor s : Problem.sensors) {
			w[s.getId()] = Math.max(0, s.getPi() / Problem.sumP - s.getE0() / Problem.sumE0);
			sum += w[s.getId()];
		}
		// O(n) n là số cảm biến
		for (Sensor s : Problem.sensors) {
			w[s.getId()] /= sum;
		}
		// O(n) n là số cảm biến
		PIndividual.setCountEval(0);
		pop = new PPopulation();
		// pop.randomInit(Parameters.P_POPULATION_SIZE);

		pop.greedyInit(Parameters.P_POPULATION_SIZE);
		// O(n^2 logn size) n lầ số cảm biến size là population size

		pop.executeSelection();
		// O(nlog(n)) với n lầ số phần tử của mảng individuals 
		best = pop.getIndividual(0);

		TSolver tsolver = new TSolver(best);
		Swarm swarm = tsolver.run();
		// O(n* T_MAX_EVAL) trong đó n là là kích thước của mảng gbest
		best.setTimeRate(swarm.getBest().clone());

		best.setProblemFitness(swarm.getBestFitness());

		bestProblemSolution = best;

		best.updateChargingResult();

		//O(n) với n là độ dài của path
		int gen = 1;
		int k = Parameters.P_POPULATION_SIZE / 2;
		while (PIndividual.getCountEval() < Parameters.P_MAX_EVAL && best.getFitness() > 0) {
			// gen++;
			// System.out.println("Count Eval: "+ PIndividual.getCountEval());
			ArrayList<PIndividual> offspring = this.reproduction(Parameters.P_POPULATION_SIZE);
			// O(P_POPULATION_SIZE)
			for (int i = 0; i < k; i++) {
				offspring.add(pop.getIndividual(i));
			}
			// O(n/2)

			pop.getIndividuals().clear();
			pop.addIndividuals(offspring);
			pop.executeSelection();
			//O(nlogn)

			// optimize charging time for the new best path found
			if (best.getFitness() > pop.getIndividual(0).getFitness()) {
				best = pop.getIndividual(0);

				if (best.getTimeRate() == null) {
					tsolver = new TSolver(best);
					swarm = tsolver.run();
					// O(n* T_MAX_EVAL*Size) trong đó n là là kích thước của mảng gbest
					best.setTimeRate(swarm.getBest().clone());
					best.setProblemFitness(swarm.getBestFitness());
					best.updateChargingResult();
					//O(n)

					if (bestProblemSolution == null
							|| bestProblemSolution.getProblemFitness() > best.getProblemFitness()) {
						bestProblemSolution = best;
					}
				}
			}

			// System.out.println("Generations"+gen + "\t" + best.getFitness() + "\t" +
			// bestProblemSolution.getProblemFitness());
		}


		return bestProblemSolution;
	}

	public ArrayList<PIndividual> reproduction(int size) {
		ArrayList<PIndividual> offspring = new ArrayList<PIndividual>();
		ArrayList<PIndividual> pool = new ArrayList<PIndividual>();
		pool.addAll(pop.getIndividuals());

		while (offspring.size() < size) {
			PIndividual par1, par2, p1, p2;

			p1 = pool.remove(Parameters.rand.nextInt(pool.size()));
			p2 = pool.remove(Parameters.rand.nextInt(pool.size()));
			if (p1.getFitness() <= p2.getFitness()) {
				par1 = p1;
				pool.add(p2);
			} else {
				par1 = p2;
				pool.add(p1);
			}

			p1 = pool.remove(Parameters.rand.nextInt(pool.size()));
			p2 = pool.remove(Parameters.rand.nextInt(pool.size()));
			if (p1.getFitness() <= p2.getFitness()) {
				par2 = p1;
				pool.add(p2);
			} else {
				par2 = p2;
				pool.add(p1);
			}

			pool.add(par1);
			pool.add(par2);

			ArrayList<PIndividual> child = pop.crossover(par1, par2);
			for (PIndividual indiv : child) {
				if (Parameters.rand.nextDouble() <= Parameters.P_MUTATION_RATIO) {
					PIndividual p = pop.mutation(indiv);
					p.setFitness(p.calculateFitness());
					offspring.add(p);
				} else {
					indiv.setFitness(indiv.calculateFitness());
					offspring.add(indiv);
				}
			}
		}

		return offspring;
	}
}
