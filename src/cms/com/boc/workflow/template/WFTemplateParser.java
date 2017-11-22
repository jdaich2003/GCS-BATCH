package com.boc.workflow.template;

import java.io.File;
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

public class WFTemplateParser {

	// 存放所有模板配置信息
	private Map<String, WFTemplateLoader> map = new HashMap<String, WFTemplateLoader>();

	 private static final String reportConfigFilePath = "com" + File.separator
	 + "boc" + File.separator + "workflow" + File.separator + "config"
	 + File.separator ;
	private static WFTemplateParser instance = new WFTemplateParser();

	private WFTemplateParser() {
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
		Element templ = d.getRootElement();

		WFTemplateLoader loader = new WFTemplateLoader();

		loader.setId(templ.attributeValue("id"));
		loader.setDesc(templ.attributeValue("desc"));
		loader.setNodeImg(templ.attributeValue("nodeImg"));
		loader.setTransImg(templ.attributeValue("transImg"));
		loader.setArrowImg(templ.attributeValue("arrowImg"));
		loader.setEndImg(templ.attributeValue("endImg"));

		Iterator nodes = d.getRootElement().elements("node").iterator();
		while (nodes.hasNext()) {
			Element node = (Element) nodes.next();
			WFNodesLoader nodeLoader = new WFNodesLoader();
			nodeLoader.setId(node.attributeValue("id"));
			nodeLoader.setDesc(node.attributeValue("desc"));
			nodeLoader.setType(node.attributeValue("type"));
			nodeLoader.setPosition(node.attributeValue("position"));
			nodeLoader.setDispatcher(node.attributeValue("dispatcher"));
			loader.getNodeList().add(nodeLoader);

		}

		Iterator transs = d.getRootElement().elements("trans").iterator();
		while (transs.hasNext()) {
			Element trans = (Element) transs.next();
			WFTransLoader transLoader = new WFTransLoader();
			transLoader.setId(trans.attributeValue("id"));
			transLoader.setDesc(trans.attributeValue("desc"));
			transLoader.setFrom(trans.attributeValue("from"));
			transLoader.setTo(trans.attributeValue("to"));
			transLoader.setPosition(trans.attributeValue("position"));
			transLoader.setCondition(trans.attributeValue("condition"));
			loader.getTransList().add(transLoader);

		}
		Element start =d.getRootElement().element("start");
		if(start!=null){
			WFNodesLoader startLoader = new WFNodesLoader();
			startLoader.setId(start.attributeValue("id"));
			startLoader.setDesc(start.attributeValue("desc"));
			startLoader.setType(start.attributeValue("type"));
			loader.setStart(startLoader);
		}
		Element end =d.getRootElement().element("end");
		if(end!=null){
			WFNodesLoader endLoader = new WFNodesLoader();
			endLoader.setId(end.attributeValue("id"));
			endLoader.setDesc(end.attributeValue("desc"));
			endLoader.setType(end.attributeValue("type"));
			loader.setEnd(endLoader);
		}
		map.put(reportName, loader);
	}

	/**
	 * 得到报表解析对象，该对象只有一个
	 * 
	 * @return 报表解析对象
	 */
	public static WFTemplateParser getInstance() {
		return instance;
	}

	/**
	 * 得到所有报表配置
	 * 
	 * @return 报表配置
	 */
	public List<WFTemplateLoader> getAllReportConfig() {
		List<WFTemplateLoader> list = new ArrayList<WFTemplateLoader>();

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
	public WFTemplateLoader getConfig(String reportName) {
		if (map.get(reportName) == null) {
			try {
				loadXml(reportName);
			} catch (Exception e) {
				throw new RuntimeException("load workflow template:" + reportName
						+ " error" + e);
			}
		}
		return map.get(reportName);
	}
}
