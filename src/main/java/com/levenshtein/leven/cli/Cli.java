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
 *
 TODO: Matching is way too slow. Look for redundant LD operations. Count them to be sure you aren't missing any as this is where all the time goes.
 TODO: Implement multi-threading for the matching.
 TODO: Currently the input is only filenames. It would be useful to allow input from pre-computed signatures.
 TODO: The fields line is wrong. Fields: input-file, target-file, expectedLD, raw-sig-ld, estimated-ld, c, n, sig1-len, sig2-len, out-char-set, sig1, sig2

 */
public class Cli {
    protected static String ARG_DASHES = "-";
    protected static int DEF_SB_SIZE = 1024;
    protected String[] argv = null;
    protected String propsfile = null;
    protected String infile = null;
    protected int c = 0;
    protected int n = 0;
    protected String outChars;
    protected boolean ld = false;
    protected String targetFile = null;
    protected double x = 0.0;

    private ScoreDistance sd = null;

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
     * input file is not specified</p>
     * @return
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
        printOutputFields();
        List<FileSignature> targets = getSigList(targetFile);
        int inputFileCount=0;
        if (infile == null) {
            // Input expected from the command line
            Scanner scanner = new Scanner(System.in);
            String instr = null;
            while ((instr = scanner.nextLine()) != null) {
                inputFileCount++;
                List<LDResult> ldResults = getLDResults(instr, targets, x);
                printLDResults(ldResults);
            }
            scanner.close();
        } else {
            // input from file
            List<String> fsList = FileAndTimeUtility.readListFromFile(infile);
            for (int i = 0; i < fsList.size(); i++) {
                inputFileCount++;
                String file=fsList.get(i).trim();
                if(file.equals("")){
                    return;
                }
                List<LDResult> ldResults = getLDResults(fsList.get(i), targets, x);
                System.err.println("File search " + (i+1) + " " + file + " complete.");
                printLDResults(ldResults);
            }
            System.err.println("Input files:" + inputFileCount);
            System.err.println("Target files:" + targets.size());
        }
    }

    /**
     * A format line so you know what the output means.
     */
    private void printOutputFields(){
        System.err.println("Fields: input-file, target-file, expectedLD, raw-sig-ld," +
                " estimated-ld, c, n, sig1-len, sig2-len, out-char-set, sig1, sig2");
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

    /**
     * Get a list of matches for a single input subject to filtering by the
     * significance criterion, x.
     *
     * @param infile  the file-spec of a file to process against the target signatures
     * @param targets a list of target signatures read in once
     * @param x       a significance criterion the nature of which is TBD
     * @return
     * @throws Exception
     */
    protected List<LDResult> getLDResults(String infile, List<FileSignature> targets,
                                          double x) throws Exception {
        FileSignature fsi = fileSignatureFromFilename(infile);
        List<LDResult> ldResults = new ArrayList<LDResult>();
        for (int j = 0; j < targets.size(); j++) {
            FileSignature fst = targets.get(j);
            int rawLd = sd.getLD(fsi.getSig(), fst.getSig());
            int expectedForRandom =
                    sd.expectedDistance(fsi.getInputFileLen(), fst.getInputFileLen() );
            int est =
                    sd.getLDEst(fsi.getSig(), fst.getSig(),
                    Math.min(fsi.getSig().length(),fst.getSig().length()),
                    Math.max(fsi.getSig().length(),fst.getSig().length()),
                            rawLd
                    );
            LDResult ldr = new LDResult(infile, fst.getInputFname(),
                    fsi.getInputFileLen(), fst.getInputFileLen(),
                    fsi.getSig(), fst.getSig(),
                    rawLd, expectedForRandom, est,
                    fsi.getC(), fsi.getN(), fsi.getcSet());

            SignificanceResult sdr = sd.significant(ldr, x, rawLd);
            if (sdr.getSignificnt()){
                ldr.setSignificance(sdr.getComputedSignificane());
                ldr.setX(sdr.getX());
                ldResults.add(ldr);
                System.err.println(sdr.toString());
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
                sigList.add(new FileSignature(csvLine));
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
     * Retrun an ICompressor object for the given n,c,and output character set
     * that the CLI was initialized with.
     * TODO: The choice of RH compressor is hard coded. Put it in a property.
     * @return
     */
    protected ICompressor getCompressor() {
        if (compressor == null) {
            ICompressor ic = new StringCompressorRH(n, c,
                    StringCompressorRH.StringToCharArray(outChars),
                    20, 44, 12345);
            ic.setN(n);
            ic.setC(c);
            compressor = ic;
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
            } else if (a.equals("ld")) {
                v=v.toLowerCase(Locale.ROOT);
                if (v.equals("true")) {
                    ld=true;
                }
                if(v.equals("false")){
                    ld=false;
                }
            } else if (a.equals("ft")) {
                targetFile = v;
            } else if (a.equals("x")) {
                x = Double.parseDouble(v);
            } else {
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
        sb.append("-x is how different from the expected value the estimated LD has to be.\n");
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
        if (getDoubleVal("x", defaultProps) != null) {
            x = getDoubleVal("x", defaultProps);
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
            //System.err.println("Found  " + a + " value in  properties:" + props.get(a));
            String arg = ((String) ob).trim().toLowerCase(Locale.ROOT);
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
        return null;
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
