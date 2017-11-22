package boc.gcs.batch.report.processdata;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import boc.gcs.batch.common.util.DataProcessManager;
import boc.gcs.batch.report.template.ReportTemplateCell;
import boc.gcs.batch.report.template.ReportTemplateLoader;

/**
 * 对账父类
 * 
 * @author daic
 * 
 */
public abstract class TransDetailCheckBill extends DataProcessManager {
	protected Logger logger;
	protected ReportTemplateLoader loader;
	protected PreparedStatement ps_gcs_query;
	protected ResultSet rs_gcs;

	protected PreparedStatement ps_to_check_query;
	protected ResultSet rs_to_check;

	protected PreparedStatement ps_state_update;

	protected PreparedStatement ps_unmatched_insert;

	protected PreparedStatement ps_unilateral_insert;

	protected PreparedStatement ps_matched_update;
	protected String sql_matched_update = "update gcs_transaction_info set ";
	private String sql_matched_update_condition = "where gcs_transaction_id = ?";
	private Object[] values;

	// 未达记录列表
	private List<Map<String, Object>> unreachedList;
	// 单边记录列表
	private List<Map<String, Object>> unilateralList;
	// 匹配记录列表
	private List<Map<String, Object>> matchedList;
	// 不匹配记录列表
	private List<Map<String, Object>> unmatchedList;

	protected TransDetailCheckBill(ReportTemplateLoader loader) {
		super.getConn();
		this.loader = loader;
		unreachedList = new ArrayList<Map<String, Object>>();
		unilateralList = new ArrayList<Map<String, Object>>();
		matchedList = new ArrayList<Map<String, Object>>();
		unmatchedList = new ArrayList<Map<String, Object>>();
	}

	/**
	 * 单条记录对账
	 * 
	 * @param transResult
	 * @throws Exception
	 */
	public void check(Map<String, Object> transResult) throws Exception {
		try {
			setValues(ps_gcs_query, 1, transResult.get("gcs_transaction_id"));
			rs_gcs = ps_gcs_query.executeQuery();
			Map<String, Object> gcsTransResult = getResultFromRS(rs_gcs);
			boolean matchFlag = true;
			if (gcsTransResult == null || gcsTransResult.size() == 0) {
				setValues(ps_unilateral_insert, 1, transResult
						.get("gcs_transaction_id"));
				ps_unilateral_insert.execute();
				// unilateralList.add(issTransResult);
			} else {

				for (ReportTemplateCell cell : loader.getCheckList()) {
					String key = cell.getBindKey();
					String type = cell.getType();
					// if ((transResult.get(key) != null &&
					// !(transResult.get(key))
					// .equals(gcsTransResult.get(key)))
					// || (gcsTransResult.get(key) != null && !(gcsTransResult
					// .get(key)).equals(transResult.get(key))))
					if (!checkValue(type, transResult.get(key), gcsTransResult
							.get(key))) {
						setValues(ps_unmatched_insert, 1, transResult
								.get("gcs_transaction_id"));
						ps_unmatched_insert.execute();
						// unmatchedList.add(issTransResult);

						// 修改状态为1
						setValues(ps_state_update, 1, "1");
						setValues(ps_state_update, 2, transResult
								.get("gcs_transaction_id"));
						ps_state_update.execute();
						matchFlag = false;
						break;
					}
				}

				if (matchFlag) {
					if(loader.getUpdateList().size()>0){
						values = new Object[loader.getUpdateList().size() + 1];
						for (int i = 0; i < loader.getUpdateList().size(); i++) {
							ReportTemplateCell cell = loader.getUpdateList().get(i);
							values[i] = transResult.get(cell.getBindKey());
						}
						
						values[loader.getUpdateList().size()] = transResult
								.get("gcs_transaction_id");
						for (int i = 0; i < values.length; i++) {
							setValues(ps_matched_update, i + 1, values[i]);
						}
					}
					// 修改状态为0
					setValues(ps_state_update, 1, "0");
					setValues(ps_state_update, 2, transResult
							.get("gcs_transaction_id"));
					// matchedList.add(issTransResult);
					ps_state_update.execute();
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			ps_unilateral_insert.clearParameters();
			ps_unmatched_insert.clearParameters();
			ps_state_update.clearParameters();
			ps_gcs_query.clearParameters();
			ps_matched_update.clearParameters();
			closeRS(rs_gcs);
		}
	}

	/**
	 * 根据类型进行对账（金额需转换成Double后对账）
	 * 
	 * @param type
	 * @param value1
	 * @param value2
	 * @return
	 * @throws Exception
	 */
	private boolean checkValue(String type, Object value1, Object value2)
			throws Exception {
		if (value1 != null && value2 != null) {
			if (type != null && type.equals("Double")) {
				return ((Double) value1).equals((Double) value2);
			} else {
				return value1.equals(value2);
			}
		} else if (value1 == null && value2 == null) {
			return true;
		}
		return false;
	}

	/**
	 * 更新方法
	 */
	protected void constructMatchUpdateSql(String setState) {
		sql_matched_update += setState;
		int i = 0;
		for (ReportTemplateCell cell : loader.getUpdateList()) {
			sql_matched_update += cell.getBindKey() + "=? ,";
			i++;
		}
		sql_matched_update += sql_matched_update_condition;

		System.out.println(sql_matched_update);
	}

	public List<Map<String, Object>> getUnreachedList() {
		return unreachedList;
	}

	public void setUnreachedList(List<Map<String, Object>> unreachedList) {
		this.unreachedList = unreachedList;
	}

	public List<Map<String, Object>> getUnilateralList() {
		return unilateralList;
	}

	public void setUnilateralList(List<Map<String, Object>> unilateralList) {
		this.unilateralList = unilateralList;
	}

	public List<Map<String, Object>> getMatchedList() {
		return matchedList;
	}

	public void setMatchedList(List<Map<String, Object>> matchedList) {
		this.matchedList = matchedList;
	}

	public List<Map<String, Object>> getUnmatchedList() {
		return unmatchedList;
	}

	public void setUnmatchedList(List<Map<String, Object>> unmatchedList) {
		this.unmatchedList = unmatchedList;
	}

	public static void main(String[] args) {

	}
}
