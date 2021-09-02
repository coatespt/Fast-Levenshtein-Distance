package com.levenshtein.leven;

import com.levenshtein.leven.cli.FileSignature;
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
            //log.info("Creating IDistance instance:" + distance.getClass().getName());
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
    private double wholeFileRatio = 0.22;

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
    private double sigRatio = 0.30d;

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
     * TODO Review this with Frank. THIS LOOKS WRONG.
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
     * The expected distance of two random signatures of lengths s1 and s2,
     *  give the empirically determined ratio of LD to signature length for
     *  same-length strings.
     * @param s1
     * @param s2
     * @return
     */
    public int expectedDistanceForSigs(int s1, int s2) {
        int shorter = Math.max(s1, s2);
        double shorterEst = shorter * (1 - sigRatio);
        // double shorterEst = fudgeFactor(shorter);
        int overlapPart =  Math.abs(s1 - s2);
        double est = shorterEst + overlapPart;
        int oldEst = (int) (Math.max(s1, s2) * sigRatio)  + Math.abs(s1 - s2);
        double oldToNew = oldEst/(1.0*est);
        return (int) est;
    }

    /**
     * The expected distance of two random strings of lengths s1 and s2, give
     * the expected contraction of LD (fudge factor);
     *
     * @param s1
     * @param s2
     * @return
    public int expectedDistanceForSigs(String s1, String s2) {
        return (int) fudgeFactor(Math.max(s1.length(), s2.length())) +
                Math.abs(s1.length() - s2.length());
    }
     */

    /**
     * Given two signatures and the length of the longer original string, compute
     * the raw estimate as LD(sig1,sig2)/longerSigLen*longerOriginalStringLen.
     * <p>
     * Adjust this string by the fudge factor that considers the ratio of LD to string
     * lengths for originals and signatures (they differ.)
     * <p>
     * Get the estimated LD of two files.
     *
     * @param sig1 String file name.
     * @param sig1 String file name.
     * @return double Return the estimate.
     * @throws Exception if anything fails.
     *
     * the difference in signature lengths would represent proportional amounts of the LD of
     * the full files that would require 1:1 single char operations.
     *
     * That much of the estimate should scaled directly from c.
     *
     * That proportion of the sigs length difference should be subtracted from the sigs LD and the
     * remainder scaled up by allowing for
     *
     * The rest would require
     *
     */
    public int getLDEst(String sig1, String sig2, int longerUnCompressed,
        int shorterUncompressed, Integer rawLd) throws Exception {
        int longerSig = Math.max(sig1.length(), sig2.length());
        int shorterSig = Math.min(sig1.length(), sig2.length());
        int sigDiff = longerSig-shorterSig;

        double effectiveC = (longerUnCompressed+shorterUncompressed)/(1.0*(longerSig+shorterSig));

        int ld = rawLd!=null?rawLd: getDistance().LD(sig1, sig2);

        // The total file length difference takes 1:1 character operations to create
        double ldForTheDiff = effectiveC * sigDiff;

        //
        double ldForTheRest = ((ld-sigDiff) * effectiveC) * (1.0 + wholeFileRatio) ;

        // double computedLenRatioPlain = ld / (double) longer;
        //double estimatedUnadjusted = computedLenRatioPlain * longerUnCompressed;
        int retval =  (int) (shorterSig * sigRatio)  + sigDiff;
        return retval;
        //return (int) fudgeFactor(estimatedUnadjusted);
    }
    public int getLDEstForOriginals(FileSignature fsi1, FileSignature fsi2, int ld) throws Exception {
        int longerSig = Math.max(fsi1.getSig().length(), fsi2.getSig().length());
        int shorterSig = Math.min(fsi1.getSig().length(), fsi2.getSig().length());
        int sigDiff = longerSig-shorterSig;

        int f1Len = fsi1.getInputFileLen();
        int f2Len = fsi2.getInputFileLen();
        int fDiff = Math.abs(f1Len-f2Len);

        double effectiveC = (f1Len+f2Len)/(1.0*(longerSig+shorterSig));
        double ldDiff = fDiff * (1.0 - wholeFileRatio);
        double ldForTheRest = (shorterSig *  effectiveC) * (1.0  - wholeFileRatio) ;

        int retVal = (int) ((effectiveC * ld) * (1.0 - wholeFileRatio));

        return retVal;
        //return (int) fudgeFactor(estimatedUnadjusted);
    }


    /**
     *
     * @param ldr An LDResult object with the filenames, LD, and related data
     * @param x   A double giving the threshold of signficance
     *
     * @return boolean as the two files are/are-not to be considered related
     * @throws Exception
     */
    public SignificanceResult significant(LDResult ldr, double x, Integer ldval) throws Exception {

        int shorter = Math.min(ldr.getSig1().length(), ldr.getSig2().length());
        int signatureLenDiff = Math.abs(ldr.getSig1().length() - ldr.getSig2().length());

        // TODO: The sigRatio factor applies only to English text and should be settable either from
        //  properties or by automated analysis of the data.
        //
        // The difference in length requires one operation per character.
        // The equal shorter segments require fewer operations than their length by a factor of sigRatio,
        //  which is an empirically derived quantity
        double expectedSigLD = shorter * (1.0 - sigRatio) + signatureLenDiff ;
        int actualSigLD = ldval!=null? ldval: getLD(ldr.getSig1(),ldr.getSig2());

        // if the signatures are from unrelated files, this difference should be small.
        double diff = Math.abs(expectedSigLD-actualSigLD);

        // The closer to zero the more similar the signatures are.
        // TODO: We need the mean and standard deviation of S for random files
        double significance = (diff / (1.0 * expectedSigLD));

        Boolean isSignificant = (significance > x);

        SignificanceResult retVal = new SignificanceResult(ldr.getSig1().length(),ldr.getSig2().length(), actualSigLD,
                expectedSigLD, significance, isSignificant, x);
        return retVal;
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
