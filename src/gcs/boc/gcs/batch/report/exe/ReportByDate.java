package boc.gcs.batch.report.exe;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import boc.gcs.batch.common.db.C3P0ConnectionProvider;
import boc.gcs.batch.common.util.FTPOper;
import boc.gcs.batch.common.util.PropertyUtil;
import boc.gcs.batch.report.exception.GCSReportException;
import boc.gcs.batch.report.render.CSVReprotRender;
import boc.gcs.batch.report.template.ReportTemplateLoader;
import boc.gcs.batch.report.template.ReportTemplateParser;
import boc.gcs.batch.report.transfer.FtpParameters;

public class ReportByDate {
	/**
	 * @param args
	 */
	private static Logger logger = Logger.getLogger(ReportByDate.class);

	public static void main(String[] args) {
		FTPOper ftpGet = null;
		FTPOper ftpPut = null;
		String date = "";
		try {
			// 初始化数据库
			C3P0ConnectionProvider.getInstance("report");
			// 构建查询条件
			// SimpleDateFormat format = new SimpleDateFormat("yyyymmdd");
			// date = format.format(System.currentTimeMillis());
			// date = "2016-01-13";

			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			System.out.println("Please input date：(like '2000-01-01')");

			date = in.readLine();
			if (date.indexOf("-") < 0 && date.length() != 10) {
				throw new GCSReportException("日期参数'date'必须为'2000-01-01'格式");
			}
			logger.info("#######GCS Report start! date:" + date + "########");
			Map<String, String> conditions = new HashMap<String, String>();
			conditions.put("date", date);
			conditions.put("bhId", "4850000000");
			
			 // 分行柜台交易明细报表*************************************
			CSVReprotRender render = new CSVReprotRender(ReportTemplateParser
					.getInstance().getConfig("FKJY1"), conditions);
			render.process();
			 // 分行柜台交易统计报表*************************************
			render = new CSVReprotRender(ReportTemplateParser.getInstance()
					.getConfig("FKJY0"), conditions);
			render.process();
			
			// 下载本日报表*************************************
			// ftpGet = new FTPOper("report");
			// FtpParameters paramsG = new FtpParameters();
			// paramsG.setDate(date);
			// paramsG.setLocalRootPath(PropertyUtil
			// .getFtp("report_ftp_localRootPath"));
			// paramsG.setRemoteRootPath(PropertyUtil
			// .getFtp("report_ftp_remoteRootPath"));
			// ftpGet.ftpGetForReport(paramsG);

			// 上传本日报表*************************************
			// ftpGet = new FTPOper("display");
			// FtpParameters params = new FtpParameters();
			// params.setDate(date);
			// params.setLocalRootPath(PropertyUtil
			// .getFtp("report_ftp_localRootPath"));
			// params.setRemoteRootPath(PropertyUtil
			// .getFtp("report_ftp_remoteRootPath_display"));
			// ftpGet.ftpPutForReport(params);
			logger.info("#######GCS Report end SUCCESS! date:" + date
					+ "########");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			logger.error("#######GCS Report end ERROR! date:" + date
					+ "########");
		} finally {
			if (ftpGet != null)
				ftpGet.ftpClose();
			if (ftpPut != null)
				ftpPut.ftpClose();
		}
	}

}
