package com.levenshtein.leven.cli;


import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.StringCompressorRH;
import utilities.exception.BadValueException;
import utilities.file.FileAndTimeUtility;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import static java.lang.Integer.valueOf;

/**
 * Command Line Interface
 *
 */
public class Cli {
    // So we want one or two?
    public static String ARG_DASHS = "-";
    // Default allocation for StringBuffer
    public static int DEF_SB_SIZE=1024;

    protected String [] argv = null;
    protected String propsfile=null;
    protected String infile = null;
    protected int c = 0;
    protected int n = 0;
    protected String outChars;
    protected boolean ld = false;
    protected String targetFile = null;
    double x = 0.0;

    public Cli(String [] argv){
        this.argv=argv;
    }

    public static void main(String [] argv){
        System.err.println("This is the CLI running with args:" + Integer.toString(argv.length));
        Cli cli = new Cli(argv);
        cli.go();
        System.err.println("This is the CLI exiting...");
    }

    public int go(){
        try {
            parseArgs(argv);
        }
        catch(Exception bx){
            System.err.println("Something went wrong with arguments or properties content.");
            bx.printStackTrace();
            System.exit(1);
        }
        if (ld) {
           ldCompare();
        }
        else {
            try {
                compression();
            }
            catch(Exception x){
                x.printStackTrace();
            }
        }
        return 0;
    }
    // TODO: It would be nice to have a feature that just returns the LD of two signatures.

    /**
     * Read in the list of targets. This file of signatures should have the same format as the output of compress.
     *
     * If there's a signatures **input** file, read it and execute all the LD's. Otherwise, expect
     * signatures input to appear on standard in.
     * TODO: Implement me!
     */
    protected void ldCompare(){
    }

    /**
     * If there's an input file, read it and process all the files, otherwise expect
     * input from standard in.
     */
    protected void compression() throws Exception {
        System.err.println("Starting compression.");
        if (infile==null){
            Scanner scanner = new Scanner(System.in);
            String instr = null;
            while ((instr=scanner.nextLine()) != null){
                System.err.println("" + instr);
                String contents = FileAndTimeUtility.getFileContents(instr);
                String sig = getCompressor().compress(contents);
                output(instr, sig, contents.length());
            }
            scanner.close();
        }
        else if (FileAndTimeUtility.isFileExist(infile)){
            try {
                List<String> fnames = FileAndTimeUtility.readListFromFile(infile);
                for (int i = 0; i < fnames.size(); i++) {
                    String fname = fnames.get(i);
                    String contents = FileAndTimeUtility.getFileContents(fname);
                    String sig = getCompressor().compress(contents);
                    output(fname, sig, contents.length());
                }
            } catch(Exception x){
                System.err.println("Failed reading input file:" + infile + " msg:" + x.getMessage());
            }
        }
        else {
            System.err.println("Input file for compression does not exist:" + infile);
        }
        System.err.println("compression completed normally");
    }

    public void output(String fname, String sig, int flen){
       StringBuffer sb = new StringBuffer(DEF_SB_SIZE);
       sb.append(fname);
       sb.append(",");
       sb.append(flen);
       sb.append(",");
       sb.append(sig.length());
       sb.append(",");
       sb.append(c);
       sb.append(",");
       sb.append(n);
       sb.append(",");
       sb.append(outChars);
       sb.append(",");
       sb.append(sig.length());
       sb.append(",");
       sb.append(sig);
       sb.append(",");
       System.out.println(sb.toString());
       System.out.flush();
    }
    protected ICompressor compressor=null;
    protected ICompressor getCompressor(){
        if(compressor==null){
            ICompressor ic = new StringCompressorRH(n,c,
                    StringCompressorRH.StringToCharArray(outChars),
                    20, 44, 12345);
            ic.setN(n);
            ic.setC(c);
            compressor=ic;
        }
        return compressor;
    }



    /**
     * Remove white space and any leading dash.
     * @param s
     * @return
     */
    public String trim(String s){
       s=s.trim();
       if (s.startsWith(ARG_DASHS)) {
           return s.substring(1,s.length());
       }
       return s;
    }

    /**
     * First read the properties file if one is specified, then parse
     * the command line arguments overriding anything found in properties
     * or set beforehand as a default.
     * @param argv
     * @throws BadValueException
     */
    public void parseArgs(String [] argv) throws BadValueException{
        if (argv.length % 2 != 0){
            format();
            System.exit(1);
        }
       for (int i=0; i < argv.length;) {
           String a = trim(argv[i]);
           String v = trim(argv[i+1]);
           System.err.println("parameter: " + a + " argument: " + v);
           if (a.equals("p")){
                propsfile=v;
                try {
                    readPropsFile(propsfile);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
           }
           else if (a.equals("f")){
                infile=v;
           }
           else if (a.equals("c")){
                c = Integer.parseInt(v);
           }
           else if (a.equals("n")){
                n = Integer.parseInt(v);
           }
           else if (a.equals("ch")){
                outChars = v;
           }
           else if (a.equals("ld")){
                ld = Boolean.getBoolean(v);
           }
           else if (a.equals("ft")){
                targetFile = v;
           }
           else if (a.equals("x")){
                x = Double.parseDouble(v);
           }
           else {
                System.err.println("Unknown argument encountered:" + a);
           }
           i=i+2;
       }
    }

    // -p config.properties -f infile1.csv -c 201 -n 8 -o abcdABCD1234 -ld true -ft target.csv -fi input.csv -x 0.08

    /**
     * Return a format string (in response to un-parsable arguments.)
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

    // Properties file code below here.
    // TODO: You could put this in another class.
    /**
     *  Read the properties file and override any values specified there.
     * @param pfile
     */
    public void readPropsFile(String pfile) throws Exception {
        Properties defaultProps = new Properties();
        FileInputStream in = new FileInputStream(pfile);
        defaultProps.load(in);
        in.close();
        if (getIntVal("c",defaultProps) != null) {
           c = getIntVal("c",defaultProps);
        }
        if (getIntVal("n",defaultProps) != null) {
            n = getIntVal("n",defaultProps);
        }
        if (getStringVal("f",defaultProps) != null) {
            infile = getStringVal("f",defaultProps);
        }
        if (getStringVal("ch",defaultProps) != null) {
            outChars = getStringVal("ch",defaultProps);
        }
        if (getBoolVal("ld",defaultProps) != null) {
            ld = getBoolVal("ld",defaultProps);
        }
        if (getStringVal("ft",defaultProps) != null) {
            targetFile = getStringVal("ft",defaultProps);
        }
        if (getDoubleVal("x",defaultProps) != null) {
            x = getDoubleVal("x",defaultProps);
        }
    }

    private String getStringVal(String a, Properties props) throws Exception{
        Object ob = props.get(a);
        if (ob==null){
            String err = "No "+ a +" value in properties";
        } else {
            System.err.println("Found  "+ a +" value in  properties:" + ob);
            return ((String) ob).trim();
        }
        return null;
    }

    private Boolean getBoolVal(String a, Properties props) throws Exception{
        Object ob = props.get(a);
        if (ob==null){
            String err = "No "+ a +" value in properties";
        } else {
            System.err.println("Found  "+ a +" value in  properties:" + props.get(a));
            return Boolean.valueOf(((String) ob).trim());
        }
        return null;
    }

    private Double getDoubleVal(String a, Properties props) throws Exception{
        Object ob = props.get(a);
        if (ob==null){
            String err = "No "+ a +" value in properties";
        } else {
            System.err.println("Found  "+ a +" value in  properties:" + ob);
            return Double.valueOf(((String)ob).trim());
        }
        return null;
    }

    private Integer getIntVal(String a, Properties props) throws Exception{
        Object ob = props.get(a);
        if (ob==null){
            String err = "No "+ a +" value in properties";
        } else {
            System.err.println("Found  "+ a +" value in  properties:" + ob);
            return valueOf(((String) ob).trim());
        }
        return null;
    }
}
