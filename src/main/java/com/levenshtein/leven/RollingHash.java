package com.levenshtein.leven;

import com.levenshtein.leven.utility.CircularQueue;
import org.apache.log4j.Logger;

import java.util.*;


/**
 * Experimental hashing method intended to be faster and more comprehensible.
 * 
 * Works by mapping each existing character onto a pseudo-random long and taking the XOR 
 * of the latest N characters (modulo the cardinality of the output alphabet) to produce an
 * H(neighborhood) for each position in the sequence.
 * TODO: TestCompareAccuracy says RH signatures are bigger! Why? Something must be wrong. Find out why not the same size, and why not accurate.  Use ScoreDistance for uniformity.
 * 
 * @author pcoates
 *
 */
public class RollingHash {
	static Logger log = Logger.getLogger(RollingHash.class);
	private static int NUM_CHARS=256;
    private CircularQueue<Character> circularQueue = null;
    private long xorProduct=0;
    private static Random rand;
    private int n;
    private int compFactor;
    private long charCt=0;

    /**
     * Each character maps to a random long with approximately equal number of set and un-set bits. 
     */
    private Map<Character,Long> charsToLongs=null;
    private char [] chars;

	@SuppressWarnings("unused")
	private RollingHash(){
	}


	/**
	 * Create a RollingHash object that can be reused for many signatures. 
	 * If you use the same values, you should get a repeatable result.
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
        circularQueue = new CircularQueue<Character>(n);
		long[] longs=createLongs(minBits,maxBits,seed);
		charsToLongs = new HashMap<Character,Long>();
		for(int j=0;j<NUM_CHARS;j++){
			Character aChar = new Character((char)j);
			charsToLongs.put(aChar, longs[j]);
		}
	}

	/* Function to get no of set bits in binary
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
	 * Create the set of pseudo-random longs that will be mapped to by chars.
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
		longs = new long[NUM_CHARS];
		for(int i=0; i<NUM_CHARS; i++){
			long x = rand.nextLong();
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
	 * Returns either null (most of the time) a randomized character based on the last N inputs.
	 * @param char A character of input. 
	 * @return Character or null;
	 * @throws Exception 
	 */
	public Character forChar(Character c) throws Exception {
		long lng = 0;
		charCt++;
		try{
			if(!charsToLongs.containsKey(c)){
				String error="RollingHash only deals with ASCII data--did you use values that are not in range [0:255]?";
				log.error(error);
				throw new Exception(error);
			}
			lng = charsToLongs.get(c.charValue());
		}
		catch(Exception x){
			log.error("Huh?");
			x.printStackTrace();
			throw x;
		}
		xorProduct^=lng;
		Character dec = null;
		if(charCt>n){
			dec = circularQueue.dequeue();
			xorProduct^=dec;
		}
		circularQueue.enqueue(c);
		if(charCt>n){
			long hval = Math.abs(xorProduct);
			if(hval%compFactor==0){
				return chars[(int)(hval%(long)chars.length)];
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
        circularQueue = new CircularQueue<Character>(n);
        xorProduct=0L;
	}
}
