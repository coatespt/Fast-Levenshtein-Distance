package com.levenshtein;

import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.ScoreDistance;
import com.levenshtein.leven.StringCompressorPlain;
import com.levenshtein.parent.TestParent;
import org.junit.Test;
import utilities.file.FileAndTimeUtility;

import java.io.File;
import java.util.*;


//import com.a140.util.file.FileAndTimeUtility;



/**
 * Compare every combination of files in a directory, executing LD on the originals, and on a range
 * of C and N. Print out the results. This program only computes the LD of the originals once, 
 * but it computes the signatures |C|x|N| times. Nevertheless, almost all of the time is 
 * spend computing the raw LD.
 * <p>
 * It can run for many minutes.
 *  
 * @author pcoates
 *
 */
public class TestVaryCandN extends TestParent {
	//static Logger log = Logger.getLogger(TestVaryCandN.class);

	private String inputDir="./data/demo/input-2/";
	private List<String> inputFileList=null;
	private ScoreDistance sd = null;
	int [] cvals = {25,51,101,151,199,251,307};
	int [] nvals = {5,6,7,8,9,10,11,12,13,13,15,17,19,21};
	
	/**
	 * No other constructor
	 * @throws Exception 
	 */
	public TestVaryCandN() throws Exception{ 
		sd=new ScoreDistance();
	}

	private void init() throws Exception{
		System.out.println("init() starting");
		List<String>inputFiles= FileAndTimeUtility.getFilesInDirectory(inputDir, "x");
		System.out.println("init() found num-files: " + inputFiles.size() + ", num-cvals:" + cvals.length + " num-nvals:" + nvals.length + " expected LD ests:" + (inputFiles.size() * (inputFiles.size()-1)));
		inputFileList=new ArrayList<String>();
		for(String f : inputFiles){
			String fullPath = inputDir + File.separator + f;
			inputFileList.add(fullPath);
		}
		System.out.println("init() complete.");
	}
	

	/**
	 * Execute the demon on the input directory specified in configuration.
	 * @throws Exception
	 */
	@Test
	public void testAllCAndN() throws Exception{
		System.out.println("testAllCAndN() starting");
		init();
		executeXbyYbyCbyN(inputDir);
		System.out.println("testAllCAndN() starting");
	}
	
	protected ICompressor compressor = null;

	protected ICompressor getCompressor(){
		if(compressor==null){
			ICompressor ic = new StringCompressorPlain();
			ic.setN(n);
			ic.setC(c);
			compressor=ic;
		}
		return compressor;
	}
	
	
	/**
	 * Execute an LD on every pair of raw files and on the corresponding signature pairs
	 * at a range of values of C and N. 
	 * Print out the run parameters and the average error.
	 * The first set of files takes almost all of the run time, because it only computed the LD of the raw
	 * files once, and they take thousands of times longer than the signatures.
	 * 
	 * @throws Exception
	 */
	protected void executeXbyYbyCbyN(String indir) throws Exception {
		System.out.println("createSigs() running.");
		int SPEED_TEST_ITERATIONS=10000;
		Map<String,Integer> ldMap = new HashMap<String,Integer>();
		List<String> output = new ArrayList<String>();
				long totalBytes=0;
				for(int n : nvals){
					for(int c : cvals){
						System.out.println("createSigs() n=" + n + " c=" + c);
						getCompressor().setC(c);
						getCompressor().setN(n);
						double totalScaledError=0;
						long ct=0;
						long iterationCt=0;
						long iterationTime=0;
						for(int i=0; i<inputFileList.size(); i++){
							for(int j=i+1; j<inputFileList.size(); j++){
								String f1 = inputFileList.get(i);
								String f2 = inputFileList.get(j);
								String cont1 = FileAndTimeUtility.getFileContents(f1);
								String cont2 = FileAndTimeUtility.getFileContents(f2);
								int longerOriginal = Math.max(cont1.length(), cont2.length());
								int shorterOriginal = Math.min(cont1.length(), cont2.length());
								totalBytes+=longerOriginal;
								totalBytes+=shorterOriginal;
								String key = f1 + "---" + f2;
								int act=0;
								if(ldMap.containsKey(key)){
									act=ldMap.get(key);
								}
								else{
									act = sd.getLD(cont1, cont2);
									ldMap.put(key, act);
								}
								String sig1= getCompressor().compress(cont1);
								String sig2= getCompressor().compress(cont2);
								//int expectedForRandom = sd.expectedDistance(cont1.length(), cont2.length());
								int est=0;
								Date start = new Date();
								for(int iter=0; iter<SPEED_TEST_ITERATIONS; iter++){
									est = sd.getLDEst(sig1, sig2, longerOriginal, shorterOriginal);
									iterationCt++;
								}
								Date end = new Date();
								iterationTime+=end.getTime()-start.getTime();
								//sd.getLD(sig1, sig2);
								double actLdToLen=act/(double) longerOriginal;
								double unscldErrPln=act==0?0:Math.abs(est-act)/(double)act;
								double scaledErrorPlain=(actLdToLen==0||unscldErrPln==0)?0:(unscldErrPln*actLdToLen); 
								totalScaledError+=scaledErrorPlain;
								ct++;
							}
						}
						double rate = iterationTime/(double)iterationCt*1000;
						double averageScaledError=totalScaledError/ct;
						long averageSize = totalBytes/(ct*2);
						output.add(logLine(c, n, averageScaledError,ct, averageSize, rate));
				}
			}
			System.out.println(header());
			Collections.sort(output);
			for(String s : output){
				System.out.println(s);
			}
			System.out.println("createSigs() completed.");
		}
	
	private String header(){
		return "C	n	ave-err		num-pairs	ave-size";
	}

	int ct=0;
	private String logLine(int c, int n, double aveErr, long numPairs, long averageSize, double rate){
		StringBuffer sb = new StringBuffer();			
		sb.append(c);
		sb.append("\t");
		sb.append(n);
		sb.append("\t");
		sb.append(String.format("%.4f",aveErr));
		sb.append("\t\t");
		sb.append(numPairs);
		sb.append("\t");
		sb.append(averageSize);
		sb.append("\t");
		sb.append(String.format("%.4f",rate));
		sb.append("/sec");
		return sb.toString();
	}

}
