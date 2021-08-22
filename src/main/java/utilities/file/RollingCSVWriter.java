/**
 * 
 */
package utilities.file;

import org.apache.log4j.Logger;

import java.util.Date;

/**
 * @author peter
 * 
 */
public class RollingCSVWriter {
	private String directory;
	private String baseName;
	private int maxEntries;
	private int currentCount = 0;
	private CSVLogWriter csvLog;
	public static String suffix = ".csv";
	private static Logger log = Logger.getLogger(RollingCSVWriter.class);

	public RollingCSVWriter(String dir, String nameRoot, int max)
			throws Exception {
		this.directory = dir;
		this.baseName = nameRoot;
		this.maxEntries = max;
		init();
	}

	public void process(String csv) throws Exception {
		csvLog.writeNL(csv);
		currentCount++;
		if (currentCount >= maxEntries) {
			init();
		}
	}

	/**
	 * If there is a csvLogWriter open, close it.
	 */
	public void close() {
		try {
			if (csvLog != null) {
				csvLog.close();
			}
		} catch (Exception x) {

		}
	}

	public void init() throws Exception {
		Date date = new Date();
		String f = FileAndTimeUtility.createNewFileName(directory, baseName,
				date, suffix, log);
		if (FileAndTimeUtility.isFileExist(f)) {
			log.error("RollingCSVWriter.init() finds that a file with name and date already exists:"
					+ "\n\t" + f + "\n\ttrying again.`");
			Thread.sleep(1500);
			String ff = FileAndTimeUtility.createNewFileName(directory,
					baseName, new Date(), suffix, log);
			if (FileAndTimeUtility.isFileExist(ff)) {
				log.error("RollingCSVWriter.init() finds that a file with an adujsted name and "
						+ "date already exists:"
						+ "\n\t"
						+ ff
						+ "\n\tthrowing an exception. Bye.");
				throw new Exception(
						"Can't create a rolling file with the given name and date.");
			}
			log.info("RollingCSVWriter.init() Worked on the second try!!!");
			f = ff;
		}
		close();
		csvLog = new CSVLogWriter(f);
		currentCount = 0;
		log.info("init() completed:" + f);
	}

}
