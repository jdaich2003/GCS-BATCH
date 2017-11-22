package boc.gcs.batch.common.util.aop;

public interface Interceptor {
	public void before(InvocationInfo invInfo) throws Exception;
	public void after(InvocationInfo invInfo) throws Exception;
	public void exceptionThrow(InvocationInfo invInfo) throws Exception;
	}
