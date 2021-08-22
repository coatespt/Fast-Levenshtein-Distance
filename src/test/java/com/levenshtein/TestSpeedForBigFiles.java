package com.levenshtein;

//import org.apache.log4j.Logger;
import org.junit.Test;

import com.levenshtein.leven.StringDistance;
import com.levenshtein.leven.StringCompressorPlain;
import utilities.mechanic.TimeAndRate;
import com.levenshtein.parent.TestParent;

/**
 * Run on some combinations of files 4K to 30k range and print out some
 * results.
 * <p>
 * TODO This may no longer be necessary as it repeats stuff done elsewhere.
 * <p>  
 * @author pcoates
 *
 */
public class TestSpeedForBigFiles extends TestParent {

	//static Logger log = Logger.getLogger(TestSpeedForBigFiles.class);

	@Test
	public static void testSpeedForBigCompressedFiles25() throws Exception {
		System.out.println("testSpeedForBigCompressedFiles25()");
		speedForBigCompressedFiles(25, 8, infile1, infile2);
	}

	@Test
	public static void testSpeedForBigCompressedFiles25Long() throws Exception {
		System.out.println("testSpeedForBigCompressedFiles25Long()");
		speedForBigCompressedFiles(25, 8, infile6, infile7);
	}

	@Test
	public static void testSpeedForBigCompressedFiles50() throws Exception {
		System.out.println("testSpeedForBigCompressedFiles50()");
		speedForBigCompressedFiles(50, 8, infile1, infile2);
	}

	@Test
	public static void testSpeedForBigCompressedFiles50Long() throws Exception {
		System.out.println("testSpeedForBigCompressedFiles50Long()");
		speedForBigCompressedFiles(50, 8, infile6, infile7);
	}

	@Test
	public static void testSpeedForBigCompressedFiles100() throws Exception {
		System.out.println("testSpeedForBigCompressedFiles100()");
		speedForBigCompressedFiles(100, 8, infile1, infile2);
	}

	@Test
	public static void testSpeedForBigCompressedFiles100Long() throws Exception {
		System.out.println("testSpeedForBigCompressedFiles100Long()");
		speedForBigCompressedFiles(100, 8, infile6, infile7);
	}

	@Test
	public static void testSpeedForBigCompressedFiles200() throws Exception {
		System.out.println("testSpeedForBigCompressedFiles200()");
		speedForBigCompressedFiles(200, 8, infile1, infile2);
	}

	@Test
	public static void testSpeedForBigCompressedFiles200Long() throws Exception {
		System.out.println("testSpeedForBigCompressedFiles200Long()");
		speedForBigCompressedFiles(200, 8, infile6, infile7);
	}

	/**
	 * Exercise distance calculation for compressed larger files. This is called
	 * for a range of C.
	 * 
	 * @param c
	 * @throws Exception
	 */
	public static void speedForBigCompressedFiles(int c, int n, String infile1, String infile2) throws Exception {
		int iterations = 1000;
		StringDistance d = new StringDistance();
		StringCompressorPlain comp = new StringCompressorPlain();
		String longOne = readFile(infile1);
		String longOneWithJunk = readFile(infile2);
		comp.setC(c);
		comp.setN(n);
		String compressedWithJunk = comp.compress(longOne);
		String compressed = comp.compress(longOneWithJunk);
		System.out.println("\n\tspeedForBigCompressedFiles() Testing speed for LD after compression"
						+ "\n\tcompression factor:" + c
						+ "\n\titerations: " + iterations
						+ "\n\ton files of lengths: " + longOne.length() + " and: " + longOneWithJunk.length() 
						+ "\n\tcompressed to sigs lengths: " + compressed.length() + " and:" + compressedWithJunk.length());
		TimeAndRate tAndR = new TimeAndRate();
		for (int i = 0; i < iterations; i++) {
			tAndR.event();
			@SuppressWarnings("unused")
			int distUnCompressed = d.LD(compressed, compressedWithJunk);
		}
		tAndR.compute();
		double rate = tAndR.rateSecs();
		System.out.println("\tspeedForBigCompressedFiles() " + iterations + " iterations of LD in: "
				+ tAndR.elapsedMS() + " milliseconds"
				+ "\n\tequivalent to rate:" + rate + " pairs/sec\n");
	}

}
