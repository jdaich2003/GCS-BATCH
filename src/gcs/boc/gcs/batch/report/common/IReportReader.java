package boc.gcs.batch.report.common;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import boc.gcs.batch.report.template.ReportTemplateLoader;

public interface IReportReader {
	public void init(String fileName, ReportTemplateLoader loader);

	public void read() throws Exception;

	public Map<String, Object> getBottomContent();

	public List<Map<String, Object>> getBodyContent();

	public Map<String, Object> getTitleContent();

	public Connection getConn();

	public ReportTemplateLoader getLoader();
}
