package com.levenshtein;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.levenshtein.leven.StringCompressorRH;
import com.levenshtein.leven.utility.TimeAndRate;
import com.levenshtein.parent.TestParent;


/**
 * Some simple sanity checking for basic operations
 * 
 * @author pcoates
 *
 */
public class TestRollingHash extends TestParent {

	static Logger log = Logger.getLogger(TestRollingHash.class);
	public static boolean MINIMAL_OUPUT=true;

	/**
	 * Test that the compression is always within 10% of the expected size; 
	 *
	 * @throws Exception
	 */
	@Test
	public void testCompressionRH() throws Exception {
		log.info("testCompressionRH() starting.");
		int N=8;
		double eps=0.2d;
		int fsize=fileSize(big);
		for(int i=51; i<150; i+=2){
			int compSize=_testCompressionRH(big, i, 8);
			int ratio=fsize/compSize;
			System.out.println("i:" + i + " fsize:" + fsize + " ideal:" + (fsize/i) + " actual:" + compSize + " ratio:" + ratio );
			assertTrue(ratio<i+(i/10.0d));
			assertTrue(ratio>i-(i/10.0d));
		}
		log.info("testCompressionRH() ending.");
	}

	

	public int _testCompressionRH(String fname, int c, int n) throws Exception {
		if(!MINIMAL_OUPUT){
			log.info("_testCompressionRH()  Testing compression on a large string with c:" + c + " n: " + n);
		}
		comp=new StringCompressorRH(n, c, outputChars, MINBITS, MAXBITS, SEED);
		String longOne = readFile(fname);
		TimeAndRate tAndR = new TimeAndRate();
		String compressed = comp.compress(longOne);
		tAndR.event();
		tAndR.compute();
		int lenComp = compressed.length();
		int expectedLen = longOne.length()/c;
		double ratio = lenComp/(double)expectedLen;
		double error = Math.abs(lenComp-expectedLen)/(double) expectedLen;

		if(!MINIMAL_OUPUT){
		StringBuffer sb = new StringBuffer();
		sb.append("Compressed (RH) big string document ");
		sb.append("\n\tneighborhood:"); 
		sb.append(n);
		sb.append("\n\ttime(rnd down to nearest millisec):"); 
		sb.append(tAndR.elapsedMS());
		sb.append("\n\ttext len:");
		sb.append(longOne.length());
		sb.append("\n\ttexpected sig len:");
		sb.append(expectedLen);
		sb.append("\n\tactual sig len:");
		sb.append(compressed.length());
		sb.append("\n\tnominal compression:");
		sb.append(c);
		sb.append("\n\tactual/nominal:");
		sb.append(ratio);
		sb.append("\n\terror:");
		sb.append(error);
		System.out.println(sb.toString());
		System.out.println(minMax(compressed));
		System.out.println("\tCompressed string:" + compressed);
		}
		if(MINIMAL_OUPUT){
			System.out.println("_testCompressionRH() "+ minMax(compressed)+"\tc:"+ c + "\tactual/nominal:" + String.format("%.4f",ratio) + "\terror:" + String.format("%.4f",error) + "\tsig:" + compressed);
		}
		return lenComp;
	}


}