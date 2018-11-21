package jmetal.operators.crossover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.problems.PWT;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import ttp.Optimisation.MyOperators;

public class GreedyCityCrossover extends Crossover {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4783018796230872569L;





	ArrayList<int[]> currentPermutations = null;

	/**
	 * Valid solution types to apply this operator 
	 */
	private static final List VALID_TYPES = Arrays.asList(BinarySolutionType.class,
			BinaryRealSolutionType.class,
			IntSolutionType.class) ;

	private Double crossoverProbability_ = null;

	private int parts_ = 3;
	/**
	 * Constructor
	 * Creates a new instance of the single point crossover operator
	 */
	//public SinglePointCrossover(Properties properties) {
	//    this();
	//} // SinglePointCrossover

	/**
	 * Constructor
	 * Creates a new instance of the single point crossover operator
	 */
	public GreedyCityCrossover(HashMap<String, Object> parameters) {
		super(parameters);
		if (parameters.get("probability") != null)
			crossoverProbability_ = (Double) parameters.get("probability") ; 
		//		if (parameters.get("parts") != null)
		//			parts_ = (int) parameters.get("parts") ; 
	}
	/**
	 * Perform the crossover operation.
	 * @param probability Crossover probability
	 * @param parent1 The first parent
	 * @param parent2 The second parent   
	 * @return An array containig the two offsprings
	 * @throws JMException
	 */
	public Solution[] doCrossover(double probability,
			Solution parent1,
			Solution parent2) throws JMException {
		Solution[] offSpring = new Solution[2];
		offSpring[0] = new Solution(parent1);
		offSpring[1] = new Solution(parent2);
		try {
			if (PseudoRandom.randDouble() < probability) {
				if ((parent1.getType().getClass() == BinarySolutionType.class) ||
						(parent1.getType().getClass() == BinaryRealSolutionType.class)) {
					//1. Compute the total number of bits
					int totalNumberOfBits = 0;
					for (int i = 0; i < parent1.getDecisionVariables().length; i++) {
						totalNumberOfBits +=
								((Binary) parent1.getDecisionVariables()[i]).getNumberOfBits();

					}
					// Get packingPlan from parents\
					PWT problem = (PWT)parent1.getProblem();
					int numberofitems = parent1.getProblem().getLength(0);
					//					int numberofNodes = parent1.getProblem().getLength(1);
					int numberofNodes = problem.numberOfCities_;
					int[] packingplan1 = new int[numberofitems];
					int[] packingplan2 = new int[numberofitems];
					BitSet p1_bits = ((Binary)parent1.getDecisionVariables()[0]).bits_;
					BitSet p2_bits = ((Binary)parent2.getDecisionVariables()[0]).bits_;
					for (int i=0; i<p1_bits.length(); ++i){
						if(p1_bits.get(i)){
							packingplan1[i] = 1;
						}
						else{
							packingplan1[i] = 0;
						}
						if(p2_bits.get(i)){
							packingplan2[i] = 1;
						}
						else{
							packingplan2[i] = 0;
						}
					}


					//2. Calculate the point to make the crossover\
					int crossoverPoint = PseudoRandom.randInt(0, totalNumberOfBits - 1);
					//					int numberOfcities = parent1.getProblem().getLength(1);
					int crossoverPoint1 = PseudoRandom.randInt(1, numberofNodes - 1);
					int crossoverPoint2 = PseudoRandom.randInt(1, numberofNodes - 1);
					while((Math.abs(crossoverPoint1 - crossoverPoint2) + 1) % parts_ != 0){
						crossoverPoint1 = PseudoRandom.randInt(1, numberofNodes - 2);
						crossoverPoint2 = PseudoRandom.randInt(1, numberofNodes - 2);
					}
					if(crossoverPoint1 > crossoverPoint2){
						int temp = crossoverPoint1;
						crossoverPoint1 = crossoverPoint2;
						crossoverPoint2 = temp;
					}


					double[] penalised_profits;
					try {
						penalised_profits = MyOperators.PenaltyFunction(problem, problem.tour);


						// Get city sets
						ArrayList<int[]> tourset = toursets(numberofNodes, crossoverPoint1,crossoverPoint2, parts_, problem.tour);

						//					int indexOfPackingPlan = (i-1)*ItemsPerCity+itemNumber;
						//					// output packingplan item index
						//					int PPIndex = indexOfPackingPlan;

						int ItemsPerCity = numberofitems / (numberofNodes-1);
						int[] Value_Permutation = new int[currentPermutations.size()];
						for(int i=0; i < currentPermutations.size(); i++){
							int currentResult = 0;
							int[] currentP = currentPermutations.get(i);
							for(int j = 0; j < currentP.length; j ++){ // Parts
								int[] currenttourSet = tourset.get((currentP[j]-1) % parts_);
								int currentValueOfSet = 0;

								for(int p = 0; p < currenttourSet.length; p++){ // tour Cities
									//								int indexOfPackingPlan = (i-1)*ItemsPerCity+itemNumber;

									for (int k = 0; k < ItemsPerCity; k++){ //items
										int indexOfPackingPlan = (currenttourSet[p]-1)*ItemsPerCity+k;
										//									currentValueOfSet += penalised_profits[indexOfPackingPlan];								
										int adjustIndex = crossoverPoint1 - 1 +  j * (currenttourSet.length) + p*ItemsPerCity + k;
										//									System.out.println(adjustIndex);
										if (indexOfPackingPlan == -1 || indexOfPackingPlan == penalised_profits.length){
											System.out.println(currenttourSet[p]);
										}
										if (adjustIndex == -1 || adjustIndex == penalised_profits.length){
											System.out.println(currenttourSet[p]);
										}
										if(currentP[j] < parts_){
											if (packingplan1[indexOfPackingPlan] == 1){
												currentValueOfSet += penalised_profits[adjustIndex];
											}

										}
										else{
											if (packingplan2[indexOfPackingPlan] == 1){
												currentValueOfSet += penalised_profits[adjustIndex];
											}

										}
									}// items

								}// tour Cities
								currentResult += currentValueOfSet;

							}// Parts
							Value_Permutation[i] = currentResult; 
						}

						class Value_Comparator implements Comparator<Integer> {
							@Override
							public int compare(Integer i2, Integer i1) {
								return new Integer(Value_Permutation[i1.intValue()]).compareTo(new Integer(Value_Permutation[i2.intValue()]));
							}
						}

						Integer[] tempindex = new Integer[Value_Permutation.length]; 
						for(int i=0; i<Value_Permutation.length; i++){
							tempindex[i] = i;
						}
						Arrays.sort(tempindex, new Value_Comparator());
						//					Collections.sort(currentPermutations, new Comparator<Integer>() {
						//					    public int compare(Integer left, Integer right) {
						//					        return Integer.compare(new Integer(Value_Permutation[left]), new Integer(Value_Permutation[right]));
						//					    }
						//					});
						int[] Winner_set1 = currentPermutations.get(tempindex[0]); 
						int[] Winner_set2 = currentPermutations.get(tempindex[1]); 
						int flag1 = checkWinner(Winner_set1, parts_);
						int flag2 = checkWinner(Winner_set1, parts_);







						//3. Compute the encodings.variable containing the crossoverPoint bit
						int variable = 0;
						int acountBits =
								((Binary) parent1.getDecisionVariables()[variable]).getNumberOfBits();

						while (acountBits < (crossoverPoint + 1)) {
							variable++;
							acountBits +=
									((Binary) parent1.getDecisionVariables()[variable]).getNumberOfBits();
						}

						//4. Compute the bit into the selected encodings.variable
						//					int diff = acountBits - crossoverPoint;
						//					int intoVariableCrossoverPoint =
						//							((Binary) parent1.getDecisionVariables()[variable]).getNumberOfBits() - diff ;

						//5. Make the crossover into the gene;
						Binary offSpring1, offSpring2;
						offSpring1 =
								(Binary) parent1.getDecisionVariables()[variable].deepCopy();
						offSpring2 =
								(Binary) parent2.getDecisionVariables()[variable].deepCopy();




						// Make the crossover into the gene;
						if (flag1+flag2 > 0){

							for(int j = 0; j < Winner_set1.length; j ++){ // Parts
								int[] currenttourSet1 = tourset.get((Winner_set1[j] - 1) % parts_);
								int[] currenttourSet2 = tourset.get((Winner_set2[j] - 1) % parts_);
								for(int p = 0; p < currenttourSet1.length; p++){ // tour Cities
									//							int indexOfPackingPlan = (i-1)*ItemsPerCity+itemNumber;

									for (int k = 0; k < ItemsPerCity; k++){ //items
										int indexOfPackingPlan1 = (currenttourSet1[p]-1)*ItemsPerCity+k;
										int indexOfPackingPlan2 = (currenttourSet2[p]-1)*ItemsPerCity+k;
										int adjustIndex = crossoverPoint1 -1 +  j * (currenttourSet1.length) + p*ItemsPerCity + k;
										//								System.out.println(adjustIndex);
										if (indexOfPackingPlan1 == -1 || indexOfPackingPlan1 == penalised_profits.length || indexOfPackingPlan2 == -1 || indexOfPackingPlan2 == penalised_profits.length){
											System.out.println(currenttourSet1[p]);
										}
										if (adjustIndex == -1 || adjustIndex == penalised_profits.length){
											System.out.println(currenttourSet1[p]);
										}
										if(Winner_set1[j] < parts_){
											packingplan1[adjustIndex] = 0;
											if (packingplan1[indexOfPackingPlan1] == 1){
												//										currentValueOfSet += penalised_profits[adjustIndex];
												packingplan1[adjustIndex] = 1;
											}

										}
										else{
											packingplan1[adjustIndex] = 0;
											if (packingplan2[indexOfPackingPlan1] == 1){
												packingplan1[adjustIndex] = 1;
											}

										}
										if(Winner_set2[j] < parts_){
											packingplan2[adjustIndex] = 0;
											if (packingplan1[indexOfPackingPlan2] == 1){
												//										currentValueOfSet += penalised_profits[adjustIndex];
												packingplan2[adjustIndex] = 1;
											}

										}
										else{
											packingplan2[adjustIndex] = 0;
											if (packingplan2[indexOfPackingPlan2] == 1){
												packingplan2[adjustIndex] = 1;
											}

										}
									}// items

								}// tour Cities

							}// Parts
							//	
						}else{
							for (int i = crossoverPoint1-1;
									i <= crossoverPoint2-1 ;
									i++) {
								int swap = packingplan1[i];
								packingplan1[i] = packingplan2[i];
								packingplan1[i] = swap;
							}
						}



						for (int i =0; i < packingplan2.length; i++){

							offSpring1.bits_.set(i, (packingplan1[i]==1));
							offSpring2.bits_.set(i, (packingplan2[i]==1));
						}


						offSpring[0].getDecisionVariables()[variable] = offSpring1;
						offSpring[1].getDecisionVariables()[variable] = offSpring2;

						//6. Apply the crossover to the other variables
						for (int i = 0; i < variable; i++) {
							offSpring[0].getDecisionVariables()[i] =
									parent2.getDecisionVariables()[i].deepCopy();

							offSpring[1].getDecisionVariables()[i] =
									parent1.getDecisionVariables()[i].deepCopy();

						}

						//7. Decode the results
						for (int i = 0; i < offSpring[0].getDecisionVariables().length; i++) {
							((Binary) offSpring[0].getDecisionVariables()[i]).decode();
							((Binary) offSpring[1].getDecisionVariables()[i]).decode();
						}
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} // Binary or BinaryReal

			}
		} catch (ClassCastException e1) {
			Configuration.logger_.severe("SinglePointCrossover.doCrossover: Cannot perfom " +
					"SinglePointCrossover");
			Class cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".doCrossover()");
		}
		return offSpring;
	} // doCrossover

	/**
	 * Executes the operation
	 * @param object An object containing an array of two solutions
	 * @return An object containing an array with the offSprings
	 * @throws JMException
	 */
	public Object execute(Object object) throws JMException {
		Solution[] parents = (Solution[]) object;


		if (!(VALID_TYPES.contains(parents[0].getType().getClass())  &&
				VALID_TYPES.contains(parents[1].getType().getClass())) ) {

			Configuration.logger_.severe("SinglePointCrossover.execute: the solutions " +
					"are not of the right type. The type should be 'Binary' or 'Int', but " +
					parents[0].getType() + " and " +
					parents[1].getType() + " are obtained");

			Class cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} // if

		if (parents.length < 2) {
			Configuration.logger_.severe("SinglePointCrossover.execute: operator " +
					"needs two parents");
			Class cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} 

		currentPermutations = new ArrayList<>();
		getCurrentPermutations();

		Solution[] offSpring;
		offSpring = doCrossover(crossoverProbability_,
				parents[0],
				parents[1]);

		//-> Update the offSpring solutions
		for (int i = 0; i < offSpring.length; i++) {
			offSpring[i].setCrowdingDistance(0.0);
			offSpring[i].setRank(0);
		}
		return offSpring;
	} // execute

	public void getCurrentPermutations(){
		int[] array = new int[parts_];
		permutations(array, 0, parts_*2, parts_);
	}
	public ArrayList<int []> toursets(int numbeofcities, int r1, int r2, int n,int[] tour){
		ArrayList<int[]> sets = new ArrayList<>();
		//		int[] cities = new int[numbeofcities-1];
		int length = r2 - r1 + 1;
		int gap = length / n  ;
		for (int i = 0; i < n; i++){
			int[] sub = Arrays.copyOfRange(tour, r1+((gap)*i), r1+(gap*(i+1)));
			sets.add(sub);
		}

		//		int[] sub1 = Arrays.copyOfRange(cities, r1, r1+gap);
		//		int[] sub2 = Arrays.copyOfRange(cities, r1+gap+1, r1+gap+1+gap);
		//		int[] sub3 = Arrays.copyOfRange(cities, r1+gap+1+gap+1, r2);
		//		sets.add(sub1);
		//		sets.add(sub2);
		//		sets.add(sub3);
		return sets;

	}

	public void permutations(int[] array, int now, int m, int n){
		if (now >= n){
			currentPermutations.add(array);
			return;
		}
		else{
			for (int start = 1; start <= m; start++){
				array[now] = start;
				int[] newarr = array.clone();
				if (duplicate(newarr, now)){ // 判断是否出现重复
					permutations(newarr, now + 1, m, n);
				}
			}
		}
	}

	public boolean duplicate(int[] array, int now){
		for (int i = 0; i <= now; i++){
			for (int j = i + 1; j <= now; j++){
				if (array[i] == array[j]){
					return false;
				}
			}
		}
		return true;
	}

	public int checkWinner(int[] winner, int n){

		int flag1 = 1;
		int flag2 = 1;
		for(int i =0; i < n ;i ++){

			if (winner[i] != i+1 ){
				flag1 = 0;
			}
			if (winner[i] != i+1+n ){
				flag2 = 0;
			}

		}
		return flag1+flag2;

	}







} // SinglePointCrossover





