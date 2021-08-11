package com.levenshtein;


import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.IDistance;
import com.levenshtein.leven.StringCompressorPlain;
import com.levenshtein.leven.StringDistance;
import com.levenshtein.parent.TestAccuracyDriverParent;
//import org.apache./log4j.Logger;
import org.junit.Test;

/**
 * Drives TestAccuracyDriverParent routines for computing actual LD of pairs of files,
 * 	LD estimated from signatures, error rates based on the known LD of the strings.Y
 *
 * This test runs onFiles() on pairs of files from set of 8 files taken from a single book
 * and modified in various ways. The files are cut to different lengths, bits inserted or deleted, etc.
 *
 * Every possible pair consisting of the eight is tested.
 * @author pcoates
 */
public class TestAccuracyDriverPlain101 extends TestAccuracyDriverParent {
	//Logger log = Logger.getLogger(TestAccuracyDriverPlain101.class);

	public String[] files = {infile1, infile1, infile3, infile4, infile5, infile6, infile7, infile8};

	@Test
	/**
	 * This test runs onFiles() on pairs of files from set of 8 files taken from a single book
	 * and modified in various ways. The files are cut to different lengths, bits inserted or deleted, etc.
	 *
	 * Every possible pair consisting of the eight is tested.
	 * See TestAccuracyDriverParent.java for details on computation
	 * @author pcoates
	 */
	public void testAllAgainstAll101() throws Exception {
		System.out.println("testAllAgainstAll101() Measure estimate quality for modified versions of files of lengths in low-thousands. ");
		setN(8);
		setC(101);
		for (int i = 0; i < files.length; i++) {
			String f1 = files[i];
			for (int j = i + 1; j < files.length; j++) {
				String f2 = files[j];
				StringBuffer sb = new StringBuffer();
				sb.append("\ntestAllAgainstAll101(");
				sb.append(" C: ");
				sb.append(getC());
				sb.append("\tn:");
				sb.append(getN());
				sb.append("\tfile1: ");
				sb.append(f1);
				sb.append("\tfile2: ");
				sb.append(f2);
				sb.append(")");
				onFiles(sb.toString(), f1, f2);
			}
		}
	}

	private int n;

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public int getC() {
		return c;
	}

	public void setC(int c) {
		this.c = c;
	}

	private int c;


	ICompressor compressor = null;

	protected ICompressor getCompressor() {
		if (compressor == null) {
			compressor = new StringCompressorPlain();
		}
		compressor.setC(c);
		compressor.setN(n);
		return compressor;
	}

	private IDistance distance = null;

	protected IDistance getDistance() {
		if (distance == null) {
			distance = new StringDistance();
		}
		return distance;
	}
}
