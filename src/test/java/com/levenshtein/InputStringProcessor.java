package com.levenshtein;

import com.levenshtein.leven.utility.SWOR;
import com.levenshtein.leven.utility.exception.NoNextValueException;
import com.levenshtein.parent.TestParent;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Some utilities for testing Estimated LD, including mutating a string, etc.
 * This file contains no tests--just untilities.
 * @author pcoates
 *
 */
public class InputStringProcessor extends TestParent {
	private String input = null;
	private Character [] charSet = null;
	private List<String> files;
		static Logger log = Logger.getLogger(InputStringProcessor.class);

	// The files below contain 11,023,266 characters of clean ASCII text from Gutenberg. All the header and trailer
	// have been deleted. 213,156 lines total. 

	//String cleanTextLoc="/Users/pcoates/Workspaces/personal-wkspc/data-gutenberg";
	String cleanTextLoc="./data/allfiles/";
	String [] cleanFiles1={
	"tcosb10.txt",
	"thx0710.txt",
	"tl41510.txt",
	"tmrgs10.txt",
	"tsomh10.txt",
	"tvsrv10.txt",
	"utrkj10.txt",
	"vikrv10.txt",
	"wldsp10.txt",
	"teddy10.txt",
	"thx0810.txt",
	"tmbrn10.txt",
	"tmsls10.txt",
	"tsotm10.txt"
	};
	 String [] cleanFiles2={
	"txohc10.txt",
	"uusry11.txt",
	"vlttr10.txt",
	"wtell10.txt",
	"theyi10.txt",
	"thx2010.txt",
	"tmgot10.txt",
	"tpnrs10.txt",
	"ttalk10.txt",
	"ungst10.txt",
	"vgmld10.txt",
	"wassq10.txt",
	};	

	private List<String> blocks1 = new ArrayList<String>();
	private List<String> blocks2= new ArrayList<String>();

	/**
	 * Read all the files and create a set of blocks of the given size.
	 * @throws Exception 
	 */
	public void createBlocks(int blockSize) throws Exception{
		log.info("createBlocks() runnning");
		StringBuffer sb1 = new StringBuffer(12000000);
		StringBuffer sb2 = new StringBuffer(12000000);
		for(String filename : cleanFiles1){
			String file=readFile(cleanTextLoc + "/" + filename);
			sb1.append(file);
		}
		for(String filename : cleanFiles2){
			String file=readFile(cleanTextLoc + "/" + filename);
			sb2.append(file);
		}
		int len = Math.min(sb1.length(), sb2.length());
		int numPairs = len/blockSize;


		log.info("blocks:" + numPairs + " one:" + sb1.length() + " two:" + sb2.length());
		log.info("createBlocks() completed");
	}
	
	
	

	/**
	 * Must be created with a non-null input string.
	 * @param input
	 */
	public InputStringProcessor(String input) {
		super();
		this.input = input;
		charSet=allChars(input);
	}

	/**
	 * Get a different version with about m characters mutated.
	 * @param m
	 * @return
	 */
	public String mutatedVersion(int m){
		return mutate(input,charSet,m,null);
	}
	
	/**
	 * Use the same random number sequence each time 
	 * @param m
	 * @return
	 */
	public String mutatedVersionRepeat(int m){
		return mutate(input,charSet,m, 7891011);
	}
	

	/**
	 * Return the number mutated for the last generated version.
	 * @return
	 */
	public int numMutated(){
		return mutatedCt;
	}
	

	/**
	 * return true in proportion to argument. 
	 * @param prob double 0<prob<1
	 * @return boolean t/f with stated probability
	 */
	private boolean trueOrFalse(double prob){
		if(rand.nextDouble() < prob){
			return true;
		}
		return false;
	}	

	private Random rand = new Random();

	private void initRand(int n){
		rand=new Random(n);
	}

	int randPos(int max){
		int r = Math.abs(rand.nextInt());
		return r%max;
	}	
	
	private int mutatedCt=0;
	
	
	public String generateRandomString(){
		return generateRndString(null);
	}

	public String generateRandomString(int s){
		return generateRndString(s);
	}
	
	private String generateRndString(Integer sz){
		int size;
		if(sz==null){
			size=input.length();
		}
		else{
			size=sz;
		}
		StringBuffer sb = new StringBuffer(size); 
		for(int i=0; i<size; i++){
			Character c = charSet[randPos(charSet.length)];
			sb.append(c);
		}
		return sb.toString();
	}

	private String mutate(String input, Character [] chars, int m, Integer seed){
		SWOR swor=null;
		if(seed!=null){
			swor = new SWOR(input.length(),seed);
		}
		else{
			swor = new SWOR(input.length());
		}
		List<Integer> mutate = new ArrayList<Integer>();
		for(int i=0; i<m; i++){
			try {
				mutate.add(swor.next());
			} catch (NoNextValueException e) {
				System.err.println("What?! SWOR failed!");
				e.printStackTrace();
			}
		}
		Collections.sort(mutate);
		Character [] inChars = allChars(input);
		StringBuffer sb = new StringBuffer();
		mutatedCt=0;
		double p=(double)m/input.length();
		int mutPos=0;
		for(int i=0; i<input.length();i++){	
			Character c = input.charAt(i);
			if(mutPos< mutate.size() && i==mutate.get(mutPos)){
				Character sub = inChars[randPos(inChars.length)];
				while(sub.equals(c)){
					sub = inChars[randPos(inChars.length)];
				}
				sb.append(sub);
				mutPos++;
				mutatedCt++;
			}
			else {
				sb.append(c);
			}
		}
		//log.info("Mutated:" + mutatedCt);
		return sb.toString();
	}
	

	private Character [] allChars(String input){
		Set<Character> s = new HashSet<Character>();
		for(int i=0; i<input.length();i++){
			Character c = input.charAt(i);
			s.add(c);
		}
		Character [] c = new Character[1];
		c = s.toArray(c);
		return c;
	}

	public static Map<String, Number> meanStdev(List<Integer> values){
		int min=Integer.MAX_VALUE;
		int max=Integer.MIN_VALUE;
		Map<String,Number> vals = new HashMap<String,Number>();
		int total = 0;
		double ssd = 0;
		for(Integer v : values){
			min=Math.min(min, v);
			max=Math.max(max, v);
			total+=v;
		}
		double mean = (double) total / values.size();
		for(Integer v : values){
			double diff = mean-v;
			double sqDiff = diff * diff;
			ssd+=sqDiff;
		}
		double var=ssd/values.size();
		double sd=Math.sqrt(var);
		vals.put("MEAN",mean);
		vals.put("VAR",var);
		vals.put("STDEV",sd);
		vals.put("COUNT",sd);
		vals.put("MIN",min);
		vals.put("MAX",max);
		return vals;
	}
	
}
