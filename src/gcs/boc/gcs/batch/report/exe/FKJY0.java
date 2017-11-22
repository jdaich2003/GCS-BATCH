package boc.gcs.batch.report.exe;

import java.util.HashMap;
import java.util.Map;

import boc.gcs.batch.common.db.C3P0ConnectionProvider;
import boc.gcs.batch.report.render.CSVReprotRender;
import boc.gcs.batch.report.template.ReportTemplateLoader;
import boc.gcs.batch.report.template.ReportTemplateParser;
/**
 * 交易统计报表生成
 * @author daic
 *
 */
public class FKJY0 {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			C3P0ConnectionProvider.getInstance("report");
			// 加载报表模板
			ReportTemplateLoader loader = ReportTemplateParser.getInstance()
					.getConfig("FKJY0");
			// 构建查询条件
			Map<String,String> conditions = new HashMap<String,String>();
			conditions.put("date", "2011-04-24");
			conditions.put("bhId", "4850000000");
			// 生成报表
			CSVReprotRender render = new CSVReprotRender(loader,
					conditions);
			render.process();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
