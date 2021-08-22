package com.levenshtein;

import com.levenshtein.leven.*;
import utilities.mechanic.TimeAndRate;
import com.levenshtein.parent.TestParent;
import org.junit.Test;
import utilities.exception.QueueEmptyException;
import utilities.exception.QueueFullException;
import utilities.mechanic.CircularQueue;

import java.util.HashSet;
import java.util.Set;


/**
 * Some simple sanity checking for basic operations such as circular-queue
 * hash values are distinct, speed of compressing, etc.
 * 
 * @author pcoates
 *
 */
public class TestBasicOperations extends TestParent {
	//static Logger log = Logger.getLogger(TestBasicOperations.class);

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
		System.out.println("testWhiteSpace() squeezing white space.");
		String one = "this    is     a string     with    \t\t  too\t\tmuch white   space.  ";
		StringCompressorPlain sc = new StringCompressorPlain();
		String squeezed = sc.squeezeWhite(one);
		assertTrue(squeezed.length()<one.length());
		System.out.println("testWhiteSpace()   started with:[" + one + "]");
		System.out.println("testWhiteSpace()   unsqueezed size:" + one.length());
		System.out.println("testWhiteSpace() squeezed it to:[" + squeezed + "]");
		System.out.println("testWhiteSpace() squeezed size:" + squeezed.length());
		assertTrue(squeezed.length()==43);
		System.out.println("testWhiteSpace() completed.");
	}

	/**
	 * Execute LD many times on a pair of small strings.
	 * Beware--this may fail in debug because it tests for reasonable speed.
	 * Runs about 38,000 LD's per second on a laptop.
	 * @throws Exception
	 */
	@Test
	public static void testLDSpeedOnSmallStrings() throws Exception {
		System.out.println("testLDSpeedOnSmallStrings() LD on "+ ITERATIONS+" pairs of small uncompressed strings.");
		StringDistance d = new StringDistance();
		@SuppressWarnings("unused")
		int t = 0;
		TimeAndRate tAndR = new TimeAndRate();
		for (int i = 0; i < ITERATIONS; i++) {
			tAndR.event();
			t = d.LD(t1, t2);
		}
		tAndR.compute();
		System.out.println("\n\tLD applied to " + ITERATIONS
				+ " strings of length: " + t2.length()
				+ " bytes\n\telapsed ms: " + (tAndR.elapsedMS()) + " rate:"
				+ String.format("%.2f",tAndR.rateSecs()) + " pairs/sec");
		assertTrue(tAndR.rateSecs()>10000);
		System.out.println("testLDSpeedOnSmallStrings() completed");
	}		
	
	/**
	 * Sanity check for the generation of random longs used in hashing neighborhoods of N.
	 * Number of set v not-set bits should be close to 50/50;
	 * Should be n distinct values.
	 */
	@Test
	public static void testDistinctHashVals(){
		System.out.println("testDistinctHashVals() starting.");
		longs=RollingHash.createLongs(MIN_BITS,MAX_BITS,12345);
		Set<Long> set = new HashSet<Long>();
		for(int i=0; i<longs.length; i++){
			long v = longs[i];
			set.add(v);
			if(i<=16){
				System.out.println("\tlongs[" + i + "]=" + v + " bits:" + RollingHash.countSetBits(v));
			}
			int bits=RollingHash.countSetBits(v);
			// It's **possible** but unlikely to have a bit cardinality outside of this range. It's
			// bernoulli trials so you can easily compute the probability.
			if (bits>MAX_BITS || bits <MIN_BITS) {
				System.out.println("testDistinctHashVals() bit card outside range:" + bits + " max:"  + MAX_BITS + " min:" + MIN_BITS);
			}
			//assertTrue(bits<=MAX_BITS);
			//assertTrue(bits>=MIN_BITS);
		}
		System.out.println("testDistinctHashVals() set size:" + set.size() + " array:" + longs.length);
		assertTrue(set.size()==longs.length);
		System.out.println("testDistinctHashVals() completed.");
	}
		
	/**
	 * Test the speed of LD on a pair of big strings
	 * <p>
	 * TODO: This needs some kind of assertion of the speedup
	 * @throws Exception
	 */
	@Test
	public static void testSpeedOfLDOnLargeFiles() throws Exception {
		System.out.println("testSpeedOfLDOnLargeFiles() ");
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
		System.out.println("testSpeedOfLDOnLargeFiles() rate per second:" + rate);
		assertTrue(tAndR.rateSecs()>4);
		System.out.println("testSpeedOfLDOnLargeFiles() completed.");
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
	 * Test compressing a lot of data to see how fast it it.
	 * TODO This is only done with the default hash function. I bet the proposed one speeds that up a lot.
	 * @throws Exception
	 */
	@Test
	public void testCompressionOfBigFiles() throws Exception {
		System.out.println("testCompressionOfBigFiles() 10k big files.");
		System.out.println("\tcompression speed on " + COMPRESSIONS + " big files with c:" + c + " n:" + n);
		//String longOne = readFile(big);
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
		System.out.println("testCircularQueue() starting.");
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
			System.out.println("testCircularQueue() overflowed as planned.");
			return;
		}
		fail("testCircularQueueOverflow() should have blown up!");
    }

	/**
	 * Test hat the circular queue underflow is caught.
	 */
	@Test
    public static void testCircularQueueUnderflow() {
		System.out.println("testCircularQueue() starting.");
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
			System.out.println("testCircularQueue() failed to return non-existent value as planned.");
			return;
		}
		fail("testCircularQueueUnderflow() should have blown up!");
    }
	

	/**
	 * Basic test of circular queue to just to see if it fails outright.
	 */
	@Test
    public static void testCircularQueue() {
		System.out.println("testCircularQueue() starting.");
        CircularQueue<Integer> circularQueue = new CircularQueue<Integer>(5);
        circularQueue.enqueue(11);
        circularQueue.enqueue(21);
        circularQueue.enqueue(31);
        circularQueue.enqueue(51);
        circularQueue.enqueue(61);
		assertTrue(circularQueue.isEmpty()==false);
		assertTrue(circularQueue.isFull()==true);
        System.out.println("Elements deQueued from circular Queue: ");
        System.out.print(circularQueue.dequeue()+" ");
        System.out.print(circularQueue.dequeue()+" ");
        System.out.print(circularQueue.dequeue()+" ");
        System.out.print(circularQueue.dequeue()+" ");
        System.out.print(circularQueue.dequeue()+" ");
		System.out.println("testCircularQueue() ended.");
		assertTrue(circularQueue.isEmpty()==true);
    }
	
}
