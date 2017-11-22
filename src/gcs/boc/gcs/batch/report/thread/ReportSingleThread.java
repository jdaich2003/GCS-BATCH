package boc.gcs.batch.report.thread;

import java.util.List;

public class ReportSingleThread implements Runnable{
	private List<String> list;
	private int i;
	public ReportSingleThread(List<String> list,int i){
		this.list=list;
		this.i=i;
	}
	
	public void run() {
		// TODO Auto-generated method stub
		test();
	}
	public void test(){
		list.add("thread"+i);
	}

}
