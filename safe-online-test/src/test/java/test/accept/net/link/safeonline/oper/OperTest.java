/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.accept.net.link.safeonline.oper;

import java.util.UUID;

import junit.framework.TestCase;
import test.accept.net.link.safeonline.AcceptanceTestManager;

import com.thoughtworks.selenium.Selenium;

public class OperTest extends TestCase {

	private AcceptanceTestManager acceptanceTestManager;

	private Selenium selenium;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.acceptanceTestManager = new AcceptanceTestManager();
		this.acceptanceTestManager.setUp();
		this.selenium = this.acceptanceTestManager.getSelenium();
	}

	@Override
	protected void tearDown() throws Exception {
		this.acceptanceTestManager.tearDown();
		super.tearDown();
	}

	public void testAdminLogonLogout() throws Exception {
		this.acceptanceTestManager.operLogon("admin", "admin");
		this.acceptanceTestManager.openOperWebApp("/applications.seam");
		this.acceptanceTestManager.logout();
	}

	public void testAddApplication() throws Exception {
		this.acceptanceTestManager.operLogon("admin", "admin");
		this.acceptanceTestManager.openOperWebApp("/applications.seam");
		this.acceptanceTestManager.clickButtonAndWait("add");

		String applicationName = "application-" + UUID.randomUUID().toString();
		this.acceptanceTestManager.fillInputField("name", applicationName);
		this.acceptanceTestManager.clickButtonAndWait("add");
		assertTrue(this.selenium.isTextPresent(applicationName));

		this.acceptanceTestManager.logout();
	}
}
