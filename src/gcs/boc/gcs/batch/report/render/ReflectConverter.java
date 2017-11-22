package boc.gcs.batch.report.render;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author daic
 * 
 */
public class ReflectConverter {
	/**
	 * 
	 * @param convert
	 *            形如method(paramName1,paramName2)
	 * @param params
	 *            方法参数值
	 * @return
	 * @throws Exception
	 * @throws Throwable
	 */
	public static String getValue(String convertName, Map<String, String> paramsMap)
			throws Exception {
		String[] paramsName = null;
		String[] paramsValue = null;
		String value = null;
		Class[] paramsClass = null;
		if (convertName.indexOf("(") > 0) {
			paramsName = convertName.substring(
				convertName.lastIndexOf("(") + 1, convertName.lastIndexOf(")"))
					.split(",");

			paramsClass = new Class[paramsName.length];
			paramsValue = new String[paramsName.length];
			for (int i = 0; i < paramsName.length; i++) {
				paramsClass[i] = String.class;
				if(paramsName[i].indexOf("\"")>=0){
					paramsValue[i]=StringUtils.substringBetween(paramsName[i], "\"");
				}else{
					paramsValue[i] = paramsMap.get(paramsName[i]);
				}
				
			}
			String methodName = convertName.substring(0, convertName
					.lastIndexOf("("));

			Method m = ReportConverter.class.getMethod(methodName, paramsClass);
			value = (String) m.invoke(ReportConverter.getInstance(),
				paramsValue);

		} else {
			String methodName = convertName;
			Method m = ReportConverter.class.getMethod(methodName);
			value = (String) m.invoke(ReportConverter.getInstance());
		}
		return value;

	}
}
