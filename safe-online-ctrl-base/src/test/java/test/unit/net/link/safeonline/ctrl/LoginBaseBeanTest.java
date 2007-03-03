/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ctrl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.ctrl.bean.LoginBaseBean;
import junit.framework.TestCase;

public class LoginBaseBeanTest extends TestCase {

	private LoginBaseBean testedInstance;

	private static final Log LOG = LogFactory.getLog(LoginBaseBeanTest.class);

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new LoginBaseBean("test-application");
	}

	public void testGetOverviewTargetUrl() throws Exception {
		// setup
		String requestUrl = "http://localhost:8080/safe-online-oper/main.seam";

		// operate
		String result = this.testedInstance.getOverviewTargetUrl(requestUrl);

		// verify
		LOG.debug("result: " + result);
		assertEquals("http://localhost:8080/safe-online-oper/overview.seam",
				result);
	}
}
