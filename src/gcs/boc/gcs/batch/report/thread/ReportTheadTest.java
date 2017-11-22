package boc.gcs.batch.report.thread;

public class ReportTheadTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Thread t = new Thread(new ReportMainThread());
		t.run();
	}

}
