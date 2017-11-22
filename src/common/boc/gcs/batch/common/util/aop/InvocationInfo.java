package boc.gcs.batch.common.util.aop;

import java.lang.reflect.Method;

public class InvocationInfo {
	Object proxy;
	Method method;
	Object[] args;
	Object result;
	Throwable Exception;
	Object originalObject;

	public InvocationInfo(Object proxy, Method method, Object[] args,
			Object result, Throwable exception,Object originalObject) {
		super();
		this.proxy = proxy;
		this.method = method;
		this.args = args;
		this.result = result;
		Exception = exception;
		this.originalObject = originalObject;
	}

	public Object getOriginalObject() {
		return originalObject;
	}

	public void setOriginalObject(Object originalObject) {
		this.originalObject = originalObject;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public Throwable getException() {
		return Exception;
	}

	public void setException(Throwable exception) {
		Exception = exception;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object getProxy() {
		return proxy;
	}

	public void setProxy(Object proxy) {
		this.proxy = proxy;
	}
}
