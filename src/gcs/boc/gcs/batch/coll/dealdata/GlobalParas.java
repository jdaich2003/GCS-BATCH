package boc.gcs.batch.coll.dealdata;
/**
 * 
 * @author daic
 *
 */
public class GlobalParas {
	private static int in_cycle_day;
	
	private static int batch_size;
	
	public static String[] CBC_BILL =	new String[]{"BILL_ID","CARDID","CIFSN","ACCOUNT_NUM",
					"APP_CODE",
					"M_TYPE",
					"ACCOUNT_TYPE",
					"CUSTOMER_NUM",
					"CYCLE_DAY",
					"DELINQUENT_DAYS",
					"INSTITUTION",
					"ZIP_CODE",
					"TEL_CAUSE",
					"MIN_PAY_AMOUNT",
					"TOT_PAY_AMOUNT",
					"END_DATE",
					"DUE_DAY",
					"CREDIT_LIMIT",
					"CUR_BALANCE",
					"PAY_AMOUNT",
					"DISPUT_AMOUNT",
					"SATISFIED_FLAG",
					"CASH_BC",
					"RTL_BC",
					"INTL_BC",
					"CRD_BC",
					"TOTALINTEREST",
					"S_CUSTOMER_NUM",
					"GTR_CUSTOMER_NUM",
					"END_FLAG",
					"BACK_FLAG",
					"COLLECT_STATU",
					"COLLECTOR_ID",
					"HIGH_RISK_FLAG",
					"ACCOUNT_DATE",
					"CUR_BANK_CODE",
					"PERIOD",
					"BILL_STATE",
					"SBACK_FLAG",
					"PRODUCT_ID",
					"CARDTYPE_ID",
					"SHIGH_RISK_FLAG",
					"IDLE_FLAG",
					"LASTCALL_DATE",
					"NEXTCALL_DATE",
					"IDLE_REASON",
					"BACK_REASON",
					"MAINACCOUNT_FLAG",
					"BCHID",
					"GROUP_CODE",
					"HOST_COLLECTOR_ID",
					"ORIGINAL_MINI_PAY",
					"ORIGINAL_TOTAL_PAY",
					"PRODUCT",
					"VIPTYP",
					"AUTOPAY_MTYPE",
					"AUTOPAY_FLAG",
					"AUTOPAY_MODE",
					"AUTOPAY_ACCOUNT",
					"APPLY_CODE",
					"CONSIGN_ID",
					"CON_TIME",
					"BIGFLAG",
					"BIGMONEY",
					"PROCESS_CODE",
					"OVRLMT_PERCENT",
					"OVRLMT_AMT",
					"MON_OPEN",
					"CARDS_COUNTER",
					"LSTUPDDAT"};
	public static String[] IGCS_COLL_M_AMOUNT =	new String[]{
		"CUNSTOMER_NUM",
		"M_TYPE",
		"MIN_PAY_AMOUNT",
		"TOT_PAY_AMOUNT",
		"PAY_AMOUNT",
		"PAY_DATE",
		"ACPT_PAY_AMOUNT",
		"ORING_MINI_AMOUNT",
		"ORING_TOT_AMOUNT",
		"BCH_ID",
		"CYCLE_DAY"
	};
	
	public static int getBatch_size() {
		return batch_size;
	}

	public static void setBatch_size(int batchSize) {
		batch_size = batchSize;
	}

	public static int getIn_cycle_day() {
		return in_cycle_day;
	}

	public static void setIn_cycle_day(int in_cycle_day) {
		GlobalParas.in_cycle_day = in_cycle_day;
	}
}
