package boc.gcs.batch.report.exe;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import boc.gcs.batch.common.db.C3P0ConnectionProvider;
import boc.gcs.batch.common.util.FTPOper;
import boc.gcs.batch.common.util.PropertyUtil;
import boc.gcs.batch.report.common.ReportBatchDate;
import boc.gcs.batch.report.render.CSVReprotRender;
import boc.gcs.batch.report.template.ReportTemplateParser;
import boc.gcs.batch.report.transfer.FtpParameters;

/**
 * 从ftp服务器下载、上传各系统报表（第二批）
 * 
 * @author daic
 * 
 */
public class DownAndUpReport2 {

	/**
	 * @param args
	 */
	private static Logger logger = Logger.getLogger(DownAndUpReport2.class);

	public static void main(String[] args) {

		FTPOper ftpGet = null;

		FTPOper ftpPut = null;

		String date = "";
		try {
			// 从文件中读取报表批量日期
			ReportBatchDate rbd = new ReportBatchDate();
			date = rbd.getBatchDate();

			logger.info("#######Report Download and upload start! date:" + date
					+ "########");
			// 下载本日报表
//			ftpGet = new FTPOper("report");
//			FtpParameters paramsDown = new FtpParameters();
//			paramsDown.setDate(date);
//			paramsDown.setLocalRootPath(PropertyUtil
//					.getFtp("report_ftp_localRootPath"));
//			paramsDown.setRemoteRootPath(PropertyUtil
//					.getFtp("report_ftp_remoteRootPath"));
//			ftpGet.ftpGetForReport(paramsDown);

			// 上传本日报表
			ftpPut = new FTPOper("display");
			FtpParameters paramsUp = new FtpParameters();
			paramsUp.setDate(date);
			paramsUp.setLocalRootPath(PropertyUtil
					.getFtp("report_ftp_localRootPath"));
			paramsUp.setRemoteRootPath(PropertyUtil
					.getFtp("report_ftp_remoteRootPath_display"));
			ftpPut.ftpPutForReport(paramsUp);

			// 上传本日报表到BCSP
			ftpPut = new FTPOper("bcsp");
			FtpParameters paramsBCSP = new FtpParameters();
			paramsBCSP.setDate(date);
			paramsBCSP.setLocalRootPath(PropertyUtil
					.getFtp("report_ftp_localpath_bcsp"));
			paramsBCSP.setRemoteRootPath(PropertyUtil
					.getFtp("report_ftp_remoteRootPath_bcsp"));
			ftpPut.ftpPutForBCSP(paramsBCSP);

			logger
					.info("#######Report Download and upload finished SUCCESS!date:"
							+ date + "########");
			// 批量日期加1
			rbd.addBatchDate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.error(e.getMessage(), e);
			logger
					.error("#######Report Download and upload finished ERROR!date:"
							+ date + "########");
		} finally {
			if (ftpGet != null)
				ftpGet.ftpClose();
			if (ftpPut != null)
				ftpPut.ftpClose();
		}

	}

}
