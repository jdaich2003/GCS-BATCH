package boc.gcs.batch.coll.dealdata;

import java.sql.SQLException;

import junit.framework.TestCase;
import boc.gcs.batch.coll.exception.DealDataException;
import boc.gcs.batch.common.db.C3P0ConnectionProvider;

public class MakeBillsBizCommTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetThreadStepLength() {
		try {
			GlobalParas.setIn_cycle_day(10);
			MakeBillsParas parasMain = new MakeBillsParas();
			parasMain.setConn(C3P0ConnectionProvider.getInstance("").getConnection());
			MakeBillsBizComm ddbc = new MakeBillsBizComm(parasMain);
			ddbc.setAllCount(172);
			int[] stepLen = ddbc.getThreadStepLength();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
