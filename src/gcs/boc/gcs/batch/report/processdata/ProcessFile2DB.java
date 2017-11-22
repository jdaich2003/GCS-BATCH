package boc.gcs.batch.report.processdata;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import boc.gcs.batch.common.util.DataProcessManager;
import boc.gcs.batch.report.common.IDataProcess;
import boc.gcs.batch.report.common.IReportReader;

public class ProcessFile2DB extends DataProcessManager implements IDataProcess {
	protected PreparedStatement ps;
	protected IReportReader reader;
	protected String insert_sql = "";
	protected String columns_sql = "";
	protected PreparedStatement ps_columns;
	protected String values_str = " values(";
	protected Object[] value;
	protected List<String> columnsNameList;
	protected List<String> valueSeq;
	
	protected String insert_sql_temp = "";
	protected Logger logger ;
	

	public void init(IReportReader reader) {
		this.reader = reader;
		conn = reader.getConn();
	}

	public void process(Map<String, Object> oneRecord) throws Exception {
		try {
			
			insert_sql_temp = insert_sql;
			if (columnsNameList == null) {
				ps_columns = conn.prepareStatement(columns_sql);
				columnsNameList = getColumnFromRS(ps_columns.executeQuery());
			}
			constructSql(oneRecord);
			if (ps == null) {
				ps = conn.prepareStatement(insert_sql);
			}
			for (int i = 0; i < value.length; i++) {
				setValues(ps, i + 1, value[i]);
			}
			ps.execute();
		} catch (Exception e) {
			throw e;
		} finally {
			ps.clearParameters();
			closePS(ps_columns);
		}
	}

	protected void constructSql(Map<String, Object> oneRecord) {
		//从表头取日期、时间、机构等公共信息
		oneRecord.putAll(reader.getTitleContent());
		Set<String> set = oneRecord.keySet();
		value = new Object[oneRecord.size()+10];
		String insert_sql_local = insert_sql_temp;
		String values_str_local = values_str;
		int i =0;
		for (String key : set) {
			for (String colName : columnsNameList) {
				if (key.equalsIgnoreCase(colName.toLowerCase())) {
					insert_sql_local += key + ",";
					values_str_local += "?,";
					value[i] = oneRecord.get(key);
					i++;
					break;
				}
			}
		}
		//插入机构号
		insert_sql_local += "transaction_firit_branch_id,";
		values_str_local += "?,";
		value[i] = reader.getLoader().getConditions().get("bhId");
		
		if(insert_sql!=null&&insert_sql.equals(insert_sql_temp)){
			insert_sql = StringUtils.substringBeforeLast(insert_sql_local, ",") + ") "
					+ StringUtils.substringBeforeLast(values_str_local, ",")+") ";
		}
		logger.debug(insert_sql);
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		if (ps != null) {
			ps.close();
		}
	}
}
