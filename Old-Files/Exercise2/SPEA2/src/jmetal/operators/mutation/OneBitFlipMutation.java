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

public class OneBitFlipMutation extends Mutation {
	/**
	 * Valid solution types to apply this operator 
	 */
	private static final List VALID_TYPES = Arrays.asList(BinarySolutionType.class) ;

	private Double mutationProbability_ = null ;

	private int dynamicItemFlag;
	private int totalGeneration;
	private int itemSetVersion;
	/**
	 * Constructor
	 * Creates a new instance of the Bit Flip mutation operator
	 */
	public OneBitFlipMutation(HashMap<String, Object> parameters) {
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
	} // BitFlipMutation


	/**
	 * Perform the mutation operation
	 * @param probability Mutation probability
	 * @param solution The solution to mutate
	 * @throws JMException
	 */
	public void doMutation(double probability, Solution solution) throws JMException {
		try {
			if ((solution.getType().getClass() == BinarySolutionType.class) ||
					(solution.getType().getClass() == BinaryRealSolutionType.class)) {
				
				double randomnumber = PseudoRandom.randDouble();
				while(randomnumber < probability){
					
					for (int i = 0; i < solution.getDecisionVariables().length; i++) {
						int index = (int) (PseudoRandom.randDouble() * ((Binary)solution.getDecisionVariables()[i]).getNumberOfBits());
							((Binary) solution.getDecisionVariables()[i]).bits_.flip(index);
					}
					randomnumber = PseudoRandom.randDouble();
				}


				for (int i = 0; i < solution.getDecisionVariables().length; i++) {
					((Binary) solution.getDecisionVariables()[i]).decode();
				}
			} // if
			
		} catch (ClassCastException e1) {
			Configuration.logger_.severe("OneBitFlipMutation.doMutation: " +
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
	@Override
	public Object execute(Object object) throws JMException {
		Solution solution = (Solution) object;

		if (!VALID_TYPES.contains(solution.getType().getClass())) {
			Configuration.logger_.severe("OneBitFlipMutation.execute: the solution " +
					"is not of the right type. The type should be 'Binary', " +
					"'BinaryReal' or 'Int', but " + solution.getType() + " is obtained");

			Class cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} // if 

		doMutation(mutationProbability_, solution);
		
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
