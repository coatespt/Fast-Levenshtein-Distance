package com.levenshtein.leven;

import org.apache.log4j.Logger;
import com.levenshtein.leven.utility.FileAndTimeUtility;

/**
 * Packages up the computation of LD estimates, plus some other convenience methods.
 * TODO: Strings with smaller LD are estimated more accurately than strings with large LD.  Need an
 * @author peter
 *
 */
public class ScoreDistance {
	private Logger log = Logger.getLogger(ScoreDistance.class);

	public ScoreDistance(){ }

	private IDistance distance = null;
	private IDistance getDistance(){
		if(distance==null){
			distance = new StringDistance();
			log.info("Creating IDistance instance:" + distance.getClass().getName());
		}
		return distance;
	}
	
	/**
	 * Amount by which the LD of a pair of equal length random text strings is smaller than their length. 
	 * This is used to fudge the product of signature LD and compression factor to adjust the expectation.
	 */
	private double wholeFileRatio=0.022;

	/**
	 * Amount by which the LD of a pair of equal size plain-generated signatures is smaller than their length.
	 * This is used to fudge product of signature LD and compression factor to adjust the expectation.
	 */
	private double sigRatio=0.030d;

	/**
	 * Amount by which the LD of a pair of equal size plain-generated signatures is smaller than their length.
	 */
	public double getSigRatio() {
		return sigRatio;
	}

	/**
	 * Amount by which the LD of a pair of equal size plain-generated signatures is smaller than their length.
	 */
	public void setSigRatio(double sigRatio) {
		this.sigRatio = sigRatio;
	}

	/**
	 * Amount by which the LD of a pair of equal sized random text strings is smaller than their length. 
	 */
	public double getWholeFileRatio() {
		return wholeFileRatio;
	}

	/**
	 * Amount by which the LD of a pair of equal sized random text strings is smaller than their length. 
	 */
	public void setWholeFileRatio(double wholeFileRatio) {
		this.wholeFileRatio = wholeFileRatio;
	}


	/**
	 * Adjust an estimate for the difference between the LD of randomly chosen English 
	 * text and the LD of the corresponding signatures differs.
	 * <p> 
	 * Signatures have higher entropy, hence a relatively greater LD. 
	 * <p>
	 * sigRatio is the average ratio of the LD of signatures to the corresponding string lengths
	 * to the ratio of the LD of the original strings to the string length  (for same-length originals). 
	 * @param in double The an estimated distance.
	 * @return
	 */
	public double fudgeFactor(double in){
		double correctionFactor=sigRatio-wholeFileRatio;
		double v = in+(in*(correctionFactor));
		return v;
	}	
	
	/**
	 * The expected distance of two random strings of lengths s1 and s2, give
	 * the expected contraction of LD (fudge factor);
	 * @param s1
	 * @param s2
	 * @return
	 */
	public int expectedDistance(int s1,int s2){
		return (int) fudgeFactor(Math.max(s1, s2))+Math.abs(s1-s2);
	}
	
	/**
	 * The expected distance of two random strings of lengths s1 and s2, give
	 * the expected contraction of LD (fudge factor);
	 * @param s1
	 * @param s2
	 * @return
	 */
	public int expectedDistance(String s1,String s2){
		return (int) fudgeFactor(Math.max(s1.length(), s2.length()))+Math.abs(s1.length()-s2.length());
	}
	
	/**
	 * Given two signatures and the length of the length of the longer original string, compute
	 * the raw estimate as LD(sig1,sig2)/longerSigLen*longerOriginalStringLen.
	 * <p>
	 * Adjust this string by the fudge factor that considers the ratio of LD to string
	 * lengths for originals and signatures (they differ.)
	 * 
	 * Get the estimated LD of two files.
	 * @param f1 String file name.
	 * @param f2 String file name.
	 * @return double Return the estimate.
	 * @throws Exception if anything fails.
	 */
	public int getLDEst(String sig1, String sig2, int longerUnCompressed, int shorterUncompressed) throws Exception {
		int longer=Math.max(sig1.length(), sig2.length());
		int ld = getDistance().LD(sig1,sig2);
		double computedLenRatioPlain = ld/(double)longer;
		double estimatedUnadjusted = (double) (computedLenRatioPlain * longerUnCompressed);
		return (int) fudgeFactor(estimatedUnadjusted); 
	}

	/**
	 * Return the true LD of two files (extract contents)
	 * @param f1 String filename 
	 * @param f2 String filename
	 * @return The LD of the contents of the files
	 * @throws Exception
	 */
	public int getLDForFiles(String f1, String f2) throws Exception{
		String f1Str = FileAndTimeUtility.getFileContents(f1);
		String f2Str = FileAndTimeUtility.getFileContents(f2);
		return getLD(f1Str,f2Str);
	}

	/**
	 * Get the LD of two strings;
	 * @param str1
	 * @param str2
	 * @return
	 * @throws Exception
	 */
	public int getLD(String str1, String str2) throws Exception{
		return getDistance().LD(str1, str2);
	}

}
