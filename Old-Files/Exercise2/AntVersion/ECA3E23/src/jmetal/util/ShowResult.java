package jmetal.util;

import java.util.Arrays;
import java.util.Comparator;

import jmetal.core.SolutionSet;
/**
 * this class is designed to Print sorted results to screen
 * @author zhuoyingli
 *
 */
public class ShowResult {

	/**
	 * Print sorted results to screen
	 * @param population
	 */
	public static void showresult(SolutionSet population){
		double[][] ob = new double[population.size()][2];
		// Extract the objectives
		for(int i = 0; i < population.size();i++){
			ob[i][0] =0-population.get(i).getObjective(0);
			ob[i][1] = population.get(i).getObjective(1);
		}
		// Sort the objectives
		class MyComparator implements Comparator<double[]> {
			@Override
			public int compare(double[] i,  double[] j) {
				if(i[1]>j[1]){
					return 1;
				}
				if(i[1]<j[1]){
					return -1;
				}
				return 0;
			}
		}
		Arrays.sort(ob, new MyComparator());
		// Print to screen
		for(int i = 0; i < population.size();i++){
			System.out.println(ob[i][0] + "\t" + ob[i][1]);
		}
	}
}
