package com.levenshtein.leven.cli;

/**
 The data relevant to an LD estimate for two files.
 //
*/
public class LDResult {
        private String infile1;
        private String infile2;
        private String sig1;
        private String sig2;
        private int rawLd;
        private int expectedForRandom;
        private int ldEstmate;
        private int c;
        private int n;
        private String cSet;

    public LDResult(String infile1, String infile2, String sig1, String sig2,
                    int rawLd, int expectedForRnd, int ldEst,
                    int c, int n, String cSet) {
        this.infile1 = infile1;
        this.infile2 = infile2;
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
