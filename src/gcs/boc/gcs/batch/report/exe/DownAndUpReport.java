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
 * 从ftp服务器下载、上传各系统报表
 * 
 * @author daic
 * 
 */
public class DownAndUpReport {

	/**
	 * @param args
	 */
	private static Logger logger = Logger.getLogger(DownAndUpReport.class);

	public static void main(String[] args) {

		FTPOper ftpGet = null;

		FTPOper ftpPut = null;

		String date = "";
		try {
			// SimpleDateFormat format = new SimpleDateFormat("yyyymmdd");
			// date = format.format(System.currentTimeMillis());
			// date = "2011-04-25";
			// BufferedReader in = new BufferedReader(new
			// InputStreamReader(System.in));
			// System.out.println("Please input date：(like '2000-01-01')");
			//
			// date = in.readLine();
			// if(date.indexOf("-")<0&&date.length()!=10){
			// throw new GCSReportException("日期参数'date'必须为'2000-01-01'格式");
			// }
			// 从文件中读取报表批量日期
			ReportBatchDate rbd = new ReportBatchDate();
			date = rbd.getBatchDate();

			logger.info("#######Report Download and upload start! date:" + date
					+ "########");
			// 下载本日报表
			ftpGet = new FTPOper("report");
			FtpParameters paramsDown = new FtpParameters();
			paramsDown.setDate(date);
			paramsDown.setLocalRootPath(PropertyUtil
					.getFtp("report_ftp_localRootPath"));
			paramsDown.setRemoteRootPath(PropertyUtil
					.getFtp("report_ftp_remoteRootPath"));
			ftpGet.ftpGetForReport(paramsDown);
			// 取前一天日期，VISAEU第二批
			paramsDown.setDate(rbd.getBatchDate(-1));
			ftpGet.ftpGetForReport(paramsDown);

			// 初始化数据库
			C3P0ConnectionProvider.getInstance("report");

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

			// 发卡对账报表生成
			ISSTransDetailProcesser proc = new ISSTransDetailProcesser(
					conditions);
			proc.process();
			
			// 清算对账报表生成
//			ISSTransDetailProcesser proc = new ISSTransDetailProcesser(
//					conditions);
//			proc.process();

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
			// ftpPut = new FTPOper("bcsp");
			// FtpParameters paramsBCSP = new FtpParameters();
			// paramsBCSP.setDate(date);
			// paramsBCSP.setLocalRootPath(PropertyUtil.getFtp("report_ftp_localpath_bcsp"));
			// paramsBCSP.setRemoteRootPath(PropertyUtil.getFtp("report_ftp_remoteRootPath_bcsp"));
			// ftpPut.ftpPutForBCSP(paramsBCSP);

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
