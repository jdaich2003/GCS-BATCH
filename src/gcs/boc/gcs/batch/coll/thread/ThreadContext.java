package boc.gcs.batch.coll.thread;

import java.sql.Connection;

import boc.gcs.batch.coll.dealdata.MakeBillsParas;

/**
 * 数据处理线程上下文
 * 
 * @author daic 
 * 
 */
public class ThreadContext {
	private Connection conn;
	private String timestamp;
	private String errMsg;
	private String batchType;
	private String batchNo;
	private String threadNo;
	private int dataCount;
	private long totalTime;
	private MakeBillsParas paras = new MakeBillsParas();

	public MakeBillsParas getParas() {
		return paras;
	}

	public void setParas(MakeBillsParas paras) {
		this.paras = paras;
	}

	public String getThreadNo() {
		return threadNo;
	}

	public void setThreadNo(String threadNo) {
		this.threadNo = threadNo;
	}

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public String getBatchType() {
		return batchType;
	}

	public void setBatchType(String batchType) {
		this.batchType = batchType;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}


	public int getDataCount() {
		return dataCount;
	}

	public void setDataCount(int dataCount) {
		this.dataCount = dataCount;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

}
