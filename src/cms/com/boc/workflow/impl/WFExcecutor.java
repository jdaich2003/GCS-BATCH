package com.boc.workflow.impl;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import boc.gcs.batch.common.util.UUIDGenerator;

import com.boc.workflow.common.WFDataProcessManager;
import com.boc.workflow.interfaces.IWFExcecutor;
import com.boc.workflow.template.WFNodesLoader;
import com.boc.workflow.template.WFSQLParser;
import com.boc.workflow.template.WFTemplateLoader;
import com.boc.workflow.template.WFTemplateParser;
import com.boc.workflow.template.WFTransLoader;

public class WFExcecutor extends WFDataProcessManager implements IWFExcecutor {

	private WFExcecutor() {
		loader = WFTemplateParser.getInstance().getConfig(templateName);
		startNode = loader.getStart();
		endNode = loader.getEnd();
		// 测试时使用c3p0连接池
		super.getConn();
		// try {
		// conn.setAutoCommit(true);
		// } catch (SQLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	private static WFExcecutor excecutor = null;

	public static IWFExcecutor getInstance() {
		if (excecutor == null) {
			excecutor = new WFExcecutor();
		}
		return excecutor;
	}

	private void userDetect(Map<String, String> params) throws Exception {

		if (params.get("userId") == null && params.get("groupId") == null) {
			throw new Exception(
					"params \"userId\",\"groupId\" cannot be NULL at the same time!");
		}
	}

	public boolean commitTask(Map params) throws Exception {
		// 第1步：删除原活动task
		// 第2步：修改原历史活动task
		// 第3步：新增活动task
		// 第4步：新增历史活动task

		ResultSet query_task_rs = null;
		// 查询活动任务
		PreparedStatement query_task_ps = null;
		// 查询活动任务
		PreparedStatement update_task_ps = null;
		// 新增活动任务
		PreparedStatement add_task_ps = null;
		// 新增历史任务
		PreparedStatement add_taskHis_ps = null;
		// 删除活动任务
		PreparedStatement delete_task_ps = null;
		// 修改历史任务
		PreparedStatement update_taskHis_ps = null;
		String bizId = "";
		String condition = "";
		String userId = "";
		String groupId = "";
		String roleId = "";
		String curntUserId = "";
		try {
			if (params.get("bizId") == null) {
				throw new Exception("params \"bizId\" cannot be NULL!");
			}
			if (params.get("bizId") != null)
				bizId = params.get("bizId").toString();
			if (params.get("condition") != null)
				condition = params.get("condition").toString();
			if (params.get("userId") != null)
				userId = params.get("userId").toString();
			if (params.get("groupId") != null)
				groupId = params.get("groupId").toString();
			if (params.get("roleId") != null)
				roleId = params.get("roleId").toString();
			if (params.get("curntUserId") != null)
				curntUserId = params.get("curntUserId").toString();
			String nodeTemplId = null;

			add_task_ps = conn.prepareStatement(add_task_sql);
			add_taskHis_ps = conn.prepareStatement(add_taskHis_sql);
			delete_task_ps = conn.prepareStatement(delete_task_sql);
			update_taskHis_ps = conn.prepareStatement(update_taskHis_sql);
			update_task_ps = conn.prepareStatement(update_task_sql);
			query_task_ps = conn.prepareStatement(query_task_sql);

			query_task_ps.setString(1, bizId);
			query_task_rs = query_task_ps.executeQuery();
			if (query_task_rs.next()) {
				nodeTemplId = query_task_rs.getString("TEMPL_NODE_ID");
			} else {
				nodeTemplId = startNode.getId();
			}
			List<WFTransLoader> transList = loader.getTransList();
			for (WFTransLoader transLoader : transList) {
				if ((transLoader.getFrom().equals(nodeTemplId)
						&& transLoader.getCondition() != null && transLoader
						.getCondition().equals(condition))
						|| (transLoader.getFrom().equals(nodeTemplId) && transLoader
								.getCondition() == null)) {
					if (transLoader.getTo() != null
							&& transLoader.getTo().equals(endNode.getId())) {
						update_taskHis_ps.setString(1, transLoader.getId());
						update_taskHis_ps.setString(2, bizId);
						update_taskHis_ps.execute();
						
						delete_task_ps.setString(1, bizId);
						delete_task_ps.execute();
					} else if (transLoader.getTo() != null) {
						String newTaskId = new UUIDGenerator().generate()
								.toString();
						List<WFNodesLoader> nodeList = loader.getNodeList();
						for (WFNodesLoader nodesLoader : nodeList) {
							if (nodesLoader.getId().equals(nodeTemplId)) {
								if (nodesLoader.getDispatcher() != null) {
									Method method = Class.forName(
										nodesLoader.getDispatcher()).getMethod(
										"dispatch");
									Map<String, String> result = (Map<String, String>) method
											.invoke(Class.forName(
												nodesLoader.getDispatcher())
													.newInstance());
									userDetect(result);
									if (result.get("userId") != null)
										userId = result.get("userId")
												.toString();
									if (result.get("groupId") != null)
										groupId = result.get("groupId")
												.toString();
								} else {
									userDetect(params);
								}
							}
						}

						if (nodeTemplId.equals(startNode.getId())) {
							add_taskHis_ps.setString(1, bizId);
							add_taskHis_ps.setString(2, new UUIDGenerator()
									.generate().toString());
							add_taskHis_ps.setString(3, curntUserId);
							add_taskHis_ps.setString(4, roleId);
							add_taskHis_ps.setString(5, groupId);
							add_taskHis_ps.setString(6, "01");
							add_taskHis_ps.setString(7, transLoader.getFrom());
							add_taskHis_ps.execute();

							// biz_id,wf_task_id,user_id,role_id,group_id
							add_task_ps.setString(1, bizId);
							add_task_ps.setString(2, newTaskId);
							add_task_ps.setString(3, userId);
							add_task_ps.setString(4, roleId);
							add_task_ps.setString(5, groupId);
							add_task_ps.setString(6, "01");
							add_task_ps.setString(7, transLoader.getTo());
							add_task_ps.execute();

						} else {
							update_task_ps.setString(1, transLoader.getTo());
							update_task_ps.setString(2, userId);
							update_task_ps.setString(3, roleId);
							update_task_ps.setString(4, groupId);
							update_task_ps.setString(5, "01");
							update_task_ps.setString(6, bizId);
							update_task_ps.execute();
						}
						update_taskHis_ps.setString(1, transLoader.getId());
						update_taskHis_ps.setString(2, bizId);
						update_taskHis_ps.execute();
						// biz_id,wf_task_id,user_id,role_id,group_id
						add_taskHis_ps.setString(1, bizId);
						add_taskHis_ps.setString(2, newTaskId);
						add_taskHis_ps.setString(3, userId);
						add_taskHis_ps.setString(4, roleId);
						add_taskHis_ps.setString(5, groupId);
						add_taskHis_ps.setString(6, "01");
						add_taskHis_ps.setString(7, transLoader.getTo());
						add_taskHis_ps.execute();

					}
					break;
				} else if (transLoader.getCondition() != null
						&& condition == null) {
					throw new Exception("params \"condition\" cannot be NULL!");
				}
			}
			conn.commit();
		} catch (Exception e) {
			throw e;
		} finally {
			closeRS(query_task_rs);
			closePS(add_task_ps);
			closePS(add_taskHis_ps);
			closePS(delete_task_ps);
			closePS(update_taskHis_ps);
			closePS(query_task_ps);
			closePS(update_task_ps);
		}
		return false;
	}

	public List<String> queryTaskList(Map params) throws Exception {
		String userId = "";
		String groupId = "";
		// String roleId = "";
		int start = 0;
		int pageSize = 0;
		// 查询活动任务列表
		PreparedStatement query_task_ps = null;
		ResultSet query_task_rs = null;
		List<String> result = new ArrayList<String>();
		String sql = "";
		try {
			userDetect(params);
			if (params.get("start") != null && params.get("pageSize") != null) {
				start = Integer.valueOf(params.get("start").toString());
				pageSize = Integer.valueOf(params.get("pageSize").toString());
				if (start <= 0 || pageSize <= 0) {
					throw new Exception(
							"params \"start\",\"pageSize\" should be Integer!");
				}
			}
			if (params.get("userId") != null) {
				userId = params.get("userId").toString();
				if (start != 0) {
					sql = pageStart + query_userActive_sql + pageEnd;
				} else {
					sql = query_userActive_sql;
				}
				query_task_ps = conn.prepareStatement(sql);

				query_task_ps.setString(1, userId);

			} else if (params.get("groupId") != null) {
				groupId = params.get("groupId").toString();
				if (start != 0) {
					sql = pageStart + query_groupActive_sql + pageEnd;
				} else {
					sql = query_groupActive_sql;
				}
				query_task_ps = conn.prepareStatement(sql);

				query_task_ps.setString(1, groupId);
			}
			// if (params.get("roleId") != null)
			// roleId = params.get("roleId").toString();
			query_task_rs = query_task_ps.executeQuery();
			result = super.getOneResultList(query_task_rs);

		} catch (Exception e) {
			throw e;
		} finally {
			closeRS(query_task_rs);
			closePS(query_task_ps);
		}

		return result;
	}

	public boolean newInstns(Map params) throws Exception {
		// TODO 通过配置文件读取参数映射表
		// 新增活动任务
		PreparedStatement add_task_ps = null;
		// 新增历史任务
		PreparedStatement add_taskHis_ps = null;

		String bizId = "";
		String condition = "";
		String userId = "";
		String groupId = "";
		String roleId = "";
		try {
			if (params.get("bizId") != null)
				bizId = params.get("bizId").toString();
			if (params.get("condition") != null)
				condition = params.get("condition").toString();
			if (params.get("userId") != null)
				userId = params.get("userId").toString();
			if (params.get("groupId") != null)
				groupId = params.get("groupId").toString();
			if (params.get("roleId") != null)
				roleId = params.get("roleId").toString();

			String newTaskId = new UUIDGenerator().generate().toString();
			String nodeTemplId = startNode.getId();
			add_task_ps = conn.prepareStatement(add_task_sql);
			add_taskHis_ps = conn.prepareStatement(add_taskHis_sql);

			if (startNode.getType() != null && startNode.equals("manual")) {
				add_taskHis_ps.setString(1, bizId);
				add_taskHis_ps.setString(2, newTaskId);
				add_taskHis_ps.setString(3, userId);
				add_taskHis_ps.setString(4, roleId);
				add_taskHis_ps.setString(5, groupId);
				add_taskHis_ps.setString(6, "01");
				add_taskHis_ps.setString(7, startNode.getId());
				add_taskHis_ps.execute();

				// biz_id,wf_task_id,user_id,role_id,group_id
				add_task_ps.setString(1, bizId);
				add_task_ps.setString(2, newTaskId);
				add_task_ps.setString(3, userId);
				add_task_ps.setString(4, roleId);
				add_task_ps.setString(5, groupId);
				add_task_ps.setString(6, "01");
				add_task_ps.setString(7, startNode.getId());
				add_task_ps.execute();
			} else {
				List<WFTransLoader> list = loader.getTransList();
				for (WFTransLoader transLoader : list) {
					if ((transLoader.getFrom().equals(nodeTemplId)
							&& transLoader.getCondition() != null && transLoader
							.getCondition().equals(condition))
							|| (transLoader.getFrom().equals(nodeTemplId) && transLoader
									.getCondition() == null)) {
						add_taskHis_ps.setString(1, bizId);
						add_taskHis_ps.setString(2, newTaskId);
						add_taskHis_ps.setString(3, userId);
						add_taskHis_ps.setString(4, roleId);
						add_taskHis_ps.setString(5, groupId);
						add_taskHis_ps.setString(6, "01");
						add_taskHis_ps.setString(7, transLoader.getTo());
						add_taskHis_ps.execute();

						// biz_id,wf_task_id,user_id,role_id,group_id
						add_task_ps.setString(1, bizId);
						add_task_ps.setString(2, newTaskId);
						add_task_ps.setString(3, userId);
						add_task_ps.setString(4, roleId);
						add_task_ps.setString(5, groupId);
						add_task_ps.setString(6, "01");
						add_task_ps.setString(7, transLoader.getTo());
						add_task_ps.execute();
					}
				}
			}

		} catch (Exception e) {
			throw e;
		} finally {
			closePS(add_task_ps);
			closePS(add_taskHis_ps);
		}
		return false;
	}

	public boolean newInstnsBatch(Map params) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean redistribute(Map params) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean rollbackTask(Map params) throws Exception {
		PreparedStatement query_task_ps = null;

		PreparedStatement query_lastTask_ps = null;

		ResultSet query_lastTask_rs = null;
		ResultSet query_task_rs = null;
		PreparedStatement update_task_ps = null;
		PreparedStatement add_taskHis_ps = null;
		PreparedStatement update_taskHis_ps = null;
		String bizId = "";
		String condition = "";
		String userId = "";
		String groupId = "";
		String roleId = "";
		String nodeTemplId = "";
		try {
			if (params.get("bizId") != null)
				bizId = params.get("bizId").toString();
			if (params.get("condition") != null)
				condition = params.get("condition").toString();
			if (params.get("userId") != null)
				userId = params.get("userId").toString();
			if (params.get("groupId") != null)
				groupId = params.get("groupId").toString();
			if (params.get("roleId") != null)
				roleId = params.get("roleId").toString();
			query_task_ps = conn.prepareStatement(query_task_sql);

			query_task_ps.setString(1, bizId);
			query_task_rs = query_task_ps.executeQuery();
			if (query_task_rs.next()) {
				nodeTemplId = query_task_rs.getString("TEMPL_NODE_ID");
				String newTaskId = new UUIDGenerator().generate().toString();
				List<WFTransLoader> list = loader.getTransList();

				for (WFTransLoader transLoader : list) {
					if ((transLoader.getFrom().equals(nodeTemplId)
							&& transLoader.getCondition() != null && transLoader
							.getCondition().equals(condition))
							|| (transLoader.getFrom().equals(nodeTemplId) && transLoader
									.getCondition() == null)) {
						String lastUserId = "";
						if (userId == null || userId.equals("")) {
							query_lastTask_ps = conn
									.prepareStatement(query_lastTask_sql);
							query_lastTask_ps.setString(1, transLoader.getTo());
							query_lastTask_rs = query_lastTask_ps
									.executeQuery();

							if (query_lastTask_rs.next()) {
								lastUserId = query_lastTask_rs
										.getString("USER_ID");
							}
						} else {
							lastUserId = userId;
						}
						update_task_ps = conn.prepareStatement(update_task_sql);
						add_taskHis_ps = conn.prepareStatement(add_taskHis_sql);
						update_taskHis_ps =  conn.prepareStatement(update_taskHis_sql);
						update_taskHis_ps.setString(1, transLoader.getId());
						update_taskHis_ps.setString(2, bizId);
						update_taskHis_ps.execute();
						
						add_taskHis_ps.setString(1, bizId);
						add_taskHis_ps.setString(2, newTaskId);
						add_taskHis_ps.setString(3, lastUserId);
						add_taskHis_ps.setString(4, roleId);
						add_taskHis_ps.setString(5, groupId);
						add_taskHis_ps.setString(6, "02");
						add_taskHis_ps.setString(7, transLoader.getTo());
						add_taskHis_ps.execute();

						// biz_id,wf_task_id,user_id,role_id,group_id
						update_task_ps.setString(1, transLoader.getTo());
						update_task_ps.setString(2, lastUserId);
						update_task_ps.setString(3, roleId);
						update_task_ps.setString(4, groupId);
						update_task_ps.setString(5, "02");
						update_task_ps.setString(6, bizId);
						update_task_ps.execute();
						break;						
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			closeRS(query_task_rs);
			closeRS(query_lastTask_rs);
			closePS(query_lastTask_ps);
			closePS(query_task_ps);
			closePS(update_task_ps);
			closePS(add_taskHis_ps);
			closePS(update_taskHis_ps);
		}
		conn.commit();
		return false;
	}

	public boolean newInstnsCommit(Map params) throws Exception {
		// TODO Auto-generated method stub
		// this.newInstns(params);
		if (params.get("curntUserId") == null
				|| params.get("curntUserId").equals("")) {
			throw new Exception(
					"when start workflow with COMMIT,\"curntUserId\" can NOT be null");
		}
		this.commitTask(params);
		return false;
	}

	public boolean newInstnsBatchCommit(Map params) {
		// TODO Auto-generated method stub
		return false;
	}

//	public List<String> queryForDisplay(Map params) throws Exception {
		
//		List<String> result=new
//	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	private String templateName = "workflow_template_cms.xml";
	private WFTemplateLoader loader;

	private String query_task_sql = WFSQLParser.getInstance().getSql("query_task_sql");

	private String add_task_sql = WFSQLParser.getInstance().getSql("add_task_sql");

	private String update_task_sql = WFSQLParser.getInstance().getSql("update_task_sql");

	private String add_taskHis_sql = WFSQLParser.getInstance().getSql("add_taskHis_sql");

	private String delete_task_sql = WFSQLParser.getInstance().getSql("delete_task_sql");

	private String update_taskHis_sql = WFSQLParser.getInstance().getSql("update_taskHis_sql");

	private String pageStart = "with partdata as ( ";

	private String pageEnd = ") select * from (select  rownum as rowno,partdata.* from  partdata ) where rowno>  ? and rowno<=?";

	private String query_userActive_sql = WFSQLParser.getInstance().getSql("query_userActive_sql");

	private String query_groupActive_sql = WFSQLParser.getInstance().getSql("query_groupActive_sql");

	private String query_lastTask_sql = WFSQLParser.getInstance().getSql("query_lastTask_sql");

	private WFNodesLoader startNode;

	private WFNodesLoader endNode;
}
