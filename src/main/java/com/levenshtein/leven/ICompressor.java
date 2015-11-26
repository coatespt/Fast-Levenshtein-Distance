package com.levenshtein.leven;

/**
 * Contract for a string compressor. This produces the signatures that are compared by
 * LD to derive an estimate. Typical compression rate 25X to 300X depending on application.
 * C=101 and N=8 for string in size range of a few K to a few 100K.
 * 
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
