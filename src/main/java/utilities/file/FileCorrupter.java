package utilities.file;

import org.apache.log4j.Logger;

import java.util.Random;

/**
 * Class to prepare strings corrupted or modified in various ways for testing and calibrating
 * the LD program.
 * 
 * TODO: Put these tests into JUnit
 * 
 * @author petercoates
 *
 */
public class FileCorrupter {
	static Logger log = Logger.getLogger(FileCorrupter.class);
	
	public static char[] chars = {
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
		'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
		'w', 'x', 'y', 'z', 
		'a', 'b', 'c', 'd', 'e', 'e', 'e', 'e', 'e', 'e', 'k',
		'l', 'i', 'i', 'i', 'i', 'o', 'o', 'o', 'o', 'u', 'v',
		'w', 'x', 'y', 'z', 
		'a', 'u', 'u', 'u', 'u', 'a', 'a', 'h', 'i', 'j', 'k',
		'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
		'w', 'x', 'y', 'z', 
		'A', 'B', 'C', 'D', 'E', 'F', 'G',
		'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
		'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 	
		'.', ' ', ' ', ' ', ' ',  	
		'1', '2', '3', '4', '5', '6', '7', '8', '9', '0' 	
		};
	
		static Random rand = new Random();
		
		static String TestString = "Terrence, this is stupid stuff, you eat your victuals " +
				"fast enough. There can't be much amiss tis clear, to see the rate you " +
				"drink your beer. But oh good lord the verse you make, it gives a chap the" +
				" belly ache.";
	
		public static char randomChar(){
			return chars[(rand.nextInt(chars.length-1))];
		}
	public static void main(String[] args){
		System.out.println("main() executing corrupt random chars.");
		String s;
		s = corruptRandomChars(TestString, 0.01d);
		System.out.println(s);
		s = corruptRandomChars(TestString, 0.05d);
		System.out.println(s);
		s = corruptRandomChars(TestString, 0.1d);
		System.out.println(s);
		s = corruptRandomChars(TestString, 0.2d);
		System.out.println(s);
		
		s=corruptWithNBlocksOfCChars(TestString, 0, 10);
		System.out.println(s);
		s=corruptWithNBlocksOfCChars(TestString, 1, 10);
		System.out.println(s);
		s=corruptWithNBlocksOfCChars(TestString, 2, 10);
		System.out.println(s);
		s=corruptWithNBlocksOfCChars(TestString, 3, 10);
		System.out.println(s);
		s=corruptWithNBlocksOfCChars(TestString, 4, 10);
		System.out.println(s);
		System.out.println("main() completed");
	}
		
	/**
	 * Simulate a lot of minor edits to a file. 
	 * Swap each char with something random with a probability of p.
	 * This produces fine grained changes.
	 *  
	 * @param String str A sample string to corrupt
	 * @param Double p the probability that a given char is changed.
	 * @return The new String
	 */
	public static String corruptRandomChars(String str, double p ){
		StringBuffer sb = new StringBuffer(str.length());
		for(int i=0; i<str.length(); i++){
			double d = rand.nextDouble();
			sb.append(d<p?(chars[rand.nextInt(chars.length)]):str.charAt(i));
		}
		return sb.toString();
	}
	
	/**
	 * Insert some blocks of random data into a string. 
	 * This simulates formatting of a basic text.
	 *  
	 * @param str The string to corrupt.
	 * @param n Number of blocks to insert
	 * @param c length of blocks.
	 * @return
	 */
	public static String corruptWithNBlocksOfCChars(String str, int n, int c){
		StringBuffer sb = new StringBuffer(str.length());
		if(n<=0 || c <=0){
			return str;
		}
		if(n==1){
			sb.append(nRandomChars(c));
			sb.append(str);
			return sb.toString();
		}
		if(n==2){
			sb.append(nRandomChars(c));
			sb.append(str);
			sb.append(nRandomChars(c));
			return sb.toString();
		}
		int interval = str.length()/(n-1);
		for(int i=0; i<str.length();i++){
			if(i%interval==0){
				String block = nRandomChars(c);
				sb.append(block);
			}
			sb.append(str.charAt(i));
		}
		return sb.toString();
	}
	
	/**
	 * Generate a string of N random keyboard characters. 
	 * There is no attempt to give the characters a reasonable distribution.
	 * @param n
	 * @return
	 */
	public static String nRandomChars(int n){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<n; i++){
			sb.append(chars[rand.nextInt(chars.length)]);
		}
		return sb.toString();
	}
}
