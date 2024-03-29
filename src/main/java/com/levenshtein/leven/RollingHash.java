package com.levenshtein.leven;

import utilities.mechanic.CircularQueue;
import org.apache.log4j.Logger;

import java.util.*;


/**
 * Works by creating a static map of each possible 16-bit input
 *
 * The hash of the current neighborhood is the bit-wise XOR of the latest N 16-bit characters
 * that have been read (modulo the cardinality of the output alphabet).
 *
 * It's fast because the hash of neighborhood at position k can be computed by XOR'ing
 * the character at position k into the previous hash value and XOR'ing the character
 * at position k-n out.
 *
 * This works because the XOR operation is commutative and its own inverse.
 * It is the same as putting the binary representation of the n characters in a
 * column and summing the bits in each column. If the sum is odd, the result is a one and
 * if the sum is even, it's a zero. You can XOR an old result out because you're just
 * adjusting the set of sums.
 *
 * Thus it takes a constant number of computationally cheap operations
 * for each hash regardless of the size of the neighborhood: one lookup from a circular
 * queue, two table lookups, and two XOR operations. Once you have read n characters,
 * each additional character incurs a constant number of instructions.
 *
 * Contrast this with a typical String hash algorithm, e.g., the Java String.hashcode(),
 * which does an one integer multiplications by 31, one addition, plus one array lookup for
 * each character in the string.
 *
 * A discussion of Java String.hashcode() cna be found here:
 *  https://animeshgaitonde.medium.com/the-curious-case-of-java-string-hashcode-6d98c734a313
 *
 * TODO: This is oriented towards Java 16 bit characters. Need a general purpose version that works
 * 	with bytes so it can be used with binary data. That would be part of a larger effort as
 * 	more has to change for binary files.
 *
 * @author pcoates
 *
 */
public class RollingHash {
	static Logger log = Logger.getLogger(RollingHash.class);
	// The number of possible 16-bit values 0x to FFFFx.
	private static final int NUM_CHARS=65536;
	// how to store the last n longs corresponding to the input characters
    private CircularQueue<Long> circularQueue = null;
    // accumulator
    private long xorProduct=0;
    private static Random rand;
    // n the neighborhood size
    private int n;
    // c, the compression factor
    private int compFactor;
    // chars read
    private long charCt=0;

    private Map<Character,Long> charsToLongs=null;
    private char [] chars;

	@SuppressWarnings("unused")
	private RollingHash(){
	}


	/**
	 * Create a RollingHash object that can be reused for many signatures. 
	 * If you use the same values, you should get a repeatable result.
	 * <p>
	 * You must initialize this hash the same way to be able to use the signatures
	 * at a later time.
	 * <p>
	 * Initialization creates an array, charsToLongs, of pseudo-random longs with a
	 * distinct value for * each possible character (byte) value. There are only 256 of them.
	 *
	 * It also initializes an initially-empty circular queue the length of the neighborhood.
	 * This will be used to age-out characters in order.
	 * <p>
	 * TODO: The minimum/maximum bits is there so you don't get mostly 0 or mostly 1 to keep things well mixed up.
	 * 	  Is that even necessary?
	 *
	 * @param n int Neighborhood size, e.g., 20
	 * @param c int Compression rate, e.g. 150
	 * @param chars Character[] The alphabet of output characters
	 * @param minBits int The minimum number of set bits in one of the randomized longs used to build the XOR string, e.g. 30
	 * @param maxBits int The maximimum number of set bits in one of the randomized longs used to build the XOR string, e.g, 34
	 */
	public RollingHash(int n, int c, char [] chars, int minBits, int maxBits, int seed){
		this.n=n;
		this.compFactor=c;
		this.chars=chars;
		rand=new Random(seed);
        circularQueue = new CircularQueue<Long>(n);
		long[] longs=createLongs(minBits,maxBits);
		charsToLongs = new HashMap<Character,Long>();
		for(int j=0;j<NUM_CHARS;j++){
			Character aChar = new Character((char)j);
			charsToLongs.put(aChar, longs[j]);
		}
	}



	/** Function to get number of set bits in binary
	   representation of argument. Algorithm from K&R.
		@param n the number we wish to count the bits in.
	*/
	public static int countSetBits(long n)
	{
	    int ct = 0;
	    while (n!=0){
	      n &= (n-1) ;
	      ct++;
	    }
	    return ct;
	}	

	/**
	 * Called once to create the static set of pseudo-random longs that input chars
	 * will be mapped to.
	 * The only not-pseudo-random part is that they number of set bits is constrained
	 * to a range.
	 *
	 * @param minbits
	 * @param maxbits
	 * @param seed
	 * @return
	 */
	public static long [] createLongs(int minbits, int maxbits){
		long [] longs = new long[NUM_CHARS];
		if(rand==null){
			rand=new Random(12345);
		}
		Set<Long> set = new HashSet<Long>();
		for(int i=0; i<NUM_CHARS; i++){
			long x = Math.abs(rand.nextLong());
			int numBits=RollingHash.countSetBits(x);
			while(numBits<minbits || numBits>maxbits){
				x=rand.nextLong();
				numBits=RollingHash.countSetBits(x);
				if(set.contains(x)){
					log.error("createLongs() Collision!? This this would be a one in a gazillion event--is something fishy?");
					numBits=0;
				}
			}
			set.add(x);
			longs[i]=x;
		}
		return longs;
	}

	/**
	 * The most important method. Called for each successive character of input that
	 * you want to compress.
	 *
	 * The accumulator is static, so that it will survive across calls.
	 *
	 * For a given character, it XOR's the character that is ageing out
	 * of the neighborhood and XOR's in the pseudo-random lng that maps to
	 * the current character, c.
	 *
	 * Note that it is a constant number of operations for each
	 *
	 * Returns either null (most of the time) a randomized character based on the last N inputs.
	 * @param c A character of input.
	 * @return Character or null;
	 * @throws Exception 
	 */
	public Character forChar(Character ch) throws Exception {
		long lng = 0;
		charCt++;
		// get a deterministic pseudo-random long the character of input.
		try{
			if(!charsToLongs.containsKey(ch)){
				String error="What?! This is supposed to function with all 16 bit values";
				log.error(error);
				throw new Exception(error);
			}
			char c =  ch.charValue();
			lng = charsToLongs.get(c);
		}
		catch(Exception x){
			// is this error even possible?
			log.error("Threw unknown exception trying to look up value for input character");
			x.printStackTrace();
			throw x;
		}
		xorProduct^=lng;
		if(charCt>n){
			Long dec = circularQueue.dequeue();
			xorProduct^=dec;
		}
		circularQueue.enqueue(lng);
		// We never return a non-null until we're a full neighborhood in.
		if(charCt>n){
			// We interpret the bits as a number between 0 and the compression factor -1.
			// If this number happens to be 0 (any constant would do) we look up the
			// corresponding output character and return it.
			long hval = Math.abs(xorProduct);
			int v = (int) hval % compFactor;
			if(v == 0){
				long rawVal = hval % chars.length;
				int index = Math.abs((int)(rawVal));
				Character ct =  chars[index%chars.length];
				return ct;
			}
		}
		return null;
	}
	
	/**
	 * Wipe out any sums, statistics, etc. but leave the tables so it's like you just
	 * created it.;
	 */
	public void clear(){
		charCt=0;
        circularQueue = new CircularQueue<Long>(n);
        xorProduct=0L;
	}
}