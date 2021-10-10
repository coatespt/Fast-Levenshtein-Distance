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
     * text English strings is smaller than their length.
     * TODO: This is not reliable. Need a better way to measure it and specify it as a parameter
     * <p></p>
     * factor to adjust the expectation.
     */
    private static double wholeFileRatio = 0.22;

    /**
     * Amount by which the LD of a pair of equal size plain-generated signatures
     * is smaller than their length.
     * <p>
     * This should be stabler than the wholeFileRatio because the signatures are
     *  pseudo-random while raw text is not however, it may not be stable w.r.t.
     *  the size of the output character set.
     *
     * TODO: This may not be reliable
     */
    private static double sigRatio = 0.03d;

    /**
     * Empirical estimate of the amount by which the LD of a pair of equal
     * length plain-generated signatures is smaller than their length.
     */
    public static double getSigRatio() {
        return sigRatio;
    }

    /**
     * Amount by which the LD of a pair of equal length plain-generated
     * signatures is smaller than their length.
     */
    public static void setSigRatio(double sigRatio) {
        sigRatio = sigRatio;
    }

    /**
     * Amount by which the LD of a pair of equal length random text
     * strings is smaller than their length.
     */
    public static double getWholeFileRatio() {
        return wholeFileRatio;
    }

    /**
     * Amount by which the LD of a pair of equal length random text strings
     * is smaller than their length.
     */
    public static void setWholeFileRatio(double wholeFileRatio) {
        wholeFileRatio = wholeFileRatio;
    }

    /**
     * The expected distance of two random signatures of lengths s1 and s2,
     *  give the empirically determined ratio of LD to signature length for
     *  same-length strings. Note, length difference matters.
     * @param s1
     * @param s2
     * @return
     */
    public int expectedDistanceForSigs(int s1, int s2) {
        int shorter = Math.min(s1, s2);
        double shorterEst = shorter * (1 - sigRatio);
        int overlapPart =  Math.abs(s1 - s2);
        double est = shorterEst + overlapPart;
        //int oldEst = (int) (Math.max(s1, s2) * sigRatio)  + Math.abs(s1 - s2);
        //double oldToNew = oldEst/(1.0*est);
        return (int) est;
    }

    /**
     * Compute the expected LD for two unrelated text strings of the given lengths.
     * The difference in length contributes one edit per character.
     * The LD of the two shorter lengths averages about 0.22 less than that length.
     *
     * @param t1 The length of the first string.
     * @param t2 The length of the second string.
     * @return the expected LD of strings of that length.
     */
    public int expectedDistanceForOriginals(int t1, int t2){
        int unmatched = Math.abs(t1-t2);
        int matched =  Math.min(t1,t2);
        int expected = (int) (matched * (1-getWholeFileRatio()) + unmatched);
        return expected;
    }

    /**
     * Compute the expected LD for two unrelated text strings.
     * We take the lengths and call the numeric version of this function.
     *
     * @param t1 The first string.
     * @param t2 The second string.
     * @return the expected LD of strings of that length.
     *
     * @param s1
     * @param s2
     * @return
     */
    public int expectedDistanceForOriginals(String s1, String s2){
       return expectedDistanceForOriginals(s1.length(), s2.length());
    }



    /**
     * TODO: This is obsolete and incorrect but is used in Demo. Switch Demo to the version that
     *  uses FileSignature objects plus LD and delete this incorrect version.
     *
     * @param sig1 String file name.
     * @param sig1 String file name.
     * @return double Return the estimate.
     * @throws Exception if anything fails.
     *
     */
    public int getLDEst(String sig1, String sig2, int longerUnCompressed,
        int shorterUncompressed, Integer rawLd) throws Exception {
        int longerSig = Math.max(sig1.length(), sig2.length());
        int shorterSig = Math.min(sig1.length(), sig2.length());
        int sigDiff = longerSig-shorterSig;

        double effectiveC = (longerUnCompressed+shorterUncompressed)/(1.0*(longerSig+shorterSig));

        //int ld = rawLd!=null?rawLd: getDistance().LD(sig1, sig2);
        int ld = rawLd!=null?rawLd: getLD(sig1, sig2);

        // The total file length difference takes 1:1 character operations to create
        double ldForTheDiff = effectiveC * sigDiff;

        //
        double ldForTheRest = ((ld-sigDiff) * effectiveC) * (1.0 + wholeFileRatio) ;

        // double computedLenRatioPlain = ld / (double) longer;
        //double estimatedUnadjusted = computedLenRatioPlain * longerUnCompressed;
        int retval =  (int) (shorterSig * sigRatio)  + sigDiff;
        return retval;
    }

    /**
     * Compute the estimated LD of two files represented by FileSignature objects, plus
     * the computed LD of the two signatures.The FileSignature objects hold
     * the respective signatures as well as some information about the files.
     * They do not hold the file contents.
     * TODO: This appears to give very good estimates for related files but progressively
     *  poorer estimates as the objects diverge. For unrelated objects the estimates
     *  are about 0.69 of what they should be.
     First of all, I think we agree on the majority of your formula except the best value for W as well as the last formula which currently in the implementation says

     double ldForTheRest = (sigLD-sigDiff) * effectiveC * (1.0 + wholeFileRatio) ;

     ldForRest describes the portion of the signatures that do not match, e.g., sigA = XXXYYY and sigB=XXAA,
     this focuses on the AA part. As we know the ration, we multiply it by the original ratio (e.g., 2x 48)
     thus these two elements in the signature correspond approx 96 characters in the original files.
     But, in fact, we know from our tests that even though they are not matching, there will be a little overlap
     (W = 0.22), so instead of adding 96 to the signature, we have to reduce the 96 by what "usually"
     overlaps which is 96/1,22. Remark: the goal is to come as close as possible to the original LD and there the 0.22 would exists.

     I hope this makes sense....

     *
     * @param fs1
     * @param fs2
     * @param rawSigLD
     * @return
     * @throws Exception
     */
    public int getLDEst(FileSignature fs1, FileSignature fs2, Integer rawSigLD) throws Exception {
        int longerSig = Math.max(fs1.getSig().length(), fs2.getSig().length());
        int shorterSig = Math.min(fs1.getSig().length(), fs2.getSig().length());
        int sigDiff = longerSig-shorterSig;
        int longerUnCompressed = Math.max(fs1.getInputFileLen(), fs2.getInputFileLen());
        int shorterUnCompressed = Math.min(fs1.getInputFileLen(), fs2.getInputFileLen());

        // We know how much they compressed because we have the original file lengths.
        double effectiveC = (longerUnCompressed+shorterUnCompressed)/(1.0*(longerSig+shorterSig));

        //int ld = rawLd!=null?rawLd: getDistance().LD(sig1, sig2);
        int sigLD = rawSigLD!=null?rawSigLD: getLD(fs1.getSig(), fs2.getSig());

        // The total file length difference contributes 1:1 character operations.
        //
        double ldForTheDiff = longerUnCompressed-shorterUnCompressed;

        double ldForTheRest = 0d;

        boolean ptcFormula=true;
        double tst = 0;
        if (ptcFormula) {
            // This one is the worst so far.
            //
            //ldForTheRest = (sigLD-sigDiff) * effectiveC * (1.0 + wholeFileRatio) ;

            // These give good estimates for both files that completely different and files that are
            // closely related.  The uncommented one seems marginally better but they are very close.
            // Details in the spreadsheet.
            //ldForTheRest = (sigLD-sigDiff) * effectiveC * (1.0/(1.0d+sigRatio));
            ldForTheRest  = (sigLD-sigDiff) * effectiveC * (1.0d-sigRatio);

        } else {
            // Frank's formula. Somewhat more accurate for files that are close. A lot worse for files
            // that are different.
            ldForTheRest = (sigLD - sigDiff) * effectiveC * ((1.0d-sigRatio)/(1.0d-wholeFileRatio));
        }

        int retval =  (int) (ldForTheDiff + ldForTheRest);

        return retval;
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

        // The difference in length requires one operation per character of length difference.
        // The equal shorter segments require fewer operations than their length by a factor of sigRatio,
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
     * Return the actual LD of two files (extract contents)
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


    private static int totalCalls = 0;
    public static int getTotalLDCalls(){
        return totalCalls;
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
        //System.err.println("getLD() s1:"+str1.length()+" s2:"+str2.length());
        totalCalls++;
        return getDistance().LD(str1, str2);
    }
}
