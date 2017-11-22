package boc.gcs.batch.coll.thread;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import boc.gcs.batch.coll.dealdata.MakeBillsBizComm;
import boc.gcs.batch.coll.dealdata.MakeBillsParas;
import boc.gcs.batch.coll.dealdata.PayOfBillObject;
import boc.gcs.batch.coll.exception.DealDataException;
import boc.gcs.batch.common.thread.CollThreadExcecutor;
import boc.gcs.batch.common.util.Constants;

/**
 * 数据处理主线程
 * 
 * @author daic 
 * 
 */
public class MakeBillsMainThread extends ThreadAbs implements  Runnable {
	public MakeBillsMainThread(ThreadContext context){
		try {
			logger = Logger.getLogger(MakeBillsMainThread.class);
			init(context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void run() {
		CollThreadExcecutor executor = CollThreadExcecutor.getInstance();
		try {
			//构造主处理类
			MakeBillsBizComm ddbc = new MakeBillsBizComm(context.getParas());
			//查询待处理总量
			int allCount = ddbc.queryAllCount();
			if(allCount==0){
				logger.error(new DealDataException("no data to deal with!"));
				return;
			}
			//step 1 清理数据
			ddbc.clearData();
			//step 2 根据customer_num设置线程处理量和步长
			int[] stepLen = ddbc.getThreadStepLength(); 
			
			//step 3 多线程处理数据
			int tempStart=0;
//			//初始化账户额度信息
//			List<PayOfBillObject> payList = ddbc.getMtypeDict();
			for (int i = 0; i < stepLen.length; i++) {
				ThreadContext contextSingle = new ThreadContext();
				MakeBillsParas parasSingle = new MakeBillsParas();
				parasSingle.setStart(tempStart);
				parasSingle.setEnd(tempStart+stepLen[i]);
				tempStart+=stepLen[i];
//				parasSingle.setPayList(payList);
				contextSingle.setThreadNo(String.valueOf(i+1));
				contextSingle.setDataCount(stepLen[i]);
				contextSingle.setParas(parasSingle);
				contextSingle.setBatchNo(context.getBatchNo());
				contextSingle.setBatchType(Constants.MAKEBILLS_BATCH_PREFIX);
				contextSingle.setTimestamp(context.getTimestamp());
				executor.execute(new MakeBillsSingleThread(contextSingle));
			}
			
			//等待子线程结束
			while(executor.getActiveCount()>0){
//				System.out.println("main thread waiting");
			}
			//step 4 跑批结束清理数据
			ddbc.clearDataEnd();
			context.setDataCount(allCount);
			
		} catch (Exception e) {
			setErrString(e.getMessage());
			logger.error(e.getMessage(),e);
		} finally {
			if (executor != null) {
				executor.shutdown();
			}
			try {
				endReport();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public ThreadContext getContext() {
		return context;
	}
	public void setContext(ThreadContext context) {
		this.context = context;
	}
	
}
