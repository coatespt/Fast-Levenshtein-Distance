package com.levenshtein.leven.cli;

import com.levenshtein.leven.*;
import com.levenshtein.leven.demo.SHA256Compressor;
import utilities.file.FileAndTimeUtility;

import java.io.Console;
import java.io.FileInputStream;
import java.util.*;

import static java.lang.Integer.valueOf;

/**
 * Command Line Interface
 * This program runs in standard Unix pipeline style, accepting CSV input from either a
 * file or from standard-in and writing output to standard-in allowing the input to be
 * directed to a file or piped into another program as input.
 * <p>
 * Compression mode: takes list of files from filenames given on command line
 * or one-by-one from stdin. The output is csv lines on stout. This output can be used
 * as-written as the LD target file.
 * <p>
 * LD matching: Find matches to set of input files among a set of target signatures.
 * The files are given as file names similarly to compression mode.  The targets are read
 * from precomputed CSV signature lines in a file. The CSV Signature lines are exactly
 * the format of the compression-mode output.

 Important errors or weaknesses

 TODO: ScoreDistance has two versions of the getLDEst one of which isn't used in the real code. Get rid of it
    and fix the test.

 TODO: Factor the properties file and command line arguments code out to their own class that can
    be shared with the StatisticsUtility class.

 TODO: Estimates seem to be more accurate when the files are in fact related. They  are exact when
    the files are identical, only slightly off when the files are almost the same, but deteriorate
    to about 0.7 of the true LD for files that are totally unrelated.
    Verify that this observation is true and if so, quantify. Possibly it is simply a consequence of the
    fact that the LD varies from 0 to the mean for randomly selected files and it CAN'T be off when the
    distance is zero so the mean error can only go up from there.

 TODO: Check out the significance computation (A) is it correct?

 TODO significance is a scalar between [0,1] but it might make more sense to somehow separate the cases where the
    differences seem to be concentrated. For instance, if a result is significant, do a second level that
    does the computation on K-length blocks of the files, e.g. 1/4 or 1/10 the length of the larger, with the
    smaller chopped into blocks of the same size (not the same number of blacks.) Return the global significance
     as computed now, and also compute the block-ordering that gives the highest score e.g. total-sig, b1 c3, b4
    c1, b3 c2, b2   Note that the final b had no matching c block.

 TODO: Need to work out how to put a true confidence interval around an estimate.
    Part of above, what are the mean and stdev for LD of unrelated text?
    Use the tool to find a large number of pairs that are not related---score at the bottom of significance.
    YOu probably need write a program to blindly chop off the first and last few hundred lines so  there is no
    boiler plate and leave a residuum of one fixed size.

 TODO: Deal with zero-length signatures that may result from tiny files and big C values.
    What it does now is is abort if the hash is empty.

 Possible Enhancements

 TODO: Compute a list of good compression and N values.

 TODO: Make it work for binary data. Need a hash that works for arbitrary characters, not just strings.
    perhaps the Java string hash could be modified?

 TODO: Should have option for input from pre-computed signatures similar to targets from file?

 TODO: Implement multi-threading for the matching. Would probably increase throughput by several x.
    Frank says this is low priority.

 TODO: Implement multi-threading for compression. Considered strategy of breaking an input into K pieces
    then computing hashes for n characters on either side and stitching them together at the point where
    the output for the Nth position. This would not be too hard but why not simply hand an entire file
    to each thread? Simpler and probably more efficient as it's easier to keep the input streaming for longer.
    Note, the problem will almost never be how long it takes to process a single file because
    a file so long that this was an issue would result in an unusable signature too long to run LD on.
    So just multi-thread at an input file level.

 TODO: Effective use requires that files be in bucket of similar size. Work out a command-line
    pipeline to bucket files by size into overlapping buckets.

 TODO: Cross matching. Break files into equal size pieces and do the LD estimates for the
    components. This would allow files with areas with a high degree of similarity in a limited
    region to be more readily recognized.

 TODO: The significance is quite simple. Could there be a version of it that considers the
    signatures piecemeal? Sort of like the scheme to do the signatures in sections?

 TODO:  Input from stdin has not been tested--only input from files of filenames.

 TODO: Test with some other document types such as MS Word .doc files, PDF's, etc.

 TODO: Develop a data set for matching accuracy as a function of N which has two big effects on accuracy:
    (1) Small N is less sensitive to minor differences as each can bleed out to at most
    N-1 characters in either direction.
    (2) On the other hand small N results in a low cardinality of neighborhood hash values and thus
    less pseudo-randomness in the signatures.
    (3) Number (2) is probably true because if you have N=1, you definitely get a very lumpy distribution.
        for N=2 there are 9000+ values, but many never occur. Is the number so large for reasonable size
        N that it never really matters?

 TODO: Index of Coincidence might be a good measure of signatures quality. See below this item.

 TODO: A script to go through a set of input files or find all files below some point in the fs, and assign them to
    buckets by size. This would go with an enhancement to scale the compression to the signature size.
    You would probably need to generate multiple signatures to bracket the ideal size of the signature.
    Each search sig would have to be generated in three sizes too.
    So that's nine computations for each match.
    The same signature sets could be used with multiple approaches. E.g. you could scan at highest compression,
    find a match, then estimate at lowest compression IIF it's a good match.


 *  https://www.johndcook.com/blog/2021/08/14/index-of-coincidence/
 *  John Cook blog post on "index of coincidence" which is similar to Renyi entropy
 *  (not quite the same as Shannon entropy.)
 *  i.e. it is the negative log. This is of interest here because the index of coincidence of
 *  of a body of text is characteristic of a language. (Or so I understand.) It should be different
 *  for different human languages and different types of binary data. Investigate this.
 *
 https://towardsdatascience.com/non-negative-matrix-factorization-for-image-compression-and-clustering-89bb0f9fa8ee
 */

public class Cli {
    static String ARG_DASHES = "-";
    static int DEF_SB_SIZE = 1024;
    static int MinBits=28;
    static int MaxBits=36;
    static int Seed=12345;
    protected String[] argv = null;
    protected String propsfile = null;
    protected String infile = null;
    protected int c = 0;
    protected int n = 0;
    protected String outChars;
    protected boolean ld = false;
    protected String targetFile = null;
    protected double t = 0.0;
    private ScoreDistance sd = null;
    protected boolean printHeader=true;
    protected boolean verbose = false;
    protected int outputLevel = 0;
    protected String hashType = "JAVA";
    private boolean squeeze=true;

    // ld smaller than equal-len file by this much
    // TODO: Set this default to the best value we can compute
    protected double fr=0.22d;
    // ld smaller than equal-len signature by this much
    // TODO: Set this default to a to the best value we can compute
    protected double sr=0.30d;


    /**
     * You want C and the cardinality of the output set to be co-prime.
     * @param a
     * @param b
     * @return
     */
    private boolean isCoprime(int a, int b){
           if (gcd(a,b)==1){
              return true;
           }
           return false;
    }

    /**
     * Recursively determine if C and the size of the output set are co-prime.
     * @param a
     * @param b
     * @return
     */
    public int gcd(int a, int b) {
        if (b==0) {
            return a;
        }
        return gcd(b,a%b);
    }


    /**
     * A CLI gets all it's setup via command-line arguments but you can
     * set up automated tests by constructing argv list explicitly.
     * @param argv An array of strings in the usual command-line form.
     */
    public Cli(String[] argv) {
        // If config or the cmd line does not set it, everything uses the default set
        outChars=ICompressor.getChars();
        this.argv = argv;
    }

    public static void main(String[] argv) {
        Cli cli = new Cli(argv);
        cli.go();
    }

    /**
     * Main driver of CLI.
     * <p>Get the arguments</p>
     * <p>If a properties file is specified, read them
     * properties setting instance values</p>
     * <p>Read the rest of the command line arguments. CL arguments
     * that set values already found int properties file override them.</p>
     * <p>If -ld=true it's an LD computation. Otherwise it's compression.</p>
     * <p>Print the output as CSV</p>
     * <p>Compression output can be used as target input for LD.</p>
     * <p>Both compression and LD will accept input from standard in if an
     */
    public int go() {
        try {
            parseArgs(argv);
            ICompressor.setSqueezeWhite(squeeze);
            if (!isCoprime(c, outChars.length())){
               System.err.println("Warning, c=" + c + " and outchars.length()=" + outChars.length() + " are not mutually-prime.");
               if(quit()) {
                   System.exit(0);
               }
                System.err.println("Warning, continuing despite conflict!");
            }
        } catch (Exception x) {
            failure("Failed getting arguments or properties.", x);
        }
        if (ld) {
            try {
                ScoreDistance.setSigRatio(sr);
                ScoreDistance.setWholeFileRatio(fr);
                sd = new ScoreDistance();
                doLdComparisons();
            } catch (Exception x) {
                failure("Failed doing LD.", x);
            }
        } else {
            try {
                compression();
            } catch (Exception x) {
                failure("Failed doing compression.", x);
            }
        }
        return 0;
    }

    // TODO: Always quit? Definitely for initialization failures but do you want to
    //      quit if one comparison blows up?
    private void failure(String err, Exception x) {
        System.err.println(err);
        x.printStackTrace();
        System.exit(1);
    }

    /**
     * Read the list of targets into a data structure you can scan.
     * <p>
     * The file of signatures should have the same format as the
     * output of compress.
     * <p>
     * Then accept filenames as input. Create a signature for each input
     * file and test it against all of the targets.
     * <p>
     * The input can be either from a file or from standard in.
     * <p>
     */
    protected void doLdComparisons() throws Exception {
        if(verbose) {
            System.err.println(LDResult.outputLine());
        }
        if (printHeader) {
            System.out.println(LDResult.header(outputLevel));
        }
        List<FileSignature> targets = getSigList(targetFile);
        int inputFileCount=0;
        if (infile == null) {
            // Input expected from the command line
            Scanner scanner = new Scanner(System.in);
            String instr = null;
            while ((instr = scanner.nextLine()) != null) {
                inputFileCount++;
                List<LDResult> ldResults = getLDResults(instr, targets, t);
                printLDResults(ldResults);
            }
            scanner.close();
        } else {
            // input from file
            List<String> fsList = FileAndTimeUtility.readListFromFile(infile);
            // run each input file against all targets.
            for (int i = 0; i < fsList.size(); i++) {
                inputFileCount++;
                String file=fsList.get(i).trim();
                if(file.equals("")){
                    continue;
                }
                List<LDResult> ldResults = getLDResults(fsList.get(i), targets, t);
                if (verbose) {
                    System.err.println("File search " + (i + 1) + " " + file + " complete.");
                }
                printLDResults(ldResults);
            }
            if (verbose) {
                System.err.println("Input files:" + inputFileCount);
                System.err.println("Target files:" + targets.size());
            }
        }
        if (verbose) {
            printMMStdev(ldrSigs);
        }
    }

    /**
     * Statistics about unrelated matches. Note in output that they are extremely low.
     * @param lst
     */
   private void printMMStdev(List<Double> lst) {
       System.err.println("The significance computed for unrelated files should be small, with low variance.");
       double accum = 0;
       for (int i = 0; i < lst.size(); i++) {
           accum = accum + lst.get(i);
       }
       double mean = accum / lst.size();
       double ssd = 0;
       for (int i = 0; i < lst.size(); i++) {
           double diff = mean = lst.get(i);
           ssd = ssd + (diff * diff);
       }
       double var = ssd / lst.size();
       double stdev = Math.sqrt(var);
       StringBuffer sb = new StringBuffer();
       sb.append("\n\n");
       sb.append("Significance mean:");
       sb.append((int) (mean * 10000)/10000d);
       sb.append(" variance:");
       sb.append((int) (var * 10000)/10000d);
       sb.append(" stdev:");
       sb.append((int) (stdev * 10000)/10000d);
       sb.append(" unrelated sig comparisons: ");
       sb.append(lst.size());
       sb.append(" matches:");
       sb.append(matchedSigCount);
       sb.append(" total comparisons:");
       sb.append(sd.getTotalLDCalls());
       System.err.println(sb.toString());
   }

    /**
      Print all the items in a list of results as CSV
     <p>
    TODO: Put a flag in properties for suppressing printing the signatures, etc.
    */
    protected void printLDResults(List<LDResult> lst) {
        for (int i = 0; i < lst.size(); i++) {
            System.out.println(lst.get(i).toShortCsvString(outputLevel));
        }
    }

    static List<Double> ldrSigs = new ArrayList<Double>();
    static int matchedSigCount=0;
    /**
     * Get a list of matches for a single input subject to filtering by the
     * significance criterion, t.
     *
     * @param infile  the file-spec of a file to process against the target signatures
     * @param targets a list of target signatures read in once
     * @param t       a significance criterion the nature of which is TBD
     * @return
     * @throws Exception
     */
    protected List<LDResult> getLDResults(String infile, List<FileSignature> targets,
                                          double t) throws Exception {
        FileSignature fsi = fileSignatureFromFilename(infile);
        List<LDResult> ldResults = new ArrayList<LDResult>();
        for (int j = 0; j < targets.size(); j++) {
            FileSignature fst = targets.get(j);
            int rawLd = sd.getLD(fsi.getSig(), fst.getSig());
            int expectedForRandom =
                    sd.expectedDistanceForSigs(fsi.getSig().length(), fst.getSig().length());
            int est =
                    sd.getLDEst(fsi,fst, rawLd);
            LDResult ldr = new LDResult(
                    infile, fst.getInputFname(),
                    fsi.getInputFileLen(), fst.getInputFileLen(),
                    fsi.getSig(), fst.getSig(),
                    rawLd, expectedForRandom, est,
                    fsi.getC(), fsi.getN(), fsi.getcSet());
            SignificanceResult sdr = sd.significant(ldr, t, rawLd);
            if (sdr.getSignificnt()){
                matchedSigCount++;
                ldr.setSignificance(sdr.getComputedSignificane());
                ldr.setT(sdr.getX());
                ldResults.add(ldr);
                if (verbose) {
                    System.err.println(sdr.toString());
                }
            }
            else {
                // only care about stats for unrelated file LD's
                ldrSigs.add(sdr.getComputedSignificane());
            }
        }
        return ldResults;
    }

    /**
     * Set up a set of target-file signatures to search for each line of input.
     *
     * @return A list of FileSignature objects.
     * @throws Exception if no file, can't read it, malformed, etc.
     */
    protected List<FileSignature> getSigList(String targetFile) throws Exception {
        if (targetFile == null) {
            String err = "No file of targets signatures given.";
            throw new Exception(err);
        }
        List<FileSignature> sigList = new ArrayList<FileSignature>();
        List<String> csvStrings = FileAndTimeUtility.readListFromFile(targetFile);
        for (int i = 0; i < csvStrings.size(); i++) {
            String csvLine = csvStrings.get(i).trim();
            if (csvLine.length()>8) {
                // uncomment this to find a defective line.
                //System.err.println("target:" + i);
                try {
                    sigList.add(new FileSignature(csvLine));
                }
                catch(Exception x){
                    System.err.println("Signature read failed line:" + i + " Empty sig field? Malformed? " + csvLine);
                }
            }
            else {
                System.err.println("Something fishy in the signature list." +
                        "at line:" + Integer.toString(i) + ". Blank line?");
            }
        }
        return sigList;
    }

    /**
     * If there's an input file, read it and process all the files, otherwise expect
     * input from standard in.
     */
    protected void compression() throws Exception {
        if (infile == null) {
            Scanner scanner = new Scanner(System.in);
            String instr = null;
            while ((instr = scanner.nextLine()) != null) {
                fileSignatureFromFilename(instr).compressionOutput(DEF_SB_SIZE);
            }
            scanner.close();
        }
        else if (FileAndTimeUtility.isFileExist(infile)) {
            try {
                List<String> fnames = FileAndTimeUtility.readListFromFile(infile);
                for (int i = 0; i < fnames.size(); i++) {
                    String fn = fnames.get(i);
                    try {
                        fileSignatureFromFilename(fn).compressionOutput(DEF_SB_SIZE);
                    }
                    catch(Exception x){
                        System.err.println("Failed reading input file:" + fn + " msg:" + x.getMessage());
                        continue;
                    }
                }
            } catch (Exception x) {
                System.err.println("Failed for unknown reason:" + infile + " msg:" + x.getMessage());
            }
        } else {
            System.err.println("Input file for compression does not exist:" + infile);
        }
    }

    /**
     * Creaqte a FileSignature object for the named infile.
     * @param fname The filespec of an input file.
     * @return A FileSignature object
     * @throws Exception
     */
    protected FileSignature fileSignatureFromFilename(String fname) throws Exception {
        String contents = FileAndTimeUtility.getFileContents(fname);
        String sig = getCompressor(hashType).compress(contents);
        return new FileSignature(fname,contents.length(),hashType,c,n,outChars,sig);
    }
    protected ICompressor compressor = null;
    protected ICompressor getCompressor(String type) throws Exception{
        if(type.equals("XOR")) {
            if (compressor == null) {
                compressor = new StringCompressorRH(n, c, ICompressor.StringToCharArray(outChars), MinBits, MaxBits, Seed);
                compressor.setN(n);
                compressor.setC(c);
                //System.err.println("charset = " + outChars);
            }
            return compressor;
        }
        else if(type.equals("JAVA")){
            if (compressor == null) {
                compressor = new StringCompressorPlainJava(n, c, ICompressor.StringToCharArray(outChars));
            }
            return compressor;
        }
        else if(type.equals("SHA-256")){
            if (compressor == null) {
                compressor = new SHA256Compressor(n, c, ICompressor.StringToCharArray(outChars));
                compressor.setN(n);
                compressor.setC(c);
            }
            return compressor;
        }
        else throw new Exception("Unknown compressor type:" + type);
    }



//////////////////////////////////////////
//////////////////////////////////////////
////  Just argument stuff below here /////
//////////////////////////////////////////
//////////////////////////////////////////

    /**
     * First read the properties file if one is specified, then parse
     * the command line arguments overriding anything found in properties
     * or set beforehand as a default.
     *
     * @param argv The usual String[] for command line args.
     * @throws Exception
     */
    public void parseArgs(String[] argv) throws Exception {
        if (argv.length % 2 != 0) {
            format();
            System.exit(1);
        }
        for (int i = 0; i < argv.length; ) {
            String a = argTrim(argv[i]);
            String v = argTrim(argv[i + 1]);
            //System.err.println("parameter: " + a + " argument: " + v);
            if (a.equals("p")) {
                propsfile = v;
                try {
                    readPropsFile(propsfile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (a.equals("f")) {
                infile = v;
            } else if (a.equals("c")) {
                c = Integer.parseInt(v);
            } else if (a.equals("n")) {
                n = Integer.parseInt(v);
            } else if (a.equals("ch")) {
                outChars = v;
                ICompressor.setChars(outChars);
            }
            else if (a.equals("ld")) {
                v=v.toLowerCase(Locale.ROOT);
                if (v.equals("true")) {
                    ld=true;
                }
                if(v.equals("false")){
                    ld=false;
                }
            }
            else if (a.equals("ft")) {
                targetFile = v;
            }
            else if (a.equals("t")) {
                t = Double.parseDouble(v);
            }
            else if (a.equals("v")) {
                verbose = Boolean.parseBoolean(v);
            }
            else if (a.equals("h")) {
                printHeader = boolFromString(v);
            }
            else if (a.equals("ol")){
              outputLevel = Integer.parseInt(v);
              if (outputLevel<0 || outputLevel>3) {
                  String err = "Output level must be in range 0:3";
                  throw new Exception(err);
              }
            }
            else if (a.equals("ht")){
               hashType=v;
            }
            else if (a.equals("sr")) {
                // ld this much less than equal len sig
                sr = Double.parseDouble(v);
                System.err.println("sr=" + sr);
            }
            else if (a.equals("fr")) {
                // LS this much less than equal len file
                fr = Double.parseDouble(v);
                System.err.println("fr=" + fr);
            }
            else if (a.equals("s")) {
                // LS this much less than equal len file
                squeeze = Boolean.parseBoolean(v);
                System.err.println("s=" + fr);
            }
            else {
                System.err.println("Unknown argument encountered:" + a);
            }
            i = i + 2;
        }
    }

    /**
     * Remove white space and any leading dash from argument flag.
     * @param s
     * @return
     */
    public String argTrim(String s) {
        s = s.trim();
        if (s.startsWith(ARG_DASHES)) {
            return s.substring(1, s.length());
        }
        return s;
    }

    /**
     * Return a format string (in response to un-parsable arguments.)
     *
     * @return
     */
    public String format() {
        StringBuffer sb = new StringBuffer();
        sb.append("-p <a properties file> Read in a  properties file. Any values on the command.\n");
        sb.append("-f <a file> input data would be a list of files names If -f  is not not.\n");
        sb.append("-c <an integer> 	compression rate.\n");
        sb.append("-n <an integer> 	neighborhhod size.\n");
        sb.append("-ch <char-string>	output-char-set in the form of a string.\n");
        sb.append("-ld true/false estimate mode.\n");
        sb.append("-f <input-file-spec> is a csv file of the form output by the compression step.\n");
        sb.append("-i <input-file-spec> is a csv file of one or more rows you are tyring to match.\n");
        sb.append("-t is how different from the expected value the estimated LD has to be.\n");
        sb.append("-v true/false suppress the informational ouput written to stderr.\n");
        sb.append("-h true/false print the output file header.\n");
        sb.append("-ol 0 to 3, Higher number gives fewer output fields. \n");
        sb.append("-ht hash type XOR, JAVA, SHA-256. \n");
        sb.append("-fr a float [0,1] that is the shrinkage factor for LD or unrelated text. \n");
        sb.append("-sr a float [0.1] that is the shrinkage factor for LD of unrelated signatures. \n");
        sb.append("-s a boolean. Squeeze all strings of whitespace into a single space character. \n");
        return sb.toString();
    }

    // TODO:  Properties stuff should go in another class.

    /**
     * Read the properties file and override any values specified there.
     *
     * @param pfile
     */
    public void readPropsFile(String pfile) throws Exception {
        Properties defaultProps = new Properties();
        FileInputStream in = new FileInputStream(pfile);
        defaultProps.load(in);
        in.close();
        if (getIntVal("c", defaultProps) != null) {
            c = getIntVal("c", defaultProps);
        }
        if (getIntVal("n", defaultProps) != null) {
            n = getIntVal("n", defaultProps);
        }
        if (getStringVal("f", defaultProps) != null) {
            infile = getStringVal("f", defaultProps);
        }
        if (getStringVal("ch", defaultProps) != null) {
            outChars = getStringVal("ch", defaultProps);
            ICompressor.setChars(outChars);
        }
        if (getBoolVal("ld", defaultProps) != null) {
            ld = getBoolVal("ld", defaultProps);
        }
        if (getStringVal("ft", defaultProps) != null) {
            targetFile = getStringVal("ft", defaultProps);
        }
        if (getDoubleVal("t", defaultProps) != null) {
            t = getDoubleVal("t", defaultProps);
        }
        if (getBoolVal("h", defaultProps) != null) {
            printHeader = getBoolVal("h", defaultProps);
        }
        if (getBoolVal("v", defaultProps) != null) {
            verbose = getBoolVal("v", defaultProps);
        }
        if (getIntVal("ol", defaultProps) != null) {
            outputLevel = getIntVal("ol", defaultProps);
        }
        if (getStringVal("ht", defaultProps) != null) {
            hashType = getStringVal("ht", defaultProps);
        }
        if (getDoubleVal("fr", defaultProps) != null) {
            fr = getDoubleVal("fr", defaultProps);
            System.err.println("sr=" + sr);
        }
        if (getDoubleVal("sr", defaultProps) != null) {
            sr = getDoubleVal("sr", defaultProps);
            System.err.println("sr=" + sr);
        }
        if (getBoolVal("s", defaultProps) != null) {
            squeeze = getBoolVal("s", defaultProps);
        }
    }

    private String getStringVal(String a, Properties props) throws Exception {
        String aa = a.toLowerCase(Locale.ROOT);
        Object ob = props.get(aa);
        if(ob==null){
            aa=a.toUpperCase(Locale.ROOT);
            ob = props.get(aa);
        }
        if (ob == null) {
            String err = "No " + a + " value in properties";
        } else {
            //System.err.println("Found  " + a + " value in  properties:" + ob);
            return ((String) ob).trim();
        }
        return null;
    }

    private Boolean getBoolVal(String a, Properties props) throws Exception {
        String aa = a.toLowerCase(Locale.ROOT);
        Object ob = props.get(aa);
        if(ob==null){
            aa=a.toUpperCase(Locale.ROOT);
            ob = props.get(aa);
        }
        if (ob == null) {
            String err = "No " + a + " value in properties";
        } else {
            return boolFromString((String) ob);
        }
        return null;
    }

    private Boolean boolFromString(String str) throws Exception{
        //System.err.println("Found  " + a + " value in  properties:" + props.get(a));

        String arg = (str.trim().toLowerCase(Locale.ROOT));
        if (arg.equals("true")){
            return true;
        }
        else if (arg.equals("false")){
            return false;
        }
        else {
            String err = "getBoolVal() expecting a boolean but got:" + arg;
            throw new Exception(err);
        }
    }
        private Double getDoubleVal(String a, Properties props) throws Exception {
        String aa = a.toLowerCase(Locale.ROOT);
        Object ob = props.get(a);
        if(ob==null){
            aa=a.toUpperCase(Locale.ROOT);
            ob = props.get(aa);
        }
        if (ob == null) {
            String err = "No " + a + " value in properties";
        } else {
            //System.err.println("Found  " + a + " value in  properties:" + ob);
            return Double.valueOf(((String) ob).trim());
        }
        return null;
        }

    private Integer getIntVal(String a, Properties props) throws Exception {
        String aa=a.toLowerCase(Locale.ROOT);
        Object ob = props.get(aa);
        if (ob==null){
            aa=a.toUpperCase(Locale.ROOT);
            ob = props.get(aa);
        }
        if (ob == null) {
            String err = "No " + a + " value in properties";
        } else {
            //System.err.println("Found  " + a + " value in  properties:" + ob);
            return valueOf(((String) ob).trim());
        }
        return null;
    }

    /**
     * Ask if they want to continue and warn if they say yes...
     * @return
     */
    private boolean quit() {
        Console console = System.console();
        if (console==null) {
            return true;
        }
        String input = "";
        System.out.print("Do you want to exit? Y/N: ");
        while (!"Y".equalsIgnoreCase(input) || !"N".equalsIgnoreCase(input)) {
            input = console.readLine();
            if(input.length()>1){
                input=input.substring(0,input.length()-1);
            }
            input=input.toUpperCase(Locale.ROOT);
            if (input.startsWith("Y")) {
                return true;
            }
            if (input.startsWith("N")) {
                return false;
            }
            System.out.print("Do you want to exit? Y/N: ");
        }
        return false;
    }

}
