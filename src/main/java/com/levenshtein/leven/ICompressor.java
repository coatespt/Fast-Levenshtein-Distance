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
	protected static int prime = 39595541;
	private Integer n;
	private Integer c;
	protected boolean SQUEEZE_WHITE = true;
	public static boolean PRINT_DIAGNOSTICS=false;
	private static String chars = "abcdefghijklmnopqrstuvwxsyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()_-+=[]{};:<>.?";
	public static String getChars(){
		return chars;
	}
	public static void setChars(String ch ){
		chars = ch;
	}


	/**
	 * Turns all sequences of one or more whitespace chars into a single space.
	 * This can give more realistic results for formatted input such as computer code.
	 * 
	 * @param b
	 */
	public void squeezeWhite(boolean b){
		SQUEEZE_WHITE=b;
	}

		/**
		 * Replace all multiple white spaces in the string with a single white space.
		 * @param value
		 * @return
		 */
		public String squeezeWhite(String value){
			String str=value.replaceAll("\\s+", " ");
			return str.trim();
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

	//public abstract String compress(String str);

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

	public String _compress(String str) throws Exception {
		return "";
	};

	// Convert a string to an array of characters.
	//
	public static char[] StringToCharArray(String ch) throws Exception {
		String chars = ch;
		if (chars==null || chars.length()<26){
			// Sanity check--so few chars means you probably passed in the wrong string. This can be changed or removed.
			throw new Exception("output characters set null or very short in StringToCharArray method");
		}
		char [] tmp =new char[chars.length()];
		for(int i=0; i<chars.length(); i++){
			tmp[i]=chars.charAt(i);
		}
		return tmp;
	}
}
