package a140.util.file;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Write to some kind of file. Parent of various writers for logs and etc.
 * 
 * These are all created with a path, a file name and a suffix, e.g., "../data",
 * "dar-file-summary", ".log"
 * 
 * Child classes will implement detail of constructing the messages.
 * 
 * @author peterc
 * 
 */
abstract public class AFileWriter {
	static Logger log = Logger.getLogger(AFileWriter.class.getName());
	public String FIELD_DELIM = ", ";
	public String LINE_DELIM = "\n";
	private String path;
	private String fname;
	private String pathname;
	private String suffix;
	private BufferedWriter writer;

	public void flush() {
		try {
			writer.flush();
		} catch (Exception x) {
			log.info("AFileWriter.flush() failed:" + fname);
		}
	}

	abstract protected Logger getLogger();

	public String getSuffix() {
		if (suffix == null) {
			suffix = "";
		}
		return suffix;
	}

	public synchronized void close() throws Exception {
		if (writer != null) {
			writer.flush();
			writer.close();
		}
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	/**
	 * Write out an arbitrary message terminated by a newline.
	 * 
	 * @param message
	 * @throws Exception
	 */
	synchronized public void write(String message, boolean newline)
			throws Exception {
		if (writer == null) {
			String err = getName()
					+ ": write(String) Attempt to write to null writer:["
					+ path + "] fname:[" + fname + "] pathname:[" + pathname
					+ "]";
			log.error(err);
			throw new Exception(err);
		}
		try {
			writer.append(message);
			if (newline) {
				writer.append("\n");
			}
			writer.flush();
		} catch (Exception x) {
			String err = getName() + ": write(String) Unable to write to:["
					+ path + "] fname:[" + fname + "] pathname:[" + pathname
					+ "] \n with exception:" + x.getMessage();
			log.error(err);
			x.printStackTrace();
			throw new Exception(err);
		}
	}

	synchronized public void write(String message) throws Exception {
		write(message, true);
	}

	/**
	 * User must initialize path, fname and suffix and call init();
	 */
	protected AFileWriter() {
	}

	/**
	 * All you need to get a log.
	 * 
	 * @param path
	 * @param fname
	 * @param suffix
	 * @throws Exception
	 */
	protected AFileWriter(String path, String fname, String suffix)
			throws Exception {
		this.path = path;
		this.fname = fname;
		this.suffix = suffix;
		init();
	}

	/**
	 * Just use the entire supplied pathname and attempt no fixing up.
	 * 
	 * @param fname
	 * @throws Exception
	 */
	protected AFileWriter(String fname) throws Exception {
		try {
			writer = new BufferedWriter(new FileWriter(fname, true));
			pathname = fname;
		} catch (Exception x) {
			String err = getName() + ": Attempt to initialize writer failed:["
					+ fname + "] exception:" + x.getMessage();
			log.error(err);
			throw new Exception(err);
		}
	}

	/**
	 * Identical with the three argument constructor, except that a fourth
	 * argument can turn off initialization for writers that want to override
	 * the use of a BufferedWriter but still use the convenience methods.
	 * 
	 * @param path
	 * @param fname
	 * @param suffix
	 * @param initialize
	 * @throws Exception
	 */
	protected AFileWriter(String path, String fname, String suffix,
			boolean initialize) throws Exception {
		this.path = path;
		this.fname = fname;
		this.suffix = suffix;
		if (initialize) {
			init();
		} else {
			pathname = fullPathnameForFilename(path, fname, suffix);
			// getLogger().debug(getName() + ": Logfile created:" + pathname);
		}
	}

	/**
	 * Throws an exception if the file cannot be opened.
	 * 
	 * @throws Exception
	 */
	protected void init() throws Exception {
		try {
			pathname = fullPathnameForFilename(path, fname, suffix);
			File f = new File(pathname);
			if (f.exists()) {
				f.delete();
			}
			writer = new BufferedWriter(new FileWriter(pathname, true));
		} catch (Exception x) {
			String err = getName() + ": Attempt to initialize writer failed:["
					+ path + "] fname:[" + fname + "] pathname:[" + pathname
					+ "] exception:[" + x.getMessage() + "]";
			log.error(err);
			throw new Exception(err);
		}
	}

	abstract protected String getName();

	/**
	 * Make a legal pathname out of the path and the filename
	 * 
	 * @param p
	 * @param file
	 * @return
	 */
	private String fullPathnameForFilename(String p, String file, String suffix) {
		if (p != null && !p.equals("")) {
			p = p.trim();
		} else {
			p = ".";
		}

		if (p.length() > 0 && p.charAt(p.length() - 1) == '/') {
			p = p.substring(0, p.length() - 2);
		}
		if (suffix == null) {
			suffix = "";
		}
		String f = p + "/" + file.trim() + suffix.trim();
		return f;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getPathname() {
		return pathname;
	}

	public void setPathname(String pathname) {
		this.pathname = pathname;
	}

	/**
	 * Create a string showing the human readable time
	 * 
	 * @see getMachineTime()
	 * @see getTwoTimesString()
	 * @return
	 */
	protected String getReadableTime() {
		Locale locale = Locale.getDefault();
		Date now = new Date();
		return DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
				.format(now);
	}

	/**
	 * Create a string representing machine time in millis
	 * 
	 * @see getReadableTime()
	 * @see getTwoTimesString()
	 * @return
	 */
	protected String getMachineTime() {
		return new Long((new Date()).getTime()).toString();
	}

	/**
	 * Create a substring of human-time, delimiter, machine time in millis
	 * 
	 * @return
	 */
	protected String getTwoTimesString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getReadableTime());
		sb.append(FIELD_DELIM);
		sb.append(getMachineTime());
		return sb.toString();
	}

}
