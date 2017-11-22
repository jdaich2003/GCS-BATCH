package boc.gcs.batch.report.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import boc.gcs.batch.common.util.DataProcessManager;
import boc.gcs.batch.report.common.IReportReader;
import boc.gcs.batch.report.render.ReflectConverter;
import boc.gcs.batch.report.render.ReportConverter;
import boc.gcs.batch.report.template.ReportTemplateCell;
import boc.gcs.batch.report.template.ReportTemplateLoader;

public class FixedLengthFileReader extends DataProcessManager implements
		IReportReader {

	private String fileName;

	private ReportTemplateLoader loader;
	// 抬头内容
	private Map<String, Object> titleContent;
	// 尾行内容
	private Map<String, Object> bottomContent;
	// 明细列表
	private List<Map<String, Object>> bodyContent;

	public void init(String fileName, ReportTemplateLoader loader) {
		this.fileName = fileName;
		titleContent = new HashMap<String, Object>();
		bottomContent = new HashMap<String, Object>();
		bodyContent = new ArrayList<Map<String, Object>>();
		this.loader = loader;
	}

	public void read() throws Exception {
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(fileName);
			br = new BufferedReader(fr);
			String line = null;
			int lineNum = 1;
			String lastLine = null;
			while ((line = br.readLine()) != null) {
				Map<String, Object> bodyContentline = new HashMap<String, Object>();
				// 读取报表内容(处理上一条记录)
				if (lastLine != null) {
					bodyContentline = assembleData(lastLine, loader.getRows());
					// bodyContent.add(bodyContentline);

					// 将记录插入数据库
					loader.getDataProcess().init(this);
					loader.getDataProcess().process(bodyContentline);
				}
				// 读取报表头
				if (lineNum == 1 && loader.getRowsTitle() != null
						&& loader.getRowsTitle().size() > 0) {
					titleContent = assembleData(line, loader.getRowsTitle());
					
				} else {
					lastLine = line;
				}
				lineNum++;
			}
			// 处理报表尾
			if (loader.getRowsBottom() != null
					&& loader.getRowsBottom().size() > 0) {
				bottomContent = assembleData(lastLine, loader.getRowsBottom());
			} else {
				// bodyContent.add(assembleData(lastLine, loader.getRows()));
				// 如果没有报表尾，则插入处理最后一条明细
				loader.getDataProcess().process(
					assembleData(lastLine, loader.getRows()));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (null != fr) {
				fr.close();
			}
			if (null != br) {
				br.close();
			}

		}
	}

	private Map<String, Object> assembleData(String line,
			List<ReportTemplateCell> listLine) throws Exception {
		int start = 0;
		Map<String, Object> dataMap = new HashMap<String, Object>();

		for (ReportTemplateCell cell : listLine) {
			int size = cell.getSize();
			String name = cell.getBindKey();
			String value = null;
			// 报文中如果后面字段都为空，主机会直接换行
			if (line.length() <= start) {
				value = "";
			} else {
				value = line.substring(start, start + size).trim();
			}

			// @todo 通过转换器进行数据加工
			if (StringUtils.isNotBlank(cell.getConvert())) {
				Map<String, String> paramsMap = new HashMap<String, String>();
				paramsMap.put(name, value);
				value = ReflectConverter.getValue(cell.getConvert(), paramsMap);
			}
			// @todo 类型转换
			value =  ReportConverter.getInstance().convertByType(cell.getType(), value);
			start += size;
			dataMap.put(name, value);
		}
		return dataMap;
	}

	public Map<String, Object> getTitleContent() {
		return titleContent;
	}

	public void setTitleContent(Map<String, Object> titleContent) {
		this.titleContent = titleContent;
	}

	public Map<String, Object> getBottomContent() {
		return bottomContent;
	}

	public void setBottomContent(Map<String, Object> bottomContent) {
		this.bottomContent = bottomContent;
	}

	public List<Map<String, Object>> getBodyContent() {
		return bodyContent;
	}

	public void setBodyContent(List<Map<String, Object>> bodyContent) {
		this.bodyContent = bodyContent;
	}

	public ReportTemplateLoader getLoader() {
		return loader;
	}

}
