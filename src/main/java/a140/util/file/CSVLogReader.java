/**
 * 
 */
package a140.util.file;

import a140.StringToIntHashMap;
import a140.util.Utility;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public static Map<Integer, String> readIntStringMap(String fileSpec)
			throws Exception {
		Date start = new Date();
		Map<Integer, String> map = new HashMap<Integer, String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fileSpec));
			long totalWords = 0;
			String in;
			while ((in = reader.readLine()) != null) {
				if (in != null) {
					totalWords++;
					in = in.trim();
					List<String> list = Utility.getTokenListFromString(in, ',');
					if (list.size() == 0) {
						continue;
					}
					Integer key = null;
					try {
						key = Integer.parseInt(list.get(0));
					} catch (NumberFormatException nfx) {
						log.error("ReadIntStringMap() found bad number format:"
								+ list.get(0));
						continue;
					}
					String value = list.get(1);
					if (value == null || (value = value.trim()).equals("")) {
						log.error("ReadIntStringMap() found empty word value.");
						continue;
					}
					map.put(key, value);
				}
			}
			double rate = FileAndTimeUtility.rateSec(totalWords, start,
					new Date());
			log.info("Read in Integer->String map at rate of: " + rate
					+ " lines per second.");
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return map;
	}

	/**
	 * Optimization to allow pre-allocation of the tables size. This is vastly
	 * more efficient, as it sidesteps the need to re-hash a zillion times as it
	 * grows.
	 * 
	 * A 64 bit vm takes app(130 + strlen) bytes per key-value pair. To run
	 * efficiently as the memory usage approaches the maximum heap size, you
	 * need a 20% overhead. Thus, the memory for this structure (e.g.,
	 * -Xmx1000m) must be about 20% greater than they caluculated amount, plus
	 * whatever you need for the rest.
	 * 
	 * @param fileSpec
	 * @param size
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Integer> readStringIntMap(String fileSpec,
			int maxSize) throws Exception {
		Map<String, Integer> ret = readStringIntMap(fileSpec);
		return ret;
	}

	public static StringToIntHashMap readStringIntMapLongs(String fileSpec,
			int maxSize) throws Exception {
		StringToIntHashMap ret = readStringIntMapLongs(fileSpec);
		return ret;
	}

	// private static Integer MAX_KV_PAIRS=null;

	/**
	 * Creates a Map<String,Integer> from a CSV file, pre-allocating a given
	 * quantity of slots. If the slot-size is set, use the set size. Otherwise,
	 * just insert away.
	 * 
	 * @param fileSpec
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Integer> readStringIntMap(String fileSpec)
			throws Exception {
		Date start = new Date();
		Map<String, Integer> map = new HashMap<String, Integer>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fileSpec));
			long totalWords = 0;
			String in;
			while ((in = reader.readLine()) != null) {
				if (in != null) {
					totalWords++;
					in = in.trim();
					List<String> list = Utility.getTokenListFromString(in, ',');
					if (list.size() == 0) {
						continue;
					}
					Integer value = null;
					try {
						value = Integer.parseInt(list.get(1));
					} catch (NumberFormatException nfx) {
						log.error("ReadStringIntMap() found bad number format:"
								+ list.get(0));
						continue;
					}
					String key = list.get(0);
					if (key == null || (key = key.trim()).equals("")) {
						log.error("ReadStringIntMap() found empty word key.");
						continue;
					}
					map.put(key, value);
				}
			}
			double rate = FileAndTimeUtility.rateSec(totalWords, start,
					new Date());
			log.info("Read in Integer->String map at rate of: " + rate
					+ " lines per second.");
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return map;
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

	public static StringToIntHashMap readStringIntMapLongs(String fileSpec)
			throws Exception {
		Date start = new Date();
		StringToIntHashMap map = new StringToIntHashMap();
		BufferedReader reader = null;
		try{
		reader = new BufferedReader(new FileReader(fileSpec));
		long totalWords = 0;
		String in;
		while ((in = reader.readLine()) != null) {
			if (in != null) {
				totalWords++;
				in = in.trim();
				List<String> list = Utility.getTokenListFromString(in, ',');
				if (list.size() == 0) {
					continue;
				}
				Integer value = null;
				try {
					value = Integer.parseInt(list.get(1));
				} catch (NumberFormatException nfx) {
					log.error("ReadStringIntMapLongs() found bad number format:"
							+ list.get(0));
					continue;
				}
				String key = list.get(0);
				if (key == null || (key = key.trim()).equals("")) {
					log.error("ReadStringIntMapLongs() found empty word key.");
					continue;
				}
				map.put(key, value);
			}
		}
		double rate = FileAndTimeUtility.rateSec(totalWords, start, new Date());
		log.info("Read in Integer->String map at rate of: " + rate
				+ " lines per second.");
		}
		finally{
			if(reader!=null){
				reader.close();
			}
		}
		return map;
	}
}
