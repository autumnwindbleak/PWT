package jmetal.operators.crossover;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 * This class allows to apply a Arithmetic crossover operator using two parent
 * solutions.
 * Some arithmetic operation is performed to two pareents for new offsprings
 */
public class ArithmeticCrossover extends Crossover {

	/**
	   * Valid solution types to apply this operator 
	   */
	  private static final List VALID_TYPES = Arrays.asList(BinarySolutionType.class,
	  		                                            BinaryRealSolutionType.class) ;

	  private Double probability_ = null ;
	  /**
	   * Constructor
	   * Create a new instance of the HUX crossover operator.
	   */
	  public ArithmeticCrossover(HashMap<String, Object> parameters) {
	    super(parameters) ;
	    
	  	if (parameters.get("probability") != null)
	  		probability_ = (Double) parameters.get("probability") ;  		
	  } // ArithmeticCrossover





	  /**
	   * Perform the crossover operation
	   * @param probability Crossover probability
	   * @param parent1 The first parent
	   * @param parent2 The second parent
	   * @return An array containing the two offsprings
	   * @throws JMException 
	   */
	  public Solution[] doCrossover(double   probability, 
	                                Solution parent1, 
	                                Solution parent2) throws JMException {
	    Solution [] offSpring = new Solution[2];
	    offSpring[0] = new Solution(parent1);
	    offSpring[1] = new Solution(parent2);
	    try {         
	      if (PseudoRandom.randDouble() < probability) {
	        for (int var = 0; var < parent1.getDecisionVariables().length; var++) {
	          Binary p1 = (Binary)parent1.getDecisionVariables()[var];
	          Binary p2 = (Binary)parent2.getDecisionVariables()[var];

	          //Perform arithmetic operations on each bit for new offsprings 
	          for (int bit = 0; bit < p1.getNumberOfBits(); bit++) {
	        	  boolean bit1 = p1.bits_.get(bit);
	        	  boolean bit2 = p2.bits_.get(bit);
	        	  
	                ((Binary)offSpring[0].getDecisionVariables()[var])
	                .bits_.set(bit,(bit1&&bit2));
	                ((Binary)offSpring[1].getDecisionVariables()[var])
	                .bits_.set(bit,!(bit1&&bit2));
	          }
	        }  
	        //7. Decode the results
	        for (int i = 0; i < offSpring[0].getDecisionVariables().length; i++)
	        {
	          ((Binary)offSpring[0].getDecisionVariables()[i]).decode();
	          ((Binary)offSpring[1].getDecisionVariables()[i]).decode();
	        }
	      }          
	    }catch (ClassCastException e1) {
	      
	      Configuration.logger_.severe("ArithmeticCrossover.doCrossover: Cannot perfom " +
	          " ") ;
	      Class cls = java.lang.String.class;
	      String name = cls.getName(); 
	      throw new JMException("Exception in " + name + ".doCrossover()") ;
	    }        
	    return offSpring;                                                                                      
	  } // doCrossover

	  
	  /**
	  * Executes the operation
	  * @param object An object containing an array of two solutions 
	  * @return An object containing the offSprings
	  */
	  public Object execute(Object object) throws JMException {
	    Solution [] parents = (Solution [])object;
	    
	    if (parents.length < 2)
	    {
	      Configuration.logger_.severe("ArithmeticCrossover.execute: operator needs two " +
	          "parents");
	      Class cls = java.lang.String.class;
	      String name = cls.getName(); 
	      throw new JMException("Exception in " + name + ".execute()") ;      
	    }

	    if (!(VALID_TYPES.contains(parents[0].getType().getClass())  &&
	        VALID_TYPES.contains(parents[1].getType().getClass())) ) {

	      Configuration.logger_.severe("ArithmeticCrossover.execute: the solutions " +
	          "are not of the right type. The type should be 'Binary' of " +
	          "'BinaryReal', but " +
	          parents[0].getType() + " and " + 
	          parents[1].getType() + " are obtained");

	      Class cls = java.lang.String.class;
	      String name = cls.getName(); 
	      throw new JMException("Exception in " + name + ".execute()") ;

	    } // if 
	        
	    Solution [] offSpring = doCrossover(probability_,
	                                        parents[0],
	                                        parents[1]);
	    
	    for (int i = 0; i < offSpring.length; i++)
	    {
	      offSpring[i].setCrowdingDistance(0.0);
	      offSpring[i].setRank(0);
	    } 
	    
	    return offSpring;
	  } // execute


}