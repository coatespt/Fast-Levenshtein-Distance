package com.levenshtein.leven;

/**
 * Contract for a string compressor. Implementors produce the signatures that are compared by
 * LD to derive an estimate. Typical compression rate 25X to 300X depending on application.
 *
 * TODO: Need a careful test of how performance changes with compression rate and neighborhood size
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

	public static String chars ="qrZ126stucRSTHfgmnoPQJKLIdeM345UVhvwxDEFGWXY7ij890NOakyzABClbp";
	// Convert a string to an array of characters.
	//
	public static char[] StringToCharArray(String str){
		char [] tmp =new char[chars.length()];
		for(int i=0; i<chars.length(); i++){
			tmp[i]=chars.charAt(i);
		}
		return tmp;
	}
}
