/**
 * 
 */
package com.levenshtein.leven.utility;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import org.apache.log4j.Logger;

/**
 * Convenience class for reading CSV files. 
 * TODO Stripped out of something else -- may need cleanup. 
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
	 * files with one word on a line, but it doesn't have to be. 
	 * 
	 * @param fileSpec If the file is not yet open, open it.
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
