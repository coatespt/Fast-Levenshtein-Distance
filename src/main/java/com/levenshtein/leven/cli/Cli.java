package com.levenshtein.leven.cli;
import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.ScoreDistance;
import com.levenshtein.leven.SignificanceResult;
import com.levenshtein.leven.StringCompressorRH;
import utilities.file.FileAndTimeUtility;
import java.io.FileInputStream;
import java.util.*;
import static java.lang.Integer.valueOf;

/**
 * Important errors
 * TODO: Fixme! Search file fails if blank line is present. Fix that AND don't fail if a line is unreadable.
 *  Count failed lines and fail at some point?
 * TODO: Check for empty sigs and move on. High compression can easily result in such.
 *
 * Command Line Interface
 * <p>
 * Compression: takes list of files from filenames given on command line
 * or on stdin. Output is csv lines on stout. This output can be used
 * as the LD target list.
 * <p>
 * LD matching: Find matches to set of input files among a set of
 * target signatures. Target signatures are input from CSV
 * lines as generated in compression mode.
 * The targets are always input from precomputed signature CSV lines in a file.
 * Note that the input is just file-names while the targets are pre-computed
 * signatures.
 *
 * https://www.johndcook.com/blog/2021/08/14/index-of-coincidence/
 *

 Testing/Demo
 TODO: Deal with the tiny files in ./data/allfiles.  Options:
    Get rid of file smaller than x and rerun the compression
    Handle the case where they compress to zero--does it even come up?

 TODO Verify that the CLI is getting high quality results using the larger charset.

 Possible errors

 TODO: Matching is slow. Look for redundant LD operations (stick in a counter?).

 CLI Enhancements

 TODO: Should have option for input from pre-computed signatures?

 TODO: Implement multi-threading for the matching. Frank says this is low priority.

 TODO: Implement multi-threading for compression. Frank says this is low priority.

 TODO: Flag to control output as per Frank's suggestions.  It's always useless w/o the filenames, right?
    Just the file-LD estimate?
    File-LD and Signature LD?
    Keep all the fields except  t, cn, n
    Keep all statistical fields but drop t, cn, n
    Note we need separate heading lines for each set of outputs.


 Statistical data gathering
 TODO: Run the compression for a range of compression rates and get time as a function of compression.

 TODO: Get a data set for t value as as a function of compression.

 TODO: Add statistics output for a target set. This would require ability to comment signatures.
    Should include longest file, shortest file, longest signture, shortest signature, mean, stdev.

 TODO: Index of Coincidence might be a good measure of signatures quality. See below this item.
 * John Cook blog post on "index of coincidence" which is similar to Renyi entropy
 *  (not quite the same as Shannon entropy.)
 *  i.e. it is the negative log. This is of interest here because the index of coincidence of
 *  of a body of text is characteristic of a language. (Or so I understand.) It should be different
 *  for different human languages and different types of binary data. Investigate this.
 *

 */
public class Cli {
    protected static String ARG_DASHES = "-";
    protected static int DEF_SB_SIZE = 1024;

    static int MinBits=25;
    static int MaxBits=39;
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

    /**
     * A CLI gets all it's setup via command-line arguments but you can
     * set up automated tests by constructing argv list explicitly.
     * @param argv An array of strings in the usual command-line form.
     */
    public Cli(String[] argv) {
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
        } catch (Exception x) {
            failure("Failed getting arguments or properties.", x);
        }
        if (ld) {
            try {
                // TODO: set properties for LD significance?
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
            System.out.println(LDResult.header());
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
                    return;
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

   private void printMMStdev(List<Double> lst) {
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
       sb.append(mean);
       sb.append(" variance:");
       sb.append(var);
       sb.append(" stdev:");
       sb.append(stdev);
       sb.append(" total LD comparisons:");
       sb.append(lst.size());
       sb.append(" matched:");
       sb.append(matchedSigCount);
       System.err.println(sb.toString());
   }

    /**
      Print all the items in a list of results as CSV
     <p>
    TODO: Put a flag in properties for suppressing printing the signatures, etc.
    */
    protected void printLDResults(List<LDResult> lst) {
        for (int i = 0; i < lst.size(); i++) {
            System.out.println(lst.get(i).toShortCsvString());
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
                    sd.getLDEstForOriginals(fsi,fst, rawLd);
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
                // TODO. Catch exception here, log and continue. and continue.
                // uncomment this to find a defective line.
                //System.err.println("target:" + i);
                try {
                    sigList.add(new FileSignature(csvLine));
                }
                catch(Exception x){
                    // This could be from a short file and a high compression rate.
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
                   fileSignatureFromFilename(fnames.get(i)).compressionOutput(DEF_SB_SIZE);
                }
            } catch (Exception x) {
                System.err.println("Failed reading input file:" + infile + " msg:" + x.getMessage());
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
        String sig = getCompressor().compress(contents);
        return new FileSignature(fname,contents.length(),c,n,outChars,sig);
    }

    protected ICompressor compressor = null;

    /**
     * Return an ICompressor object for the given n,c,and output character set
     * that the CLI was initialized with.
     * @return
     */
    protected ICompressor getCompressor() throws Exception{
        //compressor = new StringCompressorRH(getN(), getC(), outputChars , minBits, maxBits, seed);
        if (compressor == null) {
            compressor = new StringCompressorRH(n, c, ICompressor.StringToCharArray(outChars), MinBits, MaxBits, Seed);
            compressor.setN(n);
            compressor.setC(c);
            //System.err.println("charset = " + outChars);
        }
        return compressor;
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
    }

    private String getStringVal(String a, Properties props) throws Exception {
        Object ob = props.get(a);
        if (ob == null) {
            String err = "No " + a + " value in properties";
        } else {
            //System.err.println("Found  " + a + " value in  properties:" + ob);
            return ((String) ob).trim();
        }
        return null;
    }

    private Boolean getBoolVal(String a, Properties props) throws Exception {
        Object ob = props.get(a);
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
        Object ob = props.get(a);
        if (ob == null) {
            String err = "No " + a + " value in properties";
        } else {
            //System.err.println("Found  " + a + " value in  properties:" + ob);
            return Double.valueOf(((String) ob).trim());
        }
        return null;
        }

    private Integer getIntVal(String a, Properties props) throws Exception {
        Object ob = props.get(a);
        if (ob == null) {
            String err = "No " + a + " value in properties";
        } else {
            //System.err.println("Found  " + a + " value in  properties:" + ob);
            return valueOf(((String) ob).trim());
        }
        return null;
    }
}
