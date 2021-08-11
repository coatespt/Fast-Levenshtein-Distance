package com.levenshtein;

import com.levenshtein.leven.utility.XORHash;
import org.apache.log4j.Logger;
import org.junit.Test;

import a140.util.TimeAndRate;
import com.levenshtein.leven.StringCompressorPlain;
import com.levenshtein.parent.TestParent;


/**
 * Some simple sanity checking for basic operations
 * TODO: Needs some asserts to make this meaningful
 * @author pcoates
 *
 */
public class TestRegularHash extends TestParent {
	static Logger log = Logger.getLogger(TestRegularHash.class);
	public static boolean MINIMAL_OUPUT=true;

	/**
	 * TODO Should this go in its own class? Or rename this class appropriately.
	 * TODO This and the regular hash function should both have the same stats tests.
	 * TODO: Capture performance output.
	 *
	 * Test compression for regularity of output in terms whether it uses all or most of the output chars,
	 * min and max most frequent chars are of reasonable counts, etc.
	 *
	 * @throws Exception
	 */
	@Test
	public void xorHashRunManyTimes(){
		System.out.format("testXORHashFunctionBasic() starting.\n");

		System.out.format("testXORHashFunctionBasic() ended.\n");
	}
	@Test
	public void testXORHashFunctionBasic() throws Exception {
		System.out.format("testXORHashFunctionBasic() starting.\n");
		XORHash compressor = new XORHash(12,31, outputChars);
		StringBuffer sig = new StringBuffer(300);
		String input = readFile(big);
		int max = input.length()-n;
		for (int i=0; i<max; i++){
			int r = compressor.map(input,i);
			if (r != -1) {
				sig.append((char) r);
			}
		}
		String s = sig.toString();
		int lenS = s.length();
		int lenO = input.length();
		System.out.format("Original len: %d  signature len: %d ", lenS, lenO,"\n");
		System.out.format(" sig: %s", s, "\n");
		System.out.format("\ntestXORHashFunctionBasic() ending.\n");
	}

	// TODO: Repetition for the XOR hash with different compressions and neighborhoods.
	// TODO: Speed tests for the two kinds of hashes. Characters per minute at various compressions.
	// TODO: Occasionally a compression rate seems to result in pathological behavior for the regular hash. Why?

	/**
	 * Test compression for regularity of output in terms whether it uses all or most of the output chars, 
	 * min and max most frequent chars are of reasonable counts, etc.
	 *
	 *  TODO: Need to do some statistics on the results.  Scanning by eye suggests that some signatures
	 *  	are poor and non-random
	 *
	 * @throws Exception
	 */
	@Test
	public static void testCompressionRegular() throws Exception {
		log.info("testCompressionRegular() starting.");
		//String input=readFile(big);
		for(int i=1; i<100; i+=2){
			_testCompression(big,24 + i, 8);
		}
		log.info("testCompressionRegular() ending.");
	}


	public static void _testCompression(String fname, int c, int n) throws Exception {
		if(!MINIMAL_OUPUT){
			log.info("_testCompression()  Testing compression on a large string with c:" + c);
		}
		StringCompressorPlain comp = new StringCompressorPlain();
		String longOne = readFile(fname);
		TimeAndRate tAndR = new TimeAndRate();
		comp.setC(c);
		comp.setN(n);
		String compressed = comp.compress(longOne);
		tAndR.event();
		tAndR.compute();
		int lenComp = compressed.length();
		int expectedLen = longOne.length()/c;
		double ratio = lenComp/(double)expectedLen;
		double error = Math.abs(lenComp-expectedLen)/(double) expectedLen;

		if(!MINIMAL_OUPUT){
		StringBuffer sb = new StringBuffer();
		sb.append("Compressed big string document ");
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
		System.out.println("\tCompressed string:" + compressed);
		}
		// Actual/nominal is actual compression rate divided by C.
		//
		if(MINIMAL_OUPUT){
			System.out.println("_testCompression() "+ minMax(compressed)+"\tc:"+
					c + "\tactual/nominal:" + String.format("%.4f",ratio) +
					"\terror:" + String.format("%.4f",error) + "\tsig:" + compressed);
		}
	}
}