package boc.gcs.batch.report.common;

import java.util.Map;

public interface IDataProcess {
	
	/**
	 * 初始化数据处理类，注册报表reader
	 * @param reader
	 */
	public void init(IReportReader reader);
	/**
	 * 逐条处理明细数据，目的是不加载数据到内存
	 * @param oneRecord
	 * @throws Exception
	 */
	public void process(Map<String, Object> oneRecord) throws Exception;
}
