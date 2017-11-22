package boc.gcs.batch.report.render;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import boc.gcs.batch.common.util.DataProcessManager;
import boc.gcs.batch.common.util.DateUtil;

public class ReportConverter extends DataProcessManager {

	private static ReportConverter reportConverter;

	private ReportConverter() {

	}

	public static ReportConverter getInstance() {
		if (reportConverter == null) {
			reportConverter = new ReportConverter();
		}
		return reportConverter;
	}

	public String convertByType(String type, String value) {
		if (type != null && type.toLowerCase().equals("bigdecimal")) {
			value = ReportConverter.getInstance().dealBigDecimal(value);
		} else if (type != null && type.toLowerCase().equals("integer")) {
			value = ReportConverter.getInstance().dealInteger(value);
		} else if (type != null && type.toLowerCase().equals("date")) {
			value = ReportConverter.getInstance().dealDate(value);
		} else if (type != null && type.toLowerCase().equals("sundate")) {
			value = ReportConverter.getInstance().dealSunDate(value);
		}
		return value;
	}

	/**
	 * 第1位是借贷标记位 填'C'或'D' 第2至13位是金额域,填清算金额值.
	 * 
	 * @param orig
	 * @return
	 */
	public String convertEIAmount(String orig) {
		if (StringUtils.isNotBlank(orig)) {
			orig = StringUtils.substring(orig, 1, orig.length());
		}
		return orig;
	}

	/***************************************************************************
	 * 获得当地时间 sysdate:system date time localedate:locale date time
	 * 
	 * @throws SQLException
	 * @throws ParseException
	 * @throws NumberFormatException
	 */
	public String getLocaleTime(String sysdate, String bhId) throws Exception {
		String localeDateTime = getlocaleDateTime(sysdate, bhId);
		return StringUtils.substring(localeDateTime, 0, 10);

	}

	/***************************************************************************
	 * 获得当地日期 sysdate:system date time localedate:locale date time
	 * 
	 * @throws SQLException
	 * @throws ParseException
	 * @throws NumberFormatException
	 */
	public String getLocaleDate(String sysdate, String bhId) throws Exception {
		String localeDateTime = getlocaleDateTime(sysdate, bhId);
		return StringUtils.substring(localeDateTime, 11, 19);
	}

	public String getlocaleDateTime(String sysdate, String bhId) {
		if (sysdate == null) {
			sysdate = DateUtil.getDateTime();
		} else {
			sysdate = DateUtil.getDateTime();

		}
		String localedate = null;
		conn = super.getConn();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		String getBh = "select * from igcs_branch t where t.branch_id = ?";
		String getSum = "select * from igcs_branch t where t.branch_id =? "
				+ "and t.summer_time_begin_date<? and t.summer_time_end_date>?";
		try {
			ps = conn.prepareStatement(getBh);
			ps.setString(1, bhId);
			rs = ps.executeQuery();
			String timeZoneId = "";
			String summerFlag = "";
			String summerHour = "";
			if (rs.next()) {
				timeZoneId = rs.getString("timezone");
				summerFlag = rs.getString("summer_flag");
				summerHour = rs.getString("summer_hour");
			}
			// 得到中国地区的时间字符串
			// String sysdate = DateUtil.getDateTime();
			// 得到当地地区的时间字符串
			localedate = DateUtil.string2TimezoneDefault(sysdate, timeZoneId);

			if (summerFlag != null && summerFlag.equals("1")) {
				ps2 = conn.prepareStatement(getSum);
				ps2.setString(1, bhId);
				ps2.setString(2, sysdate);
				ps2.setString(3, sysdate);
				rs2 = ps2.executeQuery();
				if (rs2.next() && summerHour != null
						&& !summerHour.trim().equals("")) {
					localedate = DateUtil.getCutTime(localedate, Integer
							.valueOf(summerHour).intValue());
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				closeRS(rs);
				closePS(ps);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return localedate;
	}

	/**
	 * 金额字段处理
	 * 
	 * @param orig
	 * @return
	 */
	public String dealBigDecimal(String orig) {
		if (StringUtils.isNotEmpty(orig)) {
			BigDecimal result = new BigDecimal(new BigInteger(orig), 2);
			return result.toString();
		} else {
			return orig;
		}

	}

	/**
	 * 加小数点位并去掉前面的0
	 * 
	 * @param orig
	 * @return
	 */
	public String dealInteger(String orig) {
		if (StringUtils.isNotEmpty(orig)) {
			Integer dou = Integer.valueOf(orig);
			return String.valueOf(dou);
		} else {
			return "";
		}
	}

	/**
	 * 把报文中20100101型日期转换为2010-01-01型
	 * 
	 * @param orig
	 * @return
	 */
	public String dealDate(String orig) {
		if (StringUtils.isNotEmpty(orig)) {
			String noZeroOrig = String.valueOf(new Integer(orig));
			StringBuilder sb = new StringBuilder(noZeroOrig);

			if (noZeroOrig.indexOf("-") < 0 && noZeroOrig.length() == 8) {
				sb.insert(4, "-");
				sb.insert(7, "-");
			} else if (noZeroOrig.length() == 7) {
				sb.insert(4, "-0");
				sb.insert(7, "-");
			} else if (noZeroOrig.length() == 6) {
				sb.insert(4, "-0");
				sb.insert(6, "-0");
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	/**
	 * 汇率转换（前5后5）
	 * 
	 * @param transaction_exchange_rate
	 * @return
	 */
	public String getRate(String orig) {
		if (StringUtils.isNotEmpty(orig)) {
			BigDecimal result = new BigDecimal(new BigInteger(orig), 5);
			return result.toString();
		} else {
			return "";
		}
	}

	/**
	 * 转换太阳日
	 * 
	 * @param sunDate
	 * @return
	 */
	public String dealSunDate(String orig) {
		if (StringUtils.isNotEmpty(orig)) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Calendar calendar = Calendar.getInstance();
			orig = Integer.valueOf(orig).toString();
			calendar.set(Calendar.YEAR, Integer.valueOf(orig.substring(0, 4))
					.intValue());
			calendar.set(Calendar.DAY_OF_YEAR, Integer.valueOf(
				orig.substring(4, 7)).intValue());
			Date date = calendar.getTime();
			return df.format(date);
		} else {
			return "";
		}
	}
	
	//国际化
	public String getLocaleTransLan(String id) {
		conn = super.getConn();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select * from gcs_trans_lan l where l.id = ? and l.locale_lan = 'en_US'";
		String lanValue= null;
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, id);
			rs = ps.executeQuery();	
			if(rs.next()){
				lanValue = rs.getString("lan_value");
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return lanValue;
	}
	
	public static void main(String[] args) throws Exception {
		ReportConverter rc = ReportConverter.getInstance();
		Connection conn = rc.getConn();
		String sql = "select sysdate from dual";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		String sysdate = "";
		if (rs.next()) {
			sysdate = rs.getTime(1).toString();
		}
		System.out.println(rc.getLocaleDate(sysdate, "4850000000"));
	}
}
