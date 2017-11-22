package boc.gcs.batch.common.util;

import java.util.HashMap;
import java.util.Map;

import boc.gcs.batch.report.render.ReflectConverter;

public class ListMapTest {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws SecurityException 
	 */
	public static void main(String[] args) throws  Exception {
		// TODO Auto-generated method stub
//		ListOrderedMap map = new ListOrderedMap();
//		map.put("b", "bbb");
//		map.put("a", "aaa");
//		
//		map.put("c", "ccc");
//		for (int i = 0; i < map.size(); i++) {
//			System.out.println(map.get(map.get(i)));
//		}
//		ReportTemplateParser.getInstance().getConfig("aaa");
//		BigDecimal a = new BigDecimal(new BigInteger("444400"),2);
		String convertName = "convertEIAmount(\"D234234234\")";
		Map<String,String> paramsMap = new HashMap<String,String>();
//		paramsMap.put("square_amount", "D234234234");
		
		String value = ReflectConverter.getValue(convertName, paramsMap);
		
		System.out.println(value);
	}

}
