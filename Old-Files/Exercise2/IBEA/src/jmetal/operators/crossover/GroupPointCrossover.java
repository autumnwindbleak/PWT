package jmetal.operators.crossover;
/*
 * GroupPointCrossover.java
 *
 * Author: Puzhi Yao <a1205593@student.adelaide.edu.au>
 * 
 * Group Points Crossover will regroup items into multiple small groups based
 * on given size and each sub-group will have different crossover possibilities.
 */



import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * This class allows to apply a Group Points crossover operator using two parent
 * solutions.
 */
public class GroupPointCrossover extends Crossover {
	/*
	 * Valid solution types to apply this operator 
	 */
	private static final List VALID_TYPES = Arrays.asList(BinarySolutionType.class) ;
	/*
	 * crossover probability
	 */
	private Double crossoverProbability_ = null;
	/*
	 * size of item sub-group
	 */
	private Double groupSize = null;

	/**
	 * Constructor
	 * Creates a new instance of the group points crossover operator
	 */
	public GroupPointCrossover(HashMap<String, Object> parameters) {
		// read parameters from object
		super(parameters) ;
		// setup operator parameters
		if (parameters.get("probability") != null)
		{
			crossoverProbability_ = (Double) parameters.get("probability") ;  
		}
		if (parameters.get("groupSize") != null)
		{
			groupSize = (Double) parameters.get("groupSize");
		}
	} // GroupPointCrossover

	/**
	 * Perform the crossover operation.
	 * @param probability Crossover probability
	 * @param parent1 The first parent
	 * @param parent2 The second parent   
	 * @return An array containig the two offsprings
	 * @throws JMException
	 */
	public Solution[] doCrossover(Double groupSize, double probability, Solution parent1, Solution parent2) throws JMException {
		// setup containers for two parent solutions
		Solution[] offSpring = new Solution[2];
		offSpring[0] = new Solution(parent1);
		offSpring[1] = new Solution(parent2);
		// start crossover dice
		try {
			if (PseudoRandom.randDouble() < probability) {
				//1. Compute the total number of bits
				int totalNumberOfBits = 0;
				for (int i = 0; i < parent1.getDecisionVariables().length; i++) {
					totalNumberOfBits += ((Binary) parent1.getDecisionVariables()[i]).getNumberOfBits();
				}

				//2. check groupSize and totalNumber of bits
				// totalNumber must be divided with no remainder
				if (totalNumberOfBits % groupSize != 0) {
					throw new JMException("groupSize must be totalNumberOfBits/Integer");
				}
				// setup number of groups
				Double numberOfGroups = totalNumberOfBits/groupSize;
				// setup crossover rate of each subgroup
				Vector<Double> crossoverRate = new Vector<Double>();
				// setup total probability container
				Double totProb = 0d;
				// setup increment probability container
				Double preProb = 0d;
				for(int i = 0; i < numberOfGroups; ++i) {
					// calculate current Probability
					Double currentProb = preProb + 1d;
					// store in vector container
					crossoverRate.add(currentProb+totProb);
					// setup previous probability to current
					preProb = currentProb;
					// increment total probability
					totProb = totProb + currentProb;
				}
				// normalize probability to 1
				for(int i = 0; i < numberOfGroups; ++i) {
					// calculate current Probability
					Double tmpProb = crossoverRate.get(i)/totProb;
					crossoverRate.set(i, tmpProb);
				}

				//2. Calculate the group to make the crossover
				Double tmpGroupProb = PseudoRandom.randDouble();
				// setup group counter
				int crossoverGroup = 0;
				// check probability in which group range
				for(int i = 0; i < numberOfGroups; ++i) {
					if (crossoverRate.get(i) >= tmpGroupProb)
					{
						break;
					}
					// increment group counter
					crossoverGroup++;
				}
				int crossoverPoint = 10;
				//3. Setup group crossover start point and end point
				int variable = 0;
				int groupStart = (int) (crossoverGroup * groupSize);
				int groupEnd = (int) ((crossoverGroup + 1) * groupSize);

				//4. Make the crossover into the gene;
				Binary offSpring1, offSpring2;
				// copy the parent values to new children
				offSpring1 = (Binary) parent1.getDecisionVariables()[variable].deepCopy();
				offSpring2 = (Binary) parent2.getDecisionVariables()[variable].deepCopy();

				// start swap genes between two parents
				for (int i = groupStart; i < groupEnd; ++i) {
					boolean swap = offSpring1.bits_.get(i);
					offSpring1.bits_.set(i, offSpring2.bits_.get(i));
					offSpring2.bits_.set(i, swap);
				}
				// copy the value to offspring container
				offSpring[0].getDecisionVariables()[variable] = offSpring1;
				offSpring[1].getDecisionVariables()[variable] = offSpring2;

				//5. Decode the results
				for (int i = 0; i < offSpring[0].getDecisionVariables().length; i++) {
					((Binary) offSpring[0].getDecisionVariables()[i]).decode();
					((Binary) offSpring[1].getDecisionVariables()[i]).decode();
				}
			}
		} catch (ClassCastException e1) {
			// output exception error string
			Configuration.logger_.severe("SinglePointCrossover.doCrossover: Cannot perfom " + "SinglePointCrossover");
			Class cls = java.lang.String.class;
			// setup output string
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
		// setup parent container
		Solution[] parents = (Solution[]) object;

		// check if input solution type is valid
		if (!(VALID_TYPES.contains(parents[0].getType().getClass())  &&
				VALID_TYPES.contains(parents[1].getType().getClass())) ) {
			// setup output string
			Configuration.logger_.severe("GroupPointCrossover.execute: the solutions " +
					"are not of the right type. The type should be 'Binary' , but " +
					parents[0].getType() + " and " +
					parents[1].getType() + " are obtained");
			// setup output string
			Class cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} // if
		// check if there are enough number of parents
		if (parents.length < 2) {
			Configuration.logger_.severe("GroupPointCrossover.execute: operator " +
					"needs two parents");
			Class cls = java.lang.String.class;
			// setup output string
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} 
		// perform crossover function
		Solution[] offSpring;
		offSpring = doCrossover(groupSize,crossoverProbability_, parents[0], parents[1]);

		//-> Update the offSpring solutions
		for (int i = 0; i < offSpring.length; i++) {
			offSpring[i].setCrowdingDistance(0.0);
			offSpring[i].setRank(0);
		}
		return offSpring;
	} // execute
} // GroupPointCrossover
