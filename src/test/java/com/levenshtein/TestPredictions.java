package com.levenshtein;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.a140.util.TimeAndRate;
import com.levenshtein.leven.StringCompressorPlain;
import com.levenshtein.leven.StringDistance;
import com.levenshtein.parent.TestParent;

/**
 */
public class TestPredictions extends TestParent {
	Logger log = Logger.getLogger(TestAccuracyDriverPlain101.class);

	int c=100;
	int n=12;
	int a=64;
	int fsize=10000;
	int m=100;


	/**
	 * Sanity check the values detailed in 
	 * 	   /Users/pcoates/Workspaces/personal-wkspc/git-docs/latex/leven/levencalc.tex
	 * @throws Exception
	 */
	@Test
	public void testCompute() throws Exception{
		// One character changed.
		// probability of an output character v a null
		double P0 = 1.0d-1.0d/c;
		// probability original null and modified null
		double Pa = P0*P0; 
		// probability original has null output and modified has non-null
		double Pb = P0/c;
		// probability original has non null output and modified has null
		double Pc = P0/c;
		// proability original not null and modified not null and different
		double Pd = (double)(a-1)/(double)(a*c*c);
		// proability original not null and modified not null and same
		double Pe = (double)(1)/(double)(a*c*c);
		
		double ex1 = (2*P0)/c + Pd; 
		double Epn = n*((2*P0)/c + Pd); 
		
		double Dnm =0d; 
		for(int i=2; i<n; i++){
			Dnm=Dnm+Math.pow((double)m/fsize,i);
		}

		// Show the expected number of modified outputs for 
		double adjustedPerChar=Epn-Dnm;
		double adjustedAllChgs=m*adjustedPerChar;

		StringBuffer sb = new StringBuffer();
		sb.append("Cases:");
		sb.append("\n\tP0");
		sb.append(P0);
		sb.append("\n\tPa");
		sb.append(Pa);
		sb.append("\n\tPb");
		sb.append(Pb);
		sb.append("\n\tPc");
		sb.append(Pc);
		sb.append("\n\tPd");
		sb.append(Pd);
		sb.append("\n\tPe");
		sb.append(Pe);
		log.info(sb.toString());

		// show modified raw expected outputs Epn
		sb = new StringBuffer();
		sb.append("\n\tEpn:");
		sb.append(Epn);
		// show expected probability of 1,2,...n-1 collisions 
		sb.append("\n\tDnm:");
		sb.append(Dnm);
		sb.append("\n\tEpnDmn");
		// adjusted expectaion of modified output per char changed
		sb.append(adjustedPerChar);
		sb.append("\n\tmEpnDmn");
		// adjusted modified output for all changes.
		sb.append(adjustedAllChgs);
		
		log.info(sb.toString());
	}
	
	public int predictLD(int len, int n, int c, int m){
		// prob that compressing a neighborhood results in null
		double P0 = 1.0d-1.0d/c;
		// prob that originally compressed to null and modified is not null
		double Pb = P0/c;
		// probability that originally did not compress to null, and subsequently did
		double Pc = P0/c;
		// probability that originally not null, subsequently not null but values are different
		double Pd = ((double)a-1)/((double)a*c*c);
		// aggregate cases where there is a change to a neighborhood
		double Epn = (Pb+Pc+Pd); 
		// once for each change, and each change affects n neighborhoods 
		Epn = Epn * m * n; 

		// discount by the expected number of collisions in a neighborhood
		double frac = (double) m/len;
		//double Dnm = 0.0d; 
		//for(int i=1; i<n-1; i++){
		//	Dnm+=Math.pow(frac,i);
		//}
		double Dnm = frac * n;
		double adjustedAllChgs=Epn*(1-Dnm);
		return (int) adjustedAllChgs;
	}

	/**
	 * Test prediction terms add up to 1.0
	 * @param len
	 * @param n
	 * @param c
	 * @param m
	 * @return
	 */
	@Test
	public void testPredictions(){
		log.info("testPredicitons() starting");
		int c=100;
		int a=64;
		// prob that compressing a neighborhood results in null
		double P0 = 1.0d-1.0d/c;
		// probability both are null
		double Pa = P0*P0;
		// prob that originally compressed to null and modified is not null
		double Pb = P0/c;
		// probability that originally did not compress to null, and subsequently did
		double Pc = P0/c;
		// probability that originally not null, subsequently not null but values are different
		double Pd = ((double)a-1)/((double)a*c*c);
		// test both not null and equal
		double Pe = (1.0d)/((double)a*c*c);
		// aggregate cases where there is a change to a neighborhood
		double allProbs=Pa+Pb+Pc+Pd+Pe;
		log.info("Sum of probabilities Pa + Pb + Pc + Pd + Pe = " + allProbs);
		assert(allProbs==1.0d);
		log.info("testPredicitons() ending");
		
	}
	
	
	/**
	 * Note, it makes little sense to use a small neighborhood because the number of distinct values is quite small.
	 * card(alpha)^n for a random string, but language is nowhere near random, as most strings never appear.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testvals() throws Exception {
		int smallestN=8;
		int largestN=20;
		String fname = big;
		String longOne = readFile(fname);
		int n = 12;
		int c = 100;
		int mutFactor = 50;
		InputStringProcessor isp = new InputStringProcessor(longOne);
		int mutateChars = longOne.length() / mutFactor;
		StringCompressorPlain comp = new StringCompressorPlain();
		comp.setC(c);
		comp.setN(n);
		for (n = smallestN; n <= largestN; n++) {
			String mutated = isp.mutatedVersion(mutateChars); 
			String compressedOrig = comp.compress(longOne);
			String compressedMutate = comp.compress(mutated);
			StringDistance sd = new StringDistance();
			int ld = sd.LD(compressedOrig, compressedMutate);
			int predicted = predictLD(longOne.length(), n, c, isp.numMutated());
			int offBy = predicted - ld;
			double ratio = predicted / (double) ld;
			log.info("n:" + n + " c: " + c + " mut:" + isp.numMutated() + " LD:" + ld
					+ " predicted LD:" + predicted + " off by:" + offBy
					+ " ratio: " + ratio);
		}
	}

	int STDEV_ITER=200;
	int STDEV_N=12;
	int STDEV_C=100;
	int STDEV_MOD_FACTOR=50;

	@Test
	public void testCentralTendency() throws Exception {
		log.info("testCentralTendency() started.");
		String fname = big;
		String longOne = readFile(fname);
		int n = STDEV_N;
		int c = STDEV_C;
		int mutFactor = STDEV_MOD_FACTOR;
		int mutateChars = longOne.length() / mutFactor;
		centralTendency(longOne, n, c, mutateChars, STDEV_ITER);
		log.info("testCentralTendency() ended.");
	}

	/**
	 * Compute the measures of central tendency against a random string composed of the same character set
	 * as used in testCentralTendency().
	 *  
	 * @throws Exception
	 */
	@Test
	public void testCentralTendencyRnd() throws Exception {
		log.info("testCentralTendencyRnd() starting.");
		String fname = big;
		String longOne = readFile(fname);
		int n = STDEV_N;
		int c = STDEV_C;
		int mutFactor = STDEV_MOD_FACTOR;
		int mutateChars = longOne.length() / mutFactor;
		InputStringProcessor isp = new InputStringProcessor(longOne);
		String inStr=isp.generateRandomString();
		centralTendency(inStr, n, c, mutateChars, STDEV_ITER);
		log.info("testCentralTendencyRnd() ended.");
	}
	
	
	boolean VERBOSE=false;
	/**
	 * Obtain mean, standard deviation, etc. for a series of runs on the same string modified
	 * at the same number of randomly chosen positions each time.
	 * 
	 * @param inputString -- the unchanged string.
	 * @param n integer neighborhood size
	 * @param c integer compression ratio	
	 * @param m integer number of characters to modify
	 * @param iterations number of iterations to execute.
	 * @throws Exception
	 */
	private void centralTendency(String inputString, int n, int c, int m, int iterations) throws Exception{
		StringCompressorPlain comp = new StringCompressorPlain();
		comp.setC(c);
		comp.setN(n);
		String compressedOrig = comp.compress(inputString);
		StringDistance sd = new StringDistance();
		List<Integer>discrepancy=new ArrayList<Integer>();
		int ldTotal=0;
		for (int i=0; i<iterations; i++) {
			InputStringProcessor isp = new InputStringProcessor(inputString);
			String mutated = isp.mutatedVersion(m); 
			String compressedMutate = comp.compress(mutated);
			int ld = sd.LD(compressedOrig, compressedMutate);
			ldTotal+=ld;
			int predicted = predictLD(inputString.length(), n, c, isp.numMutated());
			int offBy = predicted - ld;
			discrepancy.add(offBy);
			double ratio = predicted / (double) ld;
			if(VERBOSE){
			log.info("n:" + n + " c: " + c + " mut:" + isp.numMutated() + " LD:" + ld
					+ " predicted LD:" + predicted + " off by:" + offBy
					+ " ratio: " + ratio);
			}
		}
		double ldAve = ldTotal/(double)iterations;
		Map<String,Number> map = InputStringProcessor.meanStdev(discrepancy);
		System.out.println("IN-STR:\t"+ inputString.length());
		System.out.println("LD-AVE:\t"+ ldAve);
		System.out.println("MEAN:\t"+ map.get("MEAN"));
		System.out.println("VAR:\t"+ map.get("VAR"));
		System.out.println("STDEV:\t"+ map.get("STDEV"));
		System.out.println("MIN:\t"+ map.get("MIN"));
		System.out.println("MAX:\t"+ map.get("MAX"));
	}

}