package boc.gcs.batch.common.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.TestCase;

public class PsParamsTest extends TestCase {
	private Connection conn;

	protected void setUp() throws Exception {
		super.setUp();
		// conn = C3P0ConnectionProvider.getInstance().getConnection();
		Class.forName("oracle.jdbc.driver.OracleDriver");
		conn = DriverManager.getConnection(
			"jdbc:oracle:thin:@127.0.0.1:1521:daic", "gcs", "gcs");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testPs() throws SQLException {
		try {
			/*PreparedStatement ps1 = conn
					.prepareStatement("select * from gcs_transaction_info where transaction_firit_branch_id=?");
			ParameterMetaData pmd = ps1.getParameterMetaData();
			for (int j = 0; j < pmd.getParameterCount(); j++) {
				System.out.println(pmd.getParameterTypeName(j));
			}*/
			
			PreparedStatement ps1 = conn.prepareStatement("select 1 from gcs_transaction_info_iss where 1=0");
			ResultSet rs = ps1.executeQuery();
			if(rs.next()){
				System.out.println("sdfsdf");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
