package boc.gcs.batch.common.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import boc.gcs.batch.common.util.Constants;
/**
 * 线程池
 * @author daic 
 *
 */
public class CollThreadExcecutor extends ThreadPoolExecutor {
	private static int corePoolSize = Constants.CORE_POOL_SIZE;
	private static int maximumPoolSize = Constants.MAXIMUM_POOL_SIZE;
	private static long keepAliveTime = Constants.KEEP_ALIVE_TIME;
	private static BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(
			Constants.WORK_QUEUE_NUM);
	private static TimeUnit tu = TimeUnit.SECONDS;
	private RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardOldestPolicy();

	private CollThreadExcecutor() {
		super(corePoolSize, maximumPoolSize, keepAliveTime, tu, workQueue);
	}

	private static CollThreadExcecutor excecutor = new CollThreadExcecutor();

	public static CollThreadExcecutor getInstance() {
		return excecutor;
	}
}
