package jmetal.operators.mutation;
//  BitFlipMutation.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.



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
 * This class implements a bit flip mutation operator.
 * NOTE: the operator is applied to binary or integer solutions, considering the
 * whole solution as a single encodings.variable.
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
		
	} // BitFlipMutation

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
						if(((Binary) solution.getDecisionVariables()[i]).bits_.get(j)){
							if (PseudoRandom.randDouble() < probability1) {
								((Binary) solution.getDecisionVariables()[i]).bits_.flip(j);
							}	
						}else{
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
			Configuration.logger_.severe("BitFlipMutation.doMutation: " +
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
			Configuration.logger_.severe("BitFlipMutation.execute: the solution " +
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
				//update the packing plan using the benchmark
				problem.updateCurrentItems(solution, totalGeneration, itemSetVersion);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return solution;
	} // execute
} // BitFlipMutation
