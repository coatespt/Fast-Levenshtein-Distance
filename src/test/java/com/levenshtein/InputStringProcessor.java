package com.levenshtein;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.levenshtein.leven.utility.SWOR;
import com.levenshtein.leven.utility.exception.NoNextValueException;

public class InputStringProcessor {
	private String input = null;
	private Character [] charSet = null;

	/**
	 * Must be created with a non-null input string.
	 * @param input
	 */
	public InputStringProcessor(String input) {
		super();
		this.input = input;
		charSet=allChars(input);
	}

	/**
	 * Get a different version with about m characters mutated.
	 * @param m
	 * @return
	 */
	public String mutatedVersion(int m){
		return mutate(input,charSet,m,null);
	}
	
	/**
	 * Use the same random number sequence each time 
	 * @param m
	 * @return
	 */
	public String mutatedVersionRepeat(int m){
		return mutate(input,charSet,m, 7891011);
	}
	

	/**
	 * Return the number mutated for the last generated version.
	 * @return
	 */
	public int numMutated(){
		return mutatedCt;
	}
	

	/**
	 * return true in proportion to argument. 
	 * @param prob double 0<prob<1
	 * @return boolean t/f with stated probability
	 */
	private boolean trueOrFalse(double prob){
		if(rand.nextDouble() < prob){
			return true;
		}
		return false;
	}	

	private Random rand = new Random();

	private void initRand(int n){
		rand=new Random(n);
	}

	int randPos(int max){
		int r = Math.abs(rand.nextInt());
		return r%max;
	}	
	
	private int mutatedCt=0;
	
	
	public String generateRandomString(){
		return generateRndString(null);
	}

	public String generateRandomString(int s){
		return generateRndString(s);
	}
	
	private String generateRndString(Integer sz){
		int size;
		if(sz==null){
			size=input.length();
		}
		else{
			size=sz;
		}
		StringBuffer sb = new StringBuffer(size); 
		for(int i=0; i<size; i++){
			Character c = charSet[randPos(charSet.length)];
			sb.append(c);
		}
		return sb.toString();
	}

	private String mutate(String input, Character [] chars, int m, Integer seed){
		SWOR swor=null;
		if(seed!=null){
			swor = new SWOR(input.length(),seed);
		}
		else{
			swor = new SWOR(input.length());
		}
		List<Integer> mutate = new ArrayList<Integer>();
		for(int i=0; i<m; i++){
			try {
				mutate.add(swor.next());
			} catch (NoNextValueException e) {
				System.err.println("What?! SWOR failed!");
				e.printStackTrace();
			}
		}
		Collections.sort(mutate);
		Character [] inChars = allChars(input);
		StringBuffer sb = new StringBuffer();
		mutatedCt=0;
		double p=(double)m/input.length();
		int mutPos=0;
		for(int i=0; i<input.length();i++){	
			Character c = input.charAt(i);
			if(mutPos< mutate.size() && i==mutate.get(mutPos)){
				Character sub = inChars[randPos(inChars.length)];
				while(sub.equals(c)){
					sub = inChars[randPos(inChars.length)];
				}
				sb.append(sub);
				mutPos++;
				mutatedCt++;
			}
			else {
				sb.append(c);
			}
		}
		//log.info("Mutated:" + mutatedCt);
		return sb.toString();
	}
	

	private Character [] allChars(String input){
		Set<Character> s = new HashSet<Character>();
		for(int i=0; i<input.length();i++){
			Character c = input.charAt(i);
			s.add(c);
		}
		Character [] c = new Character[1];
		c = s.toArray(c);
		return c;
	}

	public static Map<String, Number> meanStdev(List<Integer> values){
		int min=Integer.MAX_VALUE;
		int max=Integer.MIN_VALUE;
		Map<String,Number> vals = new HashMap<String,Number>();
		int total = 0;
		double ssd = 0;
		for(Integer v : values){
			min=Math.min(min, v);
			max=Math.max(max, v);
			total+=v;
		}
		double mean = (double) total / values.size();
		for(Integer v : values){
			double diff = mean-v;
			double sqDiff = diff * diff;
			ssd+=sqDiff;
		}
		double var=ssd/values.size();
		double sd=Math.sqrt(var);
		vals.put("MEAN",mean);
		vals.put("VAR",var);
		vals.put("STDEV",sd);
		vals.put("COUNT",sd);
		vals.put("MIN",min);
		vals.put("MAX",max);
		return vals;
	}
	
}
