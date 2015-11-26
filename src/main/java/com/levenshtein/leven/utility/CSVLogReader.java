/**
 * 
 */
package com.levenshtein.leven.utility;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import org.apache.log4j.Logger;

/**
 * Various ways to read in a CSV file such as Map<String,Integer>,
 * Map<Integer,String> etc.
 * 
 * @author peter
 * 
 */
public class CSVLogReader {
	static Logger log = Logger.getLogger(CSVLogReader.class);
	private String filename = null;
	private BufferedReader reader = null;
	private long totalWords = 0;
	private FileReader fr;

	public CSVLogReader() {
	}

	/**
	 * Read in a map of integers to strings. Format is one comma-separated pair
	 * of integer and string with lines separated by a "\n". It is assumed that
	 * the input data is valid. It fails if the format is violated and it does
	 * not fail if the same key is used twice. The second simply overwrites the
	 * first.
	 * 
	 * @param fileSpec
	 * @return
	 * @throws Exception
	 */


	public long getWordsRead() {
		return totalWords;
	}

	public void close() throws Exception {
		if (reader != null) {
			reader.close();
		}
		if (fr != null) {
			fr.close();
		}
	}

	public CSVLogReader(String file) throws FileNotFoundException {
		filename = file;
		fr = new FileReader(filename);
		reader = new BufferedReader(fr);
	}

	/**
	 * Reads a file and returns one line at a time. This is intended to be for
	 * files with one word on a line, but it doesn't have to be. Note, this
	 * closes the file when it's emptied.
	 * 
	 * @param fileSpec
	 *            If the file is not yet open, open it.
	 * @return The line that was read or null, if the file is empty.
	 * @throws Exception
	 */
	public String readString() throws Exception {
		String in = null;
		if ((in = reader.readLine()) != null) {
			totalWords++;
			in = in.trim();
			return in;
		} else {
			reader.close();
			return null;
		}
	}

}
