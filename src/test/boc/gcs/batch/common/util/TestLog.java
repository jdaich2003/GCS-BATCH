package boc.gcs.batch.common.util;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class TestLog {
	private static Logger logger = Logger.getLogger(TestLog.class);
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		logger.info("Testestesetestsetseset");
		File file1 = new File("c:\\report_bcsp\\475000000020160613.zip");
		File file2 = new File("c:\\report_bcsp\\475000000020160613.zip.tmp");
		if(!file2.exists()){
			file2.createNewFile();
		}
		
		file2.renameTo(file1);
	}

}
