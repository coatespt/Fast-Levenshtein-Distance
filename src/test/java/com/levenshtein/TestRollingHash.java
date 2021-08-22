package com.levenshtein;

//import org.apache.log4j.Logger;
import org.junit.Test;

import com.levenshtein.leven.StringCompressorRH;
import utilities.mechanic.TimeAndRate;
import com.levenshtein.parent.TestParent;


/**
 * Some simple sanity checking for basic operations of the RollingHash class
 * 
 * @author pcoates
 *
 */
public class TestRollingHash extends TestParent {

	//static Logger log = Logger.getLogger(TestRollingHash.class);
	public static boolean MINIMAL_OUPUT=true;

	/**
	 * Test that the actual compression rate is always within 33% of the nominal size;
	 * This is really just a smell test.
	 * TODO This is not to be relied upon because the actual compression rate is text-dependent.
	 * There's probably an easy way to prove that for any given c, n, and subject file, you could
	 * construct a file that would not compress at all, or one that would compress to nothing.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCompressionSpeedRH() throws Exception {
		System.out.println("testCompressionSpeedRH() starting.");
		int N=8;
		double eps=0.2d;
		int fsize=fileSize(big);
		for(int i=51; i<150; i+=2){
			double rate =testSpeedRH(big, i, 8, 500);
		}
		System.out.println("testCompressionSpeedRH() ending.");
	}
	// 17 second to cat x MB > /dev/null = 7 166 984 763/17 secs = 411764705  412MB/sec
	// time cat * | wc  took 42 seconds = 166,666,666MB/sec
	// XOR Hash is about 18MB/sec
	public int _testCompressionRH(String fname, int c, int n) throws Exception {
		if(!MINIMAL_OUPUT){
			System.out.println("_testCompressionRH()  Testing compression on a large " +
					"string with c:" + c + " n: " + n);
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

	public double testSpeedRH(String fname, int c, int n, int iterations) throws Exception {
		if (!MINIMAL_OUPUT) {
			System.out.println("_testCompressionRH()  Testing compression on a large " +
					"string with c:" + c + " n: " + n);
		}
		comp = new StringCompressorRH(n, c, outputChars, MINBITS, MAXBITS, SEED);
		String longOne = readFile(fname);
		// input file is about 150k
		System.out.println("one file size:" + longOne.length() + " iteratiosn:" + iterations);
		TimeAndRate tAndR = new TimeAndRate();
		tAndR.start();
		for (int i = 0; i < iterations; i++) {
			String compressed = comp.compress(longOne);
		}
		tAndR.compute();
		tAndR.elapsedMS();
		int totalChars = iterations * longOne.length();
		double rate = totalChars / (double) tAndR.elapsedMS() * 1000.0d;

		StringBuffer sb = new StringBuffer();
		sb.append("compression: ");
		sb.append(c);
		sb.append(" neighborhood:");
		sb.append(n);
		sb.append(" total chars:");
		sb.append(totalChars);
		sb.append(" elapse MS:");
		sb.append(tAndR.elapsedMS());
		sb.append(" chars/second:");
		sb.append(rate);
		System.out.println(sb.toString());
		return rate;
	}
}

