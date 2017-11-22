package boc.gcs.batch.coll.thread;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import boc.gcs.batch.coll.dealdata.MakeBillsBiz;
import boc.gcs.batch.coll.thread.ThreadAbs;
import boc.gcs.batch.coll.thread.ThreadContext;

/**
 * 数据处理线程 
 * @author daic
 *
 */
public class MakeBillsSingleThread extends ThreadAbs implements Runnable{
	public MakeBillsSingleThread(ThreadContext context){
		try {
			logger = Logger.getLogger(MakeBillsSingleThread.class);
			init(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void run() {
		try {
			MakeBillsBiz ddb = new MakeBillsBiz(context.getParas());
			ddb.execute();
		} catch (Exception e) {
			try {
				endErrReport(e.getMessage());
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			logger.error(e);
		} finally {
			try {
				endReport();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
