package jmetal.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.problems.PWT;

public class PWTQualityIndicator {

	private PWT problem = null;
	
//	private double[][] front = null;
	private ArrayList<Double[]> front = null;
	
//	private HashMap<Integer, Double> front = null;
	
	public PWTQualityIndicator(Problem problem, String FrontFileName ) {
		this.problem = (PWT)problem;
		front = new ArrayList<Double[]>();
		try {
			preProcess(FrontFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		this.front = new HashMap<>();
		// TODO Auto-generated constructor stub
	}

	public double calculateErrorRate(double front1_weight, double front1_ob, double front2_weight, double front2_ob, double objective_weight, double objective_ob){
		double result = 0d;
		// ob = A * weight
//		if (objective_weight == 0d) return 0d;
		double A = (front2_ob - front1_ob) / (front2_weight - front1_weight);
		double b = front1_ob - A * front1_weight;
		double front_objective_ob = A * objective_weight + b;
		double error = Math.abs(front_objective_ob - (-objective_ob)) / Math.abs(front_objective_ob);
		result = error;
		return result;
		
	}
	
	public double QualityEvaluate(SolutionSet population){
//		double[] Errors = new double[population.size()];
		ArrayList<Double> Errors = new ArrayList<>();
		double errorSum = -1d;
		for (int i = 0; i < population.size(); i++){
			Solution currentSolution = population.get(i);
			PWT problem = (PWT) currentSolution.getProblem();
			
			double objective_weight = population.get(i).getObjective(1);
			double objective_ob = population.get(i).getObjective(0);
			if(objective_weight > problem.capacityOfKnapsack_ || objective_weight < 0)continue;
			
			int front_Lower_Index = getFrontLowerIndex(objective_weight);
			if( front_Lower_Index == front.size() || front_Lower_Index == -1) {
				System.out.println();
			}
			if(front_Lower_Index == front.size()-1) front_Lower_Index = front_Lower_Index -1;
			if(front_Lower_Index == front.size()-1){
				System.out.println();
			}
			double front1_weight = front.get(front_Lower_Index)[0];
			double front1_ob = front.get(front_Lower_Index)[1];
			double front2_weight = front.get(front_Lower_Index + 1)[0];
			double front2_ob = front.get(front_Lower_Index + 1)[1];
			double currentError = calculateErrorRate(front1_weight, front1_ob, front2_weight, front2_ob, objective_weight, objective_ob);
			Errors.add(currentError);
			errorSum += currentError;
		}
		if (errorSum < 0) {
			errorSum = Double.NaN;
		}
		double avg_error = errorSum / population.size();
		return avg_error;
		
	}
	
	public int getFrontLowerIndex(double weight){
		int low = 0;
		int high = front.size()-1;
		int mid = 0;
		// check upper and lower bound
		if(front.get(high)[0]<=weight)
            return high;
        if(front.get(low)[0]>=weight)
            return low;
        // loop
		while (low < high){
	        mid = (low+high)/2;
	        
	        if(front.get(mid)[0]==weight)
	            return mid;
	        if(low + 1 == high )break;
	        if(front.get(low)[0] < weight && front.get(low+1)[0] > weight)
	        	return low;
	        if(front.get(mid)[0]>weight)
	            high = mid-1;
	        if(front.get(mid)[0]<weight)
	            low = mid+1;
	        
		}
		return low;
	}
	
	/**
	 * This function is used for coverting normal .front to
	 * a valid .front file and store in memory
	 * @param FrontFileName
	 * @return
	 * @throws IOException
	 */
	public void preProcess(String FrontFileName) throws IOException {
		/* Open the file */
		FileInputStream fis   = new FileInputStream(FrontFileName)     ;
		InputStreamReader isr = new InputStreamReader(fis)    ;
		BufferedReader br      = new BufferedReader(isr)      ;

		// skip first two lines
		// in oder to directely read node info
		String Line = br.readLine();
		Line = br.readLine();
		Line = br.readLine();
//		Double[] temp = {new Double(0.0), new Double(-6330.40)};
//		front.add(temp);
		// save all info to string
		while (Line!= null) {
			String[] splitLine = Line.split("\\s");
			Double tmp1 = Double.valueOf(splitLine[0]);
			Double tmp2 = Double.valueOf(splitLine[1]);
			Double[] tmp = {tmp1,tmp2};
			front.add(tmp);
			// increment 
			Line = br.readLine();
		}
	}

}
