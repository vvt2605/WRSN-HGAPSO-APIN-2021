package path;

import java.util.ArrayList;
import java.util.Collections;

import problem.Parameters;
import problem.Problem;

public class PPopulation {

	private ArrayList<PIndividual> individuals;

	public PPopulation() {
		individuals = new ArrayList<PIndividual>();
	}

	public void randomInit(int size) {
		this.getIndividuals().clear();
		while (this.getIndividuals().size() < size) {
			PIndividual indiv = new PIndividual();
			indiv.randomInit();
			indiv.setFitness(indiv.calculateFitness());
			this.getIndividuals().add(indiv);
		}
	}
	
	public void greedyInit(int size) {
		this.getIndividuals().clear();
		while (this.getIndividuals().size() < size) {
			PIndividual indiv = new PIndividual();
			indiv.greedyInit(2 + Parameters.rand.nextInt(9));
//			indiv.greedyInit(2 + Parameters.rand.nextInt(Problem.sensors.size()-1));
			indiv.setFitness(indiv.calculateFitness());
			this.getIndividuals().add(indiv);
		}
	}

	public ArrayList<PIndividual> crossover(PIndividual par1, PIndividual par2) {
		if (Parameters.rand.nextDouble() <= 0.5) {
//			return this.singlePointCrossover(par1, par2);
			ArrayList<PIndividual> offspring = new ArrayList<PIndividual>();
			offspring.add(this.order1Crossover(par1, par2));
			offspring.add(this.order1Crossover(par2, par1));
			return offspring;
		} else {
			ArrayList<PIndividual> offspring = new ArrayList<PIndividual>();
			offspring.add(this.partiallyMappedCrossover(par1, par2));
			offspring.add(this.partiallyMappedCrossover(par2, par1));
			return offspring;
		}
	}

	public PIndividual mutation(PIndividual indiv) {
		return this.swapMutation(indiv);
//		if (Parameters.rand.nextDouble() <= 0.5) {
//		} else {
//			return this.cimMutation(indiv);
//		}
	}

	@SuppressWarnings("unchecked")
		public void executeSelection() {
		Collections.sort(this.getIndividuals());
//		System.out.println("Number size " + this.getIndividuals().size());
		while (this.getIndividuals().size() > Parameters.P_POPULATION_SIZE) {
			this.getIndividuals().remove(this.getIndividuals().size() - 1);
		}

	}

	private ArrayList<PIndividual> singlePointCrossover(PIndividual par1, PIndividual par2) {
		int n = par1.getPath().length;
		ArrayList<PIndividual> result = new ArrayList<PIndividual>();
		int crossPoint = 1 + Parameters.rand.nextInt(n - 2);
		PIndividual ch1 = new PIndividual();
		PIndividual ch2 = new PIndividual();

		ArrayList<Integer> s1 = new ArrayList<Integer>();
		ArrayList<Integer> s2 = new ArrayList<Integer>();

		for (int i = 0; i <= crossPoint; i++) {
			s1.add(par1.getPath(i));
			s2.add(par2.getPath(i));
		}

		for (int i = crossPoint; i < n; i++) {
			if (!s1.contains(par2.getPath(i))) {
				s1.add(par2.getPath(i));
			}

			if (!s2.contains(par1.getPath(i))) {
				s2.add(par1.getPath(i));
			}
		}

		for (int i = 0; i < crossPoint; i++) {
			if (!s1.contains(par2.getPath(i))) {
				s1.add(par2.getPath(i));
			}
			ch1.setPath(i, s1.get(i));

			if (!s2.contains(par1.getPath(i))) {
				s2.add(par1.getPath(i));
			}
			ch2.setPath(i, s2.get(i));
		}

		for (int i = crossPoint; i < n; i++) {
			ch1.setPath(i, s1.get(i));
			ch2.setPath(i, s2.get(i));
		}

		result.add(ch1);
		result.add(ch2);
		return result;
	}

	private PIndividual partiallyMappedCrossover(PIndividual par1, PIndividual par2) {
		PIndividual child = par2.clonePath();
		int n = par1.getPath().length;

		int point1 = Parameters.rand.nextInt(n - 1);
		int point2 = point1 + 1 + Parameters.rand.nextInt(n - point1 - 1);

		int[] index1 = par1.getIndex();
		int[] index2 = par2.getIndex();

		for (int i = point1; i <= point2; i++) {
			child.setPath(i, par1.getPath(i));
		}

		for (int i = point1; i <= point2; i++) {
			int t1 = index1[par2.getPath(i)];
			int t2 = index2[par2.getPath(i)];

			if (point1 <= t1 && t1 <= point2 && point1 <= t2 && t2 <= point2)
				continue;

			int move = par2.getPath(i);
			while (point1 <= t2 && t2 <= point2) {
				t2 = index2[par1.getPath(t2)];
			}
			child.setPath(t2, move);
		}
		return child;
	}

	private PIndividual order1Crossover(PIndividual par1, PIndividual par2) {
		PIndividual child = par2.clonePath();
		int n = par1.getPath().length;

		int point1 = Parameters.rand.nextInt(n - 1);
		int point2 = point1 + 1 + Parameters.rand.nextInt(n - point1 - 1);

		int mark[] = new int[Problem.maxId + 1];

		for (int i = point1; i <= point2; i++) {
			child.setPath(i, par1.getPath(i));
			mark[par1.getPath(i)] = 1;
		}

		int tmp[] = new int[n];
		int idx = 0;
		for (int i = point2 + 1; i < n; i++) {
			tmp[idx++] = par2.getPath(i);
		}
		for (int i = 0; i <= point2; i++) {
			tmp[idx++] = par2.getPath(i);
		}

		idx = point2 + 1;
		int i = 0;
		for (i = 0; i < n && idx < n; i++) {
			if (mark[tmp[i]] == 0) {
				child.setPath(idx++, tmp[i]);
			}
		}

		idx = 0;
		while (idx < point1) {
			if (mark[tmp[i]] == 0) {
				child.setPath(idx++, tmp[i]);
			}
			i++;
		}
		
		return child;
	}

	private PIndividual swapMutation(PIndividual indiv) {
		PIndividual child = indiv.clonePath();
		int n = indiv.getPath().length;

		int point1 = Parameters.rand.nextInt(n - 1);
		int point2 = point1 + 1 + Parameters.rand.nextInt(n - point1 - 1);

		int tmp = child.getPath(point1);
		child.setPath(point1, child.getPath(point2));
		child.setPath(point2, tmp);

		return child;
	}

	private PIndividual cimMutation(PIndividual indiv) {
		int mp = 1 + Parameters.rand.nextInt(indiv.getPath().length - 1);
		PIndividual child = indiv.clonePath();

		int i = 0, j = mp;
		while (i < j) {
			int tmp = child.getPath(i);
			child.setPath(i, child.getPath(j));
			child.setPath(j, tmp);
			i++;
			j--;
		}

		i = mp + 1;
		j = indiv.getPath().length - 1;
		while (i < j) {
			int tmp = child.getPath(i);
			child.setPath(i, child.getPath(j));
			child.setPath(j, tmp);
			i++;
			j--;
		}

		return child;
	}

	public PIndividual getIndividual(int index) {
		return individuals.get(index);
	}

	public void addIndividual(PIndividual indiv) {
		this.individuals.add(indiv);
	}

	public void addIndividuals(ArrayList<PIndividual> indivs) {
		this.individuals.addAll(indivs);
	}

	public ArrayList<PIndividual> getIndividuals() {
		return individuals;
	}
}

