package boc.gcs.batch.report.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import boc.gcs.batch.common.util.PropertyUtil;

public class ReportBatchDate {
	private static String batchDateFile = "/report_date.properties";
	private static PropertyUtil proUtil;
	private Logger logger = Logger.getLogger(ReportBatchDate.class);

	public ReportBatchDate() {
		try {
			String filePath = PropertyUtil.class.getResource(batchDateFile)
					.getPath();
			proUtil = PropertyUtil.getInstance(filePath);
		} catch (Exception e) {
			logger
					.error(
						"init ReportBatchDate err,can't find report_date.properties",
						e);
		}
	}

	/**
	 * 读取报表批量日期
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getBatchDate() throws Exception {
		return proUtil.getValue("gcs_report_date");
	}

	/**
	 * 读取报表批量日期+i天的日期，i=1表示后一天，i=-1表示前一天
	 * 
	 * @param i
	 * @return
	 * @throws Exception
	 */
	public String getBatchDate(int i) throws Exception {
		String reportDate = proUtil.getValue("gcs_report_date");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = format.parse(reportDate);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		// while(1==1){
		calendar.add(Calendar.DAY_OF_YEAR, i);
		String result = format.format(calendar.getTime());
		return result;
	}
	/**
	 * 报表批量文件修改
	 * @throws Exception
	 */
	public void addBatchDate() throws Exception {
		String reportDate = proUtil.getValue("gcs_report_date");

		// calendar.add(Calendar.DAY_OF_YEAR, 1);
		// Date date = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = format.parse(reportDate);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		// while(1==1){
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		proUtil.setValue("gcs_report_date", format.format(calendar.getTime()));
		proUtil.savefile("");
		// System.out.println(format.format(calendar.getTime()));
		// Thread.sleep(100);
		// }
	}

	public static void main(String[] args) {
		try {
			new ReportBatchDate().addBatchDate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
