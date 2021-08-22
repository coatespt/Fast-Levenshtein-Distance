package com.levenshtein;

import utilities.file.CSVLogWriter;
import utilities.mechanic.EntropyCalc;
import utilities.file.FileAndTimeUtility;
import com.levenshtein.parent.TestParent;
import org.junit.Test;

import java.io.File;
import java.util.*;

// Snips for extracting filenames in a certain size range and putting them in a file where 
// the test routine will fine them.
//
// find  /Volumes/Seagate\ Backup\ Plus\ Drive/guten-txt/ -name *.txt  > files.list
// find  /Volumes/Seagate\ Backup\ Plus\ Drive/guten-txt/ -name *.txt | awk '{print "\"" $0 "\""}' > allfilenames.txt
// cat allfilenames.txt | xargs wc > allfilenames.wc
// cat allfilenames.wc | awk '{if($1>10000 && $1<10100){print "\""$4" "$5" "$6" "$7"\""}}' > biggerthatn10k.txt
//
// New procedure to get files in a range
// cd to a directory below where the files are.
// export LOW=10000
// export HIGH=20000
// The following creates a list of the file names.
// find ./allfiles -name "*.txt" | awk '{print  $0 }' > allfiles.txt
// The following creates a stream of file word-counts.
// cat allfiles.txt | xargs wc > allfiles.wc
// Result is a list of relative path names of files in the length range
// Note how the filename is concatenated. It's the variables in the quotes, not the strings.
// cat allfiles.wc | awk -v low=$LOW -v high=$HIGH '{ if($3>low && $3<high){print $4}}' > file_"$LOW"_"$HIGH".txt

// TODO Figure out what these files are supposted to look like
//All32KSigs
// use on the output of testEntropyCompare
// cat junk.txt | awk 'BEGIN{} {print $27 " " $28 " " $29 " " $30}' | awk 'BEGIN{FS=":";} {for(i=1;i<=NF;i++){str=str ( $i " ")}; print str; str=""}' | awk 'BEGIN{}{d=$6-$8;d=d<0?d*-1:d; print $6 " " $8 " " d}' | awk '{sum1+=$1; sum2+=$2; sum3+=$3;}END{print sum1/NR " " sum2/NR " " sum3/NR}'
// cat 32kEntropy.txt | awk '{}{raw+=$3; fifty+=$5; hunert+=$7; onefity+=$9; two+=$11; twofity+=$13; three+=$15; threefity+=$17;}END{print raw/NR " " fifty/NR " " hunert/NR " " onefity/NR " " two/NR " " twofity/NR " " three/NR " " threefity/NR}'

	/**
	 * This can be run from JUnit, but it's not really unit tests.
	 * 
	 * Performance for LD Estimates plus some utility routines for creating big files of signatures.
	 *
	 * There are modified versions of 10 files in the Gutenberg directory. See below. 
	 * I took 10 of these and copied them three times to versions <filename>_mut_1.txt, <filename>._mut_2.txt, etc.
	 * Each of the _mut_x.txt versions has some mods--a few lines deleted, a block of lines deleted, etc.
	 * 
	 * The plain <filename> files are were used in testCreateSignatures() to generate a signatures.txt file.
	 * some of them were placed in testSigs.txt.
	 * 
	 * The other test routine testSigCompare() runs every signature in the testSigs.txt against every signature in the signatures.txt file.
	 * TODO Fix me! All tests in this file break because the test data is missing.
	 * @author pcoates
	 */
public class TestMassCompare extends TestParent {
		//static Logger log = Logger.getLogger(TestMassCompare.class);
		static int N=20;
		static int C=140;
		static Integer[] CVals = {50,100,150,200,250,300,350};

		// TODO This should all be in a properties file
		// The location of the bulk gutenberg data files.
		// There are about 18k of them.	It looks like the 8-bit files are separated out.
		// THis needs to be in .gitignore--too much to inline in the project!

		/** Bigger of the two filename lists. These are relative path names of all 18k books*/
		static String FilesFile="./data/allfiles.txt";

		/** Signatures corresponding to searchFiles */
		static String SigFile="./data/allfiles-sigs.txt";

		/** Smaller of two filename lists */
		static String TestFiles="./data/testfiles.txt";

		/** signatures corresponding to testFiles*/
		static String TestSigFile="./data/testfile-sigs.txt";
		
		static String EntropyOut = "./data/allfiles-entropy.txt";

// BEWARE this takes a couple of hours
		@Test
		public void testCreate18k() throws Exception {
			System.out.println("testCreateSignatures()");
			_testCreateSignatures(SigFile,FilesFile);
		}

		/**
		 * Create two files of signatures according to the contents of the 
		 *  TestFiles and FilesFile listings of input files.
		 *  This isn't really a test--it just prepares the data for the tests.
		 */
		@Test
		public void testCreate50Signatures() throws Exception {
			System.out.println("testCreateSignatures()");
			_testCreateSignatures(TestSigFile, TestFiles);
		}
		
		/**
		 * Computes the LD of a pair of signatures and the LD of the corresponding files and prints out some time and accuracy information.
		 * 
		 * For every entry in one file, computes against each entry in the other file.
		 * 
		 * The difference between the lengths of the two files 
		 * 
		 * The error rate is Math.abs(distUncompressed-expectedLD)/(fudge(Min(fileLen1,fileLen2)+Math.abs(fileLen1-fileLen2));
		 * 
		 * fudge() is a constant factor < 1.0 to account for the fact that the LD of two equal length random strings is shorter than
		 * their length. The difference in their lengths is added. 
		 * 
		 * @throws Exception
		 */
//		@Test
//		public void testSigCompare() throws Exception {
//			System.out.println("testSigCompare()");
//			int sig200Index=5;
//			List<String> targetList = FileAndTimeUtility.readListFromFile(TestSigFile);
//			List<String> testList = FileAndTimeUtility.readListFromFile(SigFile);
//			for(String targetLine : targetList){
//				StringDistance d = new StringDistance();
//				List<String> targFields = FileAndTimeUtility.getTokenListFromString(targetLine, '|', 16);
//				String targFile = targFields.get(0);
//				int c = Integer.parseInt(targFields.get(sig200Index-1));
//				long targSize = Long.parseLong(targFields.get(1));
//				String targetSig = targFields.get(sig200Index);
//				long targSigSize = targetSig.length();
//				String tFileContent=FileAndTimeUtility.getFileContents(targFile);
//				for(String line : testList){
//					List<String> testFields = FileAndTimeUtility.getTokenListFromString(line, '|', 16);
//					String searchFile = testFields.get(0);
//					String searchSig = testFields.get(sig200Index);
//					long searchSize = Long.parseLong(testFields.get(1));
//					long searchSigSize = searchSig.length();
//					String sFileContent=FileAndTimeUtility.getFileContents(searchFile);
//					Date start = new Date();
//					int distCompressed = d.LD(targetSig, searchSig); 
//					long elapsedLDCmp = FileAndTimeUtility.elapsedMS(start, new Date());
//					start = new Date();
//					int distUncompressed = d.LD(tFileContent, sFileContent); 
//					long elapsedLDUncmp = FileAndTimeUtility.elapsedMS(start, new Date());
//					long expectedLD = (long) fudge(distCompressed*c);
//					double error = Math.abs(distUncompressed-expectedLD)/(fudge(Math.min(targSize,searchSize))+Math.abs(targSize-searchSize));
//					String out = output(c,targFile,targSize,targSigSize,searchFile, searchSize,searchSigSize, 
//							distUncompressed, distCompressed, expectedLD, error, elapsedLDUncmp, elapsedLDCmp,"\tNO-ENTROPY-COMPUTED");
//					System.out.println(out);
//				}
//			}
//		}
		
		protected boolean fileExists(String fname){
			File f = new File(fname);
			return f.exists();
		}

//		@Test
		public void ttttestEntropy() throws Exception {
			System.out.println("testEntropy()");
			int bufLen=200;
			List<String> targetList = FileAndTimeUtility.readListFromFile(SigFile);
			long files=0;
			long chars=0;
			Map<Integer,Long>sigCounts=new HashMap<Integer,Long>();
			Map<Integer,Long>sigMin=new HashMap<Integer,Long>();
			Map<Integer,Long>sigMax=new HashMap<Integer,Long>();
			sigCounts.put(0,0L);
			sigMin.put(0,Long.MAX_VALUE);
			sigMax.put(0,Long.MIN_VALUE);
			CSVLogWriter csvlw = new CSVLogWriter(EntropyOut);
			for(String targetLine : targetList){
				StringBuffer sb = new StringBuffer(bufLen);
				List<String> targFields = FileAndTimeUtility.getTokenListFromString(targetLine, '|', 16);
				// fname
				String targFile = targFields.get(0);
				sb.append(justFileName(targFile));
				sb.append("\t");
				// filesize
				long targSize = Long.parseLong(targFields.get(1));
				if(targSize<10000){
					continue;
				}
				sb.append(targSize);
				sb.append("\t");
				// file contents
				if(!fileExists(targFile)){
						System.out.println("file not found! " + targFile);
						continue;
				};
				String tFileContent=FileAndTimeUtility.getFileContents(targFile);
				// entropy
				chars+=tFileContent.length();
				files++;
				double fileEnt=computeEntropy(tFileContent);
				sb.append(String.format("%.4f",fileEnt));
				sb.append("\t");
				sigCounts.put(0, sigCounts.get(0)+tFileContent.length());
				if(sigMin.get(0)>tFileContent.length()){
					sigMin.put(0,(long) tFileContent.length());
				}
				if(sigMax.get(0)<tFileContent.length()){
					sigMax.put(0,(long) tFileContent.length());
				}
				sigCounts.put(0, sigCounts.get(0)+tFileContent.length());
				sigCounts.put(0, sigCounts.get(0)+tFileContent.length());
				for(int j=0; j<CVals.length; j++){
					String cStr = targFields.get(2+j*2);
					int c = Integer.parseInt(cStr);
					String sig = targFields.get(2+j*2+1);
					if(sig==null){
						System.out.println("signature null for file " + targFile);
						continue;
					}
					double sigEntropy=computeEntropy(sig);
					if(!sigCounts.containsKey(c)){
						sigCounts.put(c, 0L);
						sigMin.put(c, Long.MAX_VALUE);
						sigMax.put(c, Long.MIN_VALUE);
					}
					sigCounts.put(c, sigCounts.get(c)+sig.length());
					if(sigMin.get(c)>sig.length()){
						sigMin.put(c,(long) sig.length());
					}
					if(sigMax.get(c)<sig.length()){
						sigMax.put(c,(long) sig.length());
					}
					sb.append(c);
					sb.append("\t");
					sb.append(String.format("%.4f",sigEntropy));
					sb.append("\t");
				}
				System.out.println(sb.toString());
				csvlw.writeNL(sb.toString());
			}
			csvlw.close();
			System.out.println("processed:" + files);
			StringBuffer sb = new StringBuffer();
			sb.append("files:");
			sb.append(files);
			sb.append("\tchars read:");
			sb.append(chars);
			sb.append("\tC,min,max,average");
			sb.append(forSig(files,sigCounts,sigMin,sigMax));
			System.out.println(sb.toString());
		}
		
		
		private String forSig(long files, Map<Integer,Long> ct, Map<Integer,Long> min, Map<Integer,Long> max){
			StringBuffer sb = new StringBuffer();
			Set<Integer> keys = ct.keySet();
			List<Integer> list = new ArrayList<Integer>();
			list.addAll(keys);
			Collections.sort(list);
			sb.append("\ttotal files:");
			sb.append(files);
			for(Integer i : list){
				sb.append("\n\t\tC: ");
				sb.append(i);
				sb.append("\t\ttotal: ");
				sb.append(ct.get(i));
				sb.append("\t\taverage: ");
				sb.append(ct.get(i)/files);
				sb.append("\t\tmin: ");
				sb.append(min.get(i));
				sb.append("\t\tmax: ");
				sb.append(max.get(i));
			}
			return sb.toString();
		}
		

		

		private double computeEntropy(String f2 ){
			double f2H=EntropyCalc.calculateShannonEntropy(f2);
			return f2H;
		}
		
		
		/**
		 * Run the EntropyCalc routine against two strings and format the output for display.
		 * This is intended to show how the actual entropy of the file compares to the entropy of the signature.
		 * @param label1
		 * @param f1
		 * @param label2
		 * @param f2
		 * @return
		 */
		private String computeEntropy(String label1, String f1, String label2, String f2 ){
			StringBuffer sb = new StringBuffer();
			double f1H=EntropyCalc.calculateShannonEntropy(f1);
			double f2H=EntropyCalc.calculateShannonEntropy(f2);
			sb.append("\t");
			sb.append(label1);
			sb.append(String.format("%4f", f1H));
			sb.append("\t");
			sb.append(label2);
			sb.append(String.format("%4f", f2H));
			return sb.toString();
		}
		
		
		/**
		 * Format the output of the cross compare routine.
		 * @param c
		 * @param targFile
		 * @param targSize
		 * @param targSigSize
		 * @param searchFile
		 * @param searchSize
		 * @param searchSigSize
		 * @param fDist
		 * @param sigDist
		 * @param expectedLd
		 * @param error
		 * @param elapsedLDUncmp
		 * @param elapsedLDCmp
		 * @return
		 */
		private String output(int c, String targFile, long targSize, long targSigSize, String searchFile, long searchSize, long searchSigSize, long fDist, long sigDist, 
				long expectedLd, double error, long elapsedLDUncmp, long elapsedLDCmp, String entropy){
			StringBuffer sb = new StringBuffer();
			sb.append("C:");
			sb.append(c);
			sb.append("\t");
			sb.append(" f1: ");
			sb.append(justFileName(targFile));
			sb.append("\t");
			sb.append(" tSz: ");
			sb.append(targSize);
			sb.append("\t");
			sb.append(" tSgSzl: ");
			sb.append(targSigSize);
			sb.append("\t");
			sb.append(" f2: ");
			sb.append(justFileName(searchFile));
			sb.append("\t");
			sb.append(" f2Sz: ");
			sb.append(searchSize);
			sb.append("\t");
			sb.append(" f2SigSz: ");
			sb.append(searchSigSize);
			sb.append("\t");
			sb.append(" sDist: ");
			sb.append(sigDist);
			sb.append("\t");
			sb.append(" LD/estimate: ");
			sb.append(fDist);
			sb.append("/");
			sb.append(expectedLd);
			sb.append("\t");
			sb.append(" error: ");
			sb.append(String.format("%.4f", error));
			sb.append("\t");
			sb.append(" ms for LD:");
			sb.append(elapsedLDUncmp);
			sb.append("\t");
			sb.append(" ms for est:");
			sb.append(elapsedLDCmp);
			sb.append("\t");
			sb.append(entropy);
			return sb.toString();
		}
		
		
		/**
		 * Read a list of filename and generate one or more signatures into 
		 * a pipe-delimited file. Generate a signature for each value 
		 * in CVals array.
		 *<p> 
		 * Output statistics include read time for every file for every signature, so it's not a good 
		 * measure if you have an network-mounted disk, access over WiFi, etc.
		 *<p> 
		 * Format is {filename| file-size| C1| sig-1| ... |Cn| sigN}
		 *<p> 
		 * 
		 * @throws Exception
		 */
		public void _testCreateSignatures(String sigFileName, String inFileList) throws Exception {
			clearCompressStats();
			System.out.println("_testCreateSignatures()");
			System.out.println("\tDropping signature file:" + sigFileName);
			dropFile(sigFileName);
			System.out.println("_testCreateSignatures()");
			CSVLogWriter csvLog = new CSVLogWriter(sigFileName);
			List<String> list = FileAndTimeUtility.readListFromFile(inFileList);
			System.out.println("Got " + list.size() + " filenames.");
			Date start = new Date();
			int sigsComputed=0;
			int ctProcessed=0;
			int exceptionCt = 0;
			TestParent.clearCompressStats();
			for(String s : list){
				ctProcessed++;
				StringBuffer sb = new StringBuffer();
				String str = s.substring(0,s.length());
				long len = fileLen(str);
				sb.append(str);
				sb.append("|");
				sb.append(len);
				sb.append("|");
				int ct=0;
				for(Integer c : CVals){
					try{
						sigsComputed++;
						System.out.println("\twriting "+ctProcessed+"'th sigs for file: " + str + " C=" + c + " len:" + len);
						String sig = compressToC(c,N,str);
						sb.append(c);
						sb.append("|");
						sb.append(sig);
						if(ct++<CVals.length-1){
							sb.append("|");
						}
					}
					catch(Exception x){
						System.out.println("Exception thrown for file:" + s + " Ex:" + x.getMessage());
						x.printStackTrace();
					}
				}
				csvLog.writeNL(sb.toString());
			}
			Date end = new Date();
			long elapsedSecs = FileAndTimeUtility.elapsedSec(start, end);
			long charsPerSec = totalCharsRead/elapsedSecs;
			StringBuffer sb = new StringBuffer();
			sb.append("Run statistics");
			sb.append("\n\tfiles:");
			sb.append(ctProcessed);
			sb.append("\n\tcharsRead:");
			sb.append(totalCharsRead);
			sb.append("\n\tsigs/file:");
			sb.append(CVals.length);
			sb.append("\n\tsigs produced:");
			sb.append(sigsComputed);
			sb.append("\n\tsig volume:");
			sb.append(totalSigsProduced);
			sb.append("\n\telapsed seconds:");
			sb.append(elapsedSecs);
			sb.append("\n\tsigGen rate:");
			sb.append(charsPerSec);
			sb.append("\n\tfailures:");
			sb.append(exceptionCt);
			sb.append("\n\tExpected v. actual compression Rates");
			sb.append("\n\t\tC   \tActual Compression");
			for(Integer c : CVals){
				sb.append("\n\t\t");
				sb.append(c);
				sb.append("\t");
				sb.append(String.format("%.4f",compRateForSigs(c)));
			}
			System.out.println(sb.toString());
			csvLog.close();
		}

}
