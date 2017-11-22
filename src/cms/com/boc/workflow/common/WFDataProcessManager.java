package com.boc.workflow.common;

import java.sql.Connection;


public class WFDataProcessManager extends DataProcessManager {

	public WFDataProcessManager() {
		// TODO Auto-generated constructor stub
	}

	public Connection getConnection() throws Exception {
		if (conn == null) {
			throw new Exception(
					"should user setConnection() to initialize Connection for Transaction!");
		}
		return conn;
	}
	public void setConnection(Connection conn){
		this.conn=conn;
	}
}
