package com.levenshtein;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

//import com.a140.util.file.FileAndTimeUtility;
import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.IDistance;
import com.levenshtein.leven.StringCompressorPlain;
import com.levenshtein.leven.StringCompressorRH;
import com.levenshtein.leven.StringDistance;
import com.levenshtein.leven.utility.FileAndTimeUtility;
import com.levenshtein.parent.TestParent;

/**
 * Compare the accuracy of the RollingHash to the original hash.
 * So far it looks like the RollingHash is significantly less accurate
 * 
 * TODO: This has never been finished. Need a speed comparison to see if RollingHash is even faster.
 * 
 * @author pcoates
 */
public class TestCompareAccuracy extends TestParent {
	Logger log = Logger.getLogger(TestCompareAccuracy.class);

	static String outputAlphaString = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "1234567890";
	static int MIN_BITS=28;
	static int MAX_BITS=MIN_BITS+8;
	static int SEED=12345;
	static int C = 101;
	static int N = 12;
	static char []outputAlphabet = null;
	private IDistance distance = null;
	public String [] files = {infile1,infile1,infile3,infile4, infile5, infile6, infile7, infile8};

	public IDistance getDistance(){
		if(distance==null){
			distance = new StringDistance();
		}
		return distance;
	}
	private ICompressor plainCompressor;
	public ICompressor getPlainCompressor(){
		if(plainCompressor==null){
			plainCompressor = new StringCompressorPlain();
			plainCompressor.setC(C);
			plainCompressor.setN(N);
		}
		return plainCompressor;
	}
	
	private void clearCompressors(){
		plainCompressor=null;
		rhCompressor=null;
	}
	
	private ICompressor rhCompressor;
	public ICompressor getRHCompressor(){
		if(rhCompressor==null){
			outputAlphabet=new char[outputAlphaString.length()];
			for(int i=0;i<outputAlphaString.length();i++){
				char cc = outputAlphaString.charAt(i);
				outputAlphabet[i]=cc;
			}
			rhCompressor = new StringCompressorRH(N, C, outputAlphabet, MIN_BITS, MAX_BITS, SEED);
		}
		return rhCompressor;
	}
	
	
	public String _compare(String f1, String f2) throws Exception {
		String f1Str = readFile(f1);
		String f2Str = readFile(f2);

		int actual = _actualLD(f1Str, f2Str);
		int [] plainLD =  _plain(f1Str,f2Str,N,C);
		int [] rhLD =  _rh(f1Str,f2Str);
		String longerUnCompressed=(f1Str.length()<=f2Str.length())?f2Str:f1Str;

		double computedLenRatioRH = rhLD[0]/(double) rhLD[1];
		double computedLenRatioPlain = plainLD[0]/(double) plainLD[1];
		
		double actualLenRatio = actual / (double) longerUnCompressed.length();
	
		double estimateRH = (double) (computedLenRatioRH * longerUnCompressed.length());
		double estimatePlain = (double) (computedLenRatioPlain * longerUnCompressed.length());
		
		double unscaledErrorRH = actual==0?0:Math.abs(estimateRH-actual)/actual;
		double scaledErrorRH = (actualLenRatio==0||unscaledErrorRH==0)?0:(unscaledErrorRH*actualLenRatio); 

		double unscaledErrorPlain = actual==0?0:Math.abs(estimatePlain-actual)/actual;
		double scaledErrorPlain = (actualLenRatio==0||unscaledErrorPlain==0)?0:(unscaledErrorPlain*actualLenRatio); 

		if(scaledErrorPlain>scaledErrorRH){
			rhBetter++;
		}
		else if(scaledErrorPlain<scaledErrorRH){
			plainBetter++;
		}
		else{
			neither++;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("f1: ");
		sb.append(f1Str.length());
		sb.append("\tf2: ");
		sb.append(f2Str.length());
		sb.append("\tsigRH: ");
		sb.append(rhLD[1]);
		sb.append("\tsigPlain: ");
		sb.append(plainLD[1]);
		sb.append("\tactual LD:");
		sb.append(actual);
		sb.append("\t\testRH:");
		sb.append(String.format("%.2f",estimateRH));
		sb.append("\testPlain:");
		sb.append(String.format("%.2f",estimatePlain));
		sb.append("\traw errRH:");
		sb.append(String.format("%.2f",unscaledErrorRH));
		sb.append("\traw errPlain:");
		sb.append(String.format("%.2f",unscaledErrorPlain));
		sb.append("\tscaledErrRH:");
		sb.append(String.format("%.2f",scaledErrorRH));
		sb.append("\tscaledErrPln:");
		sb.append(String.format("%.2f",scaledErrorPlain));
		return sb.toString();
	}

	public int _actualLD(String f1Str, String f2Str) throws Exception {
		IDistance d = getDistance();
		return d.LD(f1Str, f2Str);
	}
	
	public int [] _plain(String f1Str, String f2Str, int n, int c) throws Exception {
		IDistance d = getDistance();
		String compressedF1 = getPlainCompressor().compress(f1Str);
		String compressedF2 = getPlainCompressor().compress(f2Str);
		int longerCompressed=((compressedF1.length()<=compressedF2.length())?compressedF2:compressedF1).length();
		int [] ret = new int[3];
		ret[0]=d.LD(compressedF1,compressedF2);
		ret[1]=longerCompressed;
		return ret;
	}

	public int [] _rh(String f1Str, String f2Str) throws Exception {
		IDistance d = getDistance();
		String compressedF1 = getRHCompressor().compress(f1Str);
		String compressedF2 = getRHCompressor().compress(f2Str);
		int longerCompressed=((compressedF1.length()<=compressedF2.length())?compressedF2:compressedF1).length();
		int [] ret = new int[2];
		ret[0]=d.LD(compressedF1,compressedF2);
		ret[1]=longerCompressed;
		return ret;
	}

	int rhBetter=0;
	int plainBetter=0;
	int neither=0;
	
// this one shows the output of running at a single compression rate
//	@Test
//	public void testAllAgainstAll() throws Exception{
//		log.info("testAllAgainstAll101() starting.");
//		for(String f1 : files){
//			for(String f2 : files){
//				String result = _compare(f1, f2);
//				System.out.println(result);
//			}
//		}
//		log.info("testAllAgainstAll101() neigher:" + neither + " plain:" + plainBetter + " rh:" + rhBetter);
//	}

	// 50k files "split" from two completely unrelated 1MB Gutenberg books by different authors. All boilerplate removed.
	private String [] fileSet={
			"xaa","xac","xae","xag","xai","xak","xam","xao","xaq","xas", "xau","xaw",
			"xay","xba","xbc","xbe","xbi","xab","xad","xaf","xah","xaj","xal","xan",
			"xap","xar","xat", "xav","xax","xaz","xbb","xbd","xbf","xbh"
	};
	private String set1path="./data/unrelated/unrelated-1/";
	private String set2path="./data/unrelated/unrelated-2/";

	/**
	 * Statistics on the LD estimate variance on random text.
	 * 
	 * Two unrelated Guterberg books on different subject have all boilerplate removed, and the
	 * remainder of each chopped into 34 equal size pieces using Linux split. 
	 * Each file set has the same file names but unrelated english text content.
	 * 
	 * <p> 
	 * The squeeze white function of the compressor is disabled so files are exactly the same length when compared with LD.
	 * <p> 
	 * For each pair of similarly named files the LD of the raw files and the signatures are computed, as well as string lengths.
	 * <p>
	 * The stdev of the LD of the whole files is about 0.022 of the average length, which is pretty tight. 
	 * <p>
	 * The stdev of the LD of the signatures is about 0.030 of the average length, not quite as tight. 
	 * <p>
	 * The ratio of the LD to the file length is: 0.7909
	 * <p>
	 * The ratio of average standard deviation of LD to length of whole files is about 0.0137 
	 * <p>
	 * The ratio of average standard deviation of LD to signature length is about 0.0493 
	 * <p>
	 * The last of these could be the basis of a fudge factor to tighten the estimate.
	 * @throws Exception
	 */
	@Test
	public void testAverageLD() throws Exception{
		log.info("testAllAgainstAll101() starting.");
		long total = 0;
		long totalSig = 0;
		int minLenFile=Integer.MAX_VALUE;
		int maxLenFile=Integer.MIN_VALUE;
		int minLenSig=Integer.MAX_VALUE;
		int maxLenSig=Integer.MIN_VALUE;
		int minLenLD=Integer.MAX_VALUE;
		int maxLenLD=Integer.MIN_VALUE;
		int minLenLDWhole=Integer.MAX_VALUE;
		int maxLenLDWhole=Integer.MIN_VALUE;
		List<Integer> ldList = new ArrayList<Integer>();
		List<Integer> ldSigList = new ArrayList<Integer>();
		Date start = new Date();
		getPlainCompressor().squeezeWhite(false);
		for(String f : fileSet){
				String pn1 = set1path + f;  
				String pn2 = set2path + f;  
				String str1 = FileAndTimeUtility.getFileContents(pn1);
				minLenFile=Math.min(str1.length(), minLenFile);
				maxLenFile=Math.max(str1.length(), maxLenFile);
				String str2 = FileAndTimeUtility.getFileContents(pn2);
				minLenFile=Math.min(str2.length(), minLenFile);
				maxLenFile=Math.min(str2.length(), maxLenFile);
				String sig1 = getPlainCompressor().compress(str1);
				minLenSig=Math.min(sig1.length(), minLenSig);
				maxLenSig=Math.max(sig1.length(), maxLenSig);
				String sig2 = getPlainCompressor().compress(str2);
				minLenSig=Math.min(sig2.length(), minLenSig);
				maxLenSig=Math.max(sig2.length(), maxLenSig);
				int ld = getDistance().LD(sig1, sig2);
				totalSig+=ld;
				ldSigList.add(ld);
				minLenLD=Math.min(ld, minLenLD);
				maxLenLD=Math.max(ld, minLenLD);
				int ldWhole = getDistance().LD(str1, str2);
				minLenLDWhole=Math.min(ldWhole, minLenLD);
				maxLenLDWhole=Math.max(ldWhole, minLenLD);
				ldList.add(ldWhole);
				total+=ldWhole;
				total+=ld;
				Date end = new Date();
				long elapsedMs=end.getTime()-start.getTime();
				StringBuffer sb = new StringBuffer();
				sb.append("LD sig:");
				sb.append(ld);
				sb.append("\tLD whole:");
				sb.append(ldWhole);
				sb.append("\t msecs:");
				sb.append(elapsedMs);
				sb.append("\t LD less by:");
				sb.append(String.format("%.4f",ldWhole/(double)str1.length()));
				System.out.println(sb.toString());
		}
		Date end = new Date();
		long elapsedMs=end.getTime()-start.getTime();
		int averageLD=(int)(total/fileSet.length);
		List<Double> vList = new ArrayList<Double>();
		for(Integer ld : ldList){
			double diff = averageLD-ld;
			vList.add(diff*diff);
		}
		double sumSqDiff=0;
		for(Double v : vList){
			sumSqDiff+=v;
		}
		double var = sumSqDiff/vList.size();
		double sd = Math.sqrt(var);

		sumSqDiff=0;
		vList=new ArrayList<Double>();
		int averageLDSig=(int)(totalSig/fileSet.length);
		for(Integer ld : ldSigList){
			double diff = averageLDSig-ld;
			vList.add(diff*diff);
		}
		sumSqDiff=0;
		for(Double v : vList){
			sumSqDiff+=v;
		}
		double varSig = sumSqDiff/vList.size();
		double sdSig = Math.sqrt(varSig);

		StringBuffer sb = new StringBuffer();
		sb.append("C:");
		sb.append(C);
		sb.append("\tave:");
		sb.append(averageLD);
		sb.append("\tsd:");
		sb.append(String.format("%.2f", sd));
		sb.append("\t secs:");
		sb.append(elapsedMs/1000/fileSet.length);
		sb.append("\t\tmin file:");
		sb.append(minLenFile);
		sb.append("\tmax file:");
		sb.append(maxLenFile);
		sb.append("\tmin sig:");
		sb.append(minLenSig);
		sb.append("\tave sig LD:");
		sb.append(averageLDSig);
		sb.append("\tsig LD sd:");
		sb.append(sdSig);
		sb.append("\tmax sig:");
		sb.append(maxLenSig);
		sb.append("\tmin LD:");
		sb.append(minLenLD);
		sb.append("\tmax LD:");
		sb.append(maxLenLD);
		sb.append("\t min LD Whole:");
		sb.append(minLenLDWhole);
		sb.append("\t max LD Whole:");
		sb.append(maxLenLDWhole);
		System.out.println(sb.toString());
		System.out.println("ratio stdev/ave-LD-whole-files:" + String.format("%.4f",sd/averageLD) + 
				"\tratio sd-Sig/ave-LD-sigs:" + String.format("%.4f",sdSig/averageLDSig ) +
				"\tLD averages " + String.format("%.4f",(averageLD/(double)maxLenFile)) + " smaller than string length."
				);
	}

	
	
	/**
	 * How does the RollingHash fare against the plain hash a range of C values?
	 * 
	 * Prints how many comparisons gave equal quality estimate, RH estimate better, plain estimate better.
	 * Every file compared to every file except itself.
	 * <p>
	 * Beware this runs for several minutes.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAllAgainstAllRange() throws Exception{
		log.info("testAllAgainstAll101() starting.");
		int c=C;
		for(int i=1; i<10; i++){
		clearCompressors();
		rhBetter=0;
		plainBetter=0;
		neither=0;
		C=c+10*i;
		for(int j=0; j<files.length; j++){
			String f1=files[j];
			for(int k=j+1; k<files.length; k++){
				String f2=files[k];
				String result = _compare(f1, f2);
			//	System.out.println(result);
			}
		}
		System.out.println("\tC:" + C  + "\tneither:" + neither + "\tplain:" + plainBetter + "\trh:" + rhBetter);
		}
		C=c;
	}
	
}