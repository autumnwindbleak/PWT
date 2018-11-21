package jmetal.problems;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.util.JMException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * a class representing problem Packing While Traveling
 * @author ian
 *
 */
public class PWT extends Problem {
	//the total number of cities
	public int	numberOfCities_ ;
	//the total number of items
	public int	numberOfItems_;
	//the capacity of the vehicle
	public long	capacityOfKnapsack_;
	//the cost by time
	public double	rentingRatio_;
	//maxspeed the vehicle can travel
	public double	minSpeed_;
	//minspeed the vehicle can travel
	public double	maxSpeed_;
	//the name of the problem
	public String	problemName_;
	//the data type of knapsack
	public String	knapsackDataType_;
	//the type of edge weight
	public String	edgeWeightType_;
	//the coordinate of each city : coordinate_[i][0] is the city number,coordinate_[i][1] is x, coordinate_[i][2] is y
	public double[][]	coordinate_;
	//the fixed tour
	public int[]	tour;
	//the information of each item: items[i][0] is the item number, items[i][1] is the profit, items[i][2] is the weight, items[i][3] is the city this item assigned
	public int [][] items;
	//the input ttp file name
	public String filename;
	//the fixed tour file name
	public String tourfilename;
	
	//dynamic items benchmark
	private static HashMap<Integer,ArrayList<Integer>> unavailableitemsbygeneration = null; 
	
	//count up the current generation(-1 for initial)
	private static int currentGeneration = -1;
	

	/**
	 * constructor
	 * @param solutionType
	 * @param file: the TTP input file (input file example can be found at "http://cs.adelaide.edu.au/~optlog/CEC2014Comp/", in "Instances and Code" section)
	 * @param tourfile: a fixed tour file
	 * @throws IOException
	 */
	public PWT(String solutionType, String file, String tourfile) throws IOException {
		//store the file name
		this.filename = file;
		this.tourfilename = tourfile;
		readProblem(file,tourfile) ;
		//only 1 variables: the packing plan
		numberOfVariables_  = 1;
		//two objective value: 0 for final profit, 1 for total weight
		numberOfObjectives_ = 2;
		//one constraint: the total weight should not be over than the capacity
		numberOfConstraints_= 1;
		//problem name is PWT
		problemName_        = "PWT";

		//for initial the variables in solution class 
		length_ = new int[numberOfVariables_];
		//print information
		System.out.println("Cities: " + numberOfCities_ +"\t Items: " + numberOfItems_);
		//the first variables in a solution need contain an array with the length equals to numberOfItems_;
		length_      [0] = numberOfItems_;
		//PWT is a binary stored problem
		if (solutionType.compareTo("Binary") == 0)
			solutionType_ = new BinarySolutionType(this) ;
		else {
			System.out.println("Error: solution type " + solutionType + " invalid") ;
			System.exit(-1) ;
		}

	} // PWT

	/**
	 * evaluate a solution: get the final profit and the total weight of a solution
	 */
	public void evaluate(Solution solution) {
		//initial the tour
		int[] tour = this.tour;
		// get the Binary packing plan in the solution
		Binary b =(Binary)solution.getDecisionVariables()[0];
		//initial and transform the binary form to int form
		int[] packingPlan = new int[this.numberOfItems_];
		for(int i = 0; i < this.numberOfItems_;i++){
			if(b.getIth(i)){
				packingPlan[i] = 1;
			}else{
				packingPlan[i] = 0;
			}
		}
		//get the parameters that need to calculate the profit
		long weightofKnapsack = this.capacityOfKnapsack_;
		double rentRate = this.rentingRatio_;
		double vmin = this.minSpeed_;
		double vmax = this.maxSpeed_;
		//initial the result
		double finaltime = 0;
		double finalprofit = 0;
		double totalWeight = 0;

		// correctness check: does the tour start and end in the same city
		if(tour[0]!=tour[tour.length-1]) {
			System.out.println("ERROR: The last city must be the same as the first city");
			System.exit(-1);
			return;
		}
	
		// loop for each city in the tour (no first city)
		for (int i=1; i<tour.length-1; i++) {
			//get the number fo current city
			int currentCity = tour[i];
			//loop for each items
			for(int j = 0; j < items.length; j++){
				//if the items is in the current city
				if(items[j][3] == currentCity){
					//if the items is picked
					if(packingPlan[items[j][0]] == 1){
						finalprofit = finalprofit + items[j][1];
						totalWeight = totalWeight + items[j][2];
					}
				}
			}
			

			//calculate the distance to the next city
			long distance = (long)Math.ceil(distances(tour[i],tour[i+1]));
//			long distance = (long)distances(tour[i],tour[i+1]);

			// calculate the final time
			finaltime=finaltime+(distance / (vmax-totalWeight*(vmax-vmin)/weightofKnapsack)); 
		}

		//objective 0: final profit
		//			2: total weight

		solution.setObjective(0, -(finalprofit - finaltime*rentRate));
		solution.setObjective(1, totalWeight);
	}


	/**
	 * read the input file
	 * @param file: ttp input file
	 * @param tourfile: fixed tour
	 * @throws IOException
	 */
	public void readProblem(String file,String tourfile) throws IOException {
		File f = new File(file);
		Scanner input = new Scanner(f);
		//scan each line
		while(input.hasNextLine()){
			String line = input.nextLine();
			// process the line
			if (line.startsWith("PROBLEM NAME")) {
				line = line.substring(line.indexOf(":")+1);
				line = line.replaceAll("\\s+","");
				this.problemName_ = line;
			}
			if (line.startsWith("KNAPSACK DATA TYPE")) {
				line = line.substring(line.indexOf(":")+1);
				line = line.replaceAll("\\s+","");
				this.knapsackDataType_ = line;
			}
			if (line.startsWith("DIMENSION")) {
				line = line.substring(line.indexOf(":")+1);
				line = line.replaceAll("\\s+","");
				this.numberOfCities_=Integer.parseInt(line);
			}
			if (line.startsWith("NUMBER OF ITEMS")) {
				line = line.substring(line.indexOf(":")+1);
				line = line.replaceAll("\\s+","");
				this.numberOfItems_=Integer.parseInt(line);
			}
			if (line.startsWith("CAPACITY OF KNAPSACK")) {
				line = line.substring(line.indexOf(":")+1);
				line = line.replaceAll("\\s+","");
				this.capacityOfKnapsack_=Long.parseLong(line);
			}
			if (line.startsWith("MIN SPEED")) {
				line = line.substring(line.indexOf(":")+1);
				line = line.replaceAll("\\s+","");
				this.minSpeed_=Double.parseDouble(line);
			}
			if (line.startsWith("MAX SPEED")) {
				line = line.substring(line.indexOf(":")+1);
				line = line.replaceAll("\\s+","");
				this.maxSpeed_=Double.parseDouble(line);
			}
			if (line.startsWith("RENTING RATIO")) {
				line = line.substring(line.indexOf(":")+1);
				line = line.replaceAll("\\s+","");
				this.rentingRatio_=Double.parseDouble(line);
			}
			if (line.startsWith("EDGE_WEIGHT_TYPE")) {
				line = line.substring(line.indexOf(":")+1);
				line = line.replaceAll("\\s+","");
				this.edgeWeightType_ = line;
			}
			//start reading information of cities
			if (line.startsWith("NODE_COORD_SECTION")) {
				//initial the coordinate
				this.coordinate_ = new double[this.numberOfCities_][3];
				//read each city's number and x,y coordinate
				for (int i=0; i<this.numberOfCities_; i++) {
					line = input.nextLine();
					String[] splittedLine = line.split("\\s+");
					for (int j=0; j<splittedLine.length; j++) {
						double temp = Double.parseDouble(splittedLine[j]);
						if (j==0) temp =  temp-1;
						this.coordinate_[i][j] = temp;
					}
				}
			}
			//start reading information of items
			if (line.startsWith("ITEMS SECTION")) {
				//initial items
				this.items = new int[this.numberOfItems_][4];
				//read each items' number, profit, weight and assigned city
				for (int i=0; i<this.numberOfItems_; i++) {
					line = input.nextLine();
					String[] splittedLine = line.split("\\s+");
					for (int j=0; j<splittedLine.length; j++) {
						int temp = Integer.parseInt(splittedLine[j]);
						if (j==0) temp =  temp-1;  // item numbers start here with 0 --> in TTP files with 1
						if (j==3) temp =  temp-1;  // city numbers start here with 0 --> in TTP files with 1
						this.items[i][j] = temp;
					}
				}
			}
		}
		input.close();

		//read tour
		File tf = new File(tourfile);
		input = new Scanner(tf);
		//initial tour
		this.tour = new int[this.numberOfCities_];
		//skip the first line
		if(input.hasNextLine()){
			input.nextLine();
		}
		//scan each line and record the tour
		for(int i = 0; i<this.numberOfCities_; i++){
			String line = input.nextLine();
			this.tour[i] = Integer.parseInt(line.split(" ")[0]);
		}
		//let the last city equals to the first city to make a circle
		this.tour[this.numberOfCities_-1] = this.tour[0];
		input.close();

	} // readProblem

	/**
	 * evalutation of the constraints
	 */
	public void evaluateConstraints(Solution solution) throws JMException {
		//get the constraints (PWT only have one here)
		double [] constraint = new double[this.getNumberOfConstraints()];
		//the leftweight should be as much as possible and should not be below 0, it is good to use as a constraint
		double leftweight = this.capacityOfKnapsack_-solution.getObjective(1);

		constraint[0] =  leftweight;

		//intial total value of constraints
		double total = 0.0;
		//count the number of violated constraints
		int number = 0;
		//loop for each constraint
		for (int i = 0; i < this.getNumberOfConstraints(); i++)
			if (constraint[i]<0.0){
				total+=constraint[i];
				number++;
			}
		//set the constraints
		solution.setOverallConstraintViolation(total);    
		solution.setNumberOfViolatedConstraint(number);         
	} // evaluateConstraints  
	
	
	/**
	 * This method takes current item sets and generation number
	 * to generate new item sets.
	 * @param inputfile: ttp file
	 * @param totalGeneration: how many generation will run
	 * @param itemSetVersion: the benchmark set
	 * @throws IOException
	 */
	private void generateDynamicItem(int totalGeneration,int itemSetVersion) throws IOException {
		// setup which target version folder of 
		// dynamic benchmark
		String version = "";
		String benchmark1 = "benchmark1";
		String benchmark2 = "benchmark2";

		// setup benchmark version
		if(itemSetVersion == 1) {
			version = benchmark1;
		}
		else if (itemSetVersion == 2) {
			version = benchmark2;
		}
		else {
			// print out error message
			System.out.println("Invalid item set version number, Please choose from 1 or 2.");
			System.exit(0); 
		}
		

		// dynamic mutate items' availability
		// and save the new item set to benchmark file
		String inputDataName = this.filename;
		inputDataName = inputDataName.substring(10, inputDataName.length()-4);
		String outputFileName = "benchmarks/dynamicItems/"+inputDataName+"/"+version+ "-items.txt";
		// create file and folders if not exist
		File file = new File(outputFileName);
		file.getParentFile().mkdirs();

		// start write applied operation into file
		try(  FileWriter writer = new FileWriter(file);  ){
			// loop generation
			for(int generation = 1; generation <= totalGeneration; ++generation) {

				// check generation flag to see 
				// if it reaches every 50 generation
				int generationFlag = generation % 50;
				if(generationFlag != 0) {
					// no action will perform
					// between each 50 generation
					continue;
				}
				// write current generation into file
				String currentItemStatus = String.valueOf(generation) + ":";
				int row = items.length;
				double prob = 5d/row;
				// loop all items in current generation
				for(int i = 0; i < row; ++i) {
					double currentDice = Math.random();
					// current dice value larger than probability
					// then no status change on this element
					if(currentDice > prob) {
						// jump to next element
						continue;
					}
					else {
						String tmp = String.valueOf(i);
						// check if it is the first applied operation
						if(currentItemStatus.charAt(currentItemStatus.length()-1) == ':') {
							// save item's status in benchmark
							currentItemStatus = currentItemStatus + tmp;
						}
						else {
							// save item's status in benchmark
							currentItemStatus = currentItemStatus + "," + tmp;
						}
					}
				}
				// start from new line
				currentItemStatus = currentItemStatus + "\n";
				writer.write(currentItemStatus);
			}
			writer.close();
		}
	}
	

	/**
	 * This method takes input file name and item set version
	 * number to read pre-generated dynamic item sets.
	 * @param file: TTP input file name
	 * @param itemSetVersion
	 * @throws IOException 
	 */
	private void readDynamicItem(int totalGeneration, int itemSetVersion) throws IOException {
		// setup which version of pre-generated dynamic benchmark
		// will be read in this generation.
		String version = "";
		String benchmark1 = "benchmark1";
		String benchmark2 = "benchmark2";

		// setup benchmark version
		if(itemSetVersion == 1) {
			version = benchmark1;
		}
		else if (itemSetVersion == 2) {
			version = benchmark2;
		}
		else {
			// print out error message
			// terminate program
			System.out.println("Invalid item set version number, Please choose from 1 or 2.");
			System.exit(0); 
		}

		// data set initialization
		unavailableitemsbygeneration = new HashMap<Integer,ArrayList<Integer>>();

		// setup new item file name
		String inputDataName = this.filename;
		inputDataName = inputDataName.substring(10, inputDataName.length()-4);
		String readFileName = "benchmarks/dynamicItems/"+inputDataName+"/"+version + "-items.txt";

		// open item file
		File file = new File(readFileName);
		if(!file.exists()){
			generateDynamicItem(totalGeneration,itemSetVersion);
		}

		// start read item's info from item file
		// setup new reader and text container
		Scanner reader = new Scanner(file);
		String text = null;

		while (reader.hasNextLine()) {
			// read status changed info of dynamic item
			text = reader.nextLine();
			String[] tmpSplit = text.split(":");
			
			if(tmpSplit.length == 2) {
				String tmpItems = tmpSplit[1];
				String[] tmpStatus = tmpItems.split(",");
				
				ArrayList<Integer> unavailableitems = new ArrayList<Integer>();
				// store all applied changes to hashmap
				for(int i = 0; i < tmpStatus.length; ++i) {
					unavailableitems.add(Integer.parseInt(tmpStatus[i]));
				}
				// store to list
				unavailableitemsbygeneration.put(Integer.parseInt(tmpSplit[0]), unavailableitems);
			}
		}
		reader.close();
	}
	
	
	
	/**
	 * This method will update item status based on current
	 * generation.
	 * @param solution: the current solution
	 * @param currentGeneration: the current generation
	 * @param totalGeneration: the total generation
	 * @throws IOException 
	 */
	public void updateCurrentItems(Solution solution,int totalGeneration,int itemSetVersion) throws IOException {
		//intial the currentGeneration for the first time
		if(currentGeneration == -1){
			currentGeneration = 0;
		}
		//get the number of items
		int row = items.length;
		//get the binary form packing plan
		Binary packingPlan =(Binary)solution.getDecisionVariables()[0];
		// check generation flag to see 
		// if it reaches every 50 generation
		int generationFlag = currentGeneration % 50;
		if(generationFlag != 0) {
			// no action will perform
			// between each 50 generation
			return;
		}
		if(!isDynamicItemsExist()){
			readDynamicItem(totalGeneration,itemSetVersion);
		}
		//check if this generation all the items is available
		if(unavailableitemsbygeneration.get(currentGeneration) != null){
			ArrayList<Integer> unavailableitems = unavailableitemsbygeneration.get(currentGeneration);
			for(int i = 0; i < unavailableitems.size(); i++){
				if(packingPlan.getIth(unavailableitems.get(i))){
					packingPlan.setIth(unavailableitems.get(i), false);
				}
			}
			Variable[]  variables = new Variable[1];
			variables[0] = packingPlan;
			solution.setDecisionVariables(variables);
		}
		currentGeneration++;
	}
	
	
	/**
	 * check the unavailableitemsbygenerations is null or not (dynamic items is read or not)
	 * @return
	 */
	private boolean isDynamicItemsExist(){
		if(unavailableitemsbygeneration == null){
			return false;
		}else{
			return true;
		}
	}
	
	


	/**
	 * calculate the distance between city i and j
	 * @param i: index of the first city
	 * @param j: index of the second city
	 * @return the distance of two city
	 */
	public double distances(int i, int j) {
		double result = 0;
		result = Math.sqrt(
				(this.coordinate_[i][1]-this.coordinate_[j][1]) *
				(this.coordinate_[i][1]-this.coordinate_[j][1]) + 
				(this.coordinate_[i][2]-this.coordinate_[j][2]) *
				(this.coordinate_[i][2]-this.coordinate_[j][2]) 
				);
		return result;
	}

} // PWT
