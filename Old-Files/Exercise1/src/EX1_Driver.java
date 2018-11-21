/*	PWT_Driver.java
 * 
 * 	Authors: Puzhi Yao
 * 			 Xueyang Wang
 * 			 Zhuoying Li
 * 			 Jingwen Wei
 * 
 *  This file is adopted from Antonio J. Nebro, Juan J. Durillo
 */

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.ibea.IBEA;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.metaheuristics.spea2.SPEA2;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.crossover.HUXCrossover;
import jmetal.operators.crossover.SBXSinglePointCrossover;
import jmetal.operators.crossover.SinglePointCrossover;
import jmetal.operators.mutation.BitFlipMutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.mutation.PolynomialBitFlipMutation;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.Kursawe;
import jmetal.problems.ProblemFactory;
import jmetal.problems.ZDT.ZDT2;
import jmetal.problems.ZDT.ZDT3;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.NonDominatedSolutionList;
import jmetal.util.argsConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * Class for configuring and running the PWT problem
 */
public class EX1_Driver {
	public static Logger      logger_ ;      // Logger object
	public static FileHandler fileHandler_ ; // FileHandler object

	/**
	 * This is the main function of PWT problem
	 * @param args all info will be defined in config file
	 * @throws JMException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void main(String [] args) throws JMException, IOException, ClassNotFoundException {
		/* The current sequence of parameters is
		 * args[0]  Input instance
		 * args[1]  Input Front file
		 * args[2]  Algorithm (SEPA2..)
		 * args[3] 	CrossoverOperator
		 * args[4] 	MutationOperator
		 * args[5]  Number_of_Population
		 * args[6]	Dynamic Item Set Mode: 1 = ON, 2 = OFF
		 * args[7]	target version of benchmark (Choose 1 or 2) since each data sets has two dynamic item benchmark
		 * args[8]  number of generations
		 * args[9]  number of runs
		 * args[10] Tour
		 * 
		 * All parameters are set in Config file
		 */
		// clear previous result files
		clearResults();

		// run algorithms and operators
		// based on args
		long initTime = System.currentTimeMillis();
		Run();
		long TotTime = System.currentTimeMillis() - initTime;
		System.out.println(TotTime);
		//Run(args);

	}//main

	/**
	 * This function is used for plotting .front results into .csv file
	 * @param population
	 * @param fileName
	 * @throws IOException
	 */
	public static void plotFrontAndResult(SolutionSet population, String fileName, int numberOfRun, String algorithm, String crossover, String mutation, String generations) throws IOException {
		String tmpFileName = fileName;
		String outputFileName = "csvResult/"+tmpFileName+"-"+algorithm+"-"+crossover+"-"+mutation+"-"+generations+"-"+numberOfRun+"-Run"+".csv";

		// check output dir exist or not
		File outputFile = new File(outputFileName);
		// check if output file exist or not 
		if(!outputFile.exists()) {
			// check if output file folder exist or not
			if(!outputFile.getParentFile().exists()) {
				// create folder
				outputFile.getParentFile().mkdirs();
			}
			// create file
			outputFile.createNewFile();
		}

		// writing results into file
		FileWriter fw = new FileWriter(outputFileName, true);
		try(  PrintWriter out = new PrintWriter(fw)  ){
			out.println("Objective 1, Objective 2");
			for(int i = 0; i < population.getCapacity(); ++i) {
				int tmpIndex = i + 1;
				String tmpLine = population.get(i).toString();
				String[] tmpSplit = tmpLine.split(" ");
				Double tmpOb = Double.valueOf(tmpSplit[0]);
				tmpOb = -tmpOb;
				out.println(tmpOb.toString() + "," + tmpSplit[1]);
			}
		}
	} // plotFrontAndResult

	/**
	 * This function run ZDT2
	 * @param args
	 * @throws IOException
	 * @throws JMException
	 * @throws ClassNotFoundException
	 */
	public static void RunZDT2(int popSize) throws IOException, JMException, ClassNotFoundException {
			// setup problem variable containers
			// The problem to solve
			Problem   problem   ;
			// The algorithm to use
			Algorithm algorithm = null;
			// Crossover operator
			Operator  crossover = null;
			// Mutation operator
			Operator  mutation  = null;
			// Selection operator
			Operator  selection = null;         
			// Object to get quality indicators
			QualityIndicator indicators ; 
			// Operator parameters
			HashMap  parameters ;
			// initial parameters from args
			int populationSize = popSize;
			
		    // Logger object and file to store log messages
		    logger_      = Configuration.logger_ ;
		    fileHandler_ = new FileHandler("NSGAII_main.log"); 
		    logger_.addHandler(fileHandler_) ;
			
			// setup indicators
			indicators = null ;
			// setup PWT problem object
			problem = new ZDT2("ArrayReal", 30);
			//indicators = new QualityIndicator(problem, convertFrontFile(args[1])) ;

			algorithm = new NSGAII(problem);
		    //algorithm = new ssNSGAII(problem);

		    // Algorithm parameters
		    algorithm.setInputParameter("populationSize",populationSize);
		    algorithm.setInputParameter("maxEvaluations",10000);

		    // Mutation and Crossover for Real codification 
		    parameters = new HashMap() ;
		    parameters.put("probability", 0.9) ;
		    parameters.put("distributionIndex", 20.0) ;
		    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);
		    

		    parameters = new HashMap() ;
		    parameters.put("probability", 1.0/problem.getNumberOfVariables()) ;
		    parameters.put("distributionIndex", 20.0) ;
		    mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);                    

		    // Selection Operator 
		    parameters = null ;
		    selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters) ;                           

		    // Add the operators to the algorithm
		    algorithm.addOperator("crossover",crossover);
		    algorithm.addOperator("mutation",mutation);
		    algorithm.addOperator("selection",selection);

		    // Add the indicator object to the algorithm
		    algorithm.setInputParameter("indicators", indicators) ;


			// Execute the algorithm
			long initTime = System.currentTimeMillis();
			SolutionSet population = algorithm.execute();
			long estimatedTime = System.currentTimeMillis() - initTime;

			// Result messages
			logger_.info("Current run execution time: "+estimatedTime + "ms");
			logger_.info("Current run Objectives values have been writen to file FUN");
			population.printObjectivesToFile("FUN");
			logger_.info("Current run Variables values have been writen to file VAR");
			population.printVariablesToFile("VAR");
			// calculate errors for each operator combination
			//population.printObjectives();
			// Plot result front
			plotFrontAndResult(population,"ZDT2",1,"NSGAII","SBXCrossover","PolynomialMutation","10000");
			//logger_.info("All Objectives values have been writen to csv result folder");
	} // Run

	/**
	 * This function run ZDT3
	 * @param args
	 * @throws IOException
	 * @throws JMException
	 * @throws ClassNotFoundException
	 */
	public static void RunZDT3(int popSize) throws IOException, JMException, ClassNotFoundException {
			// setup problem variable containers
			// The problem to solve
			Problem   problem   ;
			// The algorithm to use
			Algorithm algorithm = null;
			// Crossover operator
			Operator  crossover = null;
			// Mutation operator
			Operator  mutation  = null;
			// Selection operator
			Operator  selection = null;         
			// Object to get quality indicators
			QualityIndicator indicators ; 
			// Operator parameters
			HashMap  parameters ;
			// initial parameters from args
			int populationSize = popSize;

		    // Logger object and file to store log messages
		    logger_      = Configuration.logger_ ;
		    fileHandler_ = new FileHandler("NSGAII_main.log"); 
		    logger_.addHandler(fileHandler_) ;
			
			// setup indicators
			indicators = null ;
			// setup PWT problem object
			problem = new ZDT3("ArrayReal", 30);
			//indicators = new QualityIndicator(problem, convertFrontFile(args[1])) ;

			algorithm = new NSGAII(problem);
		    //algorithm = new ssNSGAII(problem);

		    // Algorithm parameters
		    algorithm.setInputParameter("populationSize",populationSize);
		    algorithm.setInputParameter("maxEvaluations",10000);

		    // Mutation and Crossover for Real codification 
		    parameters = new HashMap() ;
		    parameters.put("probability", 0.9) ;
		    parameters.put("distributionIndex", 20.0) ;
		    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);
		    

		    parameters = new HashMap() ;
		    parameters.put("probability", 1.0/problem.getNumberOfVariables()) ;
		    parameters.put("distributionIndex", 20.0) ;
		    mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);                    

		    // Selection Operator 
		    parameters = null ;
		    selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters) ;                           

		    // Add the operators to the algorithm
		    algorithm.addOperator("crossover",crossover);
		    algorithm.addOperator("mutation",mutation);
		    algorithm.addOperator("selection",selection);

		    // Add the indicator object to the algorithm
		    algorithm.setInputParameter("indicators", indicators) ;


			// Execute the algorithm
			long initTime = System.currentTimeMillis();
			SolutionSet population = algorithm.execute();
			long estimatedTime = System.currentTimeMillis() - initTime;

			// Result messages
			logger_.info("Current run execution time: "+estimatedTime + "ms");
			logger_.info("Current run Objectives values have been writen to file FUN");
			population.printObjectivesToFile("FUN");
			logger_.info("Current run Variables values have been writen to file VAR");
			population.printVariablesToFile("VAR");
			// calculate errors for each operator combination
			//population.printObjectives();
			// Plot result front
			plotFrontAndResult(population,"ZDT3",1,"NSGAII","SBXCrossover","PolynomialMutation","10000");
			//logger_.info("All Objectives values have been writen to csv result folder");
	} // Run

	/**
	 * Unit Test Scripts
	 * @param args
	 * @throws IOException
	 * @throws JMException
	 * @throws ClassNotFoundException
	 */
	public static void Run() throws IOException, JMException, ClassNotFoundException{
		// population size
		int[] populationSize = {10,100,1000};
		// loop 
		for(int i = 0; i < populationSize.length; i++) {
			RunZDT2(populationSize[i]);
			RunZDT3(populationSize[i]);
		}
	}

	/**
	 * This function is used to clear all files in result
	 * folder
	 */
	public static void clearResults() {
		// check file empty
		File dir1 = new File("csvResult/");
		if(!dir1.exists()){
			File file = new File("csvResult/");
			file.mkdirs();
		}
		// clear all previous results in csvResult folder
		for(File file: dir1.listFiles())
			// loop all files
			if (!file.isDirectory()) 
				file.delete();
	}
} // PWT_Driver.java
