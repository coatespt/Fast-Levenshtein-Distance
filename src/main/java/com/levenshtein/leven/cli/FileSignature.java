package com.levenshtein.leven.cli;


import utilities.file.FileAndTimeUtility;

import java.util.ArrayList;
import java.util.List;

/**
  Hold a signature and the parameters that define it.
    These are the same as the output of the compression step.
    input-fname, c, n, char-set, signature
*/
public class FileSignature {
    public static String DELIM = ",";
    private String inputFname;
    private int inputFileLen;
    private int c;
    private int n;
    private String cSet;
    private String sig;

    /**
     * Parse a CSV file of signatures into a List of FileSignature objects.
     * @param sigFile
     * @return
     */
    public List<FileSignature> sigsFromFile(String sigFile) throws Exception{
        List<FileSignature> sigs = new ArrayList<FileSignature>();
        List<String> csvStrings = FileAndTimeUtility.readListFromFile(sigFile);
        for(int i=0; i<csvStrings.size(); i++){
           sigs.add(new FileSignature(csvStrings.get(i)));
        }
        return sigs;
    }


    /**
     * Create a FileSignature object from a csv line of the following form:
     * input-fname, c, n, char-set, signature
     * @param csv
     * @throws Exception
     */
    public FileSignature(String csv) throws Exception {
        List<String> lst = FileAndTimeUtility.getStringListFromString(csv,DELIM);
        setInputFname(lst.get(0));
        setInputFileLen(Integer.parseInt(lst.get(1)));
        setC(Integer.parseInt(lst.get(3)));
        setN(Integer.parseInt(lst.get(4)));
        setcSet(lst.get(5));
        setSig(lst.get(6));
    }

    /**
     * Constructor takes all five arguments. This will normally be built from
     * CSV rows of the fields in the same order.
     *
     * @param infile     The name of the file the signature represents.
     * @param comp       The compression rate.
     * @param neigh      The size of the neighborhood.
     * @param cSet       The output character set.
     * @param signature  The signature.
     */
    public FileSignature(String infile, int flen, int comp, int neigh,
                         String cSet, String signature){
       this.inputFname=infile;
       this.inputFileLen=flen;
       this.c=comp;
       this.n=neigh;
       this.cSet=cSet;
       this.sig=signature;
    }

    public String getcSet() {
        return cSet;
    }

    public void setcSet(String cSet) {
        this.cSet = cSet;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public String getInputFname() {
        return inputFname;
    }

    public void setInputFname(String inputFname) {
        this.inputFname = inputFname;
    }

    public int getInputFileLen(){
        return inputFileLen;
    }

    public void setInputFileLen(int v){
        inputFileLen=v;
    }
    public void compressionOutput(int defsbsize) {
        StringBuffer sb = new StringBuffer(defsbsize);
        sb.append(getInputFname());
        sb.append(",");
        sb.append(getInputFileLen());
        sb.append(",");
        sb.append(getSig().length());
        sb.append(",");
        sb.append(c);
        sb.append(",");
        sb.append(n);
        sb.append(",");
        sb.append(getcSet());
        sb.append(",");
        sb.append(getSig());
        System.out.println(sb.toString());
        System.out.flush();
    }

}
