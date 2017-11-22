package boc.gcs.batch.report.exe;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import boc.gcs.batch.common.db.C3P0ConnectionProvider;
import boc.gcs.batch.common.util.FTPOper;
import boc.gcs.batch.common.util.PropertyUtil;
import boc.gcs.batch.common.util.aop.AOPFactory;
import boc.gcs.batch.common.util.aop.AOPRuntimeException;
import boc.gcs.batch.report.common.IReportReader;
import boc.gcs.batch.report.exception.GCSReportException;
import boc.gcs.batch.report.processdata.ISSTransDetailCheckBill;
import boc.gcs.batch.report.render.CSVReprotRender;
import boc.gcs.batch.report.template.ReportTemplateLoader;
import boc.gcs.batch.report.template.ReportTemplateParser;
import boc.gcs.batch.report.transfer.FtpParameters;

/**
 * 发卡对账
 * 
 * @author daic
 * 
 */
public class ISSTransDetailProcesserCK {

	// private final String interfaceTempName = "EI_TRANS_DETAIL_INTERFACE";
	// private final String repDetailTempName = "ISS_TRANS_DETAIL_REPORT";
	// private final String repStatTempName = "ISS_TRANS_STAT_REPORT";
	private Map<String, String> conditions;
	String checkFile;
	private static Logger logger = Logger
			.getLogger(ISSTransDetailProcesserCK.class);

	public ISSTransDetailProcesserCK(Map<String, String> conditions)
			throws GCSReportException {
		this.conditions = conditions;
		String fileDate;
		String fileBhid;
		// 转换日期
		if (conditions.get("date") != null) {
			if (conditions.get("date").indexOf("-") < 0) {
				throw new GCSReportException("日期参数'date'必须为'2000-01-01'格式");
			}
			fileDate = conditions.get("date").replaceAll("-", "");
		} else {
			throw new GCSReportException("日期参数'date'不能为空");
		}
		if (conditions.get("bhId") != null) {
			fileBhid = conditions.get("bhId");
		} else {
			throw new GCSReportException("日期参数'bhId'不能为空");
		}

		checkFile = PropertyUtil.getFtp("report_ftp_localRootPath")
				+ File.separator + "ISS" + File.separator + fileDate
				+ File.separator + fileBhid + File.separator
				+ PropertyUtil.getFtp("report_ftp_localpath_iss_interfaceFile");
		// +".B"+fileBhid+".D"+fileDate;
	}

	public void process() throws GCSReportException {
		FTPOper ftpPut = null;
		try {
			// 加载报表模板
			ReportTemplateLoader loader = ReportTemplateParser.getInstance()
					.getConfig("ISS_TRANS_DETAIL_INTERFACE");
			// 设置条件
			loader.setConditions(conditions);

			// 对账
			ISSTransDetailCheckBill checker = new ISSTransDetailCheckBill(
					loader);
			checker.checkBill();

			// 生成报表
			CSVReprotRender render = new CSVReprotRender(ReportTemplateParser
					.getInstance().getConfig("FKDZ1"), conditions);
			render.process();
			CSVReprotRender rendernm = new CSVReprotRender(ReportTemplateParser
					.getInstance().getConfig("FKDZX1"), conditions);
			rendernm.process();

		} catch (Exception e) {
			logger.error("ISS check error !!!", e);
		} finally {
			if (ftpPut != null)
				ftpPut.ftpClose();
		}
	}

	public static void main(String[] args) {
		String date = "";
		try {

			// 初始化数据库
			C3P0ConnectionProvider.getInstance("report");

			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			System.out.println("Please input date：(like '2000-01-01')");

			date = in.readLine();
			if (date.indexOf("-") < 0 && date.length() != 10) {
				throw new GCSReportException("日期参数'date'必须为'2000-01-01'格式");
			}
			// 初始化报表参数
			Map<String, String> conditions = new HashMap<String, String>();
			// 加载对账文件
			// conditions.put("date", "2016-03-11");
			// 对账
			// conditions.put("date", "2016-03-05");
			conditions.put("date", date);
			conditions.put("bhId", "4850000000");
			ISSTransDetailProcesserCK proc = new ISSTransDetailProcesserCK(
					conditions);
			proc.process();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
