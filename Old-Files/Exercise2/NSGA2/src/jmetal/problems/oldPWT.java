package jmetal.problems;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayIntSolutionType;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.solutionType.PermutationSolutionType;
import jmetal.encodings.variable.ArrayInt;
import jmetal.encodings.variable.Binary;
import jmetal.util.JMException;
import ttp.TTPInstance;
import ttp.TTPSolution;
import ttp.Optimisation.Optimisation;

public class oldPWT extends Problem {

	public TTPInstance instance = null;
	public int[] tour = null;


	public oldPWT(String filename){
		this("Binary", filename) ;
	}

	/**
	 * Creates a new TSP problem instance. It accepts data files from TSPLIB
	 * @param filename The file containing the definition of the problem
	 */
	public oldPWT(String solutionType, String filename) {
		numberOfVariables_  = 1;
		numberOfObjectives_ = 2;
		numberOfConstraints_= 1;
		problemName_        = "PWT";

		solutionType_ = new BinarySolutionType(this) ;

		length_       = new int[numberOfVariables_];
		
		lowerLimit_ = new double[numberOfVariables_];
		upperLimit_ = new double[numberOfVariables_];
		
	    for (int var = 0; var < numberOfVariables_; var++){
	        lowerLimit_[var] = 0.0;
	        upperLimit_[var] = 1.0;
	      } //for
		
		try {
			if (solutionType.compareTo("Binary") == 0)
				solutionType_ = new BinarySolutionType(this) ;
			else {
				throw new JMException("Solution type invalid") ;
			}
		} catch (JMException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		try {
			readProblem(filename) ;
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		//	    System.out.println(numberOfCities_) ;
		length_      [0] = instance.numberOfItems; // PackingPlan
//		length_      [1] = instance.numberOfNodes; // Nodes

	} // TSP


	public void readProblem(String filename)throws
	IOException {
		File f = new File(filename);
		instance =  new TTPInstance(f);
		tour = Optimisation.linkernTour(instance);
	}

	@Override
	public void evaluate(Solution solution) throws JMException {
		// TODO Auto-generated method stub
		Binary Variable0 = (Binary)solution.getDecisionVariables()[0];
//		long[] bits = Variable0.bits_
		int[] packingPlan = new int[length_[0]];
		
		for (int i=0; i<Variable0.bits_.length(); ++i)
			if(Variable0.bits_.get(i)){
				packingPlan[i] = 1;
			}
			else{
				packingPlan[i] = 0;
			}
				
		

		TTPSolution ToBeEvaluate = new TTPSolution(tour, packingPlan);
		instance.evaluate(ToBeEvaluate);
//		System.out.println(ToBeEvaluate.ob);
		solution.setObjective(0, -ToBeEvaluate.ob);
		solution.setObjective(1, ToBeEvaluate.wendUsed);

	}
	 /** 
	  * Evaluates the constraint overhead of a solution 
	  * @param solution The solution
	 * @throws JMException 
	  */  
	  public void evaluateConstraints(Solution solution) throws JMException {
	    double [] constraint = new double[this.getNumberOfConstraints()];

	    double x0 = solution.getObjective(0);
	    double x1 = solution.getObjective(1);
//	    double x0 = solution.getDecisionVariables()[0].getValue();
//	    double x1 = solution.getDecisionVariables()[1].getValue();
	    
	    
//	    constraint[0] =  x0 ;
	    constraint[0] =  instance.capacityOfKnapsack - x1;
//	    constraint[0] =  x1;
//	        System.out.println(constraint[0]);
	    double total = 0.0;
	    int number = 0;
	    for (int i = 0; i < this.getNumberOfConstraints(); i++)
	      if (constraint[i]<=0.0){
	        total+=constraint[i];
	        number++;
	      }
	        
	    solution.setOverallConstraintViolation(total);    
	    solution.setNumberOfViolatedConstraint(number);         
	  } // evaluateConstraints  







}
