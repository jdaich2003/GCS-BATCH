package boc.gcs.batch.common.util.aop;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

public class AOPFactory {
	private static Logger logger = Logger.getLogger(AOPFactory.class);

	/**
	 * 根据类名创建类实例
	 * 
	 * @param clzName
	 * @return
	 * @throws AOPRuntimeException
	 * @throws ClassNotFoundException
	 */
	public static Object getClassInstance(String clzName, Class[] paramsType, Object[] paramsValue)
			throws AOPRuntimeException {
		Class cls;
		Constructor constructor;
		Object object = null;
		try {
			cls = Class.forName(clzName);
			constructor = cls.getConstructor(paramsType);
			object = (Object) constructor.newInstance(paramsValue);
			// return (Object) cls.newInstance();
		} catch (ClassNotFoundException e) {
			logger.debug(e);
			throw new AOPRuntimeException(e);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return object;
	}

	/**
	 * 根据传入的类名，返回AOP代理对象
	 * 
	 * @param clzName
	 * @return
	 * @throws AOPRuntimeException
	 */
	public static Object getAOPProxyedObject(String clzName,
			Class[] paramsType, Object[] paramsValue)
			throws AOPRuntimeException {
		AOPHandler txHandler = new AOPHandler();
		Object obj = getClassInstance(clzName,paramsType, paramsValue);
		return txHandler.bind(obj);
	}
	
	/**
	 * 根据传入的类名，返回AOP代理对象
	 * 
	 * @param clzName
	 * @return
	 * @throws AOPRuntimeException
	 */
	public static Object getAOPProxyedObject(String clzName)
			throws AOPRuntimeException {
		AOPHandler txHandler = new AOPHandler();
		Object obj = getClassInstance(clzName,null, null);
		return txHandler.bind(obj);
	}
}
