package boc.gcs.batch.report.template;

import java.util.ArrayList;
import java.util.List;

public class ReportSql {
	private String sqlId;
	private String sqlText;
	private List<ReportParams> sqlParams = new ArrayList<ReportParams>();

	public String getSqlId() {
		return sqlId;
	}

	public void setSqlId(String sqlId) {
		this.sqlId = sqlId;
	}

	public String getSqlText() {
		return sqlText;
	}

	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}

	public List<ReportParams> getSqlParams() {
		return sqlParams;
	}

	public void setSqlParams(List<ReportParams> sqlParams) {
		this.sqlParams = sqlParams;
	}


}
