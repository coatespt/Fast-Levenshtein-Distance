package com.levenshtein.leven;

/**
 * Contract for a compressor, which produces the signatures that LD compares.
 * @author pcoates
 *
 */
public abstract class ICompressor {

	private Integer n;
	private Integer c;
	protected boolean SQUEEZE_WHITE = true;

	/**
	 * Turns all sequences of one or more whitespace chars into a single space.
	 * This can give more realistic results for formatted input such as computer code.
	 * 
	 * @param b
	 */
	public void squeezeWhite(boolean b){
		SQUEEZE_WHITE=b;
	}
	public int getN(){
		return n;
	}
	public void setN(int n){
		 this.n=n;
	}
	public int getC(){
		return c;
	}
	public void setC(int c){
		 this.c=c;
	}	
	
	public abstract String compress(String str);
}
