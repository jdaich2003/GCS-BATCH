package boc.gcs.batch.coll;

import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;

import boc.gcs.batch.coll.dealdata.GlobalParas;
import boc.gcs.batch.coll.thread.MakeBillsMainThread;
import boc.gcs.batch.coll.thread.ThreadContext;
import boc.gcs.batch.common.db.C3P0ConnectionProvider;
import boc.gcs.batch.common.util.Constants;
import boc.gcs.batch.common.util.PropertyUtil;
/**
 * 启动make bills批量
 * @author daic
 *
 */
public class MakeBillsMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			
			//初始化数据库
			C3P0ConnectionProvider.getInstance("coll");
			ThreadContext context = new ThreadContext();
			//记录跑批类型
			context.setBatchType(Constants.MAKEBILLS_BATCH_PREFIX);
			//记录跑批时间
			SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			String timeStamp = f.format(System.currentTimeMillis()).toString();
			context.setTimestamp(timeStamp);
			//设置批次号
			context.setBatchNo("1");
			String key = "cycle_day_make";
			String filePath = MakeBillsMain.class.getResource("/batch.properties").getPath();
	    	PropertyUtil proUtil =  PropertyUtil.getInstance(filePath);
//	    	proUtil.getCycle_day("cycle_day_catch");
	    	String cycle_day = proUtil.getValue(key);
	    	System.out.println(cycle_day);
			GlobalParas.setIn_cycle_day(cycle_day!=null?Integer.valueOf(cycle_day):0);
//			GlobalParas.setIn_cycle_day(Integer.valueOf(StringUtils.substring(timeStamp, 6, 8)));
			new Thread(new MakeBillsMainThread(context)).run();
			proUtil.updateCycle_day(key);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
