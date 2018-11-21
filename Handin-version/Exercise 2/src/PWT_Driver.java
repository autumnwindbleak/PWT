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
import jmetal.operators.crossover.ArithmeticCrossover;
import jmetal.operators.crossover.GroupPointCrossover;
import jmetal.operators.crossover.HUXCrossover;
import jmetal.operators.crossover.ReducedSurrogateCrossover;
import jmetal.operators.crossover.SinglePointCrossover;
import jmetal.operators.crossover.UniformCrossover;
import jmetal.operators.mutation.BitFlipMutation;
import jmetal.operators.mutation.DynamicBitFlipMutation;
import jmetal.operators.mutation.GreedyMutation;
import jmetal.operators.mutation.InterchangingMutation;
import jmetal.operators.mutation.OneBitFlipMutation;
import jmetal.operators.mutation.ReversingMutation;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.PWT;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;
import jmetal.util.PWTQualityIndicator;
import jmetal.util.argsConfiguration;
import jmetal.util.comparators.PriorityComparator;

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
public class PWT_Driver {
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

		// run arguments configuration
		argsConfiguration config = new argsConfiguration();
		// all information are set in configuration file
		args = config.getConfig();

		// run algorithms and operators
		// based on args
		long initTime = System.currentTimeMillis();
		test(args);
		//Run(args);
		long TotTime = System.currentTimeMillis() - initTime;
		System.out.println(TotTime);
	}//main

	/**
	 * This function is used for plotting .front results into .csv file
	 * @param population
	 * @param fileName
	 * @throws IOException
	 */
	public static void plotFrontAndResult(SolutionSet population, String fileName, int numberOfRun, String algorithm, String crossover, String mutation, String generations) throws IOException {
		String tmpFileName = fileName;
		String outputFileName = "csvResult/"+tmpFileName.substring(10, tmpFileName.length()-4)+"-"+algorithm+"-"+crossover+"-"+mutation+"-"+generations+"-"+numberOfRun+"-Run"+".csv";

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
			out.println("Objective Value, Total Weight");
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
	 * This function run all algorithms and operators based on given 
	 * configuration.
	 * @param args
	 * @throws IOException
	 * @throws JMException
	 * @throws ClassNotFoundException
	 */
	public static void Run(String[] args) throws IOException, JMException, ClassNotFoundException {
		// print out info
		System.out.println(args[0]);
		System.out.println(args[2]);
		System.out.println(args[3]);
		System.out.println(args[4]);
		// run several times
		int numberOfRun = Integer.valueOf(args[9]);
		for (int i = 0; i < numberOfRun; ++i) {
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
			int populationSize = Integer.valueOf(args[5]);
			int totalGeneration = Integer.valueOf(args[8]);
			// setup dynamic item mode and version
			int itemSetVersion = Integer.valueOf(args[7]);
			int dynamicItemFlag = Integer.valueOf(args[6]);

			// setup indicators
			indicators = null ;
			// setup PWT problem object
			problem = new PWT("Binary",args[0],args[10]);
			//indicators = new QualityIndicator(problem, convertFrontFile(args[1])) ;

			// pass problem to configured algorithm
			if (args[2].equals("SPEA2")) {
				// Logger object and file to store log messages
				//logger_      = Configuration.logger_ ;
				//fileHandler_ = new FileHandler("SPEA2.log"); 
				//logger_.addHandler(fileHandler_) ;
				// pass problem to SPEA2 algorithm
				algorithm = new SPEA2(problem);
				// Algorithm parameters
				algorithm.setInputParameter("populationSize",populationSize);
				algorithm.setInputParameter("archiveSize",populationSize);
				algorithm.setInputParameter("maxEvaluations",totalGeneration);
				// Selection operator
				parameters = null ;
				selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters) ;                           
			}
			else if (args[2].equals("NSGA2")) {
				// Logger object and file to store log messages
				//logger_      = Configuration.logger_ ;
				//fileHandler_ = new FileHandler("NSGAII_main.log"); 
				//logger_.addHandler(fileHandler_) ;
				// pass problem to NSGA2 algorithm
				algorithm = new NSGAII(problem);
				// Algorithm parameters
				algorithm.setInputParameter("populationSize",populationSize);
				algorithm.setInputParameter("maxEvaluations",totalGeneration);
				// Selection operator
				parameters = null ;
				selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters) ;
			}
			else if (args[2].equals("IBEA")) {
				// Logger object and file to store log messages
				//logger_      = Configuration.logger_ ;
				//fileHandler_ = new FileHandler("IBEA.log"); 
				//logger_.addHandler(fileHandler_) ;
				// pass problem to IBEA algorithm
				algorithm = new IBEA(problem);
				// Algorithm parameters
				algorithm.setInputParameter("populationSize",populationSize);
				algorithm.setInputParameter("archiveSize",populationSize);
				algorithm.setInputParameter("maxEvaluations",totalGeneration);
				parameters = new HashMap() ; 
				//  parameters.put("comparator", new DominanceComparator()) ;
				parameters.put("comparator", new PriorityComparator()) ;
				selection = selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters) ;
			}
			else {
				// no valid algorithm is set
				// throw exception
				throw new JMException("Exception in Algorithm setup: No valid algorithm is selected");
			}

			// setup Crossover operator
			if (args[3].equals("GroupPointCrossover")) {
				parameters = new HashMap() ;
				parameters.put("probability", 0.9) ;
				parameters.put("groupSize", 10.0) ;	
				crossover = new GroupPointCrossover(parameters);
			}
			else if (args[3].equals("SinglePointCrossover")) {
				parameters = new HashMap() ;
				parameters.put("probability", 0.9) ;
				crossover = new SinglePointCrossover(parameters);
			}
			else if (args[3].equals("UniformCrossover")) {
				parameters = new HashMap() ;
				parameters.put("probability", 0.9) ;
				parameters.put("mask", 0.5) ;	
				crossover = new UniformCrossover(parameters);
			}
			else if (args[3].equals("ReducedSurrogateCrossover")) {
				parameters = new HashMap() ;
				parameters.put("probability", 0.9) ;
				//parameters.put("mask", 0.5) ;	
				crossover = new ReducedSurrogateCrossover(parameters);
			}
			else if (args[3].equals("ArithmeticCrossover")) {
				parameters = new HashMap() ;
				parameters.put("probability", 0.9) ;
				//parameters.put("mask", 0.5) ;	
				crossover = new ArithmeticCrossover(parameters);
			}
			else if (args[3].equals("HUXCrossover")) {
				parameters = new HashMap() ;
				parameters.put("probability", 0.9) ;
				//parameters.put("mask", 0.5) ;	
				crossover = new HUXCrossover(parameters);
			}
			else {
				// no valid crossover operator is set
				// throw exception
				throw new JMException("Exception in Crossover setup: No valid Crossover operator is selected");
			}

			// setup mutation operator
			if (args[4].equals("DynamicBitFlipMutation")) {
				parameters = new HashMap();
				parameters.put("probability", 0.02);
				parameters.put("totalGeneration", totalGeneration);
				parameters.put("itemSetVersion", itemSetVersion);
				parameters.put("dynamicItemFlag", dynamicItemFlag);
				mutation = new DynamicBitFlipMutation(parameters);
			}
			else if (args[4].equals("InterchangingMutation")) {
				parameters = new HashMap();
				parameters.put("probability", 0.12);
				parameters.put("totalGeneration", totalGeneration);
				parameters.put("itemSetVersion", itemSetVersion);
				parameters.put("dynamicItemFlag", dynamicItemFlag);
				mutation = new InterchangingMutation(parameters);
			}
			else if (args[4].equals("OneBitFlipMutation")) {
				parameters = new HashMap();
				parameters.put("probability", 0.12);
				parameters.put("totalGeneration", totalGeneration);
				parameters.put("itemSetVersion", itemSetVersion);
				parameters.put("dynamicItemFlag", dynamicItemFlag);
				mutation = new OneBitFlipMutation(parameters);
			}
			else if (args[4].equals("ReversingMutation")) {
				parameters = new HashMap();
				parameters.put("probability", 0.15);
				parameters.put("totalGeneration", totalGeneration);
				parameters.put("itemSetVersion", itemSetVersion);
				parameters.put("dynamicItemFlag", dynamicItemFlag);
				mutation = new ReversingMutation(parameters);
			}
			else if (args[4].equals("BitFlipMutation")) {
				parameters = new HashMap();
				parameters.put("probability", 0.02);
				parameters.put("totalGeneration", totalGeneration);
				parameters.put("itemSetVersion", itemSetVersion);
				parameters.put("dynamicItemFlag", dynamicItemFlag);
				mutation = new BitFlipMutation(parameters);
			}
			else if (args[4].equals("GreedyMutation")) {
				parameters = new HashMap();
				parameters.put("probability", 0.05);
				parameters.put("totalGeneration", totalGeneration);
				parameters.put("itemSetVersion", itemSetVersion);
				parameters.put("dynamicItemFlag", dynamicItemFlag);
				mutation = new GreedyMutation(parameters);
			}
			else {
				// no valid mutation operator is set
				// throw exception
				throw new JMException("Exception in Mutation setup: No valid mutation operator is selected");
			}

			// Add the operators to the algorithm
			algorithm.addOperator("crossover",crossover);
			algorithm.addOperator("mutation",mutation);
			algorithm.addOperator("selection",selection);


			// Execute the algorithm
			//long initTime = System.currentTimeMillis();
			SolutionSet population = algorithm.execute();
			//long estimatedTime = System.currentTimeMillis() - initTime;

			// Result messages
			//logger_.info("Run "+i+": ");
			//logger_.info("Current run execution time: "+estimatedTime + "ms");
			//logger_.info("Current run Objectives values have been writen to file FUN");
			//population.printObjectivesToFile("FUN");
			//logger_.info("Current run Variables values have been writen to file VAR");
			//population.printVariablesToFile("VAR");
			// calculate errors for each operator combination
			PWTQualityIndicator pI = new PWTQualityIndicator(problem, args[1]);
			double error = Double.parseDouble(String.format("%.2f",pI.QualityEvaluate(population)*100));
			//System.out.println("Error: " + error);
			printError(args[0],i+1,args[2],args[3],args[4],args[8],error);
			//population.printObjectives();
			// Plot result front
			plotFrontAndResult(population,args[0],numberOfRun,args[2],args[3],args[4],args[8]);
			//logger_.info("All Objectives values have been writen to csv result folder");
		}
	} // Run

	/**
	 * Unit Test Scripts
	 * @param args
	 * @throws IOException
	 * @throws JMException
	 * @throws ClassNotFoundException
	 */
	public static void test(String[] args)throws IOException, JMException, ClassNotFoundException{
		// all instance names
		String[] ins = {
				"instance/eil101_n100_bounded-strongly-corr_01.ttp",
				"instance/eil101_n100_bounded-strongly-corr_06.ttp",
				"instance/eil101_n100_bounded-strongly-corr_10.ttp",
				"instance/eil101_n500_bounded-strongly-corr_01.ttp",
				"instance/eil101_n500_bounded-strongly-corr_06.ttp",
				"instance/eil101_n500_bounded-strongly-corr_10.ttp",
				"instance/eil101_n1000_bounded-strongly-corr_01.ttp",
				"instance/eil101_n1000_bounded-strongly-corr_06.ttp",
				"instance/eil101_n1000_bounded-strongly-corr_10.ttp",
		};
		// all front file names
		String[] frontName = {
				"instance/eil101_n100_bounded-strongly-corr_01.ttp.front",
				"instance/eil101_n100_bounded-strongly-corr_06.ttp.front",
				"instance/eil101_n100_bounded-strongly-corr_10.ttp.front",
				"instance/eil101_n500_bounded-strongly-corr_01.ttp.front",
				"instance/eil101_n500_bounded-strongly-corr_06.ttp.front",
				"instance/eil101_n500_bounded-strongly-corr_10.ttp.front",
				"instance/eil101_n1000_bounded-strongly-corr_01.ttp.front",
				"instance/eil101_n1000_bounded-strongly-corr_06.ttp.front",
				"instance/eil101_n1000_bounded-strongly-corr_10.ttp.front",
		};
		String[] algorithms = {"SPEA2","IBEA","NSGA2"};
		String[] crossovers = {"GroupPointCrossover", "SinglePointCrossover","UniformCrossover", "ReducedSurrogateCrossover", "ArithmeticCrossover","HUXCrossover"};
		String[] mutations = {"DynamicBitFlipMutation", "InterchangingMutation", "OneBitFlipMutation", "GreedyMutation","ReversingMutation", "BitFlipMutation"};
		Double totEvents = (double) (ins.length * algorithms.length * crossovers.length * mutations.length);
		Double counter = 0d;
		for(int insIndex = 0; insIndex < ins.length; insIndex++) {
			for(int k = 0; k < algorithms.length; k++){
				for (int i = 0; i < crossovers.length; i++){
					for(int j = 0; j < mutations.length; j++){
						args[0] = ins[insIndex];
						args[1] = frontName[insIndex];
						args[2] = algorithms[k];
						args[3] = crossovers[i];
						args[4] = mutations[j];
						// run all test cases
						Run(args);
						counter++;
						// Round a completeRate to 3 significant figures
						Double completeRate = (counter/totEvents) * 100;
						BigDecimal bd = new BigDecimal(completeRate);
						bd = bd.round(new MathContext(3));
						double roundRatio= bd.doubleValue();
						System.out.println("Total Complete Rate: " + roundRatio + " %");
					}
				}
			}
		}

	}

	/**
	 * This function will write error into error report for each
	 * combination.
	 * @param fileName
	 * @param numberOfRun
	 * @param algorithm
	 * @param crossover
	 * @param mutation
	 * @param generations
	 * @param error
	 * @throws IOException
	 */
	public static void printError(String fileName, int runNum, String algorithm, String crossover, String mutation, String generations,double error) throws IOException {
		String tmpFileName = fileName;
		String outputFileName = "errorResult/"+tmpFileName.substring(10, tmpFileName.length()-4)+"-"+generations+"-generations"+"-ErrorReport.csv";

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
			out.println(algorithm+","+crossover+","+mutation+",RunNum=" + runNum+","+ String.valueOf(error));
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
		// check file empty
		File dir2 = new File("errorResult/");
		if(!dir2.exists()){
			File file = new File("errorResult/");
			file.mkdirs();
		}
		// clear all previous results in csvResult folder
		for(File file: dir2.listFiles()) 
			// loop all files
			if (!file.isDirectory()) 
				file.delete();
	}
} // PWT_Driver.java
