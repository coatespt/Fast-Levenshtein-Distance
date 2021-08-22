package utilities.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

/**
 * Utilities to deal with gzip files.
 * @author petercoates
 *
 */
public class Zip {
	
	/**
	 * Unzip a file and leave it without the trailing gz.
	 * @param inFilePath
	 * @return
	 * @throws Exception
	 */
	public static String Unzip(String inFilePath) throws Exception
	{
	    FileInputStream fis = new FileInputStream(inFilePath);
	    GZIPInputStream gzipInputStream = new GZIPInputStream(fis);
	 
	    String outFilePath = inFilePath.replace(".gz", "");
	    OutputStream out = new FileOutputStream(outFilePath);
	 
	    byte[] buf = new byte[1024];
	    int len;
	    try{
	    while ((len = gzipInputStream.read(buf)) > 0)
	        out.write(buf, 0, len);
	    }
	    finally{
	    	//fis.close();
	    	gzipInputStream.close();
	    	out.close();
	    	// These may be unnecessary--put in in an attempt to obviate a quirk
	    	// of how MacOS handles inactive memory.
	    	System.gc();
	    	System.runFinalization();
	    }
	 
	    return outFilePath;
	}
	
	/**
	 * Delete the unzipped file. As protection, throw an exception if, like an idiot, you try
	 * to delete the zipped version by mistake.
	 * 
	 * @param unzippedFile
	 * @throws Exception
	 */
	public static void deleteUnzipped(String unzippedFile) throws Exception{
		if(unzippedFile.indexOf(".gz")==unzippedFile.length()-3){
			String err = "Looks like you are deleting the wrong file:" + unzippedFile;
			throw new Exception(err);
		}
	    File ff = new File(unzippedFile);
	    ff.delete();
	}
}
