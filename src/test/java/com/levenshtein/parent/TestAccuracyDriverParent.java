package com.levenshtein.parent;

import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.IDistance;
import com.levenshtein.leven.ScoreDistance;
import com.levenshtein.leven.SignificanceResult;
import com.levenshtein.leven.cli.FileSignature;
import com.levenshtein.leven.cli.LDResult;
import org.junit.Test;

import java.util.Date;

/**
 * Abstract test class that requires concrete classes to implement getCompressor() and getDistance()
 * methods.
 * <p>
 * Most of the time in this routine is spent computing the true LD of the files.
 *
 * @author pcoates
 */
public abstract class TestAccuracyDriverParent extends TestParent {
    abstract protected ICompressor getCompressor();
    abstract protected IDistance getDistance();

    static double wholeFileRatio = 0.022;
    static double sigRatio = 0.030d;
    static int COMP_CT = 1000;

    /**
     * Maven blows up if there are no tests in a file named Test*
     */
    @Test
    public void testNothing(){
        System.out.println("testNothing testing nothing.");
    }
    /**
     * This method is called with the path/names of a pair of files and computes a number of
     * statistics.  The most important are the expected LD for two unrelated files of that distance,
     * the actual LD for the given files, and the predicted LD from the signatures.
     * <p>
     * Note that the true LD computation is sensitive to the size. 25k is about as big as is practical
     * to compute this for. Beyond that it takes forever if you don't run out of memory first.
     * <p>
     * The caller should set the parameters of the compression, setC(), setN(), etc. These are in the
     * TestParent class that this class derives from.
     * <p>
     * @param str String Descriptive string for what is being tested.
     * @param f1  String file path one
     * @param f2  String file path two
     * @throws Exception Thrown for file not found, etc.
     */
    public void onFiles(String str, String f1, String f2) throws Exception {
        ScoreDistance scoreD = new ScoreDistance();
        scoreD.setSigRatio(sigRatio);
        scoreD.setWholeFileRatio(wholeFileRatio);
        System.out.println("\nonFiles() executing.");

        IDistance d = getDistance();
        String f1Str = readFile(f1);
        String f2Str = readFile(f2);
        String compressedF1 = getCompressor().compress(f1Str);
        String compressedF2 = getCompressor().compress(f2Str);

        StringBuffer sb = new StringBuffer();
        sb.append("\t" + str + "\n");

        int distCompressed = 0;
        Date start = new Date();
        for (int k = 0; k < COMP_CT; k++) {
            distCompressed = d.LD(compressedF2, compressedF1);
        }
        double rateCompressed = (COMP_CT / ((double)(new Date().getTime() - start.getTime()))) * 1000;

        start = new Date();
        int ldUnCompressed = d.LD(f1Str, f2Str);
        double rateUncompressed = 1.0d / (new Date().getTime() - start.getTime()) * 1000;

        sb.append("\tLD computation rate for raw files: ");
        sb.append(String.format("%.4f", rateUncompressed));
        sb.append(" files/sec");
        sb.append("\n\tLD computation rate sigs:");
        sb.append(String.format("%.4f", rateCompressed));
        sb.append(" pairs/sec");
        sb.append("\n\tSpeed increase: ");
        sb.append(String.format("%.4f", rateCompressed / rateUncompressed));
        sb.append("x\n");

        FileSignature fs1 = new FileSignature(f1,f1Str.length(),"UNK",getC(),getN(),outputCharString,compressedF1);
        FileSignature fs2 = new FileSignature(f2,f2Str.length(),"UNK", getC(),getN(),outputCharString,compressedF2);
        System.out.println("f1:" + fs1.getInputFname() + " len:" + fs1.getInputFileLen() +
                " f2:" + fs2.getInputFname()+ " len:" + fs2.getInputFileLen());
        ScoreDistance sd = new ScoreDistance();
        double expectedForRandomSigs = sd.expectedDistanceForSigs(compressedF1.length(),compressedF2.length());
        //double ldEstimate = sd.getLDEstForOriginals(fs1,fs2,distCompressed);
        double ldEstimate=sd.getLDEst(fs1, fs2, distCompressed);
        LDResult ldr = new LDResult(
                f1, f2,
                fs1.getInputFileLen(), fs2.getInputFileLen(),
                fs1.getSig(), fs2.getSig(),
                distCompressed, (int) expectedForRandomSigs, (int)ldEstimate,
                fs1.getC(), fs1.getN(), fs1.getcSet());
        SignificanceResult sr =  sd.significant(ldr, 0.4, distCompressed);

        double estimatedToReal = 1.0;
        // Can do this because the algorithm returns 0 if the signatures are the same.
        if (ldUnCompressed!=0){
            estimatedToReal=((double)ldEstimate)/ldUnCompressed;
        }
        if (estimatedToReal>1.0d){
            estimatedToReal=Math.pow(estimatedToReal,-1.0);
        } else if (estimatedToReal != 1d){
            estimatedToReal=-1d * estimatedToReal;
        }

        int estLDForUnrelated = sd.expectedDistanceForOriginals(f1Str,f2Str);
        sb.append("\tFile lengths: ");
        sb.append(f1Str.length());
        sb.append(", ");
        sb.append(f2Str.length());
        sb.append("\n");
        sb.append("\tSignature lengths: ");
        sb.append(compressedF2.length());
        sb.append(", ");
        sb.append(compressedF1.length());
        sb.append("\n\tExpected LD for unrelated files:");
        sb.append(estLDForUnrelated);
        sb.append("\tComputed LD of signatures:");
        sb.append(distCompressed);
        sb.append("\n\tExpected LD for random sigs::");
        sb.append((int) expectedForRandomSigs);
        sb.append("\n\tSignificance:");
        sb.append((int)(sr.getComputedSignificane()*10000)/10000.0);
        sb.append("\n\tComputed LD of files:");
        sb.append(ldUnCompressed);
        sb.append("\n\tEstimated LD of files:\t");
        sb.append((int) ldEstimate);
        sb.append("\n\testimate diverges by:");
        sb.append(estimatedToReal);
        System.out.println(sb);
        System.out.flush();
    }
}