package boc.gcs.batch.report.render;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import boc.gcs.batch.common.util.DataProcessManager;
import boc.gcs.batch.common.util.PropertyUtil;
import boc.gcs.batch.report.exception.GCSReportException;
import boc.gcs.batch.report.template.ReportParams;
import boc.gcs.batch.report.template.ReportTemplateCell;
import boc.gcs.batch.report.template.ReportTemplateLoader;
import boc.gcs.batch.report.template.ReportTemplateParser;

public class CSVReprotRender extends DataProcessManager {

	private ReportTemplateLoader loader;
	// private QueryConditions conditions;
	private Map<String, String> conditions;
	private String sql_query_row;
	private PreparedStatement ps_query_row;
	private ResultSet rs_query_row;
	private String sql_query_title;
	private PreparedStatement ps_query_title;
	private ResultSet rs_query_title;
	private String sql_query_bottom;
	private PreparedStatement ps_query_bottom;
	private ResultSet rs_query_bottom;

	private Logger logger = Logger.getLogger(CSVReprotRender.class);
	private int rowCount;
	private BufferedWriter bfw = null;
	private FileWriter fw = null;

	private static final int maxPageCount = 50000;
	private boolean lastFileHasResult = false;
	private int fileNum = 1;
	private String filePath = "";
	private String fileSys = "GCS";
	private String fileDate = "";
	private String fileBhid = "";

	public CSVReprotRender(ReportTemplateLoader loader,
			Map<String, String> conditions) throws GCSReportException {
		// super();
		super.getConn();
		this.loader = loader;
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

		// 本地报表生成路径 /GCS/日期/BH号
		filePath = PropertyUtil.getFtp("report_ftp_localRootPath")
				+ File.separator + fileSys + File.separator + fileDate
				+ File.separator + fileBhid + File.separator;
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public void process() throws GCSReportException {
		try {
			fillNewFile();
		} catch (Exception e) {
			logger.error(e);
			throw new GCSReportException(e);
		} finally {
			try {
				fw.close();
				bfw.close();
				// 如果最后写的文件没有记录则删除已写文件
				if (!lastFileHasResult) {
					String fileName = fileBhid + "_" + loader.getId() + "_"
							+ fileDate + "_" + (fileNum - 1) + ".csv";
					File file = new File(filePath + fileName);
					if (file.exists()) {
						file.delete();
					}
				}
				closePS(ps_query_row);
				closeRS(rs_query_row);
				closePS(ps_query_title);
				closeRS(rs_query_title);
				closePS(ps_query_bottom);
				closeRS(rs_query_bottom);
			} catch (Exception e) {
				logger.error(e);
				throw new GCSReportException(e);
			}
		}
	}

	private void fillNewFile() throws Exception {
		// 文件名 BH号_报表名_日期_文件号.csv
		String fileName = fileBhid + "_" + loader.getId() + "_" + fileDate
				+ "_" + fileNum + ".csv";
		fileNum++;
		fw = new FileWriter(filePath + fileName);
		bfw = new BufferedWriter(fw);
		rowCount = 1;
		writeTitle();
		writeDetail();
		writeBottom();
	}

	/**
	 * 写报表头
	 * 
	 * @throws Exception
	 */
	private void writeTitle() throws Exception {
		if (loader.getRowsTitle() != null && loader.getRowsTitle().size() > 0) {
			sql_query_title = ReportTemplateParser.getInstance().getSql(
				loader.getTitleSqlId());
			if (sql_query_title != null) {
				writeDesc(loader.getRowsTitle());
				ps_query_title = conn.prepareStatement(sql_query_title);
				setParams(ps_query_title, loader.getTitleSqlId());

				rs_query_title = ps_query_title.executeQuery();
				while (rs_query_title.next()) {
					rs2FileSingle(100, loader.getRowsTitle());
				}
			} else {
				noSql2FileSingle(loader.getRowsTitle());
			}
		}
	}

	/**
	 * 写报表明细，当明细数量大于每页最大数量时，重新写一个文件
	 * 
	 * @throws Exception
	 */
	private void writeDetail() throws Exception {
		writeDesc(loader.getRows());
		if (loader.getRows() != null && loader.getRows().size() > 0) {
			sql_query_row = ReportTemplateParser.getInstance().getSql(
				loader.getRowSqlId());
			ps_query_row = conn.prepareStatement(sql_query_row);
			setParams(ps_query_row, loader.getRowSqlId());
			rs_query_row = ps_query_row.executeQuery();
			rs2File(rs_query_row);
		}
	}

	private void setParams(PreparedStatement ps, String sqlId) throws Exception {
		List<ReportParams> list = ReportTemplateParser.getInstance()
				.getSqlParams(sqlId);
		for (int i = 0; i < list.size(); i++) {
			ReportParams param = list.get(i);
			// Object object = conditions.get(param.getBindKey());
			if (conditions.get(param.getBindKey()) == null) {
				throw new Exception("报表参数" + param.getBindKey() + "不能为空！");
			}
//			if (param.getBindKey().equals("date")) {
//				String date = (String) conditions.get(param.getBindKey());
//				StringBuilder sb = new StringBuilder(date);
//				sb.insert(4, "-");
//				sb.insert(7, "-");
//				setValues(ps, i + 1, sb.toString());
//			} else {
//				setValues(ps, i + 1, conditions.get(param.getBindKey()));
//			}
			setValues(ps, i + 1, conditions.get(param.getBindKey()));
		}
	}

	/**
	 * 写报表尾
	 * 
	 * @throws Exception
	 */
	private void writeBottom() throws Exception {
		if (loader.getRowsBottom() != null && loader.getRowsBottom().size() > 0) {
			sql_query_bottom = ReportTemplateParser.getInstance().getSql(
				loader.getBottomSqlId());
			if (sql_query_bottom != null) {
				writeDesc(loader.getRowsBottom());
				ps_query_bottom = conn.prepareStatement(sql_query_bottom);
				setParams(ps_query_bottom, loader.getBottomSqlId());
				rs_query_bottom = ps_query_bottom.executeQuery();
				while (rs_query_bottom.next()) {
					rs2FileSingle(200, loader.getRowsBottom());
				}
			} else {
				noSql2FileSingle(loader.getRowsBottom());
			}
		}
	}

	/**
	 * 写报表字段名称
	 * 
	 * @throws Exception
	 */
	private void writeDesc(List<ReportTemplateCell> list) throws Exception {
		StringBuilder line = new StringBuilder();
		for (ReportTemplateCell reportTemplateCell : list) {
			line.append(reportTemplateCell.getText()).append(",");
		}
		bfw.write(StringUtils.substringBeforeLast(line.toString(), ",") + "\n");
		bfw.flush();
	}

	/**
	 * 結果集明细直接輸出為文件
	 * 
	 * @param rs
	 * @throws Exception
	 */
	private void rs2File(ResultSet rs) throws Exception {

		while (rs.next()) {
			rs2FileSingle(000, loader.getRows());
			lastFileHasResult = true;
			rowCount++;
			if ((rowCount - 1) % maxPageCount == 0) {
				writeBottom();
				if (fw != null)
					fw.close();
				if (bfw != null)
					bfw.close();
				// 文件名 BH号_报表名_日期_文件号.csv
				String fileName = fileBhid + "_" + loader.getId() + "_"
						+ fileDate + "_" + fileNum + ".csv";
				fileNum++;
				fw = new FileWriter(filePath + fileName);
				bfw = new BufferedWriter(fw);
				rowCount = 1;
				lastFileHasResult = false;
				writeTitle();
				writeDesc(loader.getRows());
			}
		}

	}

	private void rs2FileSingle(int flag, List<ReportTemplateCell> rowList)
			throws Exception {
		ResultSetMetaData rsm = null;
		switch (flag) {
		case 100:
			rsm = rs_query_title.getMetaData();
			break;
		case 200:
			rsm = rs_query_bottom.getMetaData();
			break;
		default:
			rsm = rs_query_row.getMetaData();
			break;
		}
		// ResultSetMetaData rsm = rs.getMetaData();
		Map<String, String> conditionsTemp = new HashMap<String, String>();
		conditionsTemp.putAll(conditions);

		StringBuilder line = new StringBuilder();
		// lastFileHasResult = true;

		Map<String, String> row = new HashMap<String, String>();
		int size = rsm.getColumnCount();
		for (int j = 1; j <= size; j++) {
			Object obj = null;
			switch (flag) {
			case 100:
				obj = rs_query_title.getObject(j);
				break;
			case 200:
				obj = rs_query_bottom.getObject(j);
				break;
			default:
				obj = rs_query_row.getObject(j);
				break;
			}
			if (obj != null) {
				row.put(rsm.getColumnLabel(j).toLowerCase(), obj.toString());
			} else {
				row.put(rsm.getColumnLabel(j).toLowerCase(), "");
			}
		}

		conditionsTemp.putAll(row);

		for (int i = 0; i < rowList.size(); i++) {
			ReportTemplateCell cell = rowList.get(i);
			String value = "";
			if (cell.getBindKey() != null && row.containsKey(cell.getBindKey())) {
				if (row.get(cell.getBindKey()) != null) {
					value = row.get(cell.getBindKey());

				}
			} // @todo 通过转换器进行数据加工
			if (StringUtils.isNotBlank(cell.getConvert())) {
				value = ReflectConverter.getValue(cell.getConvert(),
					conditionsTemp);
			}
			if (value == null || value.trim().equals("")) {
				if (cell.getType().equals("BigDecimal")) {
					value = "0.00";
				} else {
					value = "";
				}
			}
			value = value.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll(",", "，");
			line.append(value).append("\t").append(",");
		}
		bfw.write(StringUtils.substringBeforeLast(line.toString(), ",") + "\n");
		bfw.flush();
	}

	private void noSql2FileSingle(List<ReportTemplateCell> rowList)
			throws Exception {
		Map<String, String> conditionsTemp = new HashMap<String, String>();
		conditionsTemp.putAll(conditions);
		StringBuilder line = new StringBuilder();
		for (int i = 0; i < rowList.size(); i++) {
			ReportTemplateCell cell = rowList.get(i);
			String value = "";
			// @todo 通过转换器进行数据加工
			if (StringUtils.isNotBlank(cell.getConvert())) {
				value = ReflectConverter.getValue(cell.getConvert(),
					conditionsTemp);
			}
			if (value == null || value.trim().equals("")) {
				if (cell.getType().equalsIgnoreCase("BigDecimal")) {
					value = "0";
				} else {
					value = "";
				}
			}
			line.append(value).append("\t").append(",");
		}
		bfw.write(StringUtils.substringBeforeLast(line.toString(), ",") + "\n");
		bfw.flush();
	}
}
