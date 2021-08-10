package a140.util.file;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

/**
 * The main log written to for processing failures. This is not the thrift log,
 * which recoreds the actual failed JSON text. This gives file and line number,
 * etc.
 * 
 * @author peterc
 * 
 */
public class CSVLogWriter extends AFileWriter {
	static Logger log = Logger.getLogger(CSVLogWriter.class.getName());

	private String name = this.getClass().getName();

	@Override
	protected String getName() {
		return name;
	}

	@Override
	public Logger getLogger() {
		return log;
	}

	public void setLogger(Logger l) {
		log = l;
	}

	public CSVLogWriter() {
		super();
	}

	public CSVLogWriter(String path, String fname, String suffix)
			throws Exception {
		super(path, fname, suffix);
	}

	/**
	 * Just use the entire supplied pathname and attempt no fixing up.
	 * 
	 * @param fname
	 * @throws Exception
	 */
	public CSVLogWriter(String fname) throws Exception {
		super(fname);
	}

	/**
	 * Write a CSV string to the file stream owned by this writer.
	 * 
	 * @param reqs
	 *            A string consisting of comma separated values terminated by a
	 *            newline.
	 * @throws Exception
	 */
	@Override
	public synchronized void write(String csv) throws Exception {
		try {
			write(csv, false);
		} catch (Exception x) {
			String err = "Logging failure in CSVLogWriter:" + csv + " for:"
					+ getPathname();
			log.error(err);
			log.error(err);
			throw new Exception(err);
		}
	}

	/**
	 * Write a CSV string to the file stream owned by this writer and append a
	 * newline.
	 * 
	 * @param reqs
	 *            A string consisting of comma separated values terminated by a
	 *            newline.
	 * @throws Exception
	 */
	public synchronized void writeNL(String csv) throws Exception {
		try {
			write(csv, true);
		} catch (Exception x) {
			String err = "Logging failure in CSVLogWriter:" + csv + " for:"
					+ getPathname();
			log.error(err);
			System.out.println(err);
			x.printStackTrace();
		}
	}

	static int DEFAULT_BUFFER_SIZE = 10000;
	static int FLUSH_PERIOD = 1000;

	private StringBuffer sb = null;

	// private int writeCount=0;

	/**
	 * Check the buffer periodically and flush it if it is not empty.
	 * 
	 * @author petercoates
	 * 
	 */
	class BufferFlusher implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					flushBuffer();
				} catch (Exception e1) {
					log.error("BufferFlusher failed! " + e1.getMessage());
					e1.printStackTrace();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private BufferFlusher bFlusher = null;

	private synchronized void flushBuffer() throws Exception {
		if (sb != null && sb.length() > 0) {
			write(sb.toString());
			sb = new StringBuffer(DEFAULT_BUFFER_SIZE);
		}
	}

	@Override
	public synchronized void close() throws Exception {
		flushBuffer();
		sb = null;
		super.close();
	}

	private synchronized void initBuffering() {
		sb = new StringBuffer(DEFAULT_BUFFER_SIZE);
		bFlusher = new BufferFlusher();
		(new Thread(bFlusher)).start();
	}

	private synchronized void writeToBuffer(String str) throws Exception {
		if (sb == null) {
			initBuffering();
		}
		if (sb.capacity() <= str.length()) {
			flushBuffer();
		}
		sb.append(str);
		sb.append("\n");
		// writeCount++;
	}

	/**
	 * Experimental method to store up many writes, and write them
	 * asynchronously, rather than inline.
	 */
	public synchronized void writeNLBuffered(String str) throws Exception {
		writeToBuffer(str);
	}

	/**
	 * Write a set of CSV strings to a file stream owned by this writer.
	 * 
	 * @param csvs
	 *            A list of strings consising of comma separated values,
	 *            terminated by newlines.
	 * @throws Exception
	 *             Thrown on any failed write.
	 */
	public synchronized void write(List<String> csvs) throws Exception {
		if (csvs == null) {
			String err = "null List<String> inf writeCVS for:" + getPathname();
			log.error(err);
			throw new Exception(err);
		}
		if (csvs != null) {
			for (String csv : csvs) {
				write(csv, false);
			}
		}
	}

	/**
	 * Write out all the pairs in a <String,Integer> map and then close the
	 * writer.
	 * 
	 * @param pathname
	 * @param map
	 * @throws Exception
	 */
	public synchronized void writeOutAMap(String pathname,
			Map<String, Integer> map) throws Exception {
		CSVLogWriter csvlog = new CSVLogWriter(pathname);
		for (String key : map.keySet()) {
			csvlog.writeNL(key + "," + map.get(key));
		}
		csvlog.flush();
		csvlog.close();
	}

	/**
	 * Open the file and copy all the lines to yourself.
	 * 
	 * @param fname
	 */
	public synchronized void writeAnEntireFile(String fname) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(fname));
		String str;
		int count = 0;
		while ((str = in.readLine()) != null) {
			writeNL(str);
			count++;
		}
		in.close();
		System.out.println("CVSLogWriter loaded " + count
				+ " lines from file: " + fname);
	}
}
