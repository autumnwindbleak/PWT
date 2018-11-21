import java.io.IOException;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.crossover.GreedyCityCrossover;
import jmetal.operators.crossover.GroupPointCrossover;
import jmetal.operators.mutation.DynamicBitFlipMutation;
import jmetal.operators.mutation.GreedyCityMutation;
import jmetal.operators.mutation.GreedyMutation;
import jmetal.operators.mutation.OneBitFlipMutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.PWT;
import jmetal.problems.mTSP;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.ShowResult;

public class TestMain {

	public static Logger      logger_ ;      // Logger object
	public static FileHandler fileHandler_ ; // FileHandler object

	/**
	 * @param args Command line arguments.
	 * @throws JMException 
	 * @throws IOException 
	 * @throws SecurityException 
	 * Usage: 
	 *      - jmetal.metaheuristics.nsgaII.NSGAII_mTSP_main
	 */
	public static void main(String [] args) throws 
	JMException, 
	SecurityException, 
	IOException, 
	ClassNotFoundException {
		Problem   problem   ; // The problem to solve
		Algorithm algorithm ; // The algorithm to use
		Operator  crossover ; // Crossover operator
		Operator  mutation  ; // Mutation operator
		Operator  selection ; // Selection operator

		HashMap  parameters ; // Operator parameters

		QualityIndicator indicators ; // Object to get quality indicators

		// Logger object and file to store log messages
		logger_      = Configuration.logger_ ;
		fileHandler_ = new FileHandler("NSGAII_main.log"); 
		logger_.addHandler(fileHandler_) ;

		indicators = null ;
//		problem = new PWT("instance/a280_n2790_uncorr_10.ttp");
		problem = new PWT("Binary",args[0], args[10]);

		algorithm = new NSGAII(problem);
		//algorithm = new ssNSGAII(problem);

		// Algorithm parameters
		algorithm.setInputParameter("populationSize",20);
		algorithm.setInputParameter("maxEvaluations",1000);

		/* Crossver operator */
		parameters = new HashMap() ;
		parameters.put("probability", 0.90) ;
		parameters.put("groupSize", 10d) ;
//		parameters.put("parts",3);
//		crossover = CrossoverFactory.getCrossoverOperator("TwoPointsCrossover", parameters);
		crossover = CrossoverFactory.getCrossoverOperator("SinglePointCrossover", parameters);
//		crossover = new GreedyCityCrossover(parameters);
		crossover = new GroupPointCrossover(parameters);

		/* Mutation operator */
		parameters = new HashMap() ;
		parameters.put("probability", 0.01) ;
//		mutation = MutationFactory.getMutationOperator("BitFlipMutation", parameters);   
		mutation = new OneBitFlipMutation(parameters);
//		mutation = new DynamicBitFlipMutation(parameters);
//		mutation = new GreedyMutation(parameters);
//		mutation = new GreedyCityMutation(parameters);
		
		/* Selection Operator */
		parameters = null;
		selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters) ;                            

		// Add the operators to the algorithm
		algorithm.addOperator("crossover",crossover);
		algorithm.addOperator("mutation",mutation);
		algorithm.addOperator("selection",selection);

		// Add the indicator object to the algorithm
		algorithm.setInputParameter("indicators", indicators) ;

		// Execute the Algorithm
		long initTime = System.currentTimeMillis();
		SolutionSet population = algorithm.execute();
		long estimatedTime = System.currentTimeMillis() - initTime;

		   // Result messages 
	    logger_.info("Total execution time: "+estimatedTime + "ms");
	    logger_.info("Variables values have been writen to file VAR");
	    population.printVariablesToFile("VAR");    
	    logger_.info("Objectives values have been writen to file FUN");
	    population.printObjectivesToFile("FUN");
	    ShowResult.showresult(population);
	  
	    if (indicators != null) {
	      logger_.info("Quality indicators") ;
	      logger_.info("Hypervolume: " + indicators.getHypervolume(population)) ;
	      logger_.info("GD         : " + indicators.getGD(population)) ;
	      logger_.info("IGD        : " + indicators.getIGD(population)) ;
	      logger_.info("Spread     : " + indicators.getSpread(population)) ;
	      logger_.info("Epsilon    : " + indicators.getEpsilon(population)) ;  
	     
	      int evaluations = ((Integer)algorithm.getOutputParameter("evaluations")).intValue();
	      logger_.info("Speed      : " + evaluations + " evaluations") ;      
	    } // if
	} //main
} // NSGAII_main


