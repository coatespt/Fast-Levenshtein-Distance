package com.levenshtein.leven.utility;

import java.util.Random;

import org.apache.log4j.Logger;

import com.levenshtein.leven.utility.exception.NoNextValueException;

/**
 * Select without replacement from a set of zero to range.
 * Create the class with a range. A series of next() calls will
 * return a random distinct element of the set.
 * This algorithm exhibits constant time, with space equal to the number of units * size of an int.
 * 
 * @author peterc
 *
 */
public class SWOR {
	private int [] valueSet;
	private int range;
	private Random rand=new Random();
	static Logger log = Logger.getLogger(SWOR.class);

	/**
	 * Create a SWOR object with the random generator specifically
	 * initialized.
	 * @param range
	 * @param seed
	 */
	public SWOR(int range, int seed){
		rand = new Random(seed);
		valueSet = new int[range];
		this.range = range; 
		for(int i=0; i<range; i++){
			valueSet[i]=i;
		}
	}
	
	/**
	 * Create a SWOR object with the default random initialization
	 * @param range
	 */
	public SWOR(int range){
		valueSet = new int[range];
		this.range = range; 
		for(int i=0; i<range; i++){
			valueSet[i]=i;
		}
	}
	
	public boolean hasNext(){
		return range>=1;
	}
	
	public int next() throws NoNextValueException {
		if(range<=0){
			throw new NoNextValueException();
		}
		int retIndex = rand.nextInt(range);
		int retVal = valueSet[retIndex];
		valueSet[retIndex]=valueSet[range-1];
		range--;
		return retVal;
	}
	
	
}
