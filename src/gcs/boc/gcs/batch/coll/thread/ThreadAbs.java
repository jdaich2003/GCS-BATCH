package boc.gcs.batch.coll.thread;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import boc.gcs.batch.common.db.C3P0ConnectionProvider;

/**
 * 处理线程父类
 * 
 * @author daic
 * 
 */
public abstract class ThreadAbs {
	protected Logger logger;
	protected ThreadContext context;
	private Connection conn;
	private long startTime;
	private long endTime;
	private String errString;
	private int startRow;
	private int endRow;

	protected void init(ThreadContext context) throws Exception {
		this.context = context;
		this.conn = getConnection();
		if (conn.getAutoCommit()) {
			conn.setAutoCommit(false);
		}
		startRow = this.context.getParas().getStart();
		endRow = this.context.getParas().getEnd();
		this.context.getParas().setConn(conn);
		startReport();
	}

	private Connection getConnection() throws Exception {
		return C3P0ConnectionProvider.getInstance("").getConnection();
	}

	private void closeConnection() throws Exception {
		if (conn != null) {
			C3P0ConnectionProvider.getInstance("").closeConnection(conn);
		}
	}

	private void rollback() throws SQLException {
		conn.rollback();
	}

	private void startReport() {
		startTime = System.currentTimeMillis();
		if (context.getThreadNo() == null) {
			context.setThreadNo("MAIN");
		}
		// 记录线程日志
		// logger.info(context.getBatchType() + "_" + context.getBatchNo() + "_"
		// + context.getThreadNo() + "_" + context.getTimestamp() + " is
		// started");
		// 插入数据库
	}

	protected void endReport() throws Exception {
		endTime = System.currentTimeMillis();
		// 记录线程日志
		logger.info(context.getBatchType() + "_" + context.getBatchNo() + "_"
				+ context.getThreadNo() + "_" + context.getTimestamp()
				+ " success,$scope:" + startRow + "---" + endRow + "$count:"
				+ context.getDataCount() + "$time:" + (endTime - startTime)
				/ 1000 + " seconds");
		// 关闭数据库
		closeConnection();
	}

	protected void endErrReport(String errString) throws SQLException {
		endTime = System.currentTimeMillis();
		// 记录线程日志
		logger.error(context.getBatchType() + "_" + context.getBatchNo() + "_"
				+ context.getThreadNo() + "_" + context.getTimestamp()
				+ " get error,data scope:" + startRow + "---" + endRow
				+ "reason is:" + errString);

		// 数据回滚
		rollback();
	}

	public String getErrString() {
		return errString;
	}

	public void setErrString(String errString) {
		this.errString = errString;

	}

}
