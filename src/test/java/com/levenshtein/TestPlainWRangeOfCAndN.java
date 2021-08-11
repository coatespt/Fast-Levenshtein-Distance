package com.levenshtein;
//import org.apache.log4j.Logger;
import org.junit.Test;

import com.levenshtein.leven.ICompressor;
import com.levenshtein.leven.IDistance;
import com.levenshtein.leven.StringCompressorPlain;
import com.levenshtein.leven.StringDistance;
import com.levenshtein.parent.TestAccuracyDriverParent;

/**
 * Tests and measurements for Levenstein distance estimation for a range of C and N
 * with the standard compressor.
 * 
 * TODO: Needs asserts 
 * 
 * @author pcoates
 */
public class TestPlainWRangeOfCAndN extends TestAccuracyDriverParent {
	//Logger log = Logger.getLogger(TestPlainWRangeOfCAndN.class);


	@Test
	public void testModerateMangledC25() throws Exception {
		System.out.println("testModerateMangledC25()");
		setN(12);
		setC(25);
		onFiles("Moderately mangled, c=25", infile1, infile2);
		System.out.println("testModerateMangledC25() ending");
	}

	@Test
	public void testModerateMangledC50() throws Exception {
		System.out.println("testModerateMangledC50() starting");
		setN(12);
		setC(51);
		onFiles("Moderately mangled, c=51", infile1, infile2);
		System.out.println("testModerateMangledC50() ending");
	}

	@Test
	public void testModerateMangledC100() throws Exception {
		System.out.println("testModerateMangledC100() starting");
		setN(12);
		setC(101);
		onFiles("moderately mangled c=101", infile1, infile2);
		System.out.println("testModerateMangledC100() ending");
	}

	@Test
	public void testHeavilyMangledC100() throws Exception {
		System.out.println("testHeavilyMangledC100() starting");
		setN(12);
		setC(25);
		onFiles("heavily mangled c=25", infile1, infile3);
		System.out.println("testHeavilyMangledC100() ending");
	}

	@Test
	public void testHeavilyMangledC50() throws Exception {
		System.out.println("testHeavilyMangledC50() starting");
		setN(12);
		setC(51);
		onFiles("Heavily mangled c=51", infile1, infile3);
		System.out.println("testHeavilyMangledC50() ending");
	}

	@Test
	public void testHeavilyMangledC25() throws Exception {
		System.out.println("testHeavilyMangledC25() starting");
		setN(12);
		setC(101);
		onFiles("Heavily mangled c=101", infile1, infile3);
		System.out.println("testHeavilyMangledC25() ending");
	}

	@Test
	public void testHeavily50_173_25() throws Exception {
		System.out.println("testHeavily50_173_25() starting");
		setN(12);
		setC(25);
		onFiles("50 of 173 lines deleted at random c=25", infile1, infile4);
		System.out.println("testHeavily50_173_25() ending");
	}

	@Test
	public void testHeavily50_173_50() throws Exception {
		System.out.println("testHeavily50_173_50() starting");
		setN(12);
		setC(51);
		onFiles("50 of 173 lines deleted at random c=51", infile1, infile4);
		System.out.println("testHeavily50_173_50() ending");
	}

	@Test
	public void testHeavily50_173_100() throws Exception {
		System.out.println("testHeavily50_173_100() starting");
		setN(12);
		setC(101);
		onFiles("50 of 173 lines deleted at random c=101", infile1, infile4);
		System.out.println("testHeavily50_173_100() ending");
	}

	@Test
	public void testOnBiggerFile() throws Exception {
		System.out.println("testOnBiggerFIle() starting");
		setN(12);
		setC(101);
		onFiles("Identical 14k files c=101", infile5, infile5);
		System.out.println("testOnBiggerFIle() starting");
	}

	@Test
	public void testOnBiggerFileVsOrig() throws Exception {
		System.out.println("testOnBiggerFIleVsOrig() starting");
		setN(12);
		setC(101);
		onFiles("Original 7.4k version against 14k files n=12 c=101", infile1, infile5);
		System.out.println("testOnBiggerFIleVsOrig() ending");
	}

	@Test
	public void testOnSameSizeFiles100n12() throws Exception {
		System.out.println("testOnSameSizeFiles100n12() starting");
		setN(12);
		setC(101);
		onFiles("Same size 14k file with unrelated Latex content n=12  c=101",
				infile6, infile7);
		System.out.println("testOnSameSizeFiles100n12() ending");
	}

	@Test
	public void testOnSameSizeFiles50n12() throws Exception {
		System.out.println("testOnSameSizeFiles50n12() starting");
		setN(12);
		setC(51);
		onFiles("Same size 14k file with unrelated Latex content n=12  c=51",
				infile6, infile7);
		System.out.println("testOnSameSizeFiles50n12() ending");
	}

	@Test
	public void testOnSameSizeFiles25n12() throws Exception {
		System.out.println("testOnSameSizeFiles25n12() starting");
		setN(12);
		setC(25);
		onFiles("Same size 14k file with unrelated Latex content n=12  c=25",
				infile6, infile7);
		System.out.println("testOnSameSizeFiles25n12() ending");
	}

	@Test
	public void testOnSameSizeFiles100n6() throws Exception {
		System.out.println("testOnSameSizeFiles100n12() starting");
		setN(6);
		setC(101);
		onFiles("Same size 14k file with unrelated Latex content n=6  c=101",
				infile6, infile7);
		System.out.println("testOnSameSizeFiles100n12() ending");
	}

	@Test
	public void testOnSameSizeFiles50n6() throws Exception {
		System.out.println("testOnSameSizeFiles60n6() starting");
		setN(6);
		setC(51);
		onFiles("Same size 14k file with unrelated Latex content n=6  c=51",
				infile6, infile7);
		System.out.println("testOnSameSizeFiles60n6() ending");
	}

	@Test
	public void testOnSameSizeFiles25n6() throws Exception {
		System.out.println("testOnSameSizeFiles25n6() starting");
		setN(6);
		setC(25);
		onFiles("Same size 14k file with unrelated Latex content n=6  c=25",
				infile6, infile7);
		System.out.println("testOnSameSizeFiles25n6() ending");
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
	

	ICompressor compressor=null;
	protected ICompressor getCompressor(){
		if(compressor==null){
			compressor = new StringCompressorPlain();
		}
		compressor.setC(c);
		compressor.setN(n);
		return compressor;
	}
	
	private IDistance distance = null;
	protected IDistance getDistance(){
		if(distance==null){
			distance = new StringDistance();
		}
		return distance;
	}
	
}
