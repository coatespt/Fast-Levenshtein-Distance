/**
 * 
 */
package com.levenshtein.leven;

import org.apache.log4j.Logger;

/**
 * Signature generator for ASCII data.
 * 
 * Original algorithm for compression.
 * 
 * TODO: This seems to work well, but how it works is obscure. Cleanup, document, and verify that it actually does a good job.
 * 
 * @author peter
 *
 */
public class StringCompressorPlain extends ICompressor{
	static Logger log = Logger.getLogger(StringCompressorPlain.class);
	//
	// Punctuation starts at 32, numerals at 48, upper case at 65 lower case at 97, 
	// and the last text chars are at 126.
	//
	
	private int MAX_N = 30;
	private boolean PRINT_DIAGNOSTICS=false;

	private static char[] indexSet;
	
	static String chars ="qrZ126stucRSTHfgmnoPQJKLIdeM3"
			+ "45UVhvwxDEFGWXY7ij890NOakyzABClbp";

	static int [] primes = {
			2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,
			59,61,67,71,73,79,83,89,97,101,103,107,109,
			113,127,131,137,139,149,151,157,163,167,173,
			179,181,191,193,197,199,211,223,227,229,233,
			239,241,251,257,263,269,271,277,281,283,293,
			307,311,313,317,331,337,347,349,353,359,367,
			373,379,383,389,397,401,409,419,421,431,433,
			439,443,449,457,461,463,467,479,487,491,499,
			503,509,521,523,541,547,557,563,569,571,577,
			587,593,599,601,607,613,617,619,631,641,643,
			647,653,659,661,673,677,683,691,701,709,719,
			727,733,739,743,751,757,761,769,773,787,797,
			809,811,821,823,827,829,839,853,857,859,863,
			877,881,883,887,907,911,919,929,937,941,947,
			953,967,971,977,983,991,997,1009,1013
		};
	
	static{
		indexSet=new char[chars.length()];
		for(int i=0; i<chars.length(); i++){
			indexSet[i]=chars.charAt(i);
		}
	}
	
	
	/**
	 * Squeeze a string down to about 1/c of its starting size
	 * using neighborhood size n.
	 * 
	 * @param String The input string to compress. 
	 * @param int the neighborhood size for each hash
	 * @param int The comprssion factor
	 * @return String a compressed signature string
	 */
	public String compress(String str){
		return compressAlt(str, getN(), getC());
	}
	
	/**
	 * TODO Revisit this. It looks very inefficient and possibly not even correct. 
	 * 
	 * @param str
	 * @param n
	 * @param c
	 * @return
	 */
	public String compressAlt(String str, int n, int c) {
		StringBuffer sb = new StringBuffer();
		int strPos=0;
		if(SQUEEZE_WHITE){
			str=squeezeWhite(str);
		}
		
		int strLen = str.length();
		if(strLen==0){
			return "";
		}
		while(strPos+n<strLen){
			long curSum = 0;
			for(int i=0; i<n; i++){
				long val = str.charAt(strPos+i);
				if(val<0){
					log.info("wtf?");
				}
				val<<=i*8%64;
				curSum^=val;
			}
			curSum=Math.abs(curSum);
			if(curSum%c==0){
				int indx=(int)(curSum%indexSet.length);
				if(indx<0){
					log.info("wtf?");
				}
				char outChar = indexSet[indx];
				if(PRINT_DIAGNOSTICS){
					System.out.print(outChar);
				}
				sb.append(outChar);
			}
			strPos++;
		}
		if(PRINT_DIAGNOSTICS){
			System.out.println();
		}
		return sb.toString();
	}

	/**
	 * Replace all multiple white spaces in the string with a single white space.
	 * @param value
	 * @return
	 */
	public String squeezeWhite(String value){
			// Replace all whitespace blocks with single spaces.
			String str=value.replaceAll("\\s+", " ");
			return str.trim();
	}
	
	
}







