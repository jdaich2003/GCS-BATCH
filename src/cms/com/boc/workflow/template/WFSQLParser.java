package com.boc.workflow.template;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class WFSQLParser {

	// 存放所有模板配置信息
	private Map<String, String> map = new HashMap<String, String>();
	private String sqlFileName="workflow_sql_cms.xml";
	 private static final String reportConfigFilePath = "com" + File.separator
	 + "boc" + File.separator + "workflow" + File.separator + "config"
	 + File.separator ;
	private static WFSQLParser instance = new WFSQLParser();

	private WFSQLParser() {
		// try {
		// loadXml();
		// } catch (Exception e) {
		// throw new RuntimeException("未能加载报表配置文件" + e);
		// }
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
	private void loadXml(String reportName) throws DocumentException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		SAXReader reader = new SAXReader();
		Document d = reader.read(classLoader.getResourceAsStream(reportConfigFilePath+reportName),
			"utf-8");

		Iterator sqls = d.getRootElement().elements("sql").iterator();
		while (sqls.hasNext()) {
			Element sql = (Element) sqls.next();
			
			map.put(sql.attributeValue("id"), sql.getText());
		}
	}

	/**
	 * 得到报表解析对象，该对象只有一个
	 * 
	 * @return 报表解析对象
	 */
	public static WFSQLParser getInstance() {
		return instance;
	}

	/**
	 * 得到指定报表id的配置信息
	 * 
	 * @param reportId
	 *            报表id
	 * @return 报表配置
	 */
	public String getSql(String sqlName) {
		if (map.get(sqlName) == null) {
			try {
				loadXml(sqlFileName);
			} catch (Exception e) {
				throw new RuntimeException("load workflow template:" + sqlFileName
						+ " error" + e);
			}
		}
		return map.get(sqlName);
	}
}
