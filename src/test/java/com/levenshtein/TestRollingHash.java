package com.levenshtein;

//import org.apache.log4j.Logger;

import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.StringCompressorRH;
import com.levenshtein.parent.TestParent;
import org.junit.Test;
import utilities.mechanic.TimeAndRate;

import java.util.HashMap;
import java.util.Map;


/**
 * Some simple sanity checking for basic operations of the
 * RollingHash class
 * 
 * @author pcoates
 *
 */
public class TestRollingHash extends TestParent {

	//static Logger log = Logger.getLogger(TestRollingHash.class);
	public static boolean MINIMAL_OUPUT=true;
	static int MIN_BITS=29;
	static int MAX_BITS=35;
	static int SEED=12345;
	static int C = 503;
	static int N = 11;
	static String charString = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()_+=-{}[]:;/.<>?";

	private ICompressor compressor;
	@Override

	/**
	 * We force a new compressor each time because in these tests we're changing the values.
	 */
	protected ICompressor getCompressor(){
		compressor = new StringCompressorRH(getC(), getN(), outputChars , MIN_BITS, MAX_BITS, SEED);
		compressor.setC(C);
		compressor.setN(N);
		return compressor;
	}

	/**
	 * Compress the same big string over a range of compression rates and
	 * compute statistics on the coverage of the output character set.
	 *
	 * The tighter the distribution of the values that appear in the compressed
	 * strings, the better. Smaller stdev is better.
	 *
	 * Observation. The difference between min and max shrinks as does the stdev
	 *   as neighborhood increases. With single-digit neighborhood, you sometimes see
	 *   characters that get no action at all.
	 * 			For c = 51 n = 17 it stdev 253
	 * 			For c = 51 n = 13 it stdev 431
	 * 	 		For c = 51 n = 7 it stdev 2080  which is approximately the mean
	 *
	 * 	Note the difference between n=7 and n=17.
	 *
	 * 	 Increased neighborhood size should therefore increase the entropy of the
	 * 	 compressed string but it will tend to mask fine-grain changes. Choice
	 * 	 is application depenednt
	 *
	 * @throws Exception
	 */
	@Test
	public void testCompressionDistRH() throws Exception {
		String reallybig = "./data/allfiles/jbunc10.txt";
		System.out.println("testCompressionSpeedRH() starting.");
		for(int i=51; i<150; i+=1){
			_rollingHashCharDist(reallybig, i, 11, 500);
		}
		System.out.println("testCompressionSpeedRH() ending.");
	}

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
		for(int i=51; i<150; i+=11){
			_testSpeedRH(big, i, 17, 50);
		}
		System.out.println("testCompressionSpeedRH() ending.");
	}

	public void _rollingHashCharDist(String fname, int c, int n, int iterations) throws Exception {
		setC(c);
		setN(n);
		setOutputChars(charString);
		getCompressor().squeezeWhite(true);

		if (!MINIMAL_OUPUT) {
			System.out.println("_testCompressionRH()  Testing compression on a large " +
					"string with c:" + c + " n: " + n);
		}
		comp = getCompressor();
		String longOne = readFile(fname);
		String compressed=null;
		compressed = comp.compress(longOne);
		_outcharStats(compressed);
	}

	/** Compress a with the given parameters and see how closely the actual compression
	 * and the nominal compression (c) correspond. Asserts that they are within 0.85 which
	 * is an arbitrary choice--just a smoke test to show that it's reasonable.
	 * The file is compressed several times to get a good number for rate.
	 * @param fname
	 * @param c
	 * @param n
	 * @param iterations
	 * @return
	 * @throws Exception
	 */
	public double _testSpeedRH(String fname, int c, int n, int iterations) throws Exception {
		setC(c);
		setN(n);
		setOutputChars(charString);
		getCompressor().squeezeWhite(true);

		if (!MINIMAL_OUPUT) {
			System.out.println("_testCompressionRH()  Testing compression on a large " +
					"string with c:" + c + " n: " + n);
		}
		comp = getCompressor();
		String longOne = readFile(fname);
		System.out.println("file size:" + longOne.length() + " iteratiosn:" + iterations);
		TimeAndRate tAndR = new TimeAndRate();
		String compressed=null;
		tAndR.start();
		for (int i = 0; i < iterations; i++) {
			compressed = comp.compress(longOne);
		}
		tAndR.compute();
		int totalChars = iterations * longOne.length();
		double rate = totalChars / (double) tAndR.elapsedMS() * 1000.0d;

		double expectedComp = longOne.length()/(double)getC();
		double compAccuracy = Math.min(compressed.length(),expectedComp)/Math.max(compressed.length(),expectedComp);

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
		sb.append(" compression accuracy:");
		sb.append(compAccuracy);
		System.out.println(sb.toString());
		assertTrue(compAccuracy>0.85);
		return rate;
	}

	/**
	 * Compute min, max, mean, var, stdev for the number of occurrences of each character
	 * of the output character set in a compressed signature. A more even distribution is
	 * better as an uneven distribution means that the signatures are not as pseudo-random
	 * as they could be.
	 *
	 * This would make the LD estimates less accurate because a difference in the
	 * compression output is less certain to correspond to a difference in the input.
	 *
	 * @param compstr
	 */
	public void _outcharStats(String compstr){
		Map<Character,Integer> cts = new HashMap<Character,Integer>();
		for (int i=0; i<compstr.length(); i++){
			Character c = compstr.charAt(i);
			if (! cts.containsKey(c)){
				cts.put(c,1);
			}
			cts.put(c,cts.get(c)+1);
		}
		int total = 0;
		for (Character entry : cts.keySet()){
			total += cts.get(entry);
		}
		double mean = (double)total/cts.size();
		double ssd = 0d;
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (Character entry : cts.keySet()) {
			int ct = cts.get(entry);
			min=Math.min(min,ct);
			max=Math.max(max,ct);
			double diff = mean-ct;
			ssd += (diff*diff);
		}
		mean=(int)(mean*1000)/1000d;
		double var = ssd/cts.size();
		double stdev = Math.sqrt(var);
		var = (int)(var * 1000)/1000d;
		stdev = (int)(stdev * 1000) / 1000d;
		System.out.println("Comp: " + getC() + "\tN: " + getN() + "\tmin: " + min + "\tmax: " +
				max + "\tmean: " + mean +  "\tstdev: " + stdev + "\t chars in output:" + cts.size() + "\tchars in set:" + outputChars.length);
		if (cts.size()!=outputChars.length) {
			// This will fail sometimes for smaller values of n unless you uses a very large file.
			// E.g n=7 will fail occasionally for 150k files.  This isn't an error per se but it's in
			// here so it doesn't get forgotten.
			assert (cts.size() == outputChars.length);
			System.out.println("\toutput chars used:" + cts.size() + " != output set size: " + outputChars.length);
		}
	}
}



