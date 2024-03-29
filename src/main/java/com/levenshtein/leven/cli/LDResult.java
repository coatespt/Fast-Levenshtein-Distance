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
        private double significance;
        private double t;
        private int c;
        private int n;
        private String cSet;

    public double getT(){return t;}

    public void setT(double d){
        t = d;}

    public void setSignificance(double s){ significance = s; }

    public double getSignificance(){ return significance; }

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


    public LDResult(String infile1, String infile2, int if1Len, int if2Len,
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
        StringBuffer sb = new StringBuffer(toShortCsvString(0));
        sb.append(", ");
        sb.append(cSet);
        sb.append(", ");
        sb.append(sig1);
        sb.append(", ");
        sb.append(sig2);
        return sb.toString();
    }

    /**
     * Leave out the signatures, which can be excessively long
     * @return
     */
    public String toShortCsvString(int level) {
        StringBuffer sb = new StringBuffer(512);
        sb.append(infile1);
        sb.append(", ");
        sb.append(infile2);
        sb.append(", ");
        if(level==0 || level==1) {
            sb.append(infile1Len);
            sb.append(", ");
            sb.append(infile2Len);
            sb.append(", ");
        }
        if(level==0) {
            sb.append(sig1.length());
            sb.append(", ");
            sb.append(sig2.length());
            sb.append(", ");
        }
        if (level==0) {
            sb.append(expectedForRandom);
            sb.append(", ");
            sb.append(rawLd);
            sb.append(", ");
        }

        sb.append(ldEstmate);

        if (level == 0 || level == 1 || level == 2 ) {
            sb.append(", ");
            sb.append(significance);
        }

        if (level == 0) {
            sb.append(", ");
            sb.append(t);
            sb.append(", ");
            sb.append(c);
            sb.append(", ");
            sb.append(n);
        }
        return sb.toString();
    }

    public static String outputLine(){
        return "\nsig-ld-fr-rand is the signature LD you'd expect for random signatures of this size." +
                "\nraw-sig is the actual signature LD(sig1, sig2)" +
                "\nfile-ld-est is the heuristic estimate of LD of the two files." +
                "\nsignificance is a function of raw-sig, sig-ld-fr-rand, and the two signature lengths." +
                "\nt is minimum significance for which output will be generated. Choice of t depends on the " +
                "\n\tdata type and goal. t=0 returns all data regardless. t=1 returns only perfect signature matches.";
    }

    public static String header(int n){
        if (n==0) {
            return "infile1, infile2, infile1-len, infile2-len, sig1-len, sig2-len, sig-ld-fr-rand, raw-sig-ld, " +
                    "file-ld-est, significance, t, c, n";
        }
        else if (n==1) {
            return "infile1, infile2, infile1-len, infile2-len, file-ld-est, significance";
        }
        if (n==2) {
            return "infile1, infile2, file-ld-est, significance";
        }
        if (n==3) {
            return "infile1, infile2, file-ld-est";
        }
        else {
            return "BAD HEADER NUMBER SPECIFIED IN PROPS OR ARGUMENTS. MUST BE 0:2";
        }
    }

}
