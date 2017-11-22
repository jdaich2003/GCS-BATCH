package boc.gcs.batch.common.util.aop;

import boc.gcs.batch.report.common.IReportReader;

public class AOPTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
			Class[] paramsType = {String.class};
			Object[] paramsValue = {"sssssssssssssssssssssss"};
			IReportReader handler = (IReportReader) AOPFactory
					.getAOPProxyedObject("boc.gcs.batch.common.util.aop.TestObject",paramsType,paramsValue);
//			handler.invoke(handler, TestObject.class.getMethod("test",
//				String.class), args);
			handler.read();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AOPRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
