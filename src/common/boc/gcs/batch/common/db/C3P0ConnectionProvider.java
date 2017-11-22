package boc.gcs.batch.common.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import boc.gcs.batch.common.util.Constants;
import boc.gcs.batch.common.util.PropertyUtil;

import com.mchange.v2.c3p0.DataSources;

/**
 * C3P0数据库连接池封装类
 * 
 * @author daic 20100401 
 * 
 */
public class C3P0ConnectionProvider {

	private static final Logger log = Logger
			.getLogger(C3P0ConnectionProvider.class);

	private final static String C3P0_STYLE_MIN_POOL_SIZE = "c3p0.minPoolSize";

	private final static String C3P0_STYLE_MAX_POOL_SIZE = "c3p0.maxPoolSize";

	private final static String C3P0_STYLE_MAX_IDLE_TIME = "c3p0.maxIdleTime";

	private final static String C3P0_STYLE_MAX_STATEMENTS = "c3p0.maxStatements";

	private final static String C3P0_STYLE_ACQUIRE_INCREMENT = "c3p0.acquireIncrement";

	private final static String C3P0_STYLE_IDLE_CONNECTION_TEST_PERIOD = "c3p0.idleConnectionTestPeriod";

	private final static String C3P0_STYLE_TEST_CONNECTION_ON_CHECKOUT = "c3p0.testConnectionOnCheckout";

	private final static String C3P0_STYLE_INITIAL_POOL_SIZE = "c3p0.initialPoolSize";

	private DataSource ds;

	private Integer isolation;

	private boolean autocommit;
	
	private static String dbPref = "";

	private static C3P0ConnectionProvider c3pcp = null;

	private C3P0ConnectionProvider() {
		try {
			configure();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public static C3P0ConnectionProvider getInstance(String dbPrefix)
			throws Exception {
		if (c3pcp == null) {
			if(dbPrefix==null||dbPrefix.equals("")){
				throw new Exception("请通过C3P0ConnectionProvider.getInstance(dbPrefix),指定数据库前缀");
			}
			dbPref = dbPrefix;
			c3pcp = new C3P0ConnectionProvider();
			return c3pcp;
		} else
			return c3pcp;
	}

	/**
	 * 获得连接
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		log.debug("获得连接开始");
		Connection c = ds.getConnection();
		log.debug("获得连接结束:" + c.toString());
		if (isolation != null) {
			c.setTransactionIsolation(isolation.intValue());
		}
		if (c.getAutoCommit() != autocommit) {
			c.setAutoCommit(autocommit);
		}
		return c;
	}

	/**
	 * 关闭连接
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	public void closeConnection(Connection conn) throws SQLException {
		conn.close();
	}

	/**
	 * 初始化连接池
	 * 
	 * @throws Exception
	 */
	private void configure() throws Exception {
		

		String jdbcDriverClass = PropertyUtil.getJDBC(dbPref+"_jdbc.driver");
		String jdbcUrl = PropertyUtil.getJDBC(dbPref+"_jdbc.url");
		String userName = PropertyUtil.getJDBC(dbPref+"_jdbc.username");
		String password = PropertyUtil.getJDBC(dbPref+"_jdbc.password");
		// 设置连接属性
		Properties props = new Properties();
		props.setProperty("user", userName);
		props.setProperty("password", password);
		props.setProperty("URL", jdbcUrl);
		log.info("C3P0 using driver: " + jdbcDriverClass + " at URL: "
				+ jdbcUrl);

		if (jdbcDriverClass == null) {
			log.warn("No JDBC Driver class was specified by property "
					+ PropertyUtil.getJDBC("jdbc.driver"));
		} else {
			try {
				Class.forName(jdbcDriverClass);
			} catch (ClassNotFoundException cnfe) {

			}
		}

		try {

			Integer minPoolSize = new Integer(1);
			Integer maxPoolSize = new Integer(50);
			Integer maxIdleTime = new Integer(50000);
			// Integer maxStatements = new Integer(50);
			// Integer acquireIncrement = new Integer(50);
			// Integer idleTestPeriod = new Integer(50);
			Properties c3props = new Properties();

			setOverwriteProperty(C3P0_STYLE_MIN_POOL_SIZE, c3props, minPoolSize);
			setOverwriteProperty(C3P0_STYLE_MAX_POOL_SIZE, c3props, maxPoolSize);
			setOverwriteProperty(C3P0_STYLE_MAX_IDLE_TIME, c3props, maxIdleTime);
			// setOverwriteProperty(C3P0_STYLE_MAX_STATEMENTS, c3props,
			// maxStatements);
			// setOverwriteProperty(C3P0_STYLE_ACQUIRE_INCREMENT, c3props,
			// acquireIncrement);
			// setOverwriteProperty(C3P0_STYLE_IDLE_CONNECTION_TEST_PERIOD,
			// c3props, idleTestPeriod);

			Integer initialPoolSize = getInteger(C3P0_STYLE_INITIAL_POOL_SIZE,
					props);
			if (initialPoolSize == null && minPoolSize != null) {
				c3props.put(C3P0_STYLE_INITIAL_POOL_SIZE, String.valueOf(
						minPoolSize).trim());
			}

			DataSource unpooled = DataSources
					.unpooledDataSource(jdbcUrl, props);

			Properties allProps = (Properties) props.clone();
			allProps.putAll(c3props);

			ds = DataSources.pooledDataSource(unpooled, allProps);
		} catch (Exception e) {
			log.fatal("could not instantiate C3P0 connection pool", e);
			throw new Exception("Could not instantiate C3P0 connection pool", e);
		}

	}

	public void close() {
		try {
			DataSources.destroy(ds);
		} catch (SQLException sqle) {
			log.warn("could not destroy C3P0 connection pool", sqle);
		}
	}

	private static void setOverwriteProperty(String c3p0StyleKey,
			Properties c3p, Integer value) {
		if (value != null) {
			c3p.put(c3p0StyleKey, String.valueOf(value).trim());

		}
	}

	private static Integer getInteger(String propertyName, Properties properties) {
		String value = extractPropertyValue(propertyName, properties);
		return value == null ? null : Integer.valueOf(value);
	}

	private static String extractPropertyValue(String propertyName,
			Properties properties) {
		String value = properties.getProperty(propertyName);
		if (value == null) {
			return null;
		}
		value = value.trim();
		if (isEmpty(value)) {
			return null;
		}
		return value;
	}

	private static boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}
}
