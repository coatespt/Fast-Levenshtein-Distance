package com.levenshtein;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.IDistance;
import com.levenshtein.leven.StringCompressorRH;
import com.levenshtein.leven.StringDistance;
import com.levenshtein.parent.TestAccuracyDriverParent;

/**
 * Compute LD for original stings, signatures, plus error rates based on 
 * the known LD of the strings.
 * <p>
 * TODO: This may be merged with TestSpeedForBigFiles or deleted
 * @author pcoates
 */
public class TestRegularVRolling101 extends TestAccuracyDriverParent {
	Logger log = Logger.getLogger(TestRegularVRolling101.class);

	static String outputAlphaString = 
			  "abcdefghijklmnopqrstuvwxyz"
			+ "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
			+ "1234567890"; 
	static int MIN_BITS=29;
	static int MAX_BITS=35;
	static int SEED=12345;
	static int C = 51;
	static int N = 20;

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


	@Test
	public void testModerateMangledC51() throws Exception {
		onFiles("Moderately mangled, c=51", infile1, infile2);
	}

	@Test
	public void testHeavilyMangledC51() throws Exception {
		onFiles("Heavily mangled c=51", infile1, infile3);
	}

	@Test
	public void testHeavily50_173_51() throws Exception {
		onFiles("50 of 173 lines deleted at random c=51", infile1, infile4);
	}

	@Test
	public void testOnSameSizeFiles51() throws Exception {
		onFiles("Same size 14k file with unrelated Latex content n=12  c=51",
				infile6, infile7);
	}
	
}

