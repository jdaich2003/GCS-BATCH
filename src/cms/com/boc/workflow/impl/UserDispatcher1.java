package com.boc.workflow.impl;

import java.util.HashMap;
import java.util.Map;

import boc.gcs.batch.common.util.DataProcessManager;

import com.boc.workflow.interfaces.IWFUserDispatcher;

public class UserDispatcher1 extends DataProcessManager implements IWFUserDispatcher {
	
	public UserDispatcher1(){
		super.getConn();
	}

	public Map<String, String> dispatch() {
		// TODO Auto-generated method stub
		Map<String,String> result = new HashMap<String,String>();
		
		
		return result;
	}

}
