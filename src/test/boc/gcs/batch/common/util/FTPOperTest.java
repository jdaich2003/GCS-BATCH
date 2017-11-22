package boc.gcs.batch.common.util;

import boc.gcs.batch.report.exception.GCSReportException;
import junit.framework.TestCase;

public class FTPOperTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testFtpGet() {
		try {
			FTPOper ftp = new FTPOper("report");
//			ftp.ftpGet("e:\\iss", "/root/abc");
		} catch (GCSReportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
