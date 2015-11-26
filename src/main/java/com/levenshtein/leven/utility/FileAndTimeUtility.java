package com.levenshtein.leven.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import java.util.StringTokenizer;

/**
 * Utility methods borrowed form some other project---many are unused.
 * TODO: Clear out all the dead code.
 * @author pcoates
 *
 */
public class FileAndTimeUtility {

	public static char BACKSLASH = '\\';
	public static String DATE_STR_FORMAT_FOR_LOG = "dd-MMM-yyyy_HH:mm:ss";
	public static String DATE_STR_FORMAT_FOR_FILENAME = "dd-MMM-yyyy_HHmmss";
	public static String DATE_STR_FORMAT_SHORT = "dd-MMM-yyyy";


	/**
	 * Compute the elapsed milliseconds for an interval
	 * 
	 * @param start
	 *            Start of the interval.
	 * @param end
	 *            End of the interval.
	 * @return
	 */
	public static long elapsedMS(Date start, Date end) {
		long elapsed = end.getTime() - start.getTime();
		return elapsed;
	}

	/**
	 * Compute the elapsed seconds for an interval
	 * 
	 * @param start
	 *            A start time
	 * @param end
	 *            And end time
	 * @return
	 */
	public static long elapsedSec(Date start, Date end) {
		return elapsedMS(start, end) / 1000;
	}

	/**
	 * TODO: move this to to Utility Compute the rate per second for something.
	 * 
	 * @param something
	 *            A count of anything
	 * @param start
	 *            A start time for the interval
	 * @param end
	 *            An end time for the interval.
	 * @return
	 */
	public static Double rateSec(long something, Date start, Date end) {
		return ((double) something / (double) elapsedMS(start, end)) * 1000.0d;
	}

	/**
	 * This method exists because the tokenizer can't deal with either escaped
	 * strings or with empty fields. Takes a csv string, the delimiter, in the
	 * case of csv, a comma, and the expected number of fields.
	 * 
	 * The expected number of fields is necessary because there is no difference
	 * between a string with n fields, the last one of which is blank, and a set
	 * of n-1 fields.
	 * 
	 * 
	 * @param csv
	 *            The csv string to parse into a list of strings, some of which
	 *            may be empty.
	 * @param delim
	 *            The delimiter, one character.
	 * @param expected
	 *            The expected number of fields in the output.
	 * @return
	 * @throws Exception
	 */
	public static List<String> getTokenListFromString(String csv, char delim,
			int expected) throws Exception {
		List<String> list = new ArrayList<String>();
		int len = csv.length();
		int position = 0;

		while (position < len) {
			StringBuffer sb = new StringBuffer();
			while (position < len) {
				char c = csv.charAt(position++);
				if (c == BACKSLASH) {
					sb.append(c);
					if (position < len) {
						c = csv.charAt(position++);
						sb.append(c);
					} else {
						break;
					}
				} else if (c == delim) {
					break;
				} else {
					sb.append(c);
				}
			}
			list.add(sb.toString());
		}
		if (list.size() == expected - 1) {
			list.add(null);
		}
		return list;
	}

	/**
	 * Escape an field so it can have quotes and commas. The method
	 * getTokenListFromString(String csv, char delim, int expected) takes care
	 * of the escaping when the string is parsed into words. 
	 * 
	 * TODO Create a <i>String unescape(String s)</i> method that is the inverse of this one.
	 * 
	 * @param str
	 * @return
	 */
	public static String escapeAField(String str) {
		int position = 0;
		int len = str.length();
		StringBuffer sb = new StringBuffer(len + 10);
		while (position < len) {
			while (position < len) {
				char c = str.charAt(position++);
				if (c == '\'') {
					sb.append(BACKSLASH);
					sb.append(c);
				} else if (c == ',') {
					sb.append(BACKSLASH);
					sb.append(c);
				} else {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	static List<String> list = new ArrayList<String>();

	private static void getTokenFromST(List<String> list, StringTokenizer st,
			String delim, boolean consumeDelim) throws Exception {
		if (!st.hasMoreElements()) {
			return;
		}
		String tok = st.nextToken();
		int numDelims = hasDelims(tok, delim);
		if (numDelims > 0) {
			numDelims -= (consumeDelim ? 1 : 0);
			for (int i = 0; i < numDelims; i++) {
				list.add(null);
			}
		} else {
			list.add(tok);
		}
		getTokenFromST(list, st, delim, true);
	}

	private static int hasDelims(String tok, String delim) {
		if (tok == null) {
			return 0;
		}
		if (tok.indexOf(delim) < 0) {
			return 0;
		}
		return tok.length();
	}

	/**
	 * Compute the entire pathname based on the configured path and the file
	 * name. This fixes whether the path terminates with "/" or not, whether it
	 * is implicitly in "." etc.
	 */
	public static String fullPathnameForFilename(String file, String path,
			Logger log) {
		if (path != null) {
			path = path.trim();
		} else {
			path = ".";
		}
		if (path.charAt(path.length() - 1) == '/') {
			path = path.substring(0, path.length() - 2);
		}
		String filename = path + "/" + file.trim();
		if (log != null) {
			log.info("\n\tCreated pathname:" + filename);
		}
		return filename;
	}

//
//	/**
//	 * Get the filenames in a directory that have the given base.
//	 * 
//	 * @param path
//	 * @param filebase
//	 * @param fileSuffix
//	 * @param log
//	 * @return
//	 * @throws Exception
//	 */
//	public static List<String> getFilesInDirectory(String path,
//			String filebase, String fileSuffix, Logger log) throws Exception {
//		List<String> strings = new ArrayList<String>();
//		File dir = new File(path);
//
//		String[] children = dir.list();
//		if (children == null) {
//			String err = "There seems to be either no such directory as:"
//					+ path + " or no files with base:" + filebase
//					+ " and suffix:" + fileSuffix;
//			if (log != null) {
//				log.error(err);
//			}
//			throw new Exception(err);
//		} else {
//			for (int i = 0; i < children.length; i++) {
//				String f = children[i];
//				if (matchFileName(f, filebase, fileSuffix, log)) {
//					strings.add(f);
//					if (log != null) {
//						log.info("getFileInDirectory() found matching file:"
//								+ f + " in directory:" + path);
//					}
//				} else {
//					if (log != null) {
//						log.info("getFileInDirectory found NON-matching file:"
//								+ f + " in directory:" + path);
//					}
//				}
//			}
//		}
//		return strings;
//	}

	/**
	 * Get all file names form the given directory that match the path and file
	 * base.
	 * 
	 * @param path
	 * @param filebase
	 * @param log
	 * @return
	 * @throws Exception
	 */
	public static List<String> getFilesInDirectory(String path,
			String filebase, Logger log) throws Exception {
		List<String> strings = new ArrayList<String>();
		File dir = new File(path);

		String[] children = dir.list();
		if (children == null) {
			String err = "There seems to be either no such directory as:"
					+ path + " or no files with base:" + filebase;
			if (log != null) {
				log.error(err);
			}
			throw new Exception(err);
		} else {
			for (int i = 0; i < children.length; i++) {
				String f = children[i];
				if (matchFileName(f, filebase, log)) {
					strings.add(f);
					if (log != null) {
						log.debug("FOUND MATCHING FILE:" + f + " in directory:"
								+ path);
					}
				}
			}
		}
		return strings;
	}

//	/**
//	 * 
//	 * @param f
//	 * @return
//	 */
//	public static boolean matchFileName(String f, String filebase,
//			String fileSuffix, Logger log) {
//
//		Pattern pattern = Pattern.compile(filebase);
//		Matcher matcher = pattern.matcher(f);
//
//		if (!matcher.find()) {
//			return false;
//		}
//
//		String trailing = "." + fileSuffix;
//		pattern = Pattern.compile(trailing);
//		matcher = pattern.matcher(f);
//
//		if (!matcher.find()) {
//			return false;
//		}
//
//		int start = matcher.start();
//		int end = start + trailing.length();
//		int spos = f.length();
//		if (spos != end) {
//			if (log != null) {
//				log.debug("Data file:" + f + " does not seem to end with:"
//						+ trailing);
//			}
//			return false;
//		}
//		return true;
//	}

	public static boolean matchFileName(String f, String filebase, Logger log) {

		Pattern pattern = Pattern.compile(filebase);
		Matcher matcher = pattern.matcher(f);

		if (!matcher.find()) {
			return false;
		}
		return true;
	}

	/**
	 * Take a path and a filename and return a valid pathname with extra slashes
	 * accounted for, etc.
	 * 
	 * @param p
	 * @param file
	 * @return
	 */
	public static String fullPathnameForFilename(String p, String file) {
		if (p != null) {
			p = p.trim();
		} else {
			p = ".";
		}
		if (p.charAt(p.length() - 1) == '/') {
			p = p.substring(0, p.length() - 2);
		}
		String fname = p + "/" + file.trim();
		return fname;
	}


	/**
	 * Format the date into a string suitable for logging.
	 * 
	 * @param date
	 * @return
	 */
	public static String getDateString(Date date) {
		DateFormat formatter = new SimpleDateFormat(DATE_STR_FORMAT_FOR_LOG);
		String dateString = formatter.format(date);
		return dateString;
	}

	public static String getDateStringForFile(Date date) {
		DateFormat formatter = new SimpleDateFormat(
				DATE_STR_FORMAT_FOR_FILENAME);
		String dateString = formatter.format(date);
		return dateString;
	}

	/**
	 * Invert getDateStringForFile
	 * 
	 * @throws ParseException
	 */
	public static Date getDateForFormattedString(String s)
			throws ParseException {
		DateFormat formatter = new SimpleDateFormat(
				DATE_STR_FORMAT_FOR_FILENAME);
		Date d = formatter.parse(s);
		return d;
	}

	/**
	 * Get a date for a very simple date format:dd-MMM-yyyy
	 * 
	 * @param s
	 * @return
	 * @throws ParseException
	 */
	public static Date getDateForSimpleFormattedString(String s)
			throws ParseException {
		DateFormat formatter = new SimpleDateFormat(DATE_STR_FORMAT_SHORT);
		Date d = formatter.parse(s);
		return d;
	}

	public static String getFileContents(String filename)
			throws Exception {
		File f = new File(filename);
		long len = f.length();
		StringBuffer sb = new StringBuffer((int)(len*1.1));
		FileReader fr = new FileReader(f);
		BufferedReader reader = new BufferedReader(fr);
		String in;
		while ((in = reader.readLine()) != null) {
			if (in != null) {
				sb.append(in);
			}
		}
		reader.close();
		if (fr != null) {
			fr.close();
		}
		return sb.toString();
	}

	/**
	 * for a pair of files, return the first line of the one that contains the flag string.
	 * This is used for diagnostics on test files.
	 * @param f1
	 * @param f2
	 * @param flag
	 * @return
	 * @throws Exception
	 */
	public static String getFirstLineFlagged(String f1, String f2, String flag)
			throws Exception {
		String retVal="";
		File f = new File(f1);
		FileReader fr = new FileReader(f);
		BufferedReader reader = new BufferedReader(fr);
		String in;
		if ((in = reader.readLine()) == null) {
			throw new Exception("file " + f1 + " does not exist.");
		}
		reader.close();
		if (fr != null) {
			fr.close();
		}
		if(in.indexOf(flag)>=0){
			int index = in.indexOf(flag);
			String ret = in.substring(index + flag.length());
			retVal=retVal+ret;
		}
		else{
			retVal=retVal+"-- no comment --";
		}
		f = new File(f2);
		fr = new FileReader(f);
		reader = new BufferedReader(fr);
		if ((in = reader.readLine()) == null) {
			throw new Exception("file " + f1 + " does not exist.");
		}
		reader.close();
		if (fr != null) {
			fr.close();
		}
		retVal=retVal+" | ";
		if(in.indexOf(flag)>=0){
			int index = in.indexOf(flag);
			String ret = in.substring(index + flag.length());
			retVal=retVal+ret;
		}
		else{
			retVal=retVal+"-- no comment --";
		}
		return retVal;
	}



	public static List<String> readListFromFile(String filename)
			throws Exception {
		List<String> list = new ArrayList<String>();
		File f = new File(filename);
		FileReader fr = new FileReader(f);
		BufferedReader reader = new BufferedReader(fr);
		String in;
		while ((in = reader.readLine()) != null) {
			if (in != null) {
				in = in.trim();
				list.add(in);
			}
		}
		reader.close();
		if (fr != null) {
			fr.close();
		}
		return list;
	}


}
