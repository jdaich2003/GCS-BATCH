package boc.gcs.batch.coll.thread;

import boc.gcs.batch.common.thread.CollThreadExcecutor;

public class TestDealData {

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CollThreadExcecutor executor = CollThreadExcecutor.getInstance();
		for (int i = 0; i < 10; i++) {
			executor.execute(new Thread(){
				public void run(){
					try {
						System.out.println("child thread waiting");
						sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
		while(executor.getActiveCount()>0){
			System.out.println("main thread waiting");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("main thread over");
		System.exit(0);
	}

}
