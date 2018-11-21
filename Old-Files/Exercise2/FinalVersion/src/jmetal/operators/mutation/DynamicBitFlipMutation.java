package jmetal.operators.mutation;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.operators.mutation.Mutation;
import jmetal.problems.PWT;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This class implements a Dynamic bit flip mutation operator.
 * If the total weight is over KP capacity, only allow bits flip from 1 to 0
 */
public class DynamicBitFlipMutation extends Mutation {
	/**
	 * Valid solution types to apply this operator 
	 */
	private static final List VALID_TYPES = Arrays.asList(BinarySolutionType.class,
			BinaryRealSolutionType.class,
			IntSolutionType.class) ;
	//the mutation probability
	private Double mutationProbability_ = null ;

	private int dynamicItemFlag;
	private int totalGeneration;
	private int itemSetVersion;


	/**
	 * Constructor
	 * Creates a new instance of the Bit Flip mutation operator
	 */
	public DynamicBitFlipMutation(HashMap<String, Object> parameters) {
		super(parameters) ;
		//set probability
		if (parameters.get("probability") != null)
			mutationProbability_ = (Double) parameters.get("probability") ; 
		//check the dynamic item flag
		if(parameters.get("dynamicItemFlag") != null){
			this.dynamicItemFlag =(int)parameters.get("dynamicItemFlag");
			//if use dynamic item benchmark
			if( this.dynamicItemFlag == 1){
				//check totalGeneration exist
				if(parameters.get("totalGeneration")!= null){
					totalGeneration = (int) parameters.get("totalGeneration");
				}else{
					System.out.println("Cannot find total generation!");
					System.exit(-1);
				}
				//check item set version exist
				if(parameters.get("itemSetVersion")!= null){
					itemSetVersion = (int) parameters.get("itemSetVersion");
				}else{
					System.out.println("Cannot find item set version!");
					System.exit(-1);
				}
			}
		}else{
			System.out.println("Cannot find dynamic item flag!");
			System.exit(-1);
		}

	} // DynamicBitFlipMutation

	/**
	 * Perform the mutation operation
	 * @param probability Mutation probability
	 * @param solution The solution to mutate
	 * @throws JMException
	 */
	public void doMutation(double probability0,double probability1, Solution solution) throws JMException {
		try {
			if ((solution.getType().getClass() == BinarySolutionType.class) ||
					(solution.getType().getClass() == BinaryRealSolutionType.class)) {
				for (int i = 0; i < solution.getDecisionVariables().length; i++) {
					for (int j = 0; j < ((Binary) solution.getDecisionVariables()[i]).getNumberOfBits(); j++) {
						// Check the bit if true or false
						if(((Binary) solution.getDecisionVariables()[i]).bits_.get(j)){
							if (PseudoRandom.randDouble() < probability1) {
								((Binary) solution.getDecisionVariables()[i]).bits_.flip(j);
							}	
						}else{
							// Flip bit according to probability of 0 to 1
							if (PseudoRandom.randDouble() < probability0) {
								((Binary) solution.getDecisionVariables()[i]).bits_.flip(j);
							}	
						}

					}
				}

				for (int i = 0; i < solution.getDecisionVariables().length; i++) {
					((Binary) solution.getDecisionVariables()[i]).decode();
				}
			} // if
			else { // Integer representation
				for (int i = 0; i < solution.getDecisionVariables().length; i++)
					if (PseudoRandom.randDouble() < mutationProbability_) {
						int value = PseudoRandom.randInt(
								(int)solution.getDecisionVariables()[i].getLowerBound(),
								(int)solution.getDecisionVariables()[i].getUpperBound());
						solution.getDecisionVariables()[i].setValue(value);
					} // if
			} // else
		} catch (ClassCastException e1) {
			Configuration.logger_.severe("DynamicBitFlipMutation.doMutation: " +
					"ClassCastException error" + e1.getMessage());
			Class cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".doMutation()");
		}
	} // doMutation

	/**
	 * Executes the operation
	 * @param object An object containing a solution to mutate
	 * @return An object containing the mutated solution
	 * @throws JMException 
	 */
	public Object execute(Object object) throws JMException {
		Solution solution = (Solution) object;
		PWT problem = (PWT) solution.getProblem();
		double probability1 = mutationProbability_;
		double probability0 = mutationProbability_;
		if(solution.getOverallConstraintViolation() < 0){
			probability0 = 0;
		}


		if (!VALID_TYPES.contains(solution.getType().getClass())) {
			Configuration.logger_.severe("DynamicBitFlipMutation.execute: the solution " +
					"is not of the right type. The type should be 'Binary', " +
					"'BinaryReal' or 'Int', but " + solution.getType() + " is obtained");

			Class cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} // if 

		doMutation(probability0,probability1, solution);
		//if use dynamic items
		if(this.dynamicItemFlag == 1){
			try {
				// update the packing plan using the benchmark
				problem.updateCurrentItemsCheckList(solution, totalGeneration, itemSetVersion);
				// check new solution's dynamic item availability
				problem.validateCurrentItems(solution);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return solution;
	} // execute
} // DynamicBitFlipMutation
