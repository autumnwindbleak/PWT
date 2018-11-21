package jmetal.util.comparators;

import java.util.Comparator;

import jmetal.core.Solution;

public class PriorityComparator implements Comparator {
	IConstraintViolationComparator violationConstraintComparator_ ;

	/**
	* Constructor
	*/
	public PriorityComparator() {
	violationConstraintComparator_ = new OverallConstraintViolationComparator(); 
	//violationConstraintComparator_ = new NumberOfViolatedConstraintComparator(); 
	}

	/**
	* Constructor
	* @param comparator
	*/
	public  PriorityComparator(IConstraintViolationComparator comparator) {
	violationConstraintComparator_ = comparator ;
	}

	/**
	* Compares two solutions.
	* @param object1 Object representing the first <code>Solution</code>.
	* @param object2 Object representing the second <code>Solution</code>.
	* @return -1, or 0, or 1 if solution1 dominates solution2, both are 
	* non-dominated, or solution1  is dominated by solution22, respectively.
	*/
	public int compare(Object object1, Object object2) {
	if (object1==null)
	  return 1;
	else if (object2 == null)
	  return -1;

	Solution solution1 = (Solution)object1;
	Solution solution2 = (Solution)object2;

	int flag; //stores the result of the comparison

	// Test to determine whether at least a solution violates some constraint
	if (violationConstraintComparator_.needToCompare(solution1, solution2))
	  return violationConstraintComparator_.compare(solution1, solution2) ;



	double value1, value2;
	for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
	  value1 = solution1.getObjective(i);
	  value2 = solution2.getObjective(i);
	  if (value1 < value2) {
	    return -1;
	  } else if (value1 > value2) {
	    return 1;
	  }
	}
	return 0;
	        
	} // compare
}

