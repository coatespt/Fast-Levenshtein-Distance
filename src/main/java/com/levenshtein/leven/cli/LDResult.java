package com.levenshtein.leven.cli;

/**
 The data relevant to an LD estimate for two files.
 //
*/
public class LDResult {
        private String infile1;
        private String infile2;
        private int infile1Len;
        private int infile2Len;
        private String sig1;
        private String sig2;
        private int rawLd;
        private int expectedForRandom;
        private int ldEstmate;
        private int c;
        private int n;
        private String cSet;

    public int getInfile1Len(){ return infile1Len; }

    public int getInfile2Len(){ return infile2Len; }

    public String getInfile1() {
        return infile1;
    }

    public void setInfile1(String infile1) {
        this.infile1 = infile1;
    }

    public String getInfile2() {
        return infile2;
    }

    public void setInfile2(String infile2) {
        this.infile2 = infile2;
    }

    public String getSig1() {
        return sig1;
    }

    public void setSig1(String sig1) {
        this.sig1 = sig1;
    }

    public void setSig2(String sig2) {
        this.sig2 = sig2;
    }

    public int getRawLd() {
        return rawLd;
    }

    public void setRawLd(int rawLd) {
        this.rawLd = rawLd;
    }

    public int getExpectedForRandom() {
        return expectedForRandom;
    }

    public void setExpectedForRandom(int expectedForRandom) {
        this.expectedForRandom = expectedForRandom;
    }

    public void setLdEstmate(int ldEstmate) {
        this.ldEstmate = ldEstmate;
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

    public String getcSet() {
        return cSet;
    }

    public void setcSet(String cSet) { this.cSet = cSet; }

    public String getSig2() { return sig2; }

    public int getLdEstmate() { return ldEstmate; }


    public LDResult(String infile1, String infile2,
                    int if1Len, int if2Len,
                    String sig1, String sig2,
                    int rawLd, int expectedForRnd, int ldEst,
                    int c, int n, String cSet) {
        this.infile1 = infile1;
        this.infile2 = infile2;
        this.infile1Len = if1Len;
        this.infile2Len = if2Len;
        this.sig1 = sig1;
        this.sig2 = sig2;
        this.rawLd = rawLd;
        this.expectedForRandom=expectedForRnd;
        this.ldEstmate=ldEst;
        this.c = c;
        this.n = n;
        this.cSet = cSet;
    }

    /**
     * Prints the all the basics plus the actual signatures, which are large.
     * @return
     */
    public String toFullCsvString() {
        StringBuffer sb = new StringBuffer(toShortCsvString());
        sb.append(", ");
        sb.append(sig1);
        sb.append(", ");
        sb.append(sig2);
        return sb.toString();
    }

    /**
     * Leave out the signatures, which can be excessively long
     // TODO: Demo also has scores adjusted for file lengths. Add them?
     * @return
     */
    public String toShortCsvString() {
        StringBuffer sb = new StringBuffer(512);
        sb.append(infile1);
        sb.append(", ");
        sb.append(infile2);
        sb.append(", ");
        sb.append(infile1Len);
        sb.append(", ");
        sb.append(infile2Len);
        sb.append(", ");
        sb.append(expectedForRandom);
        sb.append(", ");
        sb.append(rawLd);
        sb.append(", ");
        sb.append(ldEstmate);
        sb.append(", ");
        sb.append(c);
        sb.append(", ");
        sb.append(n);
        sb.append(", ");
        sb.append(sig1.length());
        sb.append(", ");
        sb.append(sig2.length());
        sb.append(", ");
        sb.append(cSet);
        return sb.toString();
    }


}
