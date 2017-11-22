package boc.gcs.batch.common.util.aop;

import org.apache.log4j.Logger;

public class MyInterceptor implements Interceptor{
	private static Logger logger = Logger.getLogger(MyInterceptor.class);
	public void before(InvocationInfo invInfo) {
	logger.debug("Pre-processing");
	}
	public void after(InvocationInfo invInfo) {
	logger.debug("Post-processing");
	}
	public void exceptionThrow(InvocationInfo invInfo) {
	logger.debug("Exception-processing");
	}
	}
