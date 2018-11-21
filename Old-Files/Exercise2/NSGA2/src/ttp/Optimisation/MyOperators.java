package ttp.Optimisation;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import jmetal.core.Solution;
import jmetal.problems.PWT;
import ttp.TTPInstance;
import ttp.TTPSolution;
import ttp.Utils.DeepCopy;

/**
 * This class is designed for finding an acceptable PackingPlan solution for the given TTP problem instance and TSP tour 
 * @author Zhuoying Li (a1675725)
 * @author Puzhi Yao (a1205593)
 *
 */
public class MyOperators {
	/**
	 * This function is designed for converting the index of PackingPlan into the index of in Items
	 * @param PPIndex Index of PackingPlan
	 * @param instance TTP instance
	 * @param tour TSP tour
	 * @return itemIndex index of items
	 */
	public static int PackingPlan2Item(int PPIndex, TTPInstance instance, int[] tour){
		int ItemsPerCity = instance.numberOfItems / (instance.numberOfNodes-1);
		// Formula:	
		// int indexOfPackingPlan = (i-1)*ItemsPerCity+itemNumber;
		int i = PPIndex / ItemsPerCity + 1;
		int itemNumber = PPIndex - (i-1) * ItemsPerCity;
		int currentCity = tour[i]-1;
		// calculate item index based on
		// previous calculated parameters
		int itemIndex = currentCity+itemNumber*(instance.numberOfNodes-1);//* (this.numberOfNodes-1); 
		return itemIndex;

	}

	/**
	 * This function is designed for converting the index of Items into the index of in PackingPlan
	 * @param ItemIndex Index of items
	 * @param instance TTP instance
	 * @param tour TSP tour
	 * @return itemIndex index of PackingPlan
	 */
	public static int Item2PackingPlan(int ItemIndex, TTPInstance instance, int[] tour){
		int ItemsPerCity = instance.numberOfItems / (instance.numberOfNodes-1);
		// Formula:     
		// int itemIndex = currentCity+itemNumber*(instance.numberOfNodes-1);//* (this.numberOfNodes-1); 
		int itemNumber = ItemIndex / (instance.numberOfNodes-1);
		// calculate current city index
		int currentCity = ItemIndex % (instance.numberOfNodes-1);
		int currentCityIndex = -1;
		// loop to find the current city index in tour
		for(int city =0 ; city < tour.length; ++city){
			if(currentCity + 1 == tour[city]) {
				currentCityIndex = city;
				break;
			}
		}
		int i = currentCityIndex;
		// calculate item index in packingPlan
		int indexOfPackingPlan = (i-1)*ItemsPerCity+itemNumber;
		// output packingplan item index
		int PPIndex = indexOfPackingPlan;
		return PPIndex;

	}


	/**
	 * This function is designed for recalculating the profit of each item according to the weight and increasing travel time
	 * For each item in each city, first calculate the remain distance from the current city to the end
	 * Then calculate the modified velocity if taking a item and the increasing travel time
	 * Finally modify the profit of each item by decreasing the cost from increasing time * RentRatio
	 * @param instance TTP instance
	 * @param tour TSP tour
	 * @return double[] penalised_profits calculated profits
	 * @throws ClassNotFoundException 
	 */
	public static double[] PenaltyFunction(PWT instance, int[] tour) throws ClassNotFoundException{
		//int indexOfPackingPlan = (i-1)*itemsPerCity+itemNumber;
		
		// Initialise a solution for calculating the total distance
		int[] zeros =  new int[instance.numberOfCities_];
		// setup zero array
		Arrays.fill(zeros, 0);
		// create new solution
		Solution temp = new Solution(instance);
		// evaluate new solution
		instance.evaluate(temp);
		// record current ftraw in solution
		long rawdistance = 0;
		for (int i=0; i<tour.length-1; i++) {
			long distance = (long)Math.ceil(instance.distances(tour[i],tour[i+1]));
			rawdistance += distance;
		}
		
		// setup penalty value arrays
		double[] penalised_profits = new double[instance.numberOfItems_];
		// calculate the number of items in each city
		int ItemsPerCity = instance.numberOfItems_ / (instance.numberOfItems_-1);
		long remainDistance = rawdistance;
		
		// Calculate the profits of items by starting from the second city (no items in the first city)
		for(int CityIndex = 1; CityIndex < instance.numberOfCities_-1; ++CityIndex){
			// setup city index info
			int currentCityIndex = CityIndex;
			int PrevCityIndex = currentCityIndex - 1;
			int currentCity = tour[currentCityIndex] -1;
			if (currentCity == -1){
				System.out.println(currentCityIndex);
			}
			// Calculate the distance of the remain tour for the current city 
			long prevDistance = (long)Math.ceil(instance.distances(tour[PrevCityIndex],tour[currentCityIndex]));
			remainDistance = remainDistance - prevDistance;

			// Calculate the penalised profit of each item in the current city
			for(int itemNumberInCity = 0; itemNumberInCity < ItemsPerCity; ++ itemNumberInCity){
				
				// Calculate the itemIndex in the items of instance for the current item
				int itemIndex = currentCity+itemNumberInCity*(instance.numberOfCities_-1);//* (this.numberOfNodes-1); 

				// Weight of the current item
				double wc = 0.0d + instance.items[itemIndex][2];
				// Calculate the modified velocity if taking this item
				double V_Diff = instance.maxSpeed_ - wc *(instance.maxSpeed_-instance.minSpeed_)/instance.capacityOfKnapsack_;
				// Calculate the increasing travel time  if taking this item
				double time_Diff = remainDistance / V_Diff - remainDistance / instance.maxSpeed_;
				// Calculate the increasing Rent if taking this item according the time
				double Penalty_profit = time_Diff * instance.rentingRatio_;
				// Modify the proft according to the increasing travel cost if if taking this item
				double processed_profit = instance.items[itemIndex][1] - Penalty_profit;
				penalised_profits[itemIndex] = (double)processed_profit /  (double)instance.items[itemIndex][2];
			}

		}

		return penalised_profits;

	}

	/**
	 * This function is designed for improve the packingPlan from Greedy Algorithm using the referenced Local search algorithm
	 * The Local Search algorithm is RLS adopted from the given Package.
	 * @param instance TTP instance
	 * @param tour TSP tour
	 * @param durationWithoutImprovement termination condition if no improvement reaching this number
	 * @param maxRuntime termination condition if total computation time reaching this number
	 * @param packingPlan the PackiingPlan to be improved
	 * @return bestOne the improved solution
	 */
	public static TTPSolution LocalSearch(TTPInstance instance, int[] tour, int durationWithoutImprovement, int maxRuntime, int[] packingPlan){
		// parameter setup
		int i = 0;
		int counter = 0;
		// setup time clock
		long LS_Start_Time = System.currentTimeMillis();
		
		// setup new packinplan
		int[] newPackingPlan = (int[])DeepCopy.copy(packingPlan);
		TTPSolution bestOne = new TTPSolution(tour, packingPlan);
		// Evaluate current new plan
		instance.evaluate(bestOne);
		while(counter<durationWithoutImprovement) {
			// do the time check just every 50 iterations, as it is time consuming
			if((i % 10) == 0 &&  (System.currentTimeMillis()-LS_Start_Time) > maxRuntime) {
				break;
			}
			// load current plan into container
			newPackingPlan = (int[])DeepCopy.copy(packingPlan);
			
			// start next position flip
			int position = (int)(Math.random()*newPackingPlan.length);
			if (newPackingPlan[position] == 1) {
				newPackingPlan[position] = 0;
			} else {
				newPackingPlan[position] = 1;
			}
			// create new solution
			TTPSolution NextbestOne = new TTPSolution(tour, newPackingPlan);
			// evaluate new solution
			instance.evaluate(NextbestOne);
//			if(NextbestOne.ob > 3E7) {
//				System.out.println("up");
//			}
			if (NextbestOne.ob >= bestOne.ob && NextbestOne.wend >=0 ) {
				// if new solution is valid
				// then copy new solution to current solution
				packingPlan = newPackingPlan;
				bestOne = NextbestOne;
				counter = 0;
			}
			else {
				// if not valid
				// counter increment
				counter++;
			}
			i++;
		}
		return bestOne;

	}
	
	/**
	 * This function is adopted from Local search algorithm
	 * The key point of this method is that the number of mutating position
	 * depends on the size of item number. (0.01% of the total number of items)
	 * The Local Search algorithm is RLS adopted from the given Package()
	 * @param instance
	 * @param tour
	 * @param durationToImprovement
	 * @param maxRuntime
	 * @param packingPlan
	 * @return
	 */
	public static TTPSolution ModifiedLocalSearch(TTPInstance instance, int[] tour, int durationToImprovement, int maxRuntime, int[] packingPlan){
		// parameter setup
		int index = 0;
		int counter = 0;
		// setup time clock
		long LS_Start_Time = System.currentTimeMillis();
		
		// setup current solution
		int[] newPackingPlan = (int[])DeepCopy.copy(packingPlan);
		TTPSolution currentSolution = new TTPSolution(tour, packingPlan); 
		
		// evaluate current solution
		instance.evaluate(currentSolution);
		
		while(true) {
			// do the time check just every 10 iterations, as it is time consuming
			if((index % 10) == 0 &&  (System.currentTimeMillis()-LS_Start_Time) > maxRuntime) {
				break;
			}
			// setup checking length
			double Length = 1;
			int checkLength = (int) Math.ceil(Length);
			// setup restore space
			int[][] tmpStatus = new int[checkLength][2];
			// start flip loop
			for(int i = 0; i < checkLength; ++i) {
				// random pick one bit to flip
				int position = (int)(Math.random()*newPackingPlan.length);
				// store current status of picked position
				tmpStatus[i][0] = position;
				tmpStatus[i][1] = newPackingPlan[position];
				
				// flip the status
				if (newPackingPlan[position] == 1) {
					newPackingPlan[position] = 0;
				} else {
					newPackingPlan[position] = 1;
				}
			}
			
			// check out the new solution objective value
			TTPSolution NextSolution = new TTPSolution(tour, newPackingPlan);
			instance.evaluate(NextSolution);
			if (NextSolution.ob > currentSolution.ob && NextSolution.wend >=0 ) {
				// update new solution
				currentSolution = NextSolution;
			}
			else{
				// roll back to old plan
				// and continue pick next random position
				for(int i = 0; i < checkLength; ++i) {
					newPackingPlan[tmpStatus[i][0]] = tmpStatus[i][1];
				}
			}
			// increment total number of iteration counter
			index++;
		}
		return currentSolution;
	}
	

	/**
	 * This function is adopted from (1+1) EA
	 * The (1+1)EA is EA adopted from the given Package()
	 * @param instance
	 * @param tour
	 * @param durationToImprovement
	 * @param maxRuntime
	 * @param packingPlan
	 * @return
	 */
	public static TTPSolution ModifiedEA(TTPInstance instance, int[] tour, int durationToImprovement, int maxRuntime, int[] packingPlan){
		// parameter setup
		int index = 0;
		int counter = 0;
		// setup time clock
		long LS_Start_Time = System.currentTimeMillis();
		
		// setup current solution
		TTPSolution currentSolution = new TTPSolution(tour, packingPlan); 
		
		// evaluate current solution
		instance.evaluate(currentSolution);
		
		while(true) {
			// do the time check just every 50 iterations, as it is time consuming
			if((index % 10) == 0 &&  (System.currentTimeMillis()-LS_Start_Time) > maxRuntime) {
				break;
			}
			// setup new packing-plan
			int[] newPackingPlan = (int[])DeepCopy.copy(packingPlan);

			// flip the status
            for (int j=0; j<packingPlan.length; j++) {
            	// flip with probability 1/n
                if (Math.random()<1d/packingPlan.length)
                	// check if item available or not
                	if(instance.items[PackingPlan2Item(j,instance,tour)][1] <= 0) {
                		continue;
                	}
                	// flip status based on current status
                    if (newPackingPlan[j] == 0) {
                        newPackingPlan[j] = 1;
                    } else {
                        newPackingPlan[j] = 0;
                    }
            }
			
			// check out the new solution objective value
			TTPSolution NextSolution = new TTPSolution(tour, newPackingPlan);
			instance.evaluate(NextSolution);
			if (NextSolution.ob >= currentSolution.ob && NextSolution.wend >=0 ) {
				// update new solution
				packingPlan = newPackingPlan;
				currentSolution = NextSolution;
			}
			else{
				// roll back to old plan
				// increment improvement false counter
			}
			// increment total number of iteration counter
			index++;
		}
		return currentSolution;
	}
}
