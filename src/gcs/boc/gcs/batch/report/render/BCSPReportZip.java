package boc.gcs.batch.report.render;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipInputStream;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import boc.gcs.batch.common.util.PropertyUtil;
import boc.gcs.batch.report.transfer.FtpParameters;

public class BCSPReportZip {

	private static FileOutputStream fileOut = null;
	private static ZipOutputStream outputStream = null;
	private static String filepara;
	private static String filepara_tmp;

	public static void init(FtpParameters params) throws Exception {

		filepara = PropertyUtil.getFtp("report_ftp_localpath_bcsp")
				+ File.separator + params.getBhid() + params.getDate() + ".zip";
		filepara_tmp = PropertyUtil.getFtp("report_ftp_localpath_bcsp")
				+ File.separator + params.getBhid() + params.getDate()
				+ ".zip.tmp";
		File dir = new File(PropertyUtil.getFtp("report_ftp_localpath_bcsp"));
		if (!dir.exists()) {
			dir.mkdirs();
		}
		fileOut = new FileOutputStream(filepara_tmp);
		outputStream = new ZipOutputStream(fileOut);

	}

	public static void zipForReport(File sourceFile) throws Exception {

		byte[] buf = new byte[1024];
		int readLen = 0;
		outputStream.putNextEntry(new ZipEntry(sourceFile.getName()));
		// outputStream.setEncoding("GB2312");

		InputStream is = new BufferedInputStream(
				new FileInputStream(sourceFile));
		while ((readLen = is.read(buf, 0, 1024)) != -1) {
			outputStream.write(buf, 0, readLen);
		}
		is.close();

		outputStream.flush();
		fileOut.flush();
	}

	public static void end() throws Exception {
		// 合并zip文件
		byte[] buf = new byte[1024];
		int readLen = 0;
		File oldFile = new File(filepara);
		if (oldFile.exists()) {
			ZipFile zipFile = new ZipFile(oldFile);
			Enumeration<ZipEntry> e = zipFile.getEntries();
			while (e.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) e.nextElement();
				outputStream.putNextEntry(zipEntry);
				InputStream inputStream = zipFile.getInputStream(zipEntry);
				while ((readLen = inputStream.read(buf, 0, 1024)) != -1) {
					outputStream.write(buf, 0, readLen);
				}
				inputStream.close();
			}
			zipFile.close();
			// FileInputStream in = new FileInputStream(oldFile);
			// ZipInputStream zInputStream = new ZipInputStream(in);
			// ZipEntry zipEntry;
			// while ((zipEntry = (ZipEntry)zInputStream.getNextEntry()) !=
			// null) {
			// outputStream.putNextEntry(zipEntry);
			// while ((readLen = zInputStream.read(buf, 0, 1024)) != -1) {
			// outputStream.write(buf, 0, readLen);
			// }
			// }
			// zInputStream.closeEntry();
			// zInputStream.close();
			// in.close();
		}
		if (outputStream != null) {
			outputStream.closeEntry();
			outputStream.close();
		}
		if (fileOut != null)
			fileOut.close();

		if (oldFile.exists()) {
			oldFile.delete();
		}
		File newFile = new File(filepara_tmp);
		boolean aa = newFile.renameTo(oldFile);
		// System.out.println(aa);

	}

	public static void main(String[] args) {
		try {
			FtpParameters params = new FtpParameters();
			params.setBhid("4750000000");
			params.setDate("2016-06-13");

			BCSPReportZip.init(params);
			// String fileName =
			// "C:\\report\\GCS\\20160304\\4850000000\\4850000000_FKDZX1_20160304_1.csv";
			String fileName = "e:\\gcsftp\\gcs\\20110701\\4850000000\\4850000000_UPLOAD_20110701_2.csv";
			// String fileName =
			// "C:\\report\\GCS\\20160306\\4850000000\\4850000000_FKDZ1_20160306_1.csv";
			File sourceFile = new File(fileName);

			BCSPReportZip.zipForReport(sourceFile);
			BCSPReportZip.end();
			System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}

	}
}
