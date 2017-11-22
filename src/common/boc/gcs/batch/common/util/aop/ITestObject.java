package boc.gcs.batch.common.util.aop;

import java.sql.Connection;

public interface ITestObject {
	public String test(String org);
	public String read(String org) throws Exception;
	public void setConn(Connection conn);
	public Connection getConn();
}
