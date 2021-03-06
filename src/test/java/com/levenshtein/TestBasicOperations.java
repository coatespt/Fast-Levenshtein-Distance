package com.levenshtein;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.a140.util.TimeAndRate;
import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.IDistance;
import com.levenshtein.leven.RollingHash;
import com.levenshtein.leven.StringDistance;
import com.levenshtein.leven.StringCompressorPlain;
import com.levenshtein.leven.utility.CircularQueue;
import com.levenshtein.leven.utility.exception.QueueEmptyException;
import com.levenshtein.leven.utility.exception.QueueFullException;
import com.levenshtein.parent.TestParent;


/**
 * Some simple sanity checking for basic operations
 * 
 * @author pcoates
 *
 */
public class TestBasicOperations extends TestParent {
	static Logger log = Logger.getLogger(TestBasicOperations.class);

	static int ALPHA_SIZE=256;
	static int MIN_BITS=28;
	static int MAX_BITS=36;
	static int SEED=12345;
	static int C=150;
	static int N=20;
	static long [] longs = null;
	static char [] chars = new char[62];

	static{
		String charString = 
			  "abcdefghijklmnopqrstuvwxyz"
			+ "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
			+ "1234567890"; 
		for(int i=0;i<chars.length;i++){
			chars[i]=charString.charAt(i);
		}
	}

	/**
	 * Test squeezing each whitespace sequences down to a single space.
	 * Optional in compression. Squeezing whitespace removes a lot of meaningless
	 * small line changes e.g., MSDOS v Unix.
	 */
	@Test
	public static void testWhitespaceKiller(){
		log.info("testWhiteSpace() squeezing white space.");
		String one = "this    is     a string     with    \t\t  too\t\tmuch white   space.  ";
		StringCompressorPlain sc = new StringCompressorPlain();
		String squeezed = sc.squeezeWhite(one);
		assertTrue(squeezed.length()<one.length());
		log.info("testWhiteSpace()   started with:[" + one + "]");
		log.info("testWhiteSpace()   unsqueezed size:" + one.length());
		log.info("testWhiteSpace() squeezed it to:[" + squeezed + "]");
		log.info("testWhiteSpace() squeezed size:" + squeezed.length());
		assertTrue(squeezed.length()==43);
		log.info("testWhiteSpace() completed.");
	}

	/**
	 * Execute LD many times on a pair of small strings.
	 * Beware--this may fail in debug because it tests for reasonable speed.
	 * Runs about 38,000 LD's per second on a laptop.
	 * @throws Exception
	 */
	@Test
	public static void testLDSpeedOnSmallStrings() throws Exception {
		log.info("testLDSpeedOnSmallStrings() LD on "+ ITERATIONS+" pairs of small uncompressed strings.");
		StringDistance d = new StringDistance();
		@SuppressWarnings("unused")
		int t = 0;
		TimeAndRate tAndR = new TimeAndRate();
		for (int i = 0; i < ITERATIONS; i++) {
			tAndR.event();
			t = d.LD(t1, t2);
		}
		tAndR.compute();
		log.info("\n\tLD applied to " + ITERATIONS
				+ " strings of length: " + t2.length()
				+ " bytes\n\telapsed ms: " + (tAndR.elapsedMS()) + " rate:"
				+ String.format("%.2f",tAndR.rateSecs()) + " pairs/sec");
		assertTrue(tAndR.rateSecs()>10000);
		log.info("testLDSpeedOnSmallStrings() completed");
	}		
	
	/**
	 * Sanity check for the generation of random longs used in hashing neighborhoods of N.
	 * Number of set v not-set bits should be close to 50/50;
	 * Should be n distinct values.
	 */
	@Test
	public static void testDistinctHashVals(){
		log.info("testDistinctHashVals() starting.");
		longs=RollingHash.createLongs(MIN_BITS,MAX_BITS,12345);
		Set<Long> set = new HashSet<Long>();
		for(int i=0; i<longs.length; i++){
			long v = longs[i];
			set.add(v);
			if(i<=16){
				System.out.println("\tlongs[" + i + "]=" + v + " bits:" + RollingHash.countSetBits(v));
			}
			int bits=RollingHash.countSetBits(v);
			assertTrue(bits<=MAX_BITS);
			assertTrue(bits>=MIN_BITS);
		}
		log.info("testDistinctHashVals() set size:" + set.size() + " array:" + longs.length);
		assertTrue(set.size()==longs.length);
		assertTrue(set.size()==ALPHA_SIZE);
		log.info("testDistinctHashVals() completed.");
	}
		
	/**
	 * Test the speed of LD on a pair of big strings
	 * <p>
	 * TODO: This needs some kind of assertion of the speedup
	 * @throws Exception
	 */
	@Test
	public static void testSpeedOfLDOnLargeFiles() throws Exception {
		log.info("testSpeedOfLDOnLargeFiles() ");
		System.out.println("\tSpeed of LD on a pair of 7k uncompressed strings");
		StringDistance d = new StringDistance();
		String longOne = readFile(infile1);
		String longOneWithJunk = readFile(infile2);
		System.out.println("\tString is length:" + longOne.length());
		TimeAndRate tAndR = new TimeAndRate();
		@SuppressWarnings("unused")
		int t = d.LD(longOne, longOneWithJunk);
		tAndR.event();
		tAndR.compute();
		System.out.println("\tLD(S1,S2) in: " + (tAndR.elapsedMS())
				+ " milliseconds, equivalent to  rate: " + tAndR.rateSecs()
				+ " pairs/sec");
		double rate = tAndR.rateSecs();
		log.info("testSpeedOfLDOnLargeFiles() rate per second:" + rate);
		assertTrue(tAndR.rateSecs()>4);
		log.info("testSpeedOfLDOnLargeFiles() completed.");
	}

	private ICompressor compressor=null;
	protected ICompressor getCompressor(){
		if(compressor==null){
			compressor = new StringCompressorPlain();
		}
		compressor.setC(c);
		compressor.setN(n);
		return compressor;
	}
	private IDistance distance = null;
	protected IDistance getDistance(){
		if(distance==null){
			distance = new StringDistance();
		}
		return distance;
	}

	/**
	 * Test that compression of big files is reasonably close to C
	 * @throws Exception
	 */
	@Test
	public void testCompressionOfBigFiles() throws Exception {
		log.info("testCompressionOfBigFiles()");
		System.out.println("\tcompression speed on " + COMPRESSIONS + " big files with c:" + c + " n:" + n);
		String longOne = readFile(infile1);
		int len = longOne.length();
		long total = len*COMPRESSIONS;
		long start = System.currentTimeMillis();
		for (int i = 0; i < COMPRESSIONS; i++) {
			getCompressor().setC(c);
			getCompressor().setN(n);
			String result = getCompressor().compress(longOne);
			// sanity check that the result is reasonably close to that predicted by c.
			assertTrue(result.length()*c*0.8<longOne.length());
		}
		long end = System.currentTimeMillis();
		double rate = COMPRESSIONS / (double) (end - start) * 1000;
		int charsSec = (int) (total/(double)(end-start) * 1000);
		System.out.println("\tcompressing of "+longOne.length()+" string:" + 
				COMPRESSIONS + " times for total "+total+" chars. "
						+ "\n\telapsed ms: " + (end - start) + 
				"\n\tfile-rate:" + rate + "/sec "
						+ "\n\tchar-rate:" + charsSec + "/sec");
	}
	

	/**
	 * Test that circular queue overflow throws exception
	 */
	@Test
    public static void testCircularQueueOverflow() {
		log.info("testCircularQueue() starting.");
		int LEN=5;
        CircularQueue<Integer> circularQueue = new CircularQueue<Integer>(LEN);
        for(int i=0; i<LEN; i++){
        	circularQueue.enqueue(i);
        }
		assertTrue(circularQueue.isFull()==true);
		try{
			circularQueue.enqueue(11);
		}
		catch(QueueFullException qfx){
			log.info("testCircularQueue() overflowed as planned.");
			return;
		}
		fail("testCircularQueueOverflow() should have blown up!");
    }

	/**
	 * Test hat the circular queue underflow is caught.
	 */
	@Test
    public static void testCircularQueueUnderflow() {
		log.info("testCircularQueue() starting.");
		int LEN=5;
        CircularQueue<Integer> circularQueue = new CircularQueue<Integer>(LEN);
        for(int i=0; i<LEN; i++){
        	circularQueue.enqueue(i);
        }
		assertTrue(circularQueue.isFull()==true);
        for(int i=0; i<LEN; i++){
        	int val = circularQueue.dequeue();
        	assertTrue(val==i);
        }
		try{
			int val = circularQueue.dequeue();
		}
		catch(QueueEmptyException qfx){
			log.info("testCircularQueue() failed to return non-existent value as planned.");
			return;
		}
		fail("testCircularQueueUnderflow() should have blown up!");
    }
	

	/**
	 * Basic test of circular queue to just to see if it fails outright.
	 */
	@Test
    public static void testCircularQueue() {
		log.info("testCircularQueue() starting.");
        CircularQueue<Integer> circularQueue = new CircularQueue<Integer>(5);
        circularQueue.enqueue(11);
        circularQueue.enqueue(21);
        circularQueue.enqueue(31);
        circularQueue.enqueue(51);
        circularQueue.enqueue(61);
		assertTrue(circularQueue.isEmpty()==false);
		assertTrue(circularQueue.isFull()==true);
        log.info("Elements deQueued from circular Queue: ");
        System.out.print(circularQueue.dequeue()+" ");
        System.out.print(circularQueue.dequeue()+" ");
        System.out.print(circularQueue.dequeue()+" ");
        System.out.print(circularQueue.dequeue()+" ");
        System.out.print(circularQueue.dequeue()+" ");
		log.info("testCircularQueue() ended.");
		assertTrue(circularQueue.isEmpty()==true);
    }
	
}
