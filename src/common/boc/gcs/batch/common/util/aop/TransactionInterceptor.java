package boc.gcs.batch.common.util.aop;

import java.lang.reflect.Method;
import java.sql.Connection;

import org.apache.log4j.Logger;

import boc.gcs.batch.common.db.C3P0ConnectionProvider;

public class TransactionInterceptor implements Interceptor {
	private static Logger logger = Logger.getLogger(MyInterceptor.class);

	public void before(InvocationInfo invInfo) throws Exception {
		Method method = invInfo.getOriginalObject().getClass().getSuperclass()
				.getDeclaredMethod("getConn");
		if (!method.isAccessible()) {
			method.setAccessible(true);
		}
		method.invoke(invInfo.getOriginalObject());

		logger.debug("Pre-processing transaction started");
	}

	public void after(InvocationInfo invInfo) throws Exception {
		Method method = invInfo.getOriginalObject().getClass().getSuperclass()
				.getDeclaredMethod("getConn");
		if (!method.isAccessible()) {
			method.setAccessible(true);
		}
		Connection conn = (Connection) method.invoke(invInfo
				.getOriginalObject());
		conn.commit();
		logger.debug("Post-processing transaction commit");
	}

	public void exceptionThrow(InvocationInfo invInfo) throws Exception {
		Method method = invInfo.getOriginalObject().getClass().getSuperclass()
				.getDeclaredMethod("getConn");
		if (!method.isAccessible()) {
			method.setAccessible(true);
		}
		Connection conn = (Connection) method.invoke(invInfo
				.getOriginalObject());
		conn.rollback();
		logger.debug("Exception-processing transaction rollback");
	}
}
