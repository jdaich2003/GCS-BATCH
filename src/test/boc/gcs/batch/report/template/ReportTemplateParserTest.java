package boc.gcs.batch.report.template;

import junit.framework.TestCase;

public class ReportTemplateParserTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetInstance() {
		try {
			ReportTemplateParser.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
