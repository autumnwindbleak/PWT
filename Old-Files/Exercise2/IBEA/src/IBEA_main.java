//  IBEA_main.java
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



import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.ibea.IBEA;
import jmetal.operators.crossover.GroupPointCrossover;
import jmetal.operators.crossover.HUXCrossover;
import jmetal.operators.crossover.SinglePointCrossover;
import jmetal.operators.mutation.BitFlipMutation;
import jmetal.operators.mutation.DynamicBitFlipMutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.BinaryTournament;
import jmetal.operators.selection.Selection;
import jmetal.problems.PWT;
import jmetal.problems.ProblemFactory;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.ShowResult;
import jmetal.util.comparators.DominanceComparator;
import jmetal.util.comparators.PriorityComparator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * Class for configuring and running the DENSEA algorithm
 */
public class IBEA_main {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object

  /**
   * @param args Command line arguments.
   * @throws JMException 
   * @throws IOException 
   * @throws SecurityException 
   * Usage: three choices
   *      - jmetal.metaheuristics.nsgaII.NSGAII_main
   *      - jmetal.metaheuristics.nsgaII.NSGAII_main problemName
   *      - jmetal.metaheuristics.nsgaII.NSGAII_main problemName paretoFrontFile
   */
  public static void main(String [] args) throws JMException, IOException, ClassNotFoundException {
    Problem   problem   ;         // The problem to solve
    Algorithm algorithm ;         // The algorithm to use
    Operator  crossover ;         // Crossover operator
    Operator  mutation  ;         // Mutation operator
    Operator  selection ;         // Selection operator

    QualityIndicator indicators ; // Object to get quality indicators

    HashMap  parameters ; // Operator parameters

    // Logger object and file to store log messages
    logger_      = Configuration.logger_ ;
    fileHandler_ = new FileHandler("IBEA.log"); 
    logger_.addHandler(fileHandler_) ;
    
    indicators = null ;
    if (args.length == 1) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0],params);
    } // if
    else if (args.length == 2) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0],params);
      indicators = new QualityIndicator(problem, convertFrontFile(args[1])) ;
    } // if
    else { // Default problem
      problem = new PWT("Binary","instance/eil101_n100_bounded-strongly-corr_01.ttp","instance/eil101.linkern.tour");
      //problem = new Kursawe("Real", 3); 
      //problem = new Kursawe("BinaryReal", 3);
      //problem = new Water("Real");
      //problem = new ZDT1("ArrayReal", 100);
      //problem = new ConstrEx("Real");
      //problem = new DTLZ1("Real");
      //problem = new OKA2("Real") ;
    } // else

    algorithm = new IBEA(problem);

    //initial parameters
    int populationsize = 20;
    int totalGeneration = 5000;
    int itemSetVersion = 1;
    int dynamicItemFlag = 1;
    
    // Algorithm parameters
    algorithm.setInputParameter("populationSize",populationsize);
    algorithm.setInputParameter("archiveSize",populationsize);
    algorithm.setInputParameter("maxEvaluations",totalGeneration);

    // Mutation and Crossover for Real codification 
    parameters = new HashMap() ;
    parameters.put("probability", 0.9) ;
    parameters.put("groupSize", 10.0) ;
    //crossover = CrossoverFactory.getCrossoverOperator("SinglePointCrossover", parameters);
//    crossover = new SinglePointCrossover(parameters);
    crossover = new GroupPointCrossover(parameters);

    parameters = new HashMap() ;
    parameters.put("probability", 0.01) ;
    parameters.put("distributionIndex", 20.0) ;
    parameters.put("totalGeneration", totalGeneration);
    parameters.put("itemSetVersion", itemSetVersion);
    parameters.put("dynamicItemFlag", dynamicItemFlag);
//    mutation = MutationFactory.getMutationOperator("BitFlipMutation", parameters);
    mutation = new DynamicBitFlipMutation(parameters);

    /* Selection Operator */
    parameters = new HashMap() ; 
//    parameters.put("comparator", new DominanceComparator()) ;
    parameters.put("comparator", new PriorityComparator()) ;
    
    selection = new BinaryTournament(parameters);
    
    // Add the operators to the algorithm
    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("mutation",mutation);
    algorithm.addOperator("selection",selection);

    // Execute the Algorithm
    long initTime = System.currentTimeMillis();
    SolutionSet population = algorithm.execute();
    long estimatedTime = System.currentTimeMillis() - initTime;

    // Print the results
//    logger_.info("Total execution time: "+estimatedTime + "ms");
//    logger_.info("Variables values have been writen to file VAR");
//    population.printVariablesToFile("VAR");    
//    logger_.info("Objectives values have been writen to file FUN");
//    population.printObjectivesToFile("FUN");
//    population.printObjectives();
      ShowResult.showresult(population);
    
  
//    if (indicators != null) {
//      logger_.info("Quality indicators") ;
//      logger_.info("Hypervolume: " + indicators.getHypervolume(population)) ;
//      logger_.info("GD         : " + indicators.getGD(population)) ;
//      logger_.info("IGD        : " + indicators.getIGD(population)) ;
//      logger_.info("Spread     : " + indicators.getSpread(population)) ;
//      logger_.info("Epsilon    : " + indicators.getEpsilon(population)) ;  
//    } // if
  } //main
  
  
  
  
	public static String convertFrontFile(String FrontFileName) throws IOException {
		/* Open the file */
		FileInputStream fis   = new FileInputStream(FrontFileName)     ;
		InputStreamReader isr = new InputStreamReader(fis)    ;
		BufferedReader br      = new BufferedReader(isr)      ;

		// skip first two lines
		// in oder to directely read node info
		String aux = br.readLine();
		aux = br.readLine();
		aux = br.readLine();

		// save all info to string
		Vector<String> frontInfo = new Vector<String>();
		while (aux!= null) {
			frontInfo.add(aux);
			// increment 
			aux = br.readLine();
		}

		String tmpFileName = FrontFileName;
		String outputFileName = tmpFileName.substring(0, tmpFileName.length()-10)+"-Simplified"+".front";

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
			for(int i = 0; i < frontInfo.size(); ++i) {
				out.println(frontInfo.get(i));
			}
		}
		return outputFileName;
	}
  
} // IBEA_main.java
