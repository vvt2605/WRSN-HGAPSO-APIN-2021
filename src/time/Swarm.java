package time;

import java.util.ArrayList;

public class Swarm {

	private ArrayList<Particle> particles;
	private double[] best;
	private double bestFitness;
	
	public Swarm() {
		this.setParticles(new ArrayList<Particle>());
		this.setBestFitness(Double.MAX_VALUE);
	}
	
	public void greedyInitSwarm(int size) {
		while (this.getParticles().size() < size) {
			Particle par = new Particle();
			par.greedyInitParticle();
			this.updateBest(par);
			this.getParticles().add(par);
		}
	}
	
	public void randomInitSwarm(int size) {
		while (this.getParticles().size() < size) {
			Particle par = new Particle();
			par.randomInitParticle();
			this.updateBest(par);
			this.getParticles().add(par);
		}
	}
	
	public void updateBest(Particle par) {
		if (this.getBestFitness() > par.getFitness()) {
			this.setBest(par.getX().clone());
			this.setBestFitness(par.getFitness());
		}
	}

	public void setBest(double[] best) {
		this.best = best;
	}
	
	public double[] getBest() {
		return best;
	}
	
	public double getBest(int index) {
		return best[index];
	}

	public ArrayList<Particle> getParticles() {
		return particles;
	}

	public void setParticles(ArrayList<Particle> particles) {
		this.particles = particles;
	}

	public double getBestFitness() {
		return bestFitness;
	}

	public void setBestFitness(double bestFitness) {
		this.bestFitness = bestFitness;
	}
}
