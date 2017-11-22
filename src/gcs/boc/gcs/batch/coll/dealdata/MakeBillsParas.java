package boc.gcs.batch.coll.dealdata;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
/**
 * 业务逻辑参数
 * @author daic
 *
 */
public class MakeBillsParas {
	private Connection conn;
	private int start;
	private int end;
	private List<PayOfBillObject> payList;


	public List<PayOfBillObject> getPayList() {
		return payList;
	}

	public void setPayList(List<PayOfBillObject> payList) {
		this.payList = payList;
	}

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}  
}
