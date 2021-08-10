package a140.util.json;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class FormatJSON {

	static Logger log = null;
	private String filename;
	int charPerDot = 100000;
	int dotPerLine = 1000000;
	int nlCount = 0;

	public FormatJSON(String s, Logger l) {
		filename = s;
		log = l;
		if (log == null) {
			log = Logger.getLogger(FormatJSON.class.getName());
		}
	}

	/**
	 * Read in the file and count the apparent blocks. Throw an exception if it
	 * appears to be mal-formed, i.e., the left and the right curly brackets are
	 * out of parity.
	 * 
	 * @param toConsole
	 * @return
	 */
	public int read(boolean toConsole) throws FileNotFoundException, Exception {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filename));
			char[] charbuf = new char[1];
			int left = -1;
			int right = -1;
			@SuppressWarnings("unused")
			int nr = 0;
			int cct = 0;
			if (toConsole) {
				System.out.println("Dots indicate count progressing.");
			}
			while ((nr = in.read(charbuf)) >= 0) {
				cct++;
				if (toConsole) {
					if ((cct % 1000000) == 0) {
						System.out.print(".");
						if (cct % 50000000 == 0) {
							System.out.println();
						}
					}
				}
				char c = charbuf[0];
				if (c == '{') {
					left++;
				} else if (c == '}') {
					right++;
				}
				// out.write(c);
				if (left == right) {
					// out.write("\n");
					nlCount++;
				}
				if (right > left) {
					String err = "Too many right } brackets! l:" + left + " r:"
							+ right + " blocks so far:" + nlCount + "chars:"
							+ cct + "\n";
					throw new Exception(err);
				}
			}
			if (toConsole) {
				System.out.println("\npre-read complete");
			}
			if (toConsole) {
				logToConsole(nlCount, cct, left, right);
			}
			log.info("pre-read found record count: = " + nlCount
					+ " count chars:" + cct);
			if (left != right) {
				String err = ("pre-read terminated with mismatch left:" + left
						+ " right:" + right + " chars:" + cct);
				throw new Exception(err);
			}
		} finally {
			if(in!=null){
				in.close();
			}
		}
		return nlCount;
	}

	private void logToConsole(int nlCount, int cct, int left, int right) {
		System.out.println();
		System.out
				.println("********************************************************");
		System.out.println("******  record count = " + nlCount
				+ " count chars:" + cct);
		if (left != right) {
			String err = ("******pre-read terminated with mismatch left:"
					+ left + " right:" + right + " chars:" + cct);
			System.out.println(err);
		}
		System.out
				.println("********************************************************");
	}
}
