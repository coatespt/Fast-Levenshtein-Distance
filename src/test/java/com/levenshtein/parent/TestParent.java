package com.levenshtein.parent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.IDistance;
import com.levenshtein.leven.StringCompressorPlain;
import com.levenshtein.leven.StringDistance;

public class TestParent  extends TestCase{
	static Logger log = Logger.getLogger(TestParent.class);
	public int n = 12;
	public int c = 25;
	public static int copies = 100;
	// 5000
	public static String infile1 = "./data/infile1.txt"; 
	// 4936
	public static String infile2 = "./data/infile2.txt";
	// 2500
	public static String infile3 = "./data/infile3.txt";
	// 2524
	public static String infile4 = "./data/infile4.txt";
	//28120
	public static String infile5 = "./data/infile5.txt";
	// 27967
	public static String infile6 = "./data/infile6.txt";
	// 24390
	public static String infile7 = "./data/infile7.txt";
	//4743
	public static String infile8 = "./data/infile8.txt";
	// 152699
	public static String big = "./data/big.txt";

	// qbf-1
	public static String t1 = "The quick brown fox jumped over the very lazy dog,."
				+ " and each time the fox jumped he scared the zebra.";
	// qbf-2
	public static String t2 = "The quick brown fox jumped over the fat dog,"
				+ " and each time the fox jumped he scared the zebra.";
	

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

	private ICompressor compressor=null;
	/**
	 * Get the standard compressor (plain)
	 */
	protected ICompressor getCompressor(){
		if(compressor==null){
			compressor = new StringCompressorPlain();
		}
		compressor.setC(c);
		compressor.setN(n);
		return compressor;
	}
	
	private IDistance distance = null;
	/**
	 * Get the standard distance compute object.
	 * @return
	 */
	protected IDistance getDistance(){
		if(distance==null){
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
	 * @param fileName
	 */
	protected void dropFile(String fileName) {
		File file = new File(fileName);
		if(file.exists()){
			file.delete();
		}
	}

	/**
	 * Utility method to get the length of a file.
	 * @param fileName
	 * @return
	 */
	protected long fileLen(String fileName) {
		File file = new File(fileName);
		long len=-1;
		if(file.exists()){
			len=file.length();
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
	 * @param c
	 * @param n
	 * @param infile
	 * @return
	 * @throws Exception
	 */
	protected String compressToC(int c, int n, String infile)
			throws Exception {
				StringCompressorPlain comp = new StringCompressorPlain();
				String longOne = readFile(infile);
				totalCharsRead+=longOne.length();
				if(!CToSigTotal.containsKey(c)){
					CToSigTotal.put(c, 0L);
				}
				if(!CToReadTotal.containsKey(c)){
					CToReadTotal.put(c, 0L);
				}
				if(!CallsForC.containsKey(c)){
					CallsForC.put(c, 0L);
				}
				CallsForC.put(c, (CallsForC.get(c)+1));
				CToReadTotal.put(c, (CToReadTotal.get(c)+longOne.length()));
				String sig = comp.compress(longOne);
				CToSigTotal.put(c, (CToSigTotal.get(c)+sig.length()));
				totalSigsProduced+=sig.length();
				return sig;
			}
	/**
	 * The LD of two random strings of equal length is 
	 * almost always smaller than the strings because some chars will just happen 
	 * to line up. In practice, only very non-random strings will have an LD equal to their length.
	 * 
	 * This fudge factor is meant to account for this.  It is situationally dependent
	 * because of word usage patterns, boiler plate such as HTML header junk, etc.
	 * In the gutenberg books it can be large because they have pages and pages of 
	 * junk that is fairly standard.
	 * 
	 * 
	 * @param expected
	 * @return
	 */
	protected static double fudge(double expected) {
				double fudge = 0.87d;
		return expected * fudge;
	}

	protected static long totalCharsRead=0;
	protected static long totalSigsProduced=0;
	static Map <Integer,Long> CToSigTotal = new HashMap<Integer,Long>();
	static Map <Integer,Long> CToReadTotal = new HashMap<Integer,Long>();
	static Map <Integer,Long> CallsForC = new HashMap<Integer,Long>();
	
	protected static double compRateForSigs(int c) {
		double actualCompress=CToReadTotal.get(c)/CToSigTotal.get(c);
		return actualCompress;
	}

	protected static void clearCompressStats(){
		totalCharsRead=0;
		totalSigsProduced=0;
		CToReadTotal.clear();
		CToReadTotal.clear();
	}

	/**
	 * String with inputsize/cardinality-of-outputchars, actual average per char in output, least char ct, greates char ct  
	 * @param s
	 * @return
	 */
	protected static String minMax(String s) {
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
		int actual=s.length()/map.size();
		return " distinct:" + map.size()+ "\texpected: "+ (s.length()/outputChars.length) + "\tactual:" + actual + "\tmin-max:" + minChar + "/" + maxChar + " ct:" + map.get(minChar) + "/" + map.get(maxChar); 
	}
	

	protected static String outputCharString = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	protected static ICompressor comp = null;
	protected static char [] outputChars=null;
	protected static int MINBITS=31;
	protected static int MAXBITS=33;
	protected static int SEED=12345;
	static {
		outputChars=new char[outputCharString.length()];
		for(int i=0; i<outputCharString.length(); i++){
			outputChars[i]=outputCharString.charAt(i);
		}
	}

}
