/**
 * 
 */
package utilities.util;

import utilities.exception.BadValueException;
import utilities.file.CSVLogWriter;
import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.util.*;

/**
 */
public class Utility {
	static Logger log = Logger.getLogger(Utility.class);
	private static char BACKSLASH = '\\';
	static String DOUBLES_FORMAT = "##.####";
	static int DOUBLES_PRINT_WIDTH = 20;

	/**
	 * Print out an array of pairs for diagnostic purposes.
	 * 
	 * @param header
	 * @param m
	 * @param d
	 */
	public static void printDualDoublesArray(String header, int m, double d[][]) {
		log.info("PRINTING DUAL DOUBLES ARRAY");
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		DecimalFormat df = new DecimalFormat(DOUBLES_FORMAT);
		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		for (int i = 0; i < m; i++) {
			sb.append("(");
			sb.append(df.format(d[i][0]));
			sb.append(",");
			sb.append(df.format(d[i][1]));
			sb.append("), ");
			if (((i + 1) % (DOUBLES_PRINT_WIDTH / 2)) == 0) {
				sb.append("\n");
			}
			min = Math.min(((d[i][0] + d[i][1]) / 2), min);
			max = Math.max(((d[i][0] + d[i][1]) / 2), max);
		}
		sb.append("\n");
		StringBuffer sb2 = new StringBuffer();
		sb2.append("\n");
		sb2.append(header);
		sb2.append(" MIN:");
		sb2.append(min);
		sb2.append(" MAX:");
		sb2.append(max);
		sb2.append(" SIZE:");
		sb2.append(d.length);
		sb2.append(" M:");
		sb2.append(m);
		sb2.append(" ");
		sb2.append(sb);
		System.out.println(sb2.toString());
	}

	/**
	 * Print out a list of pairs, preceded by a header.
	 * 
	 * @param header
	 * @param m
	 * @param d
	 */
	public static void printDoublesArray(String header, int m, double d[]) {
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		DecimalFormat df = new DecimalFormat(DOUBLES_FORMAT);
		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		for (int i = 0; i < m; i++) {
			min = Math.min(min, d[i]);
			max = Math.max(max, d[i]);
			sb.append(df.format(d[i]));
			sb.append(", ");
			if (((i + 1) % DOUBLES_PRINT_WIDTH) == 0) {
				sb.append("\n");
			}
		}
		sb.append("\n");
		StringBuffer sb2 = new StringBuffer();
		sb2.append("\n");
		sb2.append(header);
		sb2.append("MIN:");
		sb2.append(min);
		sb2.append(" MAX:");
		sb2.append(max);
		sb2.append(" SIZE:");
		sb2.append(d.length);
		sb2.append(" M:");
		sb2.append(m);
		sb2.append(" ");
		sb2.append(sb);
		System.out.println(sb2.toString());
	}

	public static void printMeanAndStdev(String header, double[] d) {
		double sum = 0;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (int i = 0; i < d.length; i++) {
			sum += d[i];
			min = Math.min(min, d[i]);
			max = Math.max(max, d[i]);
		}
		double mean = sum / d.length;
		double var = 0;
		for (int i = 0; i < d.length; i++) {
			var += (d[i] - mean) * (d[i] - mean);
		}
		var /= d.length;
		double sd = Math.sqrt(var);
		StringBuffer sb = new StringBuffer();
		sb.append(" array size:");
		sb.append(d.length);
		sb.append(" mean:");
		sb.append(mean);
		sb.append(" variance:");
		sb.append(var);
		sb.append(" standared deviation:");
		sb.append(sd);
		sb.append(" min:");
		sb.append(min);
		sb.append(" max:");
		sb.append(max);
		System.out.println(header + sb.toString());
	}

	public static void printMeanAndStdev(String header, int[] d, int winSize) {
		int sum = 0;
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < d.length; i++) {
			sum += d[i];
			min = Math.min(min, d[i]);
			max = Math.max(max, d[i]);
		}
		double mean = sum / d.length;
		double proportion = mean / (double) winSize;
		double var = 0;
		for (int i = 0; i < d.length; i++) {
			var += (d[i] - mean) * (d[i] - mean);
		}
		var /= d.length;
		double sd = Math.sqrt(var);
		StringBuffer sb = new StringBuffer();
		sb.append(" array size:");
		sb.append(d.length);
		sb.append(" mean:");
		sb.append(mean);
		sb.append(" variance:");
		sb.append(var);
		sb.append(" standared deviation:");
		sb.append(sd);
		sb.append(" min:");
		sb.append(min);
		sb.append(" max:");
		sb.append(max);
		sb.append(" proportion:");
		sb.append(proportion);

		System.out.println(header + sb.toString());
	}

	/**
	 * Dump a map to a comma/NL-delimited file with rows of the form
	 * {ID,Set<ID>}
	 * 
	 * @param map
	 * @param fname
	 * @throws Exception
	 */
	public static void writeIntegerMapToFile(Map<Integer, Set<Integer>> map,
			String fname) throws Exception {
		CSVLogWriter csvlog = new CSVLogWriter(fname);
		for (Integer key : map.keySet()) {
			StringBuffer sb = new StringBuffer(map.get(key).size() * 5 + 10);
			sb.append(key);
			sb.append(",");
			int ct = 0;
			int size = map.get(key).size();
			for (Integer i : map.get(key)) {
				ct++;
				sb.append(i);
				if (ct < size) {
					sb.append(",");
				}
			}
			csvlog.writeNL(sb.toString());
		}
	}

	public static void writeStringMapToFile(Map<Integer, Set<String>> map,
			String fname) throws Exception {
		CSVLogWriter csvlog = new CSVLogWriter(fname);
		for (Integer key : map.keySet()) {
			StringBuffer sb = new StringBuffer(map.get(key).size() * 5 + 10);
			sb.append(key);
			sb.append(",");
			int ct = 0;
			int size = map.get(key).size();
			for (String i : map.get(key)) {
				ct++;
				sb.append(i);
				if (ct < size) {
					sb.append(",");
				}
			}
			csvlog.writeNL(sb.toString());
		}
	}

	/**
	 * Print out the statistics for a full Map-file run.
	 * 
	 * @param map
	 */
	public static void printFullStats(Map<Integer, int[]> map,
			boolean zeroOutput) {
		long vals = 0;
		long valsMax = 0;
		long keys = map.keySet().size();
		if (zeroOutput) {
			return;
		}
		for (Integer i : map.keySet()) {
			int[] values = map.get(i);
			vals += values.length;
			valsMax = Math.max(valsMax, values.length);
		}
		float average = vals / (float) keys;
		StringBuffer sb = new StringBuffer();
		sb.append("\ntotal keys:");
		sb.append(keys);
		sb.append("\tvalues:");
		sb.append(vals);
		sb.append("\tmax:");
		sb.append(valsMax);
		sb.append("\t average:");
		sb.append(average);
		log.info(sb.toString());
	}

	/**
	 * Convert a list of strings to a list of Integers.
	 * 
	 * @param inList
	 * @return
	 * @throws BadValueException
	 */
	public static List<Integer> stringListToIntList(List<String> inList)
			throws BadValueException {
		List<Integer> outList = new ArrayList<Integer>();
		try {
			for (String s : inList) {
				s = s.trim();
				outList.add(Integer.parseInt(s));
			}
		} catch (NumberFormatException nfx) {
			log.info("stringListToIntList() got bad value--check if its a file header left in by sql dump");
			throw new BadValueException("stringListToIntList() got bad value:"
					+ nfx.getMessage());
		}
		return outList;
	}

	/**
	 * Take a list of strings,
	 * 
	 * @param inList
	 * @param isInt
	 * @return
	 * @throws BadValueException
	 */
	public static List<Integer> stringListToObjectList(List<String> inList,
			int isInt) throws BadValueException {
		List<Integer> outList = new ArrayList<Integer>();
		try {
			for (String s : inList) {
				s = s.trim();
				outList.add(Integer.parseInt(s));
			}
		} catch (NumberFormatException nfx) {
			throw new BadValueException("stringListToIntList() got bad value:");
		}
		return outList;
	}

	public static int[] integerSetToIntArray(Set<Integer> inList)
			throws BadValueException {
		List<Integer> outList = new ArrayList<Integer>();
		outList.addAll(inList);
		Collections.sort(outList);
		int[] intArray = new int[outList.size()];
		for (int i = 0; i < outList.size(); i++) {
			intArray[i] = outList.get(i);
		}
		return intArray;
	}

	public static List<String> getTokenListFromString(String csv)
			throws Exception {
		return getTokenListFromString(csv, ',');
	}

	public static List<String> getTokenListFromString(String csv, char delim)
			throws Exception {
		List<String> list = new ArrayList<String>();
		int len = csv.length();
		int position = 0;

		while (position < len) {
			StringBuffer sb = new StringBuffer();
			while (position < len) {
				char c = csv.charAt(position++);
				if (c == BACKSLASH) {
					sb.append(c);
					if (position < len) {
						c = csv.charAt(position++);
						sb.append(c);
					} else {
						break;
					}
				} else if (c == delim) {
					break;
				} else {
					sb.append(c);
				}
			}
			list.add(sb.toString());
		}
		return list;
	}

	/**
	 * Take two lists and return a sorted list of the union of their elements.
	 * 
	 * @param listOne
	 * @param listTwo
	 * @return
	 */
	private static List<String> makeCombineList(List<String> listOne,
			List<String> listTwo) {
		Set<String> combined = new HashSet<String>();
		combined.addAll(listOne);
		combined.addAll(listTwo);
		List<String> comboList = new ArrayList<String>();
		comboList.addAll(combined);
		Collections.sort(comboList);
		return comboList;
	}

	private static List<String> makeUnionList(List<String> listOne,
			List<String> listTwo) {
		Set<String> string1 = new HashSet<String>();
		string1.addAll(listOne);
		string1.addAll(listTwo);
		List<String> unionList = new ArrayList<String>();
		unionList.addAll(string1);
		Collections.sort(unionList);
		return unionList;
	}

	private static List<String> makeIntersectionList(List<String> listOne,
			List<String> listTwo) {
		Set<String> string1 = new HashSet<String>();
		Set<String> string2 = new HashSet<String>();
		string1.addAll(listOne);
		string2.addAll(listTwo);
		Set<String> intersectionSet = new HashSet<String>();
		for (String s : string1) {
			if (string2.contains(s)) {
				intersectionSet.add(s);
			}
		}
		List<String> intersectionList = new ArrayList<String>();
		intersectionList.addAll(intersectionSet);
		Collections.sort(intersectionList);
		return intersectionList;
	}

	/**
	 * Make a list of blank strings the same size as the elements of the sorted
	 * combo list.
	 * 
	 * @param comboList
	 * @return
	 */
	private static List<String> makeBlankList(List<String> comboList) {
		List<String> newList = new ArrayList<String>();
		for (String str : comboList) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < str.length(); i++) {
				sb.append(" ");
			}
			newList.add(sb.toString());
		}
		return newList;
	}

	private static Map<String, Integer> makeStringMap(List<String> comboList) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < comboList.size(); i++) {
			String s = comboList.get(i);
			map.put(s, i);
		}
		return map;
	}

	/**
	 * Take two list with the correct spacing entries and construct two
	 * vertically aligned strings.
	 * 
	 * @param map
	 * @param comboList
	 * @param wordList
	 * @return
	 */
	private static StringBuffer makeSpacedString(Map<String, Integer> map,
			List<String> comboList, List<String> wordList) {
		StringBuffer sb = new StringBuffer();
		List<String> blanks = makeBlankList(comboList);
		for (String s : wordList) {
			int i = map.get(s);
			blanks.set(i, s);
		}
		for (int i = 0; i < blanks.size(); i++) {
			sb.append(blanks.get(i));
			if (i != blanks.size() - 1) {
				sb.append(", ");
			}
		}
		return sb;
	}

	private static String makeList(List<String> list) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			String s = list.get(i);
			sb.append(s);
			if (i < (list.size() - 1)) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	public static String alignTwoStrings(String s1, String s2) throws Exception {
		List<String> listOne = getTokenListFromString(s1, ' ');
		List<String> listTwo = getTokenListFromString(s2, ' ');
		String aligned = alignedListsJustAlign(listOne, listTwo, "WORDS_1",
				"WORDS_2");
		return aligned;
	}

	/**
	 * Take two lists of words. Write both lists out in sorted order, one above
	 * the other, with each word that appears in both lists aligned over it's
	 * counter part. Also prints out the union of the lists and the intersection
	 * of the lists.
	 * 
	 * @param listOne
	 * @param listTwo
	 * @return
	 */
	public static String alignedLists(List<String> listOne,
			List<String> listTwo, Object idOne, Object idTwo) {
		List<String> intersection = makeIntersectionList(listOne, listTwo);
		List<String> union = makeUnionList(listOne, listTwo);
		String intersectString = makeList(intersection);
		String unionString = makeList(union);
		List<String> comboList = makeCombineList(listOne, listTwo);
		Map<String, Integer> map = makeStringMap(comboList);
		StringBuffer listString = new StringBuffer();
		listString.append(idOne);
		listString.append(":\t");
		listString.append(makeSpacedString(map, comboList, listOne));
		listString.append("\n");
		listString.append(idTwo);
		listString.append(":\t");
		listString.append(makeSpacedString(map, comboList, listTwo));
		listString.append("\n");
		listString.append("intersection:");
		listString.append(intersectString);
		listString.append("\n");
		listString.append("union:    ");
		listString.append(unionString);
		listString.append("\n");
		return listString.toString();
	}

	public static String alignedListsJustAlign(List<String> listOne,
			List<String> listTwo, Object idOne, Object idTwo) {
		List<String> comboList = makeCombineList(listOne, listTwo);
		Map<String, Integer> map = makeStringMap(comboList);
		StringBuffer listString = new StringBuffer();
		listString.append(idOne);
		listString.append(":\t");
		listString.append(makeSpacedString(map, comboList, listOne));
		listString.append("\n");
		listString.append(idTwo);
		listString.append(":\t");
		listString.append(makeSpacedString(map, comboList, listTwo));
		listString.append("\n");
		return listString.toString();
	}

	/**
	 * Normalize the scores in each of the value arrays in a map to the range
	 * [0.0d,1.0d].
	 * 
	 * Normalization is with respect to the individual arrays.
	 * 
	 * @param map
	 * @return
	 */
	public static Map<Integer, double[]> normalizeScores(Map<Integer, int[]> map) {
		// iterate the set of count maps
		Map<Integer, double[]> retmap = new HashMap<Integer, double[]>();
		for (Integer key : map.keySet()) {
			int[] array = map.get(key);
			int max = findMax(array);
			double[] normarray = new double[array.length];
			for (int i = 0; i < array.length; i++) {
				normarray[i] = (double) array[i] / (double) max;
			}
			retmap.put(key, normarray);
		}
		return retmap;
	}

	/**
	 * Find the maximum in an array of integers.
	 * 
	 * @param array
	 * @return
	 */
	public static int findMax(int[] array) {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < array.length; i++) {
			max = Math.max(max, array[i]);
			min = Math.min(min, array[i]);
		}
		log.info("array min:" + min + " array max:" + max + " size:"
				+ array.length);
		return max;
	}

	/**
	 * Find the minimum of an array of integers.
	 * 
	 * @param array
	 * @return
	 */
	public static int findMin(int[] array) {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < array.length; i++) {
			max = Math.max(max, array[i]);
			min = Math.min(min, array[i]);
		}
		log.info("array min:" + min + " array max:" + max + " size:"
				+ array.length);
		return min;
	}

	/**
	 * Compute the mean of an array of doubles.
	 * 
	 * @param array
	 * @return
	 */
	public static double findMean(double[] array) {
		double total = 0.0;
		for (int i = 0; i < array.length; i++) {
			total += array[i];
		}
		double mean = total / array.length;
		return mean;
	}

	/**
	 * Compute the mean of an array of integers as a double.
	 * 
	 * @param array
	 * @return
	 */
	public static double findMean(int[] array) {
		double total = 0.0;
		for (int i = 0; i < array.length; i++) {
			total += array[i];
		}
		double mean = total / array.length;
		return mean;
	}

	/**
	 * Compute the standard deviation of double values in an array.
	 * 
	 * @param array
	 * @return
	 */
	public static double findStdev(double[] array) {
		double mean = findMean(array);
		double var = 0.0d;
		for (int i = 0; i < array.length; i++) {
			double diff = mean - array[i];
			var += diff * diff;
		}
		double stdev = Math.sqrt(var);
		log.info("standard dev:" + stdev);
		return stdev;
	}

}
