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

/**
 * This class is designed to evaluate the quality of a solution population
 * @author zhuoyingli
 *
 */
public class PWTQualityIndicator {

	/*
	 *  The PWT problem
	 */
	private PWT problem = null;
	
	/*
	 * The values of pareto front 
	 */
	private ArrayList<Double[]> front = null;
	
	/**
	 * Constructor
	 * @param problem the problem instance
	 * @param FrontFileName the file name of pareto front
	 */
	public PWTQualityIndicator(Problem problem, String FrontFileName ) {
		this.problem = (PWT)problem;
		front = new ArrayList<Double[]>();
		try {
			preProcess(FrontFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Calculate the error rate of a point
	 * @param front1_weight the weight of neighbour point of the objective point
	 * @param front1_ob  the value of neighbour point of the objective point
	 * @param front2_weight the weight of neighbour point of the objective point
	 * @param front2_ob the value of neighbour point of the objective point
	 * @param objective_weight the weight of the objective point
	 * @param objective_ob the value of the objective point
	 * @return the error rate of the objective point
	 */
	public double calculateErrorRate(double front1_weight, double front1_ob, double front2_weight, double front2_ob, double objective_weight, double objective_ob){
		double result = 0d;
		// y = Ax + b
		// calculate the factor A
		double A = (front2_ob - front1_ob) / (front2_weight - front1_weight);
		// calculate the const value b
		double b = front1_ob - A * front1_weight;
		// calculate the front value at the weight of objective point
		double front_objective_ob = A * objective_weight + b;
		// calculate the error rate
		double error = Math.abs(front_objective_ob - (-objective_ob)) / Math.abs(front_objective_ob);
		result = error;
		return result;
		
	}
	
	/**
	 * interface for evaluate the quality
	 * @param population the population to be evaluated
	 * @return the average error rate
	 */
	public double QualityEvaluate(SolutionSet population){

		ArrayList<Double> Errors = new ArrayList<>();
		double errorSum = -1d;
		// Loop the population
		for (int i = 0; i < population.size(); i++){
			Solution currentSolution = population.get(i);
			PWT problem = (PWT) currentSolution.getProblem();
			// get the weight and ob value of the current solution
			double objective_weight = population.get(i).getObjective(1);
			double objective_ob = population.get(i).getObjective(0);
			// next solution if the current one is infeasible
			if(objective_weight > problem.capacityOfKnapsack_ || objective_weight < 0)continue;
			
			// Get the neighbour points 
			int front_Lower_Index = getFrontLowerIndex(objective_weight);
			// handle boundary points
			if( front_Lower_Index == front.size() || front_Lower_Index == -1) {
				System.out.println();
			}
			if(front_Lower_Index == front.size()-1) front_Lower_Index = front_Lower_Index -1;
			if(front_Lower_Index == front.size()-1){
				System.out.println();
			}
			
			// get the neighbour values 
			double front1_weight = front.get(front_Lower_Index)[0];
			double front1_ob = front.get(front_Lower_Index)[1];
			double front2_weight = front.get(front_Lower_Index + 1)[0];
			double front2_ob = front.get(front_Lower_Index + 1)[1];
			
			// calculate the error of the current solution
			double currentError = calculateErrorRate(front1_weight, front1_ob, front2_weight, front2_ob, objective_weight, objective_ob);
			Errors.add(currentError);
			// Sum the total error of this population
			errorSum += currentError;
		}
		// if no feasible solution set error as NaN
		if (errorSum < 0) {
			errorSum = Double.NaN;
		}
		// calculate the average error rate
		double avg_error = errorSum / population.size();
		return avg_error;
		
	}
	
	/**
	 * get the index of the left neighbour using binary search
	 * @param weight the objective point 
	 * @return the left neighbour
	 */
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
