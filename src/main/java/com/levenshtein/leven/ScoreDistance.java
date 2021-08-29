package com.levenshtein.leven;

import com.levenshtein.leven.cli.LDResult;
import utilities.file.FileAndTimeUtility;

import java.util.logging.Logger;

/**
 * Packages up the computation of LD estimates, plus some other convenience methods.
 * TODO: Strings with smaller LD are estimated more accurately than
 *  strings with large LD.
 *
 * @author peter
 */
public class ScoreDistance {
    private final Logger log = Logger.getLogger(String.valueOf(ScoreDistance.class));
    private IDistance distance = null;

    /**
     * Need to parameterized this with any parameters for computing significance.
     */
    public ScoreDistance() {
    }

    /**
     * TODO This once made sense. Does it still? Assumption is there might be
     *  more than one way to score significance.
     *
     * @return
     */
    private IDistance getDistance() {
        if (distance == null) {
            distance = new StringDistance();
            log.info("Creating IDistance instance:" + distance.getClass().getName());
        }
        return distance;
    }

    /**
     * Approximate amount by which the LD of a pair of equal length random
     * text English strings is * smaller than their length.
     * <p></p>
     * This is used to fudge the product of signature LD and compression
     * factor to adjust the expectation.
     * TODO: Is this an empirical value computed from a corpus? Where did it come
     *  from and isn't it dependent upon the type of data being processed?
     *  Several aspects of this need looking at.
     */
    private double wholeFileRatio = 0.022;

    /**
     * Amount by which the LD of a pair of equal size plain-generated signatures
     * is smaller than their length.
     * <p></p>
     * This is used to fudge product of signature LD and compression factor
     * to adjust the expectation.
     * TODO: As with wholeFileRatio, there are multiple of issues.
     *  This could be stabler than the wholeFileRatio because the signatures are
     *  pseudo-random while raw text is not. It's probably bigger because the
     *  entropy of the raw text is lower--e.g., disproportionately many of a few chars
     *  compared to the signatures, which have approximately equal numbers of
     *  almost seventy characters.
     */
    private double sigRatio = 0.030d;

    /**
     * Empirical estimate of the amount by which the LD of a pair of equal
     * length plain-generated signatures is smaller than their length.
     */
    public double getSigRatio() {
        return sigRatio;
    }

    /**
     * Amount by which the LD of a pair of equal length plain-generated
     * signatures is smaller than their length.
     */
    public void setSigRatio(double sigRatio) {
        this.sigRatio = sigRatio;
    }

    /**
     * Amount by which the LD of a pair of equal length random text
     * strings is smaller than their length.
     */
    public double getWholeFileRatio() {
        return wholeFileRatio;
    }

    /**
     * Amount by which the LD of a pair of equal length random text strings
     * is smaller than their length.
     */
    public void setWholeFileRatio(double wholeFileRatio) {
        this.wholeFileRatio = wholeFileRatio;
    }

    /**
     * Adjust an estimate for the difference between the LD of randomly chosen English
     * text and the LD of the corresponding signatures differs.
     * <p>
     * Signatures have higher entropy, hence a relatively greater LD.
     * <p>
     * sigRatio is the average ratio of the LD of signatures to the
     * corresponding string lengths to the ratio of the LD of the original
     * strings to the string length  (for same-length originals).
     *
     * TODO Review this with Frank.
     *
     * @param in double The an estimated distance.
     * @return
     */
    public double fudgeFactor(double in) {
        double correctionFactor = sigRatio - wholeFileRatio;
        double v = in + (in * (correctionFactor));
        return v;
    }

    /**
     * The expected distance of two random strings of lengths s1 and s2, give
     * the expected contraction of LD (fudge factor);
     *
     * @param s1
     * @param s2
     * @return
     */
    public int expectedDistance(int s1, int s2) {
        return (int) fudgeFactor(Math.max(s1, s2)) + Math.abs(s1 - s2);
    }

    /**
     * The expected distance of two random strings of lengths s1 and s2, give
     * the expected contraction of LD (fudge factor);
     *
     * @param s1
     * @param s2
     * @return
     */
    public int expectedDistance(String s1, String s2) {
        return (int) fudgeFactor(Math.max(s1.length(), s2.length())) +
                Math.abs(s1.length() - s2.length());
    }

    /**
     * Given two signatures and the length of the longer original string, compute
     * the raw estimate as LD(sig1,sig2)/longerSigLen*longerOriginalStringLen.
     * <p>
     * Adjust this string by the fudge factor that considers the ratio of LD to string
     * lengths for originals and signatures (they differ.)
     * <p>
     * Get the estimated LD of two files.
     *
     * @param f1 String file name.
     * @param f2 String file name.
     * @return double Return the estimate.
     * @throws Exception if anything fails.
     */
    public int getLDEst(String sig1, String sig2, int longerUnCompressed,
                        int shorterUncompressed) throws Exception {
        int longer = Math.max(sig1.length(), sig2.length());
        int ld = getDistance().LD(sig1, sig2);
        double computedLenRatioPlain = ld / (double) longer;
        double estimatedUnadjusted = computedLenRatioPlain * longerUnCompressed;
        return (int) fudgeFactor(estimatedUnadjusted);
    }

    /**
     *
     * @param ldr An LDResult object with the filenames, LD, and related data
     * @param x   A double giving the threshold of signficance
     *
     * @return boolean as the two files are/are-not to be considered related
     * @throws Exception
     */
    public boolean significant(LDResult ldr, double x) throws Exception {
        int shorter = Math.min(ldr.getSig1().length(), ldr.getSig2().length());

        int signatureLenDiff = Math.abs(ldr.getSig1().length() - ldr.getSig2().length());

        // After allowing for the difference in length, we'd expect the LD of the sigs to be about.
        // for random pairs of files of this length.
        double expectedOverlapLD = shorter * (1-getSigRatio());

        int actualLd = ldr.getRawLd();

        // The calculaed LD minus the number of characters in the difference, all of which would
        //    incu an edit operation.
        int adjustedActualLD = actualLd - signatureLenDiff;

        // The shorter must be almost entirely contained in the longer
        if (adjustedActualLD == 0) {
            return true;
        }

        if (expectedOverlapLD == 0) {
            throw new Exception("expected overlap LD computed to be 0! This should never happen.");
        }

        // If the signatures are from unrelated files, the computed LD, adjusted for the difference
        //  in length and the expected LD for files of that length should be close, so diff should
        //  small, making the quotient close to 0.
        double diff = Math.abs(expectedOverlapLD - adjustedActualLD);
        double significance = diff / expectedOverlapLD;

        if (significance < x) {
            System.err.println("file:" + ldr.getInfile1() + " file2: " + ldr.getInfile2() + "Significance x:" + significance + " x:" + x);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return the true LD of two files (extract contents)
     *
     * @param f1 String filename
     * @param f2 String filename
     * @return The LD of the contents of the files
     * @throws Exception
     */
    public int getLDForFiles(String f1, String f2) throws Exception {
        String f1Str = FileAndTimeUtility.getFileContents(f1);
        String f2Str = FileAndTimeUtility.getFileContents(f2);
        return getLD(f1Str, f2Str);
    }

    /**
     * Get the LD of two strings (not files.)
     *
     * @param str1
     * @param str2
     * @return
     * @throws Exception
     */
    public int getLD(String str1, String str2) throws Exception {
        return getDistance().LD(str1, str2);
    }

    /**
     * TODO: This is supposed to just say if it's significant
     *  Need to find a place to put all the rest of what it computes
     * @param ldr
     * @param x
     * @return
    public boolean goodEnough(LDResult ldr, double x) {
        int longerOriginal = Math.max(ldr.getInfile1Len(),ldr.getInfile2Len());
        double actLdToLen=ldr.getLdEstmate()/(double) longerOriginal;
        double unscldErrPln=ldr.getRawLd()==0?0:Math.abs(ldr.getLdEstmate()-ldr.getRawLd())/(double)ldr.getRawLd();
        double corrected = ldr.getLdEstmate()/1.7d;
        double unscldErrCorrected=ldr.getRawLd()==0?0:Math.abs(corrected-ldr.getLdEstmate())/(double)ldr.getRawLd();
        double correctedScaled = (actLdToLen==0||corrected==0)?0:(unscldErrCorrected*actLdToLen);
        double scaledErrorPlain=(actLdToLen==0||unscldErrPln==0)?0:(unscldErrPln*actLdToLen);

        //System.err.println("actual:" + ldr.getRawLd() + " unscaled error:" + unscldErrPln +
        //         " scaled error:" + scaledErrorPlain + " corrected scaled error:" +  correctedScaled) ;

        System.err.println("sig LD:" + ldr.getRawLd() + " sig"

        if (corrected <= x){
        }
        return true;
    }
     */

}
