package boc.gcs.batch.report.common;

import java.sql.Connection;

public interface ITransManager {
	public void setConn(Connection conn);
	public Connection getConn();
}
