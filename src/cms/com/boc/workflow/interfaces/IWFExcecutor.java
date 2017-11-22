package com.boc.workflow.interfaces;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IWFExcecutor {
	
	/**
	 * 启动新流程
	 * @param params
	 * @return
	 * @throws Exception 
	 */
	public boolean newInstns(Map params) throws Exception;
	/**
	 * 启动新流程并提交第一个节点
	 * @param params
	 * @return
	 * @throws Exception 
	 */
	public boolean newInstnsCommit(Map params) throws Exception;
	/**
	 * 批量启动新流程
	 * @param params
	 * @return
	 */
	public boolean newInstnsBatch(Map params);
	/**
	 * 批量启动新流程并提交第一个节点
	 * @param params
	 * @return
	 */
	public boolean newInstnsBatchCommit(Map params);
	/**
	 * 提交任务
	 * @param params
	 * 输入的参数列表
	 * bizId 业务id(非空)
	 * userId 用户id（和groupId不能同时为空）
	 * condition 工作流连线定义的流转条件，为1,2,3...等字符串
	 * groupId 组id
	 * @return
	 * @throws Exception 
	 */
	public boolean commitTask(Map params) throws Exception;
	/**
	 * 回退任务
	 * @param params
	 * @return
	 * @throws Exception 
	 */
	public boolean rollbackTask(Map params) throws Exception;
	/**
	 * 重新分配任务
	 * @param params
	 * @return
	 */
	public boolean redistribute(Map params);
	/**
	 * 获取任务列表
	 * @param params 
	 * 输入的参数列表
	 * userId 查询某用户的任务队列时非空
	 * groupId 查询某组（团队）的任务队列时非空
	 * start 分页查询的开始位置，必须>0的整数
	 * pageSize 分页查询的页大小，必须>0的整数
	 * @return
	 * @throws Exception 
	 */
	public List<String> queryTaskList(Map params) throws Exception;
	
}
