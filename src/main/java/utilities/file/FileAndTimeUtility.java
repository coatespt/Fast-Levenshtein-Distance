package utilities.file;

import org.apache.log4j.Logger;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

public class FileAndTimeUtility {

	public static char BACKSLASH = '\\';
	public static String DATE_STR_FORMAT_FOR_LOG = "dd-MMM-yyyy_HH:mm:ss";
	public static String DATE_STR_FORMAT_FOR_FILENAME = "dd-MMM-yyyy_HHmmss";
	public static String DATE_STR_FORMAT_SHORT = "dd-MMM-yyyy";

	/**
	 * Compress a file and rename it, or fail and throw an exception. Renaming
	 * is useful because we recognize files types by their leading substrings.
	 * 
	 * @param oldName
	 * @param newName
	 * @param log
	 * @throws Exception
	 */
	public static void compressAndRename(String oldName, String newName,
			Logger log) throws Exception {
		rename(oldName, newName, log);
		compress(newName, log);
	}

	/**
	 * Rename a file or fail and throw an exception.
	 * 
	 * @param oldName
	 *            File to rename
	 * @param newName
	 *            New name
	 * @param log
	 *            An optional logger--this may be null.
	 * @throws Exception
	 */
	public static void rename(String oldName, String newName, Logger log)
			throws Exception {
		System.out.println("Original file name:" + newName
				+ " no longer exists.");
		// rename the tmp file to the original name
		File fnew = new File(newName);
		File ftmp = new File(oldName);
		boolean success = ftmp.renameTo(fnew);
		if (!success) {
			String err = "\n\tFailed to rename file:" + oldName + " to:"
					+ newName;
			if (log != null) {
				log.error(err);
			}
			throw new Exception(err);
		} else {
			if (log != null) {
				log.error("\n\tRenamed file:" + oldName + " to:" + newName);
			}
		}
	}

	/**
	 * Comress the given file or throw an exception
	 * 
	 * @param filename
	 *            Full pathname of the file
	 * @param log
	 *            Optional logger--this can be null.
	 * @throws Exception
	 *             Thrown if the operation fails
	 */
	public static void compress(String filename, Logger log) throws Exception {
		try {
			// Create the GZIP output stream
			String outFilename = filename + ".gz"; // make the output file name
													// simply with the gz
													// extension
			GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(
					outFilename));

			// Open the input file
			FileInputStream in = new FileInputStream(filename);

			// Transfer bytes from the input file to the GZIP output stream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();

			// Complete the GZIP file
			out.finish();
			out.close();
			// now delete the original file
			File of = new File(filename);
			of.delete(); // this is the expected gzip result to delete with
							// compression
		} catch (IOException e) {
			String err = "Compression failed for file:" + filename;
			if (log != null) {
				log.error(err);
			}
			throw new Exception(err);
		}
	}

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

	public static List<String> getTokenListFromStringX(String csv,
			String delim, int expected) throws Exception {
		list.clear();
		StringTokenizer st = new StringTokenizer(csv, delim, true);
		getTokenFromST(list, st, delim, false);
		if (list.size() == expected - 1) {
			list.add(null);
		}
		return list;
	}

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

	/**
	 * Gets one of the filenames of a file in a directory. Presumably you are
	 * going to remove it when you have processed it or you will keep getting
	 * the same one over and over.
	 * 
	 * @param path
	 * @param filebase
	 * @param filesuffix
	 * @param log
	 * @return
	 * @throws Exception
	 */
	public static String getAFileFromDirectory(String path, String filebase,
			String filesuffix, Logger log) throws Exception {
		List<String> list = getFilesInDirectory(path, filebase, filesuffix, log);
		sortInReverse(list);
		if (list.size() > 0) {
			String fullname = FileAndTimeUtility.fullPathnameForFilename(path,
					list.get(0));
			return fullname;
		}
		return null;
	}

	/**
	 * Get the filenames in a directory that have the given base.
	 * 
	 * @param path
	 * @param filebase
	 * @param fileSuffix
	 * @param log
	 * @return
	 * @throws Exception
	 */
	public static List<String> getFilesInDirectory(String path,
			String filebase, String fileSuffix, Logger log) throws Exception {
		List<String> strings = new ArrayList<String>();
		File dir = new File(path);

		String[] children = dir.list();
		if (children == null) {
			String err = "There seems to be either no such directory as:"
					+ path + " or no files with base:" + filebase
					+ " and suffix:" + fileSuffix;
			if (log != null) {
				log.error(err);
			}
			throw new Exception(err);
		} else {
			for (int i = 0; i < children.length; i++) {
				String f = children[i];
				if (matchFileName(f, filebase, fileSuffix, log)) {
					strings.add(f);
					if (log != null) {
						log.info("getFileInDirectory() found matching file:"
								+ f + " in directory:" + path);
					}
				} else {
					if (log != null) {
						log.info("getFileInDirectory found NON-matching file:"
								+ f + " in directory:" + path);
					}
				}
			}
		}
		return strings;
	}

	/**
	 * Append a line to a file.
	 * 
	 * @param processedListFileName
	 * @param filename
	 * @throws Exception
	 */
	public static void appendToFile(String processedListFileName,
			String filename) throws Exception {
		BufferedWriter out = new BufferedWriter(new FileWriter(
				processedListFileName, true));
		out.write(filename + "\n");
		out.close();
	}

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

	public static void sortInReverse(List<String> inputList) {
		Collections.sort(inputList, String.CASE_INSENSITIVE_ORDER);
	}

	/**
	 * 
	 * @param f
	 * @return
	 */
	public static boolean matchFileName(String f, String filebase,
			String fileSuffix, Logger log) {

		Pattern pattern = Pattern.compile(filebase);
		Matcher matcher = pattern.matcher(f);

		if (!matcher.find()) {
			return false;
		}

		String trailing = "." + fileSuffix;
		pattern = Pattern.compile(trailing);
		matcher = pattern.matcher(f);

		if (!matcher.find()) {
			return false;
		}

		int start = matcher.start();
		int end = start + trailing.length();
		int spos = f.length();
		if (spos != end) {
			if (log != null) {
				log.debug("Data file:" + f + " does not seem to end with:"
						+ trailing);
			}
			return false;
		}
		return true;
	}

	public static boolean matchFileName(String f, String filebase, Logger log) {

		Pattern pattern = Pattern.compile(filebase);
		Matcher matcher = pattern.matcher(f);

		return matcher.find();
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
	 * Rename the data file after processing the data.
	 * 
	 * @param oldname
	 * @throws Exception
	 */
	public static void renameDataFile(String oldname, String suffix, Logger log)
			throws Exception {
		File file = new File(oldname);
		if (!file.canWrite()) {
			String err = "renameDataFile(String oldname) The file:" + oldname
					+ " does not seem to be writable for name change.";
			if (log != null) {
				log.error(err);
			}
			throw new Exception();
		}

		String newname = oldname + suffix;
		if (log != null) {
			log.info("Renaming:" + oldname + " to:" + newname);
		}
		File file2 = new File(newname);

		boolean success = file.renameTo(file2);
		if (!success) {
			String err = "renameDataFile(String oldname) The file " + oldname
					+ " was not successfully renamed!";
			if (log != null) {
				log.error(err);
			}
			throw new Exception();
		}
	}

	/**
	 * Take a server name of the form N_M_..._Q and return a list of integers
	 * {N,M,...,Q}
	 * 
	 * @param srv
	 * @return
	 * @throws Exception
	 */
	public static int[] computeDivisionsFromServerName(String srv)
			throws Exception {
		List<Integer> lst = new ArrayList<Integer>();
		StringTokenizer st = new StringTokenizer(srv, "_");
		while (st.hasMoreElements()) {
			String tok = st.nextToken();
			int val = Integer.parseInt(tok);
			lst.add(val);
		}
		int[] array = new int[lst.size()];
		for (int i = 0; i < lst.size(); i++) {
			array[i] = lst.get(i);
		}
		return array;
	}

	public static String getReadableTime() {
		Locale locale = Locale.getDefault();
		Date now = new Date();
		return DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
				.format(now);
	}

	/**
	 * Turn one line of delimited input into a list of strings. This routine is
	 * suitable only for well formed input such as lines from your properties
	 * file---it does no escape processing, does not handle handle null values,
	 * i.e., two delimiters next to each other, etc.
	 * 
	 * @param input
	 *            a comma-delimited string with no missing values, excaped
	 *            values, etc.
	 * @param delim
	 *            a string delimiter, e.g., a comma.
	 * @throws Exception
	 */
	public static List<String> getStringListFromString(String input,
			String delim) throws Exception {
		List<String> list = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(input, delim);
		while (st.hasMoreElements()) {
			String fTok = st.nextToken();
			fTok = fTok.trim();
			if (fTok.length() > 0) {
				list.add(fTok);
			}
		}
		return list;
	}

	/**
	 * Create a filename for a rolling file. IT has the form base_timestring.csv
	 * 
	 * @param path
	 * @param fname
	 * @param time
	 * @param suffix
	 * @param aLog
	 * @return
	 */
	public static String createNewFileName(String path, String fname,
			Date time, String suffix, Logger aLog) {
		StringBuffer sb = new StringBuffer();
		String rootPathName = FileAndTimeUtility.fullPathnameForFilename(fname,
				path, aLog);
		sb.append(rootPathName);
		sb.append("_");
		String dateString = FileAndTimeUtility.getDateStringForFile(time);
		sb.append(dateString);
		sb.append(suffix);
		return sb.toString();
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

	/**
	 * Get the files that are in the specified range given the datapath and data
	 * file base name
	 */
	public static List<String> getDataFilesNamesInRange(String dataPath,
			String dataFileBase, Date start, Date end, Logger log)
			throws Exception {
		List<String> fileList = FileAndTimeUtility.getFilesInDirectory(
				dataPath, dataFileBase, log);
		List<String> retList = new ArrayList<String>();
		long startTime = start.getTime();
		long endTime = end.getTime();
		assert (startTime < endTime);
		if (log != null) {
			log.info(" start:\t" + DateFormat.getInstance().format(startTime));
			log.info(" end:\t" + DateFormat.getInstance().format(endTime));
		}
		for (String fileName : fileList) {
			int startIndex = fileName.indexOf("_") + 1;
			int endIndex = fileName.lastIndexOf(".");
			String dateString = fileName.substring(startIndex, endIndex);
			Date d = FileAndTimeUtility.getDateForFormattedString(dateString);
			// System.out.println("start:" + start + "\t" + startTime);
			// System.out.println("filedate:" + d + "\t" + d.getTime());
			// System.out.println("end:" + end + "\t" + endTime);
			long time = d.getTime();
			if ((startTime <= time) && (endTime >= time)) {
				String f = FileAndTimeUtility.fullPathnameForFilename(fileName,
						dataPath, log);
				retList.add(f);
				if (log != null) {
					log.info("getDataFileNamesInRange() accepting file:" + f);
				}
			}
		}
		if (log != null) {
			log.info("getDataFileNamesInRange() found:" + fileList.size()
					+ " files and and accepted:" + retList.size() + " of them.");
		}
		return retList;
	}

	public static String getDateFileName(String fbase, Date date, String fSuffix) {
		String out = getDateStringForFile(date);
		return fbase + "." + out + fSuffix;
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
	public static String getFirstLineIfFlagged(String f1, String f2, String flag)
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

	public static boolean isFileExist(String fileName) {
		File f = new File(fileName);
		return f.exists();
	}

	/**
	 * Check out a file and throw an exception if the file is not readable.
	 * 
	 * @param fileName
	 *            The path-name of the file
	 * @param callerLog
	 *            The logger of the caller, so it's obvious where it failed.
	 * @param readWrite
	 *            The string "READ", "WRITE", or "READ_WRITE"
	 * @throws Exception
	 *             Throws an exception if file is not accessible
	 */
	public static void verifyFileReadable(String fileName, Logger callerLog,
			String readWrite) throws Exception {
		try {
			File f = new File(fileName);
			if (!f.exists()) {
				String err = "File " + fileName + " does not exist.";
				if (callerLog != null) {
					callerLog.error(err);
				}
				throw new Exception(err);
			}
			if ((readWrite.equals("READ") || readWrite.equals("READ_WRITE"))
					&& !f.canRead()) {
				String err = "Read ccess denied for file " + fileName + ".";
				if (callerLog != null) {
					callerLog.error(err);
				}
				throw new Exception(err);
			}
			if ((readWrite.equals("WRITE") || readWrite.equals("READ_WRITE"))
					&& !f.canWrite()) {
				String err = "Write access denied for file " + fileName + ".";
				if (callerLog != null) {
					callerLog.error(err);
				}
				throw new Exception(err);
			}
			if (!readWrite.equals("READ") && !readWrite.equals("WRITE")
					&& !readWrite.equals("READ_WRITE")) {
				String err = "Unknown mode of access:" + readWrite
						+ " to file:" + fileName;
				if (callerLog != null) {
					callerLog.error(err);
				}
				throw new Exception(err);
			}
		} catch (NullPointerException npx) {
			String err = "File name is null.";
			if (callerLog != null) {
				callerLog.error(err);
			}
			throw new Exception(err);
		} catch (SecurityException sx) {
			String err = "Access to file:" + fileName
					+ " denied by SecurityManager";
			if (callerLog != null) {
				callerLog.error(err);
			}
			throw new Exception(err);
		} catch (Exception x) {
			String err = "File:" + fileName
					+ " can't be accessed for unknown reason:" + x.getMessage();
			if (callerLog != null) {
				callerLog.error(err);
			}
			throw new Exception(err);
		}

	}

	/**
	 * Check out a file and throw an exception if the file is not readable.
	 * 
	 * @param fileName
	 *            The path-name of the file
	 * @param callerLog
	 *            The logger of the caller, so it's obvious where it failed.
	 * @param readWrite
	 *            The string "READ", "WRITE", or "READ_WRITE"
	 * @throws Exception
	 *             Throws an exception if file is not accessible
	 */
	public static boolean isFileReadable(String fileName, Logger callerLog,
			String readWrite) throws Exception {
		try {
			File f = new File(fileName);
			if (!f.exists()) {
				String err = "File " + fileName + " does not exist.";
				callerLog.error(err);
				return false;
			}
			if ((readWrite.equals("READ") || readWrite.equals("READ_WRITE"))
					&& !f.canRead()) {
				String err = "Read ccess denied for file " + fileName + ".";
				callerLog.error(err);
				return false;
			}
			if ((readWrite.equals("WRITE") || readWrite.equals("READ_WRITE"))
					&& !f.canWrite()) {
				String err = "Write access denied for file " + fileName + ".";
				callerLog.error(err);
				return false;
			}
			if (!readWrite.equals("READ") && !readWrite.equals("WRITE")
					&& !readWrite.equals("READ_WRITE")) {
				String err = "Unknown mode of access:" + readWrite
						+ " to file:" + fileName;
				callerLog.error(err);
				return false;
			}
		} catch (NullPointerException npx) {
			String err = "File name is null.";
			callerLog.error(err);
			return false;
		} catch (SecurityException sx) {
			String err = "Access to file:" + fileName
					+ " denied by SecurityManager";
			callerLog.error(err);
			return false;
		} catch (Exception x) {
			String err = "File:" + fileName
					+ " can't be accessed for unknown reason:" + x.getMessage();
			callerLog.error(err);
			return false;
		}
		return true;
	}

	/**
	 * Drop all files in given directory path that match the given base.
	 * 
	 * @param path
	 *            A path, with or without terminating slash
	 * @param base
	 *            The leading segment of a file name
	 * @param throwException
	 *            Turn off throwing of exceptions
	 * @throws Exception
	 *             Thrown if any of the deletions fail for any reason, unless
	 *             throwException is false.
	 */
	public static void dropFiles(String path, String base,
			boolean throwException, Logger log) throws Exception {
		log.info("\n\tDropping filebase:" + base + " in directory:" + path);
		List<String> files = new ArrayList<String>();
		try {
			files = getFilesInDirectory(path, base, log);
		} catch (Exception x) {
			System.out
					.println("Delete did not work---maybe this is the first time?");
			return;
		}
		for (String fname : files) {
			boolean success = false;
			try {
				String fullName = fullPathnameForFilename(path, fname);
				File f = new File(fullName);
				success = f.delete();
			} catch (Exception x) {
				String err = "\n\tUnable to delete file:" + fname
						+ " due to exception:" + x.getMessage();
				log.error(err);
				if (throwException) {
					throw new Exception(err);
				}
			}
			if (!success & throwException) {
				String err = "\n\tUnsuccessful result in attempt to delete file:"
						+ fname;
				log.error(err);
				throw new Exception(err);
			}
		}

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
												   String filebase) throws Exception {
		List<String> strings = new ArrayList<String>();
		File dir = new File(path);

		String[] children = dir.list();
		if (children == null) {
			String err = "There seems to be either no such directory as:"
					+ path + " or no files with base:" + filebase;
			System.err.println(err);
			throw new Exception(err);
		} else {
			for (int i = 0; i < children.length; i++) {
				String f = children[i];
				if (matchFileName(f, filebase)) {
					strings.add(f);
				}
			}
		}
		return strings;
	}


	public static boolean matchFileName(String f, String filebase) {

		Pattern pattern = Pattern.compile(filebase);
		Matcher matcher = pattern.matcher(f);

		return matcher.find();
	}
}
