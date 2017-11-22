package boc.gcs.batch.coll.dealdata;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import boc.gcs.batch.coll.exception.DealDataException;
import boc.gcs.batch.common.util.Constants;
import boc.gcs.batch.common.util.DataProcessManager;
import boc.gcs.batch.common.util.PropertyUtil;

/**
 * 处理子线程业务逻辑
 * 
 * @author daic
 * 
 */
public class MakeBillsBiz extends DataProcessManager {
	private Logger logger = Logger.getLogger(MakeBillsBiz.class);
	private String mycrnt_bill_sql = "SELECT * FROM CBC_CRNTBILL WHERE SATISFIED_FLAG=0 and cycle_day<>0 and customer_num = ?";
	private String mycrnt_prevbill_sql = "SELECT * FROM CBC_CRNTBILL WHERE  SATISFIED_FLAG=0 and cycle_day=0 and customer_num = ?";
	// in_cycle_day
	private String _bill_sql = "with partdata as (SELECT * FROM "+PropertyUtil.getColl("db_link_table")+" WHERE cycle_day=? "+PropertyUtil.getColl("in_data")+" ORDER BY customer_num,bchid) select * from (select  rownum as rowno,partdata.* from  partdata ) where rowno >?and rowno<=?";
	private String _d_bill_sql = "with partdata as (SELECT * FROM "+PropertyUtil.getColl("db_link_table")+" WHERE cycle_day!=? "+PropertyUtil.getColl("in_data")+" ORDER BY customer_num,bchid) select * from (select  rownum as rowno,partdata.* from  partdata ) where rowno >?and rowno<=?";

	private String mycur_bill_sql = "with partdata as (SELECT * FROM CBC_BILL WHERE cycle_day=? ORDER BY customer_num,bchid) select * from (select  rownum as rowno,partdata.* from  partdata ) where rowno >?and rowno<=?";
	// in_cust_id
	private String mycif_customer_sql = "SELECT cifsn,lstnam,langid,idnum,idtyp,VIPTYP FROM CIF_CUSTOMER WHERE cstnum=RPAD(?,20,' ')";
	// in_cust_id
	private String mycif_account_sql = "SELECT (trim(BCHID) || trim(NWKID)) AS BCHID,PRODTYPID FROM CIF_ACCOUNT WHERE actnum=RPAD(?,20,' ')";

	// in_account_type
	private String my_cardstat_dict_sql = "SELECT card_kind_code FROM CBC_CARD_IDSTATE_SET WHERE UPPER(trim(id_state_code))=UPPER(trim(?))";

	// private String my_cbc_dict_sql = "SELECT NVL(dict_sort,0) FROM CBC_DICT
	// WHERE trim(dict_code)=trim(?) AND trim(dict_type)=trim(?)";

	private String my_cbc_dict_sql = "SELECT NVL(dict_sort,0) FROM gcs_uat.igcs_biz_args WHERE biz_type_prop_code=trim(?) AND biz_type_code=trim(?)";

	private String insert_pay_sql = "insert into IGCS_COLL_M_AMOUNT "
			+ "(CUNSTOMER_NUM,M_TYPE,MIN_PAY_AMOUNT,TOT_PAY_AMOUNT,PAY_AMOUNT,"
			+ "ORING_MINI_AMOUNT,ORING_TOT_AMOUNT,BCH_ID,PAY_DATE,cycle_day) "
			+ "values(?,?,?,?,?,?,?,?,?,?)";

	private String update_pay_sql = "update IGCS_COLL_M_AMOUNT set "
			+ "MIN_PAY_AMOUNT=?,TOT_PAY_AMOUNT=?,PAY_AMOUNT=?,"
			+ "ORING_MINI_AMOUNT=?,ORING_TOT_AMOUNT=?,PAY_DATE=?) "
			+ " where CUNSTOMER_NUM=? and M_TYPE=? and BCH_ID=? and cycle_day<>0";

	private String my_mtype_dict_sql = "SELECT biz_type_prop_code dict_code FROM "
			+ PropertyUtil.getColl("gcs_prefix")
			+ "igcs_biz_args WHERE  biz_type_code='CBC_M_TYPE'";

	private PreparedStatement mycrnt_bill_ps;
	private PreparedStatement mycrnt_prevbill_ps;
	private PreparedStatement _bill_ps;
	private PreparedStatement mycur_bill_ps;
	private PreparedStatement mycif_customer_ps;
	private PreparedStatement mycif_account_ps;

	private PreparedStatement my_cardstat_dict_ps;

	private PreparedStatement my_cbc_dict_ps;

	private PreparedStatement insert_ps;
	private PreparedStatement update_ps;

	private PreparedStatement insert_pay_ps;
	private PreparedStatement update_pay_ps;
	private MakeBillsParas paras;

	private Map<String, PayOfBillObject> payMap;

	private int batch_count_temp;

	public MakeBillsBiz(MakeBillsParas paras) throws Exception {
		try {
			this.paras = paras;
			this.conn = paras.getConn();
		} catch (Exception e1) {
			throw new DealDataException("initialize DealDataBizComm error!!",
					e1);
		}
	}
	public void executeR() throws Exception {
		catch_bill();
	}
	public void executeU() throws Exception {
		update_bill();
	}
	public void execute() throws Exception {
		mycur_bill();
	}
	private void update_bill() throws Exception {
		ResultSet rs = null;
		Statement stmLocal = conn.createStatement();
		Statement stm = conn.createStatement();
		batch_count_temp = 0;
		try {
			_bill_ps = conn.prepareStatement(_d_bill_sql);
			_bill_ps.setInt(1, GlobalParas.getIn_cycle_day());
			// 分页查询
			_bill_ps.setInt(2, paras.getStart());
			_bill_ps.setInt(3, paras.getEnd()); 
			rs = _bill_ps.executeQuery();
			while(rs.next()){
				StringBuilder sbU = new StringBuilder("UPDATE ").append(" CBC_BILL");
				StringBuilder sbM = new StringBuilder("UPDATE ").append("IGCS_COLL_M_AMOUNT");
				boolean b = false;
				String [] s = GlobalParas.CBC_BILL;
				String [] m = GlobalParas.IGCS_COLL_M_AMOUNT;
				double payAmount = 0.00;
				Date lsdate = null;
				String customer_num = "";
				String bill_id = "";
				if(rs.getBigDecimal("PAY_AMOUNT").doubleValue()>0){
					payAmount = rs.getBigDecimal("PAY_AMOUNT").doubleValue();
					lsdate = rs.getDate("LSTUPDDAT");
					customer_num = rs.getString("CUSTOMER_NUM");
					sbU.append(" SET ").append(" PAY_AMOUNT ").append("=").append("'").append(payAmount).append("'").append(",").append("LSTUPDDAT=").append("to_date('").append(lsdate).append("','yyyy-mm-dd')");
					sbU.append(" WHERE ").append(" BILL_ID").append("=").append("'").append(rs.getString("BILL_ID")).append("'");
					
					sbM.append(" SET ").append(" PAY_AMOUNT ").append("=").append(" PAY_AMOUNT").append("+").append("'").append(payAmount).append("'").append(" ,").append("PAY_DATE=").append("to_date('").append(lsdate).append("','yyyy-mm-dd')");
					sbM.append(" WHERE ").append("CUNSTOMER_NUM ").append("=").append("'").append(rs.getString("CUSTOMER_NUM")).append("'").append(" AND ");
					sbM.append("BCH_ID ").append("=").append("'").append(rs.getString("BCHID")).append("'").append(" AND ");
					sbM.append(" M_TYPE ").append("=").append("'").append(rs.getString("M_TYPE")).append("'");
					stmLocal.execute(sbU.toString());
					stm.execute(sbM.toString());
				}
			}
			batchCommit();
		} catch (Exception e) {
			logger.error("mycrnt_bill errer!", e);
			throw e;
		} finally {
			closePS(_bill_ps);
			closeRS(rs);
		}
	}
	private void catch_bill() throws Exception {
		ResultSet rs = null;
		Statement stmLocal = conn.createStatement();
		batch_count_temp = 0;
		try {
			_bill_ps = conn.prepareStatement(_bill_sql);
			_bill_ps.setInt(1, GlobalParas.getIn_cycle_day());
			// 分页查询
			_bill_ps.setInt(2, paras.getStart());
			_bill_ps.setInt(3, paras.getEnd()); 
			rs = _bill_ps.executeQuery();
			while(rs.next()){
				StringBuilder sbABegin = new StringBuilder("INSERT INTO ").append("CBC_BILL").append("(");
				StringBuilder sbAEnd = new StringBuilder(")values(");
				boolean b = false;
				String [] s = GlobalParas.CBC_BILL;
				for(int k=0;k < s.length;k++){
					if(!b){
						b = true;
						sbABegin.append(s[k]);
						sbAEnd.append("'").append(rs.getString(s[k])==null?"":rs.getString(s[k])).append("'");
					}else{
						sbABegin.append(",").append(s[k]);
						if(s[k].equals("COLLECTOR_ID")){
							sbAEnd.append(",").append("'").append("").append("'");
						}else if(s[k].equals("END_DATE") || s[k].equals("DUE_DAY")
								   || s[k].equals("ACCOUNT_DATE") || s[k].equals("LASTCALL_DATE")
								   || s[k].equals("NEXTCALL_DATE")|| s[k].equals("LSTUPDDAT")){
							sbAEnd.append(",").append("to_date('").append(rs.getDate(s[k])==null?"":rs.getDate(s[k])).append("','yyyy-mm-dd')");
						}
						
						else
						{
						sbAEnd.append(",").append("'").append(rs.getString(s[k])==null?"":rs.getString(s[k])).append("'");
						}
					}
				}
				try{
					stmLocal.execute(sbABegin.append(sbAEnd).append(")").toString());
				}catch(Exception e){
					System.out.println(sbABegin.toString());
					e.printStackTrace();
				}
			}
			batchCommit();
		} catch (Exception e) {
			logger.error("mycrnt_bill errer!", e);
			throw e;
		} finally {
			closePS(_bill_ps);
			closeRS(rs);
		}
	}
	private void mycur_bill() throws Exception {
		ResultSet rs = null;
		batch_count_temp = 0;
		try {
			mycur_bill_ps = conn.prepareStatement(mycur_bill_sql);
			mycur_bill_ps.setInt(1, GlobalParas.getIn_cycle_day());
			// 分页查询
			mycur_bill_ps.setInt(2, paras.getStart());
			mycur_bill_ps.setInt(3, paras.getEnd());
			rs = mycur_bill_ps.executeQuery();
			str_temp_customer_id = " ";
			str_sql = " ";
			strlastproduct = " ";
			intexecute = 1;
			strbigflag = "0";
			// 初始化额度信息
			paras.setPayList(this.getMtypeDict());
			// 存放待处理的账单列表
			payMap = new HashMap<String, PayOfBillObject>();
			while (rs.next()) {

				String customer_num = rs.getString("CUSTOMER_NUM");
				String bchid = rs.getString("bchid");

				SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
				account_date = f.format(rs.getDate("account_date"));
				strtel = rs.getString("TEL_CAUSE");
				/* 从字典表中取出 TEL_CAUSE 催收原因序号 */
				// intsorttel = this.my_cbc_dict(strtel, "CBC_TEL_CAUSE");
				/* 从 cif_account 表中获得相关信息 账户机构，账户产品ID */
				if (this.mycif_account(rs.getString("account_num"))) {
					intexecute = 1;
				} else {
					intexecute = 0;
					continue;
					// break;
				}

				mytemp_account_bchid = bchid.substring(0, 3) + "0000000";
				/* 从字典表中取出 PCLASS 产品序号 */
				// intsortproduct = this.my_cbc_dict(strproduct, "PCLASS");
				/* 从字典表中取出 CARDTYPE_ID 产品大类序号 */
				// this.my_cardstat_dict(rs.getString("ACCOUNT_TYPE"));
				// intsortcardtype = this.my_cbc_dict(strcardtype, "CARDTYPE");
				// 判断当前取到的cbc_bill信息是否属于同一机构下的同一个账户
				if ((null != customer_num && !customer_num
						.equals(str_temp_customer_id))
						|| (customer_num.equals(str_temp_customer_id)
								&& bchid != null && !(bchid.substring(0, 3))
								.equals(str_temp_bchid.substring(0, 3)))) {
					// 操作结果集之中上一个机构客户的信息
					updateOrInsertBill();
					// start 不属于同一个客户或者同一个客户不同机构时，构造insert语句
					str_temp_customer_id = rs.getString("CUSTOMER_NUM");
					str_temp_bchid = rs.getString("bchid");
					payMap.clear();
					str_sql = "insert into CBC_CRNTBILL(CIFSN,CUSTOMER_NUM,NAME,LANGID,IDNUM,IDTYP,CYCLE_DAY,VIPTYP,BCHID,BIGFLAG,DELINQUENT_DAYS,PERIOD,TEL_CAUSE,PRODUCT_ID,CARDTYPE_ID,ACCOUNT_DATE";
					if (nvl(rs.getString("BIGFLAG"), "0").equals("1")) {
						strbigflag = "1";
					} else {
						strbigflag = "0";
					}
					// 逾期天数
					intmaxdays = (Integer) nvl(Integer.valueOf(rs
							.getInt("DELINQUENT_DAYS")), Integer.valueOf(0));
					strmaxperiod = (String) nvl(rs.getString("PERIOD"), "01");
					/* 重新初始化各个币种的金额 */
					/* 计算各个币种的金额 */
					caculatePay(rs.getString("m_type"), rs
							.getFloat("min_pay_amount"), rs
							.getFloat("tot_pay_amount"), rs
							.getFloat("pay_amount"), rs
							.getFloat("original_mini_pay"), rs
							.getFloat("original_total_pay"), customer_num);
					/* 从 cif_customer 表中获得相关信息 */
					if (this.mycif_customer(str_temp_customer_id)) {
						intexecute = 1;
					} else {
						intexecute = 0;
						// 查不到cif_customer信息则处理下一条记录
						continue;
					}

					str_sql_values += "'" + trim(mytemp_account_bchid) + "',";
					intmaxtel = intsorttel;
					intmaxproduct = intsortproduct;
					intmaxcardtype = intsortcardtype;
					strlastproduct = strproduct;
					strlasttel = strtel;
					strlastcardtype = strcardtype;

					floatMaxTotalAmount = rs.getFloat("tot_pay_amount");
					// end 不属于同一个人时，构造insert语句
					// 属于同一个人则进行额度计算；并根据催收额度大小，重新计算逾期天数、账龄、催收原因、催收产品等信息
				} else if (customer_num.equals(str_temp_customer_id)
						&& bchid != null
						&& !(bchid.substring(0, 3)).equals(str_temp_bchid
								.substring(0, 3))) {
					/* 计算各个币种的金额 */
					caculatePay(rs.getString("m_type"), rs
							.getFloat("min_pay_amount"), rs
							.getFloat("tot_pay_amount"), rs
							.getFloat("pay_amount"), rs
							.getFloat("original_mini_pay"), rs
							.getFloat("original_total_pay"), customer_num);
					if (nvl(rs.getString("BIGFLAG"), "0").equals("1")) {
						strbigflag = "1";
					}
					// else {
					// strbigflag = "0";
					// }
					if (intmaxdays < (Integer) nvl(Integer.valueOf(rs
							.getInt("DELINQUENT_DAYS")), Integer.valueOf(0))) {
						intmaxdays = (Integer) nvl(Integer.valueOf(rs
								.getInt("DELINQUENT_DAYS")), Integer.valueOf(0));
					}
					if (Integer.valueOf(strmaxperiod) < (Integer) nvl(Integer
							.valueOf(rs.getInt("PERIOD")), Integer
							.valueOf("01"))) {
						strmaxperiod = (String) nvl(rs.getString("PERIOD"),
							"01");
					}

					if (floatMaxTotalAmount < rs.getFloat("tot_pay_amount")) {
						strlasttel = trim(rs.getString("TEL_CAUSE"));
						strlastproduct = strproduct;
						strlastcardtype = strcardtype;
						floatMaxTotalAmount = rs.getFloat("tot_pay_amount");
					}
					//					
					// if(intmaxtel<intsorttel){
					// intmaxtel = intsorttel;
					// strlasttel = trim(rs.getString("TEL_CAUSE"));
					// }
					// if(intmaxproduct < intsortproduct){
					// intmaxproduct = intsortproduct;
					// strlastproduct = strproduct;
					// }
					// if(intmaxcardtype < intsortcardtype){
					// intmaxcardtype = intsortcardtype;
					// strlastcardtype = strcardtype;
					// }
				}

				if (batch_count_temp == Constants.BATCH_COMMIT_COUNT) {
					batchCommit();
					batch_count_temp = 0;
				}
			}
			// 处理最后一个客户信息
			updateOrInsertBill();
			batchCommit();
		} catch (Exception e) {
			logger.error("mycrnt_bill errer!", e);
			throw e;
		} finally {
			closePS(mycur_bill_ps);
			closeRS(rs);
		}
	}

	/**
	 * 计算金额
	 * 
	 * @param m_type
	 * @param min_pay_amount
	 * @param tot_pay_amount
	 * @param pay_amount
	 * @param original_mini_pay
	 * @param original_total_pay
	 * @throws SQLException
	 */
	private void caculatePay(String m_type, float min_pay_amount,
			float tot_pay_amount, float pay_amount, float original_mini_pay,
			float original_total_pay, String customer_num) throws SQLException {
		String key = m_type.concat(mytemp_account_bchid).concat(customer_num);
		if (payMap.containsKey(key)) {
			PayOfBillObject payObj = payMap.get(key);
			payObj.setMin_pay(Float.valueOf(nvl(min_pay_amount, 0).toString())
					.floatValue()
					+ payObj.getMin_pay());
			payObj.setTot_pay(Float.valueOf(nvl(tot_pay_amount, 0).toString())
					.floatValue()
					+ payObj.getTot_pay());
			payObj.setPay_amount(Float.valueOf(nvl(pay_amount, 0).toString())
					.floatValue()
					+ payObj.getPay_amount());
			// payObj.setAcpt_pay_amount(Float.valueOf(
			// nvl(acpt_pay_amount,
			// 0).toString()).floatValue()+payObj.getAcpt_pay_amount());
			payObj.setOrg_min(Float.valueOf(
				nvl(original_mini_pay, 0).toString()).floatValue()
					+ payObj.getOrg_min());
			payObj.setOrg_tot(Float.valueOf(
				nvl(original_total_pay, 0).toString()).floatValue()
					+ payObj.getOrg_tot());
			payMap.put(key, payObj);
		} else {
			for (PayOfBillObject payObj : paras.getPayList()) {
				if (m_type != null && m_type.equals(payObj.getM_type())) {
					payObj.setMin_pay(Float.valueOf(
						nvl(min_pay_amount, 0).toString()).floatValue());
					payObj.setTot_pay(Float.valueOf(
						nvl(tot_pay_amount, 0).toString()).floatValue());
					payObj.setPay_amount(Float.valueOf(
						nvl(pay_amount, 0).toString()).floatValue());
					// payObj.setAcpt_pay_amount(Float.valueOf(
					// nvl(acpt_pay_amount, 0).toString()).floatValue());
					payObj.setOrg_min(Float.valueOf(
						nvl(original_mini_pay, 0).toString()).floatValue());
					payObj.setOrg_tot(Float.valueOf(
						nvl(original_total_pay, 0).toString()).floatValue());
					// 设置机构信息
					payObj.setBchid(mytemp_account_bchid);
					payObj.setCustomer_id(customer_num);
					payMap.put(key, payObj);
					break;
				}
			}
		}
	}

	/**
	 * query cbc dict
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	private int my_cbc_dict(String in_dict_code, String in_dict_type)
			throws Exception {
		ResultSet rs = null;
		int result = 0;
		try {
			my_cbc_dict_ps = conn.prepareStatement(my_cbc_dict_sql);
			my_cbc_dict_ps.setString(1, in_dict_code);
			my_cbc_dict_ps.setString(2, in_dict_type);
			rs = my_cbc_dict_ps.executeQuery();
			while (rs.next()) {
				result = rs.getInt(1);
				break;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			closeRS(rs);
			closePS(my_cbc_dict_ps);
		}
		return result;
	}

	/**
	 * 获取账户信息（机构号）
	 * 
	 * @param account_num
	 * @return
	 * @throws Exception
	 */
	private boolean mycif_account(String account_num) throws Exception {
		ResultSet rs = null;
		boolean found = false;
		try {
			mycif_account_ps = conn.prepareStatement(mycif_account_sql);
			mycif_account_ps.setString(1, account_num);
			rs = mycif_account_ps.executeQuery();

			while (rs.next()) {
				found = true;
				// 设置归户机构，取账户所在机构前3位，重要！
				// mytemp_account_bchid = rs.getString(1).substring(0, 3)
				// + "0000000";
				// mytemp_account_bchid = rs.getString(1);
				strproduct = rs.getString(2);
				break;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			closeRS(rs);
			closePS(mycif_account_ps);
		}
		return found;
	}

	private void my_cardstat_dict(String account_type) throws Exception {
		ResultSet rs = null;
		try {
			my_cardstat_dict_ps = conn.prepareStatement(my_cardstat_dict_sql);
			my_cardstat_dict_ps.setString(1, account_type);
			rs = my_cardstat_dict_ps.executeQuery();
			while (rs.next()) {
				strcardtype = rs.getString(1);
				break;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			closeRS(rs);
			closePS(my_cardstat_dict_ps);
		}
	}

	/**
	 * 从CIF_CUSTOMER表中取一些数据项值
	 * 
	 * @param customer_num
	 * @return
	 * @throws Exception
	 */
	private boolean mycif_customer(String customer_num) throws Exception {
		ResultSet rs = null;
		boolean found = false;
		try {
			mycif_customer_ps = conn.prepareStatement(mycif_customer_sql);
			mycif_customer_ps.setString(1, customer_num);
			rs = mycif_customer_ps.executeQuery();
			while (rs.next()) {
				found = true;
				mytemp_customer_cifsn = rs.getInt(1);
				mytemp_customer_name = rs.getString(2);
				mytemp_customer_langid = rs.getString(3);
				mytemp_customer_idnum = rs.getString(4);
				mytemp_customer_idtyp = rs.getString(5);
				mytemp_customer_viptyp = rs.getString(6);
				str_sql_values = "values('" + to_char(mytemp_customer_cifsn)
						+ "','" + trim(customer_num) + "','"
						+ trim(mytemp_customer_name) + "','"
						+ trim(mytemp_customer_langid) + "','"
						+ trim(mytemp_customer_idnum) + "','"
						+ trim(mytemp_customer_idtyp) + "','"
						+ to_char(GlobalParas.getIn_cycle_day()) + "','"
						+ trim(mytemp_customer_viptyp) + "',";
				intexecute = 1;

				break;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			closeRS(rs);
			closePS(mycif_customer_ps);
		}
		return found;
	}

	/**
	 * 从CBC_CRNTBILL表中SATISFIED_FLAG=0 and cycle_day=cycleday的历史记录中取一些数据值
	 * 
	 * @param customer_num
	 * @return
	 * @throws Exception
	 */
	private boolean mycrnt_bill(String customer_num) throws Exception {
		ResultSet rs = null;
		boolean found = false;
		try {
			mycrnt_bill_ps = conn.prepareStatement(mycrnt_bill_sql);
			// mycrnt_bill_ps.setInt(1, GlobalParas.getIn_cycle_day());
			mycrnt_bill_ps.setString(1, customer_num);
			rs = mycrnt_bill_ps.executeQuery();
			while (rs.next()) {
				found = true;
				if (rs.getString("CUR_BANK_CODE") == null
						|| rs.getString("CUR_BANK_CODE").trim().equals("")) {
					str_bank_code = "CUR_BANK_CODE=null,";
				} else {
					str_bank_code = "CUR_BANK_CODE='"
							+ rs.getString("CUR_BANK_CODE") + "',";
				}

				if (rs.getString("BIGFLAG") == null
						|| rs.getString("BIGFLAG").trim().equals("")) {
					str_bigflag_code = "BIGFLAG='" + strbigflag + "',";
				} else {
					if (strbigflag.equals("1")) {
						str_bigflag_code = "BIGFLAG='1'";
					} else {
						str_bigflag_code = "BIGFLAG='"
								+ nvl(rs.getString("BIGFLAG"), "0") + "',";
					}
				}

				if (rs.getString("GROUP_CODE") == null
						|| rs.getString("GROUP_CODE").trim().equals("")) {
					str_group_code = "GROUP_CODE=null,";
				} else {
					str_group_code = "GROUP_CODE='"
							+ rs.getString("GROUP_CODE") + "',";
				}

				if (rs.getString("COLLECTOR_ID") == null
						|| rs.getString("COLLECTOR_ID").trim().equals("")) {
					str_collector_id = "COLLECTOR_ID=null,";
				} else {
					str_collector_id = "COLLECTOR_ID='"
							+ rs.getString("COLLECTOR_ID") + "',";
				}

				if (strmaxperiod != null && Integer.valueOf(strmaxperiod) > 6) {
					strmaxperiod = "07";
				}
				if (Integer.valueOf(strmaxperiod) < new Integer((String) nvl(rs
						.getString("PERIOD"), "01"))) {
					strmaxperiod = (String) nvl(rs.getString("PERIOD"), "01");
				} else {
					if (rs.getString("cur_bank_code") == null
							|| rs.getString("cur_bank_code").equals("4900004")) {
						str_bank_code = "CUR_BANK_CODE=null,";
					}
					str_group_code = "GROUP_CODE=null,";
					str_collector_id = "COLLECTOR_ID=null";
				}

				intsorttel = this.my_cbc_dict(rs.getString("TEL_CAUSE"),
					"TEL_CAUSE");
				if (intmaxtel < intsorttel) {
					strlasttel = trim(rs.getString("TEL_CAUSE"));
				}

				intsortproduct = this.my_cbc_dict(rs.getString("PRODUCT_ID"),
					"PCLASS");
				if (intmaxproduct < intsortproduct) {
					strlastproduct = trim(rs.getString("PRODUCT_ID"));
				}

				intsortcardtype = this.my_cbc_dict(rs.getString("CARDTYPE_ID"),
					"CARDTYPE");
				if (intmaxcardtype <= intsortcardtype) {
					strlastcardtype = trim(rs.getString("CARDTYPE_ID"));
				} else {
					if (rs.getString("cur_bank_code") != null
							&& rs.getString("cur_bank_code").equals("4900004")) {
						str_bank_code = "CUR_BANK_CODE=null,";
					}
					str_group_code = "GROUP_CODE=null,";
					str_collector_id = "COLLECTOR_ID=null";
				}
				break;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			closeRS(rs);
			closePS(mycrnt_bill_ps);
		}
		return found;
	}

	/**
	 * 插入当前分行编码
	 * 
	 * @param customer_num
	 * @return
	 * @throws Exception
	 */
	private boolean mycrnt_prevbill(String customer_num) throws Exception {
		ResultSet rs = null;
		boolean found = false;
		try {
			mycrnt_prevbill_ps = conn.prepareStatement(mycrnt_prevbill_sql);
			mycrnt_prevbill_ps.setString(1, customer_num);
			rs = mycrnt_prevbill_ps.executeQuery();
			while (rs.next()) {
				found = true;
				if (rs.getString("CUR_BANK_CODE") != null
						&& !rs.getString("cur_bank_code").equals("4900004")) {
					str_sql = trim(str_sql) + ",CUR_BANK_CODE";
					str_sql_values = trim(str_sql_values)
							+ ",'"
							+ (rs.getString("cur_bank_code").substring(0, 3) + "0000000")
							+ "'";
				}
				break;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			closeRS(rs);
			closePS(mycrnt_prevbill_ps);
		}
		return found;
	}

	/**
	 * 插入一个用户的金额信息
	 * 
	 * @throws Exception
	 */
	private void insertPay() throws Exception {

		try {
			insert_pay_ps = conn.prepareStatement(insert_pay_sql);
			Set<String> set = payMap.keySet();

			for (Iterator key = set.iterator(); key.hasNext();) {
				PayOfBillObject payObj = (PayOfBillObject) payMap.get(key
						.next());
				insert_pay_ps.setString(1, payObj.getCustomer_id());
				insert_pay_ps.setString(2, payObj.getM_type());
				insert_pay_ps.setFloat(3, payObj.getMin_pay());
				insert_pay_ps.setFloat(4, payObj.getTot_pay());
				insert_pay_ps.setFloat(5, payObj.getPay_amount());
				insert_pay_ps.setFloat(6, payObj.getOrg_min());
				insert_pay_ps.setFloat(7, payObj.getOrg_tot());
				insert_pay_ps.setString(8, payObj.getBchid());
				// insert_pay_ps.setDate(9, new
				// java.sql.Date(System.currentTimeMillis()));
				insert_pay_ps.setDate(9, null);
				insert_pay_ps.setInt(10, GlobalParas.getIn_cycle_day());
				insert_pay_ps.addBatch();
			}
			insert_pay_ps.executeBatch();
		} catch (Exception e) {
			throw e;
		} finally {
			closePS(insert_pay_ps);
		}
	}

	/**
	 * 更新一个用户的金额信息
	 * 
	 * @throws Exception
	 */
	private void updatePay(String custom_id) throws Exception {

		try {
			update_pay_ps = conn.prepareStatement(update_pay_sql);
			Set<String> set = payMap.keySet();
			for (Iterator key = set.iterator(); key.hasNext();) {
				PayOfBillObject payObj = (PayOfBillObject) payMap.get(key
						.next());

				update_pay_ps.setFloat(1, payObj.getMin_pay());
				update_pay_ps.setFloat(2, payObj.getTot_pay());
				update_pay_ps.setFloat(3, payObj.getPay_amount());
				update_pay_ps.setFloat(4, payObj.getOrg_min());
				update_pay_ps.setFloat(5, payObj.getOrg_tot());
				// update_pay_ps.setDate(6, new
				// java.sql.Date(System.currentTimeMillis()));
				update_pay_ps.setDate(6, null);
				update_pay_ps.setString(7, payObj.getCustomer_id());
				update_pay_ps.setString(8, payObj.getM_type());
				update_pay_ps.setString(9, payObj.getBchid());
				// update_pay_ps.setInt(10,GlobalParas.getIn_cycle_day());

				System.out.println(payObj.getM_type() + ","
						+ "payObj.getMin_pay()");
				update_pay_ps.addBatch();
			}
			update_pay_ps.executeBatch();
			payMap = new HashMap<String, PayOfBillObject>();
		} catch (Exception e) {
			throw e;
		} finally {
			closePS(insert_pay_ps);
		}
	}

	/**
	 * 循环客户账单时，插入新记录或者更新已插入的记录
	 * 
	 * @throws Exception
	 */
	private void updateOrInsertBill() throws Exception {
		try {
			if (!str_sql.equals(" ") && intexecute == 1) {
				if (strmaxperiod != null && Integer.valueOf(strmaxperiod) > 6) {
					strmaxperiod = "07";
				}
				// 查询已插入的信息进行更新
				/*
				 * if (mycrnt_bill(str_temp_customer_id)) {
				 * 
				 * str_sql = "Update CBC_CRNTBILL SET " + str_bigflag_code +
				 * str_bank_code + str_group_code + str_collector_id + ",
				 * COLLECT_STATU=0,CYCLE_DAY=" +
				 * String.valueOf(GlobalParas.getIn_cycle_day()) +
				 * ",DELINQUENT_DAYS=" + String.valueOf(intmaxdays) + ",PERIOD= '" +
				 * trim(strmaxperiod) + "',TEL_CAUSE='" + trim(strlasttel) +
				 * "',PRODUCT_ID='" + trim(strlastproduct) + "',CARDTYPE_ID='" +
				 * trim(strlastcardtype) + "'"; // 金额更新，待处理！！！ // FOR inti IN 1 ..
				 * array_pay.COUNT LOOP // str_sql := trim(str_sql) ||
				 * ',MIN_PAY_AMOUNT_'... str_sql = trim(str_sql) + " where
				 * CUSTOMER_NUM='" + trim(str_temp_customer_id) + "' and
				 * cycle_day <> 0"; update_ps = conn.prepareStatement(str_sql);
				 * System.out.println(str_sql); // 插入金额
				 * updatePay(str_temp_customer_id); update_ps.execute();
				 * batch_count_temp++; // 如果没有则插入 } else {
				 */
				if (strmaxperiod != null && Integer.valueOf(strmaxperiod) <= 2) {
					// 插入当前分行编码 CUR_BANK_CODE
					mycrnt_prevbill(trim(str_temp_customer_id));

				}
				str_sql_values = trim(str_sql_values) + "'" + trim(strbigflag)
						+ "'," + String.valueOf(intmaxdays) + ",'"
						+ trim(strmaxperiod) + "','" + trim(strlasttel) + "','"
						+ trim(strlastproduct) + "','" + trim(strlastcardtype)
						+ "',to_date('" + account_date + "','YYMMDD')";
				str_sql = trim(str_sql) + ") " + trim(str_sql_values) + ")";
				// System.out.println(str_sql);
				// 执行插入语句
				insert_ps = conn.prepareStatement(str_sql);
				// 插入金额
				// System.out.println(str_sql);

				insertPay();
				insert_ps.execute();
				batch_count_temp++;
				/* } */
			}
		} catch (Exception e) {
			throw e;
		} finally {
			closePS(update_ps);
			closePS(insert_ps);
		}
	}

	private String to_char(int org) {
		return String.valueOf(org);
	}

	private String trim(String org) {
		if (org != null) {
			org = org.trim();
		} else {
			org = "";
		}
		return org;
	}

	private PreparedStatement my_mtype_dict_ps;

	/**
	 * 初始化账户额度信息
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	public List<PayOfBillObject> getMtypeDict() throws DealDataException {
		ResultSet rs = null;
		List<PayOfBillObject> mtypeDict = null;
		try {
			my_mtype_dict_ps = conn.prepareStatement(my_mtype_dict_sql);
			// System.out.println(my_mtype_dict_sql);
			rs = my_mtype_dict_ps.executeQuery();
			mtypeDict = new ArrayList<PayOfBillObject>();
			while (rs.next()) {
				String m_type = rs.getString("dict_code");
				PayOfBillObject payObj = new PayOfBillObject();
				payObj.setM_type(m_type);
				payObj.initialize();
				mtypeDict.add(payObj);
			}
		} catch (Exception e) {
			throw new DealDataException("getMtypeDict errer!", e);
		} finally {
			try {
				closePS(my_mtype_dict_ps);
			} catch (SQLException e) {
				throw new DealDataException("clearData errer!", e);
			}
		}
		return mtypeDict;
	}

	private Object nvl(Object a, Object defaltValue) {
		if (null == a) {
			return defaltValue;
		}
		return a;
	}

	private Float floatMaxTotalAmount;

	private String mytemp_account_bchid;
	private String strtel;
	private String strbigflag;
	private String strlasttel;
	private String str_bank_code;
	private String str_group_code;
	private String str_bigflag_code;
	private String str_collector_id;
	private String account_date;
	private String str_temp_customer_id;
	private String str_temp_bchid;

	private String str_sql;
	private String str_sql_values;
	private String strmaxperiod;
	private int m_type_count;
	private int intexecute;
	private int intmaxdays;
	private int intsorttel;
	private int intsortproduct;
	private int intsortcardtype;
	private int intmaxtel;
	private int intmaxproduct;
	private int intmaxcardtype;
	// CIF_ACCOUNT.PRODTYPID%TYPE
	private String strproduct;
	// CIF_ACCOUNT.PRODTYPID%TYPE
	private String strlastproduct;
	// CBC_CARD_IDSTATE_SET.card_kind_code%TYPE
	private String strcardtype = "";
	// CBC_CARD_IDSTATE_SET.card_kind_code%TYPE
	private String strlastcardtype;
	// mytemp_bill_data CBC_BILL%ROWTYPE;
	// CIF_CUSTOMER.lstnam%TYPE
	String mytemp_customer_name;
	// CIF_CUSTOMER.langid%TYPE
	String mytemp_customer_langid;
	// CIF_CUSTOMER.idnum%TYPE
	String mytemp_customer_idnum;
	// CIF_CUSTOMER.idtyp%TYPE
	String mytemp_customer_idtyp;
	// CIF_CUSTOMER.cifsn%TYPE
	int mytemp_customer_cifsn;
	// CIF_CUSTOMER.viptyp%TYPE
	String mytemp_customer_viptyp;
}
