package com.levenshtein.parent;

import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.IDistance;
import com.levenshtein.leven.ScoreDistance;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * Abstract test class that requires concrete classes to implement getCompressor() and getDistance() 
 * methods. This is so they can share the driver method onFiles(String comment, String f1, String f2).
 *
 * The onFiles() method is called with the path/names of a pair of files and computes a number of
 * statistics: Exp'd speedup, LD computation rate for the raw files of that size,
 * rate to compute the signatures, LD for the signatures (pairs/sec), actual speed increase,
 *  file-lengths, signature-lengths, Actual LD, LD of the signatures, Estimated LD
 * 	LD(compressed1,compressed2)/length(longer(compressed1,compressed2),
 * 	LD(uncompressed1,uncompressed2)/length(longer(uncompressesd1, uncompressed2),
 * 	Unscaled error, Scaled error, unscaled fudged error, Scaled fudged error:0.1545,
 * 	ScoreDistance error:0.1490
 *
 * 	Most of the time in this routine is spent computing the true LD of the files.
 * 
 * @author pcoates
 *
 */
public abstract class TestAccuracyDriverParent extends TestParent{
	abstract protected ICompressor getCompressor();
	abstract protected IDistance getDistance();

	private Logger log = Logger.getLogger(TestAccuracyDriverParent.class);
	
	// Measured empirically for book text
	// May vary for other types of data.
	static double wholeFileRatio=0.022; 
	static double sigRatio=0.030d; 

	/**
	 * Compute LD for original stings, signatures, plus error rates based on the known LD of the strings.
	 * 
	 * @param str String Descriptive string for what is being tested.
	 * @param f1  String file path one
	 * @param f2  String file path two
	 * @throws Exception Thrown for file not found, etc.
	 */
	public void onFiles(String str, String f1, String f2) throws Exception {
				ScoreDistance scoreD = new ScoreDistance();
				scoreD.setSigRatio(sigRatio);
				scoreD.setWholeFileRatio(wholeFileRatio);
				log.info("onFiles() executing.");
				StringBuffer sb = new StringBuffer();
				sb.append("\t");
				sb.append(str);
				sb.append(" \n\tExp'd speedup: ");
				sb.append(Math.pow(getC(), 2));
				sb.append("\n");

				IDistance d = getDistance();

				String f1Str = readFile(f1);
				String f2Str = readFile(f2);
			
				String compressedF1 = getCompressor().compress(f1Str);
				String compressedF2 = getCompressor().compress(f2Str);
			
				String longerCompressed=(compressedF1.length()<=compressedF2.length())?compressedF2:compressedF1;
				String longerUnCompressed=(f1Str.length()<=f2Str.length())?f2Str:f1Str;
				String shorterUnCompressed=(f1Str.length()<=f2Str.length())?f2Str:f1Str;
			
				// you have to do LD many times because it's too fast to measure with ms clock.
				int distCompressed = 0;
				int COMP_CT=1000;
				Date start = new Date();
				for(int k=0; k<COMP_CT; k++){
					distCompressed = d.LD(compressedF2, compressedF1);
				}
				double rateCompressed = COMP_CT / (double) ((new Date()).getTime() - start.getTime()) * 1000;
			
				int scoreDEst = scoreD.getLDEst(compressedF1, compressedF2, longerUnCompressed.length(),shorterUnCompressed.length());
			
				start = new Date();
				int distUnCompressed=d.LD(f1Str, f2Str);
				double rateUncompressed = 1.0d / (double) ((new Date()).getTime()-start.getTime()) * 1000;
				sb.append("\tLD rate raw files: "); 
				sb.append(String.format("%.4f",rateUncompressed)); 
				sb.append(" files/sec"); 
				sb.append(" LD rate sigs:"); 
				sb.append(String.format("%.4f",rateCompressed));
				sb.append(" pairs/sec, speed increase: "); 
				sb.append(String.format("%.4f",rateCompressed/rateUncompressed));
				sb.append("X\n"); 
			
				// does not account for difference in length. 
				double computedLenRatio = distCompressed/(double) longerCompressed.length();
			
				double actualLenRatio = distUnCompressed / (double) longerUnCompressed.length();
			
				double estimate = (double) (computedLenRatio * longerUnCompressed.length());
				
				double unscaledError = distUnCompressed==0?0:Math.abs(estimate-distUnCompressed)/distUnCompressed;
				double scaledError = (actualLenRatio==0||unscaledError==0)?0:(unscaledError*actualLenRatio); 
				
				sb.append("\tRaw len: "); sb.append(f1Str.length()); sb.append(", "); sb.append(f2Str.length()); sb.append("\n"); 
				sb.append("\tSig len: "); sb.append(compressedF2.length()); sb.append(", "); sb.append(compressedF1.length()); sb.append("\n"); 
				sb.append("\tLD raw:"); sb.append(distUnCompressed); sb.append("\n");
				sb.append("\tLD sigs:"); sb.append(distCompressed); sb.append("\n"); 
				sb.append("\tEstimated LD:\t"); sb.append((int) estimate); sb.append("\n");
			
				sb.append("\tLD(compressed1,compressed2)/length(longer(compressed1,compressed2):"); sb.append(String.format("%.4f",computedLenRatio));
				sb.append("\n"); 
				sb.append("\tScoreDEstimate: "); sb.append(String.format("%.4f",computedLenRatio));
				sb.append("\n"); 
				sb.append("\tLD(uncompressed1,uncompressed2)/length(longer(uncompressesd1, uncompressed2):"); sb.append(String.format("%.4f",(actualLenRatio))); sb.append("\n");
			
				sb.append("\tUnscaled error:"); sb.append(String.format("%.4f",Math.abs(unscaledError)));
				sb.append("\tScaled error:"); sb.append(String.format("%.4f",Math.abs(scaledError)));
				double fudged = fudgeFactor(estimate);
				double unscaledFudgedError = distUnCompressed==0?0:Math.abs(fudged-distUnCompressed)/distUnCompressed;
				double scaledFudgedError = (actualLenRatio==0||unscaledFudgedError==0)?0:(unscaledFudgedError*actualLenRatio); 
				sb.append("\tUnscaled fudged error:"); sb.append(String.format("%.4f",Math.abs(unscaledFudgedError)));
				sb.append("\tScaled fudged error:"); sb.append(String.format("%.4f",Math.abs(scaledFudgedError)));

				double unscaledScoreDError = distUnCompressed==0?0:Math.abs(scoreDEst-distUnCompressed)/(double)distUnCompressed;
				double scaledScoreDError = (actualLenRatio==0||unscaledScoreDError==0)?0:(unscaledScoreDError*actualLenRatio); 
				sb.append("\tScoreDistance error:"); sb.append(String.format("%.4f",Math.abs(scaledScoreDError)));
				System.out.println(sb.toString());
			}

	/**
	 * The result is not simply scaled by the inverse of the compression. The following scales it more
	 * accurately.
	 *
	 * Ratio of signature-LD/signature-length is greater than the ratio of whole-file-LD/whole-file-length. 
	 * This method corrects for that. 
	 * 
	 * @param in
	 * @return
	 */
	public double fudgeFactor(double in){
		double wholeFileRatio=0.022;
		double sigRatio=0.030d;
		double correctionFactor=sigRatio-wholeFileRatio;
		return in-(in*correctionFactor);
	}

}