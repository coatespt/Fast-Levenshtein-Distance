package com.levenshtein;

import org.apache.log4j.Logger;

import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.IDistance;
import com.levenshtein.leven.StringCompressorRH;
import com.levenshtein.leven.StringDistance;
import com.levenshtein.parent.TestAccuracyDriverParent;

/**
 * 
 * Drives TestAccuracyDriverParent routines for computing LD raw, LD Signatures, 
 * error rates, and testing ScoreDistance using the original plain hashing algorithm.
 * 
 * Test overall behavior using rolling has that XOR's longs in and out of an accumulator. 
 * Runs 8 different files against each other in every combination;
 * 
 * The compressor using the RollingHash requires some setup.
 * 
 * @author pcoates
 */
public class TestAccuracyDriverRH101 extends TestAccuracyDriverParent {
	Logger log = Logger.getLogger(TestAccuracyDriverRH101.class);

	/**
	 *  The alphabet of characters for the output.
	 */
	static String outputAlphaString = 
			  "abcdefghijklmnopqrstuvwxyz"
			+ "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
			+ "1234567890"; 
	/**
	 * N-Character neighborhoods map to one of 512 pseudo random longs that are XOR'ed together, with the N+1th oldest XOR'ed out each time  
	 * The longs they map to must be approximately equal number of set and unset bits. 
	 */
	static int MIN_BITS=29;
	static int MAX_BITS=35;

	static int SEED=12345;

	static int C = 101;
	static int N = 8;

	static char []outputAlphabet = null;


	private IDistance distance = null;
	public IDistance getDistance(){
		if(distance==null){
			distance = new StringDistance();
		}
		return distance;
	}
	
	private ICompressor compressor;
	public ICompressor getCompressor(){
		if(compressor==null){
			outputAlphabet=new char[outputAlphaString.length()];
			for(int i=0;i<outputAlphaString.length();i++){
				char cc = outputAlphaString.charAt(i);
				outputAlphabet[i]=cc;
			}
			compressor = new StringCompressorRH(N, C, outputAlphabet, MIN_BITS, MAX_BITS, SEED);
		}
		return compressor;
	}

	public String [] files = {infile1,infile1,infile3,infile4, infile5, infile6, infile7, infile8};
	
	public void testAllAgainstAll101() throws Exception{
		for(String f1 : files){
			for(String f2 : files){
				StringBuffer sb = new StringBuffer();
				sb.append("C: ");
				sb.append(c);
				sb.append("n:");
				sb.append(" file1: ");
				sb.append(f1);
				sb.append(" file2: ");
				sb.append(f2);
				onFiles(sb.toString(), f1, f2);
			}
		}
	}

	
}
