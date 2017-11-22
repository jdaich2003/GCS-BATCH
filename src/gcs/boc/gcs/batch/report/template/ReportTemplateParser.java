package boc.gcs.batch.report.template;

import java.io.File;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import boc.gcs.batch.report.common.IDataProcess;

public class ReportTemplateParser {

	// 存放所有报表配置信息
	private Map<String, ReportTemplateLoader> map = new HashMap<String, ReportTemplateLoader>();
	// private Map<String, String> sqlMap = new HashMap<String, String>();

	private Map<String, ReportSql> sqlMap = new HashMap<String, ReportSql>();

	private static final String reportConfigFile = "boc" + File.separator
			+ "gcs" + File.separator + "batch" + File.separator + "report"
			+ File.separator + "config" + File.separator + "ReportConfig.xml";
	private static ReportTemplateParser instance = new ReportTemplateParser();

	private ReportTemplateParser() {
		try {
			loadXml();
		} catch (Exception e) {
			throw new RuntimeException("未能加载报表配置文件" + e);
		}
	}

	/**
	 * 得到报表解析对象，该对象只有一个
	 * 
	 * @return 报表解析对象
	 */
	public static ReportTemplateParser getInstance() {
		return instance;
	}

	/**
	 * 得到报表数量
	 * 
	 * @return 报表数量
	 */
	public int getReportSize() {
		return map.size();
	}

	/**
	 * 得到所有报表配置
	 * 
	 * @return 报表配置
	 */
	public List<ReportTemplateLoader> getAllReportConfig() {
		List<ReportTemplateLoader> list = new ArrayList<ReportTemplateLoader>();

		for (String key : map.keySet()) {
			list.add(map.get(key));
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * 得到指定报表id的配置信息
	 * 
	 * @param reportId
	 *            报表id
	 * @return 报表配置
	 */
	public ReportTemplateLoader getConfig(String reportId) {
		return map.get(reportId);
	}

	/**
	 * 得到指定报表sql 的配置信息
	 * 
	 * @param reportId
	 *            报表id
	 * @return 报表配置
	 */
	public String getSql(String sqlId) {
		if(sqlMap.get(sqlId)!=null)
		return sqlMap.get(sqlId).getSqlText();
		return null;
	}

	/**
	 * 得到指定报表sql 的配置信息
	 * 
	 * @param reportId
	 *            报表id
	 * @return 报表配置
	 */
	public List<ReportParams> getSqlParams(String sqlId) {
		if(sqlMap.get(sqlId)!=null)
		return sqlMap.get(sqlId).getSqlParams();
		return null;
	}

	/**
	 * 解析报表,将配置信息存放在map中
	 * 
	 * @throws DocumentException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("unchecked")
	private void loadXml() throws DocumentException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		SAXReader reader = new SAXReader();
		Document d = reader.read(classLoader
				.getResourceAsStream(reportConfigFile), "utf-8");

		// 加载sql
		Iterator sqlElems = d.getRootElement().elements("sql").iterator();
		while (sqlElems.hasNext()) {
			Element sqlElem = (Element) sqlElems.next();

			ReportSql repSql = new ReportSql();
			repSql.setSqlId(sqlElem.attributeValue("id"));
			repSql.setSqlText(sqlElem.getText());

			Iterator sqlParams = sqlElem.elements("params").iterator();
			// int i = 0;
			while (sqlParams.hasNext()) {
				Element sqlParam = (Element) sqlParams.next();
				ReportParams params = new ReportParams();
				params.setBindKey(sqlParam.attributeValue("bindKey"));
				params.setType(sqlParam.attributeValue("type"));
				params.setDesc(sqlParam.attributeValue("desc"));
				repSql.getSqlParams().add(params);
			}

			sqlMap.put(sqlElem.attributeValue("id"), repSql);
		}

		Iterator reportElems = d.getRootElement().elements("report").iterator();

		while (reportElems.hasNext()) {
			Element reportElem = (Element) reportElems.next();
			Iterator reportRows = reportElem.element("reportBody").elements(
				"row").iterator();

			ReportTemplateLoader configMould = new ReportTemplateLoader();
			configMould.setId(reportElem.attributeValue("id"));
			configMould.setWidth(reportElem.attributeValue("width"));
			configMould.setReportName(reportElem.element("reportName")
					.getText());
			if (null != reportElem.attributeValue("class")) {
				configMould.setDataProcess((IDataProcess) Class.forName(
					reportElem.attributeValue("class")).newInstance());
			}

			if ("true".equals(reportElem.element("reportBody").attributeValue(
				"showAmountRow"))) {
				configMould.setShowAmountRow(true);
			} else {
				configMould.setShowAmountRow(false);
			}
			if ("false".equals(reportElem.element("reportBody").attributeValue(
				"splitPage"))) {
				configMould.setSplitPage(false);
			} else {
				configMould.setSplitPage(true);
			}
			if (reportElem.element("reportBody").attributeValue("pageSize") != null) {
				configMould.setPageSize(Integer.parseInt(reportElem.element(
					"reportBody").attributeValue("pageSize")));
			}
			Element reportParams = reportElem.element("reportParams");
			if(reportParams!=null){
			Iterator reportParam = reportParams.elements("param").iterator();
			while (reportParam.hasNext()) {
				Element param = (Element) reportParam.next();
				
				ReportParams params = new ReportParams();
				params.setBindKey(param.attributeValue("bindKey"));
				params.setType(param.attributeValue("type"));
				params.setDesc(param.attributeValue("desc"));
				configMould.getReportParams().add(params);
			}
			}
			while (reportRows.hasNext()) {
				Element rowElem = (Element) reportRows.next();

				List<ReportTemplateCell> cellList = new ArrayList<ReportTemplateCell>();
				Iterator reportCells = rowElem.elements("cell").iterator();

				while (reportCells.hasNext()) {
					Element reportCell = (Element) reportCells.next();
					ReportTemplateCell cell = new ReportTemplateCell();
					cell.setBindKey(reportCell.attributeValue("bindKey"));
					cell.setText(reportCell.attributeValue("text"));
					cell.setExpression(reportCell.attributeValue("expression"));
					cell.setDefaultValue(reportCell
							.attributeValue("defaultValue"));
					cell.setSequence("true".equals(reportCell
							.attributeValue("isSequence")));
					cell.setConvert(reportCell.attributeValue("convert"));
					cell.setType(reportCell.attributeValue("type"));

					if (reportCell.attributeValue("colspan") != null
							&& !reportCell.attributeValue("colspan").equals("")) {
						cell.setColspan(Integer.parseInt(reportCell
								.attributeValue("colspan")));
					}
					if (reportCell.attributeValue("size") != null
							&& !reportCell.attributeValue("size").equals("")) {
						cell.setSize(Integer.parseInt(reportCell
								.attributeValue("size")));
					}
					cell.setFormat(getFormat(reportCell.attributeValue("type"),
						reportCell.attributeValue("format")));
					cellList.add(cell);
					if (reportCell.attributeValue("update") != null
							&& reportCell.attributeValue("update").equals(
								"true")) {
						configMould.getUpdateList().add(cell);
					}
					if (reportCell.attributeValue("check") != null
							&& reportCell.attributeValue("check")
									.equals("true")) {
						configMould.getCheckList().add(cell);
					}
				}
				if ("title".equals(rowElem.attributeValue("type"))) {
					configMould.setRowsTitle(cellList);
					if (rowElem.attributeValue("sqlId") != null
							&& !rowElem.attributeValue("sqlId").trim().equals(
								"")) {
						configMould.setTitleSqlId(rowElem
								.attributeValue("sqlId"));
					}
				} else if ("bottom".equals(rowElem.attributeValue("type"))) {
					configMould.setRowsBottom(cellList);
					if (rowElem.attributeValue("sqlId") != null
							&& !rowElem.attributeValue("sqlId").trim().equals(
								"")) {
						configMould.setBottomSqlId(rowElem
								.attributeValue("sqlId"));
					}
				} else {
					configMould.setRows(cellList);
					if (rowElem.attributeValue("sqlId") != null
							&& !rowElem.attributeValue("sqlId").trim().equals(
								"")) {
						configMould
								.setRowSqlId(rowElem.attributeValue("sqlId"));
					}
				}

			}
			map.put(configMould.getId(), configMould);
		}

	}

	/**
	 * 根据报表中配置的格式化信息，生成格式化对象
	 * 
	 * @param type
	 *            数据类型 date,double
	 * @param format
	 *            格式化格式
	 * @return 格式化对象
	 */
	private Format getFormat(String type, String format) {
		if (type == null || type.equals("") || format == null
				|| format.equals("")) {
			return null;
		}

		if ("date".equals(type)) {
			return new java.text.SimpleDateFormat(format);
		} else {
			return null;
		}
	}

}
