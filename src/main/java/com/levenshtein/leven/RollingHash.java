package com.levenshtein.leven;

import com.levenshtein.leven.utility.CircularQueue;
import org.apache.log4j.Logger;

import java.util.*;


/**
 * Experimental hashing method intended to be faster and more comprehensible for
 * this specialized application.
 * 
 * Works by creating a static map of each possible 8-bit input
 * onto a pseudo-random long integer (There are only 256.)
 *
 * The hash of the current neighborhood is the bit-wise XOR of the latest N characters
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
 * queue, two table lookups, and two XOR operations.
 *
 * Contrast this with a typical String hash algorithm, e.g., the Java String.hashcode(),
 * which does an one integer multiplications by 31, one addition, plus one array lookup for
 * each character in the string. This is approximately N times as many operations
 * for an N-length string.
 *
 * We are able to do this in this special case because successive neighborhoods differ
 * by only the leading * and trailing characters.
 *
 * A discussion of Java String.hascode() cna be found here:
 *  https://animeshgaitonde.medium.com/the-curious-case-of-java-string-hashcode-6d98c734a313
 *
 * TODO: TestCompareAccuracy says RH signatures are bigger!
 *    Why? Something must be wrong (could be the unit-test.) Use ScoreDistance for uniformity.
 * 
 * @author pcoates
 *
 */
public class RollingHash {
	static Logger log = Logger.getLogger(RollingHash.class);
	private static int NUM_CHARS=65536;
    private CircularQueue<Long> circularQueue = null;
    private long xorProduct=0;
    private static Random rand;
    private int n;
    private int compFactor;
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
	 * TODO: Is the minimum/maximum bits to optimize the entropy of the result? Verify.
	 * TODO: This looks like it is only valid for ASCII data--if so, it sould be
	 * 	generalized for arbitrary input data. At least for all 16bit values.
	 * 	Alternatively values greater than 8bits could be XORed into an 8 bit which
	 * 	gives you the parity of all the Nth bits.
	 *
	 * @param n int Neighborhood size, e.g., 20
	 * @param c int Compression rate, e.g. 150
	 * @param chars Character[] The alphabet of output characters
	 * @param minBits int The minimum number of set bits in one of the randomized longs used to build the XOR string, e.g. 30
	 * @param maxBits int The maximimum number of set bits in one of the randomized longs used to build the XOR string, e.g, 34
	 * @param seed int Random number generator seed.
	 */
	public RollingHash(int n, int c, char [] chars, int minBits, int maxBits, int seed){
		this.n=n;
		this.compFactor=c;
		this.chars=chars;
		rand=new Random(seed);
        circularQueue = new CircularQueue<Long>(n);
		long[] longs=createLongs(minBits,maxBits,seed);
		charsToLongs = new HashMap<Character,Long>();
		for(int j=0;j<NUM_CHARS;j++){
			Character aChar = new Character((char)j);
			charsToLongs.put(aChar, longs[j]);
		}
	}

	/* Function to get number of set bits in binary
	   representation of argument. Algorithm from K&R. */
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
	 * Create the set of pseudo-random longs that input chars will be mapped to.
	 * The only not-pseudo-random part is that they number of set bits is constrained
	 * to a range.
	 *
	 * TODO: This ignores the seed value. Why do you even care about a settable seed as
	 * 	long as the value never changes?
	 * @param minbits
	 * @param maxbits
	 * @param seed
	 * @return
	 */
	public static long [] createLongs(int minbits, int maxbits, int seed){
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
	 * Call with each successive character of input.
	 * For a given character, it XOR's the character that is ageing out
	 * of the neighborhood and XOR's in the pseudo-random long that maps to
	 * the current character, c.
	 *
	 * Returns either null (most of the time) a randomized character based on the last N inputs.
	 * @param c A character of input.
	 * @return Character or null;
	 * @throws Exception 
	 */
	public Character forChar(Character ch) throws Exception {
		long lng = 0;
		charCt++;
		try{
			if(!charsToLongs.containsKey(ch)){
				String error="WTF?! This is supposed to function with all 16 bit values";
				log.error(error);
				throw new Exception(error);
			}
			lng = charsToLongs.get(ch.charValue());
		}
		catch(Exception x){
			// is this error even possible?
			log.error("Threw unknown exception trying to look up value for input character");
			x.printStackTrace();
			throw x;
		}
		// XOR the long for the new character in.
		// Folding a 64 bit long into a 64 bit long accumulator.
		// todo: verify that lng uses the whole long, not just a byte.
		// 	and that it doesn't start 0 each call.
		xorProduct^=lng;
		//xorProduct^=((long)ch<<(charCt%7));
		// XOR the oldest character out
		// TODO Verify that this is the character n behind.
		if(charCt>n){
			Long dec = circularQueue.dequeue();
			xorProduct^=dec;
		}
		// enqueue the newest character
		circularQueue.enqueue(lng);
		if(charCt>n){
			long hval = Math.abs(xorProduct);
			int index = (int) hval%compFactor;
			if(index%compFactor==0){
				return chars[(int) (hval % (long)chars.length)];
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
