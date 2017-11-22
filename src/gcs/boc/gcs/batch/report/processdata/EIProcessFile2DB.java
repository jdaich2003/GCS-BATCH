package boc.gcs.batch.report.processdata;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 清算报文数据入库
 * 
 * @author daic
 * 
 */
public class EIProcessFile2DB extends ProcessFile2DB {

	public EIProcessFile2DB() {
		logger = Logger.getLogger(EIProcessFile2DB.class);
		insert_sql = "insert into gcs_transaction_info_ei (";
		columns_sql = "select column_name from dba_tab_columns where table_name = 'GCS_TRANSACTION_INFO_EI'";
	}

	public void constructSql(Map<String, Object> oneRecord) {
		
		oneRecord.putAll(reader.getTitleContent());
		Set<String> set = oneRecord.keySet();
		value = new String[oneRecord.size() + 10];
		int i = 0;
		String refer_no = "";
		String deal_code = "";

		String insert_sql_local = insert_sql_temp;
		String values_str_local = values_str;
		for (String key : set) {
			//不取报文中的transaction_firit_branch_id
			if(key.equalsIgnoreCase("transaction_firit_branch_id")){
				continue;
			}
			for (String colName : columnsNameList) {
				if (key.equalsIgnoreCase(colName.toLowerCase())) {
					insert_sql_local += key + ",";
					values_str_local += "?,";
					value[i] = oneRecord.get(key);

					if (key.trim().equals("refer_no")) {
						refer_no = (String) oneRecord.get(key);
					}

					if (key.trim().equals("deal_code")) {
						deal_code = (String) oneRecord.get(key);
					}
					i++;
					break;
				}
			}
		}
		// 对账 文件中参考号refer_no（12位）放16位交易流水号的前12位
		// 对账 文件中系统跟踪号deal_code（6位）放16位交易流水号的后4位，前补00
		insert_sql_local += "gcs_transaction_id,";
		values_str_local += "?,";
		value[i++] = refer_no + StringUtils.substring(deal_code, 2);
		
		//插入机构号
		insert_sql_local += "transaction_firit_branch_id,";
		values_str_local += "?,";
		value[i++] = reader.getLoader().getConditions().get("bhId");
		
		if (insert_sql != null && !insert_sql.equals("")) {
			insert_sql = StringUtils.substringBeforeLast(insert_sql_local, ",")
					+ ") "
					+ StringUtils.substringBeforeLast(values_str_local, ",")
					+ ") ";
		}
		// System.out.println(insert_sql);
	}
}
