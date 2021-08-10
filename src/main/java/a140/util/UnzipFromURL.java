package a140.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Retrieve a zip file from the given URL. Extract the files in the zip into a
 * tmp directory and make them available for processing with hasNext() and
 * getNext(), which return the path/filename of each contained document. Theese
 * methods do not return the actual document.
 * 
 * Unpacking is in a separate thread, but calls to hasNext() and getNext() will
 * hang until the unpacking is complete.
 * 
 * @author pcoates
 * 
 */
public class UnzipFromURL {
	protected String url;
	protected String outputFolder;
	protected boolean isDownloadInProgress;
	protected boolean isLowOnMemory;
	protected List<String> outFiles;
	protected static long MAX_WAIT_TIME = 1000 * 30;

	private static Logger log = Logger.getLogger(UnzipFromURL.class);

	/**
	 * A test-main that looks for an URL and a target directory in which to
	 * write the temp files it extracts.
	 * 
	 * Arguments are <url> and <tmp-file-directory>.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			log.error("Format: <url> <outputfolder>");
			System.exit(1);
		}
		String urlstr = args[0];
		String outFolder = args[1];
		UnzipFromURL uzfu = new UnzipFromURL(urlstr, outFolder);
		uzfu.startUnzipping();
		log.debug("Unzipping from a URL:" + urlstr);
		while (uzfu.hasNext() == true) {
			String filename = uzfu.getNextTmpFileName();
			log.debug("Main getting file " + filename);
			File f = new File(filename);
			if (!f.exists() && !f.canRead()) {
				log.warn("Oops! Can't open file:" + filename);
			} else {
				log.debug("File:" + filename + " is readable.");
			}
		}
	}

	/**
	 * Return whether there are any (more) documents to pick up. This call hangs
	 * while the fetch/unpacking is proceeding.
	 * 
	 * @return boolean true/false as there is or is not at least one document to
	 *         be picked up.
	 */
	public synchronized boolean hasNext() {
		while (isDownloadInProgress) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("hasNext() sleep mysteriously interrupted.");
			}
		}
		return (outFiles.size() > 0 ? true : false);
	}

	/**
	 * Return the next document path/filename. The filename is deleted from the
	 * available list before the call returns. To read the filename without
	 * deleting use peek(). To find out whether there are any more files to
	 * process use either peek or hasNext().
	 * 
	 * This call hangs while the fetch/unpacking is proceeding.
	 * 
	 * @return A string path/filename.
	 */
	public synchronized String getNextTmpFileName() {
		while (isDownloadInProgress) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("hasNext() sleep mysteriously interrupted.");
			}
		}
		if (!hasNext()) {
			return null;
		}
		return outFiles.remove(outFiles.size() - 1);
	}

	/**
	 * Utility method to delete a file. Used just before file contents are
	 * returned.
	 * 
	 * @param filename
	 */
	synchronized public static void deleteFile(String filename) {
		log.debug("UnzipFromURL.deleteFile(" + filename + ")");
		File f = new File(filename);
		if (!f.exists() || !f.canRead()) {
			log.error("loadTableData() cannot open file:<" + filename
					+ "> from input zip.");
			return;
		}
		boolean result = f.delete();
		if (result == false) {
			log.warn("loadTableData() failed to delete component file:<"
					+ filename + ">" + " from zip.");
		}
		log.debug("loadTableData() successfully deleted:<" + filename + ">");
	}

	/**
	 * Get the text of the next temporary file. This is the <i>contents</i>, not
	 * just the filename. Before successfully returning the contents, delete the
	 * file. If getting the contents fails for some reason, the contents will
	 * remain in the temporary directory.
	 * 
	 * This call hangs while the fetch/unpacking is proceeding.
	 * 
	 * @return
	 * @throws Exception
	 */
	public synchronized String getNextTmpFile() throws Exception {
		while (isDownloadInProgress) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("hasNext() sleep mysteriously interrupted.");
			}
		}
		if (!hasNext()) {
			return null;
		}
		String fname = getNextTmpFileName();
		if (fname == null) {
			return null;
		}
		File f = new File(fname);
		if (!f.exists() && f.canRead()) {
			String err = "File:" + fname + " cannot be read!";
			log.error(err);
			throw new Exception(err);
		}
		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new FileReader(fname));
			String str;
			while ((str = in.readLine()) != null) {
				sb.append(str);
			}
			in.close();
		} catch (IOException e) {
			String err = "File:" + fname
					+ " exists, is readable, but read failed!";
			log.error(err);
			throw new Exception(err);
		}
		String returnString = sb.toString();
		log.debug("File:" + fname + " successfully read. Contanins "
				+ returnString.length() + " characters.");
		deleteFile(fname);
		return returnString;
	}

	/**
	 * Return the next document path/filename <i>without</i> removing it from
	 * the list of available documents.
	 * 
	 * This call hangs while the fetch/unpacking is proceeding.
	 * 
	 * @return A string path/filename.
	 */
	public synchronized String peekAtFilename() {
		while (isDownloadInProgress) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("hasNext() sleep mysteriously interrupted.");
			}
		}
		if (!hasNext()) {
			return null;
		}
		return outFiles.get(outFiles.size() - 1);
	}

	/**
	 * The only useful constructor
	 * 
	 * @param fileUrl
	 * @param outFolderStr
	 * @throws Exception
	 */
	public UnzipFromURL(String fileUrl, String outFolderStr) throws Exception {
		url = fileUrl;
		outputFolder = outFolderStr;
		File f = new File(outputFolder);
		if ((!f.exists()) || (!f.isDirectory())) {
			String err = ("Directory:" + outFolderStr + " does not exist.");
			log.error(err);
			throw new Exception(err);
		}
		outFiles = new ArrayList<String>();
	}

	/**
	 * Start the asynchronous unzip process. The object locks until this either
	 * completes of fails.
	 * 
	 * @throws InterruptedException
	 */
	synchronized public void startUnzipping() throws Exception {
		log.debug("In startUnzipping()");
		isLowOnMemory = false;
		Thread t = (new UnzipThread());
		t.start();
		t.join();
	}

	/**
	 * Asynchronously fetch the URL contents, unzip it into a set of temporary
	 * files, and store the names of the temp files in a list that can be
	 * queried with peek and hasNext and next operations.
	 * 
	 * @author pcoates
	 * 
	 */
	private class UnzipThread extends Thread {
		@Override
		public synchronized void run() {
			Date start = new Date();
			log.debug("UnzipThread run()");
			isDownloadInProgress = true;
			URL u;
			try {
				u = new URL(url);
			} catch (MalformedURLException e3) {
				log.error("UnzipThread.run() Failed to open URL.");
				isDownloadInProgress = false;
				return;
			}
			URLConnection urlConnection;
			try {
				urlConnection = u.openConnection();
			} catch (IOException e2) {
				isDownloadInProgress = false;
				log.error("UnzipThread.run() Failed to open connection to URL. Exception."
						+ e2.getMessage());
				return;
			}
			try {
				urlConnection.connect();
			} catch (IOException e1) {
				isDownloadInProgress = false;
				log.error("UnzipThread.run() Failed to connect to URL. Exception."
						+ e1.getMessage());
			}
			try {
				int contentLength = urlConnection.getContentLength();
				String contentType = urlConnection.getContentType();
				String contentEncoding = urlConnection.getContentEncoding();
				log.debug("UnzipThread.run() Zip file:<" + url + ">");
				log.debug("UnzipThread.run() urlConnection content length:"
						+ contentLength + " type:" + contentType + " encoding:"
						+ contentEncoding);
				int ct = 1;
				ZipInputStream zipInputStream = new ZipInputStream(
						urlConnection.getInputStream());
				log.debug("UnzipThread.run() got input stream:"
						+ (zipInputStream == null ? "is null" : "is not null")
						+ " entering unpack loop.");
				for (ZipEntry zipEntry = zipInputStream.getNextEntry(); zipEntry != null; zipEntry = zipInputStream
						.getNextEntry()) {
					log.debug("UnzipThread.run() Opening zip content number:"
							+ ct++);
					String innerFileName = outputFolder + File.separator
							+ zipEntry.getName();
					// String innerFileName = zipEntry.getName();
					log.debug("UnzipThread.run() zipped file name:"
							+ innerFileName);
					File innerFile = new File(innerFileName);
					if (innerFile.exists()) {
						log.debug("UnzipThread.run() File "
								+ zipEntry.getName() + " exits! Deleting..");
						innerFile.delete();
					}
					if (zipEntry.isDirectory()) {
						log.debug("UnzipThread.run() Entry is a directory.");
						innerFile.mkdirs();
					} else {
						log.debug("UnzipThread.run() Reading the zipped inner file:"
								+ innerFileName);
						FileOutputStream outputStream = new FileOutputStream(
								innerFileName);
						final int BUFFER_SIZE = 2048;
						BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
								outputStream, BUFFER_SIZE);
						int count = 0;
						byte[] buffer = new byte[BUFFER_SIZE];
						while ((count = zipInputStream.read(buffer, 0,
								BUFFER_SIZE)) != -1) {
							bufferedOutputStream.write(buffer, 0, count);
						}
						outFiles.add(innerFileName);
						bufferedOutputStream.flush();
						bufferedOutputStream.close();
					}
					zipInputStream.closeEntry();
				}
				zipInputStream.close();
				Date end = new Date();
				long elapsed = end.getTime() - start.getTime();
				log.debug("UnzipThread.run() Unzip completed successfully in:"
						+ elapsed + " millisconds, with:" + ct + " files.");
			} catch (IOException e) {
				isDownloadInProgress = false;
				log.error("UnzipThread.run() Exception thrown by unzip thread: "
						+ e.getMessage());
				if (e.getMessage().equalsIgnoreCase("No space left on device")) {
					isLowOnMemory = true;
				}
				e.printStackTrace();
			}
			isDownloadInProgress = false;
		}
	};
}
