/**
 * 
 */
package com.levenshtein.leven;

import org.apache.log4j.Logger;

/**
 * Signature generator for ASCII data.
 * 
 * A candidate for replacing the original hashing algorithm.
 * It should be the same except that it XOR's the P-Nth value out and
 * XOR's the P value in.
 *
 * It seems not to work as well as the plain! Why? It should.TestCompareAccuracy shows
 * consistently worse accuracy when the run on the same files at varying values of C.
 * 
 * @author peter
 *
 */
public class StringCompressorRH extends ICompressor{
	static Logger log = Logger.getLogger(StringCompressorRH.class);
	
	protected static int MAX_N = 30;
	protected static int RANGE_LOW = 32;
	protected static int RANGE_HIGH = 127;
	protected static int SUM_RANGE = MAX_N * 127;
	protected static int A_PRIME = 17787;
	protected static boolean PRINT_DIAGNOSTICS=false;
	private RollingHash rh = null;
	
	@SuppressWarnings("unused")
	private StringCompressorRH(){}

	public StringCompressorRH(int n, int c, char[] outputChars, int minBits, int maxBits, int seed){
		rh=new RollingHash(n,c,outputChars,minBits,maxBits,seed);
	}


	/**
	 * Compress input string into a hashed signature;
	 */
	public String compress(String str){
		try{
			if(SQUEEZE_WHITE==true){
				str=squeezeWhite(str);
			}
			return _compress(str);
		}
		catch(Exception x){
			return null;
		}
	}
	
	/**
	 * Implementation of compression into hashed signature.
	 * @param str String A plain-text string.
	 * @return String a hashed signature.
	 * @throws Exception
	 */
	public String _compress(String str) throws Exception {
		rh.clear();
		StringBuffer sb = new StringBuffer();
		if(SQUEEZE_WHITE){
			str=squeezeWhite(str);
		}
		int len=str.length();
		if(len==0){
			return "";
		}
		for(int i=0; i<len; i++){
			char ch = str.charAt(i);
			Character chOut = rh.forChar(ch);
			if(chOut!=null){
				sb.append(chOut);
			}
		}
		if(PRINT_DIAGNOSTICS){
			System.out.println(sb.toString());
		}
		return sb.toString();
	}

	/**
	 * Replace all multiple white spaces in the string with a single white space.
	 * @param value
	 * @return
	 */
	public String squeezeWhite(String value){
			return value.replaceAll("\\s+", " ");
	}
}







