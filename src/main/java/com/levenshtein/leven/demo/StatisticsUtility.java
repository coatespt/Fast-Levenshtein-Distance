package com.levenshtein.leven.demo;

import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.ScoreDistance;
import com.levenshtein.leven.StringCompressorPlainJava;
import com.levenshtein.leven.StringCompressorRH;
import com.levenshtein.leven.cli.FileSignature;
import org.apache.log4j.Logger;
import utilities.file.FileAndTimeUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Do a pair-wise LD v estimated LD for all files in the specified directory.
 * <p>
 * This only has one function as of October 7 2021. It takes all the files in a given directory
 * and does a number of operations that yield useful statistics on the behavior of the algorithm.
 * Most notably it computes the true LD of each pair of originals as well as an estimate of
 * the LD based on signatures computed for the two files.
 *
 * Other useful statistics are computed including the significance, the size of the original files
 * the LD, the signature sizes, the LD of the sigatures, and various error computations.
 * This is where the constants for the mean ratios of original files to LD and
 * mean signature size to signature LD are computed.
 *
 * TODO: Need a way to compute the unlikeliness of a give expected/actual length ratio.
 *
 *
 * @author pcoates
 */
public class StatisticsUtility {
    static Logger log = Logger.getLogger(StatisticsUtility.class);
    static int MinBits = 28;
    static int MaxBits = 36;
    static int Seed = 12345;
    static int N = 17;
    static int C = 103;
    static String config = "./config/demo.properties";
    private String flag = null;
    private int c = C;
    private int n = N;
    private final String inputDir = null;
    private List<String> inputFileList = null;
    private String sigsDir = null;
    private ScoreDistance sd = null;
    private String hashFunc = "JAVA";

    /**
     * TODO: This can have multiple functions that can be chosen from CMD line or properties
     * @param args
     */
    public static void main(String[] args) {
        StatisticsUtility demo = new StatisticsUtility();
        try {
            demo.parseArgs(args);
            demo.run();
        } catch (Exception x) {
            log.error(x.getMessage());
            System.exit(1);
        }
    }

    /**
     * The optional command line argument.
     * @param args
     */
    protected void parseArgs(String[] args) {
        System.err.println("Parsing arguments.");
        if (args.length != 0 && args.length != 1) {
            System.err.println("takes either no arguments or one argument, the full path name of a properties file.");
            System.exit(1);
        }
        if (args.length == 1) {
            System.out.println("Attempting to open props file:" + args[0]);
            config = args[0];
        }
    }

    /**
     * No other constructor
     */
    public StatisticsUtility() {
        sd = new ScoreDistance();
    }

    /**
     * Execute the demo on the input directory specified in configuration.
     *
     * @throws Exception
     */
    public void run() throws Exception {
        getProps();
        System.out.println(descriptiveMsg());
        processPairs(inputFileList);
    }

    /**
     *
     */
    protected ICompressor compressor = null;

    /**
     *
     * @return
     * @throws Exception
     */
    protected ICompressor getCompressor() throws Exception {
        // This should be length=83 which is the largest prime that gets most of the printable ASCII characters that
        // don't have a high probablilty of  causing confusion in CSV interpretation.
        String chars = "abcdefghijklnmnoprstuvwxyz!@#$%^&*()_-+=<>;:[]{}ABCDEFGHIJKLMNOPQRSTUVWXYZ.?0123456";

        if (hashFunc.equals("XOR")){
            if (compressor == null) {
                ICompressor ic = new StringCompressorRH(n, c, StringCompressorRH.StringToCharArray(chars), MinBits, MaxBits, Seed);
                ic.setN(n);
                ic.setC(c);
                compressor = ic;
            }
            return compressor;
        }
        else if (hashFunc.trim().equals("JAVA")){
            if (compressor == null) {
                compressor = new StringCompressorPlainJava(n, c, ICompressor.StringToCharArray(chars));
            }
            return compressor;
        }
        else if (hashFunc.equals("SHA-256")){
            if (compressor == null) {
                compressor = new SHA256Compressor(n, c, ICompressor.StringToCharArray(chars));
                compressor.setN(n);
                compressor.setC(c);
            }
            return compressor;
        }

        return compressor;
    }

    private Map<String,String []> sigs = new HashMap<String,String[]>();

    /**
     * Save contents of files and their signatures to save a little time.
     * @param fname
     * @return
     * @throws Exception
     */
    private String [] getASig(String fname) throws Exception{
        if (sigs.containsKey(fname)){
            return sigs.get(fname);
        }
        else {
            String cont = FileAndTimeUtility.getFileContents(fname);
            String sig =  getCompressor().compress(cont);
            String [] ret = new String[2];
            ret[0]=cont;
            ret[1]=sig;
            sigs.put(fname,ret);
            return ret;
        }
    }

    private List<Double> rawErrorsVariantsList = new ArrayList<Double>();
    private List<Double> rawErrorsDifferentList = new ArrayList<Double>();
    private List<Double> rawErrorsList = new ArrayList<Double>();
    private List<Double> correctedErrorsVariantsList = new ArrayList<Double>();
    private List<Double> correctedErrorsDifferentList = new ArrayList<Double>();
    private List<Double> correctedErrorsList = new ArrayList<Double>();
    private List<Double> perSecondList = new ArrayList<Double>();

    /**
     * If the first sameChars characters are the same return true, else false.
     * I'm using a naming convention with 3 character names for he base file and -1 to -n for variants.
     * @param f1
     * @param f2
     * @param sameChars
     * @return
     */
    boolean same(String f1, String f2, int sameChars){
        String s1 = f1.substring(0,sameChars);
        String s2 = f2.substring(0,sameChars);
        return s1.equals(s2);
    }

    /**
     * Process a single pair of files given by path-names.
     * TODO: Should we attempt to save compressions across runs?
     * TODO: See below for ways this is fragile.
     * Note, this is fragile because it would blow up for zero length signatures.
     * Note, this is fragile because it depends on the file naming convention.
     * @param f1
     * @param f2
     * @param iterations
     * @throws Exception
     */
    protected void processPair(String f1, String f2, int iterations) throws  Exception{
        String firstLine = null;
        firstLine = FileAndTimeUtility.getFirstLineFlagged(f1, f2, flag);
        String[] contentsSig1 = getASig(f1);
        String[] contentsSig2 = getASig(f2);
        String cont1 = contentsSig1[0];
        String cont2 = contentsSig2[0];
        String sig1 = contentsSig1[1];
        String sig2 = contentsSig2[1];

        int longerOriginal = Math.max(cont1.length(), cont2.length());
        int shorterOriginal = Math.min(cont1.length(), cont2.length());
        int longerSig = Math.max(sig1.length(), sig2.length());
        int shorterSig = Math.min(sig1.length(), sig2.length());

        // Time getting the LD of the original
        Date start = new Date();
        int act = sd.getLD(cont1, cont2);
        double fileLDdRateSec = FileAndTimeUtility.rateSec(1, start, new Date());

        // Expected LD's for random signatures
        int expectedForRandomSigs = sd.expectedDistanceForSigs(sig1.length(), sig2.length());
        int expectedForRandomFiles = sd.expectedDistanceForOriginals(cont1.length(), cont2.length());

        FileSignature fs1 = new FileSignature(f1, cont1.length(), hashFunc, c, n, getCompressor().getChars(), sig1);
        FileSignature fs2 = new FileSignature(f2, cont2.length(), hashFunc, c, n, getCompressor().getChars(), sig2);

        // LD of signatures  and its ratio to mean signature length signature
        // This is often slightly greater than 1 because the difference in lengths contributes 1:1
        int sigLd = sd.getLD(sig1, sig2);
        double sigLldShrink = sigLd/((double)(sig1.length() + sig2.length())/2);

        // Similar value for the ratio of LD to the shorter signature
        double sigLDShrinkTrunc = 0.0d;
        if (Math.max(sig1.length(), sig2.length()) == sig1.length()){
            // sig1 is longer
            int ld  = sd.getLD(sig1.substring(0,sig2.length()-1), sig2);
            sigLDShrinkTrunc  = ((double)ld)/sig2.length();
        } else {
            // sig1 is not longer
            int ld  = sd.getLD(sig2.substring(0,sig1.length()-1), sig1);
            sigLDShrinkTrunc  = ((double)ld)/sig1.length();
        }

        // Estimated LD
        int est = sd.getLDEst(fs1, fs2, sd.getLD(sig1, sig2));

        // Signature LD is fast--do it many times to get a valid mean rate.
        start = new Date();
        for (int k = 0; k < iterations; k++) {
            sd.getLD(sig1, sig2);
        }
        Date end = new Date();
        double sigLDRateSec = FileAndTimeUtility.rateSec(iterations, start, end);

        // Actual LD to mean file size
        double ldToSize = act/((longerOriginal+shorterOriginal)/2d);

        // Absolute error.
        // TODO This was giving NaN in a couple of cases. That indicates that the input file set must have a
        //  duplicated file
        double ldAbsError = act==0?0d:(double)(est-act)/(double)act;
        if(Double.isNaN(ldAbsError)){
            System.err.println("Not a Number for act: " + act + " est: " + est + " f1: " + f1 + " f2: " + f2);
        } else {
            rawErrorsList.add(ldAbsError);
        }
        // Absolute error scaled to the ratio of the LD and the file size.
        // Note that an estimate that 100K files have an LD of 10 when they actually have an LD of 2 is
        // a 500% absolute error, but relative to the size of the files, it's a tiny error.
        // if the ld of the files was 15k, they would clearly be related, as the mean LD of files that size would
        // be 78k. A 500% absolute error would be the avera
        double scaledToSize = ldAbsError * ldToSize;
        correctedErrorsList.add(scaledToSize);
        if(same(f1,f2,23)){
            rawErrorsVariantsList.add(ldAbsError);
            correctedErrorsVariantsList.add(scaledToSize);
        }
        else {
            rawErrorsDifferentList.add(ldAbsError);
            correctedErrorsDifferentList.add(scaledToSize);
        }
        scaledToSize = ((int)(scaledToSize*1000))/1000d;

        // The amount that LD of the originals is different from the size of the originals.
        // TODO: Is the average of the file lengths legitimate?  I think you can only get this with equal length
        //  files because correcting for the difference in length would require that you know the result already.`
        double origLDShrink =  act/((double)(longerOriginal+shorterOriginal)/2);
        perSecondList.add(sigLDRateSec);
        System.out.println( logLineBasic(
                f1, f2,
                longerOriginal, shorterOriginal,
                longerSig, shorterSig,
                origLDShrink, sigLldShrink, sigLDShrinkTrunc,
                expectedForRandomSigs, expectedForRandomFiles, act, est, scaledToSize,
                fileLDdRateSec, sigLDRateSec, firstLine));
    }

    // Set this true if you want to consider only original files, not variants (that
    // end with hypehen followed by some integer.
    protected static boolean ONLY_FULL_SIZE=false;
    /**
     * Execute LD and other computations on every pair of files (not including file-x against file-x)
     * and on the corresponding signature pairs.
     * <p>
     * Print the relevant information.
     *
     * @throws Exception
     * @param iFileList
     */
    protected void processPairs(List<String> iFileList) throws Exception {
        log.info("createSigs() starting");
        String firstLine = null;
        int TEST_ITERATIONS = 2000;
//        System.out.println(logHeadersAll());
        System.out.println(logHeadersBasic());
        for (int i = 0; i < iFileList.size(); i++) {
            long ct = 0;
            for (int j = i + 1; j < iFileList.size(); j++) {
                String f1 = iFileList.get(i);
                String f2 = iFileList.get(j);
                //same(f1,f2,23);
                if ((ONLY_FULL_SIZE) && ((f1.charAt(f1.length()-2)=='-') || (f2.charAt(f2.length()-2)=='-'))) {
                    continue;
                }
                processPair(f1, f2, TEST_ITERATIONS);
            }
        }
        // TODO: Make multiple error lists--for variant files and for non-variant files.
        printMMMStderr("Raw Error ",rawErrorsList);
        printMMMStderr("Raw Error Variants ",rawErrorsVariantsList);
        printMMMStderr("Raw Error Different ",rawErrorsDifferentList);
        printMMMStderr("Corrected Errror ",correctedErrorsList);
        printMMMStderr("Corrected Errror Variants ",correctedErrorsVariantsList);
        printMMMStderr("Corrected Errror Different ",correctedErrorsDifferentList);
        printMMMStderr("est/sec ",perSecondList);
    }

    protected void printMMMStderr(String label, List<Double> lst){
       double total = 0d;
       double min = Double.MAX_VALUE;
       double max = Double.MIN_VALUE;
       for (int i=0; i<lst.size(); i++){
           double v = lst.get(i);
           min = Math.min(v, min);
           max = Math.max(v, max);
           total += v;
        }
        double mean = total/lst.size();
        double ssd = 0d;
        for (int i=0; i<lst.size(); i++){
            double err = mean - lst.get(i);
            ssd += err * err;
        }
        double var = ssd/lst.size();
        double stdev = Math.sqrt(var);
        StringBuffer sb = new StringBuffer(256);
        sb.append("label,\tmean,\tmin,\tmax,\tvariance,\tstdev\n");
        sb.append(label).append(",\t");
        sb.append(mean).append(",\t");
        sb.append(min).append(",\t");
        sb.append(max).append(",\t");
        sb.append(var).append(",\t");
        sb.append(stdev);
        System.err.println(sb.toString());
    }

    private String logHeadersBasic() {
        StringBuffer sb = new StringBuffer();
        sb.append("FI,                     \t");
        sb.append("F2,                    \t\t");
        sb.append("F1Len,\t");
        sb.append("F2Len,\t");
        //sb.append("Sig1Len, ");
        //sb.append("Sig2Len, ");
        //sb.append("Orig-shrink, ");
        //sb.append("Sig-shrink, ");
        //sb.append("Sig-shrink-trunc, ");
        //sb.append("Expected-sigs, ");
        sb.append("Exptd,\t");
        sb.append("Actul,\t");
        sb.append("Est,  \t");
        sb.append("Raw Error,\t");
        sb.append("Scaled Error,\t");
        sb.append("Est/Sec,\t");
        sb.append("Speedup,");
        return sb.toString();
    }
    private String logHeadersAll() {
        StringBuffer sb = new StringBuffer();
        sb.append("FI, ");
        sb.append("F2, ");
        sb.append("F1Len, ");
        sb.append("F2Len, ");
        sb.append("Sig1Len, ");
        sb.append("Sig2Len, ");
        sb.append("Orig-shrink, ");
        sb.append("Sig-shrink, ");
        sb.append("Sig-shrink-trunc, ");
        sb.append("Expected-sigs, ");
        sb.append("Expected, ");
        sb.append("Actual, ");
        sb.append("Estimate, ");
        sb.append("Raw Error, ");
        sb.append("Corrected Error, ");
        sb.append("Scaled Error, ");
        sb.append("Est/Sec, ");
        sb.append("Speedup, ");
        return sb.toString();
    }
    private String logLineBasic(String f1, String f2,
                              int lgrOrig, int shtrOrig,
                              int s1len, int s2len,
                              double origShrink, double sigShrink, double sigShrinkTrunc,
                              int expctdSig, int expctd, int act, int est,
                              double scaledToSize, double ldRateSec, double estRateSec, String firstLine) {
        StringBuffer sb = new StringBuffer();
        sb.append(f1);
        sb.append(",\t");
        sb.append(f2);
        sb.append(",\t");
        sb.append(lgrOrig);
        sb.append(",\t");
        sb.append(shtrOrig);
        sb.append(",\t");
//        sb.append(s1len);
//        sb.append(", ");
//        sb.append(s2len);
//        sb.append(", ");
//        sb.append(origShrink);
//        sb.append(", ");
//        sb.append(sigShrink);
//        sb.append(", ");
//        sb.append(sigShrinkTrunc);
//        sb.append(",\t\t");
//        sb.append(expctdSig);
//        sb.append(", ");
        sb.append(expctd);
        sb.append(",\t");
        sb.append(act);
        sb.append(",\t");
        sb.append(est);
        sb.append(",\t");
        // raw error
        double e = (double)(est-act)/act;
        //rawErrorsList.add(e);
        e=((int)(e*1000))/1000d;
        sb.append(e);
        sb.append(",\t\t");

        // scaled to size
        sb.append(scaledToSize);
        //correctedErrorsList.add(scaledToSize);
        sb.append(",\t\t");
        sb.append(Math.round(estRateSec * 1000.0) / 1000.0);
        sb.append(",\t");

        // estimate/second
        double spdup=(int)((estRateSec / (double)ldRateSec));
        sb.append(spdup);
        return sb.toString();
    }


    private String logLineAll(String f1, String f2,
                                int lgrOrig, int shtrOrig,
                                int s1len, int s2len,
                                double origShrink, double sigShrink, double sigShrinkTrunc,
                                int expctdSig, int expctd, int act, int est,
                                double scaledToSize, double ldRateSec, double estRateSec, String firstLine) {
        StringBuffer sb = new StringBuffer();
        sb.append(f1);
        sb.append(", ");
        sb.append(f2);
        sb.append(", ");
        sb.append(lgrOrig);
        sb.append(", ");
        sb.append(shtrOrig);
        sb.append(", ");
        sb.append(s1len);
        sb.append(", ");
        sb.append(s2len);
        sb.append(", ");
        sb.append(origShrink);
        sb.append(", ");
        sb.append(sigShrink);
        sb.append(", ");
        sb.append(sigShrinkTrunc);
        sb.append(",\t\t");
        sb.append(expctdSig);
        sb.append(", ");
        sb.append(expctd);
        sb.append(", ");
        sb.append(act);
        sb.append(", ");
        sb.append(est);
        sb.append(", ");
        // raw error
        double e = (1 - Math.round(((double) est / act) * 1000)) / 1000.0;
        sb.append(e);
        sb.append(", ");
        // scaled to size
        sb.append(scaledToSize);
        sb.append(", ");
        sb.append(Math.round(estRateSec * 1000.0) / 1000.0);
        sb.append(", ");
        // estimate/second
        double spdup=(int)((estRateSec / (double)ldRateSec));
        sb.append(spdup);
        return sb.toString();
    }





    /**
     * Read the configuration file for test parameters.
     *
     * @throws Exception
     */
    private void getProps() throws Exception {
        log.info("getProps() starting");
        try {
            InputStream in = new FileInputStream(config);
            Properties props = new Properties();
            props.load(in);

            n = Integer.parseInt((String) props.get("neighborhood"));
            log.info("getProps() neighborhood size:" + n);

            c = Integer.parseInt((String) props.get("compression"));
            log.info("getProps() compression rate:" + c);

            hashFunc = ((String) props.get("hashfn")).trim();
            log.info("getProps() hash function:" + hashFunc);

            sigsDir = ((String) props.get("sigs-dir")).trim();
            log.info("getProps() sigs-dir:" + sigsDir);

            String inputDir = ((String) props.get("input-dir")).trim();
            log.info("getProps() input-dir:" + inputDir);

            flag = ((String) props.get("comment-flag")).trim();
            log.info("getProps() comment-flag:" + flag);

            List<String> inputFiles = FileAndTimeUtility.getFilesInDirectory(inputDir, "x");
            inputFileList = new ArrayList<String>();
            for (String f : inputFiles) {
                String fullPath = inputDir + File.separator + f;
                inputFileList.add(fullPath);
            }
            log.info("getProps() number of input files:" + inputFileList.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("getProps() completed");
    }

    /**
     * Description of the output.
     *
     * @return
     */
    private String descriptiveMsg() {
        StringBuffer sb = new StringBuffer();
        sb.append("Most of the execution time is spent computing LD of the original strings.");
        sb.append("\n");
        sb.append("A large fraction of a second for real LD v a few tens of microseconds for an estimate.");
        sb.append("\n");
        sb.append("Number of pairs to be computed:" + (inputFileList.size() * ((inputFileList.size() - 1) / 2)));
        sb.append("\n");
        sb.append("The output fields.");
        sb.append("\n");
        sb.append("\t\t[FILE 1] First file name 1.");
        sb.append("\n");
        sb.append("\t\t[FILE 2] Second file name 2.");
        sb.append("\n");
        sb.append("\t\t[FLEN 1] File length 1.");
        sb.append("\n");
        sb.append("\t\t[FLEN 2] File length 2.");
        sb.append("\n");
        sb.append("\t\t[FLEN DIFF] Difference of file lengths.");
        sb.append("\n");
        sb.append("\t\t[EXPECTED LD] Expected LD for random text pairs of comparable lengths.");
        sb.append("\n");
        sb.append("\t\t[CALC'D LD]\tComputed LD for the file pair.");
        sb.append("\n");
        sb.append("\t\t[RAW EST]\tRaw estimated LD for the file pair--the LD of the signatures.");
        sb.append("\n");
        sb.append("\t\t[CORRECTED EST]\tCorrected LD estimate for the file pair. There are three scaling factors.");
        sb.append("\n");
        sb.append("\t\t[RAW ERR]\tRaw error--signature LD multiplied by C.");
        sb.append("\n");
        sb.append("\t\t[CORRECTED ERR]\tA constant applied to the results");
        sb.append("\n");
        sb.append("\t\t[SCALED TO ORIG]\tError scaled to file sizes.");
        sb.append("\n");
        sb.append("\t\t[FILE LD/sec]\tNumber of 25K LD computations/second (It's < 1)");
        sb.append("\n");
        sb.append("\t\t[SIG LD/sec]\testimate computations per second.");
        sb.append("\n");
        sb.append("\t\t[SPEEDUP]\tSpeedup factor for estimate v full LD computation.");
        sb.append("\n");
        sb.append("\t\t[FILE CHANGES]\tPlain English description of how the files differ from the original.");
        sb.append("\n");
        return sb.toString();
    }

}
