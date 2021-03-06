package boc.gcs.batch.report.processdata;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import boc.gcs.batch.report.template.ReportTemplateLoader;
import boc.gcs.batch.report.template.ReportTemplateParser;

/**
 * iss对账
 * 
 * @author daic
 * 
 */
public class ISSTransDetailCheckBill extends TransDetailCheckBill {

	private PreparedStatement ps_unreached_update;

	public ISSTransDetailCheckBill(ReportTemplateLoader loader) {
		super(loader);
		logger = Logger.getLogger(ISSTransDetailCheckBill.class);
	}

	public void checkBill() throws Exception {
		try {
			ps_gcs_query = conn.prepareStatement(ReportTemplateParser
					.getInstance().getSql("SELECT_TRANSACTION_INFO"));
			ps_to_check_query = conn.prepareStatement(ReportTemplateParser
					.getInstance().getSql("SELECT_TRANSACTION_INFO_ISS"));
			ps_unilateral_insert = conn.prepareStatement(ReportTemplateParser
					.getInstance().getSql("INSERT_TRANSACTION_INFO_ISS_UNILA"));
			ps_unmatched_insert = conn.prepareStatement(ReportTemplateParser
					.getInstance().getSql("INSERT_TRANSACTION_INFO_ISS_UM"));
			ps_state_update = conn.prepareStatement(ReportTemplateParser
					.getInstance().getSql("UPDATE_TRANSACTION_INFO_STATE_ISS"));
			ps_matched_update = conn.prepareStatement(sql_matched_update);

			// 匹配后的更新
			String setState = " check_flag_iss = '0' ";
			constructMatchUpdateSql(setState);

			// 查出所有iss发送来的数据，逐条对账
			setValues(ps_to_check_query, 1, (String) loader.getConditions()
					.get("bhId"));
			// setValues(ps_to_check_query, 2, loader.getCond().getDateStart());
			rs_to_check = ps_to_check_query.executeQuery();
			while (rs_to_check.next()) {
				Map<String, Object> row = new HashMap<String, Object>();
				ResultSetMetaData rsm = rs_to_check.getMetaData();
				int size = rsm.getColumnCount();
				for (int j = 1; j <= size; j++) {
					row.put(rsm.getColumnLabel(j).toLowerCase(), rs_to_check
							.getObject(j));
				}
				check(row);
			}

			checkUnreached();

			PreparedStatement ps_delete = conn
					.prepareStatement("delete from gcs_transaction_info_iss");
			ps_delete.execute();
			batchCommit();
			closePS(ps_delete);
		} catch (Exception e) {
			logger.error("one record check error", e);
			rollback();
			throw e;
		} finally {
			closePS(ps_unilateral_insert);
			closePS(ps_unmatched_insert);
			closePS(ps_state_update);
			closePS(ps_matched_update);
			closeRS(rs_gcs);
			closePS(ps_gcs_query);
			closeRS(rs_to_check);
			closePS(ps_to_check_query);
			
			closeConn();
		}
	}

	/**
	 * 查询所有未达信息并置为未达状态，挂账周期加1
	 * 
	 * @throws Exception
	 */
	public void checkUnreached() throws Exception {
		try {
			// ps_unreached_query = conn.prepareStatement(ReportTemplateParser
			// .getInstance().getSql("SELECT_TRANSACTION_INFO_UNREACH_ISS"));
			//			
			// ps_unreached_query.setString(1,
			// (String)loader.getConditions().get("bhId"));
			// //本交易日的交易信息？
			// ps_unreached_query.setString(2,
			// (String)loader.getConditions().get("date"));
			//			
			// ps_unreached_query.setString(3,
			// (String)loader.getConditions().get("bhId"));
			// ps_unreached_query.setString(4,
			// (String)loader.getConditions().get("date"));
			// rs_unreached_query = ps_unreached_query.executeQuery();
			// if (rs_unreached_query.next()) {
			ps_unreached_update = conn.prepareStatement(ReportTemplateParser
					.getInstance()
					.getSql("UPDATE_TRANSACTION_INFO_UNREACH_ISS"));

			ps_unreached_update.setString(1, (String) loader.getConditions()
					.get("bhId"));
			// 本交易日的交易信息？
			ps_unreached_update.setString(2, (String) loader.getConditions()
					.get("date"));

			ps_unreached_update.setString(3, (String) loader.getConditions()
					.get("bhId"));
			ps_unreached_update.setString(4, (String) loader.getConditions()
					.get("date"));
			ps_unreached_update.execute();
			// }

		} catch (Exception e) {
			logger.error("one record check error", e);
			super.rollback();
			throw e;
		} finally {
			// closeRS(rs_unreached_query);
			// closePS(ps_unreached_query);
			closePS(ps_unreached_update);
		}

	}

	public static void main(String[] args) {

	}
}
