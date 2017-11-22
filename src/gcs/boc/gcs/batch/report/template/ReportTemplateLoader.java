package boc.gcs.batch.report.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boc.gcs.batch.report.common.IDataProcess;
import boc.gcs.batch.report.common.IReportReader;

public class ReportTemplateLoader {
	
	private String bizType="";//业务种类 1代表iss；2代表ei
	private String tableName="";//业务种类 1代表iss；2代表ei
	private String id = ""; // 报表id
	private String width = ""; // 数字表示报表宽度（如果为空表示非定长报文）
	private String reportName = ""; // 报表名称
	private boolean showAmountRow = false; // 报表最下面是否显示总计行
	private boolean splitPage = true; // 是否分页
	private int pageSize = 30; // 每页行数
	
	private String titleSqlId="";
	private String rowSqlId="";
	private String bottomSqlId="";
	
	
	private List<ReportParams> reportParams = new ArrayList<ReportParams>(); //报表参数
	
	private List<ReportTemplateCell> rowsTitle = new ArrayList<ReportTemplateCell>();
	private List<ReportTemplateCell> rows = new ArrayList<ReportTemplateCell>();
	private List<ReportTemplateCell> rowsBottom = new ArrayList<ReportTemplateCell>();
	
	private List<ReportTemplateCell> updateList = new ArrayList<ReportTemplateCell>();
	private List<ReportTemplateCell> checkList = new ArrayList<ReportTemplateCell>();
	
	private Map<String,String> sqlMap = new HashMap<String,String>();

	private IDataProcess dataProcess;
	private IReportReader reader;
	
	private Map<String,String> conditions;
	public String getTitleSqlId() {
		return titleSqlId;
	}

	public void setTitleSqlId(String titleSqlId) {
		this.titleSqlId = titleSqlId;
	}

	public String getRowSqlId() {
		return rowSqlId;
	}

	public void setRowSqlId(String rowSqlId) {
		this.rowSqlId = rowSqlId;
	}

	public String getBottomSqlId() {
		return bottomSqlId;
	}

	public void setBottomSqlId(String bottomSqlId) {
		this.bottomSqlId = bottomSqlId;
	}

	public List<ReportTemplateCell> getUpdateList() {
		return updateList;
	}

	public void setUpdateList(List<ReportTemplateCell> updateList) {
		this.updateList = updateList;
	}

	public List<ReportTemplateCell> getCheckList() {
		return checkList;
	}

	public void setCheckList(List<ReportTemplateCell> checkList) {
		this.checkList = checkList;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Map<String, String> getSqlMap() {
		return sqlMap;
	}

	public String getBizType() {
		return bizType;
	}

	public void setBizType(String bizType) {
		this.bizType = bizType;
	}

	public void setSqlMap(Map<String, String> sqlMap) {
		this.sqlMap = sqlMap;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		if (id != null) {
			this.id = id.trim();
		}
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		if (width != null) {
			this.width = width.trim();
		}
	}

	

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public boolean isShowAmountRow() {
		return showAmountRow;
	}

	public void setShowAmountRow(boolean showAmountRow) {
		this.showAmountRow = showAmountRow;
	}


	public boolean isSplitPage() {
		return splitPage;
	}

	public void setSplitPage(boolean splitPage) {
		this.splitPage = splitPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}


	public List<ReportTemplateCell> getRowsTitle() {
		return rowsTitle;
	}

	public void setRowsTitle(List<ReportTemplateCell> rowsTitle) {
		this.rowsTitle = rowsTitle;
	}

	public List<ReportTemplateCell> getRows() {
		return rows;
	}

	public void setRows(List<ReportTemplateCell> rows) {
		this.rows = rows;
	}

	public List<ReportTemplateCell> getRowsBottom() {
		return rowsBottom;
	}

	public void setRowsBottom(List<ReportTemplateCell> rowsBottom) {
		this.rowsBottom = rowsBottom;
	}

	/**
	 * 最外面的key为该报表转换器名称,Map<String,List<String>> 为要转换的字段和参数,key为字段,List<String>为参数
	 * 
	 * @return
	 */
	public Map<String, Map<String, List<String>>> getConverts() {
		Map<String, Map<String, List<String>>> map = new HashMap<String, Map<String, List<String>>>();
//		List<ReportTemplateCell> list = rows.get(rows.size() - 1);

		for (ReportTemplateCell cell : rows) {
			String convertName = cell.getConvert();
			if (!convertName.equals("")) {
				if (convertName.indexOf("(") != -1) {
					convertName = convertName.substring(0, convertName
							.indexOf("("));
				}

				if (map.get(convertName) == null) {
					map.put(convertName, new HashMap<String, List<String>>());
				}
				Map<String, List<String>> columnMap = map.get(convertName);
				if (cell.getConvert().indexOf("(") != -1) {
					String column = cell.getBindKey();
					String args = cell.getConvert().substring(
						cell.getConvert().indexOf("(") + 1,
						cell.getConvert().indexOf(")"));
					List<String> argList = Arrays.asList(args.split(","));
					columnMap.put(column, argList);
				} else {
					List<String> argList = new ArrayList<String>();
					argList.add(cell.getBindKey());
					columnMap.put(cell.getBindKey(), argList);
				}

			}
		}

		return map;
	}

	public IDataProcess getDataProcess() {
		return dataProcess;
	}

	public void setDataProcess(IDataProcess dataProcess) {
		this.dataProcess = dataProcess;
	}


	public Map<String, String> getConditions() {
		return conditions;
	}

	public void setConditions(Map<String, String> conditions) {
		this.conditions = conditions;
	}

	public List<ReportParams> getReportParams() {
		return reportParams;
	}

	public void setReportParams(List<ReportParams> reportParams) {
		this.reportParams = reportParams;
	}

	public IReportReader getReader() {
		return reader;
	}

	public void setReader(IReportReader reader) {
		this.reader = reader;
	}


}


