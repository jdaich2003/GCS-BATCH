package com.cms.workflow.template;

import com.boc.workflow.template.WFTemplateParser;

import junit.framework.TestCase;

public class WFTemplateParserTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetConfig() {
		WFTemplateParser parser = WFTemplateParser.getInstance();
		parser.getConfig("workflow_template_cms.xml");
	}

}
