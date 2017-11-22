package com.cms.workflow.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boc.workflow.impl.WFExcecutor;

import junit.framework.TestCase;
import boc.gcs.batch.common.db.C3P0ConnectionProvider;

public class WFExcecutorTest extends TestCase {

	protected void setUp() throws Exception {
		C3P0ConnectionProvider.getInstance("cms");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCommitTask() {
		Map params = new HashMap();
		params.put("bizId", "123456");
		params.put("userId", "user3");
		try {
			WFExcecutor.getInstance().commitTask(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testQueryTaskList() {
		Map params = new HashMap();
		params.put("userId", "user2");
		try {
			List<String> bizIdList =WFExcecutor.getInstance().queryTaskList(params);
			for (String string : bizIdList) {
				System.out.println(string);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testNewInstance() {
		Map params = new HashMap();
		params.put("bizId", "123456");
		try {
			WFExcecutor.getInstance().newInstns(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testNewInstnsCommit() {
		Map params = new HashMap();
		params.put("bizId", "123456");
		params.put("curntUserId", "user1");
		params.put("userId", "user2");
		try {
			WFExcecutor.getInstance().newInstnsCommit(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testNewInstanceBatch() {
		fail("Not yet implemented");
	}

	public void testRedistribute() {
		fail("Not yet implemented");
	}

	public void testRollbackTask() {
		Map params = new HashMap();
		params.put("bizId", "123456");
		params.put("condition", "2");
		try {
			WFExcecutor.getInstance().rollbackTask(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
