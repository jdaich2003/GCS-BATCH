package boc.gcs.batch.coll.dealdata;

import junit.framework.TestCase;
import boc.gcs.batch.common.db.C3P0ConnectionProvider;

public class MakeBillsBizTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testExecute() {
		try {
			MakeBillsParas parasMain = new MakeBillsParas();
			parasMain.setConn(C3P0ConnectionProvider.getInstance("").getConnection());
			MakeBillsBizComm ddbc = new MakeBillsBizComm(parasMain);
			
			MakeBillsParas paras = new MakeBillsParas();
			paras.setConn(C3P0ConnectionProvider.getInstance("").getConnection());
			paras.setStart(0);
			paras.setEnd(20);
			paras.setPayList(null);
			GlobalParas.setIn_cycle_day(20);
			MakeBillsBiz biz = new MakeBillsBiz(paras);
			biz.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}

}
