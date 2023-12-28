package main;

import java.io.IOException;
import java.util.Random;

import element.Charger;
import path.PIndividual;
import problem.Parameters;
import problem.Problem;

public class MainTest {
	public static String input;
	public static String datatype;
	public static void main(String[] arg) throws IOException {

		for (int i = 265 ; i <= 268 ;i++) {
			int test =i;

			if (i <= 30 ) {
				 input = "data\\input\\small\\normal\\n100_01_simulated.txt";
				datatype = "n100_01_simulated";
			} else if (i <= 60 && i>30) {
				input = "data\\input\\small\\normal\\n100_02_simulated.txt";
				datatype = "n100_02_simulated";
			}else if (i <= 90 && i >60) {
				input = "data\\input\\small\\normal\\n100_03_simulated.txt";
				datatype = "n100_03_simulated";
			}
			else if (i <= 120 && i > 90) {
				 input = "data\\input\\small\\normal\\n100_04_simulated.txt";
				datatype = "n100_04_simulated";
			}
			else if (i <= 150 && i > 120) {
				input = "data\\input\\small\\normal\\n100_05_simulated.txt";
				datatype = "n100_05_simulated";
			}
			else if (i <= 180 && i > 150) {
				input = "data\\input\\small\\normal\\n100_06_simulated.txt";
				datatype = "n100_06_simulated";
			}
			else if (i <= 210 && i > 180) {
				input = "data\\input\\small\\normal\\n100_07_simulated.txt";
				datatype = "n100_07_simulated";
			}
			else if (i <= 240 && i > 210) {
				input = "data\\input\\small\\normal\\n100_08_simulated.txt";
				datatype = "n100_08_simulated";
			}
			else if (i <= 270 && i > 240) {
				input = "data\\input\\small\\normal\\n100_09_simulated.txt";
				datatype = "n100_09_simulated";
			}
			else if (i <= 300 && i >270) {
				input = "data\\input\\small\\normal\\n100_010_simulated.txt";
				datatype = "n100_010_simulated";
			}


			Parameters.surveyTime = 72000;
			Parameters.maximalCycleTime = 15000;
			Parameters.alpha = 0.5;

			Problem.reset();
			Problem.loadData(input, 1);
			Problem.charger = new Charger(108000, 5, 1, 5);
//			Parameters.rand = new Random(0);
			Problem.cycle = 1;
			Problem.deadInT.clear();
			Problem.pastCycle = 0;
			while (Problem.accTime < Parameters.surveyTime) {
				// Initialize the population $P_0$ of the upper level task
				Solver solver = new Solver();

				PIndividual result = solver.solve();
				double[] chargingTime = new double[Problem.sensors.size()];
				for (int j = 0; j < chargingTime.length; j++) {
					chargingTime[j] = result.getTimeRate()[j] * result.getChargingTimeUB();
				}

				// comment this line to disable the console logging
				Problem.logSolution(result.getPath(), chargingTime, test);

				// never comment this line
				Problem.addCycleSolution(result.getPath(), chargingTime);
			}
			int numberOfDeadSensors = Problem.deadInT.size();

			double deadRatio = 100.0 * numberOfDeadSensors / Problem.deployedSensors;
			System.out.println("Dead ratio: " + deadRatio + "%");
			System.out.println("Dead detail: " + Problem.deadInT);
			System.out.println(numberOfDeadSensors);
			PrintExcel print = new PrintExcel();
			print.writeDataToExcel(numberOfDeadSensors, i, Problem.deadInT, datatype);
		}
		}

}

