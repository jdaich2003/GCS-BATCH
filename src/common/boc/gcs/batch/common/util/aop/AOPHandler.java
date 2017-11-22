package boc.gcs.batch.common.util.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class AOPHandler implements InvocationHandler {
	private static Logger logger = Logger.getLogger(AOPHandler.class);
	private List interceptors = null;
	private Object originalObject;

	/**
	 * 返回动态代理实例
	 * 
	 * @param obj
	 * @return
	 */
	public Object bind(Object obj) {
		this.originalObject = obj;
		return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj
				.getClass().getInterfaces(), this);
	}

	/**
	 * 在Invoke方法中，加载对应的Interceptor，并进行
	 * 预处理(before)、后处理(after)以及异常处理（exceptionThrow）过程
	 */
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object result = null;
		Throwable ex = null;
		InvocationInfo invInfo = new InvocationInfo(proxy, method, args,
				result, ex, originalObject);
		if (method.getName() != null && method.getName().equals("read")) {
			logger.debug("Invoking Before Intercetpors!");
			invokeInterceptorsBefore(invInfo);
		}
		try {
			logger.debug("Invoking Proxy Method!");
			result = method.invoke(originalObject, args);
			invInfo.setResult(result);
			if (method.getName() != null && method.getName().equals("read")) {
				logger.debug("Invoking After Method!");
				invokeInterceptorsAfter(invInfo);
			}
		} catch (Exception e) {
			invInfo.setException(e);
			if (method.getName() != null && method.getName().equals("read")) {
				logger.debug("Invoking exceptionThrow Method!");
				invokeInterceptorsExceptionThrow(invInfo);
			}
			throw new AOPRuntimeException(e);
		} catch (Throwable tr) {
			invInfo.setException(tr);
			if (method.getName() != null && method.getName().equals("read")) {
				logger.debug("Invoking exceptionThrow Method!");
				invokeInterceptorsExceptionThrow(invInfo);
			}
			throw new AOPRuntimeException(tr);
		}
		return result;
	}

	/**
	 * 加载Interceptor
	 * 
	 * @return
	 */
	private synchronized List getIntercetors() {
		if (null == interceptors) {
			interceptors = new ArrayList();
			// Todo：读取配置，加载Interceptor实例,进行事务控制
			interceptors.add(new TransactionInterceptor());
		}
		return interceptors;
	}

	/**
	 * 执行预处理方法
	 * 
	 * @param invInfo
	 * @throws Exception
	 */
	private void invokeInterceptorsBefore(InvocationInfo invInfo)
			throws Exception {
		List interceptors = getIntercetors();
		int len = interceptors.size();
		for (int i = 0; i < len; i++) {
			((Interceptor) interceptors.get(i)).before(invInfo);
		}
	}

	/**
	 * 执行后处理方法
	 * 
	 * @param invInfo
	 * @throws Exception
	 */
	private void invokeInterceptorsAfter(InvocationInfo invInfo)
			throws Exception {
		List interceptors = getIntercetors();
		int len = interceptors.size();
		for (int i = len - 1; i >= 0; i--) {
			((Interceptor) interceptors.get(i)).after(invInfo);
		}
	}

	/**
	 * 执行异常处理方法
	 * 
	 * @param invInfo
	 * @throws Exception
	 */
	private void invokeInterceptorsExceptionThrow(InvocationInfo invInfo)
			throws Exception {
		List interceptors = getIntercetors();
		int len = interceptors.size();
		for (int i = len - 1; i >= 0; i--) {
			((Interceptor) interceptors.get(i)).exceptionThrow(invInfo);
		}
	}

	public void setInterceptors(List interceptors) {
		this.interceptors = interceptors;
	}
}