package boc.gcs.batch.common.util;


/**
 * 常量
 * @author daic
 *
 */
public class Constants {
	//线程池参数
	public static final int CORE_POOL_SIZE   =20 ;      
	public static final int MAXIMUM_POOL_SIZE =30;
	public static final long KEEP_ALIVE_TIME   =99999;
	public static final int WORK_QUEUE_NUM    =5;
	//线程个数
	public static final int DEALDATA_THREAD_NUM = 10;
	//批量提交步长
	public static final int BATCH_COMMIT_COUNT = 1000;
	//批量类型
	public static final String MAKEBILLS_BATCH_PREFIX = "MakeBills";
	public static final String CATCHBILLS_BATCH_PREFIX = "CatchBills";
	public static final String UPDATEBILLS_BATCH_PREFIX = "UpdateBills";
	//加工催收数据时访问gcs用户数据
	public static final String GCS_PREFIX = "gcs_uat.";
//	//加工催收数据数据库前缀
//	public static final String COLL_DB_PREFIX = "coll_";
//	//加工报表数据库前缀
//	public static final String GCS_REPORT_DB_PREFIX = "report_";
}
