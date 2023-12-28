package problem;

public class Solution {
	
	private int[] path;
	private double[] chargingTime;
	private int dead;
	private double totalCycleTime;
	private double fitness;
	
	public Solution() {
		
	}

	public int[] getPath() {
		return path;
	}

	public void setPath(int[] path) {
		this.path = path;
	}

	public double[] getChargingTime() {
		return chargingTime;
	}

	public void setChargingTime(double[] chargingTime) {
		this.chargingTime = chargingTime;
	}

	public int getDead() {
		return dead;
	}

	public void setDead(int dead) {
		this.dead = dead;
	}

	public double getTotalCycleTime() {
		return totalCycleTime;
	}

	public void setTotalCycleTime(double totalCycleTime) {
		this.totalCycleTime = totalCycleTime;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
}
