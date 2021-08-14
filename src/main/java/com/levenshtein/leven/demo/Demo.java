package com.levenshtein.leven.demo;

import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.ScoreDistance;
import com.levenshtein.leven.StringCompressorPlain;
import com.levenshtein.leven.utility.FileAndTimeUtility;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

//import com.a140.util.file.FileAndTimeUtility;

// TODO: Lost track of which hash this uses. Figure it out.
// TODO: Try this same test against original, rolling, and xor hash.
//


/**
 * Demo class does pair-wise LD v estimated LD for all files in
 * the specified directory. * for each pair.
 * <p>
 * Default files are all versions of file xaa and are named xaa-1, xaa-2, etc.
 * The differences of each file from xaa are described in a leading comment line.
 * Comments for each pair of files are given in the output line printed for the pair.
 * e.g. changes: "Deleted every 10th line | Deleted last 20 non blank lines."
 * The base file, xaa, has no comment.
 * <p>
 * Optionally, you can invoke this Demo with full path name of a property file in
 * which you can specify a different file directory, compression rate, n, etc.
 * The default file is demo.properties but I have included demo.properties.101, to 301
 * as well.
 * <p>
 * The default files supplied are clipped out of Gutenberg books and are about 25KB each.
 * <p>
 * Note that almost all of the demo time is spent on computing LD of the raw files for
 * purposes of comparison.
 * takes a few seconds per pair. Compressing them into signatures only takes 
 * a blink,and computing the estimate on the signatures takes well * under a millisecond.
 * <p>
 * All run parameters are set in ./config/demo.properties, but you can clone the config file 
 * for variations with different file sets, n and c values, etc.
 *
 * The point of the heuristic is that you can estimate for files C times
 * bigger than  are practical with the real LD but there's no good way to TEST beyond
 * a certain size.
 *
 * The files in the default set are about 25.5KB each, which is close to the practical
 * size limit for desktop or laptop. It takes about two seconds to compute LD for each pair
 * on a fast Linux laptop. A more powerful machine wouldn't get you much farther as memory
 * and time increase quadratically. 50KB is beyond the pale on my machine.
 *
 * What is scaled error? The error has to be normalized to the file-sizes to make sense.
 * Why? Consider a pair of 25k text files (about 500 lines of text) that differ only a
 * 10 added, deleted, or changed characters. If the estimated LD=100 and the real LD=10,
 * the raw LD would be off by a factor of 10x, which sounds terrible, while fact,
 * an estimate off by 90 chars over a pair of 25k files is very good.
 *
 * <p>
 * Demo creates signatures for every file in the input-file-dir 
 * <p>
 * For each signature, it scans the other signatures for matches.
 * <p>
 * Output something like filenamne: <some-file>.txt est. LD: <some-number> is match?: <true/false>
 * <p>
 * 
 * TODO: Need a way to compute the unlikeliness of a give expected/actual length ratio.
 *  
 * @author pcoates
 *
 */
public class Demo {
	static Logger log = Logger.getLogger(Demo.class);
	private String config = "./config/demo.properties";

	private String flag = null;
	private int c=0;
	private int n=0;
	private String inputDir=null;
	private List<String> inputFileList=null;
	private String sigsDir=null;
	private ScoreDistance sd = null;


	public static void main(String [] args){
		Demo demo = new Demo();
		try{
			demo.parseArgs(args);
			demo.run();
		}
		catch(Exception x){
			log.error(x.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * The optional command line argument.
	 * @param args
	 */
	protected void parseArgs(String [] args){
		if(args.length!=0 && args.length!=1){
			System.err.println("takes either no arguments or one argument, the full path name of a properties file.");
			System.exit(1);
		}
		if(args.length==1){
			System.out.println("Attempting to open props file:" + args[0]);
			config=args[0];
		}
	}
	
	/**
	 * No other constructor
	 */
	public Demo(){ 
		sd=new ScoreDistance();
	}
	
	/**
	 * Execute the demon on the input directory specified in configuration.
	 * @throws Exception
	 */
	public void run() throws Exception{
		getProps();
		System.out.println(descriptiveMsg());
		createSigs(inputDir);
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
	 * Execute an LD on every pair of files * (not including file-x against file-x)
	 * and on the * corresponding signature pairs.
	 *
	 * Print this and the other relevant information.
	 * @throws Exception
	 */
	protected void createSigs(String indir) throws Exception {
		log.info("createSigs() starting");
		String firstLine=null;
		int TEST_ITERATIONS=2000;
		double totalScaledError=0;
		double ct=0;
		for(int i=0; i<inputFileList.size(); i++){
			for(int j=i+1; j<inputFileList.size(); j++){
				String f1 = inputFileList.get(i);
				String f2 = inputFileList.get(j);
				firstLine=FileAndTimeUtility.getFirstLineFlagged(f1,f2, flag);

				String cont1 = FileAndTimeUtility.getFileContents(f1);
				String cont2 = FileAndTimeUtility.getFileContents(f2);

				int longerOriginal = Math.max(cont1.length(), cont2.length());
				int shorterOriginal = Math.min(cont1.length(), cont2.length());

				getCompressor().setC(c);
				getCompressor().setN(n);
				String sig1 = getCompressor().compress(cont1);
				String sig2 = getCompressor().compress(cont2);
				
				int expectedForRandom = sd.expectedDistance(cont1.length(), cont2.length());

				int est = sd.getLDEst(sig1, sig2, longerOriginal, shorterOriginal);
				Date start=new Date();
				int act = sd.getLD(cont1, cont2);
				Date end=new Date();
				double ldRateSec = FileAndTimeUtility.rateSec(1, start, end);
				start=new Date();
				for(int k=0; k<TEST_ITERATIONS; k++){
					sd.getLD(sig1, sig2);
				}
				end=new Date();
				double estRateSec = FileAndTimeUtility.rateSec(TEST_ITERATIONS, start, end);

				double actLdToLen=act/(double) longerOriginal;
				double unscldErrPln=act==0?0:Math.abs(est-act)/(double)act;
				double scaledErrorPlain=(actLdToLen==0||unscldErrPln==0)?0:(unscldErrPln*actLdToLen); 
				totalScaledError+=scaledErrorPlain;
				ct++;
				System.out.println(logLine(f1, f2, longerOriginal, shorterOriginal, expectedForRandom, act, est, scaledErrorPlain, ldRateSec, estRateSec,  firstLine));
			}
		}
		double averageScaledError=totalScaledError/ct;
		System.out.println("\ncreateSigs() completed. Average error:" + averageScaledError +
				" for:" + ct + " file pairs");
	}
	
	int ct=0;
	private String logLine(String f1, String f2, int lgrOrig, int shtrOrig, int expctd, int act, int est, double scdErr, double ldRateSec, double estRateSec, String firstLine){
		StringBuffer sb = new StringBuffer();			
		sb.append(++ct);
		sb.append("    f1: ");
		sb.append(f1);
		sb.append("\t f2: ");
		sb.append(f2);
		sb.append("\tlngr/shrtr: ");
		sb.append(lgrOrig);
		sb.append("/");
		sb.append(shtrOrig);
		sb.append("\tdiff: ");
		sb.append(lgrOrig-shtrOrig);
		sb.append("\texp'd LD: ");
		sb.append(expctd);
		sb.append("\tactual LD: ");
		sb.append(act);
		sb.append("\test: LD ");
		sb.append(est);
		sb.append("\tscld err: ");
		sb.append(String.format("%.4f",scdErr));
		sb.append("\tldRateSec/estRateSec: ");
		sb.append(String.format("%.4f",ldRateSec));
		sb.append("/");
		float speedup=(float) estRateSec/(float)ldRateSec;
		sb.append(String.format("%.4f",estRateSec));
		sb.append(String.format(" speedup %.2f",speedup));
		sb.append("\tchanges: ");
		sb.append(firstLine);
		return sb.toString();
	}

	/**
	 * Read the configuration file for test parameters.
	 * @throws Exception 
	 */
	private void getProps() throws Exception {
		log.info("getProps() starting");
		try{
			InputStream in = new FileInputStream(config);
			Properties props = new Properties();
			props.load(in); 

			n=Integer.parseInt((String)props.get("neighborhood"));
			log.info("getProps() neighborhood size:" + n);

			c=Integer.parseInt((String)props.get("compression"));
			log.info("getProps() compression rate:" + c);

			sigsDir=(String)props.get("sigs-dir");
			log.info("getProps() sigs-dir:" + sigsDir);

			String inputDir=(String)props.get("input-dir");
			log.info("getProps() input-dir:" + inputDir);

			flag=(String)props.get("comment-flag");
			log.info("getProps() comment-flag:" + flag);

			List<String>inputFiles= FileAndTimeUtility.getFilesInDirectory(inputDir, "x");
			inputFileList=new ArrayList<String>();
			for(String f : inputFiles){
				String fullPath = inputDir + File.separator + f;
				inputFileList.add(fullPath);
			}
			log.info("getProps() number of input files:" + inputFileList.size());

		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		log.info("getProps() completed");
	}

	/**
	 * Description of the output.
	 * @return
	 */
	private String descriptiveMsg(){
		StringBuffer sb = new StringBuffer();
		sb.append("App. 99.9 of execution time is spent computed LD of the long strings. " +
				"\nSignature comparisons are take tens of microseconds.\n");
		sb.append("Number of pairs to be computed:" + (inputFileList.size() * ((inputFileList.size()-1)/2)));
		sb.append("\n");
		sb.append("The output consists of:\n");
		sb.append("\tFile name 1\n");
		sb.append("\tFile name 2\n");
		sb.append("\tFile length 1 / file length 1\n");
		sb.append("\tDifference of file lengths\n");
		sb.append("\tExpected LD for random text pairs of that length.\n");
		sb.append("\tActual LD for the file pair\n");
		sb.append("\tEstimate LD for the file pair\n");
		sb.append("\tScaled error, i.e., adjusted for file size\n");
		sb.append("\tLD computations per second / Estimate computations per second \n");
		sb.append("\tSpeedup factor for LD v estimate\n");
		sb.append("\tDescriptive comments, if any, for each file.\n");
		return sb.toString();
	}
	
}
