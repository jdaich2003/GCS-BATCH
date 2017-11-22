package boc.gcs.batch.report.thread;

import java.util.ArrayList;
import java.util.List;

public class ReportMainThread implements Runnable{
	private  List<String> list;
	public void test() throws Exception{
		list = new ArrayList<String>();
		for (int i = 0; i < 20; i++) {
			Thread t = new Thread(new ReportSingleThread(list,i+1));
			t.run();
			
		}
		Thread.sleep(1000);
		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i)); 
		}
	}
	public void run() {
		// TODO Auto-generated method stub
		try {
			test();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
