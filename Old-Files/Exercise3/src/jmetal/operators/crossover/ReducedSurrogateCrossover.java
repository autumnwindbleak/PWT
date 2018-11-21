package jmetal.operators.crossover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 * This class allows to apply a Reduced Surrogate Crossover operator using two parent
 * solutions.
 * Reduced Surrogate Crossover minimizes the unwanted crossover operations in case of the parents having same genes. 
 */
public class ReducedSurrogateCrossover extends Crossover {

	  /**
	   * Valid solution types to apply this operator 
	   */
	  private static final List VALID_TYPES = Arrays.asList(BinarySolutionType.class,
	  		                                            BinaryRealSolutionType.class,
	  		                                            IntSolutionType.class) ;

	  private Double crossoverProbability_ = null;

	  /**
	   * Constructor
	   * Creates a new instance of the single point crossover operator
	   */
	  public ReducedSurrogateCrossover(HashMap<String, Object> parameters) {
	  	super(parameters) ;
	  	if (parameters.get("probability") != null)
	  		crossoverProbability_ = (Double) parameters.get("probability") ;  		
	  } // ReducedSurrogateCrossover


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
	                    ((Binary) parent1.getDecisionVariables()[0]).getNumberOfBits();
	          
	                    
	          // Extract the genes of the both parents are different.
	          ArrayList<Boolean> subset1 = new ArrayList<>();
	          ArrayList<Boolean> subset2 = new ArrayList<>();
	          ArrayList<Integer> subsetIndex = new ArrayList<>();
	          for(int i =0 ; i<  totalNumberOfBits; i++){
	        	  if (!( ((Binary) parent1.getDecisionVariables()[0]).bits_.get(i) &&  ((Binary) parent2.getDecisionVariables()[0]).bits_.get(i))){
	        		  subset1.add(((Binary) parent1.getDecisionVariables()[0]).bits_.get(i));
	        		  subset2.add(((Binary) parent2.getDecisionVariables()[0]).bits_.get(i));
	        		  subsetIndex.add(i);
	        	  }
	          }
	          
	          //2. Randomise the point to make the crossover
	          int crossoverPoint = PseudoRandom.randInt(1, subset2.size() - 2);
	          
	          //3. Make the crossover into the subset;
	          Binary offSpring1, offSpring2;
	          offSpring1 =
	                  (Binary) parent1.getDecisionVariables()[0].deepCopy();
	          offSpring2 =
	                  (Binary) parent2.getDecisionVariables()[0].deepCopy();

	          for (int i = crossoverPoint;
	                  i < subset2.size();
	                  i++) {
	            boolean swap = subset1.get(i);
	            subset1.set(i, subset2.get(i));
	            subset2.set(i, swap);
	          }
	          
	          // 4. Edit the bits according to the subsets
	          for( int i = 0; i < subsetIndex.size(); i++){
	        	  offSpring1.bits_.set(subsetIndex.get(i), subset1.get(i));
	        	  offSpring2.bits_.set(subsetIndex.get(i), subset2.get(i));
	          }

	          offSpring[0].getDecisionVariables()[0] = offSpring1;
	          offSpring[1].getDecisionVariables()[0] = offSpring2;

	          //6. Apply the crossover to the other variables
	          for (int i = 0; i < 1; i++) {
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
	        } // Binary or BinaryReal
	        else { // Integer representation
	          int crossoverPoint = PseudoRandom.randInt(0, parent1.numberOfVariables() - 1);
	          int valueX1;
	          int valueX2;
	          for (int i = crossoverPoint; i < parent1.numberOfVariables(); i++) {
	            valueX1 = (int) parent1.getDecisionVariables()[i].getValue();
	            valueX2 = (int) parent2.getDecisionVariables()[i].getValue();
	            offSpring[0].getDecisionVariables()[i].setValue(valueX2);
	            offSpring[1].getDecisionVariables()[i].setValue(valueX1);
	          } // for
	        } // Int representation
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

	      Configuration.logger_.severe("ReducedSurrogateCrossover.execute: the solutions " +
	              "are not of the right type. The type should be 'Binary' or 'Int', but " +
	              parents[0].getType() + " and " +
	              parents[1].getType() + " are obtained");

	      Class cls = java.lang.String.class;
	      String name = cls.getName();
	      throw new JMException("Exception in " + name + ".execute()");
	    } // if

	    if (parents.length < 2) {
	      Configuration.logger_.severe("ReducedSurrogateCrossover.execute: operator " +
	              "needs two parents");
	      Class cls = java.lang.String.class;
	      String name = cls.getName();
	      throw new JMException("Exception in " + name + ".execute()");
	    } 
	    
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

}
