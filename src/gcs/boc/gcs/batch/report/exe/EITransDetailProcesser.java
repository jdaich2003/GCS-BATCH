package boc.gcs.batch.report.exe;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import boc.gcs.batch.common.util.PropertyUtil;
import boc.gcs.batch.common.util.aop.AOPFactory;
import boc.gcs.batch.common.util.aop.AOPRuntimeException;
import boc.gcs.batch.report.common.IReportReader;
import boc.gcs.batch.report.exception.GCSReportException;
import boc.gcs.batch.report.processdata.EITransDetailCheckBill;
import boc.gcs.batch.report.template.ReportTemplateLoader;
import boc.gcs.batch.report.template.ReportTemplateParser;

/**
 * 清算对账
 * 
 * @author daic
 * 
 */
public class EITransDetailProcesser {

	// private final String interfaceTempName = "EI_TRANS_DETAIL_INTERFACE";
	// private final String repDetailTempName = "ISS_TRANS_DETAIL_REPORT";
	// private final String repStatTempName = "ISS_TRANS_STAT_REPORT";
	private Map<String, String> conditions;

	private String filePath = "";
	private String fileSys = "EI";
	private String fileDate = "";
	private String fileBhid = "";

	public EITransDetailProcesser(Map<String, String> conditions)
			throws GCSReportException {
		this.conditions = conditions;
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
		filePath = PropertyUtil.getFtp("report_ftp_localRootPath")
				+ File.separator + fileSys + File.separator + fileDate
				+ File.separator + fileBhid + File.separator + fileBhid + "."
				+ PropertyUtil.getFtp("report_ftp_localpath_ei_interfaceFile")
				+ "." + fileDate + ".txt";
	}

	public void process() throws GCSReportException {
		try {
			// 加载报表模板
			ReportTemplateLoader loader = ReportTemplateParser.getInstance()
					.getConfig("EI_TRANS_DETAIL_INTERFACE");
			// 设置条件
			loader.setConditions(conditions);
			// // 通过模板读取报表数据并插入数据库临时表
			IReportReader reader = (IReportReader) AOPFactory
					.getAOPProxyedObject("boc.gcs.batch.report.reader.FixedLengthFileReader");
			// reader.init(filePath, loader);
			// reader.read();
			// 对账
			EITransDetailCheckBill checker = new EITransDetailCheckBill(loader);
			checker.checkBill();

			// 生成报表
			// CSVReprotRender render = new CSVReprotRender(ReportTemplateParser
			// .getInstance().getConfig("FKDZ1"), conditions);
			// render.process();
			// CSVReprotRender rendernm = new
			// CSVReprotRender(ReportTemplateParser
			// .getInstance().getConfig("FKDZX1"), conditions);
			// rendernm.process();

		} catch (Exception e) {
			throw new GCSReportException(e);
		} catch (AOPRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			// 下载所有本日本机构下的报表
			// FTPOper ftpoper = new FTPOper("report");
			// SimpleDateFormat format = new SimpleDateFormat("yyyymmdd");
			// String date = format.format(System.currentTimeMillis());
			// ftpoper.ftpGetBatch(PropertyUtil.getFtp("report_ftp_localpath_iss")
			// + File.separator + date, PropertyUtil
			// .getFtp("report_ftp_remotepath_iss")
			// + File.separator + date);
			// 初始化报表参数
			Map<String, String> conditions = new HashMap<String, String>();
			conditions.put("date", "2010-07-15");
			conditions.put("bhId", "4850000000");
			EITransDetailProcesser proc = new EITransDetailProcesser(conditions);
			proc.process();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
