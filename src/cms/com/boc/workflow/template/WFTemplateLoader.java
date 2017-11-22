package com.boc.workflow.template;

import java.util.ArrayList;
import java.util.List;

public class WFTemplateLoader {
	private String id;
	private String desc;
	private String nodeImg;
	private String transImg;
	private String arrowImg;
	private String endImg;
	private List<WFNodesLoader> nodeList = new ArrayList<WFNodesLoader>();
	private List<WFTransLoader> transList = new ArrayList<WFTransLoader>();
	
	private WFNodesLoader start ;
	private WFNodesLoader end ;

	// private List<WFNodesLoader> List = new ArrayList<WFNodesLoader>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getNodeImg() {
		return nodeImg;
	}

	public void setNodeImg(String nodeImg) {
		this.nodeImg = nodeImg;
	}

	public String getTransImg() {
		return transImg;
	}

	public void setTransImg(String transImg) {
		this.transImg = transImg;
	}

	public String getArrowImg() {
		return arrowImg;
	}

	public void setArrowImg(String arrowImg) {
		this.arrowImg = arrowImg;
	}

	public String getEndImg() {
		return endImg;
	}

	public void setEndImg(String endImg) {
		this.endImg = endImg;
	}

	public List<WFNodesLoader> getNodeList() {
		return nodeList;
	}

	public void setNodeList(List<WFNodesLoader> nodeList) {
		this.nodeList = nodeList;
	}

	public List<WFTransLoader> getTransList() {
		return transList;
	}

	public void setTransList(List<WFTransLoader> transList) {
		this.transList = transList;
	}

	public WFNodesLoader getStart() {
		return start;
	}

	public void setStart(WFNodesLoader start) {
		this.start = start;
	}

	public WFNodesLoader getEnd() {
		return end;
	}

	public void setEnd(WFNodesLoader end) {
		this.end = end;
	}

}
