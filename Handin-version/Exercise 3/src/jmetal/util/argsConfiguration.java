package jmetal.util;
/*
 * Author: Puzhi Yao
 * 
 * This class read configuration info from config file
 * and setup args
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class argsConfiguration {
	/*
	 * store current config argument
	 */
	public static String[] args = new String[11];
	/*
	 * store all target instance file names
	 */
	public static String[] instancesName = {
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
	/*
	 * store all target front file names
	 */
	public static String[] frontName = {
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

	/**
	 * Constructor
	 * read configuration info from config file
	 */
	public argsConfiguration() {
		// setup new configuration file name
		String configFileName = "Config";

		// open configuration file
		File file = new File(configFileName);
		BufferedReader reader = null;
		// setup parameters' container
		String Input_Front = null;
		String Input_Instance = null;
		String Algorithm = null;
		String CrossoverOperator = null;
		String MutationOperator = null;
		String Number_of_Population = null;
		String Dynamic_Item_Mode = null;
		String Dynamic_Item_Benchmark = null;
		String Generations = null;
		String runs = null;
		String tour = null;

		// start read configuration info from configuration file
		try {
			// setup new reader and text container
			reader = new BufferedReader(new FileReader(file));
			String text = null;
			// start reading configuration file
			while ((text = reader.readLine()) != null) {
				// read line text
				// jump out if this line contains no key info
				if(text.equals("") || text.charAt(0) == '%' || text.equals("\t") || text.equals(" ") || text.charAt(0) == ' ') {
					// check to see this line 
					// is instruction comment
					continue;
				}
				else {
					if(text.contains("Input_Front")) {
						// setup scan pattern
						String regex = "\"([^\"]*)\"";
						// set pattern to read between double quotes
						Pattern pat = Pattern.compile(regex);
						Matcher m = pat.matcher(text);
						if(m.find()) {
							// find instance folder name between quotes
							Input_Front = m.group(1);
						}					
					}
					else if(text.contains("Input_Instance")) {
						// setup scan pattern
						String regex = "\"([^\"]*)\"";
						// set pattern to read between double quotes
						Pattern pat = Pattern.compile(regex);
						Matcher m = pat.matcher(text);
						if(m.find()) {
							// find instance folder name between quotes
							Input_Instance = m.group(1);
						}	
					}
					else if(text.contains("Algorithm")) {
						// setup scan pattern
						String regex = "\"([^\"]*)\"";
						// set pattern to read between double quotes
						Pattern pat = Pattern.compile(regex);
						Matcher m = pat.matcher(text);
						if(m.find()) {
							// find instance folder name between quotes
							Algorithm = m.group(1);
						}	
					}
					else if(text.contains("Number_of_Population")) {
						// setup scan pattern
						String regex = "\"([^\"]*)\"";
						// set pattern to read between double quotes
						Pattern pat = Pattern.compile(regex);
						Matcher m = pat.matcher(text);
						if(m.find()) {
							// find instance folder name between quotes
							Number_of_Population = m.group(1);
						}
					}
					else if(text.contains("CrossoverOperator")) {
						// setup scan pattern
						String regex = "\"([^\"]*)\"";
						// set pattern to read between double quotes
						Pattern pat = Pattern.compile(regex);
						Matcher m = pat.matcher(text);
						if(m.find()) {
							// find instance folder name between quotes
							CrossoverOperator = m.group(1);
						}
					}
					else if(text.contains("Dynamic_Item_Mode")) {
						// setup scan pattern
						String regex = "\"([^\"]*)\"";
						// set pattern to read between double quotes
						Pattern pat = Pattern.compile(regex);
						Matcher m = pat.matcher(text);
						if(m.find()) {
							// find instance folder name between quotes
							Dynamic_Item_Mode = m.group(1);
						}
					}
					else if(text.contains("Dynamic_Item_Benchmark")) {
						// setup scan pattern
						String regex = "\"([^\"]*)\"";
						// set pattern to read between double quotes
						Pattern pat = Pattern.compile(regex);
						Matcher m = pat.matcher(text);
						if(m.find()) {
							// find instance folder name between quotes
							Dynamic_Item_Benchmark = m.group(1);
						}
					}
					else if(text.contains("Generations")) {
						// setup scan pattern
						String regex = "\"([^\"]*)\"";
						// set pattern to read between double quotes
						Pattern pat = Pattern.compile(regex);
						Matcher m = pat.matcher(text);
						if(m.find()) {
							// find instance folder name between quotes
							Generations = m.group(1);
						}
					}
					else if(text.contains("runs")) {
						// setup scan pattern
						String regex = "\"([^\"]*)\"";
						// set pattern to read between double quotes
						Pattern pat = Pattern.compile(regex);
						Matcher m = pat.matcher(text);
						if(m.find()) {
							// find instance folder name between quotes
							runs = m.group(1);
						}
					}
					else if(text.contains("MutationOperator")) {
						// setup scan pattern
						String regex = "\"([^\"]*)\"";
						// set pattern to read between double quotes
						Pattern pat = Pattern.compile(regex);
						Matcher m = pat.matcher(text);
						if(m.find()) {
							// find instance folder name between quotes
							MutationOperator = m.group(1);
						}
					}
					else if(text.contains("Tour")) {
						// setup scan pattern
						String regex = "\"([^\"]*)\"";
						// set pattern to read between double quotes
						Pattern pat = Pattern.compile(regex);
						Matcher m = pat.matcher(text);
						if(m.find()) {
							// find instance folder name between quotes
							tour = m.group(1);
						}
					}
				}

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					// reading finish
					// close reader
					reader.close();
				}
			} catch (IOException e) {
			}
		}
		
		// setup input instance
		int instanceIndex = Integer.valueOf(Input_Instance) - 1;
		// setup input front file
		int frontIndex = Integer.valueOf(Input_Front) - 1;

		// setup arguments
		args[0] = getInstanceName(instanceIndex);
		args[1] = getFrontName(frontIndex);
		args[2] = Algorithm;
		args[3] = CrossoverOperator;
		args[4] = MutationOperator;
		args[5] = Number_of_Population;
		args[6] = Dynamic_Item_Mode;
		args[7] = Dynamic_Item_Benchmark;
		args[8] = Generations;
		args[9] = runs;
		args[10] = tour;
	}

	/**
	 * Get current config arguments
	 * @return
	 */
	public String[] getConfig() {
		return this.args;
	}
	/**
	 * Get target instance file name
	 * @param index
	 * @return
	 */
	public String getInstanceName(int index) {
		return this.instancesName[index];
	}
	/**
	 * Get target Front file name
	 * @param index
	 * @return
	 */
	public String getFrontName(int index) {
		return this.frontName[index];
	}
}

