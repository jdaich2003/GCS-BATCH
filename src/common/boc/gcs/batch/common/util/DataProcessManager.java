package boc.gcs.batch.common.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boc.gcs.batch.common.db.C3P0ConnectionProvider;

/**
 * 数据处理管理
 * 
 * @author daic 20100401
 * 
 */
public abstract class DataProcessManager {
	protected Connection conn;

	protected PreparedStatement ps;

	public Connection getConn() {
		if (conn == null) {
			try {
				conn = C3P0ConnectionProvider.getInstance("").getConnection();
				if (conn.getAutoCommit()) {
					conn.setAutoCommit(false);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return conn;
	}

	protected DataProcessManager() {
		// System.out.println("DataProcessManager@@@@@@@@@@@@@@@@@");
	}

	protected void batchCommit() throws SQLException {
		conn.commit();
	}

	protected void rollback() throws SQLException {
		conn.rollback();
	}

	protected void closeConn() throws SQLException {
		if (conn != null) {
			conn.close();
		}
	}

	protected void closeRS(ResultSet rs) throws SQLException {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				throw e;
			}
		}

	}

	protected void closePS(PreparedStatement ps) throws SQLException {
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				throw e;
			}
		}

	}

	protected List<Map<String, Object>> getListFromRS(ResultSet rs)
			throws SQLException {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		while (rs.next()) {
			ResultSetMetaData rsm = rs.getMetaData();
			int size = rsm.getColumnCount();
			Map<String, Object> row = new HashMap<String, Object>();
			for (int j = 1; j <= size; j++) {
				row.put(rsm.getColumnLabel(j), rs.getObject(j));
			}
			list.add(row);
		}
		return list;
	}

	protected List<String> getColumnFromRS(ResultSet rs) throws SQLException {
		List<String> list = new ArrayList<String>();
		while (rs.next()) {
			list.add(rs.getString(1));
		}
		return list;
	}

	protected Map<String, Object> getResultFromRS(ResultSet rs)
			throws SQLException {
		Map<String, Object> row = new HashMap<String, Object>();
		if (rs.next()) {
			ResultSetMetaData rsm = rs.getMetaData();
			int size = rsm.getColumnCount();
			for (int j = 1; j <= size; j++) {
				row.put(rsm.getColumnLabel(j).toLowerCase(), rs.getObject(j));
			}
		}
		return row;
	}

	protected List<String> getOneResultList(ResultSet rs)
			throws SQLException {
		List<String> result = new ArrayList<String>();
		if (rs.next()) {
			result.add(rs.getString(1));
			
		}
		return result;
	}

	protected void setValues(PreparedStatement ps, int index, Object o)
			throws Exception {
		if (o instanceof String) {
			ps.setString(index, (String) o);
		} else if (o instanceof Float) {
			ps.setFloat(index, (Float) o);
		} else if (o instanceof Integer) {
			ps.setInt(index, (Integer) o);
		} else if (o instanceof BigDecimal) {
			ps.setBigDecimal(index, (BigDecimal) o);
		} else if (o instanceof java.sql.Date) {
			ps.setDate(index, (java.sql.Date) o);
		}

	}
}
