package com.boc.workflow.interfaces;

import java.util.Map;

public interface IWFUserDispatcher {
	/**
	 * 分配工作任务
	 * 
	 * @return 
	 * 返回Map，key值包括"userId","groupId","roleId"等
	 */
	public Map<String, String> dispatch();
}
