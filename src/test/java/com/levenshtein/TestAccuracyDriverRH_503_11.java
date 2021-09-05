package com.levenshtein;

import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.IDistance;
import com.levenshtein.leven.StringCompressorRH;
import com.levenshtein.leven.StringDistance;
import com.levenshtein.parent.TestAccuracyDriverParent;

// TODO Need a speed test of the hashes.
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
 * Use this as a prototype for any similar tests including naming convention.
 * You are required to implement getDistance() and getCompressor() but getDistance is
 * no just a pass through for the nonce.
 *
 * @author pcoates
 */
public class TestAccuracyDriverRH_503_11 extends TestAccuracyDriverParent {
	// Params unique to this run

	static int MIN_BITS=29;
	static int MAX_BITS=35;
	static int SEED=12345;
	static int C = 503;
	static int N = 11;
	public String [] files = {infile1,infile1,infile3,infile4, infile5, infile6, infile7, infile8};

	private IDistance distance = null;
	@Override
	public IDistance getDistance(){
		if(distance==null){
			distance = new StringDistance();
		}
		return distance;
	}
	
	private ICompressor compressor;
	@Override
	protected ICompressor getCompressor(){
		if(compressor==null){
			compressor = new StringCompressorRH(getC(), getN(), outputChars , MIN_BITS, MAX_BITS, SEED);
		}
		compressor.setC(c);
		compressor.setN(n);
		return compressor;
	}

	/**
	 * Compute the LD for each file in a set against each other file and print out some relevant
	 * numbers such as expected distance for random files of the given lengths, the actual LD of the
	 * pair of files, the estimated LD of the files made from their signatures, and the error.
	 *
	 * This only fails if it blows up. We assert nothing.
	 *
	 * @throws Exception
	 */
	public void testAllAgainstAll101() throws Exception {
		setC(1007);
		setN(17);
		for(int i=0; i<files.length; i++){
			for(int j=i; j<files.length; j++){
				String  f1 = files[i];
				String  f2 = files[j];
				StringBuffer sb = new StringBuffer();
				sb.append("C: ");
				sb.append(getC());
				sb.append(" n:");
				sb.append(getN());
				sb.append(" file1: ");
				sb.append(f1);
				sb.append(" file2: ");
				sb.append(f2);
				onFiles(sb.toString(), f1, f2);
			}
		}
	}
}
