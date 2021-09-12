package com.levenshtein.leven.demo;

import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.ScoreDistance;
import com.levenshtein.leven.StringCompressorRH;
import com.levenshtein.leven.cli.FileSignature;
import org.apache.log4j.Logger;
import utilities.file.FileAndTimeUtility;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

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
 * <p>
 * The point of the heuristic is that you can estimate for files C times
 * bigger than  are practical with the real LD but there's no good way to TEST beyond
 * a certain size.
 * <p>
 * The files in the default set are about 25.5KB each, which is close to the practical
 * size limit for desktop or laptop. It takes about two seconds to compute LD for each pair
 * on a fast Linux laptop. A more powerful machine wouldn't get you much farther as memory
 * and time increase quadratically. 50KB is beyond the pale on my machine.
 * <p>
 * What is scaled error? The error has to be normalized to the file-sizes to make sense.
 * Why? Consider a pair of 25k text files (about 500 lines of text) that differ only a
 * 10 added, deleted, or changed characters. If the estimated LD=100 and the real LD=10,
 * the raw LD would be off by a factor of 10x, which sounds terrible, while fact,
 * an estimate off by 90 chars over a pair of 25k files is very good.
 * <p>
 * Demo creates signatures for every file in the input-file-dir
 * <p>
 * For each signature, it scans the other signatures for matches.
 * <p>
 * Output something like filenamne: <some-file>.txt est. LD: <some-number> is match?: <true/false>
 * <p>
 * <p>
 * TODO: Need a way to compute the unlikeliness of a give expected/actual length ratio.
 *
 * @author pcoates
 */
public class Demo {
    static Logger log = Logger.getLogger(Demo.class);
    static int MinBits = 26;
    static int MaxBits = 39;
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

    public static void main(String[] args) {
        Demo demo = new Demo();
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
    public Demo() {
        sd = new ScoreDistance();
    }

    /**
     * Execute the demon on the input directory specified in configuration.
     *
     * @throws Exception
     */
    public void run() throws Exception {
        getProps();
        System.out.println(descriptiveMsg());
        createSigs(inputDir);
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
        if (compressor == null) {
            StringCompressorRH.chars = "abcdefghijklmnopqrstuvwxsyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()_-+=[]{};:<>.?";
            ICompressor ic = new StringCompressorRH(n, c,
                    StringCompressorRH.StringToCharArray(StringCompressorRH.chars),
                    MinBits, MaxBits, Seed);
            ic.setN(n);
            ic.setC(c);
            compressor = ic;
        }
        return compressor;
    }

    /**
     * Execute an LD on every pair of files * (not including file-x against file-x)
     * and on the * corresponding signature pairs.
     * <p>
     * Print this and the other relevant information.
     *
     * @throws Exception
     */
    protected void createSigs(String indir) throws Exception {
        log.info("createSigs() starting");
        String firstLine = null;
        int TEST_ITERATIONS = 2000;
        double totalScaledError = 0;
        System.out.println(logHeadersAlt());
        for (int i = 0; i < inputFileList.size(); i++) {
            long ct = 0;
            for (int j = i + 1; j < inputFileList.size(); j++) {
                String f1 = inputFileList.get(i);
                String f2 = inputFileList.get(j);
                firstLine = FileAndTimeUtility.getFirstLineFlagged(f1, f2, flag);

                String cont1 = FileAndTimeUtility.getFileContents(f1);
                String cont2 = FileAndTimeUtility.getFileContents(f2);

                int longerOriginal = Math.max(cont1.length(), cont2.length());
                int shorterOriginal = Math.min(cont1.length(), cont2.length());

                getCompressor().setC(c);
                getCompressor().setN(n);


                String sig1 = getCompressor().compress(cont1);
                String sig2 = getCompressor().compress(cont2);

                Date start = new Date();
                // this is slow--we only need to do one to get a rate.
                int act = sd.getLD(cont1, cont2);
                Date end = new Date();
                double fileLDdRateSec = FileAndTimeUtility.rateSec(1, start, end);

                int expectedForRandom = sd.expectedDistanceForSigs(cont1.length(), cont2.length());
                FileSignature fs1 = new FileSignature(f1, cont1.length(), c, n, getCompressor().chars, sig1);
                FileSignature fs2 = new FileSignature(f2, cont2.length(), c, n, getCompressor().chars, sig2);

                int est = sd.getLDEst(fs1, fs2, sd.getLD(sig1, sig2));

                // this is fast--do it many times to get a rate.
                start = new Date();
                for (int k = 0; k < TEST_ITERATIONS; k++) {
                    sd.getLD(sig1, sig2);
                }
                end = new Date();
                long seconds = end.getTime()-start.getTime();
                double sigLDRateSec = FileAndTimeUtility.rateSec(TEST_ITERATIONS, start, end);
                if(seconds == 0){
                   System.err.println("Oh no!");
                }

                //  TODO Verify that the calculation match what is done in the CLI and review the error
                // 		calculations (which have no analog in the CLI, of course.)
                //  TODO: Use the declared value--this is the shrinkage for a signature
                double corrected = est / 1.3d;

                // The error needs to be scaled to the ratio of the LD to the size of the strings.
                // e.g an estimate of 10 when the actual difference is 100 sounds bad, but if the strings
                // are a megabyte it's amazingly perfect.
                // TODO: But is the the right calculation? Seems close...

                // The mean of the two sizes? Does that make sense?
                double ldToSize = act/((longerOriginal+shorterOriginal)/2d);
                double ldAbsError = corrected/act;
                double scaledToSize = ldAbsError * ldToSize;

                scaledToSize = ((int)(scaledToSize*1000))/1000d;
                ct++;
                System.out.println( logLineAlt(f1, f2, longerOriginal, shorterOriginal,
                                expectedForRandom, act, est, (int) corrected, scaledToSize,
                                fileLDdRateSec, sigLDRateSec, firstLine));
            }
        }
    }

    private String logHeaders() {
        StringBuffer sb = new StringBuffer();
        sb.append("FILE 1,\t\t");
        sb.append("FILE 2,\t");
        sb.append("FLEN 1,\t");
        sb.append("FLEN 2,\t");
        sb.append("FLEN DIFF,\t");
        sb.append("EXPECTED LD,\t");
        sb.append("CALC'D LD,\t");
        sb.append("RAW EST,\t");
        sb.append("CORRECTED EST,\t");
        sb.append("RAW ERR,\t");
        sb.append("CORRECTED ERR,\t");
        sb.append("SCALED TO ORIG,\t");
        sb.append("FILE LD/sec,\t");
        sb.append("SIG LD/sec\t");
        sb.append("SPEEDUP,\t");
        sb.append("FILE CHANGES");
        return sb.toString();
    }

    private String logHeadersAlt() {
        StringBuffer sb = new StringBuffer();
        sb.append("CALC'D LD,");
        sb.append("RAW EST,");
        sb.append("CORRECTED EST,");
        sb.append("RAW ERR,");
        sb.append("CORRECTED ERR,");
        sb.append("SCALED TO ORIG,");
        sb.append("SPEEDUP");
        return sb.toString();
    }

    private String logLineAlt(String f1, String f2, int lgrOrig, int shtrOrig,
                           int expctd, int act, int est, int correctedEst, double scaledToSize,
                           double ldRateSec, double estRateSec, String firstLine) {
        StringBuffer sb = new StringBuffer();
        // actual LD`
        sb.append(act);
        sb.append(",");
        // estimated
        sb.append(est);
        sb.append(",");
        // corrected estimate
        sb.append(correctedEst);
        sb.append(",");
        // raw error
        double e = (1 - Math.round(((double) est / act) * 1000)) / 1000.0;
        sb.append(e);
        sb.append(",");
        // corrected estimate
        double v = 1-(double) correctedEst / act;
        v = Math.round(v * 1000) / 1000.0;
        sb.append(v);
        sb.append(",");
        // scaled to size
        sb.append(scaledToSize);
        sb.append(",");
        sb.append(Math.round(estRateSec * 1000.0) / 1000.0);
        sb.append(",");
        // estimate/second
        double spdup=(int)((estRateSec / (double)ldRateSec));
        sb.append(spdup);
        sb.append("");
        return sb.toString();
    }


    int ct = 0;

    private String logLine(String f1, String f2, int lgrOrig, int shtrOrig,
                           int expctd, int act, int est, int correctedEst, double scaledToSize,
                           double ldRateSec, double estRateSec, String firstLine) {
        StringBuffer sb = new StringBuffer();
        sb.append(++ct);
        sb.append(", ");
        // File 1
        sb.append(f1.substring(20));
        sb.append(",\t");
        // file 2
        sb.append(f2.substring(20));
        sb.append(",\t");
        // Longer file len
        sb.append(lgrOrig);
        sb.append(",\t");
        // Shorter file len
        sb.append(shtrOrig);
        sb.append(",\t");
        // Difference between file lengths
        sb.append(lgrOrig - shtrOrig);
        sb.append(",\t\t");
        // Expected LD for random files of these lengths
        sb.append(expctd);
        sb.append(",\t\t\t");
        // Actual calculated LD
        sb.append(act);
        sb.append(",\t\t");
        // RAW LD Estimate uncorrected, not scaled to file sizes
        sb.append(est);
        sb.append(",\t\t");
        // Corrected LD Estimate
        sb.append(correctedEst);
        sb.append(",\t\t\t");
        // Raw error -- uncorrected estimate over actual
        double e = (1 - Math.round(((double) est / act) * 1000)) / 1000.0;
        //sb.append(Math.round(((double) est / act) * 10000) / 10000.0);
        sb.append(e);
        sb.append(",\t\t");
        // corrected error -- corrected estimate/actual
        double v = 1-(double) correctedEst / act;
        v = Math.round(v * 1000) / 1000.0;
        sb.append(v);
        sb.append(",\t\t\t");
        // error scaled to size of originals
        sb.append(scaledToSize);
        sb.append(",\t\t\t");
        // LD Comparisons for rhe real files, per second
        sb.append(Math.round(ldRateSec * 1000.0) / 1000.0);
        sb.append(",\t\t\t");
        // LD Estimates from signatures, per second
        sb.append(Math.round(estRateSec * 1000.0) / 1000.0);
        sb.append(",\t");
        // Speedup
        double spdup=(int)((estRateSec / (double)ldRateSec));
        sb.append(spdup);
        sb.append(",\t");
        // Descriptions of file changes
        sb.append(firstLine.replaceAll(",", " "));
        return sb.toString();
    }

    private String logLineWTags(String f1, String f2, int lgrOrig, int shtrOrig,
                                int expctd, int act, int est, int correctedEst,
                                double unscaledErrPln, double scdErr, double correctedScaled,
                                double ldRateSec, double estRateSec, String firstLine) {
        StringBuffer sb = new StringBuffer();
        sb.append(++ct);
        sb.append("    f1: ");
        sb.append(f1.substring(20));
        sb.append("\t f2: ");
        sb.append(f2.substring(20));
        sb.append("\tf-lens: ");
        sb.append(lgrOrig);
        sb.append(" ");
        sb.append(shtrOrig);
        sb.append("\tdiff: ");
        sb.append(lgrOrig - shtrOrig);
        sb.append("\texp'd LD: ");
        sb.append(expctd);
        sb.append("\ttrue LD: ");
        sb.append(act);
        sb.append("\traw estimate: ");
        sb.append(est);
        sb.append("\tcorrected estimate: ");
        sb.append(correctedEst);
        sb.append("\tuncorrected raw err: ");
        sb.append(Math.round(((double) (est + 1) / act) * 1000) / 1000.0);
        sb.append("\tcorrected raw err: ");
        double v = (double) (correctedEst + 1) / act;
        v = Math.round(v * 1000) / 1000.0;
        sb.append(v);
        sb.append("\tscaled err: ");
        sb.append(Math.round(scdErr * 1000.0) / 1000.0);
        sb.append("\tcorrected scaled: ");
        // TODO ?WTF?
        sb.append(Math.round(correctedScaled * 1000.0) / 1000.0);
        sb.append("\tLD/second: ");
        sb.append(Math.round(ldRateSec * 1000.0) / 1000.0);
        sb.append("\testimate/second: ");
        sb.append(Math.round(estRateSec * 1000.0) / 1000.0);
        sb.append("\tfile diffs: ");
        sb.append(firstLine);
        return sb.toString();
    }

    private String logLine(String f1, String f2, int lgrOrig, int shtrOrig, int expctd, int act, int est, double scdErr, double ldRateSec, double estRateSec, String firstLine) {
        StringBuffer sb = new StringBuffer();
        sb.append(++ct);
        sb.append("    f1: ");
        sb.append(f1.substring(20));
        sb.append("\t f2: ");
        sb.append(f2.substring(20));
        sb.append("\tlengths: ");
        sb.append(lgrOrig);
        sb.append("/");
        sb.append(shtrOrig);
        sb.append("\tdiff: ");
        sb.append(lgrOrig - shtrOrig);
        sb.append("\texp'd LD: ");
        sb.append(expctd);
        sb.append("\tactual LD: ");
        sb.append(act);
        sb.append("\testimated LD: ");
        sb.append(est);
        sb.append("\traw err factor: ");
        sb.append((double) (est + 1) / act);
        sb.append("\tscaled err: ");
        sb.append(String.format("%.4f", scdErr));
        sb.append("\tld rates: ");
        sb.append(String.format("%.4f", ldRateSec));
        sb.append("/");
        float speedup = (float) estRateSec / (float) ldRateSec;
        sb.append(String.format("%.4f", estRateSec));
        sb.append(String.format(" est speedup %.2f", speedup));
        sb.append("\tfile diffs: ");
        sb.append(firstLine);
        return sb.toString();
    }

    private String logLineCSV(String f1, String f2, int lgrOrig, int shtrOrig, int expctd, int act, int est, double scdErr, double ldRateSec, double estRateSec, String firstLine) {
        StringBuffer sb = new StringBuffer();
        sb.append(f1);
        sb.append(", ");
        sb.append(f2);
        sb.append(", ");
        sb.append(lgrOrig);
        sb.append(", ");
        sb.append(shtrOrig);
        sb.append(",");
        sb.append(lgrOrig - shtrOrig);
        sb.append(",");
        sb.append(expctd);
        sb.append(",");
        sb.append(act);
        sb.append(",");
        sb.append(est);
        sb.append(",");
        sb.append(String.format("%.4f", scdErr));
        sb.append(",");
        sb.append(String.format("%.4f", ldRateSec));
        sb.append(",");
        float speedup = (float) estRateSec / (float) ldRateSec;
        sb.append(String.format("%.4f", estRateSec));
        sb.append(",");
        sb.append(String.format("%.2f", speedup));
        sb.append(",");
        sb.append(firstLine);
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

            sigsDir = (String) props.get("sigs-dir");
            log.info("getProps() sigs-dir:" + sigsDir);

            String inputDir = (String) props.get("input-dir");
            log.info("getProps() input-dir:" + inputDir);

            flag = (String) props.get("comment-flag");
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
