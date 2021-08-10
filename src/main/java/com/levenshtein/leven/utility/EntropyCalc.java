package com.levenshtein.leven.utility;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: I think this was from somewhere! Where?  find our or rewrite.
 *
 * Compute the Shannon entropy of a large input string.
 * 
 * @author pcoates
 */
public class EntropyCalc {
	
	public static Double calculateShannonEntropy(String str) {
		  Map<Character, Integer> map = new HashMap<Character, Integer>();
		  for (int i=0; i<str.length();i++) {
			Character c = str.charAt(i);
		    if (!map.containsKey(c)) {
		      map.put(c, 0);
		    }
		    map.put(c, map.get(c) + 1);
		  }
		  long total=0;
		  for (Character c : map.keySet()) {
			  total+=map.get(c);
		  }
		  Double result = 0.0;
		  for (Character c : map.keySet()) {
		    Double frequency = (double) map.get(c) / total;
		    result -= frequency * (Math.log(frequency) / Math.log(2));
		  }
		  return result;
		}
}
