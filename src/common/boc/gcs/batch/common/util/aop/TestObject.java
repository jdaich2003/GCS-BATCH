package boc.gcs.batch.common.util.aop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import boc.gcs.batch.common.util.DataProcessManager;
import boc.gcs.batch.report.common.IReportReader;
import boc.gcs.batch.report.template.ReportTemplateLoader;

public class TestObject extends DataProcessManager implements IReportReader{
	
	public TestObject(String construct){
		super();
		System.out.println(construct);
	}
	
	public String test(String org){
		System.out.println(conn);
		return org+"finished";
	}
	
	public void read() throws Exception{
		PreparedStatement ps = null;
		try {
			String sql = "insert into gcs_transaction_info(gcs_transaction_id) values('sdfsdf')";
			ps = conn.prepareStatement(sql);
			ps.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}finally{
			try {
				if(ps!=null)
				ps.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			}
		}
	}

	public List<Map<String, Object>> getBodyContent() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, Object> getBottomContent() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, Object> getTitleContent() {
		// TODO Auto-generated method stub
		return null;
	}

	public void init(String fileName, ReportTemplateLoader loader) {
		// TODO Auto-generated method stub
		
	}

	public ReportTemplateLoader getLoader() {
		// TODO Auto-generated method stub
		return null;
	}

}
