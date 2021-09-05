package utilities.mechanic;

import java.util.HashMap;
import java.util.Map;

public class EntropyCalc {
	// TODO: Index of Coincidence might be better suited to the purpose than Shannon entropy.

	/**
	 * Compute the Shannon entropy of a large input string.
	 * Did I write this or crib it from somewhere?
	 *
	 * @author pcoates
	 */
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
