package boc.gcs.batch.report.processdata;

import org.apache.log4j.Logger;


/**
 * 发卡报文数据入库
 * 
 * @author daic
 * 
 */
public class ISSProcessFile2DB extends ProcessFile2DB {

	public ISSProcessFile2DB() {
		logger = Logger.getLogger(ISSProcessFile2DB.class);
		insert_sql = "insert into gcs_transaction_info_iss (";
		columns_sql = "select column_name from dba_tab_columns where table_name = 'GCS_TRANSACTION_INFO_ISS'";
	}
}
