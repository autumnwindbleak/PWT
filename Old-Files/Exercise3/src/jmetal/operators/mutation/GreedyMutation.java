package jmetal.operators.mutation;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

/**
 * This class implements a Greedy bit flip mutation operator.
 * If no positive profit can be gained from a bit, the bit will be set to false
 */
public class GreedyMutation extends Mutation {

	/**
	 * Valid solution types to apply this operator 
	 */
	private static final List VALID_TYPES = Arrays.asList(BinarySolutionType.class,
			BinaryRealSolutionType.class,
			IntSolutionType.class) ;

	private Double mutationProbability_ = null ;
	
	private int dynamicItemFlag;
	private int totalGeneration;
	private int itemSetVersion;
	/**
	 * Constructor
	 * Creates a new instance of the GreedyMutation mutation operator
	 */
	public GreedyMutation(HashMap<String, Object> parameters) {
		super(parameters) ;
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
	} // GreedyMutation

	/**
	 * Perform the mutation operation
	 * @param probability Mutation probability
	 * @param solution The solution to mutate
	 * @throws JMException
	 */
	public void doMutation(double probability0, double probability1, Solution solution) throws JMException {
		try {
			if ((solution.getType().getClass() == BinarySolutionType.class) ||
					(solution.getType().getClass() == BinaryRealSolutionType.class)) {

				PWT problem = (PWT)solution.getProblem();
				double[] penalised_profits;
				try {
					// Calculate penalty profit according to the weight and increasing Rent
					penalised_profits = MyOperators.PenaltyFunction(problem, problem.tour);



				for (int i = 0; i < solution.getDecisionVariables().length; i++) {
					for (int j = 0; j < ((Binary) solution.getDecisionVariables()[i]).getNumberOfBits(); j++) {


						// mutation main body
						if(((Binary) solution.getDecisionVariables()[i]).bits_.get(j)){
							if(penalised_profits[j] >= 0){
								if (PseudoRandom.randDouble() < probability1) {
									((Binary) solution.getDecisionVariables()[i]).bits_.flip(j);
								}	
								
							}else{ // if no positive profit can be gained from a bit, then set to false
								((Binary) solution.getDecisionVariables()[i]).bits_.flip(j);
							}
						}else{
							if(penalised_profits[j] >= 0){
								if (PseudoRandom.randDouble() < probability0) {
									((Binary) solution.getDecisionVariables()[i]).bits_.flip(j);
								}	
							}

						}

					}

					((Binary) solution.getDecisionVariables()[i]).decode();
				}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} // if

		} catch (ClassCastException e1) {
			Configuration.logger_.severe("GreedyMutation.doMutation: " +
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
		
		double probability1 = mutationProbability_;
		double probability0 = mutationProbability_;
		if(solution.getOverallConstraintViolation() < 0){
			probability0 = 0;
		}


		if (!VALID_TYPES.contains(solution.getType().getClass())) {
			Configuration.logger_.severe("GreedyMutation.execute: the solution " +
					"is not of the right type. The type should be 'Binary', " +
					"'BinaryReal' or 'Int', but " + solution.getType() + " is obtained");

			Class cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} // if 

		doMutation(probability0,probability1, solution);
		
		PWT problem = (PWT) solution.getProblem();
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

}
