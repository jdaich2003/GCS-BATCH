package boc.gcs.batch.coll.dealdata;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import boc.gcs.batch.coll.exception.DealDataException;
import boc.gcs.batch.common.util.Constants;
import boc.gcs.batch.common.util.DataProcessManager;
import boc.gcs.batch.common.util.PropertyUtil;

/**
 * 处理主线程业务逻辑
 * 
 * @author daic
 * 
 */
public class MakeBillsBizComm extends DataProcessManager {
	private String del_crntbill_sql = "delete from CBC_CRNTBILL where cycle_day=0";
	// in_cycle_day
	private String upd_crntbill_sql = "update CBC_CRNTBILL set  cycle_day=0 where cycle_day= TO_CHAR(?)";

	private String upd_COLL_M_AMOUNT_sql = "update IGCS_COLL_M_AMOUNT set  cycle_day=0 where cycle_day= TO_CHAR(?)";
	// in_cycle_day
	private String del_bill_sql = "delete from CBC_BILL where TEL_CAUSE='02' And bill_state='1' And cycle_day= TO_CHAR(?)";
	//delete local data in_cycle_day
	private String _bill_sql = "delete from CBC_BILL where  cycle_day= TO_CHAR(?)";
	//update local data pay_amount is null
	private String _bill_update_m = "update IGCS_COLL_M_AMOUNT set  pay_amount=0 where cycle_day!= TO_CHAR(?)";


	private String _bill_count_sql = "SELECT count(1) FROM "+PropertyUtil.getColl("db_link_table")+" WHERE cycle_day=? "+PropertyUtil.getColl("in_data") ;
	private String _bill_d_count_sql = "SELECT count(1) FROM "+PropertyUtil.getColl("db_link_table")+" WHERE cycle_day!=? "+PropertyUtil.getColl("in_data") ;
	private String mycur_bill_count_sql = "SELECT count(1) FROM CBC_BILL WHERE cycle_day=?  ORDER BY customer_num";
	private String _bill_rownum_sql = "with partdata as (SELECT customer_num FROM "+PropertyUtil.getColl("db_link_table")+" WHERE cycle_day=? "+PropertyUtil.getColl("in_data")+"  ORDER BY customer_num) select customer_num from (select  rownum as rowno,partdata.* from  partdata ) where rowno = ?";
	private String _bill_d_rownum_sql = "with partdata as (SELECT customer_num FROM "+PropertyUtil.getColl("db_link_table")+" WHERE cycle_day!=? "+PropertyUtil.getColl("in_data")+"  ORDER BY customer_num) select customer_num from (select  rownum as rowno,partdata.* from  partdata ) where rowno = ?";
	private String mycur_bill_rownum_sql = "with partdata as (SELECT customer_num FROM CBC_BILL WHERE cycle_day=?   ORDER BY customer_num) select customer_num from (select  rownum as rowno,partdata.* from  partdata ) where rowno = ?";
	private String _bill_scope_sql = "with partdata as (SELECT customer_num FROM "+PropertyUtil.getColl("db_link_table")+" WHERE cycle_day=? "+PropertyUtil.getColl("in_data")+" ORDER BY customer_num) select count(1) from (select  rownum as rowno,partdata.* from  partdata ) where customer_num>  ? and customer_num<=?";
	private String _bill_d_scope_sql = "with partdata as (SELECT customer_num FROM "+PropertyUtil.getColl("db_link_table")+" WHERE cycle_day!=? "+PropertyUtil.getColl("in_data")+" ORDER BY customer_num) select count(1) from (select  rownum as rowno,partdata.* from  partdata ) where customer_num>  ? and customer_num<=?";
	private String mycur_bill_scope_sql = "with partdata as (SELECT customer_num FROM CBC_BILL WHERE cycle_day=?   ORDER BY customer_num) select count(1) from (select  rownum as rowno,partdata.* from  partdata ) where customer_num>  ? and customer_num<=?";

	private String clear_end_sql = "delete from CBC_CRNTBILL where cycle_day=0";
	private String clear_end_pay_sql = "delete from IGCS_COLL_M_AMOUNT where cycle_day=0";

	private PreparedStatement del_crntbill_ps;
	private PreparedStatement upd_crntbill_ps;
	private PreparedStatement upd_COLL_M_AMOUNT_ps;
	private PreparedStatement _bill_ps;
	private PreparedStatement del_bill_ps;

	private PreparedStatement _bill_count_ps;
	private PreparedStatement mycur_bill_count_ps;
	private PreparedStatement _bill_rownum_ps;
	private PreparedStatement mycur_bill_rownum_ps;
	private PreparedStatement _bill_scope_ps;
	private PreparedStatement mycur_bill_scope_ps;

	private PreparedStatement clear_end_ps;
	private PreparedStatement clear_end_pay_ps;

	private int allCount;
	private MakeBillsParas paras;

	public MakeBillsBizComm(MakeBillsParas paras) throws DealDataException {
		try {
			this.paras = paras;
			this.conn = paras.getConn();
		} catch (Exception e1) {
			throw new DealDataException("initialize DealDataBizComm error!!",
					e1);
		}
	}
	/**
	 * 跑批前清理Local还款数据
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	public void clearLocalPayData() throws DealDataException {
		try {
			_bill_ps = conn.prepareStatement(_bill_update_m);

			_bill_ps.setInt(1, GlobalParas.getIn_cycle_day());
			_bill_ps.execute();
			batchCommit();
		} catch (Exception e) {
			try {
				rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new DealDataException("clearData errer!", e);
		} finally {
			try {
				closePS(_bill_ps);
			} catch (SQLException e) {
				throw new DealDataException("clearData errer!", e);
			}
		}
	}
	/**
	 * 跑批前清理Local数据
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	public void clearLocalData() throws DealDataException {
		try {
			_bill_ps = conn.prepareStatement(_bill_sql);

			_bill_ps.setInt(1, GlobalParas.getIn_cycle_day());
			_bill_ps.execute();
			batchCommit();
		} catch (Exception e) {
			try {
				rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new DealDataException("clearData errer!", e);
		} finally {
			try {
				closePS(_bill_ps);
			} catch (SQLException e) {
				throw new DealDataException("clearData errer!", e);
			}
		}
	}
	/**
	 * 跑批前清理数据
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	public void clearData() throws DealDataException {
		try {
			del_crntbill_ps = conn.prepareStatement(del_crntbill_sql);
			upd_crntbill_ps = conn.prepareStatement(upd_crntbill_sql);
			del_bill_ps = conn.prepareStatement(del_bill_sql);

			upd_COLL_M_AMOUNT_ps = conn.prepareStatement(upd_COLL_M_AMOUNT_sql);

			del_crntbill_ps.execute();

			upd_crntbill_ps.setInt(1, GlobalParas.getIn_cycle_day());
			upd_crntbill_ps.execute();

			upd_COLL_M_AMOUNT_ps.setInt(1, GlobalParas.getIn_cycle_day());
			upd_COLL_M_AMOUNT_ps.execute();

			del_bill_ps.setInt(1, GlobalParas.getIn_cycle_day());
			del_bill_ps.execute();
			batchCommit();
		} catch (Exception e) {
			try {
				rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new DealDataException("clearData errer!", e);
		} finally {
			try {
				closePS(del_crntbill_ps);
				closePS(upd_crntbill_ps);
				closePS(del_bill_ps);
				closePS(upd_COLL_M_AMOUNT_ps);
			} catch (SQLException e) {
				throw new DealDataException("clearData errer!", e);
			}
		}
	}

	/**
	 * 跑批后清理数据
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	public void clearDataEnd() throws DealDataException {
		try {
			clear_end_ps = conn.prepareStatement(clear_end_sql);
			clear_end_pay_ps = conn.prepareStatement(clear_end_pay_sql);
			clear_end_ps.execute();
			clear_end_pay_ps.execute();
			batchCommit();
		} catch (Exception e) {
			try {
				rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new DealDataException("clearData errer!", e);
		} finally {
			try {
				closePS(clear_end_ps);
				closePS(clear_end_pay_ps);
			} catch (SQLException e) {
				throw new DealDataException("clearData errer!", e);
			}
		}
	}

	/**
	 * 计算线程步长
	 * 
	 * @return
	 * @throws DealDataException
	 */
	public int[] getThreadStepLength() throws DealDataException {
		int stepLenTemp = allCount / Constants.DEALDATA_THREAD_NUM;
		if (allCount % Constants.DEALDATA_THREAD_NUM > 0) {
			stepLenTemp += 1;
		}
		ResultSet customer_num_rs = null;
		ResultSet scope_rs = null;
		int[] stepLen = new int[Constants.DEALDATA_THREAD_NUM];
		try {
			String temp = "0";
			for (int i = 0; i < Constants.DEALDATA_THREAD_NUM; i++) {
				String customer_num = null;
				int scope_count = 0;
				mycur_bill_rownum_ps = conn
						.prepareStatement(mycur_bill_rownum_sql);
				mycur_bill_rownum_ps.setInt(1, GlobalParas.getIn_cycle_day());
				if (i + 1 == Constants.DEALDATA_THREAD_NUM) {
					mycur_bill_rownum_ps.setInt(2, allCount);
				} else {
					mycur_bill_rownum_ps.setInt(2, (i + 1) * stepLenTemp);
				}
				customer_num_rs = mycur_bill_rownum_ps.executeQuery();

				while (customer_num_rs.next()) {
					customer_num = customer_num_rs.getString(1);
					break;
				}
				if (customer_num != null && !customer_num.equals(temp)) {
					mycur_bill_scope_ps = conn
							.prepareStatement(mycur_bill_scope_sql);
					mycur_bill_scope_ps
							.setInt(1, GlobalParas.getIn_cycle_day());
					mycur_bill_scope_ps.setString(2, temp);
					mycur_bill_scope_ps.setString(3, customer_num);
					scope_rs = mycur_bill_scope_ps.executeQuery();

					while (scope_rs.next()) {
						scope_count = scope_rs.getInt(1);
					}
					stepLen[i] = scope_count;
					temp = customer_num;
					if (scope_rs != null) {
						scope_rs.close();
						scope_rs = null;
					}
				} else {
					stepLen[i] = 0;
					continue;
				}
			}
		} catch (Exception e) {
			throw new DealDataException("getAllCount errer!", e);
		} finally {
			try {
				closeRS(customer_num_rs);
				closeRS(scope_rs);

				closePS(mycur_bill_rownum_ps);
				closePS(mycur_bill_scope_ps);
			} catch (SQLException e) {
				throw new DealDataException("clearData errer!", e);
			}
		}
		return stepLen;
	}
	
	/**
	 * 计算remote data线程步长
	 * 
	 * @return
	 * @throws DealDataException
	 */
	public int[] getThreadRStepLength(int type) throws DealDataException {
		String sql = "";
		String sqlT = "";
		int stepLenTemp = allCount / Constants.DEALDATA_THREAD_NUM;
		if (allCount % Constants.DEALDATA_THREAD_NUM > 0) {
			stepLenTemp += 1;
		}
		ResultSet customer_num_rs = null;
		ResultSet scope_rs = null;
		int[] stepLen = new int[Constants.DEALDATA_THREAD_NUM];
		try {
			String temp = "0";
			if(type==1){
				sql = _bill_rownum_sql;
				sqlT = _bill_scope_sql;
			}else if(type==2){
				sql = _bill_d_rownum_sql;
				sqlT = _bill_d_scope_sql;
			}
			for (int i = 0; i < Constants.DEALDATA_THREAD_NUM; i++) {
				String customer_num = null;
				int scope_count = 0;
				_bill_rownum_ps = conn.prepareStatement(sql);
				_bill_rownum_ps.setInt(1, GlobalParas.getIn_cycle_day());
				if (i + 1 == Constants.DEALDATA_THREAD_NUM) {
					_bill_rownum_ps.setInt(2, allCount);
				} else {
					_bill_rownum_ps.setInt(2, (i + 1) * stepLenTemp);
				}
				customer_num_rs = _bill_rownum_ps.executeQuery();

				while (customer_num_rs.next()) {
					customer_num = customer_num_rs.getString(1);
					break;
				}
				if (customer_num != null && !customer_num.equals(temp)) {
					_bill_scope_ps = conn.prepareStatement(sqlT);
					_bill_scope_ps.setInt(1, GlobalParas.getIn_cycle_day());
					_bill_scope_ps.setString(2, temp);
					_bill_scope_ps.setString(3, customer_num);
					scope_rs = _bill_scope_ps.executeQuery();

					while (scope_rs.next()) {
						scope_count = scope_rs.getInt(1);
					}
					stepLen[i] = scope_count;
					temp = customer_num;
					if (scope_rs != null) {
						scope_rs.close();
						scope_rs = null;
					}
				} else {
					stepLen[i] = 0;
					continue;
				}
			}
		} catch (Exception e) {
			throw new DealDataException("getAllCount errer!", e);
		} finally {
			try {
				closeRS(customer_num_rs);
				closeRS(scope_rs);

				closePS(_bill_rownum_ps);
				closePS(_bill_scope_ps);
			} catch (SQLException e) {
				throw new DealDataException("clearData errer!", e);
			}
		}
		return stepLen;
	}


	/**
	 * 查询待处理数据总数
	 * 
	 * @return
	 * @throws DealDataException
	 */
	public int queryAllCount() throws DealDataException {
		ResultSet rs = null;
		allCount = 0;
		try {
			mycur_bill_count_ps = conn.prepareStatement(mycur_bill_count_sql);
			mycur_bill_count_ps.setInt(1, GlobalParas.getIn_cycle_day());
			rs = mycur_bill_count_ps.executeQuery();
			while (rs.next()) {
				allCount = rs.getInt(1);
			}
		} catch (Exception e) {
			throw new DealDataException("getAllCount errer!", e);
		} finally {
			try {
				closeRS(rs);
				closePS(mycur_bill_count_ps);
			} catch (SQLException e) {
				throw new DealDataException("clearData errer!", e);
			}
		}
		setAllCount(allCount);
		return allCount;
	}
	/**
	 * 查询远程数据总数
	 * 
	 * @return
	 * @throws DealDataException
	 */
	public int queryRemoteAllCount(int type) throws DealDataException {
		ResultSet rs = null;
		String sql = "";
		allCount = 0;
		try {
			if(type==0)
				return 0;
			//query account date data
			if(type==1){
				sql = _bill_count_sql;
			}else if(type==2){
				sql = _bill_d_count_sql;
			}
			_bill_count_ps = conn.prepareStatement(sql);
			_bill_count_ps.setInt(1, GlobalParas.getIn_cycle_day());
			rs = _bill_count_ps.executeQuery();
			while (rs.next()) {
				allCount = rs.getInt(1);
			}
		} catch (Exception e) {
			throw new DealDataException("getRemoteAllCount errer!", e);
		} finally {
			try {
				closeRS(rs);
				closePS(_bill_count_ps);
			} catch (SQLException e) {
				throw new DealDataException("clearData errer!", e);
			}
		}
		setAllCount(allCount);
		return allCount;
	}

	

	public void setAllCount(int allCount) {
		this.allCount = allCount;
	}

	public int getAllCount() {
		return allCount;
	}

}
