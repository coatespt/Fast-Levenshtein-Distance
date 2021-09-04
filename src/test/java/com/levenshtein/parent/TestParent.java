package com.levenshtein.parent;

import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.IDistance;
import com.levenshtein.leven.StringCompressorRH;
import com.levenshtein.leven.StringDistance;
import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Parent class of all tests. Utility methods, default values, etc.
 */
public class TestParent  extends TestCase {
	//static Logger log = Logger.getLogger(TestParent.class);
	public int n = 12;
	public int c = 25;
	public static int copies = 100;

	// TODO: Separate the test data file from the filenames.
	// TODO: Allow the test methods to select the test set.
	//
	// 5000
	public static String infile1 = "./data/testfiles/infile1.txt";
	// 4936
	public static String infile2 = "./data/testfiles/infile2.txt";
	// 2500
	public static String infile3 = "./data/testfiles/infile3.txt";
	// 2524
	public static String infile4 = "./data/testfiles/infile4.txt";
	//28120
	public static String infile5 = "./data/testfiles/infile5.txt";
	// 27967
	public static String infile6 = "./data/testfiles/infile6.txt";
	// 24390
	public static String infile7 = "./data/testfiles/infile7.txt";
	//4743
	public static String infile8 = "./data/testfiles/infile10.txt";
	//public static String infile8 = "./data/testfiles/infile8.txt";
	// 152699
	public static String big = "./data/big.txt";

	// qbf-1
	public static String t1 = "The quick brown fox jumped over the very lazy dog,."
			+ " and each time the fox jumped he scared the zebra.";
	// qbf-2
	public static String t2 = "The quick brown fox jumped over the fat dog,"
			+ " and each time the fox jumped he scared the zebra.";

	// 44 25.6K files "split" from two unrelated 1MB Gutenberg books by different authors.
	// All leading and trailing boilerplate removed from because it is very similar for all works.
	// One book is a history of china the other is a Jules Verne novel.
	protected String [] fileSet={
			"xab", "xad", "xaf", "xah", "xaj", "xal", "xan", "xap", "xar",
			"xat", "xav", "xax", "xaz", "xbb", "xbd", "xbf", "xbh", "xbj",
			"xbl", "xbn", "xbp", "xbr", "xaa", "xac", "xae", "xag", "xai",
			"xak", "xam", "xao", "xaq", "xas", "xau", "xaw", "xay", "xba",
			"xbc", "xbe", "xbg", "xbi", "xbk", "xbm", "xbo", "xbq"
	};
	protected String loc="./data/many/";
	protected String set1path=loc + "china/";
	protected String set2path=loc + "verne/";


	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public int getC() {
		return c;
	}

	public void setC(int c) {
		this.c = c;
	}

	private ICompressor compressor = null;

	int minBits=25;
	int maxBits=39;
	int seed=12345;
	/**
	 * Get the standard compressor (plain)
	 */

	protected ICompressor getCompressor() {
		if (compressor == null) {
			//compressor = new StringCompressorPlain();
			compressor = new StringCompressorRH(getN(), getC(), outputChars , minBits, maxBits, seed);
			}
		compressor.setC(c);
		compressor.setN(n);
		return compressor;
	}

	private IDistance distance = null;

	/**
	 * Get the standard distance compute object.
	 *
	 * @return
	 */
	protected IDistance getDistance() {
		if (distance == null) {
			distance = new StringDistance();
		}
		return distance;
	}

	public static int ITERATIONS = 100000;
	/**
	 * Maximum compression to test in test grid
	 */
	protected static int CMAX = 220;
	/**
	 * Increment of increase for C in test grid
	 */
	protected static int CINC = 25;
	/**
	 * Largest N to test in grid
	 */
	protected static int MAX_N = 21;
	/**
	 * Increment of increase for N in test grid
	 */
	protected static int N_INC = 3;
	/**
	 * Number of compression cycles to execute for compression tests
	 * on big files.
	 */
	public static int COMPRESSIONS = 10000;

	protected static int defaultN = 12;

	/**
	 * Utility method to read a file into a string.
	 *
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static String readFile(String filename) throws Exception {
		StringBuffer sb = new StringBuffer();
		File f = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String in;
		while ((in = reader.readLine()) != null) {
			sb.append(in);
			sb.append(" ");
		}
		reader.close();
		return sb.toString();
	}

	/**
	 * Utility method to return the length of the longer of two strings.
	 *
	 * @param one
	 * @param two
	 * @return
	 */
	public static int longerOf(String one, String two) {
		String longer = one;
		if (two.length() > one.length()) {
			longer = two;
		}
		return longer.length();
	}

	/**
	 * Utility method to return the size of a given file in bytes.
	 *
	 * @param fname
	 * @return
	 * @throws Exception
	 */
	public static int fileSize(String fname) throws Exception {
		String contents = readFile(fname);
		return contents.length();
	}


	/**
	 * Utility method to drop a file
	 *
	 * @param fileName
	 */
	protected void dropFile(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * Utility method to get the length of a file.
	 *
	 * @param fileName
	 * @return
	 */
	protected long fileLen(String fileName) {
		File file = new File(fileName);
		long len = -1;
		if (file.exists()) {
			len = file.length();
		}
		return len;
	}

	/**
	 * Extract the contents of a file as a string.
	 * @param fpath
	 * @return
	 * @throws Exception
	protected String getFileConents(String fpath) throws Exception {
	return FileAndTimeUtility.getFileContents(fpath);
	}
	 */

	/**
	 * Chop off the path and return just the file name
	 *
	 * @param pathname
	 * @return
	 */
	protected String justFileName(String pathname) {
		File f = new File(pathname);
		return f.getName();
	}

	/**
	 * Convenience method to create signature for a file.
	 * Compress the specified input file to level c with neighborhood size n.
	 *
	 * @param c
	 * @param n
	 * @param infile
	 * @return
	 * @throws Exception
	 */
	protected String compressToC(int c, int n, String infile)
			throws Exception {
		//StringCompressorPlain comp = new StringCompressorPlain();
		ICompressor comp = new StringCompressorRH(c, n, outputChars , minBits, maxBits, seed);
		comp.setC(c);
		comp.setN(n);
		String longOne = readFile(infile);
		totalCharsRead += longOne.length();
		if (!CToSigTotal.containsKey(c)) {
			CToSigTotal.put(c, 0L);
		}
		if (!CToReadTotal.containsKey(c)) {
			CToReadTotal.put(c, 0L);
		}
		if (!CallsForC.containsKey(c)) {
			CallsForC.put(c, 0L);
		}
		CallsForC.put(c, (CallsForC.get(c) + 1));
		CToReadTotal.put(c, (CToReadTotal.get(c) + longOne.length()));
		String sig = comp.compress(longOne);
		CToSigTotal.put(c, (CToSigTotal.get(c) + sig.length()));
		totalSigsProduced += sig.length();
		return sig;
	}

	/**
	 * The LD of two random strings of equal length is
	 * almost always smaller than the strings because some chars will just happen
	 * to line up. In practice, only very non-random strings will have an LD equal to their length.
	 * <p>
	 * This fudge factor is meant to account for this.  It is situationally dependent
	 * because of word usage patterns, boiler plate such as HTML header junk, etc.
	 * In the gutenberg books it can be large because they have pages and pages of
	 * junk that is fairly standard.
	 *
	 * @param expected
	 * @return
	 */
	protected static double fudge(double expected) {
		double fudge = 0.87d;
		return expected * fudge;
	}

	protected static long totalCharsRead = 0;
	protected static long totalSigsProduced = 0;
	static Map<Integer, Long> CToSigTotal = new HashMap<Integer, Long>();
	static Map<Integer, Long> CToReadTotal = new HashMap<Integer, Long>();
	static Map<Integer, Long> CallsForC = new HashMap<Integer, Long>();

	protected static double compRateForSigs(int c) {
		double actualCompress = CToReadTotal.get(c) / CToSigTotal.get(c);
		return actualCompress;
	}

	protected static void clearCompressStats() {
		totalCharsRead = 0;
		totalSigsProduced = 0;
		CToReadTotal.clear();
		CToReadTotal.clear();
	}

	protected static String minMax(String s) {
		Map<Character, Integer> map = new HashMap<Character, Integer>();
		for (int i = 0; i < s.length(); i++) {
			Character c = s.charAt(i);
			if (!map.containsKey(c)) {
				map.put(c, 0);
			}
			map.put(c, map.get(c) + 1);
		}
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		Character minChar = null;
		Character maxChar = null;
		for (Character c : map.keySet()) {
			int ct = map.get(c);
			if (min > ct) {
				min = ct;
				minChar = c;
			}
			if (max < ct) {
				max = ct;
				maxChar = c;
			}
		}
		return "distinct:" + map.size() + "\tcharset: " + outputChars.length +
				"\tmin reps:" + minChar + "\tct:" + map.get(minChar) +
				"\tmax reps:" + maxChar + "\tct:" + map.get(maxChar);
	}

	public Map<Integer,Double> meanStdevForDict(Map<Character,Integer> cts ){
		int total = 0;
		Map<Integer,Double> ret = new HashMap<Integer,Double>();
		for(Character c : cts.keySet()){
			total = total + cts.get(c);
		}
		double mean = total/cts.size();
		double ssd = 0;
		for(Character c : cts.keySet()){
			int diff = (int) (mean - cts.get(c));
			ssd = ssd + (diff * diff);
		}
		double var = ssd/cts.size();
		ret.put(MEAN_COUNT,mean);
		ret.put(VARIANCE_COUNT,var);
		ret.put(STDEV_COUNT,Math.sqrt(var));
		return ret;
	}

	static public Integer DISTINCT_CHARS = 1;
	static public Integer OUTPUT_CARD= 2;
	static public Integer LEAST_FREQUENT = 3;
	static public Integer LEAST_FREQUENT_COUNT = 4;
	static public Integer MOST_FREQUENT = 5;
	static public Integer MOST_FREQUENT_COUNT = 6;
	static public Integer MEAN_COUNT = 7;
	static public Integer VARIANCE_COUNT = 8;
	static public Integer STDEV_COUNT = 9;
	/**
	 *
	 * @param s A signature
	 * @return Map of some statistics useful for verification.
	**/
	protected static Map<Integer, Integer> minMaxDictionary(String s) {
		Map<Integer,Integer> retMap = new HashMap<Integer,Integer>();
		Map<Character, Integer> map = new HashMap<Character,Integer>();
		for(int i=0; i<s.length();i++){
			Character c=s.charAt(i);
			if(!map.containsKey(c)){
				map.put(c,0);
			}
			map.put(c, map.get(c)+1);
		}
		int min=Integer.MAX_VALUE;
		int max=Integer.MIN_VALUE;
		Character minChar=null;
		Character maxChar=null;
		for(Character c : map.keySet()){
			int ct = map.get(c);
			if(min>ct){
				min=ct;
				minChar=c;
			}
			if(max<ct){
				max=ct;
				maxChar=c;
			}
		}
		retMap.put(DISTINCT_CHARS,map.size());
		retMap.put(OUTPUT_CARD,outputChars.length);
		retMap.put(LEAST_FREQUENT,Character.getNumericValue(minChar));
		retMap.put(LEAST_FREQUENT_COUNT,map.get(minChar));
		retMap.put(MOST_FREQUENT,Character.getNumericValue(maxChar));
		retMap.put(MOST_FREQUENT_COUNT,map.get(maxChar));
		return retMap;
	}

	// TODO This string could have more characters. This is only 62. You could add in puctuation, etc.
	// TODO Might be useful to make it setable.
	protected static String outputCharString = "abcdefghijklmnopqrstuvwxyzABCDEFGHI" +
			"JKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_=+{}[]?><.";
	protected static ICompressor comp = null;
	protected static char [] outputChars=null;
	protected static int MINBITS=31;
	protected static int MAXBITS=33;
	protected static int SEED=12345;


	protected static void setOutputChars(String str){
		outputChars = new char[str.length()];
		for(int i=0; i<str.length(); i++){
			outputChars[i]=str.charAt(i);
		}
	}

	static {
		setOutputChars(outputCharString);
	}
}
