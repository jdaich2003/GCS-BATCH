package boc.gcs.batch.report.exe;

import java.util.HashMap;
import java.util.Map;

import boc.gcs.batch.report.render.CSVReprotRender;
import boc.gcs.batch.report.template.ReportTemplateLoader;
import boc.gcs.batch.report.template.ReportTemplateParser;
/**
 * 交易明细报表生成
 * @author daic
 *
 */
public class FKJY1 {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// 加载报表模板
			ReportTemplateLoader loader = ReportTemplateParser.getInstance()
					.getConfig("FKJY1");
			// 构建查询条件
			Map<String,String> conditions = new HashMap<String,String>();
			conditions.put("date", "20110424");
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
